package com.dsh.wevideoplayer.util;

import java.io.File;

/**
 * 模拟歌词加载模块
 * @author Administrator
 *
 */
public class LyricLoader {
	private static String LYRIC_DIR = "/mnt/sdcard/test/audio";
	public static File loadLyricFile(String audioName){
		File file = new File(LYRIC_DIR, StringUtil.formatAudioName(audioName)+".txt");
		if(!file.exists()){
			file = new File(LYRIC_DIR, StringUtil.formatAudioName(audioName)+".lrc");
		}
		return file;
	}
}
