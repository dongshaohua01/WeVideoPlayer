package com.dsh.wevideoplayer.activtiy;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.dsh.wevideoplayer.BaseActivity;
import com.dsh.wevideoplayer.R;
import com.dsh.wevideoplayer.bean.AudioItem;
import com.dsh.wevideoplayer.bean.Lyric;
import com.dsh.wevideoplayer.service.AudioPlayService;
import com.dsh.wevideoplayer.util.LyricLoader;
import com.dsh.wevideoplayer.util.LyricParser;
import com.dsh.wevideoplayer.util.StringUtil;
import com.dsh.wevideoplayer.view.LyricView;

import java.io.File;
import java.util.ArrayList;


public class AudioPlayerActivity extends BaseActivity{
	private ImageView iv_anim,btn_back;
	private ImageView btn_play,btn_paly_mode,btn_pre,btn_next;
	private TextView tv_title,tv_artist,tv_time;
	private SeekBar seekbar;
	private LyricView lyricView;
	
	private AudioBroadcastReceiver audioBroadcastReceiver;
	private AudioServiceConnnection audioServiceConnnection;
	private AudioPlayService.AudioServiceBinder audioServiceBinder;
	
	private static final int MESSAGE_UPDATE_PROGRESS = 0;//更新进度
	private static final int MESSAGE_UPDATE_LYRIC = 1;//更新歌词
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MESSAGE_UPDATE_PROGRESS:
				updatePlayProgress();
				break;
			case MESSAGE_UPDATE_LYRIC:
				updateLyric();
				break;
			}
		};
	};
	
	/**
	 * 更新歌词
	 */
	private void updateLyric(){
		lyricView.roll(audioServiceBinder.getCurrentPosition(),audioServiceBinder.getDuration());
		handler.sendEmptyMessage(MESSAGE_UPDATE_LYRIC);
	}
	
	/**
	 * 更新播放进度
	 */
	private void updatePlayProgress(){
		seekbar.setProgress((int) audioServiceBinder.getCurrentPosition());
		String currentTime = StringUtil.formatVideoDuration(audioServiceBinder.getCurrentPosition());
		tv_time.setText(currentTime+"/"+StringUtil.formatVideoDuration(audioServiceBinder.getDuration()));
		handler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PROGRESS, 1000);
	}
	
	@Override
	public void initView() {
		setContentView(R.layout.activity_audio_player);
		btn_play = (ImageView) findViewById(R.id.btn_play);
		btn_paly_mode = (ImageView) findViewById(R.id.btn_paly_mode);
		btn_pre = (ImageView) findViewById(R.id.btn_pre);
		btn_next = (ImageView) findViewById(R.id.btn_next);
		tv_title = (TextView) findViewById(R.id.tv_title);
		tv_time = (TextView) findViewById(R.id.tv_time);
		tv_artist = (TextView) findViewById(R.id.tv_artist);
		seekbar = (SeekBar) findViewById(R.id.seekbar);
		lyricView = (LyricView) findViewById(R.id.lyricView);
		
		btn_back = (ImageView) findViewById(R.id.btn_back);
		iv_anim = (ImageView) findViewById(R.id.iv_anim);
		AnimationDrawable animationDrawable = (AnimationDrawable) iv_anim.getBackground();
		animationDrawable.start();
	}

	@Override
	public void initLinstener() {
		btn_back.setOnClickListener(this);
		btn_paly_mode.setOnClickListener(this);
		btn_play.setOnClickListener(this);
		btn_pre.setOnClickListener(this);
		btn_next.setOnClickListener(this);
		
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
			}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){
					audioServiceBinder.seekTo(progress);
					tv_time.setText(StringUtil.formatVideoDuration(progress)+"/"
							+StringUtil.formatVideoDuration(audioServiceBinder.getDuration()));
				}
			}
		});
	}

	@Override
	public void initDate() {
		registerAudioBroadcastReceiver();
		
		Intent intent = new Intent(this,AudioPlayService.class);
		
		boolean isFromNotification = getIntent().getBooleanExtra("isFromNotification", false);
		if(isFromNotification){
			//表面是通知开启的activity
			intent.putExtra("isFromNotification", isFromNotification);
			intent.putExtra("view_action", getIntent().getIntExtra("view_action", -1));
		}else{
			//从音乐列表进入的
			int currentPosition = getIntent().getExtras().getInt("currentPosition");
			ArrayList<AudioItem> audioList = (ArrayList<AudioItem>) getIntent().getExtras().getSerializable("audioList");
			Bundle bundle = new Bundle();
			bundle.putInt("currentPosition", currentPosition);
			bundle.putSerializable("audioList", audioList);
			intent.putExtras(bundle);
		}
		audioServiceConnnection = new AudioServiceConnnection();
		startService(intent);//传递数据用的
		bindService(intent, audioServiceConnnection, Service.BIND_AUTO_CREATE);
		
	}
	
	private void registerAudioBroadcastReceiver(){
		audioBroadcastReceiver = new AudioBroadcastReceiver();
		IntentFilter filter = new IntentFilter(AudioPlayService.ACION_MEDIA_PREPARED);
		filter.addAction(AudioPlayService.ACION_MEDIA_COMPLETION);
		filter.addAction(AudioPlayService.ACION_MEDIA_FIRST);
		filter.addAction(AudioPlayService.ACION_MEDIA_LAST);
		registerReceiver(audioBroadcastReceiver, filter);
	}

	@Override
	public void processClick(View view) {
		switch (view.getId()) {
		case R.id.btn_back:
			finish();
			break;
		case R.id.btn_play:
			if(audioServiceBinder.isPlaying()){
				audioServiceBinder.pause();
			}else {
				audioServiceBinder.start();
			}
			updatePlayBtnBg();
			break;
		case R.id.btn_pre:
			audioServiceBinder.playPre(true);
			break;
		case R.id.btn_next:
			audioServiceBinder.playNext(true);
			break;
		case R.id.btn_paly_mode:
			audioServiceBinder.switchPlayMode();
			updatePlayModeBtnBg();
			break;
		}
	}
	
	/**
	 * 更新播放模式按钮的背景
	 */
	private void updatePlayModeBtnBg(){
		switch (audioServiceBinder.getPlayMode()) {
		case AudioPlayService.MODE_ORDER:
			btn_paly_mode.setBackgroundResource(R.drawable.selector_audio_mode_normal);
			break;
		case AudioPlayService.MODE_SINGLE_REPEAT:
			btn_paly_mode.setBackgroundResource(R.drawable.selector_audio_mode_single_repeat);
			break;
		case AudioPlayService.MODE_ALL_REPEAT:
			btn_paly_mode.setBackgroundResource(R.drawable.selector_audio_mode_all_repeat);
			break;
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		handler.removeCallbacksAndMessages(null);
	}
	
	/**
	 * 根据是否正在播放改变播放按钮的 背景图片
	 */
	private void updatePlayBtnBg(){
		btn_play.setBackgroundResource(audioServiceBinder.isPlaying()?
				R.drawable.selector_btn_audio_pause:R.drawable.selector_btn_audio_play);
	}
	
	
	class AudioBroadcastReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			if(AudioPlayService.ACION_MEDIA_PREPARED.equals(intent.getAction())){
				AudioItem audioItem = (AudioItem) intent.getExtras().getSerializable("audioItem");
				
				seekbar.setMax((int) audioItem.getDuration());
				tv_time.setText("00:00/"+StringUtil.formatVideoDuration(audioItem.getDuration()));
				tv_title.setText(StringUtil.formatAudioName(audioItem.getTitle()));
				tv_artist.setText(audioItem.getArtist());
				btn_play.setBackgroundResource(R.drawable.selector_btn_audio_pause);
				updatePlayModeBtnBg();
				
				updatePlayProgress();//开始更新播放进度
				
				File lyricFile = LyricLoader.loadLyricFile(audioItem.getTitle());
				ArrayList<Lyric> list = LyricParser.parseLyricFromFile(lyricFile);
				lyricView.setLyricList(list);
				
			//	updateLyric();//更新歌词
				
			}else if (AudioPlayService.ACION_MEDIA_COMPLETION.equals(intent.getAction())) {
				AudioItem audioItem = (AudioItem) intent.getExtras().getSerializable("audioItem");
				
				seekbar.setProgress((int) audioItem.getDuration());
				tv_time.setText(StringUtil.formatVideoDuration(audioItem.getDuration())+"/"
				+StringUtil.formatVideoDuration(audioItem.getDuration()));
				btn_play.setBackgroundResource(R.drawable.selector_btn_audio_play);
			}else if (AudioPlayService.ACION_MEDIA_FIRST.equals(intent.getAction())) {
				Toast.makeText(AudioPlayerActivity.this, "当前是第一首", Toast.LENGTH_SHORT).show();
			}else if (AudioPlayService.ACION_MEDIA_LAST.equals(intent.getAction())) {
				Toast.makeText(AudioPlayerActivity.this, "当前是最后一首", Toast.LENGTH_SHORT).show();
			}
		}
		
	}
	
	class AudioServiceConnnection implements ServiceConnection{
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			audioServiceBinder = (AudioPlayService.AudioServiceBinder) service;
		}
		@Override
		public void onServiceDisconnected(ComponentName name) {
			
		}
	}

}
