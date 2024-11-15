package com.javabender.screenstream;

import com.javabender.screenstream.stream.management.ScreenCapture;
import com.javabender.screenstream.stream.management.StreamManager;
import com.javabender.screenstream.stream.management.VideoStream;

import org.bytedeco.javacv.Frame;

import java.awt.*;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Main {
    private static final int FRAME_RATE = 30;
    public static void main(String[] args) throws AWTException, IOException {
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter the ip address: ");
        String ipAddress = scan.nextLine();
        scan.close();
        if(ipAddress.isEmpty()) {
            ipAddress = "127.0.0.1";
        }
        BlockingQueue<Frame> frames = new LinkedBlockingQueue<Frame>();
        StreamManager streamManager = new StreamManager(ipAddress, FRAME_RATE);
        streamManager.startStream();
    }
}
