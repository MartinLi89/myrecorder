package com.example.martin.mytranvice;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import demo.martin.simplerecorder.CoreManagerBuffer;
import demo.martin.simplerecorder.CoreRecorderManager;

public class MainActivity extends AppCompatActivity {

	// Used to load the 'native-lib' library on application startup.


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);


		AndPermission.with(this)
				.runtime()
				.permission(Permission.Group.STORAGE)
				.permission(Permission.Group.MICROPHONE)
				.onGranted(new Action<List<String>>() {
					@Override
					public void onAction(List<String> data) {
						init();


					}
				})
				.start();


	}

	final String wavPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "test.wav";
	final String mp3Path = Environment.getExternalStorageDirectory().getPath() + File.separator + "test.mp3";

	private long currentTime;

	private void init() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				putAssetsToSDCard("test.wav", wavPath);
			}
		}).start();

		Mp3Converter.init_util(44100, 1, 0, 44100, 96, 7);

		// Example of a call to a native method
		TextView tv = (TextView) findViewById(R.id.sample_text);
		tv.setText(Mp3Converter.getLameVersion());
		Button recorder = findViewById(R.id.button);
		recorder.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				try {
					CoreRecorderManager manager; //= CoreRecorderManager.getInstance();
					CoreRecorderManager.RecorderBuilder builder = new CoreRecorderManager.RecorderBuilder();
					builder.setCallbackInterval(100);
					manager = builder.build();

					manager.setCoreRecorderCallback(new CoreRecorderManager.CoreRecorderCallback() {
						@Override
						public void onRecorder(short[] pcmBuffer) {

							long l = System.currentTimeMillis();
							long time = l - currentTime;
							currentTime = l;
							Log.d(TAG, pcmBuffer.length + " 时间差为" + time);

//							for (short i : pcmBuffer) {
//								Log.d(TAG, i + "");
//							}

						}

						@Override
						public boolean onRecorderReady() {
							return true;
						}

						@Override
						public boolean onRecorderStart(int sampleRate, int channelCount, int audioFormat) {
							return true;
						}

						@Override
						public void onRecorderError(String errorMsg) {

						}

						@Override
						public void onRecorderStop() {

						}
					});
//					manager.recorderStart();

					new CoreManagerBuffer(manager).recorderStart();

//					manager.play();
				} catch (Exception e) {
					e.printStackTrace();

				}
			}
		});


		findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				try {
					CoreRecorderManager manager = CoreRecorderManager.getInstance();
					manager.recorderStop();

					Mp3Converter.close();
				} catch (Exception e) {
					e.printStackTrace();

				}
			}
		});

		findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				try {

					new Thread(new Runnable() {
						@Override
						public void run() {
							long start_time = System.currentTimeMillis();
							Mp3Converter.convertMp3(wavPath, mp3Path);
							Log.d(TAG, ((System.currentTimeMillis() - start_time) / 1000) + "");
						}
					}).start();


				} catch (Exception e) {
					e.printStackTrace();

				}
			}
		});
	}

	public static String TAG = "mp3converter";

	public void putAssetsToSDCard(String assetsPath,
								  String sdCardPath) {
		try {
			String mString[] = getAssets().list(assetsPath);
			if (mString.length == 0) { // 说明assetsPath为空,或者assetsPath是一个文件

				File file = new File(sdCardPath);
				if (!file.exists()) {
					file.createNewFile(); // 创建文件
					Log.d(TAG, "创建了新文件");
				} else {
					Log.d(TAG, "已经存在该文件啦");
					return;//已经存在直接退出
				}

				InputStream mIs = getAssets().open(assetsPath); // 读取流

				byte[] mbuffer = new byte[1024 * 4];
				int bt = 0;
				FileOutputStream fos = new FileOutputStream(file); // 写入流
				while ((bt = mIs.read(mbuffer)) != -1) { // assets为文件,从文件中读取流
					fos.write(mbuffer, 0, bt);// 写入流到文件中
				}
				fos.flush();// 刷新缓冲区
				mIs.close();// 关闭读取流
				fos.close();// 关闭写入流
				Log.d(TAG, "文件写入完成");
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, e.getMessage());
		}
	}

}
