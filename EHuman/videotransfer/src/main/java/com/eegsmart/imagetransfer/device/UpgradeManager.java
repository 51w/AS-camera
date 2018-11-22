package com.eegsmart.imagetransfer.device;

import android.content.Context;
import android.util.Log;

import com.eegsmart.imagetransfer.controller.CmdController;
import com.eegsmart.imagetransfer.listener.CmdResultListener;
import com.eegsmart.imagetransfer.util.WiFiUtil;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 固件升级
 * Created by lidongxing on 2017/8/30.
 */
public class UpgradeManager {
    private static final String TAG = "UpgradeManager";
    private static final String UPGRADE_FILE_PRIFIX = "update-";
    private static final String UPGRADE_FILE_POSTFIX = ".zip";

    private List<String> firmwareFilenameList;    // assets中的文件
    private List<String> firmwareFileAbsPathList; // 存储中的固件路径

    private UpgradeListener listener;

    public UpgradeManager(UpgradeListener listener) {
        firmwareFilenameList = new ArrayList<>();
        firmwareFileAbsPathList = new ArrayList<>();
        this.listener = listener;
    }

    public void addFirmwareFilename(String filename) {
        firmwareFilenameList.add(filename);
    }

    public void addFirmwareFileAbsPath(String absPath) {
        firmwareFileAbsPathList.add(absPath);
    }

    public void clearFirmwareFilename() {
        firmwareFilenameList.clear();
    }

    public void clearFirmwareFileAbsPath() {
        firmwareFileAbsPathList.clear();
    }

