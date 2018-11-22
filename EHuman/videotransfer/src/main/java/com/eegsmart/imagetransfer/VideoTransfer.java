package com.eegsmart.imagetransfer;

import com.eegsmart.imagetransfer.model.CameraInfo;
import com.eegsmart.imagetransfer.model.ImageTransferMode;
import com.eegsmart.imagetransfer.model.PreviewResolutionMode;

/**
 * 图传模块信息中心
 * Created by ES on 2018/2/1.
 */
public final class VideoTransfer {

    public static int onFrameCount = 0; // JNI送回的帧数  仅做参考

    private static VideoTransfer videoTransfer = new VideoTransfer();

    private VideoTransfer() {

    }

    public static VideoTransfer getVideoTransfer() {
        if (null == videoTransfer) {
            videoTransfer = new VideoTransfer();
        }
        return videoTransfer;
    }

    private ImageTransferMode imageTransferMode = ImageTransferMode.ACS;
    private PreviewResolutionMode previewResolutionMode = PreviewResolutionMode.R_720P;
    private CameraInfo cameraInfo;

    public ImageTransferMode getImageTransferMode() {
        return imageTransferMode;
    }

    public void setImageTransferMode(ImageTransferMode imageTransferMode) {
        this.imageTransferMode = imageTransferMode;
    }

    public PreviewResolutionMode getPreviewResolutionMode() {
        return previewResolutionMode;
    }

    public void setPreviewResolutionMode(PreviewResolutionMode previewResolutionMode) {
        this.previewResolutionMode = previewResolutionMode;
    }

    public CameraInfo getCameraInfo()
    {
        return cameraInfo;
    }

    public void setCameraInfo(CameraInfo cameraInfo)
    {
        this.cameraInfo = cameraInfo;
    }

}
