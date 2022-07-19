package com.shenyutao.myapplication;

import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;

/**
 * @author shenyutao
 * 视频编码类，通过该类对视频进行编码
 */
public class VideoCodec extends Thread {
    private static final String TAG = "VideoCodec";
    private final ScreenLive screenLive;
    private MediaProjection mediaProjection;
    private MediaCodec mediaCodec;
    private boolean isLiving;
    private VirtualDisplay virtualDisplay;
    private long timeStamp;
    private long startTime = 0 ;

    public VideoCodec(ScreenLive screenLive) {
        this.screenLive = screenLive;
    }

    public void startLive(MediaProjection mediaProjection) {
        this.mediaProjection = mediaProjection;

        MediaFormat format = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, 720, 1280);
        //视频采集编码颜色格式(不懂，待了解)，这里采用YUV格式
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
        //视频码率，帧率，关键帧间隔
        format.setInteger(MediaFormat.KEY_BIT_RATE, 8000_000);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 16);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 0);

        try {
            //MediaFormat.MIMETYPE_VIDEO_AVC指的是视频编码格式，与视频格式区分
            mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            //设置MediaCodec，传入上面定义好的MediaFormat实例化对象format
            mediaCodec.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            //获取画布（surface）
            Surface surface = mediaCodec.createInputSurface();

            /*
            将画布surface传入mediaProjection的createVirtualDisplay方法中，
            mediaProjection会不断将捕获到的(屏幕或者摄像头数据)帧数据传入到surface中，
            该surface是mediaCodec创建的inputSurface(输入画布),
            所以mediaCodec一检测到该surface中有数据传入后会自动将其取出(出队列)进行编码后送入输出队列中,
            此时我们再用while循环不断去输出队列中获取数据即可

            参数传递：
            name随便传主要做标识用，
            width 捕获宽度
            height 捕获长度
            dpi传1
            flag随便传
            surface传入MediaCodec创建的画布，
            callback 看需求，这里传入null
            handle 传null


            调用完该方法后，最后记得调用mediaProjection.stop()
            */
            virtualDisplay = mediaProjection.createVirtualDisplay("随意"
                    , 720, 1280, 1, DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC, surface, null, null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        //启动线程
        LiveTaskManager.getInstance().execute(this);
    }

    @Override
    public void run() {
        isLiving = true;
        //启动MediaCode
        mediaCodec.start();

        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        while (isLiving) {
            //每隔1000毫秒手动触发输出关键帧
            if (System.currentTimeMillis() - timeStamp >= 1000) {
                Bundle bundle = new Bundle();
                //立刻刷新 让下一帧是关键帧
                bundle.putInt(MediaCodec.PARAMETER_KEY_REQUEST_SYNC_FRAME, 0);
                mediaCodec.setParameters(bundle);
                timeStamp = System.currentTimeMillis();
            }
            // 经过mediaCodec.dequeueOutputBuffer之后，bufferInfo就会被赋上相对应的值
            int index = mediaCodec.dequeueOutputBuffer(bufferInfo, 100000);


            if (index >= 0) {
                ByteBuffer byteBuffer = mediaCodec.getOutputBuffer(index);
                MediaFormat mediaFormat = mediaCodec.getOutputFormat(index);
                Log.i(TAG, "mediaFormat: " + mediaFormat.toString());
                byte[] bytes = new byte[bufferInfo.size];
                //面向过程的编程，经过该函数处理后bytes数组就会填充数据
                byteBuffer.get(bytes);
                if (startTime == 0) {
                    // 微妙转为毫秒
                    startTime = bufferInfo.presentationTimeUs / 1000;
                }
                RTMPPackage rtmpPackage = new RTMPPackage(bytes, (bufferInfo.presentationTimeUs / 1000) - startTime);
                rtmpPackage.setType(RTMPPackage.RTMP_PACKET_TYPE_VIDEO);
                screenLive.addPackage(rtmpPackage);
                //释放内存
                mediaCodec.releaseOutputBuffer(index, false);
            }
        }
        isLiving = false;
        startTime = 0;
        mediaCodec.stop();
        mediaCodec.release();
        mediaCodec = null;
        virtualDisplay.release();
        virtualDisplay = null;
        mediaProjection.stop();
        mediaProjection = null;
    }
}
