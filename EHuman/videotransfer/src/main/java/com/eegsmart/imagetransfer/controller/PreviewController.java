package com.eegsmart.imagetransfer.controller;

import android.app.Activity;
import android.media.Image;
import android.util.Log;

import com.eegsmart.imagetransfer.VTConstants;
import com.eegsmart.imagetransfer.VideoTransfer;
import com.eegsmart.imagetransfer.business.NativeDecoder;
import com.eegsmart.imagetransfer.jni.PreviewJNI;
import com.eegsmart.imagetransfer.listener.NativeDecoderListener;
import com.eegsmart.imagetransfer.listener.PreviewListener;
import com.eegsmart.imagetransfer.listener.TakePictureListener;
import com.eegsmart.imagetransfer.model.PreviewResolutionMode;
import com.eegsmart.imagetransfer.util.ConverterUtil;
import com.eegsmart.imagetransfer.util.WiFiUtil;
import com.eegsmart.imagetransfer.view.GLFrameRenderer;
import com.eegsmart.imagetransfer.view.GLFrameSurface;

import java.nio.ByteBuffer;

import static com.eegsmart.imagetransfer.VideoTransfer.onFrameCount;

/**
 * 图传预览控制器
 * Created by lidongxing on 2017/9/4.
 */
public class PreviewController implements PreviewListener {
    private static final String TAG = "PreviewController";

