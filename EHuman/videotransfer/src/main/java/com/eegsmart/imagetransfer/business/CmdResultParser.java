package com.eegsmart.imagetransfer.business;

import android.util.Log;

import com.eegsmart.imagetransfer.VTConstants;
import com.eegsmart.imagetransfer.listener.TFCardListener;
import com.eegsmart.imagetransfer.model.TFCardStatus;
import com.eegsmart.imagetransfer.model.CameraInfo;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by lidongxing on 2017/8/28.
 */
public class CmdResultParser {
    private static final String TAG = "CmdResultParser";

    public static CameraInfo parseSystemParameters(JSONObject jsonObject, TFCardListener tfCardListener) {
        CameraInfo cameraInfo = new CameraInfo();

        JSONObject paramJson = null;
        try {
            paramJson = new JSONObject(getString(jsonObject, VTConstants.PARAM));

            JSONObject wifiJson = new JSONObject(getString(paramJson, VTConstants.WIFI_PARAM));
            cameraInfo.setWifiName(getString(wifiJson, VTConstants.WIFI_SSID));
            cameraInfo.setWifiPassword(getString(wifiJson, VTConstants.WIFI_PASSWORD));

            cameraInfo.setCharge(getInt(paramJson, VTConstants.BATTERY) == CameraInfo.STS_BATTERY.STS_CHAGE_YSE.ordinal());
            cameraInfo.setBrightness(getInt(paramJson, VTConstants.BRIGHTNESS));
            cameraInfo.setWhiteBalance(getInt(paramJson, VTConstants.WHITE_BALANCE));
            cameraInfo.setFlash(getInt(paramJson, VTConstants.FLASH));
            cameraInfo.setExposure(getInt(paramJson, VTConstants.EXPOSURE));
            cameraInfo.setContrast(getInt(paramJson, VTConstants.CONTRAST));
            cameraInfo.setVersion(getString(paramJson, VTConstants.VERSION));

            JSONObject sdcardJson = new JSONObject(getString(paramJson, VTConstants.SDCARD));
            int online = getInt(sdcardJson, VTConstants.SDCARD_ONLINE);
            TFCardStatus status = TFCardStatus.fromIntValue(online);
//            Log.d(TAG, "sdcardJson: " + sdcardJson);
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
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG, "parseSystemParameters: ", e);
        }

        Log.d(TAG, cameraInfo.toString());
        return cameraInfo;
    }

    private static String getString(JSONObject jsonObject, String name) {
        try {
            return jsonObject.getString(name);
        } catch (JSONException ex) {
            Log.e(TAG, ex.getMessage());
        }

        return null;
    }

    private static int getInt(JSONObject jsonObject, String name) {
        try {
            return Integer.valueOf(jsonObject.getString(name));
        } catch (JSONException ex) {
            Log.e(TAG, ex.getMessage());
        }

        return 0;
    }
}
