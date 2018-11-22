package com.eegsmart.imagetransfer.listener;

import android.media.Image;

import java.nio.ByteBuffer;

/**
 * 解码后的数据监听器
 * 要保证传递的ByteBuffer是有效的
 * Created by lidongxing on 2017/10/20.
 */
public interface NativeDecoderListener {
    void onDataDecoded(ByteBuffer data, int colorFormat);

    /**
     * @param image 调用此方法后  上面的byteBuffer变为无效
     */
    void onImage(Image image);
}
