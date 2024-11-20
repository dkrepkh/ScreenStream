package com.javabender.screenstream.stream.management;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.Frame;

import java.util.Optional;

public class ScreenCapture {
    private final FFmpegFrameGrabber grabber;

    public ScreenCapture(int width, int height, int frameRate) {
        grabber = createFrameGrabber();
        grabber.setImageWidth(width);
        grabber.setImageHeight(height);
        grabber.setFrameRate(frameRate);
        grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
        grabber.setPixelFormat(avutil.AV_PIX_FMT_BGR24);
        System.out.println("Grabber width: " + grabber.getImageWidth());
        System.out.println("Grabber height: " + grabber.getImageHeight());

        try {
            grabber.start();
            System.out.println("Grabber started successfully.");
            System.out.println("Grabber width: " + grabber.getImageWidth());
            System.out.println("Grabber height: " + grabber.getImageHeight());
        } catch (FFmpegFrameGrabber.Exception e) {
            System.err.println("Failed to start grabber: " + e.getMessage());
            throw new RuntimeException("Unable to start the grabber", e);
        }
    }

    public Optional<Frame> grabFrame() {
        try {
            Frame frame = grabber.grab();
            if (frame == null || frame.image == null) {
                System.err.println("Frame is null or empty!");
                return Optional.empty();
            }
            return Optional.of(frame);
        } catch (FFmpegFrameGrabber.Exception e) {
            System.err.println("Failed to grab a frame: " + e.getMessage());
            return Optional.empty();
        }
    }

    private FFmpegFrameGrabber createFrameGrabber() {
        String osName = System.getProperty("os.name").toLowerCase();
        FFmpegFrameGrabber frameGrabber;
        try {
            if (osName.contains("win")) {
                frameGrabber = new FFmpegFrameGrabber("desktop");
                frameGrabber.setFormat("gdigrab");
            } else if (osName.contains("mac")) {
                frameGrabber = new FFmpegFrameGrabber("1:none");
                frameGrabber.setFormat("avfoundation");
            } else if (osName.contains("linux")) {
                frameGrabber = new FFmpegFrameGrabber(":0.0+0,0");
                frameGrabber.setFormat("x11grab");
            } else {
                throw new RuntimeException("Unsupported OS: " + osName);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize grabber for OS: " + osName, e);
        }
        return frameGrabber;
    }

    public void stop() {
        try {
            grabber.stop();
            grabber.release();
            System.out.println("Grabber stopped and resources released.");
        } catch (FFmpegFrameGrabber.Exception e) {
            System.err.println("Failed to stop the grabber: " + e.getMessage());
        }
    }
}




