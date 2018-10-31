package com.dsh.wevideoplayer.adapter;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.dsh.wevideoplayer.R;
import com.dsh.wevideoplayer.bean.VideoItem;
import com.dsh.wevideoplayer.util.StringUtil;

/**
 * Created by Administrator on 2018/10/30.
 */

public class VideoListAdpter extends CursorAdapter {

    public VideoListAdpter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return View.inflate(context, R.layout.adapter_video_list,null);
    }

    ViewHolder holder;
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
       holder = getHolder(view);

        VideoItem videoItem = VideoItem.fromCursor(cursor);
        Log.i("videoItem--->",videoItem.getTitle()+"");
       holder.tv_title.setText(videoItem.getTitle());
       holder.tv_size.setText(android.text.format.Formatter.formatFileSize(context,videoItem.getSize()));
       holder.tv_duration.setText(StringUtil.formatVideoDuration(videoItem.getDuration()));
    }

    private ViewHolder getHolder(View view){
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        if(viewHolder == null){
             viewHolder = new ViewHolder(view);
             view.setTag(viewHolder);
        }
        return viewHolder;
    }

    class ViewHolder{
        TextView tv_title,tv_duration,tv_size;
        public ViewHolder(View view){
            tv_title =(TextView) view.findViewById(R.id.tv_title);
            tv_duration = (TextView) view.findViewById(R.id.tv_duration);
            tv_size = (TextView)  view.findViewById(R.id.tv_size);
        }
    }
}
