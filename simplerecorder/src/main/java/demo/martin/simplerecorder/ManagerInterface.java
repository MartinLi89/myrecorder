package demo.martin.simplerecorder;

/**
 * @author MartinLi 2019/6/13
 * 22:33
 */
public interface ManagerInterface {

	void setCoreRecorderCallback(CoreRecorderManager.CoreRecorderCallback callback);

	CoreRecorderManager.CoreRecorderCallback getmCallback();

	boolean recorderStart();
	void recorderStop();

	void quickStop();

	boolean isRecord();
}
