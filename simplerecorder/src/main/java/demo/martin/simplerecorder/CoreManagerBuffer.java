package demo.martin.simplerecorder;

/**
 * @author MartinLi 2019/6/13
 * 22:30
 * 增强模式, 在回调之外处理数据
 */
public class CoreManagerBuffer implements ManagerInterface{

	CoreRecorderManager mManager;
	CoreRecorderManager.CoreRecorderCallback coreRecorderCallback = new CoreRecorderManager.CoreRecorderCallback
			() {
		@Override
		public void onRecorder(short[] pcmBuffer) {

			if (managerCallback != null) {
				managerCallback.onRecorder(pcmBuffer);
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
			if (managerCallback != null) {
				managerCallback.onRecorderStart(sampleRate, channelCount, audioFormat);
			}
			return true;
		}

		@Override
		public void onRecorderError(String errorMsg) {
			if (managerCallback != null) {
				managerCallback.onRecorderError(errorMsg);
			}
		}

		@Override
		public void onRecorderStop() {
			if (managerCallback != null) {
				managerCallback.onRecorderStop();
			}
		}
	};
	public CoreManagerBuffer(CoreRecorderManager manager) {
		mManager = manager;
		managerCallback =manager.getmCallback();
		manager.setCoreRecorderCallback(coreRecorderCallback);
	}


	CoreRecorderManager.CoreRecorderCallback mCallBack;
	CoreRecorderManager.CoreRecorderCallback managerCallback;

	@Override
	public void setCoreRecorderCallback(CoreRecorderManager.CoreRecorderCallback callback) {
		mCallBack = callback;

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

}
