package com.eegsmart.imagetransfer.model;

public class CameraInfo {

    private int whiteBalance;
    private String version;
    private int exposure;
    private int brightness;
    private int contrast;
    private int flash;
    private int recordFlash;//录像时的闪光灯
    private String wifiName;
    private String wifiPassword;
    private boolean isCharge;//充电状态
    private int power;//电量

    public enum FLASH_TYPE {
        ALWAYS_CLOSE, ALWAYS_OPEN, NORMAL_ONCE
    }

    public enum STS_BATTERY {
        STS_CHAGE_NO, //不充电
        STS_CHAGE_YSE  //在充电
    }

    public CameraInfo() {
        super();
    }

    public CameraInfo(int whiteBalance) {
        this.whiteBalance = whiteBalance;
    }

    public int getWhiteBalance() {
        return whiteBalance;
    }

    public void setWhiteBalance(int whiteBalance) {
        this.whiteBalance = whiteBalance;
    }


    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public int getExposure() {
        return exposure;
    }

    public void setExposure(int exposure) {
        this.exposure = exposure;
    }


    public int getBrightness() {
        return brightness;
    }


    public void setBrightness(int brightness) {
        this.brightness = brightness;
    }

    public int getContrast() {
        return contrast;
    }


    public void setContrast(int contrast) {
        this.contrast = contrast;
    }

    public String getWifiName() {
        return wifiName;
    }

    public void setWifiName(String wifiName) {
        this.wifiName = wifiName;
    }

    public String getWifiPassword() {
        return wifiPassword;
    }

    public void setWifiPassword(String wifiPassword) {
        this.wifiPassword = wifiPassword;
    }

    public int getFlash() {
        return flash;
    }

    public void setFlash(int flash) {
        this.flash = flash;
    }

    public void setCharge(CameraInfo CameraInfo) {
        this.isCharge = CameraInfo.isCharge();
        this.power = CameraInfo.getPower();
    }

    public boolean isCharge() {
        return isCharge;
    }

    public void setCharge(boolean charge) {
        isCharge = charge;
    }

    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    @Override
    public String toString() {
        return "CameraInfo{" +
                "whiteBalance=" + whiteBalance +
                ", version='" + version + '\'' +
                ", exposure=" + exposure +
                ", brightness=" + brightness +
                ", contrast=" + contrast +
                ", flash=" + flash +
                ", wifiName='" + wifiName + '\'' +
                ", wifiPassword='" + wifiPassword + '\'' +
                '}';
    }
}
