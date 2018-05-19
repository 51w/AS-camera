package com.es.ehuman;
// javah com.es.ehuman.EHuman

import android.content.Context;
import android.graphics.Rect;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * ES human detect
 * Created by Rust on 2018/5/19.
 */
public class EHuman {
    private static final String TAG = "ESAppEHuman";
    public static final String OUT_PROTO_DIR = Environment.getExternalStorageDirectory() + File.separator + "EHuman";
    public static final String DIR_NAME = "e_human";
    private static final String NCNN_BIN = "ncnn.bin";
    private static final String NCNN_PROTO = "ncnn.proto";
    private static File mPicDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "EHuman");
    public static String mNCNNBinAbsPath;
    public static String mNCNNProtoAbsPath;

    static {
        System.loadLibrary("ehuman");
    }

    private static Handler mCalHandler;
    private static HandlerThread mCalThread;
    private volatile static boolean mPrepareFinish = false;
    private volatile static boolean mComputing = false;

    static {
        if (!mPicDir.exists()) {
            boolean mp = mPicDir.mkdir();
            Log.d(TAG, "static initializer: mPicDir.mkdir " + mp);
        }
        mCalThread = new HandlerThread("EHumanCalThread");
        mCalThread.start();
        mCalHandler = new Handler(mCalThread.getLooper());
    }

    /**
     * Copy files to app's dir
     */
    public static void prepare(Context context) throws IOException {
        Log.d(TAG, "prepare: Start");
        File dir = new File(context.getFilesDir(), DIR_NAME);
        if (!dir.exists()) {
            boolean d = dir.mkdir();
            Log.d(TAG, "prepare: 创建目录 " + dir + " " + d);
        }
//        for (File f : dir.listFiles()) {
//            Log.d(TAG, "prepare:   |--- " + f.getAbsolutePath());
//        }
        File nFile1 = new File(dir, NCNN_BIN);
        File nFile2 = new File(dir, NCNN_PROTO);
        if (!nFile1.exists()) {
            mNCNNBinAbsPath = copyAssetsFile(context, dir, NCNN_BIN);
        } else {
            mNCNNBinAbsPath = nFile1.getAbsolutePath();
        }
        if (!nFile2.exists()) {
            mNCNNProtoAbsPath = copyAssetsFile(context, dir, NCNN_PROTO);
        } else {
            mNCNNProtoAbsPath = nFile2.getAbsolutePath();
        }
        Log.d(TAG, "prepare: " + mNCNNBinAbsPath);
        Log.d(TAG, "prepare: " + mNCNNProtoAbsPath);
        Log.d(TAG, "prepare: native init ncnn");
        nInitMobilenet(mNCNNBinAbsPath, mNCNNProtoAbsPath);
        mPrepareFinish = true;
        Log.d(TAG, "prepare: DONE");
    }

    public static void prepareProto(String protoDir) {
        mNCNNBinAbsPath = new File(protoDir, "ncnn.bin").getAbsolutePath();
        mNCNNProtoAbsPath = new File(protoDir, "ncnn.proto").getAbsolutePath();
        Log.d(TAG, "prepareProto: " + mNCNNBinAbsPath);
        Log.d(TAG, "prepareProto: " + mNCNNProtoAbsPath);
        nInitMobilenet(mNCNNBinAbsPath, mNCNNProtoAbsPath);
        mPrepareFinish = true;
    }

    private static String copyAssetsFile(Context context, File dir, String filename) throws IOException {
        File file = new File(dir, filename);
        InputStream is1 = context.getAssets().open(filename);
        FileOutputStream fos1 = new FileOutputStream(file);
        byte[] tmpB = new byte[1024];
        int count1;
        while ((count1 = is1.read(tmpB)) > 0) {
            fos1.write(tmpB, 0, count1);
        }
        fos1.flush();
        fos1.close();
        is1.close();
        Log.d(TAG, "prepare: " + file + " " + file.exists());
        return file.getAbsolutePath();
    }

    private static int mCalCount = 0;

    public synchronized static void detectHuman(final byte[] nv21, final int nv21Width, final int nv21Height) {
        if (mComputing || !mPrepareFinish) {
//            Log.d(TAG, "skip this frame, busy...");
            return;
        }
        mCalCount++;
        mComputing = true;
        mCalHandler.post(new Runnable() {
            @Override
            public void run() {
                ArrayList<int[]> resList = new ArrayList<>(); // 存储结果
                nDetectHuman(resList, nv21.clone(), nv21.length, nv21Width, nv21Height, false,
                        new File(mPicDir, "p_" + mCalCount + ".jpg").getAbsolutePath());
//                Log.d(TAG, String.format(Locale.CHINA, "%05d, detectHuman res==null? %b; nv21 [Wid,Height] = [%d, %d]",
//                        mCalCount, (resList == null), nv21Width, nv21Height));
                if (null != onResultListener) {
                    ArrayList<Rect> rectArrayList = new ArrayList<>();
                    if (!resList.isEmpty()) {
                        for (int[] r : resList) {
                            // 从OpenCV Rect转换成Android Rect
                            rectArrayList.add(new Rect(r[0], r[1], r[0] + r[2], r[1] + r[3]));
                        }
                    }
                    onResultListener.onResult(rectArrayList, nv21Width, nv21Height);
                }
                mComputing = false;
            }
        });
    }

    private static OnResultListener onResultListener;

    public static void setOnResultListener(OnResultListener listener) {
        EHuman.onResultListener = listener;
    }

    public interface OnResultListener {
        void onResult(List<Rect> resArray, final int picWidth, final int picHeight);
    }

    public static native void nInitMobilenet(String binPath, String protoPath);

    public static native void nDetectHuman(ArrayList<int[]> resList, byte[] nv21, int nv21Len, int nv21Width, int nv21Height, boolean saveInputPic, String savePicPath);
}
