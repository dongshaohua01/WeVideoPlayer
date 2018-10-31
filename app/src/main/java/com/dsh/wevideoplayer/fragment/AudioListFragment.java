package com.dsh.wevideoplayer.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dsh.wevideoplayer.BaseFragment;
import com.dsh.wevideoplayer.R;

/**
 * Created by Administrator on 2018/10/29.
 */

public class AudioListFragment extends BaseFragment {
    @Override
    protected View initView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_audio_list,container,false);
    }

    @Override
    protected void initListener() {

    }

    @Override
    protected void initDate() {

    }

    @Override
    protected void processClick(View view) {

    }
}
