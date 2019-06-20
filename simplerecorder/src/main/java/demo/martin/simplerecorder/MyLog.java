package demo.martin.simplerecorder;


import android.util.Log;

import com.orhanobut.logger.Logger;

/**
 * @author MartinLi 2019/6/20
 * 15:57
 */
public class MyLog {


	public static void v(String tag, String msg) {
		Logger.v(tag, msg);
	}


	public static void i(String tag, String msg) {
		Logger.i(tag, msg);
	}
	public static void d(String tag, String msg) {
//		Logger.d(tag, msg);
		Log.d(tag, msg);
	}
	public static void w(String tag, String msg) {
		Logger.w(tag, msg);
	}

	public static void e(String tag, String msg) {
		Logger.e(tag, msg);
	}
}
