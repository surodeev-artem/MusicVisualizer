package artem.surodeev.musicvisualizer;

import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

import java.io.File;
import java.io.IOException;

public class FFT {
    private static boolean songIsPlaying;
    private static Context context;

    static Animation enlargeAnim;
    static Animation shrinkAnim;

    static ImageView imageView;

    public static void setSongIsPlaying(boolean value){
        songIsPlaying = value;
    }
    public static void startFFT(final File file){
        new Thread(new Runnable() {
            @Override
            public void run() {
                WavFile wavFile;
                try {
                    wavFile = WavFile.openWavFile(file);
                    int framesRead = 0;
                    double[] buffer = new double[8192];
                    do {
                        if(Song.getStatus()){
                            framesRead = wavFile.readFrames(buffer, 4096);
                            double[] y = new double[4096];
                            for (int i = 0; i < y.length; i++) {
                                y[i] = 0;
                            }
                            int counter = 0;
                            for (int i = 0; i < buffer.length; i += 2) {
                                y[counter++] = buffer[i];
                            }
                            FastFourierTransformer fft = new FastFourierTransformer(DftNormalization.STANDARD);
                            Complex[] transform = fft.transform(y, TransformType.FORWARD);
                            boolean alreadyWas = false;
                            for (int i = 0; i < transform.length; i++) {
                                double res = transform[i].getReal();
                                if (!alreadyWas) {
                                    if (res > 150) {
                                        playAnim();
                                        alreadyWas = true;
                                    }
                                }
                            }
                            Thread.sleep(92, 879819);
                        }else{
                            imageView.clearAnimation();
                            Thread.sleep(100);
                        }
                    } while (framesRead != 0 && songIsPlaying);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (WavFileException e) {

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void setContext(Context context){
        FFT.context = context;
    }

    public static void setImageView(ImageView imageView){
        FFT.imageView = imageView;
    }

    public static void setupAnim(){
        enlargeAnim = AnimationUtils.loadAnimation(context, R.anim.enlarge);
        enlargeAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                imageView.startAnimation(shrinkAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        shrinkAnim = AnimationUtils.loadAnimation(context, R.anim.shrink);
    }

    public static void playAnim(){
        imageView.startAnimation(enlargeAnim);
    }
}
