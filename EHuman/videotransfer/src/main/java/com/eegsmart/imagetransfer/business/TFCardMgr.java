package com.eegsmart.imagetransfer.business;

/**
 * 无人机上的TF卡
 * Created by Rust on 2018/5/22.
 */
public class TFCardMgr {

    public static boolean mCardOnline = false; // 无人机上是否有TF卡

   public static int mFreeSpaceMb; // 可用空间   单位 mb

   public static int mUsedSpaceMb;

   public static int mTotalSpaceMb;

    public static float getFreeSpaceGb() {
        return mFreeSpaceMb / 1000.0f;
    }

    public static float getUsedSpaceGb() {
        return mUsedSpaceMb / 1000.0f;
    }

    public static float getTotalSpaceGb() {
        return mTotalSpaceMb / 1000.0f;
    }

}
