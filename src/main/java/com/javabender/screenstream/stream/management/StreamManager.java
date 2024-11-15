package com.javabender.screenstream.stream.management;

import java.awt.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.bytedeco.javacv.Frame;

public class StreamManager {

    private final VideoStream videoStream;
    private final ScreenCapture screenCapture;

    private final StreamThreadPool threadPool;
    private final AtomicBoolean hasStopped = new AtomicBoolean(false);

    public StreamManager(String ipAddress, int frameRate) throws UnknownHostException {
        if (!isIPAddressValid(ipAddress)) {
            throw new IllegalArgumentException("Invalid IP address: " + ipAddress);
        }
        if (!isIPAddressAvailable(ipAddress)) {
            throw new UnknownHostException("Server is not available: " + ipAddress);
        }
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int physicalWidth = gd.getDisplayMode().getWidth();
        int physicalHeight = gd.getDisplayMode().getHeight();
        System.out.println("Physical screen resolution: " + physicalWidth + "x" + physicalHeight);
        threadPool = new StreamThreadPool(hasStopped);
        BlockingQueue<Frame> frameBuffer = new LinkedBlockingQueue<>(100);
        screenCapture = new ScreenCapture(frameBuffer, frameRate, physicalHeight, physicalWidth, hasStopped);
        videoStream = new VideoStream(String.format("rtmp://%s:1935/live/mystream", ipAddress), frameBuffer, frameRate, physicalHeight, physicalWidth, hasStopped);
    }

    private boolean isIPAddressValid(String ipAddress) {
        String ipPattern = "^(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\\."
                + "(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\\."
                + "(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])\\."
                + "(25[0-5]|2[0-4][0-9]|1[0-9]{2}|[1-9]?[0-9])$";
        return ipAddress != null && !ipAddress.isEmpty() && ipAddress.matches(ipPattern);
    }

    private boolean isIPAddressAvailable(String ipAddress) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ipAddress, 1935), 2000); // 2000 ms timeout
            return true;
        } catch (IOException e) {
            System.err.println("Server connection failed for IP address: " + ipAddress);
            return false;
        }
    }

    public void startStream() {
        try {
            threadPool.submit(screenCapture);
            threadPool.submit(videoStream);
            System.out.println("Streaming started.");
        } catch (RejectedExecutionException e) {
            System.err.println("Error starting stream tasks: " + e.getMessage());
            stopStream(); // Ensure a clean shutdown on error
        }
    }

    public void stopStream() {
        hasStopped.set(true);
        threadPool.shutdown();
    }
}

