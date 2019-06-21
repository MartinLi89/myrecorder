package com.example.martin.mytranvice;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
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
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.util.List;

import demo.martin.simplerecorder.BytesTransUtil;
import demo.martin.simplerecorder.CoreManagerBuffer;
import demo.martin.simplerecorder.CoreRecorderManager;
import demo.martin.simplerecorder.ManagerInterface;
import demo.martin.simplerecorder.MyLog;
import demo.martin.simplerecorder.RecordCenter.Myrecorder;
import demo.martin.simplerecorder.playercenter.PcmPlayer;

public class MainActivity extends AppCompatActivity {
	private ManagerInterface coreManagerBuffer;

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
	private String recordPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "text33.wav";
	private String testPath = Environment.getExternalStorageDirectory().getPath() + File.separator + "text33.wav";



	private void init() {

		new Thread(new Runnable() {
			@Override
			public void run() {
				Utils.putAssetsToSDCard(MainActivity.this,"test.wav", wavPath);
			}
		}).start();


		// Example of a call to a native method
		TextView tv = (TextView) findViewById(R.id.sample_text);
		tv.setText(Mp3Converter.getLameVersion());




		findViewById(R.id.luying).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				recording();

//				new Thread(new Runnable() {
//					@Override
//					public void run() {
//						recording2();
//					}
//				}).start();

			}


		});


		findViewById(R.id.shifang).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {


//				recording2();
				try {
					coreManagerBuffer.recorderStop();


//					Mp3Converter.close();
				} catch (Exception e) {
					e.printStackTrace();

				}
			}
		});

		findViewById(R.id.zhuanma).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				player();
//				zhuanMa();
			}
		});
	}

	private void zhuanMa() {
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

//
//	boolean isRecording = false;
//	AudioRecord audioRecord;
//	RandomAccessFile randomAccessFile = null;


	/*private void recording2() {
		if (isRecording) {
			if (audioRecord != null) {
				audioRecord.stop();

			}
			isRecording = false;
			MyLog.d(TAG, "录音停止");
			return;
		}


		isRecording = true;
		int sampleRate = 44100;
		int channle = AudioFormat.CHANNEL_IN_MONO;
		int weikuan = AudioFormat.ENCODING_PCM_16BIT;
		int minBufferSize = AudioRecord.getMinBufferSize(sampleRate, channle, weikuan);
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
				44100
				, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize);
		audioRecord.startRecording();

		short[] bufferSize = new short[minBufferSize + 500];
		FileOutputStream outputStream = null;

		if (audioRecord == null) {
			return;
		}
		try {


			File file = new File(recordPath);
			if (file.exists()) {
				file.delete();
			} else {
				File parentFile = file.getParentFile();
				if (!parentFile.exists()) {
					parentFile.mkdirs();
				}
			}

			randomAccessFile = new RandomAccessFile(file, "rw");
//			outputStream = new FileOutputStream(file);
			randomAccessFile.setLength(0);

			MyLog.d(TAG, "录音开始");
			for (; isRecording; ) {
				int readLen = audioRecord.read(bufferSize, 0, bufferSize.length);

				if (readLen > 0) {

					byte[] bytes = BytesTransUtil.getInstance().shorts2Bytes(bufferSize);
//					outputStream.write(bytes, 0, readLen*2 );
					randomAccessFile.write(bytes, 0, readLen * 2);
				} else {

					isRecording = false;
					MyLog.d(TAG, "录音停止");
				}
			}
//			outputStream.flush();


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			isRecording = false;
			if (randomAccessFile != null) {
				try {
					randomAccessFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				randomAccessFile = null;

			}
			if (outputStream != null) {
				try {
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				outputStream = null;
			}

		}


	}*/


	/**
	 * 存储
	 */
	private void recording() {
		try {
			ManagerInterface manager; //= CoreRecorderManager.getInstance();


			CoreRecorderManager.RecorderBuilder builder = new CoreRecorderManager.RecorderBuilder();
			builder.setCallbackInterval(100);
			builder.setChannelConfig(AudioFormat.CHANNEL_IN_MONO);
			builder.setSampleRate(CoreRecorderManager.RecorderBuilder.SAMPLE_RATE_44K_HZ);
			builder.setAudioFormat(AudioFormat.ENCODING_PCM_16BIT);

			manager = builder.build();

//			manager = Myrecorder.getInstance();

			manager.setCoreRecorderCallback(coreRecorderCallback);
			coreManagerBuffer = new CoreManagerBuffer(manager);

//			coreManagerBuffer = manager;
			coreManagerBuffer.recorderStart();
		} catch (Exception e) {
			e.printStackTrace();

		}
	}

	CoreRecorderManager.CoreRecorderCallback coreRecorderCallback = new CoreRecorderManager.CoreRecorderCallback() {
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
		public void onRecorder(byte[] pcmBuffer, int size) {


		}

		@Override
		public boolean onRecorderReady() {
			return true;
		}

		@Override
		public boolean onRecorderStart(int sampleRate, int channelCount, int audioFormat) {
			PcmPlayer.getInstance().setConfig(sampleRate, channelCount, audioFormat);

			MyLog.d(TAG, "onRecorderStart  \nsampleRate  " + sampleRate + "\n  channelCount  " + channelCount +
					"\n  audioFormat  " + audioFormat);


//					Mp3Converter.init_util(sampleRate, channelCount, audioFormat, sampleRate, 128, 7);
			return true;
		}

		@Override
		public void onRecorderError(String errorMsg) {

		}

		@Override
		public void onRecorderStop() {

		}
	};
	public static String TAG = "mp3converter";

	public void onPause() {
		super.onPause();
		this.releaseAudioTrack();
	}

	private void releaseAudioTrack() {

		PcmPlayer.getInstance().stopPlayer();

	}



	private void player() {
		PcmPlayer.getInstance().setResource(recordPath);
		PcmPlayer.getInstance().startPlayer();

	}

}
