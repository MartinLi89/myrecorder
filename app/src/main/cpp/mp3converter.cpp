#include <jni.h>
#include <string>
#include <lame.h>
//#include "lamemp3/lame.h"
#define BUFFER_SIZE 8192

//转码对象
static lame_global_flags *lame = NULL;
long nowConvertBytes = 0;

void lameInit(jint inSampleRate,
              jint channel, jint mode, jint outSampleRate,
              jint outBitRate, jint quality);

void resetLame();

extern "C" JNIEXPORT void JNICALL
Java_com_example_martin_mytranvice_Mp3Converter_init(JNIEnv *env, jclass type, jint inSampleRate,
                                                     jint channel, jint mode, jint outSampleRate,
                                                     jint outBitRate, jint quality) {
    lameInit(inSampleRate, channel, mode, outSampleRate, outBitRate, quality);
}

//初始化转码类
void lameInit(jint inSampleRate,
              jint channel, jint mode, jint outSampleRate,
              jint outBitRate, jint quality) {
    //重置环境
    resetLame();
    //  初始化编码器引擎，返回一个lame_global_flags结构体类型指针
    //  说明编码所需内存分配完成，否则，返回NULL
    lame = lame_init();
//    LOGI("初始化lame库完成");
    // 设置输入数据流的采样率，默认为44100Hz
    lame_set_in_samplerate(lame, inSampleRate);
    // 设置输入数据流的通道数量，默认为2
    lame_set_num_channels(lame, channel);
    //设置最终mp3编码输出的声道模式，如果不设置则和输入声道数一样。
    //参数是枚举，STEREO代表双声道，MONO代表单声道。
    lame_set_mode(lame, STEREO);
    // 设置最终mp3编码输出的声音的采样率，单位KHz,如果不设置则和输入采样率一样
    lame_set_out_samplerate(lame, outSampleRate);
    // 设置比特压缩率，默认为11 ，只有在CBR模式下才生效。
    lame_set_brate(lame, outBitRate);
    lame_set_VBR_mean_bitrate_kbps(lame, outBitRate);

    // 编码质量，推荐2、5、7
    lame_set_quality(lame, quality);
    if (mode == 0) { // use CBR
        lame_set_VBR(lame, vbr_default);
    } else if (mode == 1) { //use VBR
        lame_set_VBR(lame, vbr_abr);
    } else { // use ABR
        lame_set_VBR(lame, vbr_mtrh);
    }
    // 配置参数
    lame_init_params(lame);
//    LOGI("配置lame参数完成");
}

void resetLame() {
    if (lame != NULL) {
        lame_close(lame);
        lame = NULL;
    }
}


//将inputpath文件 转换为 mp3文件
extern "C" JNIEXPORT void JNICALL
Java_com_example_martin_mytranvice_Mp3Converter_convertMp3
        (JNIEnv *env, jclass obj, jstring jInputPath, jstring jMp3Path) {
//    输入文件路径
    const char *cInput = env->GetStringUTFChars(jInputPath, 0);
//    输出文件路径
    const char *cMp3 = env->GetStringUTFChars(jMp3Path, 0);
    //open input file and output file
//    生成输入文件对象
    FILE *fInput = fopen(cInput, "rb");


    /*fseek 重定位流上的文件指针
     第一个参数stream为文件指针
     第二个参数offset为偏移量，整数表示正向偏移，负数表示负向偏移
     第三个参数origin设定从文件的哪里开始偏移,可能取值为：SEEK_CUR、 SEEK_END 或 SEEK_SET
     SEEK_SET： 文件开头
     SEEK_CUR： 当前位置
     SEEK_END： 文件结尾*/
    fseek(fInput, 4 * 1024, SEEK_CUR);
//    生成输出文件对象
    FILE *fMp3 = fopen(cMp3, "wb");
//    缓存字节数组
    short int inputBuffer[BUFFER_SIZE * 2];
//    输出文件数组
    unsigned char mp3Buffer[BUFFER_SIZE];//You must specified at least 7200  建议 为  采样率/20+7200。
//    每次读取的字节数
    int read = 0; // number of bytes in inputBuffer, if in the end return 0
//            每次写文件的字节数
    int write = 0;// number of bytes output in mp3buffer.  can be 0
//            输入流总字节数
    long total = 0; // the bytes of reading input file
    nowConvertBytes = 0;
    //if you don't init lame, it will init lame use the default value
//  校验转码对象是否存在,不存在则默认初始化一个
    if (lame == NULL) {
        lameInit(44100, 2, 0, 44100, 96, 7);
    }

    //convert to mp3
//    开始转码为mp3
    do {
//        static_cast<int>转换为int类型
//        fread 读取输入流
        read = static_cast<int>(fread(inputBuffer, sizeof(short int) * 2, BUFFER_SIZE, fInput));
//        记录已经读取流的字节总大小
        total += read * sizeof(short int) * 2;
        nowConvertBytes = total;
        if (read != 0) {
            //将PCM数据送入编码器，获取编码出的mp3数据

            //lame_encode_buffer 输入的参数中双声道数据是分开的
            // 单声道输入只能使用lame_encode_buffer，把单声道数据当成左声道数据传入，右声道传NULL即可。

            //lame_encode_buffer_interleaved  输入的参数中双声道数据是交错在一起输入的
            //返回的数据 写入文件就是mp3
            write = lame_encode_buffer_interleaved(lame, inputBuffer, read, mp3Buffer, BUFFER_SIZE);
            //write the converted buffer to the file
            fwrite(mp3Buffer, sizeof(unsigned char), static_cast<size_t>(write), fMp3);
        }
        //if in the end flush
        //当已经没有数据时 将内存中 余量数据写完 退出循环
        if (read == 0) {
            lame_encode_flush(lame, mp3Buffer, BUFFER_SIZE);
            lame_mp3_tags_fid(lame, fMp3);
        }
    } while (read != 0);

    //release resources
    resetLame();
    fclose(fInput);
    fclose(fMp3);
    env->ReleaseStringUTFChars(jInputPath, cInput);
    env->ReleaseStringUTFChars(jMp3Path, cMp3);
    nowConvertBytes = -1;
}

//获取版本号
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_martin_mytranvice_Mp3Converter_getLameVersion(
        JNIEnv *env, jclass obj /* this */) {
    return env->NewStringUTF(get_lame_version());
}

//关闭 转码器
extern "C" JNIEXPORT void JNICALL
Java_com_example_martin_mytranvice_Mp3Converter_close
        (JNIEnv *env, jclass cls) {
//    // 释放所占内存资源
    if (lame != NULL) {
        lame_close(lame);
    }
    lame = NULL;

//    LOGI("释放lame资源");
}

//获取当前已经转码的 字节总长度
extern "C" JNIEXPORT jlong JNICALL
Java_com_example_martin_mytranvice_Mp3Converter_getConvertBytes(JNIEnv *env, jclass type) {
//    LOGD("convert bytes%d", nowConvertBytes);
    return nowConvertBytes;
}