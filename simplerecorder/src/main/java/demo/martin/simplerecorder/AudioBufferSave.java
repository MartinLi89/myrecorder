package demo.martin.simplerecorder;

import android.media.AudioFormat;
import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * @author MartinLi 2019/6/18
 * 10:44
 */
public class AudioBufferSave {


	private static final String TAG = AudioBufferSave.class.getSimpleName();
	private String savePath;

	public AudioBufferSave(AudioBufferSaveCallback callback) {
		mCallback = callback;
	}

	/**
	 * 保存文件的绝对路径(含名字)
	 *
	 * @param savePath
	 */
	public void setSavePath(String savePath) {
		this.savePath = savePath;
	}

	public int mSampleRate;//采样率
	public int mAudioFormat;//位宽模式 精度模式
	public int mChannelConfig;//通道数


	/**
	 * @param sampleRate
	 * @param audioFormat
	 * @param channelConfig
	 */
	public void setRecordConfig(int sampleRate, int audioFormat, int channelConfig) {
		mSampleRate = sampleRate;
		mAudioFormat = audioFormat;
		mChannelConfig = channelConfig;
	}

	//文件写入器
	RandomAccessFile randomAccessFile;
	private File saveFile;

	boolean isInited;

	public void saveByet(byte[] data, int offset, int size) {
		if (randomAccessFile == null) {
			return;
		}
		try {
			write(data, offset, size);
		} catch (IOException e) {
			e.printStackTrace();
			onFailed("写入失败");

		}
	}

	private int dataSize;

	private void write(byte[] data, int offset, int size) throws IOException {
		dataSize += size;
		randomAccessFile.write(data, offset, size);
	}

	public void finish() {

		try {
			flush();
		} catch (IOException e) {
			e.printStackTrace();
			onFailed("关闭失败");
		}

	}

	public void cancel() {

		randomAccessFile = null;

		if (saveFile.exists()) {
			saveFile.delete();
			saveFile = null;
		}


	}

	private void flush() throws IOException {

		try {
			if (randomAccessFile == null) {
				onFailed("未初始化");
				return;
			}
//			writeInsertHead();

			int a = 0;

			if (mAudioFormat == AudioFormat.ENCODING_PCM_16BIT) {
				a = 16;
			} else {
				a = 8;
			}

			writeHead(dataSize, mSampleRate
					, mSampleRate * mChannelConfig * a / 8
					, mChannelConfig, a);


		} finally {
			if (randomAccessFile != null) {
				randomAccessFile.close();
				randomAccessFile = null;
			}
			isInited = false;
		}
	}

	private void writeInsertHead() throws IOException {
		RandomAccessFile rand = randomAccessFile;

		rand.seek(4); // riff chunk size
//			rand.writeInt(Integer.reverseBytes((int) (rand.length() - 8)));
		rand.writeInt(Integer.reverseBytes((int) (dataSize + 36)));
		rand.seek(40); // data chunk size
//			rand.writeInt(Integer.reverseBytes((int) (rand.length() - 44)));
		rand.writeInt(Integer.reverseBytes((int) (dataSize)));

		onSuccess(rand.length());
	}


	public boolean onWriteStart() {

		boolean isSuccess = false;
		if (isInited) {
			return isSuccess;
		}

		try {
			init();
			isSuccess = true;
		} catch (IOException e) {
			e.printStackTrace();
			onFailed("初始化失败");
			isSuccess = false;
		}
		isInited = isSuccess;
		return isSuccess;

	}

	private void onSuccess(long fileSize) {

		if (mCallback != null) {
			mCallback.onSuccess(fileSize);

		}
	}

	private void onFailed(String msg) {
		if (mCallback != null) {
			mCallback.onFailed(msg);
		}
	}

	private void init() throws IOException {

		if (mChannelConfig == 0 || mAudioFormat == 0 || mSampleRate == 0) {
			onFailed("录音数据 参数未配置");
			return;
		}


		if (TextUtils.isEmpty(savePath)) {
			onFailed("存储路径为空");
			return;
		}
		saveFile = new File(savePath);

		if (saveFile.isDirectory()) {
			onFailed("初始化 文件 路径 不是个文件");
			return;
		}

		if (saveFile.exists()) {
			saveFile.delete();
		}
		File parentFile = saveFile.getParentFile();
		if (!parentFile.exists()) {
			parentFile.mkdirs();
		}

		short bSamples;
		short nChannels;
		int sRate;

		if (mAudioFormat == AudioFormat.ENCODING_PCM_16BIT) {
			bSamples = 16;
		} else {
			bSamples = 8;
		}

		if (mChannelConfig == AudioFormat.CHANNEL_IN_MONO) {
			nChannels = 1;
		} else {
			nChannels = 2;
		}

		sRate = mSampleRate;


		RandomAccessFile rand;
		rand = new RandomAccessFile(saveFile, "rw");
		randomAccessFile = rand;

		writeHead(bSamples, nChannels, sRate);
		return;


	}


