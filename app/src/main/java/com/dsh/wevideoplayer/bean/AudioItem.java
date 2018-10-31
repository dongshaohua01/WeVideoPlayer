package com.dsh.wevideoplayer.bean;

import android.database.Cursor;
import android.provider.MediaStore.Audio.Media;

import java.io.Serializable;

public class AudioItem implements Serializable{
	private String title;
	private String artist;
	private long duration;
	private String path;
	
	public static AudioItem fromCursor(Cursor cursor){
		AudioItem audioItem = new AudioItem();
		audioItem.setArtist(cursor.getString(cursor.getColumnIndex(Media.ARTIST)));
		audioItem.setDuration(cursor.getLong(cursor.getColumnIndex(Media.DURATION)));
		audioItem.setPath(cursor.getString(cursor.getColumnIndex(Media.DATA)));
		audioItem.setTitle(cursor.getString(cursor.getColumnIndex(Media.DISPLAY_NAME)));
		return audioItem;
	}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getArtist() {
		return artist;
	}
	public void setArtist(String artist) {
		this.artist = artist;
	}
	public long getDuration() {
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	
	
}
