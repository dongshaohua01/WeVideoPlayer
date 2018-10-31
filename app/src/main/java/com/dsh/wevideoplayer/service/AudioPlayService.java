package com.dsh.wevideoplayer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.RemoteViews;

import com.dsh.wevideoplayer.R;
import com.dsh.wevideoplayer.activtiy.AudioPlayerActivity;
import com.dsh.wevideoplayer.bean.AudioItem;
import com.dsh.wevideoplayer.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;

public class AudioPlayService extends Service{
	public static final String ACION_MEDIA_PREPARED = "ACION_MEDIA_PREPARED";
	public static final String ACION_MEDIA_COMPLETION = "ACION_MEDIA_COMPLETION";
	public static final String ACION_MEDIA_FIRST = "ACION_MEDIA_FIRST";
	public static final String ACION_MEDIA_LAST = "ACION_MEDIA_LAST";
	
	public static final int MODE_ORDER = 0;//顺序播放
	public static final int MODE_SINGLE_REPEAT = 1;//单曲循环
	public static final int MODE_ALL_REPEAT = 2;//循环播放
	public static int currentPlayMode = MODE_ORDER;//默认是顺序播放
	
	private static final int VIEW_PRE = 1;//通知栏的上一个
	private static final int VIEW_NEXT = 2;//通知栏的下一个
	private static final int VIEW_CONTAINER = 3;//通知栏的整体布局
	
	private SharedPreferences sp;
	private NotificationManager notificationManager;
	
	private AudioServiceBinder audioServiceBinder;
	private MediaPlayer mediaPlayer;
	
