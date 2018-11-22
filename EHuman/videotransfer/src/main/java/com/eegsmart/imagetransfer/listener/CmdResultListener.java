package com.eegsmart.imagetransfer.listener;

import org.json.JSONObject;

/**
 * 命令结果监听处理器
 * Created by lidongxing on 2017/11/1.
 */
public interface CmdResultListener {
    /**
     * 发送命令成功事件
     * @param jsonObject 命令反馈结果JSON数据对象
     */
    void onSuccess(JSONObject jsonObject);
    /**
     * 发送命令失败事件
     */
    void onFailed();
}
