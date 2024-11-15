package com.javabender.screenstream.stream.management;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class VideoStream implements Runnable {
    private final BlockingQueue<Frame> frameBuffer;
    private final FFmpegFrameRecorder recorder;
    private final AtomicBoolean hasStopped;

    VideoStream(String inetAddress, BlockingQueue<Frame> frameBuffer, int frameRate, int screenHeight, int screenWidth, AtomicBoolean hasStopped) {
        this.frameBuffer = frameBuffer;
        FFmpegLogCallback.set(); // Регистрируем FFmpeg лог один раз

        recorder = new FFmpegFrameRecorder(inetAddress, screenWidth, screenHeight);
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
        this.hasStopped  = hasStopped;


        try {
            recorder.start();
        } catch (FFmpegFrameRecorder.Exception e) {
           throw new RuntimeException(e);
        }
    }

    public void run() {
        FFmpegLogCallback.set();
        try {
            while(!hasStopped.get()) {
                Frame frame = frameBuffer.poll(200, TimeUnit.MILLISECONDS);
                if (frame != null) {
                    System.out.println("[VideoStream]: Pulled frame ID " + frame.opaque);
                    recorder.record(frame);
                } else {
                    System.out.println("[VideoStream]: Frame is null!");
                }
            }
        } catch (FFmpegFrameRecorder.Exception | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                recorder.stop();
            } catch (FFmpegFrameRecorder.Exception e) {
                System.err.println("Error stopping recorder: " + e.getMessage());
            }
        }
    }
}







