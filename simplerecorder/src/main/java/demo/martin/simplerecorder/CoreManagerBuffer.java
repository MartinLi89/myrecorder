package demo.martin.simplerecorder;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author MartinLi 2019/6/13
 * 22:30
 * 增强模式, 在回调之外处理数据
 */
public class CoreManagerBuffer implements ManagerInterface {
	private final String TAG = CoreManagerBuffer.class.getSimpleName();

	ManagerInterface mManager;
	private int mSampleRate;
	private int mChannelCount;
	private int mAudioFormat;
	CoreRecorderManager.CoreRecorderCallback coreRecorderCallback = new CoreRecorderManager.CoreRecorderCallback
			() {
		@Override
		public void onRecorder(short[] pcmBuffer, int size) {

			if (saveFileThread != null) {
				saveBuffer(pcmBuffer, size);
			}


//			deliverStream2lefta_right(pcmBuffer, size);


			if (managerCallback != null) {
				managerCallback.onRecorder(pcmBuffer, size);
			}

			if (!saveHandler.isSaving()) {

				saveHandler.sendMessage(Message.obtain(saveHandler, SaveFileThread.SAVEING));
			}
		}

		@Override
		public boolean onRecorderReady() {
			if (managerCallback != null) {
				managerCallback.onRecorderReady();
			}
			return true;
		}

		@Override
		public boolean onRecorderStart(int sampleRate, int channelCount, int audioFormat) {

			mSampleRate = sampleRate;
			mChannelCount = channelCount;
			mAudioFormat = audioFormat;
			if (saveFileThread != null) {
				saveFileThread.setRecordConfig(sampleRate, audioFormat, channelCount);

			}

			if (managerCallback != null) {
				managerCallback.onRecorderStart(sampleRate, channelCount, audioFormat);
			}


			return true;
		}

		@Override
		public void onRecorderError(String errorMsg) {
			if (saveFileThread != null) {
				saveFileThread.cancel();
			}
			saveHandler.removeCallbacksAndMessages(null);

			if (managerCallback != null) {
				managerCallback.onRecorderError(errorMsg);
			}
		}

		@Override
		public void onRecorderStop() {
			// TODO: 2019/6/19 存储线程结束
			saveHandler.sendMessageDelayed(Message.obtain(saveHandler,SaveFileThread.STOP), 3000);
			if (managerCallback != null) {
				managerCallback.onRecorderStop();
			}
		}
	};

	private void saveBuffer(short[] pcmBuffer, int size) {
		byte[] bytes = BytesTransUtil.getInstance().shorts2Bytes(pcmBuffer);

		saveFileThread.saveByes(bytes, size);
//		saveFile.saveByet(bytes, 0, size);
	}

	private DataEncoderThread encoderThread;
	private SaveFileThread saveFileThread;

	SaveFileThread.SaveHandler saveHandler;

	public CoreManagerBuffer(CoreRecorderManager manager) {
		mManager = manager;
		managerCallback = manager.getmCallback();
		manager.setCoreRecorderCallback(coreRecorderCallback);

		encoderThread = new DataEncoderThread();
		encoderThread.start();
		saveFileThread = new SaveFileThread();
		saveFileThread.start();
		saveHandler = new SaveFileThread.SaveHandler(saveFileThread);

	}


	CoreRecorderManager.CoreRecorderCallback mCallBack;
	CoreRecorderManager.CoreRecorderCallback managerCallback;

	@Override
	public void setCoreRecorderCallback(CoreRecorderManager.CoreRecorderCallback callback) {
		mCallBack = callback;

	}


