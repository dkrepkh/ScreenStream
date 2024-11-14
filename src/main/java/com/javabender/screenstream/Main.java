package com.javabender.screenstream;

import com.javabender.screenstream.stream.management.ScreenCapture;
import com.javabender.screenstream.stream.management.StreamManager;
import com.javabender.screenstream.stream.management.VideoStream;

import java.awt.*;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    private static final int FRAME_RATE = 10;
    public static void main(String[] args) throws AWTException, IOException {
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter the ip address: ");
        String ipAddress = scan.nextLine();
        scan.close();
        if(ipAddress.isEmpty()) {
            ipAddress = "127.0.0.1";
        }
        StreamManager streamManager = new StreamManager(ipAddress, FRAME_RATE);
        streamManager.startStream();
        /*try {
            Thread.sleep(10000);
            System.out.println("стрим останавливается");
            streamManager.stopStream();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }
}
