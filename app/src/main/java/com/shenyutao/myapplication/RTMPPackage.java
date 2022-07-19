package com.shenyutao.myapplication;

public class RTMPPackage {

    private byte[] buffer;
    /**
     * 时间片 该封包中含有的帧所构成的视频的时间长
     */
    private long tms;
    /**
     * 包类型：视频包 或 音频包
     */
    private int type;
    public static final int RTMP_PACKET_TYPE_AUDIO_DATA = 2;
    public static final int RTMP_PACKET_TYPE_AUDIO_HEAD = 1;
    public static final int RTMP_PACKET_TYPE_VIDEO = 0;

    public RTMPPackage(byte[] buffer, long tms) {
        this.buffer = buffer;
        this.tms = tms;
    }

    public RTMPPackage() {
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public byte[] getBuffer() {
        return buffer;
    }

    public void setBuffer(byte[] buffer) {
        this.buffer = buffer;
    }

    public long getTms() {
        return tms;
    }

    public void setTms(long tms) {
        this.tms = tms;
    }
}