	/**
	 * 将音频中,左右声道分离
	 * 将任务 添加到转码线程中
	 *
	 * @param pcmBuffer
	 * @param realSize
	 */
	private void deliverStream2lefta_right(short[] pcmBuffer, int realSize) {


		//波形声音的码率＝采样频率×量化位数×声道数/8（时长为时间秒的音频大小为数据量大小） 单位b/s
//		int bytesPerSecond = mSampleRate * mAudioFormat * mChannelCount / 8;


//		encoderThread.addPuffer(pcmBuffer, realSize);

		if (mChannelCount == 1) {
			//单通道
			encoderThread.addTask(pcmBuffer, realSize);
		} else if (mChannelCount == 2) {
			//双通道

			int halfSize = realSize / 2;

			short[] leftData = new short[halfSize];
			short[] rightData = new short[halfSize];
			for (int i = 0; i < halfSize; i = i + 2) {
				leftData[i] = pcmBuffer[2 * i];

				if (2 * i + 1 < realSize) {
					leftData[i + 1] = pcmBuffer[2 * i + 1];
				}

				if (2 * i + 2 < realSize) {
					rightData[i] = pcmBuffer[2 * i + 2];
				}

				if (2 * i + 3 < realSize) {
					rightData[i + 1] = pcmBuffer[2 * i + 3];
				}


			}

			MyLog.d(TAG, leftData.length + " 长度 " + rightData.length);
			encoderThread.addTask(leftData, rightData, realSize);
		}


	}

	@Override
	public boolean recorderStart() {
		return mManager.recorderStart();
	}

	@Override
	public void recorderStop() {
		mManager.recorderStop();
	}

	@Override
	public void quickStop() {
		mManager.quickStop();
	}

	@Override
	public boolean isRecord() {
		return mManager.isRecord();
	}


	/**
	 * 文件存储线程
	 */
	private static class SaveFileThread extends HandlerThread {
		private static final String SaveFileThreadTAG = SaveFileThread.class.getSimpleName();
		private static final int mPriority = 0x6678;

		public static final int SAVEING = 0x231;
		public static final int STOP = 0x232;
		private List<SaveTask> saveDatas = Collections.synchronizedList(new ArrayList());

		public SaveFileThread() {
			super("SaveFileThread", mPriority);
			init();

		}

		@Override
		public void run() {
			MyLog.e(SaveFileThreadTAG, "存储线程启动" + Thread.currentThread().getName());
			super.run();

		}

		AudioBufferSave saveFile;
		private String recordPath = Environment.getExternalStorageDirectory().getPath() + File.separator +
				"text33.wav";

		private void init() {


			saveFile = new AudioBufferSave(new AudioBufferSave.AudioBufferSaveCallback() {

				@Override
				public void onSuccess(long fileSize) {

					MyLog.d(SaveFileThreadTAG, "saveFile onSuccess fileSize " + fileSize);
				}

				@Override
				public void onFailed(String s) {
					MyLog.d(SaveFileThreadTAG, "saveFile onFailed  " + s);

				}
			});

			saveFile.setSavePath(recordPath);
			MyLog.d(SaveFileThreadTAG, "saveFile init");
		}

		public void saveByes(byte[] bytes, int size) {
			saveDatas.add(new SaveTask(bytes, size));
		}

		public void setRecordConfig(int sampleRate, int audioFormat, int channelCount) {
			if (saveFile != null) {

				saveFile.setRecordConfig(sampleRate, audioFormat, channelCount);
				saveFile.onWriteStart();
			}
		}

		public void cancel() {
			MyLog.d(SaveFileThreadTAG, "cancel");

			saveDatas.clear();

			if (saveFile != null) {
				saveFile.cancel();
				saveFile = null;
			}

			getLooper().quit();
		}

		public void finish() {
			MyLog.d(SaveFileThreadTAG, "finish");


			if (saveFile != null) {
				saveFile.finish();
			}
			getLooper().quit();


		}

		private class SaveTask {

			private byte[] saveData;
			private int mSize;

			SaveTask(byte[] data, int size) {
				saveData = data;
				mSize = size;
			}
		}

		public static class SaveHandler extends Handler {

			WeakReference<SaveFileThread> saveThread;

			SaveHandler(SaveFileThread thread) {
				super(thread.getLooper());

				saveThread = new WeakReference<>(thread);
			}

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				SaveFileThread saveFileThread = saveThread.get();
				if (msg.what == SAVEING) {
					saving = true;
					while (saveFileThread.doWork() > 0) ;
//					Log.e(SaveFileThreadTAG, "收到 msg");

					saving = false;
				} else if (msg.what == STOP) {
					saveFileThread.finish();
					removeCallbacksAndMessages(null);
					getLooper().quit();

				}


			}

