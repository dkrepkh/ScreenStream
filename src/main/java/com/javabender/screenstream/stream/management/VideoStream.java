package com.javabender.screenstream.stream.management;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FFmpegLogCallback;
import org.bytedeco.javacv.Frame;

import java.awt.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class VideoStream implements Runnable {
    private BlockingQueue<Frame> images;
    private FFmpegFrameRecorder recorder;
    private AtomicBoolean stop = new AtomicBoolean(false); // Используем AtomicBoolean для остановки
    private ExecutorService executorService = Executors.newSingleThreadExecutor(); // Пул потоков для более надежной обработки

    VideoStream(String inetAddress, BlockingQueue<Frame> images, int frameRate) {
        this.images = images;
        FFmpegLogCallback.set();

        int width = Toolkit.getDefaultToolkit().getScreenSize().width; // Фиксированное разрешение
        int height = Toolkit.getDefaultToolkit().getScreenSize().height;
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
        } catch (FFmpegFrameRecorder.Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error starting FFmpegFrameRecorder", e);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Frame currentFrame = images.poll(200, TimeUnit.MILLISECONDS);  // Таймаут для извлечения из очереди
                if (currentFrame == null) {
                    System.out.println("Buffer empty");
                    continue;  // Если нет изображения, пропустим итерацию
                }
                System.out.println("frame successfully pulled out");
                recorder.record(currentFrame);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (FFmpegFrameRecorder.Exception e) {
            e.printStackTrace();
        } finally {
            try {
                recorder.release();
                recorder.stop();
            } catch (FFmpegFrameRecorder.Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}




