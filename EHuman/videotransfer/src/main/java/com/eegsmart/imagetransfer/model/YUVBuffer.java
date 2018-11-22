package com.eegsmart.imagetransfer.model;

import java.nio.ByteBuffer;

/**
 * Created by lidongxing on 2017/10/23.
 */

public class YUVBuffer {
    private ByteBuffer y;
    private ByteBuffer uv;
    private int ySize;
    private int uvSize;

    public YUVBuffer(int width, int height) {
        ySize = width * height;
        uvSize = ySize / 2;

        y = ByteBuffer.allocate(ySize);
        uv = ByteBuffer.allocate(uvSize);
    }

    public void clear() {
        if(y!=null) {
            y.clear();
        }
        if(uv!=null) {
            uv.clear();
        }
    }

    public ByteBuffer getY() {
        return y;
    }

    public void setY(ByteBuffer y) {
        this.y = y;
    }

    public void setY(byte[] data) {
        y.put(data);
    }

    public ByteBuffer getUv() {
        return uv;
    }

    public void setUv(ByteBuffer uv) {
        this.uv = uv;
    }

    public void setUv(byte[] data) {
        uv.put(data);
    }

    public int getySize() {
        return ySize;
    }

    public int getUvSize() {
        return uvSize;
    }
}