    public void checkVersion(Context context, String deviceVersion) {
        try {
            String localVersion = getLocalVersion(context);
            Log.d(TAG, "device version:" + deviceVersion);
            Log.d(TAG, "local version:" + localVersion);

            String[] deviceVersions = deviceVersion.split("\\.");
            String[] localVersions = localVersion.split("\\.");

            if (Integer.parseInt(localVersions[0]) > Integer.parseInt(deviceVersions[0])) {
                listener.onCheckNewVersion(localVersion);
            } else if (Integer.parseInt(localVersions[0]) == Integer.parseInt(deviceVersions[0])) {
                if (Integer.parseInt(localVersions[1]) > Integer.parseInt(deviceVersions[1])) {
                    listener.onCheckNewVersion(localVersion);
                } else if (Integer.parseInt(localVersions[1]) == Integer.parseInt(deviceVersions[1])) {
                    if (Integer.parseInt(localVersions[2]) > Integer.parseInt(deviceVersions[2])) {
                        listener.onCheckNewVersion(localVersion);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    /**
     * 通过预先存储的固件路径进行升级
     */
    public void startUpgradeByAbsPath(final Context context,final String versionName) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int fCount = 0;
                    for (String firmwareAbsPath : firmwareFileAbsPathList) {
                        File fFile = new File(firmwareAbsPath);
                        if (fFile.exists()) {
                            if (ftpUpload(WiFiUtil.getRouterIP(context), 21, "AW819", "1663819", "/",
                                    fFile.getAbsolutePath(), fFile.getName())) {
                                fCount++;
                                listener.onUploadFileSuccess(fFile.getName(), fCount, firmwareFileAbsPathList.size());
                            } else {
                                listener.onUploadFailed();
                                break;
                            }
                        } else {
                            listener.onUploadFailed();
                            break;
                        }
                    }
                    if (fCount == firmwareFileAbsPathList.size()) {
                        sendUpgradeCmd(versionName);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onUpgradeFailed();
                }
            }
        }).start();
    }

    public void startUpgrade(final Context context, final String firmwareVersion) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int fCount = 0;
                    for (String firmwareFilename : firmwareFilenameList) {
                        File fFile = new File(copyFileFromAssetsToCacheDir(context, firmwareFilename));
                        if (fFile.exists()) {
                            if (ftpUpload(WiFiUtil.getRouterIP(context), 21, "AW819", "1663819", "/",
                                    fFile.getAbsolutePath(), fFile.getName())) {
                                fCount++;
                                listener.onUploadFileSuccess(fFile.getName(), fCount, firmwareFilenameList.size());
                            } else {
                                listener.onUploadFailed();
                                break;
                            }
                        } else {
                            listener.onUploadFailed();
                            break;
                        }
                    }
                    if (fCount == firmwareFilenameList.size()) {
                        sendUpgradeCmd(firmwareVersion);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    listener.onUpgradeFailed();
                }
//                final File file1;
//                final File file2;
//                try {
//                    file1 = new File(copyFileFromAssetsToCacheDir(context, UPGRADE_FILE_1));
//                    file2 = new File(copyFileFromAssetsToCacheDir(context, UPGRADE_FILE_2));
//                    Log.d(TAG, "upload file:" + file1.getAbsolutePath());
//                    if (file1.exists()) {
//                        if (ftpUpload(WiFiUtil.getRouterIP(context), 21, "AW819", "1663819", "/",
//                                file1.getAbsolutePath(), file1.getName())) {
//                            listener.onUploadFileSuccess(file1.getName(), 1, FIRMWARE_FILE_TOTAL_COUNT);
//                        } else {
//                            listener.onUploadFailed();
//                            return;
//                        }
//                        if (file2.exists()) {
//                            if (ftpUpload(WiFiUtil.getRouterIP(context), 21, "AW819", "1663819", "/",
//                                    file2.getAbsolutePath(), file2.getName())) {
//                                listener.onUploadFileSuccess(file2.getName(), 2, FIRMWARE_FILE_TOTAL_COUNT);
//                            sendUpgradeCmd(firmwareVersion);
//                            } else {
//                                listener.onUploadFailed();
//                            }
//                        } else {
//                            listener.onUploadFailed();
//                        }
//                    } else {
//                        listener.onUploadFailed();
//                    }
//                } catch (IOException e) {
//                    Log.e(TAG, "upgrade failed: " + e.getMessage());
//                    listener.onUpgradeFailed();
//                }
            }
        }).start();
    }

    /**
     * 通过ftp上传文件
     *
     * @param host         ftp服务器地址 如： 192.168.1.110
     * @param port         端口如 ： 21
     * @param username     登录名
     * @param password     密码
     * @param remotePath   上到ftp服务器的磁盘路径
     * @param fileNamePath 要上传的文件
     * @param fileName     要上传的文件名
     */
    private boolean ftpUpload(String host, int port, String username, String password, String remotePath, String fileNamePath, final String fileName) {
        FTPClient ftpClient = new FTPClient();
        FileInputStream fis;
        boolean isSuccess = false;
        Log.d(TAG, "ftpUpload start");
        try {
            final long fileTotalSize = new File(fileNamePath).length();
            ftpClient.connect(host, port);
            boolean loginResult = ftpClient.login(username, password);
            int returnCode = ftpClient.getReplyCode();
            if (loginResult && FTPReply.isPositiveCompletion(returnCode)) {// 如果登录成功
                ftpClient.makeDirectory(remotePath);
                // 设置上传目录
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setBufferSize(1024);
                ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.enterLocalPassiveMode();
                fis = new FileInputStream(fileNamePath);
                ftpClient.setCopyStreamListener(new CopyStreamListener() {
                    @Override
                    public void bytesTransferred(CopyStreamEvent copyStreamEvent) {

                    }

                    @Override
                    public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {
                        listener.onUploadingFile(fileName, (int) ((totalBytesTransferred * 100.0f) / fileTotalSize));
//                        Log.d(TAG, fileName + ", " + totalBytesTransferred + "/" + fileTotalSize);
                    }
                });
                ftpClient.storeFile(fileName, fis);

                isSuccess = true;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        } finally {
            //IOUtils.closeQuietly(fis);
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        }

        return isSuccess;
    }

    private void sendUpgradeCmd(String localVersion) {
        CmdController.getInstance().startUpgrade(localVersion, new CmdResultListener() {
            @Override
            public void onSuccess(JSONObject jsonObject) {
                Log.d(TAG, "upgrade successful");
                listener.onUpgradeSuccess();
            }

            @Override
            public void onFailed() {
                Log.d(TAG, "send upload cmd failed");
                listener.onUpgradeFailed();
            }
        });
    }

    public String copyFileFromAssetsToCacheDir(Context context, String fileName) throws IOException {
        Log.d(TAG, "copyFileFromAssetsToCacheDir:" + fileName);
        InputStream is = context.getAssets().open(fileName);
        File file = new File(context.getCacheDir() + File.separator + fileName);
        if (file.exists()) {
            file.delete();
        }

        FileOutputStream fos = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int byteCount = 0;
        while ((byteCount = is.read(buffer)) != -1) {//循环从输入流读取 buffer字节
            fos.write(buffer, 0, byteCount);//将读取的输入流写入到输出流
        }

        fos.flush();//刷新缓冲区

        is.close();
        fos.close();

        return file.getAbsolutePath();
    }

    private String getLocalVersion(Context context) {
        String localVersion = null;

        try {
            String[] fileNames = context.getAssets().list("");

            for (String fileName : fileNames) {
                if (fileName.startsWith(UPGRADE_FILE_PRIFIX)
                        && fileName.endsWith(UPGRADE_FILE_POSTFIX)) {
                    localVersion = fileName.substring(UPGRADE_FILE_PRIFIX.length(),
                            fileName.length() - UPGRADE_FILE_POSTFIX.length());

                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return localVersion;
    }
}
