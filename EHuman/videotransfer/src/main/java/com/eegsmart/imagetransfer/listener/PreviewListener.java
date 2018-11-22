package com.eegsmart.imagetransfer.listener;

public interface PreviewListener{
	void playResult(int result);
	void onFrame(byte[] data);
    void onSPSFrame(byte[] data, int width, int height);
    void onPPSFrame(byte[] data);
}