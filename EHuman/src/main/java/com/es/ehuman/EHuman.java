package com.es.ehuman;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * ES human detect
 * Created by Rust on 2018/5/19.
 */
public class EHuman {
    private static final String TAG = "ESAppEHuman";
    public static final String DIR_NAME = "e_human";
    private static final String NCNN_BIN = "ncnn.bin";
    private static final String NCNN_PROTO = "ncnn.proto";

    public static String mNCNNBinAbsPath;
    public static String mNCNNProtoAbsPath;

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
        Log.d(TAG, "prepare: DONE");
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

}
