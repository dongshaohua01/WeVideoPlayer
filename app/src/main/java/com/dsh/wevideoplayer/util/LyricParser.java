package com.dsh.wevideoplayer.util;

import com.dsh.wevideoplayer.bean.Lyric;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;


/**
 * 歌词解析模块
 * @author Administrator
 *
 */
public class LyricParser {
	private static String tag = LyricParser.class.getSimpleName();
	public static ArrayList<Lyric> parseLyricFromFile(File lyricFile){
		if(lyricFile==null || !lyricFile.exists()) return null;
		
		ArrayList<Lyric> list = new ArrayList<Lyric>();
		
		try {
			//1.获取每一行文本内容
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(lyricFile), "gbk"));
			String line;
			while((line=reader.readLine())!=null){
				//2.将每行歌词内容转为lyric对象
				//[00:07.33][00:02.00]听个工人说 -split("]")
				//[00:07.33  [00:02.00      听个工人说 
				String[] arr = line.split("\\]");
				for (int i = 0; i < arr.length-1; i++) {
					Lyric lyric = new Lyric();
					lyric.setContent(arr[arr.length-1]);
					lyric.setStartPoint(formatLyricStartPoint(arr[i]));
					
					list.add(lyric);
				}
			}
			//3.对歌词进行排序
			Collections.sort(list);
		}catch (IOException e) {
			e.printStackTrace();
		}
		
		return list;
	}
	
	/**
	 * 将[00:07.33转为long
	 * @param startPoint
	 * @return
	 */
	private static long formatLyricStartPoint(String startPoint){
		startPoint = startPoint.substring(1);
		//split(":") - > 00   07.33
		String[] arr = startPoint.split("\\:");//第1个元素是分钟
		String[] arr2 = arr[1].split("\\.");//07   33  
		int minute = Integer.parseInt(arr[0]);//00
		int second = Integer.parseInt(arr2[0]);//07
		int mills = Integer.parseInt(arr2[1]);//33
		return minute*60*1000 + second*1000 + mills*10;
	}
}
