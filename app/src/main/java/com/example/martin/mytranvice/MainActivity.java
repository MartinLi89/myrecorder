package com.example.martin.mytranvice;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.util.List;

import demo.martin.simplerecorder.CoreManagerBuffer;
import demo.martin.simplerecorder.CoreRecorderManager;
import demo.martin.simplerecorder.MyLog;

public class MainActivity extends AppCompatActivity {
	private CoreManagerBuffer coreManagerBuffer;

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


		int a = 10000;
		a = Integer.reverseBytes(a);
		byte b = (byte) (a & 0xff);
		byte c = (byte) ((a >> 8) & 0xff);
		byte d = (byte) ((a >> 16) & 0xff);
		byte e = (byte) ((a >> 24) & 0xff);
		MyLog.d(TAG, "b=" + b
				+ "\nc=" + c
				+ "\nd=" + d
				+ "\ne=" + e + "\nByteOrder" + ByteOrder.nativeOrder());


	}

	final String wavPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "test.wav";
	final String mp3Path = Environment.getExternalStorageDirectory().getPath() + File.separator + "test.mp3";
	private String recordPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "text33.wav";

	private long currentTime;
//	AudioBufferSave saveFile;


	private void init() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				putAssetsToSDCard("test.wav", wavPath);
			}
		}).start();

//		Mp3Converter.init_util(44100, 1, 0, 44100, 96, 7);

		// Example of a call to a native method
		TextView tv = (TextView) findViewById(R.id.sample_text);
		tv.setText(Mp3Converter.getLameVersion());


//		initSave();


		findViewById(R.id.luying).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				recording();
			}
		});


		findViewById(R.id.shifang).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				try {
//					CoreRecorderManager manager = CoreRecorderManager.getInstance();
					coreManagerBuffer.recorderStop();


					Mp3Converter.close();
				} catch (Exception e) {
					e.printStackTrace();

				}
			}
		});

		findViewById(R.id.zhuanma).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
