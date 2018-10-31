package com.dsh.wevideoplayer.fragment;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dsh.wevideoplayer.BaseFragment;
import com.dsh.wevideoplayer.R;
import com.dsh.wevideoplayer.SimpleQueryHandler;
import com.dsh.wevideoplayer.activtiy.AudioPlayerActivity;
import com.dsh.wevideoplayer.adapter.AudioListAdapter;
import com.dsh.wevideoplayer.bean.AudioItem;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/10/29.
 */

public class AudioListFragment extends BaseFragment {
    private ListView listView;

    private AudioListAdapter adapter;
    private SimpleQueryHandler queryHandler;
    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     View view= inflater.inflate(R.layout.fragment_audio_list,container,false);
          listView = (ListView) view.findViewById(R.id.listview);
        return view;
    }

    @Override
    protected void initListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                 Cursor cursor = (Cursor) adapter.getItem(position);
                 Bundle bundle = new Bundle();
                 bundle.putInt("currentPosition", position);
                 bundle.putSerializable("audioList", cursorToList(cursor));
                 enterActivity(AudioPlayerActivity.class,bundle);
            }
        });
    }

    @Override
    protected void initDate() {
          adapter = new AudioListAdapter(getActivity(),null);
          listView.setAdapter(adapter);

          queryHandler = new SimpleQueryHandler(getActivity().getContentResolver());
          String[] projection = {MediaStore.Audio.Media._ID, MediaStore.Audio.Media.DISPLAY_NAME, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION};
          queryHandler.startQuery(0,adapter, MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,projection,null,null,null);

    }

    @Override
    protected void processClick(View view) {}

    private ArrayList<AudioItem> cursorToList(Cursor cursor){
        ArrayList<AudioItem> list = new ArrayList<>();
        cursor.moveToPosition(-1);
        while (cursor.moveToNext()){
            list.add(AudioItem.fromCursor(cursor));
        }
        return  list;
    }
}
