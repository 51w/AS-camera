package com.eegsmart.imagetransfer.model;

/**
 * Created by lidongxing on 2017/10/30.
 */

public enum PreviewResolutionMode {
    R_720P(0),//default
    R_VGA(1);

    private int value;

    PreviewResolutionMode(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static PreviewResolutionMode fromValue(int value) {
        PreviewResolutionMode mode = R_720P;

        switch (value) {
            case 0:
                mode = R_720P;
                break;
            case 1:
                mode = R_VGA;
                break;
        }

        return mode;
    }
}
