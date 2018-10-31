package com.dsh.wevideoplayer.bean;

import java.io.Serializable;

import android.database.Cursor;
import android.provider.MediaStore.Video.Media;

public class VideoItem implements Serializable{
	private long size;
	private String title;
	private long duration;
	private String path;
	
	public static VideoItem fromCursor(Cursor cursor){
		VideoItem videoItem = new VideoItem();
		videoItem.setDuration(cursor.getLong(cursor.getColumnIndex(Media.DURATION)));
		videoItem.setPath(cursor.getString(cursor.getColumnIndex(Media.DATA)));
		videoItem.setSize(cursor.getLong(cursor.getColumnIndex(Media.SIZE)));
		videoItem.setTitle(cursor.getString(cursor.getColumnIndex(Media.TITLE)));
		return videoItem;
	}
	
	public long getSize() {
		return size;
	}
	public void setSize(long size) {
		this.size = size;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
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

	@Override
	public String toString() {
		return "VideoItem [size=" + size + ", title=" + title + ", duration="
				+ duration + ", path=" + path + "]";
	}
	
	
}
