package com.dsh.wevideoplayer.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.dsh.wevideoplayer.R;
import com.dsh.wevideoplayer.bean.AudioItem;
import com.dsh.wevideoplayer.util.StringUtil;

/**
 * Created by Administrator on 2018/10/31.
 */

public class AudioListAdapter extends CursorAdapter {
    public AudioListAdapter(Context context, Cursor c) {
        super(context, c);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return View.inflate(context, R.layout.adapter_audio_list,null);
    }

    ViewHolder viewHolder;
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
            viewHolder = getHholder(view);

        AudioItem audioItem = AudioItem.fromCursor(cursor);

        viewHolder.tv_artist.setText(audioItem.getArtist());
        viewHolder.tv_name.setText(StringUtil.formatAudioName(audioItem.getTitle()));
    }

    private ViewHolder getHholder(View view)
    {
         viewHolder = (ViewHolder) view.getTag();
        if(viewHolder == null)
        {
            viewHolder = new ViewHolder(view);
            view.setTag(view);
        }
        return viewHolder;
    }

    class ViewHolder{
        TextView tv_name,tv_artist;
        public ViewHolder(View view){
             tv_name = (TextView) view.findViewById(R.id.tv_name);
             tv_artist = (TextView) view.findViewById(R.id.tv_artist);
        }
    }
}