//				player();
				try {

					new Thread(new Runnable() {
						@Override
						public void run() {
							long start_time = System.currentTimeMillis();
//							Mp3Converter.convertMp3(wavPath, mp3Path);

							Mp3Converter.convertMp3(recordPath, mp3Path);
							MyLog.d(TAG, ((System.currentTimeMillis() - start_time) / 1000) + "");
						}
					}).start();


				} catch (Exception e) {
					e.printStackTrace();

				}
			}
		});
	}

	private void initSave() {
//		saveFile = new AudioBufferSave(new AudioBufferSave.AudioBufferSaveCallback() {
//
//			@Override
//			public void onSuccess(long fileSize) {
//
//				Log.d(TAG, "saveFile onSuccess");
//			}
//
//			@Override
//			public void onFailed(String s) {
//				Log.d(TAG, "saveFile onFailed  " + s);
//
//			}
//		});
//		Log.d(TAG, "setSavePath  " + recordPath);
//
//		saveFile.setSavePath(recordPath);
	}



	public void record() {
		MediaRecorder recorder = new MediaRecorder();
		recorder.setAudioChannels(1);
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setAudioSamplingRate(8000);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);

		recorder.start();

	}

	/**
	 * 存储
	 */
	private void recording() {
		try {
			CoreRecorderManager manager; //= CoreRecorderManager.getInstance();
			CoreRecorderManager.RecorderBuilder builder = new CoreRecorderManager.RecorderBuilder();
			builder.setCallbackInterval(100);
			builder.setChannelConfig(AudioFormat.CHANNEL_IN_MONO);
			builder.setSampleRate(CoreRecorderManager.RecorderBuilder.SAMPLE_RATE_8K_HZ);
			builder.setAudioFormat(AudioFormat.ENCODING_PCM_16BIT);

			manager = builder.build();


			manager.setCoreRecorderCallback(new CoreRecorderManager.CoreRecorderCallback() {
				@Override
				public void onRecorder(short[] pcmBuffer, int size) {

//					long l = System.currentTimeMillis();
//					long time = l - currentTime;
//					currentTime = l;
//					Log.d(TAG, pcmBuffer.length + " 时间差为" + time);

//							for (short i : pcmBuffer) {
//								Log.d(TAG, i + "");
//							}

//					if (saveFile == null) {
//						return;
//					}
//					byte[] bytes = BytesTransUtil.getInstance().shorts2Bytes(pcmBuffer);
//					saveFile.saveByet(bytes, 0, size);

				}

				@Override
				public boolean onRecorderReady() {
					return true;
				}

				@Override
				public boolean onRecorderStart(int sampleRate, int channelCount, int audioFormat) {

					MyLog.d(TAG, "onRecorderStart  \nsampleRate  " + sampleRate + "\n  channelCount  " + channelCount +
							"\n  audioFormat  " + audioFormat);


					Mp3Converter.init_util(sampleRate, channelCount, audioFormat, sampleRate, 128, 7);
//					if (saveFile == null) {
//						return true;
//					}
//
//					saveFile.setRecordConfig(sampleRate, audioFormat, channelCount);
//					saveFile.onWriteStart();
					return true;
				}

				@Override
				public void onRecorderError(String errorMsg) {
					/*if (saveFile == null) {
						return;
					}
					saveFile.cancel();*/
				}

				@Override
				public void onRecorderStop() {
					/*if (saveFile == null) {
						return;
					}
					saveFile.finish();*/

				}
			});
//			manager.recorderStart();
			coreManagerBuffer = new CoreManagerBuffer(manager);

			coreManagerBuffer.recorderStart();
//					manager.play();
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	public static String TAG = "mp3converter";

	public void onPause() {
		super.onPause();
		this.releaseAudioTrack();
	}

	private void releaseAudioTrack() {
		if (this.audioTrack != null) {
			MyLog.d(TAG, "Stopping");
			audioTrack.stop();
			MyLog.d(TAG, "Releasing");
			audioTrack.release();
			MyLog.d(TAG, "Nulling");
		}
	}

	public void putAssetsToSDCard(String assetsPath,
								  String sdCardPath) {
		try {
			String mString[] = getAssets().list(assetsPath);
			if (mString.length == 0) { // 说明assetsPath为空,或者assetsPath是一个文件

				File file = new File(sdCardPath);
				if (!file.exists()) {
					file.createNewFile(); // 创建文件
					MyLog.d(TAG, "创建了新文件");
				} else {
					MyLog.d(TAG, "已经存在该文件啦");
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
				MyLog.d(TAG, "文件写入完成");
			}
		} catch (Exception e) {
			e.printStackTrace();
			MyLog.d(TAG, e.getMessage());
		}
	}


	private void player() {
		new Thread(new PcmPlayerRunnable()).start();


	}

	int bufsize;
	AudioTrack audioTrack;

	private class PcmPlayerRunnable implements Runnable {
		@Override
		public void run() {
			try {
				String fileName = recordPath;

				InputStream in = new FileInputStream(new File(fileName));


//				int sampleRateInHz = saveFile.mSampleRate;
				int sampleRateInHz = 44100;

				//根据采样率，采样精度，单双声道来得到frame的大小。


//				int channelOutMono = saveFile.mChannelConfig;
				int channelOutMono = AudioFormat.CHANNEL_OUT_MONO;
//				if (saveFile.mChannelConfig == AudioFormat.CHANNEL_IN_MONO) {
//					channelOutMono = 1;
//				} else {
//					channelOutMono = 2;
//				}


				int encodingPcm8bit = AudioFormat.ENCODING_PCM_16BIT;
//				if (saveFile.mAudioFormat == AudioFormat.ENCODING_PCM_16BIT) {
//					encodingPcm8bit = 16;
//				} else {
//					encodingPcm8bit = 8;
//				}


				bufsize = AudioTrack.getMinBufferSize(sampleRateInHz,
						channelOutMono,
						encodingPcm8bit);

				audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRateInHz,
						channelOutMono, encodingPcm8bit,
						bufsize,
						AudioTrack.MODE_STREAM);//AudioTrack.MODE_STATIC 文件模式   AudioTrack.MODE_STREAM流模式


				byte[] buf = new byte[bufsize];
//				ByteArrayOutputStream out = new ByteArrayOutputStream(264848);
				for (int readCount = 0; (readCount = in.read(buf)) > 1; ) {
//					out.write(b);
					if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE) {
						continue;
					}
					audioTrack.play();
					audioTrack.write(buf, 0, readCount);
				}


			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();

			}
		}
	}


}
