package com.dsh.wevideoplayer.activtiy;

import android.Manifest;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import com.dsh.wevideoplayer.R;
import com.dsh.wevideoplayer.adapter.MainPagerAdapter;
import com.dsh.wevideoplayer.fragment.AudioListFragment;
import com.dsh.wevideoplayer.fragment.VideoListFragment;
import com.dsh.wevideoplayer.util.StatusBarUtil;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.ArrayList;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends FragmentActivity implements View.OnClickListener {

    private static final int RC_WRITE_EXTERNAL_STORAGE = 0x114;

    private TextView tab_video, tab_audio;
    private View indicate_line;
    private ViewPager viewPager;

    private MainPagerAdapter adapter;
    private ArrayList<Fragment> fragments = new ArrayList<>();
    private int indicateLineWidth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initLinstener();
        initDate();
        //检查权限
        if (!EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            EasyPermissions
                    .requestPermissions(this, getString(R.string.read),
                            RC_WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }


    public void initView() {
        setContentView(R.layout.activity_main);
        //设置状态栏颜色
        StatusBarUtil.setStatusBarColor(this,R.color.indicate_line);
        tab_video = (TextView) findViewById(R.id.tab_video);
        tab_audio = (TextView) findViewById(R.id.tab_audio);
        indicate_line = findViewById(R.id.indicate_line);
        viewPager = (ViewPager) findViewById(R.id.viewPager);
    }

    public void initDate() {
        fragments.add(new VideoListFragment());
        fragments.add(new AudioListFragment());

        caculateIndicateLineWidth();

        adapter = new MainPagerAdapter(getSupportFragmentManager(),fragments);
        viewPager.setAdapter(adapter);
    }


    public void initLinstener() {
        tab_video.setOnClickListener(this);
        tab_audio.setOnClickListener(this);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                int targetPosition = position * indicateLineWidth + positionOffsetPixels/fragments.size();
                ViewPropertyAnimator.animate(indicate_line).translationX(targetPosition).setDuration(0);
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    /**
     * 计算指示线的宽
     */
    private void caculateIndicateLineWidth(){
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        indicateLineWidth = screenWidth/fragments.size();
        indicate_line.getLayoutParams().width = indicateLineWidth;
        indicate_line.requestLayout();
    }

    /**
     * 高亮并缩放标题
     */
    private void lightAndScaleTitle(){
        int currentPage = viewPager.getCurrentItem();

        tab_video.setTextColor(currentPage == 0? getResources().getColor(
                R.color.indicate_line) : getResources().getColor(R.color.gray_white));
        tab_audio.setTextColor(currentPage == 1? getResources().getColor(
              R.color.indicate_line) : getResources().getColor(R.color.gray_white));

         ViewPropertyAnimator.animate(tab_video)
                 .scaleX(currentPage == 0 ? 1.2f : 1.0f).setDuration(200);
         ViewPropertyAnimator.animate(tab_video)
                 .scaleX(currentPage == 0 ? 1.2f : 1.0f).setDuration(200);
         ViewPropertyAnimator.animate(tab_audio)
                 .scaleX(currentPage == 1 ? 1.2f : 1.0f).setDuration(200);
         ViewPropertyAnimator.animate(tab_audio)
                 .scaleX(currentPage == 1 ? 1.2f : 1.0f).setDuration(200);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.tab_video:
                viewPager.setCurrentItem(0);
                break;
            case R.id.tab_audio:
                viewPager.setCurrentItem(1);
                break;
        }
    }
}
