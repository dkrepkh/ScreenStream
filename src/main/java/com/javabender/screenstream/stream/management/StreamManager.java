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

    private final BlockingQueue<Frame> frameBuffer;
    private final ExecutorService executorService;
    private final AtomicBoolean hasStopped = new AtomicBoolean(false);

    public StreamManager(String ipAddress, int frameRate) throws UnknownHostException, AWTException {
        if (!isIPAddressValid(ipAddress)) {
            throw new IllegalArgumentException("Invalid IP address: " + ipAddress);
        }
        if (!isIPAddressAvailable(ipAddress)) {
            throw new UnknownHostException("Server is not available: " + ipAddress);
        }
        executorService = Executors.newFixedThreadPool(2);
        frameBuffer = new LinkedBlockingQueue<>(100);
        screenCapture = new ScreenCapture(frameBuffer, frameRate);
        videoStream = new VideoStream(String.format("rtmp://%s:1935/live/mystream", ipAddress), frameBuffer, frameRate);
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
            executorService.submit(screenCapture);
            executorService.submit(videoStream);
            System.out.println("Streaming started.");
        } catch (RejectedExecutionException e) {
            System.err.println("Error starting stream tasks: " + e.getMessage());
            stopStream(); // Ensure a clean shutdown on error
        }
    }

    public void stopStream() {
        hasStopped.set(true);  // Signal all threads to stop
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                System.err.println("Forcing shutdown of stream tasks...");
                executorService.shutdownNow(); // Force stop if tasks don't complete in time
            }
            System.out.println("Streaming stopped.");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();  // Restore interrupted status
            System.err.println("Stream stop interrupted: " + e.getMessage());
            executorService.shutdownNow();
        }
    }
}

