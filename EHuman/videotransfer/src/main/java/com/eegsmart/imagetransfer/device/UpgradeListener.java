package com.eegsmart.imagetransfer.device;


public interface UpgradeListener {
    void onCheckNewVersion(String versionName);

    void onUploadingFile(String filename, int percent);

    /**
     * 上传文件成功
     */
    void onUploadFileSuccess(final String fileName, final int fileNumber, final int totalFileCount);

    void onUploadFailed();

    void onUpgradeSuccess();

    void onUpgradeFailed();
}
