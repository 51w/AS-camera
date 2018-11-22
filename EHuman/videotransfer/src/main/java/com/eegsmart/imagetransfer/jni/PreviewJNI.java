package com.eegsmart.imagetransfer.jni;


import android.util.Log;

import com.eegsmart.imagetransfer.listener.PreviewListener;

public class PreviewJNI {
    private static final String TAG = "Live555Jni";

    static {
        System.loadLibrary("avutil-55");
        System.loadLibrary("swscale-4");
        System.loadLibrary("swresample-2");
        System.loadLibrary("avcodec-57");
        System.loadLibrary("avformat-57");
        System.loadLibrary("avfilter-6");
        System.loadLibrary("gnustl_shared");
        System.loadLibrary("live555");
        System.loadLibrary("Mp4v2");
        System.loadLibrary("main");
    }

    /**
     * 图传预览监听器
     */
    private static PreviewListener previewListener;

    /**
     * 设置图传预览监听器
     */
    public static void setPreviewListener(PreviewListener listener) {
        PreviewJNI.previewListener = listener;
    }

    /**
     * 初始化图传预览
     *
     * @param routerIP
     */
    public static native void init(String routerIP);

    /**
     * 关闭图传预览
     */
    public static native void shutDown();

    /**
     * 拍照
     *
     * @param path 照片存放路径
     * @return true:success false:fail
     */
    public static native boolean takePicture(String path);

    /**
     * 是否正在拍照
     *
     * @return
     */
    public static native boolean isTakingPicture();

    /**
     * 开始录像
     *
     * @param path 视频文件存放路径
     * @return true:success false:fail
     */
    public static native boolean recordStart(String path);

    /**
     * 是否正在录像
     *
     * @return true:是 false:否
     */
    public static native boolean recording();

    /**
     * 结束录像
     */
    public static native void recordStop();

    public static native void setDecodeFrame(boolean isDecodeFrame);

    /**
     * 图传预览播放结果
     */
    public static void playResult(int result) {
        Log.d(TAG, "playResult:" + result);
        if (previewListener != null) {
            previewListener.playResult(result);
        }
    }

    /**
     * 图传预览SPS帧数据
     */
    public static void onJniSPSFrame(byte[] data, int width, int height) {
        Log.d(TAG, "onJniSPSFrame:" + data.length);
        if (previewListener != null) {
            previewListener.onSPSFrame(data, width, height);
        }
    }

    /**
     * 图传预览PPS帧数据
     */
    public static void onJniPPSFrame(byte[] data) {
        Log.d(TAG, "onJniPPSFrame:" + data.length);
        if (previewListener != null) {
            previewListener.onPPSFrame(data);
        }
    }

    /**
     * 图传预览帧数据
     */
    public static void onJniFrame(byte[] data) {
//        Log.d(TAG, "onJniFrame:" + data.length);
        if (previewListener != null) {
            previewListener.onFrame(data);
        }
    }
}
