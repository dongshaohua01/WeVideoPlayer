package com.dsh.wevideoplayer.util;

import android.util.Log;

public class LogUtil {
	private static boolean isDebug = true;
	
	public static void i(String tag,String msg){
		if(isDebug){
			Log.i(tag, msg);
		}
	}
	public static void i(Object object,String msg){
		if(isDebug){
			Log.i(object.getClass().getSimpleName(), msg);
		}
	}
	
	public static void e(String tag,String msg){
		if(isDebug){
			Log.e(tag, msg);
		}
	}
	public static void e(Object object,String msg){
		if(isDebug){
			Log.e(object.getClass().getSimpleName(), msg);
		}
	}
}	
