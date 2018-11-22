package com.eegsmart.imagetransfer.controller;

import android.graphics.Point;
import android.util.Log;

import com.eegsmart.imagetransfer.VTConstants;
import com.eegsmart.imagetransfer.VideoTransfer;
import com.eegsmart.imagetransfer.business.CmdResultParser;
import com.eegsmart.imagetransfer.business.TFCardMgr;
import com.eegsmart.imagetransfer.listener.CmdResultListener;
import com.eegsmart.imagetransfer.listener.FollowListener;
import com.eegsmart.imagetransfer.listener.OnReceiveDataListener;
import com.eegsmart.imagetransfer.listener.TFCardListener;
import com.eegsmart.imagetransfer.model.CameraInfo;
import com.eegsmart.imagetransfer.model.ImageTransferCmd;
import com.eegsmart.imagetransfer.model.ImageTransferReport;
import com.eegsmart.imagetransfer.model.TFCardStatus;
import com.eegsmart.imagetransfer.util.JsonUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * 图传命令控制器
 * 依赖 TcpController
 * Created by lidongxing on 2017/11/1.
 */
public class CmdController implements OnReceiveDataListener {
    private static final String TAG = "CmdController";
    private static final CmdController instance = new CmdController();
    private final Map<ImageTransferCmd, CmdResultListener> resultListenerMap = new HashMap<>();
    private TFCardListener tfCardListener;
    private FollowListener followListener;

    /**
     * 无人机是否正在录像
     */
    private volatile static boolean mRecordingVideo = false;

    /**
     * 拍照成功一次就改变一次这个值
     */
    public volatile static int mTakenPicture = 0;

    public synchronized static void changeTakenPictureValue() {
        if (mTakenPicture == 0) {
            mTakenPicture = 1;
        } else {
            mTakenPicture = 0;
        }
    }

    public synchronized static void setRecordingVideo(boolean recording) {
        CmdController.mRecordingVideo = recording;
    }

    public static boolean isRecordingVideo() {
        return mRecordingVideo;
    }

    private CmdController() {
    }

    public static CmdController getInstance() {
        return instance;
    }

    /**
     * 发送设备信息同步命令：时间同步及飞机系统信息获取
     * @param listener 命令结果监听处理器
     */
    public void syncWithDevice(final CmdResultListener listener) {
        Log.d(TAG, "发送设备信息同步命令：时间同步及飞机系统信息获取");
        CmdResultListener getSystemParamListener = new CmdResultListener() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                CameraInfo cameraInfo = CmdResultParser.parseSystemParameters(jsonObject, tfCardListener);
                VideoTransfer.getVideoTransfer().setCameraInfo(cameraInfo);

                String sendStr = JsonUtil.creatTimeJson(ImageTransferCmd.CMD_REQ_DATE_TIME_SET.ordinal());
                if(TcpController.getInstance().sendCmd(sendStr)) {
                    putCmdListener(ImageTransferCmd.CMD_REQ_DATE_TIME_SET, listener);
                }
                else {
                    Log.e(TAG, "发送设备信息同步命令：同步失败");
                    listener.onFailed();
                }
            }

