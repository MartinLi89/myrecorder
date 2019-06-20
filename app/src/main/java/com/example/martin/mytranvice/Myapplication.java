package com.example.martin.mytranvice;

import android.app.Application;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;


/**
 * @author MartinLi 2019/6/20
 * 15:50
 */
public class Myapplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();


		PrettyFormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
				.showThreadInfo(false)  //（可选）是否显示线程信息。 默认值为true
//				.methodCount(1)         // （可选）要显示的方法行数。 默认2
				.methodOffset(5)        // （可选）隐藏内部方法调用到偏移量。 默认5
//				.tag("doShare")//（可选）每个日志的全局标记。 默认PRETTY_LOGGER
				.build();
		Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
	}
}
