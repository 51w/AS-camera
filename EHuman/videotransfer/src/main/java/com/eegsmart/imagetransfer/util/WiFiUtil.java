package com.eegsmart.imagetransfer.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

/**
 * WiFi工具类
 * Created by lidongxing on 2017/10/12.
 */
public class WiFiUtil {
    public static String getRouterIP(Context context) {
        try {
            WifiManager manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            if (manager.getDhcpInfo() != null) {
                return intToIp(manager.getDhcpInfo().serverAddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("ESApp", "getRouterIP: ", e);
        }
        return "0.0.0.0";
    }

    public static String getWiFiName(Context context) {
        String wifiId = "WIFI_NAME_NOT_FOUND";
        try {
            WifiManager wifiMgr = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo info = wifiMgr.getConnectionInfo();
            wifiId = info != null ? info.getSSID() : null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(wifiId) && wifiId.startsWith("\"")) {
            wifiId = wifiId.substring(1); // 删去前面那个引号
        }
        return wifiId;
    }

    private static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "."
                + ((i >> 24) & 0xFF);
    }
}
