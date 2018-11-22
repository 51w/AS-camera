package com.eegsmart.imagetransfer.listener;

/**
 * TF卡监听处理器
 * Created by lidongxing on 2017/11/15.
 */
public interface TFCardListener {
    /**
     * 插入事件
     */
    void onUnplugged();
    /**
     * 挂载失败事件
     * @param toFormat true：需要格式化；false：不需要格式化
     */
    void onMountFailed(boolean toFormat);
    /**
     * 挂载成功事件
     * @param freeSpace 可用空间大小
     * @param usedSpace 已用空间大小
     * @param totalSpace 总空间大小
     */
    void onMountSuccess(int freeSpace, int usedSpace, int totalSpace);
    /**
     * 卡格式不支持事件
     */
    void onUnspport();
}
