package com.eegsmart.imagetransfer.view;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

import java.nio.ByteBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLFrameRenderer implements Renderer {
    private static final String TAG = "GLFrameRenderer";
    private GLSurfaceView mTargetSurface;
    private GLProgram prog;

    private final Object mRenderLock;
    private ByteBuffer y;
    private ByteBuffer uv;
    private int orderVU;
    private int type;

    private int screenWidth;
    private int screenHeight;

    private int yHorizontalStride;
    private int uvHorizontalStride;
    private int verticalStride;

    private int lrSpace;
    private int cSpace;

    public GLFrameRenderer(GLSurfaceView surface, int vrSpaceLeftRightResId, int vrSpaceCenterDimenResID) {
        mRenderLock = new Object();
        mTargetSurface = surface;

        lrSpace = surface.getResources().getDimensionPixelSize(vrSpaceLeftRightResId);
        cSpace = surface.getResources().getDimensionPixelSize(vrSpaceCenterDimenResID);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated");
        prog = new GLProgram(0);

        if (!prog.isProgramBuilt()) {
            prog.buildProgram();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Log.d(TAG, "onSurfaceChanged of width: " + width + ", height:" + height);
//        GLES20.glViewport(0, 0, width, height);
        this.screenWidth = width;
        this.screenHeight = height;
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        synchronized (mRenderLock) {
            Log.d(TAG, "onDrawFrame start");
            if (y != null) {
                // reset position, have to be done
                y.position(0);
                uv.position(0);

                prog.buildTextures(y, uv, type, yHorizontalStride, uvHorizontalStride, verticalStride);
                GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
                GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

//                if (TachApplication.getInstance().isVRMode()) {
//                    GLES20.glViewport(lrSpace, screenHeight / 4, screenWidth / 2 - lrSpace - cSpace, screenHeight / 2);
//                    prog.drawFrame(orderVU, type);
//                    GLES20.glViewport(screenWidth / 2 + cSpace, screenHeight / 4, screenWidth / 2 - lrSpace - cSpace, screenHeight / 2);
//                    prog.drawFrame(orderVU, type);
//                } else {
                GLES20.glViewport(0, 0, screenWidth, screenHeight);
                prog.drawFrame(orderVU, type);
//                }

                y.clear();
                uv.clear();
            }
            Log.d(TAG, "onDrawFrame end");
        }
    }

    /**
     * this method will be called from native code, it happens when the video is about to play or
     * the video size changes.
     */
    public void update(int w, int h) {
        Log.d(TAG, "update of width: " + w + ", height:" + h);
        if (w > 0 && h > 0) {
            // 初始化容器
            //TODO:
        }
    }

    /**
     * this method will be called from native code, it's used for passing yuv data to me.
     */
    public byte[] update(ByteBuffer data, int width, int height, int colorFormat) {
        synchronized (mRenderLock) {
            android.util.Log.d(TAG, "更新界面 update start " + Thread.currentThread().toString());
            int yVerticalSpan = height;
            int uvVerticalSpan = height;
            verticalStride = height;

            switch (colorFormat) {
                case 19://COLOR_FormatYUV420Planar
                    type = 0;
                    orderVU = 0;
                    yHorizontalStride = width;
                    yVerticalSpan = height;
                    uvHorizontalStride = yHorizontalStride / 2;
                    uvVerticalSpan = yVerticalSpan;
                    break;
                case 21://COLOR_FormatYUV420SemiPlanar
                    type = 1;
                    orderVU = 0;
                    yHorizontalStride = width;
                    yVerticalSpan = height;
                    uvHorizontalStride = yHorizontalStride;
                    uvVerticalSpan = yVerticalSpan / 2;
                    break;
                case 0x7FA30C03://OMX_QCOM_COLOR_FormatYUV420PackedSemiPlanar64x32Tile2m8ka
                    type = 1;
                    orderVU = 1;
                    int hAlign = 64;
                    int vAlign = 32;
                    yHorizontalStride = width % hAlign > 0 ? hAlign * (width / hAlign + 1) : width;
                    yVerticalSpan = height % vAlign > 0 ? vAlign * (height / vAlign + 1) : height;
                    uvHorizontalStride = yHorizontalStride;
                    uvVerticalSpan = yVerticalSpan / 2;
                    break;
                case 0x7FA30C04://COLOR_QCOM_FormatYUV420SemiPlanar32m
                    type = 1;
                    orderVU = 0;
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

            ByteBuffer y = ByteBuffer.allocate(ySize);
            ByteBuffer uv = ByteBuffer.allocate(uvSize);

            data.position(0);
            final byte[] nv21 = new byte[ySize + uvSize];
//            byte[] yb = new byte[ySize];
            data.get(nv21, 0, ySize);
            y.put(nv21, 0, ySize);

//            byte[] uvb = new byte[uvSize];
            data.get(nv21, ySize, uvSize);
            uv.put(nv21, ySize, uvSize);

            this.y = y;
            this.uv = uv;

            // request to render
            mTargetSurface.requestRender();
            android.util.Log.d(TAG, "更新界面 update end " + Thread.currentThread().toString());
            return nv21;
        }
    }

}
