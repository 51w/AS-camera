package com.eegsmart.imagetransfer.controller;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.eegsmart.imagetransfer.business.TcpService;
import com.eegsmart.imagetransfer.listener.OnConnectedListener;
import com.eegsmart.imagetransfer.listener.OnDisconnectedListener;
import com.eegsmart.imagetransfer.listener.OnReceiveDataListener;
import com.eegsmart.imagetransfer.util.WiFiUtil;

/**
 * 图传TCP连接控制器
 */
public class TcpController {
	private static final String TAG = "TcpController";

	private static final TcpController instance = new TcpController();
    private static String wifiNamePrefix = "";
    private static boolean limitWiFiName = false; // 只允许连接特定的WiFi名

    /**
     * WiFi IP 地址
     */
	private String wifiIP = "";

	private TcpService tcpService;
    private ConnectToDroneThread connectToDroneThread;

	private OnConnectedListener onConnectedListener;
	private OnDisconnectedListener onDisconnectedListener;
	private OnReceiveDataListener onReceiveDataListener;

	private TcpController() {
	    tcpService = new TcpService();
	}

	public static void setTCPPort(int tcpPort) {
	    TcpService.setTcpPort(tcpPort);
    }

    public static TcpController getInstance() {
		return instance;
	}

    public String getWifiIP() {
        return wifiIP;
    }

    public static void setWifiNamePrefix(String prefix) {
        TcpController.wifiNamePrefix = prefix;
    }

    public static void setLimitWiFiName(boolean limit) {
        TcpController.limitWiFiName = limit;
    }

    /**
     * 终止当前的连接线程
     */
    private void connectTo(String ip) {
        wifiIP = ip;
		Log.d(TAG, "connect to " + ip);
        if (null != connectToDroneThread) {
            if (connectToDroneThread.isConnecting()) {
                return; // 沿用原来的线程继续连接
            } else {
                connectToDroneThread.interrupt(); // 打断旧线程
                connectToDroneThread = null;
            }
        }
        connectToDroneThread = new ConnectToDroneThread();
        connectToDroneThread.start();
    }

    /**
     * 尝试连接线程  负责连接操作
     * 会不停地尝试连接
     */
	private class ConnectToDroneThread extends Thread {
	    private boolean isConnecting = false;

        boolean isConnecting() {
            return isConnecting;
        }

        @Override
        public void run() {
			Log.d(TAG, " ConnectToDroneThread start - " + this.toString());
            isConnecting = true;
            while (!isInterrupted() && tcpService != null) {
                if (TextUtils.isEmpty(wifiIP)) {
                    Log.e(TAG, "WiFi IP is invalid!");
                    try {
                        Thread.sleep(1000); // 这里会循环尝试连接
                    } catch (InterruptedException e) {
                        isConnecting = false;
                        e.printStackTrace();
                    }
                    continue;
                }
                Log.d(TAG, " tcpService.connect -> " + wifiIP);
                if (!tcpService.connectIP(wifiIP)) {
                    try {
                        Thread.sleep(1000); // 这里会循环尝试连接
                    } catch (InterruptedException e) {
                        isConnecting = false;
                        e.printStackTrace();
                    }
                } else {
                    break;
                }
            }
            isConnecting = false;

            if (tcpService != null
                    && tcpService.isConnect()
                    && onConnectedListener != null) {
                onConnectedListener.onConnected();
            }
			Log.d(TAG, " ConnectToDroneThread end - " + this.toString());
        }
    }

    /**
     * 接收WiFi状态广播
     */
	private BroadcastReceiver wiFiConnectBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {//wifi连接上与否
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                switch (info.getState()) {
                    case CONNECTED:
                        if (!isConnect()) {
                            if (limitWiFiName) {
                                if (WiFiUtil.getWiFiName(context).toLowerCase().startsWith(wifiNamePrefix)) {
                                    connectTo(WiFiUtil.getRouterIP(context));
                                }
                            } else {
                                connectTo(WiFiUtil.getRouterIP(context));
                            }
                        }
                        break;
                    case DISCONNECTED:
                    case DISCONNECTING:
                        if (null != onDisconnectedListener) {
                            onDisconnectedListener.onDisconnected();
                        }
                        break;
                }
            }
        }
	};

	/**
	 * 建立TCP连接
	 * @param context 上下文
	 * @param connectedListener 连接成功监听器
	 * @param disconnectedListener 连接断开监听器
	 * @param receiveDataListener 数据反馈接收处理器
	 */
	public void start(Context context,
					  OnConnectedListener connectedListener,
					  OnDisconnectedListener disconnectedListener,
					  OnReceiveDataListener receiveDataListener) {
		this.onConnectedListener = connectedListener;
		this.onDisconnectedListener = disconnectedListener;
		this.onReceiveDataListener = receiveDataListener;
            wifiIP = WiFiUtil.getRouterIP(context);
		if (tcpService != null) {
            tcpService.setOnDisconnectedListener(onDisconnectedListener);
            tcpService.setOnReceiveDataListener(onReceiveDataListener);
			IntentFilter intentFilter = new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
			context.registerReceiver(wiFiConnectBroadcastReceiver, intentFilter);
		}
	}

	/**
	 * 销毁TCP连接
	 */
	public void stop(Context context) {
        try {
            context.unregisterReceiver(wiFiConnectBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (tcpService != null) {
            tcpService.close();
        }
    }

	/**
	 * 是否已建立连接
	 * @return true：已连接；false：未连接
	 */
	public boolean isConnect() {
		if (tcpService != null) {
			return tcpService.isConnect();
		}
		Log.d(TAG, " isConnect:false");
		return false;
	}

    /**
     * 发送命令数据
     *
     * @param cmd 命令数据
     * @return true：发送成功；false：发送失败
     */
    public boolean sendCmd(String cmd) {
        Log.d(TAG, "send to " + wifiIP + " CMD--> " + cmd);
        if (tcpService == null) {
            return false;
        }

        if (!isConnect()) {
            tcpService.connectIP(wifiIP);
            return false;
        }

        return tcpService.sendData(cmd);
    }


}