    private static final byte[] SPS = {(byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x67, (byte) 0x4d, (byte) 0x00, (byte) 0x1f, (byte) 0xe5, (byte) 0x40, (byte) 0x28, (byte) 0x02, (byte) 0xd8, (byte) 0x80};
    private static final byte[] PPS = {(byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x68, (byte) 0xee, (byte) 0x31, (byte) 0x12};

    private NativeDecoder decoder;

    private boolean isPreviewing;

    private Activity activity;
    private GLFrameSurface frameSurface;
    private GLFrameRenderer renderer;

    private OnEvent onEventListener;

    private int width = 0;
    private int height = 0;
    private byte[] spsBuffer = null;
    private byte[] ppsBuffer = null;

    private boolean isUseSoftwareDecodeFrame = false;

    private static final PreviewController ourInstance = new PreviewController();

    public static PreviewController getInstance() {
        return ourInstance;
    }

    private PreviewController() {
        isPreviewing = false;

        decoder = new NativeDecoder();

        PreviewJNI.setPreviewListener(this);
    }

    public void setOnEventListener(OnEvent onEventListener) {
        this.onEventListener = onEventListener;
    }

    /**
     * 设置预览视频渲染画板
     *
     * @param activity 预览所在activity
     * @param surface  预览视频渲染画板【OpenGL画板】
     */
    public void setSurface(Activity activity, GLFrameSurface surface,
                           int vrLeftRightDimenResID,
                           int vrSpaceCenterDimenResID) {
        this.activity = activity;
        this.frameSurface = surface;

        if (surface.hasRenderer()) {
            renderer = surface.getRenderer();
        } else {
            renderer = new GLFrameRenderer(surface, vrLeftRightDimenResID, vrSpaceCenterDimenResID);
            surface.setRenderer(renderer);
        }

        if (width > 0 && height > 0) {
            renderer.update(width, height);
        }
    }

    /**
     * 是否已开启预览
     *
     * @return true:已开启; false:未开启
     */
    public boolean isPreviewing() {
        return isPreviewing;
    }

    /**
     * 开启预览
     */
    public void startPreview() {
        synchronized (ourInstance) {
            if (!isPreviewing) {
                isPreviewing = true;
                String ip = WiFiUtil.getRouterIP(activity);
                Log.d(TAG, "start preview ip " + ip);
                PreviewJNI.init(ip);
            } else {
                Log.e(TAG, "startPreview() - isPreviewing");
            }
        }
    }

    /**
     * 结束预览
     */
    public void stopPreview() {
        if (isPreviewing) {
            Log.d(TAG, "SDL stop recording");

            isPreviewing = false;

            PreviewJNI.shutDown();

            Log.d(TAG, "SDL top end");

            setSurfaceVisible(false);
        }
    }

    /**
     * 拍照【本地拍照处理】
     *
     * @param takePicPath 照片本地存储路径
     * @param listener    拍照结果监听处理器
     */
    public void takePicture(final String takePicPath, final TakePictureListener listener) {
        if (takePicPath == null) {
            if (listener != null) {
                listener.onFailed();
            }

            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                //避免多次拍照一起导致的问题
                while (PreviewJNI.isTakingPicture()) {
                    try {
                        Thread.sleep(300);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                boolean result = PreviewJNI.takePicture(takePicPath);

                if (listener != null) {
                    if (result) {
                        listener.onSuccess();
                    } else {
                        listener.onFailed();
                    }
                }
            }
        }).start();
    }

    /**
     * 开始录像【本地录像处理】
     *
     * @param recordFilePath 录像视频本地存储路径
     */
    public void startRecord(String recordFilePath) {
        PreviewJNI.recordStart(recordFilePath);
    }

    /**
     * 结束录像【本地录像处理】
     */
    public void stopRecord() {
        PreviewJNI.recordStop();
    }

    @Override
    public void onSPSFrame(byte[] buf, int width, int height) {
        this.width = width;
        this.height = height;

        if (renderer != null) {
            renderer.update(width, height);
        }

        if (height == 720) {
            VideoTransfer.getVideoTransfer().setPreviewResolutionMode(PreviewResolutionMode.R_720P);
            this.isUseSoftwareDecodeFrame = false;
        } else {
            VideoTransfer.getVideoTransfer().setPreviewResolutionMode(PreviewResolutionMode.R_VGA);
            this.isUseSoftwareDecodeFrame = false; // es debug 暂时不适用底层软解
//            this.isUseSoftwareDecodeFrame = true;
        }

        //设置是否使用底层软解
        PreviewJNI.setDecodeFrame(isUseSoftwareDecodeFrame);

        spsBuffer = new byte[buf.length + 3];
        spsBuffer[0] = 0;
        spsBuffer[1] = 0;
        spsBuffer[2] = 1;

        System.arraycopy(buf, 0, spsBuffer, 3, buf.length);
    }

    @Override
    public void onPPSFrame(byte[] buf) {
        ppsBuffer = new byte[buf.length + 3];
        ppsBuffer[0] = 0;
        ppsBuffer[1] = 0;
        ppsBuffer[2] = 1;

        System.arraycopy(buf, 0, ppsBuffer, 3, buf.length);

        setSurfaceVisible(true);
    }

    private NativeDecoderListener listener = new NativeDecoderListener() {
        private long lastTime = 0;
        private long MIN_DIFF = 3;

        @Override
        public void onDataDecoded(ByteBuffer data, int colorFormat) {
            synchronized (this) {
                long diff = System.currentTimeMillis() - lastTime;
                if (diff > MIN_DIFF) {
                    Log.d(TAG, "onDataDecoded of format: " + colorFormat);
                    lastTime = System.currentTimeMillis();
                    final byte[] nv21 = renderer.update(data, width, height, colorFormat);
                    if (null != onFrameDataListener) {
                        onFrameDataListener.onNv21Data(nv21, width, height);
                    }
                    Log.d(TAG, "onDataDecoded after.  nv21.length==" + nv21.length);
                } else {
                    android.util.Log.d(TAG, "listener onDataDecoded: skip this " + diff);
                }
            }
        }

        @Override
        public void onImage(Image image) {
            notifyFrameImage(image); // 继续向外发送image
        }
    };

    private static byte[] getNv21Data(ByteBuffer data, int width, int height, int colorFormat) {
        int yVerticalSpan = height;
        int uvVerticalSpan = height;
        int verticalStride = height;
        int yHorizontalStride = width;
        int uvHorizontalStride = width;
        switch (colorFormat) {
            case 19://COLOR_FormatYUV420Planar
                yHorizontalStride = width;
                yVerticalSpan = height;
                uvHorizontalStride = yHorizontalStride / 2;
                uvVerticalSpan = yVerticalSpan;
                break;
            case 21://COLOR_FormatYUV420SemiPlanar
                yHorizontalStride = width;
                yVerticalSpan = height;
                uvHorizontalStride = yHorizontalStride;
                uvVerticalSpan = yVerticalSpan / 2;
                break;
            case 0x7FA30C03://OMX_QCOM_COLOR_FormatYUV420PackedSemiPlanar64x32Tile2m8ka
                int hAlign = 64;
                int vAlign = 32;
                yHorizontalStride = width % hAlign > 0 ? hAlign * (width / hAlign + 1) : width;
                yVerticalSpan = height % vAlign > 0 ? vAlign * (height / vAlign + 1) : height;
                uvHorizontalStride = yHorizontalStride;
                uvVerticalSpan = yVerticalSpan / 2;
                break;
            case 0x7FA30C04://COLOR_QCOM_FormatYUV420SemiPlanar32m
                hAlign = 128;
                vAlign = 32;
                yHorizontalStride = width % hAlign > 0 ? hAlign * (width / hAlign + 1) : width;
                yVerticalSpan = height % vAlign > 0 ? vAlign * (height / vAlign + 1) : height;
                uvHorizontalStride = yHorizontalStride;
                uvVerticalSpan = yVerticalSpan / 2;
                break;
        }

        int ySize = yHorizontalStride * yVerticalSpan;
        int uvSize = uvHorizontalStride * uvVerticalSpan;
        byte[] nv21 = new byte[ySize + uvSize];
        data.position(0);
        data.get(nv21, 0, ySize);
        data.get(nv21, ySize, uvSize);
        return nv21;
    }

    @Override
    public void onFrame(byte[] buf) {
        synchronized (ourInstance) {
            if (!decoder.isCodecCreated()) {
                Log.d(TAG, Thread.currentThread().toString() + " onFrame, create codec of sps : " + ConverterUtil.bytes2Hex(spsBuffer));
                decoder.createCodec(
                        listener,
                        spsBuffer == null ? SPS : spsBuffer,
                        ppsBuffer == null ? PPS : ppsBuffer,
                        spsBuffer == null ? VTConstants.WIDTH : width,
                        spsBuffer == null ? VTConstants.HEIGHT : height
                );
            }

            if (isPreviewing) {
//                Log.d(TAG, "onFrame, add data frame, isUseSoftwareDecodeFrame is " + isUseSoftwareDecodeFrame);
                if (isUseSoftwareDecodeFrame) {
                    if (renderer != null) {
                        ByteBuffer data = ByteBuffer.allocate(buf.length);
                        data.put(buf);
                        renderer.update(data, width, height, 19);
                    }
                } else {
                    byte[] dataBuffer = new byte[buf.length + 3];
                    dataBuffer[0] = 0;
                    dataBuffer[1] = 0;
                    dataBuffer[2] = 1;
                    onFrameCount++;
                    System.arraycopy(buf, 0, dataBuffer, 3, buf.length);
                    decoder.addData(dataBuffer);
                }
            } else {
                Log.e(TAG, "onFrame, is not previewing.");
            }
        }
    }

    @Override
    public void playResult(int result) {
        Log.d(TAG, "playResult:" + result);

        if (result != 0) {
            Log.d(TAG, "result err, stop preview!");
            stopPreview();
        }
    }

    private void setSurfaceVisible(final boolean visible) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (frameSurface != null) {
                    frameSurface.setAlpha(visible ? 1f : 0f);
                }
            }
        });
    }

    private void notifyFrameImage(Image image) {
        if (null != onEventListener) {
            onEventListener.onFrameImage(image);
        }
    }

    /**
     * 控制器向外发送消息
     */
    public interface OnEvent {

        /**
         * @param image 解码得到的image文件  使用完后可以释放
         */
        void onFrameImage(Image image);
    }

    private OnFrameDataListener onFrameDataListener;

    public void setOnFrameDataListener(OnFrameDataListener onFrameDataListener) {
        this.onFrameDataListener = onFrameDataListener;
    }

    public interface OnFrameDataListener {
        /**
         * @param nv21 解码器直接输出的数据
         */
        void onNv21Data(final byte[] nv21, final int width, final int height);
    }

}
