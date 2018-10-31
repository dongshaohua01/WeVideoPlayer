package com.dsh.wevideoplayer.activtiy;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.dsh.wevideoplayer.BaseActivity;
import com.dsh.wevideoplayer.R;
import com.dsh.wevideoplayer.bean.VideoItem;
import com.dsh.wevideoplayer.util.StringUtil;
import com.dsh.wevideoplayer.view.VideoView;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.ArrayList;

public class VitamioPlayerActivity extends BaseActivity{
	VideoView videoView;
	//顶部控制面板控件
	private ImageView btn_exit,btn_pre,btn_play,btn_next,btn_screen;
	private TextView tv_name,tv_time;
	private ImageView iv_battery;
	private ImageView iv_volume;
	private SeekBar volumn_seekbar;
	private LinearLayout ll_top_control;
	//底部控制面板控件
	private TextView tv_current_position,tv_total_time;
	private SeekBar play_seekbar;
	private LinearLayout ll_bottom_control;
	private LinearLayout ll_loading;
	private LinearLayout ll_buffering;
	
	private IntentFilter filter;
	private BatteryBroadcastReceiver batteryBroadcastReceiver;
	
	private final int MESSAGE_UPDATE_TIME = 0;
	private final int MESSAGE_UPDATE_PLAY_PROGRESS = 1;//更新播放进度
	private final int MESSAGE_HIDE_CONTROL = 2;//隐藏控制面板
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MESSAGE_UPDATE_TIME:
				updateSystemTime();
				break;
			case MESSAGE_UPDATE_PLAY_PROGRESS:
				updatePlayProgress();
				break;
			case MESSAGE_HIDE_CONTROL:
				hideControl();
				break;
			}
		};
	};
	private int maxVolume;//最大音量
	private int currentVolume;//当前音量
	private AudioManager audioManager;
	private boolean isMute = false;//是否是静音模式
	private int screenWidth,screenHeight;
	private int currentPosition;//当前播放视频在集合中的位置
	private ArrayList<VideoItem> videoList;//从视频列表界面传入的集合
	private GestureDetector gestureDetector;
	private boolean isShowControl = false;//是否显示控制面板
	
	@Override
	public void initView() {
		/*if (!LibsChecker.checkVitamioLibs(this))
			return;*/
		setContentView(R.layout.activity_vitamio_player);
		videoView = (VideoView) findViewById(R.id.videoView);
		btn_exit = (ImageView) findViewById(R.id.btn_exit);
		btn_pre = (ImageView) findViewById(R.id.btn_pre);
		btn_play = (ImageView) findViewById(R.id.btn_play);
		btn_next = (ImageView) findViewById(R.id.btn_next);
		btn_screen = (ImageView) findViewById(R.id.btn_screen);
		tv_name = (TextView) findViewById(R.id.tv_name);
		tv_time = (TextView) findViewById(R.id.tv_time);
		iv_battery = (ImageView) findViewById(R.id.iv_battery);
		iv_volume = (ImageView) findViewById(R.id.iv_volume);
		volumn_seekbar = (SeekBar) findViewById(R.id.volumn_seekbar);
		tv_current_position = (TextView) findViewById(R.id.tv_current_position);
		tv_total_time = (TextView) findViewById(R.id.tv_total_time);
		play_seekbar = (SeekBar) findViewById(R.id.play_seekbar);
		ll_top_control = (LinearLayout) findViewById(R.id.ll_top_control);
		ll_bottom_control = (LinearLayout) findViewById(R.id.ll_bottom_control);
		ll_loading = (LinearLayout) findViewById(R.id.ll_loading);
		ll_buffering = (LinearLayout) findViewById(R.id.ll_buffering);
		
//		ll_top_control.measure(0, 0);//通知系统去主动测量(调用measure)
//		ViewPropertyAnimator.animate(ll_top_control).translationY(-1*ll_top_control.getMeasuredHeight()).setDuration(0);
	}

	@Override
	public void initLinstener() {
		iv_volume.setOnClickListener(this);
		btn_exit.setOnClickListener(this);
		btn_pre.setOnClickListener(this);
		btn_play.setOnClickListener(this);
		btn_next.setOnClickListener(this);
		btn_screen.setOnClickListener(this);
		
		//当view包括自己的子view全部layout完成时执行该回调方法
		ll_top_control.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				//及时移除监听器，是因为只要某个子view的宽高改变或者layout改变都会引起该方法回调
				ll_top_control.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				ViewPropertyAnimator.animate(ll_top_control).translationY(-1*ll_top_control.getHeight()).setDuration(0);
			}
		});
		ll_bottom_control.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				ll_bottom_control.getViewTreeObserver().removeGlobalOnLayoutListener(this);
				ViewPropertyAnimator.animate(ll_bottom_control).translationY(ll_bottom_control.getHeight()).setDuration(0);
			}
		});
		
		
		volumn_seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				handler.sendEmptyMessageDelayed(MESSAGE_HIDE_CONTROL, 5000);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				handler.removeMessages(MESSAGE_HIDE_CONTROL);
			}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){//是人为手动拖动改变进度
					isMute = false;
					currentVolume = progress;
					updateVolume();
				}
			}
		});
		play_seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				handler.sendEmptyMessageDelayed(MESSAGE_HIDE_CONTROL, 5000);
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				handler.removeMessages(MESSAGE_HIDE_CONTROL);
			}
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				if(fromUser){
					videoView.seekTo(progress);
					tv_current_position.setText(StringUtil.formatVideoDuration(videoView.getCurrentPosition()));
				}
			}
		});
		videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				btn_play.setBackgroundResource(R.drawable.selector_btn_play);
				//防止播放完成之后仍然去执行更新进度的任务
				play_seekbar.setProgress((int) videoView.getDuration());
				handler.removeMessages(MESSAGE_UPDATE_PLAY_PROGRESS);
			}
		});
		videoView.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
			@Override
			public void onBufferingUpdate(MediaPlayer mp, int percent) {
				float floatPercent = percent/100f;
				float bufferProgress = floatPercent*videoView.getDuration();
				play_seekbar.setSecondaryProgress((int) bufferProgress);
			}
		});
		videoView.setOnInfoListener(new MediaPlayer.OnInfoListener() {
			@Override
			public boolean onInfo(MediaPlayer mp, int what, int extra) {
				switch (what) {
				case MediaPlayer.MEDIA_INFO_BUFFERING_START://播放开始卡顿
					ll_buffering.setVisibility(View.VISIBLE);
//					Toast.makeText(VideoPlayerActivity.this, "开始卡顿", 0).show();
					break;
				case MediaPlayer.MEDIA_INFO_BUFFERING_END://播放卡顿结束
					ll_buffering.setVisibility(View.GONE);
//					Toast.makeText(VideoPlayerActivity.this, "卡顿结束了。。。", 0).show();
					break;
				}
				return false;
			}
		});
		videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				switch (what) {
				case MediaPlayer.MEDIA_ERROR_UNKNOWN:
					AlertDialog.Builder dialog = new AlertDialog.Builder(VitamioPlayerActivity.this);
					dialog.setTitle("提示");
					dialog.setMessage("播放出错,点击确定退出播放器");
					dialog.setPositiveButton("确定", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					});
					dialog.create().show();
					break;
				}
				return true;
			}
		});
	}

	@Override
	public void initDate() {
		screenWidth = getWindowManager().getDefaultDisplay().getWidth();
		screenHeight = getWindowManager().getDefaultDisplay().getHeight();
		gestureDetector = new GestureDetector(this, new MyOnGestureListener());
		
		registerBatteryBroadcastReceiver();
		updateSystemTime();
		initVolume();
		
		Uri uri = getIntent().getData();
		if(uri!=null){
			//从文件发起的播放请求
			videoView.setVideoURI(uri);
			btn_next.setEnabled(false);
			btn_pre.setEnabled(false);
			tv_name.setText(uri.getPath());
		}else {
			//从视频列表传入的
			currentPosition = getIntent().getExtras().getInt("currentPosition");
			videoList = (ArrayList<VideoItem>) getIntent().getExtras().getSerializable("videoList");
			playVideo(currentPosition);
		}
		
		
		videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
			@Override
			public void onPrepared(MediaPlayer mp) {
				//给加载界面添加渐隐的动画效果
				ViewPropertyAnimator.animate(ll_loading).alpha(0).setDuration(800).setListener(new AnimatorListener() {
					@Override
					public void onAnimationStart(Animator arg0) {
					}
					@Override
					public void onAnimationRepeat(Animator arg0) {
					}
					@Override
					public void onAnimationEnd(Animator arg0) {
						ll_loading.setVisibility(View.GONE);
					}
					@Override
					public void onAnimationCancel(Animator arg0) {
					}
				});

				videoView.start();
				
				btn_play.setBackgroundResource(R.drawable.selector_btn_pause);
				play_seekbar.setMax((int) videoView.getDuration());
				tv_current_position.setText("00:00");
				tv_total_time.setText(StringUtil.formatVideoDuration(videoView.getDuration()));
				updatePlayProgress();
			}
		});
		
