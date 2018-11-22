package com.eegsmart.imagetransfer.business;

import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.AsyncTask;
import android.util.Log;

import com.eegsmart.imagetransfer.VTConstants;
import com.eegsmart.imagetransfer.listener.NativeDecoderListener;

import java.nio.ByteBuffer;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * 编解码器
 * Created by aw on 2017/10/9.
 */
public class NativeDecoder {
    private static final String TAG = "NativeDecoder";

    private static final String MIME_TYPE = "video/avc";
    private static final String CSD0 = "csd-0";
    private static final String CSD1 = "csd-1";

    private static final int TIME_INTERNAL = 20;
    private static final int DECODER_TIME_INTERNAL = 4;

    private MediaCodec mCodec;
    private long mCount = 0; // 媒体解码器MediaCodec用的

    private Queue<byte[]> data = null;
    private volatile ConcurrentSkipListSet<Integer> outputBufferStatusSet; // 存储正在使用的buffer下标

    private DecoderThread decoderThread;
    private NDWatcherThread ndWatcherThread;
    private NativeDecoderListener listener;

    public NativeDecoder() {
        data = new ConcurrentLinkedQueue<>();
        outputBufferStatusSet = new ConcurrentSkipListSet<>();
    }

    public boolean isCodecCreated() {
        return mCodec!=null;
    }

    public boolean createCodec(NativeDecoderListener listener, byte[] spsBuffer, byte[] ppsBuffer, int width, int height) {
        this.listener = listener;

        try {
            mCodec = MediaCodec.createDecoderByType(VTConstants.MIME_TYPE);
            MediaFormat mediaFormat = createVideoFormat(spsBuffer, ppsBuffer, width, height);
            mCodec.configure(mediaFormat, null, null, 0);
            mCodec.start();

            Log.d(TAG, "decoderThread mediaFormat in:" + mediaFormat);

            if (null == decoderThread) {
                decoderThread = new DecoderThread();
                decoderThread.start();
            } else {
                android.util.Log.e(TAG, "decoderThread 已经存在!");
            }
//            if (null == ndWatcherThread) {
//                ndWatcherThread = new NDWatcherThread();
//                ndWatcherThread.start();
//            } else {
//                android.util.Log.e(TAG, "NDWatcherThread 已经存在!");
//            }
            return true;
        }
        catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "MediaCodec create error:" + e.getMessage());

