package com.javabender.screenstream.stream.management;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;

public class ScreenCapture implements Runnable {
    private final int FRAME_RATE;
    private final BlockingQueue<Frame> imageQueue;
    private final Robot robot;
    private final Rectangle screenRect;
    private final Java2DFrameConverter converter; // Инициализация конвертера

    ScreenCapture(BlockingQueue<Frame> imageQueue, int frameRate) throws AWTException {
        this.screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
        this.imageQueue = imageQueue;
        this.FRAME_RATE = frameRate;
        this.robot = new Robot();
        this.converter = new Java2DFrameConverter(); // Инициализация конвертера
    }

    @Override
    public void run() {
        while (true) {
            BufferedImage image = robot.createScreenCapture(screenRect);

            // Конвертируем и добавляем кадр в очередь, ожидая, если очередь заполнена
            Frame convertedImage = converter.convert(image);
            try {
                imageQueue.put(convertedImage); // Ожидаем освобождения места
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            try {
                Thread.sleep(1000 / FRAME_RATE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }
}


