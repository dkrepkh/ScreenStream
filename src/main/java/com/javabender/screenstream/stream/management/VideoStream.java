package com.javabender.screenstream.stream.management;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;

public class VideoStream {
    private final FFmpegFrameRecorder recorder;

    public VideoStream(String inetAddress, int width, int height, int frameRate) {
        recorder = new FFmpegFrameRecorder(inetAddress, width, height);
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        recorder.setFormat("flv");
        recorder.setFrameRate(frameRate); // Используем frameRate, переданный в конструктор
        recorder.setVideoOption("preset", "superfast"); // Изменено на superfast для сбалансированности скорости и качества
        recorder.setVideoOption("tune", "zerolatency");
        recorder.setGopSize(30);
        recorder.setVideoBitrate(5000 * 1000); // Увеличен битрейт для улучшения качества
        recorder.setOption("flush_packets", "1");
        recorder.setOption("fflags", "nobuffer");
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);


        try {
            recorder.start();
            System.out.println("Recorder started successfully.");
        } catch (FFmpegFrameRecorder.Exception e) {
            System.err.println("Failed to start recorder: " + e.getMessage());
            throw new RuntimeException("Unable to start the recorder", e);
        }
    }

    public void sendFrame(Frame frame) {
        try {
            recorder.record(frame);
        } catch (FFmpegFrameRecorder.Exception e) {
            System.err.println("Failed to send frame: " + e.getMessage());
        }
    }

    public void stop() {
        try {
            recorder.stop();
            recorder.release();
            System.out.println("Recorder stopped and resources released.");
        } catch (FFmpegFrameRecorder.Exception e) {
            System.err.println("Failed to stop the recorder: " + e.getMessage());
        }
    }
}








