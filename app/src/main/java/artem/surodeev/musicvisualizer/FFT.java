package artem.surodeev.musicvisualizer;

import android.graphics.Color;
import android.graphics.Paint;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.io.File;
import java.io.IOException;
import java.util.Date;

class FFT {
    private static boolean songIsPlaying;
    private static Complex[] transform;
    private static Paint paint;
    private static WavFile wavFile;
    static void setSongIsPlaying(boolean value){
        songIsPlaying = value;
    }
    static void startFFT(final File file){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    wavFile = WavFile.openWavFile(file);

                    paint = new Paint();
                    paint.setColor(Color.argb(255,228,50,50));

                    changeColor();
                    startDrawing();

                    Song.startMusic();
                    Song.setStatus(true);

                    preparationDataOfFFT();
                }catch (IOException ignored) {}
                 catch (WavFileException ignored) {}
            }
        }).start();
    }

    private static void startDrawing(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(songIsPlaying){
                    Song.draw(calcFreq(), paint);
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private static void changeColor(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                int red = 228;
                int green = 50;
                int blue = 50;
                int dr = 1,db = 0;
                int stage = 0;
                while(songIsPlaying){
                    int color = Color.argb(255, red, green, blue);
                    paint.setColor(color);
                    if(red == 228 && blue == 50 && stage == 3) {
                        stage = 0;
                    }else if(red == 228 && blue == 228 && stage == 0){
                        stage = 1;
                    }else if(red == 50 && blue == 228 && stage == 1){
                        stage = 2;
                    }else if(red == 228 && blue == 228 && stage == 2){
                        stage = 3;
                    }
                    if(stage == 0){
                        dr = 0;
                        db = 1;
                    }else if(stage == 1){
                        dr = -1;
                        db = 0;
                    }else if(stage == 2){
                        dr = 1;
                        db = 0;
                    }else if(stage == 3){
                        dr = 0;
                        db = -1;
                    }
                    red += dr;
                    blue += db;
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }).start();
    }

    private static void preparationDataOfFFT(){
        int framesRead = 0;
        double[] buffer = new double[8820];
        try {
            do {
                if (Song.getStatus()) {
                    long time = new Date().getTime();
                    framesRead = wavFile.readFrames(buffer, 4410);
                    double[] y = new double[16384];
                    for (int i = 0; i < y.length; i++) {
                        y[i] = 0;
                    }
                    int counter = 0;
                    for (int i = 0; i < buffer.length; i += 2) {
                        y[counter++] = buffer[i];
                    }
                    FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
                    transform = fft.transform(y, TransformType.FORWARD);
                    time = new Date().getTime() - time;
                    if (time <= 100) {
                        Thread.sleep(100 - time);
                    }
                } else {
                    Thread.sleep(1);
                }
            } while (framesRead != 0 && songIsPlaying);
        }catch (WavFileException ignored){}
         catch (InterruptedException ignored){}
         catch (IOException ignored){}
    }

    private static int[] calcFreq(){
        final int[] freqs = new int[64];
        try{
            new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < freqs.length; i++) {
                        freqs[i] = 0;
                    }
                    int delta = 50;
                    int startPos=0;
                    int endPos = 50;
                    for (int i = 0; i < freqs.length; i++) {
                        for (int j = startPos; j < endPos; j++) {
                            if(transform == null){
                                freqs[i] = 0;
                                continue;
                            }
                            freqs[i] += (transform[j].getReal());
                        }
                        freqs[i] /= delta;
                        startPos+=delta;
                        endPos+=delta;
                    }
                    transform = null;
                }
            }).start();
        }catch (NullPointerException ignored){}
        return freqs;
    }
}
