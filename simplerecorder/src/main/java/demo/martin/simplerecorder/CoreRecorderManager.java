package demo.martin.simplerecorder;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * @author MartinLi 2019/6/12
 * 22:05
 * 实现录音的核心类
 */
public class CoreRecorderManager implements ManagerInterface {

	private static final String TAG = CoreRecorderManager.class.getSimpleName();
	AudioRecord mAudioRecorder;
	static RecorderBuilder mRecorderBuilder;

	private static CoreRecorderManager INSTANCE;

	private CoreRecorderManager(RecorderBuilder recorderBuilder) {
		mRecorderBuilder = recorderBuilder;
	}


	public synchronized static CoreRecorderManager getInstance() {

		if (mRecorderBuilder == null) {
			RecorderBuilder builder = new RecorderBuilder();
			return builder.build();
		}
		return INSTANCE;
	}

	/**
	 * 录音回调
	 */
	CoreRecorderCallback mCallback;

	/**
	 * 用于录音类的回调 外部  获取录音数据的唯一方式
	 */
	public interface CoreRecorderCallback {


		/**
		 * 录音中的回调
		 *
		 * @param pcmBuffer
		 */
		void onRecorder(short[] pcmBuffer);


		boolean onRecorderReady();


		/**
		 * 当前录音所使用的参数
		 *
		 * @param sampleRate   采样率
		 * @param channelCount 通道数
		 * @param audioFormat  位宽
		 * @return
		 */
		boolean onRecorderStart(int sampleRate, int channelCount, int audioFormat);

		void onRecorderError(String errorMsg);

		void onRecorderStop();
	}

	public CoreRecorderCallback getmCallback() {
		return mCallback;
	}

	@Override
	public void setCoreRecorderCallback(CoreRecorderCallback coreRecorderCallback) {
		mCallback = coreRecorderCallback;

	}

	/**
	 * 录音线程
	 */
	private HandlerThread recordThread;


	/**
	 * 录音中
	 */
	private Runnable mRecordingRunnable = new Runnable() {
		@Override
		public void run() {
			if (mAudioRecorder != null && mAudioRecorder.getState() == AudioRecord.STATE_INITIALIZED) {

				try {
					mAudioRecorder.stop();
					mAudioRecorder.startRecording();

				} catch (Exception e) {
					e.printStackTrace();
					mCallback.onRecorderError(e.getMessage());

					mAudioRecorder = null;
				}
			}
			try {


				for (int i = 0; i < 2; i++) {
					if (mAudioRecorder == null) {
						isRecord = false;
						break;
					}
					mAudioRecorder.read(mPcmBuffer, 0, mPcmBuffer.length);
				}

				int nLen = -1;

				for (; isRecord; ) {
					nLen = mAudioRecorder.read(mPcmBuffer, 0, mPcmBuffer.length);
					if (nLen == mPcmBuffer.length) {
						mCallback.onRecorder(mPcmBuffer);
					} else {
						isRecord = false;
					}
				}


			} catch (Exception e) {
				e.printStackTrace();
				mCallback.onRecorderError(e.getMessage());

				isRecord = false;

			} finally {
				Log.d(TAG, isRecord + "");
				unInitializeRecord();
				onRecorderStop();

			}
		}
	};


	/**
	 * 是否正在录音
	 */
	private volatile boolean isRecord;


	Handler mHandler;

	/**
	 * 开始录音
	 *
	 * @return
	 */
	@Override
	public boolean recorderStart() {
		if (isRecord()) {

			return isRecord;
		}

		isRecord = true;


		if (onRedorderReady()) {
			if (initializeRecord()) {
				initPcmBuffer();
				if (onRecorderStart()) {
					recordThread = new HandlerThread(TAG);
					recordThread.start();
					mHandler = new Handler(recordThread.getLooper());
					mHandler.post(mRecordingRunnable);
//					start();

					return true;
				}
			}
		}

		isRecord = false;
		return false;
	}

	/**
	 * 结束录音
	 */
	@Override
	public void recorderStop() {
		isRecord = false;
		if (mHandler != null) {
			mHandler.removeCallbacksAndMessages(null);
		}
		recordThread = null;


	}


