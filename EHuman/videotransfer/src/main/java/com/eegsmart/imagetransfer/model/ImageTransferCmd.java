package com.eegsmart.imagetransfer.model;

/**
 * Created by lidongxing on 2017/11/3.
 */

public enum ImageTransferCmd {

    /*-----------------client->server:active request-----------------*/
    CMD_REQ_SYS_PARAM_GET ,            	 // 系统参数同步请求
    CMD_REQ_STATE_SET,							 	 // 界面状态更新请求:摄像状态--拍摄界面/设置界面:0;文件管理状态-- 图片/视频/缩略图/图片预览/视频播放:1
    CMD_REQ_VID_ENC_PREVIEW_ON,						 // 图传开启请求
    CMD_REQ_VID_ENC_PREVIEW_OFF,					 // 图传关闭请求
    CMD_REQ_VID_ENC_START,							 // 录像开启请求
    CMD_REQ_VID_ENC_STOP,							 // 录像关闭请求
    CMD_REQ_VID_ENC_PAUSE,							 // 录像暂停请求
    CMD_REQ_VID_ENC_RESUME,							 // 录像恢复请求

    CMD_REQ_VID_ENC_DURATION_SET,					 // 录像视频时长设置请求
    CMD_REQ_VID_ENC_TIME_LAPSE_SET,					 // 缩时录影设置请求
    CMD_REQ_VID_ENC_SLOW_SET,//10					 // 慢摄影设置请求

    CMD_REQ_VID_ENC_CAPTURE,						 // 拍照请求
    CMD_REQ_AUD_ENC_VOLUME_SET,						 // 调节录像录音音量请求

    CMD_REQ_VID_ENC_CAPTURE_TIMER_SHOT_SET,			 // 定时拍照设置请求
    CMD_REQ_VID_ENC_CAPTURE_AUTO_SHOT_SET,			 // 自动拍照设置请求
    CMD_REQ_VID_ENC_CAPTURE_SPORT_ORBIT_SHOT_SET,    // 运动轨迹拍照设置请求

    CMD_REQ_RESOLUTION_SET,							 // 镜头分辨率设置请求
    CMD_REQ_AUTO_AWB_SET, 							 // 镜头自动白平衡设置请求
    CMD_REQ_AE_SET,									 // 镜头自动曝光设置请求
    CMD_REQ_AG_SET,									 // 镜头自动增益设置请求
    CMD_REQ_HUE_SET,// 20							 // 镜头色彩设置请求
    CMD_REQ_SATURATION_SET,	 						 // 镜头饱和度设置请求
    CMD_REQ_BRIGHTNESS_SET,				 			 // 镜头亮度设置请求
    CMD_REQ_CONTRAST_SET,							 // 镜头对比度设置请求
    CMD_REQ_SHAPNESS_SET,							 // 镜头锐利度设置请求
    CMD_REQ_ISO_SET,								 // 镜头感光度设置请求
    CMD_REQ_FLIP_SET,								 // 镜头翻转设置请求
    CMD_REQ_AUTO_FOCUS_SET,							 // 镜头自动聚焦请求
    CMD_REQ_FRM_RATE_SET, 							 // 镜头采集图像帧率设置请求

    CMD_REQ_DATE_TIME_SET,							 // 日期/时间设置请求
    CMD_REQ_GSENSOR_SET,//30							 // G-Sensor灵敏度设置请求
    CMD_REQ_TIME_WATER_MARK_SET,					 // 时间水印显示开关设置请求
    CMD_REQ_LED_IND_SET,							 // LED指示灯设置请求
    CMD_REQ_LIGHT_FREQ_SET,							 // 镜头光源频率设置请求
    CMD_REQ_SCREEN_FLIP_SET,						 // LCD屏幕旋转设置请求
    CMD_REQ_VEHICLE_MODE_SET,						 // 车载模式设置请求
    CMD_REQ_AUTO_PWR_OFF_SET,						 // 自动关机延迟时间设置请求
    CMD_REQ_AUTO_SCREEN_SAVER_SET,					 // 自动屏保延迟时间设置请求

    CMD_REQ_FACTORY_RESTORE_SET,					 // 恢复出厂设置请求
    CMD_REQ_FIRMWARE_VERSION_GET,					 // 获取固件版本请求
    CMD_REQ_PWR_OFF,//40								 // 手动关机请求

    CMD_REQ_DISP_JPG_THUMB_LIST_DEC,				 // 获取图片缩略图请求
    CMD_REQ_DISP_JPG,								 // 预览图片请求
    CMD_REQ_DISP_VID_THUMB_LIST_DEC,				 // 获取视频缩略图请求

    CMD_REQ_VID_DEC_START,							 // 播放视频请求
    CMD_REQ_VID_DEC_STOP,							 // 停止播放视频请求
    CMD_REQ_VID_DEC_PAUSE,							 // 暂停播放视频请求
    CMD_REQ_VID_DEC_RESUME,							 // 恢复播放视频请求
    CMD_REQ_VID_DEC_SEEK,							 // 跳播视频请求

    CMD_REQ_AUD_DEC_START,							 // 播放音频请求
    CMD_REQ_AUD_DEC_STOP,//50						 // 停止播放音频请求
    CMD_REQ_AUD_DEC_PAUSE,							 // 暂停播放音频请求
    CMD_REQ_AUD_DEC_RESUME,							 // 恢复播放音频请求
    CMD_REQ_AUD_DEC_VOLUME_SET,						 // 调节播放音频音量请求
    CMD_REQ_AUD_DEC_FF,								 // 播放音频快进请求
    CMD_REQ_AUD_DEC_FB,								 // 播放音频快退请求

    CMD_REQ_DEL_FILE, 								 // 删除文件列表请求
    CMD_REQ_DEL_ALL_FILE,						     // 删除所有文件请求
    CMD_REQ_LOCK_FILE,								 // 加锁文件列表请求
    CMD_REQ_LOCK_ALL_FILE,							 // 加锁所有文件请求
    CMD_REQ_UNLOCK_FILE,//60							 // 解锁文件列表请求
    CMD_REQ_UNLOCK_ALL_FILE,						 // 解锁所有文件请求
    CMD_REQ_FORMAT,									 // 格式化TF卡请求
    CMD_REQ_CARD_INFO_GET,							 // 获取TF卡容量信息请求
    CMD_REQ_MANUAL_LOCK,							 // 手动加锁视频请求
    CMD_REQ_RECORDING_TIME_GET,						 // 获取当前录像时间请求
    CMD_REQ_PLAYING_TIME_GET,						 // 获取播放时间请求
    CMD_REQ_NET_DISCONN,						     // 网络连接断开请求
    CMD_REQ_NET_SSID,								 //net name
    CMD_REQ_NET_PASSWORD,
    CMD_REQ_ENC_CAPTURE_SET_NUM,//70			     //连拍设置
    CMD_REQ_FIRMWARE_UPDATE, 					     //版本更新命令，上传版本固件后发的命令
    CMD_CFM_X_Y_UPDATE,					 			 //follow me
    CMD_REQ_PREVIEW_RESOLUTION,                      //预览分辨率
    CMD_REQ_FLLOW_ME_ON,                             //开启跟随
    CMD_REQ_FLLOW_ME_OFF,                            //结束跟随
    CMD_IND_SD_STS_UPDATE  //SDcard上报
}
