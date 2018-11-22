package com.eegsmart.imagetransfer.listener;

import android.graphics.Point;

/**
 * 跟随结果和物体位置变化监听处理器
 * Created by lidongxing on 2017/11/20.
 */
public interface FollowListener {
    /**
     * 跟随结果/物体位置变化事件
     * @param result true:跟踪成功；false：跟踪失败
     * @param startPoint 物体位置起始点【左上】
     * @param endPoint 物体位置结束点【右下】
     */
    void onFollow(boolean result, Point startPoint, Point endPoint);
}