	/**
	 * @param totalAudioLen 音频数据总大小
	 * @param sampleRate    采样率
	 * @param byteRate      位元（组）率(每秒的数据量 单位 字节/秒)   采样率(44100之类的) * 通道数(1,或者2)*每次采样得到的样本位数(16或者8) / 8;
	 * @param nChannels     声道数量
	 * @param weikuan       位宽
	 * @throws IOException
	 */
	private void writeHead(int totalAudioLen, int sampleRate, int byteRate, int nChannels, int weikuan) throws
			IOException {

		RandomAccessFile rand = randomAccessFile;


		long totalDataLen = totalAudioLen + 36;
		rand.seek(0);

		byte[] header = new byte[44];
		header[0] = 'R'; // RIFF/WAVE header
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		header[12] = 'f'; // 'fmt ' chunk
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		header[16] = 16; // 4 bytes: size of 'fmt ' chunk
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		header[20] = 1; // format = 1
		header[21] = 0;
		header[22] = (byte) (nChannels & 0xff);
		header[23] = (byte) ((nChannels >> 8) & 0xff);

		header[24] = (byte) (sampleRate & 0xff);//采样率
		header[25] = (byte) ((sampleRate >> 8) & 0xff);
		header[26] = (byte) ((sampleRate >> 16) & 0xff);
		header[27] = (byte) ((sampleRate >> 24) & 0xff);

		header[28] = (byte) (byteRate & 0xff);//取八位
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);

		int b = weikuan * nChannels / 8;//每次采样的大小
		header[32] = (byte) (b & 0xff); // block align
		header[33] = (byte) ((b >> 8) & 0xff);

		header[34] = (byte) (weikuan & 0xff);
		header[35] = (byte) ((weikuan >> 8) & 0xff);

//		header[34] = (byte) ((weikuan >> 16) & 0xff); // bits per sample 位宽
//		header[35] = (byte) ((weikuan >> 32) & 0xff);


		header[36] = 'd';//data
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);

		rand.write(header, 0, 44);
	}


	/**
	 * @param weikuan    位宽
	 * @param nChannels  通道数
	 * @param sampleRate 采样率
	 * @throws IOException
	 */
	private void writeHead(short weikuan, short nChannels, int sampleRate) throws IOException {
		RandomAccessFile rand;

		int byteofSeconds = weikuan * nChannels * sampleRate/8;
		rand = randomAccessFile;
		//设置文件大小为0, 防止未知情况导致 该文件已经存在
		rand.setLength(0);

		//初始化wav文件的头文件

		/* RIFF header */
		rand.writeBytes("RIFF"); // riff id
		rand.writeInt(0); // riff chunk size *PLACEHOLDER*
		rand.writeBytes("WAVE"); // wave type

		/* fmt chunk */
		rand.writeBytes("fmt "); // fmt id
		rand.writeInt(Integer.reverseBytes(16)); // fmt chunk size
		rand.writeShort(Short.reverseBytes((short) 1)); // AudioFormat,1 for PCM
		rand.writeShort(Short.reverseBytes(nChannels));// Number of channels, 1 for mono, 2 for stereo
		rand.writeInt(Integer.reverseBytes(sampleRate)); // Sample rate 采样率
		rand.writeInt(Integer.reverseBytes(byteofSeconds));//Byte rate,波形数据传输速率（每秒平均字节数）
		// SampleRate*NumberOfChannels*BitsPerSample/8
		//每秒播放字节数
		rand.writeShort(Short.reverseBytes((short) (nChannels * weikuan / 8))); // Block align,
		// NumberOfChannels*BitsPerSample/8
		rand.writeShort(Short.reverseBytes(weikuan)); // Bits per sample

		/* data chunk */
		rand.writeBytes("data"); // data id
		rand.writeInt(0); // data chunk size *PLACEHOLDER*
	}


	private AudioBufferSaveCallback mCallback;


	public interface AudioBufferSaveCallback {
		/**
		 * 文件保存成功
		 *
		 * @param fileSize 文件大小
		 */
		void onSuccess(long fileSize);


		/**
		 * 文件保存失败
		 *
		 * @param s
		 */
		void onFailed(String s);
	}
}
