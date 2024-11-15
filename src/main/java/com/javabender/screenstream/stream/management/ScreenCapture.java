package com.javabender.screenstream.stream.management;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;

import java.awt.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ScreenCapture implements Runnable {
    private final BlockingQueue<Frame> imageQueue;
    private final FFmpegFrameGrabber grabber;
    private final AtomicBoolean hasStopped;

    ScreenCapture(BlockingQueue<Frame> imageQueue, int frameRate, int screenHeight, int screenWidth, AtomicBoolean hasStopped) throws AWTException {
        this.imageQueue = imageQueue;
        grabber = getFfmpegFrameGrabber();
        grabber.setImageWidth(screenWidth);
        grabber.setImageHeight(screenHeight);
        grabber.setFrameRate(frameRate);
        grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        grabber.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);
        System.out.println("Grabber width: " + grabber.getImageWidth());
        System.out.println("Grabber height: " + grabber.getImageHeight());
        this.hasStopped = hasStopped;
    }

    @Override
    public void run() {
        FFmpegLogCallback.set();
        try {
            grabber.start();
            while (!hasStopped.get()) {
                Frame frame = grabber.grab();
                if (frame != null) {
                    Frame frameCopy = frame.clone(); // Создаём независимую копию кадра
                    System.out.println("[ScreenCapture]: Captured frame with ID: " + frameCopy);
                    imageQueue.put(frameCopy);
                } else {
                    System.out.println("[ScreenCapture]: Grabbed frame is null!");
                }

            }
        } catch (FFmpegFrameGrabber.Exception | InterruptedException e) {
           throw new RuntimeException(e);
        }
    }
    private FFmpegFrameGrabber getFfmpegFrameGrabber() {
        FFmpegFrameGrabber fFmpegFrameGrabber;
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            fFmpegFrameGrabber = new FFmpegFrameGrabber("desktop");
            fFmpegFrameGrabber.setFormat("gdigrab");
            System.out.println("W");
        } else if (osName.contains("mac")) {
            fFmpegFrameGrabber = new FFmpegFrameGrabber("1:none");
            fFmpegFrameGrabber.setFormat("avfoundation");
            System.out.println("M");
        } else if (osName.contains("linux")) {
            fFmpegFrameGrabber = new FFmpegFrameGrabber(":0.0+0,0");
            fFmpegFrameGrabber.setFormat("x11grab");
            System.out.println("L");
        } else {
            throw new RuntimeException("Unsupported OS: " + osName);
        }
        System.out.println(osName);
        return fFmpegFrameGrabber;
    }
}


