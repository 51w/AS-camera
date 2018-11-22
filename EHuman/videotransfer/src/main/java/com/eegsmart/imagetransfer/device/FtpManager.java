package com.eegsmart.imagetransfer.device;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.Log;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.LinkedList;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

public class FtpManager {
    public static final String FTP_UPLOAD_LOADING = "ftp_upload_loading";
    public static final String FTP_CONNECT_SUCCESSS = "FTP_CONNECT_SUCCESSS";
    public static final String FTP_CONNECT_FAIL = "FTP_CONNECT_FAIL";
    public static final String FTP_DISCONNECT_SUCCESS = "FTP_DISCONNECT_SUCCESS";
    public static final String FTP_FILE_NOTEXISTS = "FTP_FILE_NOTEXISTS";
    public static final String FTP_UPLOAD_SUCCESS = "FTP_UPLOAD_SUCCESS";
    public static final String FTP_UPLOAD_FAIL = "FTP_UPLOAD_FAIL";
    public static final String FTP_DOWN_LOADING = "FTP_DOWN_LOADING";
    public static final String FTP_DOWN_SUCCESS = "FTP_DOWN_SUCCESS";
    public static final String FTP_DOWN_FAIL = "FTP_DOWN_FAIL";
    public static final String FTP_DELETEFILE_SUCCESS = "FTP_DELETEFILE_SUCCESS";
    public static final String FTP_DELETEFILE_FAIL = "FTP_DELETEFILE_FAIL";

    private String hostName;

    private int serverPort;

    private String userName;

    private String password;

    private FTPClient ftpClient;

    private Context context;
    private static FtpManager ftpManager;

    public FtpManager(Context context) {
        this.context = context;
        this.hostName = getRouterIP(context);
        this.serverPort = 21;
        this.userName = "AW819";
        this.password = "1663819";
        this.ftpClient = new FTPClient();
    }

    public static FtpManager getInstance(Context context) {
        if (ftpManager == null) {
            ftpManager = new FtpManager(context);
        }
        return ftpManager;
    }

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