            @Override
            public void onFailed() {
                Log.e(TAG, "发送设备信息同步命令：同步失败");
                listener.onFailed();
            }
        };

        sendEmptyCmd(ImageTransferCmd.CMD_REQ_SYS_PARAM_GET, getSystemParamListener);
    }

    /**
     * 发送开始升级命令
     * @param localVersion 当前APP版本号
     * @param listener 命令结果监听处理器
     */
    public void startUpgrade(final String localVersion, final CmdResultListener listener) {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String sendStr = JsonUtil.creatJson(ImageTransferCmd.CMD_REQ_FIRMWARE_UPDATE.ordinal(),
                        localVersion);
                Log.d(TAG, "sendStr:" + sendStr);
                if (TcpController.getInstance().sendCmd(sendStr)) {
                    putCmdListener(ImageTransferCmd.CMD_REQ_FIRMWARE_UPDATE, listener);
                }
                else {
                    listener.onFailed();
                }
            }
        }).start();
    }

    /**
     * 发送拍照命令
     * @param num 1：单拍；>1：连拍
     * @param delay 0：立即拍照；>0：延时拍照
     * @param listener 命令结果监听处理器
     */
    public void takePic(final int num, final int delay, final CmdResultListener listener) {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String sendStr = JsonUtil.creatTakePicJson(
                        ImageTransferCmd.CMD_REQ_VID_ENC_CAPTURE.ordinal(), num, delay);
                Log.d(TAG, "sendStr:" + sendStr);
                if (TcpController.getInstance().sendCmd(sendStr)) {
                    putCmdListener(ImageTransferCmd.CMD_REQ_VID_ENC_CAPTURE, listener);
                }
                else {
                    listener.onFailed();
                }
            }
        }).start();
    }

    /**
     * 发送开始录像命令
     * @param listener 命令结果监听处理器
     */
    public void startRecord(CmdResultListener listener) {
        sendEmptyCmd(ImageTransferCmd.CMD_REQ_VID_ENC_START, listener);
    }

    /**
     * 发送结束录像命令
     * @param listener 命令结果监听处理器
     */
    public void stopRecord(CmdResultListener listener) {
        sendEmptyCmd(ImageTransferCmd.CMD_REQ_VID_ENC_STOP, listener);
    }

    /**
     * 发送设置透明度命令
     * @param num 透明度值
     * @param listener 命令结果监听处理器
     */
    public void setBrightness(final int num, final CmdResultListener listener) {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String sendStr = JsonUtil.creatJson(
                        ImageTransferCmd.CMD_REQ_BRIGHTNESS_SET.ordinal(), num);
                Log.d(TAG, "sendStr:" + sendStr);
                if (TcpController.getInstance().sendCmd(sendStr)) {
                    putCmdListener(ImageTransferCmd.CMD_REQ_BRIGHTNESS_SET, listener);
                }
                else {
                    listener.onFailed();
                }
            }
        }).start();
    }

    /**
     * 发送设置对比度命令
     * @param num 对比度值
     * @param listener 命令结果监听处理器
     */
    public void setContrast(final int num, final CmdResultListener listener) {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String sendStr = JsonUtil.creatJson(
                        ImageTransferCmd.CMD_REQ_CONTRAST_SET.ordinal(), num);
                Log.d(TAG, "sendStr:" + sendStr);
                if (TcpController.getInstance().sendCmd(sendStr)) {
                    putCmdListener(ImageTransferCmd.CMD_REQ_CONTRAST_SET, listener);
                }
                else {
                    listener.onFailed();
                }
            }
        }).start();
    }

    /**
     * 发送设置曝光度命令
     * @param num 曝光度值
     * @param listener 命令结果监听处理器
     */
    public void setExposure(final int num, final CmdResultListener listener) {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String sendStr = JsonUtil.creatJson(
                        ImageTransferCmd.CMD_REQ_AE_SET.ordinal(), num);
                Log.d(TAG, "sendStr:" + sendStr);
                if (TcpController.getInstance().sendCmd(sendStr)) {
                    putCmdListener(ImageTransferCmd.CMD_REQ_AE_SET, listener);
                }
                else {
                    listener.onFailed();
                }
            }
        }).start();
    }

    /**
     * 发送设置白平衡命令
     * @param num 白平衡值
     * @param listener 命令结果监听处理器
     */
    public void setWhiteBalance(final int num, final CmdResultListener listener) {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String sendStr = JsonUtil.creatJson(
                        ImageTransferCmd.CMD_REQ_AUTO_AWB_SET.ordinal(), num);
                Log.d(TAG, "sendStr:" + sendStr);
                if (TcpController.getInstance().sendCmd(sendStr)) {
                    putCmdListener(ImageTransferCmd.CMD_REQ_AUTO_AWB_SET, listener);
                }
                else {
                    listener.onFailed();
                }
            }
        }).start();
    }

    /**
     * 发送重置相机相关设置命令
     * @param listener 命令结果监听处理器
     */
    public void resetCamera(final CmdResultListener listener) {
        sendEmptyCmd(ImageTransferCmd.CMD_REQ_FACTORY_RESTORE_SET, listener);
    }

    /**
     * 发送修改wifi信息命令
     * @param ssid wifi SSID
     * @param password wifi 密码
     * @param listener 命令结果监听处理器
     */
    public void changeWiFiInfo(final String ssid, final String password, final CmdResultListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sendStr = JsonUtil.creatJson(ImageTransferCmd.CMD_REQ_NET_SSID.ordinal(), ssid, password);
                Log.d(TAG, "sendStr:" + sendStr);
                if (TcpController.getInstance().sendCmd(sendStr)) {
                    putCmdListener(ImageTransferCmd.CMD_REQ_NET_SSID, listener);
                }
                else {
                    listener.onFailed();
                }
            }
        }).start();
    }

    /**
     * 发送修改预览分辨率命令
     * @param width 分辨率的长
     * @param height 分辨率的宽
     * @param listener 命令结果监听处理器
     */
    public void changePreviewResolution(final int width, final int height, final CmdResultListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sendStr = JsonUtil.creatPreviewJson(ImageTransferCmd.CMD_REQ_PREVIEW_RESOLUTION.ordinal(), width, height);
                Log.d(TAG, "sendStr:" + sendStr);
                if (TcpController.getInstance().sendCmd(sendStr)) {
                    putCmdListener(ImageTransferCmd.CMD_REQ_PREVIEW_RESOLUTION, listener);
                }
                else {
                    listener.onFailed();
                }
            }
        }).start();
    }

    /**
     * 发送开启跟随命令
     * @param startPoint 跟随的起始点【左上】
     * @param endPoint 跟随的结束点【右下】
     * @param listener 命令结果监听处理器
     */
    public void startFollow(final Point startPoint, final Point endPoint, final CmdResultListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sendStr = JsonUtil.creatFollowJson(ImageTransferCmd.CMD_REQ_FLLOW_ME_ON.ordinal(), startPoint, endPoint);
                Log.d(TAG, "sendStr:" + sendStr);
                if (TcpController.getInstance().sendCmd(sendStr)) {
                    putCmdListener(ImageTransferCmd.CMD_REQ_FLLOW_ME_ON, listener);
                }
                else {
                    if (null != listener) {
                        listener.onFailed();
                    }
                }
            }
        }).start();
    }

    /**
     * 发送结束跟随命令
     * @param listener 命令结果监听处理器
     */
    public void endFollow(final CmdResultListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sendStr = JsonUtil.creatJson(ImageTransferCmd.CMD_REQ_FLLOW_ME_OFF.ordinal(), -1);
                Log.d(TAG, "sendStr:" + sendStr);
                if (TcpController.getInstance().sendCmd(sendStr)) {
                    putCmdListener(ImageTransferCmd.CMD_REQ_FLLOW_ME_OFF, listener);
                }
                else {
                    listener.onFailed();
                }
            }
        }).start();
    }

    /**
     * 发送TF卡格式化命令
     * @param listener 命令结果监听处理器
     */
    public void formatTFCard(final CmdResultListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sendStr = JsonUtil.creatFormatJson(ImageTransferCmd.CMD_REQ_FORMAT.ordinal(), 1);
                Log.d(TAG, "sendStr:" + sendStr);
                if (TcpController.getInstance().sendCmd(sendStr)) {
                    putCmdListener(ImageTransferCmd.CMD_REQ_FORMAT, listener);
                }
                else {
                    listener.onFailed();
                }
            }
        }).start();
    }

    /**
     * 设置TF卡监听处理器
     * @param tfCardListener TF卡监听处理器
     */
    public void setTfCardListener(TFCardListener tfCardListener) {
        this.tfCardListener = tfCardListener;
    }

    /**
     * 设置跟随结果和物体位置变化监听处理器
     * @param followListener 跟随结果和物体位置变化监听处理器
     */
    public void setFollowListener(FollowListener followListener) {
        this.followListener = followListener;
    }

    @Override
    public void onReceiveData(String data) {
        try {
            JSONObject jsonObject = new JSONObject(data);

            parseReport(jsonObject);

            parseCmd(jsonObject);
        }
        catch (JSONException e) {
            Log.e(TAG, "on receive data error: " + e.getMessage());
        }
    }

    private void sendEmptyCmd(final ImageTransferCmd cmd, final CmdResultListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String sendStr = JsonUtil.creatJson(cmd.ordinal(), -1);
                if (TcpController.getInstance().sendCmd(sendStr)) {
                    putCmdListener(cmd, listener);
                }
                else {
                    listener.onFailed();
                }
            }
        }).start();
    }

    private void parseCmd(JSONObject jsonObject) {
        try {
            if(jsonObject.has(VTConstants.CMD)) {
                int cmd = Integer.valueOf(jsonObject.getString(VTConstants.CMD));
                int result = Integer.valueOf(jsonObject.getString(VTConstants.RESULT));
                ImageTransferCmd seq = ImageTransferCmd.values()[cmd];

                CmdResultListener listener = getCmdListener(seq);
                if (listener != null) {
                    if (result == 0) {
                        listener.onSuccess(jsonObject);
                    } else {
                        listener.onFailed();
                    }

                    removeCmdListener(seq);
                }
            }
        }
        catch (JSONException e) {
            Log.e(TAG, "parse report error: " + e.getMessage());
        }
    }

    private void parseReport(JSONObject jsonObject) {
        try {
            if(jsonObject.has(VTConstants.REPORT)) {
                int report = Integer.valueOf(jsonObject.getString(VTConstants.REPORT));
                ImageTransferReport seq = ImageTransferReport.values()[report];

                switch (seq) {
                    case MSG_SDCARE:
                        //:  { "REPORT": 0, "PARAM": { "online": 1, "freeSpace": 3208, "usedSpace": 574, "totalSpace": 3782 } }   1插卡，0拔卡
                        JSONObject sdcardJson = new JSONObject(jsonObject.getString(VTConstants.PARAM));
                        int online = Integer.valueOf(sdcardJson.getString(VTConstants.SDCARD_ONLINE));
                        TFCardStatus status = TFCardStatus.fromIntValue(online);
                        switch (status) {
                            case TF_CARD_UNPLUGGED:
                                TFCardMgr.mCardOnline = false;
                                if (tfCardListener != null) {
                                    tfCardListener.onUnplugged();
                                }
                                break;
                            case TF_CARD_MOUNT_FAILED:
                                TFCardMgr.mCardOnline = false;
                                if (tfCardListener != null) {
                                    tfCardListener.onMountFailed(Integer.valueOf(sdcardJson.getString(VTConstants.SDCARD_FORMAT)) == 1);
                                }
                                break;
                            case TF_CARD_MOUNT_SUCCESS:
                                int free = Integer.valueOf(sdcardJson.getString(VTConstants.SDCARD_FREE_SPACE));
                                int used = Integer.valueOf(sdcardJson.getString(VTConstants.SDCARD_USED_SPACE));
                                int total = Integer.valueOf(sdcardJson.getString(VTConstants.SDCARD_TOTAL_SPACE));
                                TFCardMgr.mFreeSpaceMb = free;
                                TFCardMgr.mUsedSpaceMb = used;
                                TFCardMgr.mTotalSpaceMb = total;
                                TFCardMgr.mCardOnline = true;
                                if (tfCardListener != null) {
                                    tfCardListener.onMountSuccess(free, used, total);
                                }
                                break;
                            case TF_CARD_UNSPPORT:
                                TFCardMgr.mCardOnline = false;
                                if (tfCardListener != null) {
                                    tfCardListener.onUnspport();
                                }
                                break;
                        }
                        break;
                    case MSG_FOLLOW:
                        //{"REPORT":2,"PARAM":{{"RESULT":ret},{"X0":pX0},{"Y0":pY0},{"X1":pX1},{"Y1":pY1}}}
                        JSONObject paramObject = new JSONObject(jsonObject.getString(VTConstants.PARAM));
                        int result = Integer.valueOf(paramObject.getString(VTConstants.RESULT));
                        int x0 = Integer.valueOf(paramObject.getString(VTConstants.POSITION_X0));
                        int y0 = Integer.valueOf(paramObject.getString(VTConstants.POSITION_Y0));
                        int x1 = Integer.valueOf(paramObject.getString(VTConstants.POSITION_X1));
                        int y1 = Integer.valueOf(paramObject.getString(VTConstants.POSITION_Y1));

                        if(followListener!=null) {
                            followListener.onFollow(
                                    result==0, new Point(x0, y0), new Point(x1, y1));
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        catch (JSONException e) {
            Log.e(TAG, "parse report error: " + e.getMessage());
        }
    }

    public CmdResultListener getCmdListener(ImageTransferCmd cmd) {
        if(resultListenerMap.containsKey(cmd)) {
            return resultListenerMap.get(cmd);
        }

        return null;
    }

    public void putCmdListener(ImageTransferCmd cmd, CmdResultListener listener) {
        if(listener==null) {
            return;
        }

        if(resultListenerMap.containsKey(cmd)) {
            resultListenerMap.remove(cmd);
        }

        resultListenerMap.put(cmd, listener);
    }

    public void removeCmdListener(ImageTransferCmd cmd) {
        if(resultListenerMap.containsKey(cmd)) {
            resultListenerMap.remove(cmd);
        }
    }
}
