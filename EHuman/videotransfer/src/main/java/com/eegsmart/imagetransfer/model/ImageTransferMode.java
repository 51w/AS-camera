package com.eegsmart.imagetransfer.model;

/**
 * 图传模式
 * Created by lidongxing on 2017/10/30.
 */
public enum ImageTransferMode {
    ACS(0),//default
    FCS(1),
    RA(2);

    private int value;

    ImageTransferMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static ImageTransferMode fromValue(int value) {
        ImageTransferMode mode = ACS;

        switch (value) {
            case 0:
                mode = ACS;
                break;
            case 1:
                mode = FCS;
                break;
            case 2:
                mode = RA;
                break;
        }

        return mode;
    }
}
