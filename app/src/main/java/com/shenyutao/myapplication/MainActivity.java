package com.shenyutao.myapplication;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.shenyutao.myapplication.databinding.ActivityMainBinding;

/**
 * @author shenyutao
 */
public class MainActivity extends AppCompatActivity {
    private MediaProjectionManager mediaProjectionManager;
    private String url = "rtmp://sendtc3.douyu.com/live/10113106riwi5XsF?wsSecret=5fe922c85fd30f727cac4bfe6e3078bb&wsTime=611b96df&wsSeek=off&wm=0&tw=0&roirecognition=0&record=flv&origin=tct";
    private MediaProjection mediaProjection;

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        checkPermission();
        initListener();
    }

    public boolean checkPermission() {
        if (checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO
            }, 1);

        }
        return false;
    }


    //按钮点击事件
    public void startLivePush(){
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        //获取屏幕捕捉(Screen Capture，也叫截屏)的intent
        Intent captureIntent = mediaProjectionManager.createScreenCaptureIntent();
        startActivityForResult(captureIntent, 100);
    }

    public void initListener(){
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLivePush();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100 && resultCode == Activity.RESULT_OK){
            mediaProjection = mediaProjectionManager.getMediaProjection(resultCode,data);
            ScreenLive screenLive = new ScreenLive();
            screenLive.startLive(url,mediaProjection);
        }
    }

}