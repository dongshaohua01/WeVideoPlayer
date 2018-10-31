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
import com.dsh.wevideoplayer.activtiy.VideoPlayerActivity;
import com.dsh.wevideoplayer.adapter.VideoListAdpter;
import com.dsh.wevideoplayer.bean.VideoItem;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/10/29.
 */

public class VideoListFragment extends BaseFragment {

    private ListView listView;

    private VideoListAdpter adpter;
    private SimpleQueryHandler queryHandler;
    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.framgent_video_list,container,false);
        listView = view.findViewById(R.id.listview);
        return view;
    }

    @Override
    protected void initListener() {
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) adpter.getItem(position);
                ArrayList<VideoItem> videoList = cursorToList(cursor);

                Bundle bundle = new Bundle();
                bundle.putInt("currentPosition",position);
                bundle.putSerializable("videoList",videoList);
                enterActivity(VideoPlayerActivity.class,bundle);
            }
        });
    }

    @Override
    protected void initDate() {
       adpter = new VideoListAdpter(getActivity(),null);
       listView.setAdapter(adpter);
       queryHandler = new SimpleQueryHandler(getActivity().getContentResolver());
       String[] projection = {MediaStore.Video.Media._ID, MediaStore.Video.Media.SIZE, MediaStore.Video.Media.DURATION, MediaStore.Video.Media.TITLE, MediaStore.Video.Media.DATA};
       queryHandler.startQuery(0,adpter, MediaStore.Video.Media.EXTERNAL_CONTENT_URI,projection, null, null, null);
    }

    @Override
    protected void processClick(View view) {}

    /**
     * 将cursor中的数据解析出来并放入集合
     * @param cursor
     * @return
     */
    private ArrayList<VideoItem> cursorToList(Cursor cursor){
        cursor.moveToPosition(-1);//将cursor移动到最初位置,否则获取到的数据很可能不全

        ArrayList<VideoItem> list = new ArrayList<VideoItem>();

        //遍历cursor的所有结果集,然后将每一条结果集转成VideoItem对象
        while (cursor.moveToNext()){
            list.add(VideoItem.fromCursor(cursor));
        }
        return list;
    }
}