	/**
	 * 立刻停止录音
	 */
	@Override
	public void quickStop() {

		isRecord = false;
		if (recordThread != null) {
			try {
				recordThread.join();
				mHandler.removeCallbacksAndMessages(null);

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

		}
		recordThread = null;

	}


	/**
	 * 是否正在录音
	 *
	 * @return
	 */
	@Override
	public boolean isRecord() {
		return isRecord;
	}


	/**
	 * 初始化容器
	 */
	private void initPcmBuffer() {

		int channel_config = mAudioRecorder.getChannelCount();
		int format = mAudioRecorder.getAudioFormat();//mRecorderBuilder.mAudioFormat;
		int sample_rate = mAudioRecorder.getSampleRate();
		short bSamples;
		short nChannels;
		if (format == AudioFormat.ENCODING_PCM_16BIT) {
			bSamples = 16;
		} else {
			bSamples = 8;
		}

		if (channel_config == AudioFormat.CHANNEL_IN_MONO) {
			nChannels = 1;
		} else {
			nChannels = 2;
		}

		int framePeriod = sample_rate * mRecorderBuilder.TIMER_INTERVAL / 1000;
		mPcmBuffer = new short[framePeriod * bSamples / 8 * nChannels / 2];
//		mPcmBuffer = new short[mRecorderBuilder.bufferSize / 4];
	}


	/**
	 * 创建录音类
	 */
	public static class RecorderBuilder {

		public static final int SAMPLE_RATE_48K_HZ = 48000;
		public static final int SAMPLE_RATE_44K_HZ = 44100;
		public static final int SAMPLE_RATE_32K_HZ = 32000;
		public static final int SAMPLE_RATE_22K_HZ = 22050;
		public static final int SAMPLE_RATE_16K_HZ = 16000;
		public static final int SAMPLE_RATE_11K_HZ = 11025;
		public static final int SAMPLE_RATE_8K_HZ = 8000;

		private final int sampleRates[] = {48000, 44100, 32000, 22050, 16000, 11025, 8000};
		private final int configs[] = {AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO};
		private final int formats[] = {AudioFormat.ENCODING_PCM_16BIT, AudioFormat.ENCODING_PCM_8BIT};


		/**
		 * Google Android 文档明确表明只有以下3个参数是可以在所有设备上保证支持的，
		 * 44100 Hz，
		 * AudioFormat.CHANNEL_IN_MONO(单声道)，
		 * AudioFormat.ENCODING_PCM_16BIT(位宽)
		 */


		private int mAudioSource = MediaRecorder.AudioSource.MIC;
		private int mSampleRate = SAMPLE_RATE_44K_HZ;
		private int mChannelConfig = AudioFormat.CHANNEL_IN_MONO;
		private int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
		private int bufferSize;

		private boolean mAutoFound = true;


		private int TIMER_INTERVAL = 10;//10ms

		/**
		 * 设置回调时长间隔
		 *
		 * @param intervalMillis
		 * @return
		 */
		public RecorderBuilder setCallbackInterval(int intervalMillis) {

			if (intervalMillis < TIMER_INTERVAL) {
				return this;
			}
			if (intervalMillis % TIMER_INTERVAL != 0) {
				intervalMillis = intervalMillis / TIMER_INTERVAL * TIMER_INTERVAL;
			}

			TIMER_INTERVAL = intervalMillis;

			return this;
		}


		/**
		 * 如果设置的参数无法启动录音机,是否自动寻找适合的参数进行录音
		 *
		 * @param autoFound
		 * @return
		 */
		public RecorderBuilder setAutoRecorder(boolean autoFound) {
			mAutoFound = autoFound;
			return this;
		}

		/**
		 * 音频源：指的是从哪里采集音频。这里我们当然是从麦克风采集音频，所以此参数的值为MIC
		 *
		 * @param audioSource
		 * @return
		 */
		public RecorderBuilder setAudioSorce(int audioSource) {
			mAudioSource = audioSource;
			return this;
		}


		/**
		 * 采样率：音频的采样频率，每秒钟能够采样的次数，采样率越高，音质越高。
		 * 一般是44100、22050、11025但不限于这几个参数。
		 * 例如要采集低质量的音频就可以使用4000、8000等低采样率。
		 * 顺带一提,如果需要做vad 那么只能使用 48000,32000,16000,8000
		 * 至于为什么,自己百度去
		 *
		 * @param sampleRate
		 * @return
		 */
		public RecorderBuilder setSampleRate(int sampleRate) {
			mSampleRate = sampleRate;

			return this;
		}

		/**
		 * 声道设置：android支持双声道立体声和单声道。MONO单声道，STEREO立体声
		 *
		 * @param channelConfig {@link AudioFormat#CHANNEL_IN_MONO}
		 *                      {@link AudioFormat#CHANNEL_IN_STEREO}
		 * @return
		 */
		public RecorderBuilder setChannelConfig(int channelConfig) {
			mChannelConfig = channelConfig;
			return this;
		}


		/**
		 * 编码制式和采样大小：采集来的数据当然使用PCM编码(脉冲代码调制编码，
		 * 即PCM编码。PCM通过抽样、量化、编码三个步骤将连续变化的模拟信号转换为数字编码。)
		 * android支持的采样大小16bit
		 * 或者8bit。
		 * 当然采样大小越大，那么信息量越多，音质也越高，现在主流的采样大小都是16bit，在低质量的语音传输的时候8bit足够了。
		 *
		 * @param audioFormat {@link android.media.AudioFormat#ENCODING_PCM_8BIT}
		 *                    {@link AudioFormat#ENCODING_PCM_16BIT}
		 *                    {@link AudioFormat#ENCODING_PCM_FLOAT}
		 * @return
		 */
		public RecorderBuilder setAudioFormat(int audioFormat) {

			mAudioFormat = audioFormat;
			return this;
		}


		public synchronized CoreRecorderManager build() {

			CoreRecorderManager coreRecorderManager;
			INSTANCE = coreRecorderManager = new CoreRecorderManager(this);

			return coreRecorderManager;
		}


	}

	private short[] mPcmBuffer;//录音搬运对象,从录音对象中获取 录音数据的实体


	/**
	 * 初始化 audiorecorder
	 *
	 * @return
	 */
	private synchronized boolean initializeRecord() {
		boolean isFound = false;
		int sample_rate = mRecorderBuilder.mSampleRate;
		int channel_config = mRecorderBuilder.mChannelConfig;
		int format = mRecorderBuilder.mAudioFormat;
		int bufsize = AudioRecord.getMinBufferSize(sample_rate, channel_config, format);


		short bSamples;
		if (format == AudioFormat.ENCODING_PCM_16BIT) {
			bSamples = 16;
		} else {
			bSamples = 8;
		}

		short nChannels;
		if (channel_config == AudioFormat.CHANNEL_IN_MONO) {
			nChannels = 1;
		} else {
			nChannels = 2;
		}
		int framePeriod = sample_rate * mRecorderBuilder.TIMER_INTERVAL / 1000;
		mRecorderBuilder.bufferSize = framePeriod * 2 * bSamples * nChannels / 8;


		//  录音 通知周期 及 录音数据读取 buffer 的设定
		// （重点：audioRecord.read()读取的大小最好是设定的缓冲区的一半，效果会好多）
		if (AudioRecord.ERROR_BAD_VALUE != bufsize && AudioRecord.ERROR != bufsize) {

			if (mRecorderBuilder.bufferSize < bufsize) {
				mRecorderBuilder.bufferSize = bufsize;
			}

			mAudioRecorder = new AudioRecord(
					mRecorderBuilder.mAudioSource, sample_rate,
					channel_config, format, mRecorderBuilder.bufferSize);


			return true;
		}


		if (AudioRecord.ERROR_BAD_VALUE == bufsize && !mRecorderBuilder.mAutoFound) {
			// TODO: 2019/6/12 给个失败的回调
			return false;
		}

		for (int x = 0; !isFound && x < mRecorderBuilder.formats.length; x++) {
			format = mRecorderBuilder.formats[x];
			for (int y = 0; !isFound && y < mRecorderBuilder.configs.length; y++) {
				channel_config = mRecorderBuilder.configs[y];

				for (int z = 0; !isFound && z < mRecorderBuilder.sampleRates.length; z++) {

					sample_rate = mRecorderBuilder.sampleRates[z];


					bufsize = AudioRecord.getMinBufferSize(sample_rate, channel_config, format);


					if (AudioRecord.ERROR_BAD_VALUE == bufsize) {
						continue;
					}
					if (AudioRecord.ERROR == bufsize) {
						continue;
					}

					if (mAudioRecorder != null) {
						unInitializeRecord();
					}

					try {
						if (mRecorderBuilder.bufferSize < bufsize) {
							mRecorderBuilder.bufferSize = bufsize;
						}
						mAudioRecorder = new AudioRecord(
								mRecorderBuilder.mAudioSource, sample_rate,
								channel_config, format, mRecorderBuilder.bufferSize);

						int state = mAudioRecorder.getState();
						if (state != AudioRecord.STATE_INITIALIZED) {
							continue;
						}
					} catch (IllegalStateException e) {
						mAudioRecorder = null;
						continue;
					}
					isFound = true;
					break;
				}
			}
		}


		return isFound;
	}

	private boolean onRecorderStart() {

		if (mCallback != null) {

			return mCallback.onRecorderStart(mAudioRecorder.getSampleRate(), mAudioRecorder.getChannelCount()
					, mAudioRecorder.getAudioFormat());
		}
		return true;
	}

	private boolean onRedorderReady() {

		if (mCallback != null) {


			return mCallback.onRecorderReady();
		}
		return true;
	}

	private void onRecorderStop() {
		if (mCallback != null) {

			mCallback.onRecorderStop();
		}
	}

	/**
	 * 释放recorder
	 */
	private synchronized void unInitializeRecord() {

		if (mAudioRecorder != null) {
			try {
				mAudioRecorder.stop();
				mAudioRecorder.release();
			} catch (Exception e) {
				e.printStackTrace();
			}
			mAudioRecorder = null;
		}

	}

}
