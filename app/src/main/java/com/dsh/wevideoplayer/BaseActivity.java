package com.dsh.wevideoplayer;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import com.dsh.wevideoplayer.util.StatusBarUtil;

/**
 * Created by Administrator on 2018/10/29.
 */

public abstract class BaseActivity extends Activity implements View.OnClickListener {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initLinstener();
        initDate();
        initSystemBar();
    }
    /**
     * 设置状态栏颜色
     */
    protected void initSystemBar() {
        if (setTranslucentStatusBar()) {
            // 设置状态栏全透明
            StatusBarUtil.transparencyBar(this);
            return;
        }
        StatusBarUtil.setStatusBarColor(this, setStatusBarColor(R.color.list_selector_pressed));
        if (isStatusBarLightMode()) {
            StatusBarUtil.StatusBarLightMode(this);
        }
    }
    public abstract void initView();
    public abstract void initDate();
    public abstract void initLinstener();
    public abstract void processClick(View view);

    @Override
    public void onClick(View v) {
        processClick(v);
    }
    /**
     * 子类可以重写决定是否使用透明状态栏
     */
    protected boolean setTranslucentStatusBar() {
        return false;
    }

    /**
     * 子类可以重写改变状态栏颜色
     */
    protected int setStatusBarColor(int mClolor) {
//    return R.color.colorPrimary;
        return mClolor;
    }
    /**
     * 子类可以重新决定使用黑色字体
     * @return
     */
    protected boolean isStatusBarLightMode() {
        return false;
    }
}
