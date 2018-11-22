package com.eegsmart.imagetransfer.business;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.Log;

import com.eegsmart.imagetransfer.listener.OnDisconnectedListener;
import com.eegsmart.imagetransfer.listener.OnReceiveDataListener;
import com.eegsmart.imagetransfer.util.WiFiUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class TcpService {
    private static final String TAG = "TcpService";

    private static int TCP_PORT = 0;// 4646//9696//6868

//    private String testIP = "172.20.11.16";

//    private static boolean isTest = false;
    private static Socket socket = null;
    private BufferedReader input = null;
    private BufferedWriter writer = null;
    private static SocketAddress address;
    private static String socketServerAddress;
    private static WifiManager manager;
    private static ReceiveThread receiveThread;

    private boolean isConnect = false;

    private OnReceiveDataListener onReceiveDataListener;
    private OnDisconnectedListener onDisconnectedListener;

    public static void setTcpPort(int tcpPort) {
        TCP_PORT = tcpPort;
    }

    /**
     * 开启连接
     */
    public boolean connect(Context context) {
        Log.d(TAG, "TcpService connect();  isConnect:" + isConnect);
        if (!isConnect) {
            try {
                manager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

                if (null != manager && manager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
                    return false;
                }

                socket =  new Socket();
                socketServerAddress = WiFiUtil.getRouterIP(context);
//                Log.d(TAG, "socketServerAddress:" + socketServerAddress);
                address = new InetSocketAddress(socketServerAddress, TCP_PORT);

                if (address != null) {
                    if (socket != null && !socket.isConnected()) {
                        socket.connect(address, 3000);
                        Thread.sleep(500); // es debug - 等待一会
                    }
                } else {
                    close();
                    return false;
                }
            }
            catch (Exception e) {
                Log.e(TAG, "[TcpService] try to connect fail:", e);
               /* cmdControl.getClientController()
                        .upData(Constants.TCP_FAIL_CONNECT);*/
                close();
                return false;
            }

            isConnect = true;
            Log.d(TAG, "[TcpService] isConnect! socket=" + socket);
            if (receiveThread == null) {
                receiveThread = new ReceiveThread();
                receiveThread.start();
                Log.d(TAG, "[TcpService] 启动数据接收线程 " + receiveThread);
            } else {
                receiveThread.setTag(false);
                receiveThread.interrupt();
                receiveThread = null;
                receiveThread = new ReceiveThread();
                receiveThread.start();
                Log.d(TAG, "[TcpService] 重启数据接收线程 " + receiveThread);
            }
        }

        return isConnect;
    }

    /**
     * @param ip 开启连接
     */
    public boolean connectIP(String ip) {
        Log.d(TAG, "TcpService connect();  isConnect:" + isConnect);
        if (TextUtils.isEmpty(ip)) {
            isConnect = false;
            close();
            return isConnect;
        }
        if (!isConnect) {
            try {
                socket = new Socket();
                socketServerAddress = ip;
                address = new InetSocketAddress(socketServerAddress, TCP_PORT);
                if (socket != null && !socket.isConnected()) {
                    socket.connect(address, 3000);
                }
            } catch (Exception e) {
                Log.e(TAG, "[TcpService] try to connect fail:", e);
                close();
                return false;
            }
            isConnect = true;
            Log.d(TAG, "[TcpService] isConnect! socket=" + socket);
            if (receiveThread == null) {
                receiveThread = new ReceiveThread();
                receiveThread.start();
                Log.d(TAG, "[TcpService] 启动数据接收线程 " + receiveThread);
            } else {
                receiveThread.setTag(false);
                receiveThread.interrupt();
                receiveThread = null;
                receiveThread = new ReceiveThread();
                receiveThread.start();
                Log.d(TAG, "[TcpService] 重启数据接收线程 " + receiveThread);
            }
        }
        return isConnect;
    }

    /**
     * 关闭连接
     */
    public void close() {
        Log.d(TAG, "	close");
        if (socket != null) {
            try {
                isConnect = false;
                Log.d(TAG, "[TcpService] try socket.close()");

                if (input != null) {
                    input.close();
                    input = null;
                }

                if (writer != null) {
                    writer.close();
                    writer = null;
                }

                try {
                    socket.close();
                    socket = null;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (receiveThread != null) {
                    receiveThread.setTag(true);
                    receiveThread = null;
                }
            }
            catch (Exception e) {
                Log.e(TAG, "关闭连接时异常", e);
            }
        }
    }

    /**
     * 发送数据
     */
    public boolean sendData(String socketData) {
        boolean result = false;

        if (isConnect) {
            try {
                Log.d(TAG, "BufferedWriter: ");
                if (writer == null) {
                    writer = new BufferedWriter(
                            new OutputStreamWriter(socket.getOutputStream()));
                }

                String content = socketData.replace("\n", " ") + "\n";
                Log.i(TAG, "content:" + content);

                writer.write(content);
                writer.flush();

                result = true;
            }
            catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "sendData - connect fail:", e);

                close();

                result = false;
            }
        }

        Log.d(TAG, "sendData end ");
        return result;
    }

    public void setOnReceiveDataListener(OnReceiveDataListener onReceiveDataListener) {
        this.onReceiveDataListener = onReceiveDataListener;
    }

    public void setOnDisconnectedListener(OnDisconnectedListener onDisconnectedListener) {
        this.onDisconnectedListener = onDisconnectedListener;
    }

    /**
     * 数据接收线程
     */
    class ReceiveThread extends Thread {
        private boolean isTag = false;

        public boolean isTag()
        {
            return isTag;
        }

        public void setTag(boolean isTag)
        {
            this.isTag = isTag;
        }

        @Override
        public void run() {
            int length = 0;

            while (!isTag) {
                Log.d(TAG, "[TcpService] ReceiveThread running: " + this.toString());
                try {
                    String result;
                    DataInputStream dinput;

                    if (socket == null || socket.getInputStream() == null) {
                        isTag = true;
                        Log.d(TAG, "[TcpService] ReceiveThread - socket:" + socket);
                        close();
                        return;
                    }

                    dinput = new DataInputStream(socket.getInputStream());

                    int len = 0;
                    StringBuffer sb = new StringBuffer();
                    byte bytes[] = new byte[1024];
                    while ((len = dinput.read(bytes)) != -1) {
                        sb.append(new String(bytes, 0, len));
                        // Log.d(TAG, " append: " + len
                        // + " " + sb.toString());
                        Log.d(TAG, " append: " + len /*
                                                                 * + " sb:" + sb
																 */);
                        break;
                    }

                    if (len > 0) {
                        length += len;
                        Log.d(TAG, " length: " + length + " len " + len);

                        if (dinput != null) {
                            Log.d(TAG, " result1: "
                                    + sb.toString());
                            result = sb.toString()
                                    .substring(0, sb.toString()
                                            .lastIndexOf("}")
                                            + 1);
                            Log.d(TAG, " result2: " + result);

                            if(onReceiveDataListener!=null) {
                                onReceiveDataListener.onReceiveData(result);
                            }
                        }
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                    isTag = true;
                    Log.e(TAG, "[TcpService] ReceiveThread", e);

                    if(onDisconnectedListener!=null) {
                        onDisconnectedListener.onDisconnected();
                    }

                    close();
                }
            }
        }
    }

    public boolean isConnect() {
        return isConnect;
    }
}