	private ArrayList<AudioItem> audioList;
	private int currentPosition;
	@Override
	public IBinder onBind(Intent intent) {
		return audioServiceBinder;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		audioServiceBinder = new AudioServiceBinder();
		sp = getSharedPreferences("palymode.cfg", 0);
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}
	
	
	/**
	 * 只有在调用startService时候才会调用这个方法
	 */
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(intent!=null && intent.getExtras()!=null){
			boolean isFromNotification = intent.getBooleanExtra("isFromNotification", false);
			if(isFromNotification){
				int viewAction = intent.getIntExtra("view_action", -1);
				switch (viewAction) {
				case VIEW_PRE:
					audioServiceBinder.playPre(false);
					break;
				case VIEW_NEXT:
					audioServiceBinder.playNext(false);
					break;
				case VIEW_CONTAINER:
					notifyPrepared();
					break;
				}
			}else {
				audioList = (ArrayList<AudioItem>) intent.getExtras().getSerializable("audioList");
				currentPosition = intent.getExtras().getInt("currentPosition");
				audioServiceBinder.openAudio();
			}
		}
		currentPlayMode = getPlayMode();
		return START_STICKY;//当服务被杀死后，会自动重启
	}
	public class AudioServiceBinder extends Binder{
		
		/**
		 * 播放一个音频
		 */
		public void openAudio(){
			if(mediaPlayer!=null){
				mediaPlayer.release();
				mediaPlayer = null;
			}
			mediaPlayer = new MediaPlayer();
			try {
				mediaPlayer.setOnPreparedListener(mOnPreparedListener);
				mediaPlayer.setOnCompletionListener(mOnCompletionListener);
				mediaPlayer.setDataSource(audioList.get(currentPosition).getPath());
				mediaPlayer.prepareAsync();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		public boolean isPlaying(){
			return mediaPlayer!=null?mediaPlayer.isPlaying():false;
		}
		
		
		public void pause(){
			if(mediaPlayer!=null){
				mediaPlayer.pause();
			}
//			notificationManager.cancel(1);
			stopForeground(true);
		}
		
		public void start(){
			if(mediaPlayer!=null){
				mediaPlayer.start();
			}
			sendNotification();
		}
		
		public long getCurrentPosition(){
			return mediaPlayer!=null?mediaPlayer.getCurrentPosition():0;
		}
		
		public long getDuration(){
			return mediaPlayer!=null?mediaPlayer.getDuration():0;
		}
		
		public void seekTo(int position){
			if(mediaPlayer!=null){
				mediaPlayer.seekTo(position);
			}
		}
		
		public void playNext(boolean isShowTips){
			if(currentPosition<(audioList.size()-1)){
				currentPosition++;
				openAudio();
			}else {
				if(isShowTips){
					notifyFirstAndLast(ACION_MEDIA_LAST);
				}
			}
		}
		
		public void playPre(boolean isShowTips){
			if(currentPosition>0){
				currentPosition--;
				openAudio();
			}else {
				if(isShowTips){
					notifyFirstAndLast(ACION_MEDIA_FIRST);
				}
			}
		}
		
		/**
		 * 切换播放模式
		 */
		public void switchPlayMode(){
			if(currentPlayMode==MODE_ORDER){
				currentPlayMode = MODE_SINGLE_REPEAT;
			}else if (currentPlayMode == MODE_SINGLE_REPEAT) {
				currentPlayMode = MODE_ALL_REPEAT;
			}else if (currentPlayMode == MODE_ALL_REPEAT) {
				currentPlayMode = MODE_ORDER;
			}
			savePlayMode();
		}
		/**
		 * 获取当前的播放模式
		 * @return
		 */
		public int getPlayMode(){
			return currentPlayMode;
		}
	}
	
	
	private OnPreparedListener mOnPreparedListener = new OnPreparedListener() {
		@Override
		public void onPrepared(MediaPlayer mp) {
			audioServiceBinder.start();
			notifyPrepared();
		}
	};
	
	private OnCompletionListener mOnCompletionListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			notifyCompletion();
			
			autoPlayByMode();
		}
	};
	
	/**
	 * 根据播放模式进行自动播放
	 */
	private void autoPlayByMode(){
		switch (currentPlayMode) {
		case MODE_ORDER:
			audioServiceBinder.playNext(false);
			break;
		case MODE_SINGLE_REPEAT:
			audioServiceBinder.openAudio();
			break;
		case MODE_ALL_REPEAT:
			if(currentPosition==(audioList.size()-1)){
				currentPosition = 0;
				audioServiceBinder.openAudio();
			}else {
				audioServiceBinder.playNext(false);
			}
			break;
		}
	}
	
	/**
	 * 发送一个自定义布局的通知
	 */
	private void sendNotification(){
		AudioItem audioItem = audioList.get(currentPosition);
		Notification.Builder builder = new Notification.Builder(this);
		builder.setOngoing(true)
		.setSmallIcon(R.drawable.notification_music_playing)
		.setTicker("正在播放:"+ StringUtil.formatAudioName(audioItem.getTitle()))
		.setWhen(System.currentTimeMillis())
		.setContent(getRemoteViews());
		
//		notificationManager.notify(1, builder.build());
		startForeground(1, builder.build());
	}
	
	private RemoteViews getRemoteViews(){
		AudioItem audioItem = audioList.get(currentPosition);
		RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.layout_notification);
		remoteViews.setTextViewText(R.id.tv_song_name, StringUtil.formatAudioName(audioItem.getTitle()));
		remoteViews.setTextViewText(R.id.tv_artist_name, audioItem.getArtist());
		
		//点击pre按钮执行的intent
		Intent preIntent = createNotificationIntent(VIEW_PRE);
		PendingIntent preContentIntent = PendingIntent.getService(this, 0, preIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.iv_notification_pre, preContentIntent);
		//点击next按钮执行的intent
		Intent nextIntent = createNotificationIntent(VIEW_NEXT);
		PendingIntent nextContentIntent = PendingIntent.getService(this, 1, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.iv_notification_next, nextContentIntent);
		//点击整个通知的布局执行的intent
		Intent containerIntent = createNotificationIntent(VIEW_CONTAINER);;
		PendingIntent containerContentIntent = PendingIntent.getActivity(this, 2, containerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		remoteViews.setOnClickPendingIntent(R.id.notification_container, containerContentIntent);
		
		
		return remoteViews;
	}
	
	private Intent createNotificationIntent(int viewAction){
		Intent intent = new Intent(AudioPlayService.this,viewAction==VIEW_CONTAINER
				?AudioPlayerActivity.class:AudioPlayService.class);
		intent.putExtra("isFromNotification", true);
		intent.putExtra("view_action", viewAction);
		return intent;
	}
	
	
	/**
	 * 通知准备完成
	 */
	private void notifyPrepared(){
		Intent intent = new Intent(ACION_MEDIA_PREPARED);
		Bundle bundle = new Bundle();
		bundle.putSerializable("audioItem", audioList.get(currentPosition));
		intent.putExtras(bundle);
		sendBroadcast(intent);
	}
	
	/**
	 * 通知播放完成
	 */
	private void notifyCompletion(){
		Intent intent = new Intent(ACION_MEDIA_COMPLETION);
		Bundle bundle = new Bundle();
		bundle.putSerializable("audioItem", audioList.get(currentPosition));
		intent.putExtras(bundle);
		sendBroadcast(intent);
	}

	/**
	 * 通知是否是第一个和最后一个
	 */
	private void notifyFirstAndLast(String action){
		Intent intent = new Intent(action);
		sendBroadcast(intent);
	}
	
	private void savePlayMode(){
		sp.edit().putInt("playMode", currentPlayMode).commit();
	}
	
	private int getPlayMode(){
		return sp.getInt("playMode", currentPlayMode);
	}

}
