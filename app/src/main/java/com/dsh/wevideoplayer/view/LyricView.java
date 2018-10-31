package com.dsh.wevideoplayer.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.TextView;

import com.dsh.wevideoplayer.bean.Lyric;

import java.util.ArrayList;

public class LyricView extends TextView{
	private final int LYRIC_ROW_HEIGHT = 22;//每一行歌词的行高
	private int COLOR_LIGHT = Color.GREEN;//高亮颜色
	private int COLOR_DEFAULT = Color.WHITE;//非高亮颜色
	
	private final int SIZE_LIGHT = 17;//高亮行歌词的size
	private final int SIZE_DEFAULT = 15;//非高亮行歌词的size
	
	private int viewWidth,viewHeight;
	
	private ArrayList<Lyric> lyricList;
	private int lightLyricIndex = 0;//高亮行歌词的索引
	private long currentAudioPosition;//当前音乐播放的position
	private long totalDuration;//歌曲的总时间
	
	private Paint mPaint;
	public LyricView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public LyricView(Context context) {
		super(context);
		init();
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		viewWidth = w;
		viewHeight = h;
	}
	
	
	
	private void init(){
		mPaint = new Paint();
		mPaint.setTextSize(SIZE_DEFAULT);
		mPaint.setColor(COLOR_LIGHT);
		mPaint.setAntiAlias(true);
		
//		//虚拟歌词
//		lyricList = new ArrayList<Lyric>();
//		for (int i = 0; i < 50; i++) {
//			lyricList.add(new Lyric("我是歌词 - "+i, i*2000));
//		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
//		String text = "我是歌词";
//		float y = viewHeight/2 + getTextHeight(text)/2;
//		drawCenterHorizontalText(canvas, text, y);
		
		if(lyricList==null){
			String text = "正在加载歌词";
			float y = viewHeight/2 + getTextHeight(text)/2;
			drawCenterHorizontalText(canvas, text, y,true);
		}else {
			drawLyric(canvas);
		}
		
	}
	
	/**
	 * 绘制歌词
	 * @param canvas
	 */
	private void drawLyric(Canvas canvas){
		Lyric lightLyric = lyricList.get(lightLyricIndex);
		//平滑移动歌词
		//1.算出歌词的总的歌唱时间：下一行歌词的startPoint减去我的starPoint
		long lightLyricDuration;
		if(lightLyricIndex==(lyricList.size()-1)){
			lightLyricDuration = totalDuration - lightLyric.getStartPoint();
		}else {
			lightLyricDuration = lyricList.get(lightLyricIndex+1).getStartPoint()-lightLyric.getStartPoint();
		}
		//2.算出当前已经唱的秒数占总时间的百分比：currentPosition减去我的startPoint
		float percent = (float)(currentAudioPosition-lightLyric.getStartPoint())/lightLyricDuration;
		//3.根据百分比算出应该移动的距离: percent*LYRIC_ROW_HEIGHT
		float dy = percent*LYRIC_ROW_HEIGHT;
		
		canvas.translate(0, -dy);
		//1.先画高亮行歌词，作为前后歌词的位置参照
		
		float lightLyricY = viewHeight/2 + getTextHeight(lightLyric.getContent())/2;
		drawCenterHorizontalText(canvas, lightLyric.getContent(), lightLyricY,true);
		
		//2.再画高亮行之前的所有歌词
		for (int i = 0; i < lightLyricIndex; i++) {
			Lyric currentLyric = lyricList.get(i);
			float currentLyricY = lightLyricY - (lightLyricIndex-i)*LYRIC_ROW_HEIGHT;
			drawCenterHorizontalText(canvas, currentLyric.getContent(), currentLyricY,false);
		}
		
		//3.再画高亮行之后的所有歌词
		for (int i = lightLyricIndex+1; i < lyricList.size(); i++) {
			Lyric currentLyric = lyricList.get(i);
			float currentLyricY = lightLyricY + (i - lightLyricIndex)*LYRIC_ROW_HEIGHT;
			drawCenterHorizontalText(canvas, currentLyric.getContent(), currentLyricY,false);
		}
	}
	
	/**
	 * 滚动歌词
	 */
	public void roll(long currentAudioPosition,long totalDuration){
		this.currentAudioPosition = currentAudioPosition;
		this.totalDuration = totalDuration;
		//1.根据音乐播放的position计算出lightLyricIndex
		calculateLightLyrciIndex();
		//2.拿到新的lightLyricIndex之后更新view，
		invalidate();
	}
	
	/**
	 * 逻辑：只要当前音乐的position大于我的startPoint，并且小于我下一行歌词的startPoint,那
	 * 我就是高亮歌词
	 */
	private void calculateLightLyrciIndex(){
		for (int i = 0; i < lyricList.size(); i++) {
			long startPoint = lyricList.get(i).getStartPoint();
			if(i==(lyricList.size()-1)){
				//如果是最后一行
				if(currentAudioPosition>startPoint){
					lightLyricIndex = i;
				}
			}else {
				//不是最后一行
				long nextStartPoint = lyricList.get(i+1).getStartPoint();
				if(currentAudioPosition>=startPoint && currentAudioPosition<nextStartPoint){
					lightLyricIndex = i;
				}
			}
		}
	}
	
	public void setLyricList(ArrayList<Lyric> lyricList){
		this.lyricList = lyricList;
	}
	
	/**
	 * 获取文本的高
	 * @param text
	 * @return
	 */
	private int getTextHeight(String text){
		Rect bounds = new Rect();
		mPaint.getTextBounds(text, 0, text.length(), bounds);
		return bounds.height();
	}
	
	/**
	 * 绘制水平居中的文本
	 */
	private void drawCenterHorizontalText(Canvas canvas,String text,float y,boolean isLight){
		mPaint.setColor(isLight?COLOR_LIGHT:COLOR_DEFAULT);
		mPaint.setTextSize(isLight?SIZE_LIGHT:SIZE_DEFAULT);
		float x = viewWidth/2 - mPaint.measureText(text)/2;
		
		canvas.drawText(text, x, y, mPaint);
	}

}
