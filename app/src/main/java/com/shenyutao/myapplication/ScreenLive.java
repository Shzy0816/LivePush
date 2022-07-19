package com.shenyutao.myapplication;

import android.media.projection.MediaProjection;
import android.util.Log;
import android.widget.Toast;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author shenyutao
 */
public class ScreenLive extends Thread {
    public final String TAG = "ScreenLive";
    private MediaProjection mediaProjection;
    private boolean isLiving;
    private LinkedBlockingQueue<RTMPPackage> queue = new LinkedBlockingQueue<>();
    private String url;


    static {
        System.loadLibrary("native-lib");
    }

    public void startLive(String url, MediaProjection mediaProjection) {
        this.mediaProjection = mediaProjection;
        this.url = url;
        LiveTaskManager.getInstance().execute(this);
    }

    public void addPackage(RTMPPackage rtmpPackage) {
        if (!isLiving) {
            return;
        }
        queue.add(rtmpPackage);
    }


    @Override
    public void run() {
        if (!connect(url)) {
            Log.i(TAG, "run: ----------->推送失败");
            return;
        }

        VideoCodec videoCodec = new VideoCodec(this);
        videoCodec.startLive(mediaProjection);

        isLiving = true;
        while (isLiving) {
            RTMPPackage rtmpPackage = null;
            try {
                rtmpPackage = queue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.i(TAG, "取出数据");
            if (rtmpPackage.getBuffer() != null && rtmpPackage.getBuffer().length != 0) {
                sendData(rtmpPackage.getBuffer(), rtmpPackage.getBuffer().length, rtmpPackage.getTms(), rtmpPackage.getType());
                Log.i(TAG, "ScreenLive run: ----------->推送 " + rtmpPackage.getBuffer().length + "   type:" + rtmpPackage.getType());
            }
        }
    }

    /**
     * 连接Rtmp服务器
     *
     * @param url Rtmp服务器地址
     * @return
     */
    private native boolean connect(String url);

    /**
     * 发送数据
     *
     * @param data 数据比特流
     * @param len  比特流长度
     * @param tms  时间片长度
     * @param type 数据类型 音频 or 视频
     * @return
     */
    private native boolean sendData(byte[] data, int len, long tms, int type);
}
