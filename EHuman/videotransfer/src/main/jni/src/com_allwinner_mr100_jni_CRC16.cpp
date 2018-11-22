//
// Created by lidongxing on 2017/7/19.
//
#include <jni.h>
#include <string>

uint16_t awlink_crc16_init()
{
    return 0xffff;
}

uint16_t awlink_crc16_update(uint8_t data,uint16_t crc)
{
    uint8_t tmp;

    tmp = data ^ (uint8_t)(crc & 0xff);
    tmp ^= (tmp<<4);
    crc = (crc>>8) ^ (tmp<<8) ^ (tmp <<3) ^ (tmp>>4);

    return crc;
}

extern "C" {
    JNIEXPORT jint JNICALL
    Java_com_allwinner_mr100_jni_CRC16_getChecksum(JNIEnv *env, jclass type, jbyteArray data_) {
        if (data_ != NULL) {
            jbyte *data = env->GetByteArrayElements(data_, NULL);
            jsize len = env->GetArrayLength(data_); //获取长度

            uint16_t checksum = 0;
            uint8_t count = 0;

            checksum = awlink_crc16_init();
            for (count = 0; count < len; count++) {
                //printf("awlink_encode:[%d]%x \r\n",count,msg->data[count]);
                checksum = awlink_crc16_update(data[count], checksum);
            }

            env->ReleaseByteArrayElements(data_, data, 0);

            return checksum;
        } else {
            return 0;
        }
    }
}