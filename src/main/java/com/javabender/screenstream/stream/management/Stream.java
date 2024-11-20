package com.javabender.screenstream.stream.management;

import java.awt.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;

import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;

public class Stream {

    private final ScreenCapture screenCapture;
    private final VideoStream videoStream;
    private boolean stop = false;

    private Stream(String inetAddress, int frameRate) {
        FFmpegLogCallback.set();
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int width = gd.getDisplayMode().getWidth();
        int height = gd.getDisplayMode().getHeight();

        screenCapture = new ScreenCapture(width, height, frameRate);
        videoStream = new VideoStream(inetAddress, width, height, frameRate);
    }

    public static Stream getInstance(String ipAddress, int frameRate) throws UnknownHostException {
        if (ipAddress == null) {
            throw new NullPointerException("IP address is null");
        }
        if (!isIPAddressValid(ipAddress)) {
            throw new IllegalArgumentException("Invalid IP address");
        }
        if (!isIPAddressAvailable(ipAddress)) {
            throw new UnknownHostException("Server is not available: " + ipAddress);
        }
        return new Stream(String.format("rtmp://%s:1935/live/mystream", ipAddress), frameRate);
    }

    private static boolean isIPAddressValid(String ipAddress) {
        String ipPattern = "^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\\."
                + "(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\\."
                + "(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\\."
                + "(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$";
        return ipAddress != null && ipAddress.matches(ipPattern);
    }

    private static boolean isIPAddressAvailable(String ipAddress) {
        int timeout = 5000;
        try {
            InetAddress inet = InetAddress.getByName(ipAddress);
            System.out.println("Pinging " + ipAddress);
            return inet.isReachable(timeout);
        } catch (Exception e) {
            System.err.println("Error checking IP availability: " + e.getMessage());
            return false;
        }
    }

    public void start() {
        while (!stop) {
            Optional<Frame> optionalFrame = screenCapture.grabFrame();
            if (optionalFrame.isPresent()) {
                videoStream.sendFrame(optionalFrame.get());
            } else {
                System.err.println("No frame captured. Exiting stream.");
                stop();
            }
        }
    }

    public void stop() {
        stop = true;
        screenCapture.stop();
        videoStream.stop();
        System.out.println("Stream stopped.");
    }
}