			volatile boolean saving;

			public boolean isSaving() {


				return saving;
			}
		}

		private int doWork() {
			if (saveFile == null) {
//				Log.d(SaveFileThreadTAG, "saveFile 为空");
				return 0;
			}

			if (saveDatas.size() <= 0) {
//				Log.d(SaveFileThreadTAG, "没有数据了");
				return 0;
			}

			SaveTask remove = saveDatas.remove(0);
			int mSize = remove.mSize;
			saveFile.saveByet(remove.saveData, 0, mSize);
			return mSize;
		}


	}


	/**
	 * 编码线程
	 */
	private static class DataEncoderThread extends HandlerThread {
		private static final String MTAG = DataEncoderThread.class.getSimpleName();
		private int encoding = 0x0224;

		private DataEncoderCallback mCallback;

		public void setmCallback(DataEncoderCallback listener) {
			mCallback = listener;
		}

		/**
		 * 编码线程的回调
		 */
		interface DataEncoderCallback {

			void onEncodeStartCallback();

			void encodedMp3();

			void onEncodeEndCallback();
		}


		//缓存数据
		private List<Task> mTasks = Collections.synchronizedList(new ArrayList<Task>());


		/**
		 * 向缓存中添加单声道数据
		 *
		 * @param rawData
		 * @param readSize
		 */
		public void addTask(short[] rawData, int readSize) {
			if (readSize < 1) {
				return;
			}
			mTasks.add(new Task(rawData, readSize));


		}

		/**
		 * 向缓存中添加双声道数据
		 *
		 * @param rawData
		 * @param rightData
		 * @param readSize
		 */
		public void addTask(short[] rawData, short[] rightData, int readSize) {
			if (readSize < 1) {
				return;
			}
			mTasks.add(new Task(rawData, rightData, readSize));
		}

		public DataEncoderThread() {
			super(MTAG);
		}

		public void addPuffer(short[] pcmBuffer, int realSize) {


		}


		/**
		 * 编码handler
		 */
		class EncoderHandler extends Handler {


			WeakReference<DataEncoderThread> encodeThread;

			EncoderHandler(DataEncoderThread thread) {
				super(thread.getLooper());

				encodeThread = new WeakReference<>(thread);
			}

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				if (msg.what == encoding) {

					DataEncoderThread threadRef = encodeThread.get();
					if (mCallback != null) {
						mCallback.onEncodeStartCallback();
					}


					// TODO: 2019/6/19 转码过程


					//处理缓冲区中的数据
					while (threadRef.processData() > 0) ;
					if (mCallback != null) {
						mCallback.onEncodeEndCallback();
					}
					// Cancel any event left in the queue
					removeCallbacksAndMessages(null);
//					threadRef.flushAndRelease();
					getLooper().quit();
				}
			}
		}

		private class Task {

			/**
			 * 单声道 或者左声道的数据
			 */
			private short[] rawData;
			private int readSize;
			/**
			 * 双声道中,右声道的数据
			 */
			private short[] rightData;

			public Task(short[] rawData, int readSize) {
				this.rawData = rawData;
				this.readSize = readSize;
			}

			public Task(short[] leftData, short[] rightData, int readSize) {
				this.rawData = leftData;
				this.rightData = rightData;
				this.readSize = readSize;
			}


			public short[] getData() {
				return rawData;
			}

			public short[] getRightData() {
				return rightData;
			}

			public int getReadSize() {
				return readSize;
			}
		}

		/**
		 * 编码数据
		 *
		 * @return
		 */
		private int processData() {
			if (mTasks.size() < 1) {
				return 0;
			}

			Task task = mTasks.remove(0);
			//左声道,单声道数据
			short[] buffer = task.getData();
			//右声道数据
			short[] rightData = task.getRightData();
			//本段数据长度
			int readSize = task.getReadSize();


			if (rightData == null || rightData.length <= 0) {
				rightData = buffer;
			}

			if (mCallback != null) {
				mCallback.encodedMp3();
			}

			return 0;
		}


	}

}

