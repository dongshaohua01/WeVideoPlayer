package com.dsh.wevideoplayer.activtiy;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;

import com.dsh.wevideoplayer.R;
import com.dsh.wevideoplayer.util.StatusBarUtil;

public class SplashActivity extends AppCompatActivity {

    private boolean hasEnterMain = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        StatusBarUtil.fullScreen(this);
        delayEnterMainActivity(true);
    }

    private void delayEnterMainActivity(boolean isDelay){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
              if(!hasEnterMain){
                  hasEnterMain = true;
                  startActivity(new Intent(SplashActivity.this, MainActivity.class));
                  finish();
              }
            }
        },isDelay ? 2000:0);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                delayEnterMainActivity(false);
                break;
        }
        return super.onTouchEvent(event);
    }
}