//		videoView.setMediaController(new MediaController(this));
	}
	
	/**
	 * 播放指定位置的视频
	 * @param position
	 */
	private void playVideo(int position){
		if(videoList==null || videoList.size()==0){
			finish();
			return;
		}
		btn_next.setEnabled(currentPosition!=(videoList.size()-1));
		btn_pre.setEnabled(currentPosition!=0);
		
		VideoItem videoItem = videoList.get(position);
		tv_name.setText(videoItem.getTitle());
		videoView.setVideoURI(Uri.parse(videoItem.getPath()));
	}
	
	/**
	 * 更新播放进度和时间
	 */
	private void updatePlayProgress(){
		tv_current_position.setText(StringUtil.formatVideoDuration(videoView.getCurrentPosition()));
		play_seekbar.setProgress((int) videoView.getCurrentPosition());
		handler.sendEmptyMessageDelayed(MESSAGE_UPDATE_PLAY_PROGRESS, 1000);
	}
	
	/**
	 * 初始化音量
	 */
	private void initVolume(){
		audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		//maxVolume最大是15，
		maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		volumn_seekbar.setMax(maxVolume);
		volumn_seekbar.setProgress(currentVolume);
	}
	
	/**
	 * 更新音量
	 */
	private void updateVolume(){
		if(isMute){
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
			volumn_seekbar.setProgress(0);
		}else {
			//第三个参数为1的时候，会显示一个view指示当前音量的变化，一般用0
			audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);
			volumn_seekbar.setProgress(currentVolume);
		}
	}
	
	/**
	 * 注册电量变化广播接受者
	 */
	private void registerBatteryBroadcastReceiver(){
		filter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		batteryBroadcastReceiver = new BatteryBroadcastReceiver();
		registerReceiver(batteryBroadcastReceiver, filter);
	}

	/**
	 * 更新系统时间
	 */
	private void updateSystemTime(){
		tv_time.setText(StringUtil.formatSystemTime());
		handler.sendEmptyMessageDelayed(MESSAGE_UPDATE_TIME, 1000);
	}
	
	@Override
	public void processClick(View view) {
		switch (view.getId()) {
		case R.id.btn_play:
			if(videoView.isPlaying()){
				videoView.pause();
				handler.removeMessages(MESSAGE_UPDATE_PLAY_PROGRESS);
			}else {
				videoView.start();
				handler.sendEmptyMessage(MESSAGE_UPDATE_PLAY_PROGRESS);
			}
			updatePlayBtnBg();
			break;
		case R.id.iv_volume:
			isMute = !isMute;
			updateVolume();
			break;
		case R.id.btn_next:
			playNext();
			break;
		case R.id.btn_pre:
			playPre();
			break;
		case R.id.btn_exit:
			finish();
			break;
		case R.id.btn_screen:
			videoView.switchScreen();
			updateScreenBtnBg();
			break;
		}
	}
	
	/**
	 * 改变屏幕按钮的背景图片
	 */
	private void updateScreenBtnBg(){
		btn_screen.setBackgroundResource(videoView.isFullScreen()?
				R.drawable.selector_btn_defaultscreen:R.drawable.selector_btn_fullscreen);
	}
	
	/**
	 * 播放下一个视频
	 */
	private void playNext(){
		if(currentPosition==(videoList.size()-1)){
			return;
		}
		currentPosition++;
		playVideo(currentPosition);
	}
	
	/**
	 * 播放上一个视频
	 */
	private void playPre(){
		if(currentPosition==0){
			return;
		}
		currentPosition--;
		playVideo(currentPosition);
	}
		
	private float downY;
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			downY = event.getY();
			break;
		case MotionEvent.ACTION_MOVE:
			float moveY = event.getY();
			float moveDistance = moveY - downY;//有可能是负值
			
			//如果用户滑动的距离小于15，则认为不想滑动改变音量
			if(Math.abs(moveDistance)<15)break;
			
			isMute = false;
			int totalDistance = Math.min(screenHeight, screenWidth);
			float movePercent = Math.abs(moveDistance)/totalDistance;
			float moveVolume = movePercent * maxVolume;//是特别小的一个值
			if(moveDistance>0){
				//减小音量
				currentVolume -= 1;
			}else {
				//增减音量
				currentVolume += 1;
			}
			
			updateVolume();
			
			downY = moveY;
			break;
		case MotionEvent.ACTION_UP:
			
			break;
		}
		return super.onTouchEvent(event);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		handler.removeCallbacksAndMessages(null);
		unregisterReceiver(batteryBroadcastReceiver);
	}
	
	class MyOnGestureListener extends SimpleOnGestureListener{
		@Override
		public void onLongPress(MotionEvent e) {
			super.onLongPress(e);
			processClick(btn_play);
		}
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			processClick(btn_screen);
			return super.onDoubleTap(e);
		}
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			if(isShowControl){
				//隐藏控制面板
				hideControl();
			}else {
				//显示控制面板
				showControl();
			}
			return super.onSingleTapConfirmed(e);
		}
	}
	
	/**
	 * 隐藏控制面板
	 */
	private void hideControl(){
		ViewPropertyAnimator.animate(ll_top_control).translationY(-1*ll_top_control.getHeight()).setDuration(200);
		ViewPropertyAnimator.animate(ll_bottom_control).translationY(ll_bottom_control.getHeight()).setDuration(200);
		isShowControl = false;
	}
	/**
	 * 显示控制面板
	 */
	private void showControl(){
		ViewPropertyAnimator.animate(ll_top_control).translationY(0).setDuration(200);
		ViewPropertyAnimator.animate(ll_bottom_control).translationY(0).setDuration(200);
		isShowControl = true;
		
		handler.sendEmptyMessageDelayed(MESSAGE_HIDE_CONTROL, 5000);
	}
	
	
	/**
	 * 更新播放按钮的背景图片
	 */
	private void updatePlayBtnBg(){
		btn_play.setBackgroundResource(videoView.isPlaying()?
				R.drawable.selector_btn_pause:R.drawable.selector_btn_play);
	}

	private class BatteryBroadcastReceiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context context, Intent intent) {
			//level:0-100
			int level = intent.getIntExtra("level", 0);
//			LogUtil.e(this, "level: "+level);
			updateBatteryImage(level);
		}
		
	}
	
	/**
	 * 根据电量level设置不同的图片
	 * @param level
	 */
	private void updateBatteryImage(int level){
		if(level<=0){
			iv_battery.setBackgroundResource(R.drawable.ic_battery_0);
		}else if (level>0 && level<=10) {
			iv_battery.setBackgroundResource(R.drawable.ic_battery_10);
		}else if (level>10 && level<20) {
			iv_battery.setBackgroundResource(R.drawable.ic_battery_20);
		}else if (level>20 && level<=40) {
			iv_battery.setBackgroundResource(R.drawable.ic_battery_40);
		}else if (level>40 && level<=60) {
			iv_battery.setBackgroundResource(R.drawable.ic_battery_60);
		}else if (level>60 && level<=80) {
			iv_battery.setBackgroundResource(R.drawable.ic_battery_80);
		}else  {
			iv_battery.setBackgroundResource(R.drawable.ic_battery_100);
		}
	}
	
}