    private static String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "."
                + ((i >> 24) & 0xFF);
    }

    /**
     * 上传单个文件.
     *
     * @param singleFile 本地文件
     * @param remotePath FTP目录 \mnt
     * @param listener   监听器
     * @throws IOException
     */
    public void uploadSingleFile(File singleFile, String remotePath,
                                 UploadProgressListener listener) throws IOException {

        this.uploadBeforeOperate(remotePath, listener);

        boolean flag;

        flag = uploadingSingle(singleFile, listener);
        if (flag) {
            listener.onUploadProgress(FTP_UPLOAD_SUCCESS, 0,
                    singleFile);
        } else {
            listener.onUploadProgress(FTP_UPLOAD_FAIL, 0,
                    singleFile);
        }

        this.uploadAfterOperate(listener);
    }

    public void uploadMultiFile(LinkedList<File> fileList, String remotePath,
                                UploadProgressListener listener) throws IOException {

        this.uploadBeforeOperate(remotePath, listener);

        boolean flag;

        for (File singleFile : fileList) {
            flag = uploadingSingle(singleFile, listener);
            if (flag) {
                listener.onUploadProgress(FTP_UPLOAD_SUCCESS, 0,
                        singleFile);
            } else {
                listener.onUploadProgress(FTP_UPLOAD_FAIL, 0,
                        singleFile);
            }
        }

        this.uploadAfterOperate(listener);
    }

    private boolean uploadingSingle(File localFile,
                                    UploadProgressListener listener) throws IOException {
        boolean flag = true;
        BufferedInputStream buffIn = new BufferedInputStream(
                new FileInputStream(localFile));
        ProgressInputStream progressInput = new ProgressInputStream(buffIn,
                listener, localFile);
        flag = ftpClient.storeFile(localFile.getName(), progressInput);
        buffIn.close();

        return flag;
    }


    private void uploadBeforeOperate(String remotePath,
                                     UploadProgressListener listener) throws IOException {

        try {
            this.openConnect();
            listener.onUploadProgress(FTP_CONNECT_SUCCESSS, 0,
                    null);
        } catch (IOException e1) {
            e1.printStackTrace();
            listener.onUploadProgress(FTP_CONNECT_FAIL, 0, null);
            return;
        }

        ftpClient.setFileTransferMode(org.apache.commons.net.ftp.FTP.STREAM_TRANSFER_MODE);
        ftpClient.makeDirectory(remotePath);
        ftpClient.changeWorkingDirectory(remotePath);

    }

    private void uploadAfterOperate(UploadProgressListener listener)
            throws IOException {
        this.closeConnect();
        listener.onUploadProgress(FTP_DISCONNECT_SUCCESS, 0, null);
    }

    public void downloadSingleFile(String serverPath, String localPath,
                                   String fileName, DownLoadProgressListener listener)
            throws Exception {

        try {
            this.openConnect();
            listener.onDownLoadProgress(FTP_CONNECT_SUCCESSS, 0,
                    null);
        } catch (IOException e1) {
            e1.printStackTrace();
            listener.onDownLoadProgress(FTP_CONNECT_FAIL, 0, null);
            return;
        }

        FTPFile[] files = ftpClient.listFiles(serverPath);
        if (files.length == 0) {
            listener.onDownLoadProgress(FTP_FILE_NOTEXISTS, 0,
                    null);
            return;
        }

        File mkFile = new File(localPath);
        if (!mkFile.exists()) {
            mkFile.mkdirs();
        }

        localPath = localPath + fileName;
        long serverSize = files[0].getSize();
        File localFile = new File(localPath);
        long localSize = 0;
        if (localFile.exists()) {
            localSize = localFile.length();
            if (localSize >= serverSize) {
                File file = new File(localPath);
                file.delete();
            }
        }

        long step = serverSize / 100;
        long process = 0;
        long currentSize = 0;
        OutputStream out = new FileOutputStream(localFile, true);
        ftpClient.setRestartOffset(localSize);
        InputStream input = null;
        input = ftpClient.retrieveFileStream(serverPath);
        byte[] b = new byte[1024];
        int length = 0;
        while ((length = input.read(b)) != -1) {
            out.write(b, 0, length);
            currentSize = currentSize + length;
            if (currentSize / step != process) {
                process = currentSize / step;
                if (process % 5 == 0) {
                    listener.onDownLoadProgress(FTP_DOWN_LOADING,
                            process, null);
                }
            }
        }
        out.flush();
        out.close();
        input.close();

        if (ftpClient.completePendingCommand()) {
            listener.onDownLoadProgress(FTP_DOWN_SUCCESS, 0,
                    new File(localPath));
        } else {
            listener.onDownLoadProgress(FTP_DOWN_FAIL, 0, null);
        }

        this.closeConnect();
        listener.onDownLoadProgress(FTP_DISCONNECT_SUCCESS, 0,
                null);

        return;
    }


    public void deleteSingleFile(String serverPath,
                                 DeleteFileProgressListener listener) throws Exception {

        // ��FTP����
        try {
            this.openConnect();
            listener.onDeleteProgress(FTP_CONNECT_SUCCESSS);
        } catch (IOException e1) {
            e1.printStackTrace();
            listener.onDeleteProgress(FTP_CONNECT_FAIL);
            return;
        }

        FTPFile[] files = ftpClient.listFiles(serverPath);
        if (files.length == 0) {
            listener.onDeleteProgress(FTP_FILE_NOTEXISTS);
            return;
        }

        boolean flag = true;
        flag = ftpClient.deleteFile(serverPath);
        if (flag) {
            listener.onDeleteProgress(FTP_DELETEFILE_SUCCESS);
        } else {
            listener.onDeleteProgress(FTP_DELETEFILE_FAIL);
        }

        this.closeConnect();
        listener.onDeleteProgress(FTP_DISCONNECT_SUCCESS);
    }


    public void openConnect() throws IOException {
        ftpClient.setControlEncoding("UTF-8");
        int reply;
        ftpClient.connect(hostName, serverPort);
        reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
            throw new IOException("connect fail: " + reply);
        }
        ftpClient.login(userName, password);
        reply = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(reply)) {
            ftpClient.disconnect();
            throw new IOException("connect fail: " + reply);
        } else {
            FTPClientConfig config = new FTPClientConfig(ftpClient
                    .getSystemType().split(" ")[0]);
            config.setServerLanguageCode("zh");
            ftpClient.configure(config);
            ftpClient.enterLocalPassiveMode();
            ftpClient
                    .setFileType(org.apache.commons.net.ftp.FTP.BINARY_FILE_TYPE);
        }
    }


    public void closeConnect() throws IOException {
        if (ftpClient != null) {
            ftpClient.logout();
            ftpClient.disconnect();
        }
    }

    public interface UploadProgressListener {
        public void onUploadProgress(String currentStep, long uploadSize,
                                     File file);
    }

    public interface DownLoadProgressListener {
        public void onDownLoadProgress(String currentStep, long downProcess,
                                       File file);
    }

    public interface DeleteFileProgressListener {
        public void onDeleteProgress(String currentStep);
    }

    public class ProgressInputStream extends InputStream {
        private static final int TEN_KILOBYTES = 1024 * 10;

        private InputStream inputStream;

        private long progress;
        private long lastUpdate;

        private boolean closed;

        private UploadProgressListener listener;
        private File localFile;

        public ProgressInputStream(InputStream inputStream, UploadProgressListener listener, File localFile) {
            this.inputStream = inputStream;
            this.progress = 0;
            this.lastUpdate = 0;
            this.listener = listener;
            this.localFile = localFile;

            this.closed = false;
        }

        @Override
        public int read() throws IOException {
            int count = inputStream.read();
            return incrementCounterAndUpdateDisplay(count);
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int count = inputStream.read(b, off, len);
            return incrementCounterAndUpdateDisplay(count);
        }

        @Override
        public void close() throws IOException {
            super.close();
            if (closed)
                throw new IOException("already closed");
            closed = true;
        }

        private int incrementCounterAndUpdateDisplay(int count) {
            if (count > 0)
                progress += count;
            lastUpdate = maybeUpdateDisplay(progress, lastUpdate);
            return count;
        }

        private long maybeUpdateDisplay(long progress, long lastUpdate) {
            if (progress - lastUpdate > TEN_KILOBYTES) {
                lastUpdate = progress;
                this.listener.onUploadProgress(FTP_UPLOAD_LOADING, progress, this.localFile);
            }
            return lastUpdate;
        }


    }

}