            return false;
        }
    }

    private MediaFormat createVideoFormat(byte[] spsBuffer, byte[] ppsBuffer, int width, int height) {
        MediaFormat mediaFormat;

        mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE, width, height);
        mediaFormat.setByteBuffer(CSD0, ByteBuffer.wrap(spsBuffer));
        mediaFormat.setByteBuffer(CSD1, ByteBuffer.wrap(ppsBuffer));
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible);
//            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);

        return mediaFormat;
    }

    private long lastInQueueTime = 0;

    public void addData(byte[] dataBuffer) {
//        final long timeDiff = System.currentTimeMillis() - lastInQueueTime;
//        if (timeDiff > 1) {
//            lastInQueueTime = System.currentTimeMillis();
        int queueSize = data.size();
        if (queueSize > 30) {
            data.clear();
            Log.e(TAG, "frame queue 帧数据队列超出上限，自动清除数据 " + queueSize);
        }
        data.add(dataBuffer); // es debug 不再复制副本
//        Log.e(TAG, "frame queue 添加一帧数据");
//        } else {
//            Log.e(TAG,"frame queue 添加速度太快,跳过此帧. timeDiff=" + timeDiff);
//        }
    }

    public void destroyCodec() {
        if (mCodec != null) {
            try {
                mCount = 0;

                if(data!=null) {
                    data.clear();

                    data = null;
                }

                if(decoderThread!=null) {
                    decoderThread.stopThread();

                    decoderThread = null;
                }

                mCodec.release();
                mCodec = null;
            }
            catch (Exception e) {
                e.printStackTrace();
                Log.d(TAG, "destroyCodec exception:" + e.toString());
            }
        }
    }


    private class DecoderThread extends Thread {
        private final int INPUT_BUFFER_FULL_COUNT_MAX = 50;
        private boolean isRunning;
        private int inputBufferFullCount = 0; // 输入缓冲区满了多少次

        DecoderThread() {
            setPriority(Thread.MAX_PRIORITY);
        }

        public void stopThread() {
            isRunning = false;
        }

        @Override
        public void run() {
            setName("NativeDecoder_DecoderThread-" + getId());
            isRunning = true;
            while (isRunning) {
                try {
                    if (data != null && !data.isEmpty()) {
                        int inputBufferIndex = mCodec.dequeueInputBuffer(0);
//                        android.util.Log.d(TAG, "inputBufferIndex=" + inputBufferIndex);
                        if (inputBufferIndex >= 0) {
                            byte[] buf = data.poll();
                            ByteBuffer inputBuffer = mCodec.getInputBuffer(inputBufferIndex);
                            if (null != inputBuffer && null != buf) {
                                inputBuffer.clear();
                                inputBuffer.put(buf, 0, buf.length);
                                mCodec.queueInputBuffer(inputBufferIndex, 0,
                                        buf.length, mCount * TIME_INTERNAL, 0);
                                mCount++;
                            }
                            inputBufferFullCount = 0; // 还有缓冲区可以用的时候重置计数
                        } else {
                            inputBufferFullCount++;
//                            Log.e(TAG, "decoderThread inputBuffer full.  inputBufferFullCount=" + inputBufferFullCount);
                            if (inputBufferFullCount > INPUT_BUFFER_FULL_COUNT_MAX) {
                                mCount = 0;
                                mCodec.flush();
//                                Log.e(TAG, "mCodec.flush()...");
                            }
                        }
                    }
//                    android.util.Log.d(TAG, "outputBufferStatusSet.size = " + outputBufferStatusSet.size());
//                    for (int wi : outputBufferStatusSet) {
//                        android.util.Log.d(TAG, "|--- working output buffer index -> " + wi);
//                    }
                    // Get output buffer index
                    MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                    int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
                    while (outputBufferIndex >= 0) {
                        if (!outputBufferStatusSet.contains(outputBufferIndex)) {
                            outputBufferStatusSet.add(outputBufferIndex);
                            releaseOutputBuffer(outputBufferIndex);
                        } else {
                            android.util.Log.e(TAG, "当前buffer仍在使用中,index=" + outputBufferIndex);
                        }
                        outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, 0);
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "decoderThread exception:" + e.getMessage());
                }

                try {
                    Thread.sleep(DECODER_TIME_INTERNAL);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private void releaseOutputBuffer(final int index) {
            new ReleaseAsyncTask().execute(index);
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
////                    Log.d(TAG, "releaseOutputBuffer " + Thread.currentThread().toString());
//                    try {
//                        ByteBuffer oBuffer = mCodec.getOutputBuffer(index);
//                        listener.onDataDecoded(oBuffer,
//                                mCodec.getOutputFormat().getInteger(MediaFormat.KEY_COLOR_FORMAT));
//                        listener.onImage(mCodec.getOutputImage(index));
//                    } catch (Exception e) {
//                        Log.e(TAG,"releaseOutputBuffer thread: " + e.toString());
//                    }
////                    Log.d(TAG, "get_outputBuffer end");
//                    try {
//                        mCodec.releaseOutputBuffer(index, false);
//                        outputBufferStatusSet.remove(index); // 从工作集合中剔除 表示此buffer回到可用状态
//                    } catch (IllegalStateException ex) {
//                        android.util.Log.e(TAG, "releaseOutputBuffer ERROR", ex);
//                    }
////                    Log.d("get_outputBuffer", "release");
//                }
//            }).start();
        }
    }

    private class ReleaseAsyncTask extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... integers) {
            int index = integers[0];
            //                    Log.d(TAG, "releaseOutputBuffer " + Thread.currentThread().toString());
            try {
                ByteBuffer oBuffer = mCodec.getOutputBuffer(index);
                listener.onDataDecoded(oBuffer,
                        mCodec.getOutputFormat().getInteger(MediaFormat.KEY_COLOR_FORMAT));
                listener.onImage(mCodec.getOutputImage(index));
            } catch (Exception e) {
                Log.e(TAG,"releaseOutputBuffer thread: " + e.toString());
            }
//                    Log.d(TAG, "get_outputBuffer end");
            try {
                mCodec.releaseOutputBuffer(index, false);
                outputBufferStatusSet.remove(index); // 从工作集合中剔除 表示此buffer回到可用状态
            } catch (IllegalStateException ex) {
                android.util.Log.e(TAG, "releaseOutputBuffer ERROR", ex);
            }
//                    Log.d("get_outputBuffer", "release");
            return null;
        }
    }

    /**
     * 监视线程
     */
    private class NDWatcherThread extends Thread {

        @Override
        public void run() {
            super.run();
            setName("NDWatcherThread-" + getId());
            Log.d(TAG,"NDWatcherThread start! " + this.toString());
            while (!isInterrupted()) {
                if (null == decoderThread) {
                    Log.e(TAG,"watcher: decoderThread is NULL ---- do something!");
                } else {
                    Log.d(TAG,String.format(Locale.CHINA, "watcher: decoderThread alive: %b, isRunning: %b, isInterrupted: %b",
                            decoderThread.isAlive(), decoderThread.isRunning, decoderThread.isInterrupted()));
                    Log.d(TAG,String.format(Locale.CHINA, "watcher: 排队中的数据数量 %d; 使用中的output buffer数量 %d",
                            data.size(), outputBufferStatusSet.size()));
                }

                if (null == mCodec) {
                    Log.e(TAG,"watcher: mCodec is NULL!!!!");
                } else {
                    Log.d(TAG,"watcher: codec is " + mCodec);

                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            Log.d(TAG,"NDWatcherThread end - bye bye!");
        }
    }

}
