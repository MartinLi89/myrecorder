package com.example.martin.mytranvice;

/**
 * @author MartinLi 2019/5/28
 * 12:52
 * 通过查看Lame库的API文档(lame-3.100\API)可知，使用Lame封装Mp3需要经历四个步骤，
 * 即初始化lame引擎、
 * 编码pcm为mp3数据帧、
 * 写入文件、
 * 释放lame引擎资源。
 * 因此，在java中，我们定义与之对应的native方法以便java层调用，最终生成所需的mp3格式文件。
 */
public class Mp3Converter {

	static {
		System.loadLibrary("mp3converter111");
	}


	/**
	 * init lame
	 *
	 * @param inSampleRate  input sample rate in Hz
	 * @param channel       number of channels
	 * @param mode          0 = CBR, 1 = VBR, 2 = ABR.  default = 0
	 * @param outSampleRate output sample rate in Hz
	 * @param outBitRate    rate compression ratio in KHz
	 * @param quality       quality=0..9. 0=best (very slow). 9=worst.<br />
	 *                      recommended:<br />
	 *                      2 near-best quality, not too slow<br />
	 *                      5 good quality, fast<br />
	 *                      7 ok quality, really fast
	 */
	private native static void init(int inSampleRate, int channel, int mode,
									int outSampleRate, int outBitRate, int quality);


	public static void init_util(int inSampleRate, int channel, int mode,
								 int outSampleRate, int outBitRate, int quality) {
		init(inSampleRate, channel, mode
				, outSampleRate, outBitRate, quality);
	}

	/**
	 * file convert to mp3
	 * it may cost a lot of time and better put it in a thread
	 * @param input
	 *          file path to be converted
	 * @param mp3
	 *          mp3 output file path
	 */
	public native  static void convertMp3(String input, String mp3);


	/**
	 * get converted bytes in inputBuffer
	 * @return
	 *          converted bytes in inputBuffer
	 *          to ignore the deviation of the file size,when return to -1 represents convert complete
	 */
	public native static long getConvertBytes();


	/**
	 * get library lame version
	 *
	 * @return
	 */
	public native static String getLameVersion();



	public native static void close();


}
