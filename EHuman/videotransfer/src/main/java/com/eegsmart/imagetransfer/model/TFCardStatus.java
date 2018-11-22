package com.eegsmart.imagetransfer.model;

/**
 * Created by lidongxing on 2017/11/15.
 */

public enum  TFCardStatus {
    TF_CARD_UNPLUGGED,     // 卡拔出
    TF_CARD_MOUNT_SUCCESS, // 卡插入挂载成功
    TF_CARD_MOUNT_FAILED,  // 卡插入但挂载失败
    TF_CARD_UNSPPORT;      // 卡插入但容量过大，系统不支持

    public static TFCardStatus fromIntValue(int intValue) {
        TFCardStatus status = TF_CARD_UNPLUGGED;

        switch (intValue) {
            case 0:
                status = TF_CARD_UNPLUGGED;
                break;
            case 1:
                status = TF_CARD_MOUNT_SUCCESS;
                break;
            case 2:
                status = TF_CARD_MOUNT_FAILED;
                break;
            case 3:
                status = TF_CARD_UNSPPORT;
                break;
        }

        return status;
    }

    public int getIntValue() {
        return this.ordinal();
    }
}
