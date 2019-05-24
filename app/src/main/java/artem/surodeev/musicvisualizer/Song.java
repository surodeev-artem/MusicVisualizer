package artem.surodeev.musicvisualizer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

public class Song extends AppCompatActivity {
    String path;
    MediaPlayer mediaPlayer;
    static boolean musicPlays;
    private static Canvas canvas;
    private static float startX;
    private static float startY;
    private static float stop;
    private static SurfaceHolder surfaceHolder;
    private static Bitmap logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new MySurfaceView(this));

        path = getIntent().getStringExtra("path");
        final Uri uri = Uri.parse(path);
        logo = drawableToBitmap(getResources().getDrawable(R.drawable.logo, getTheme()));

        new Thread(new Runnable() {
            @Override
            public void run() {
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                mediaPlayer.start();
                musicPlays = true;
            }
        }).start();
        FFT.setSongIsPlaying(true);
        try {
            FFT.startFFT(FileUtil.from(getApplicationContext(), uri));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean getStatus(){
        return musicPlays;
    }

    @Override
    protected void onDestroy() {
        FFT.setSongIsPlaying(false);
        if (mediaPlayer != null && mediaPlayer.isPlaying()){
            mediaPlayer.stop();
        }
        super.onDestroy();
    }

    public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

        DrawThread drawThread;

        public MySurfaceView(Context context) {
            super(context);
            getHolder().addCallback(this);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            drawThread = new DrawThread(holder);
            drawThread.setRunning(true);
            drawThread.start();

            stop = (surfaceHolder.getSurfaceFrame().right >> 1)-10;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            boolean retry = true;
            drawThread.setRunning(false);
            while (retry) {
                try {
                    drawThread.join();
                    retry = false;
                } catch (InterruptedException ignored) {}
            }
        }
    }

    class DrawThread extends Thread{
        private boolean runFlag = false;


        public DrawThread(SurfaceHolder surfaceHolder){
            Song.surfaceHolder = surfaceHolder;
            startX = surfaceHolder.getSurfaceFrame().right >> 1;
            startY = surfaceHolder.getSurfaceFrame().bottom >> 1;
        }

        public void setRunning(boolean run) {
            runFlag = run;
        }

        @Override
        public void run() {
            while (runFlag) {
                try {
                    canvas = surfaceHolder.lockCanvas(null);
                }
                finally {
                    if (canvas != null) {
                        surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            canvas = null;
        }
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
    private static int counter = 0;
    public static void draw(int[] frs, Paint paint){
        if(surfaceHolder != null){
            long time = SystemClock.uptimeMillis();
            canvas = surfaceHolder.lockCanvas(null);
            synchronized (surfaceHolder) {
                if (canvas != null) {
                    //canvas.rotate(3F*counter++, startX, startY);
                    canvas.drawColor(Color.BLACK);
                    Paint circle = new Paint();
                    for (int i = 0; i < frs.length; i++) {
                        int fr = frs[i];
                        if (fr < 0) {
                            fr = -fr;
                        }
                        fr *= 100;
                        if(fr > stop){
                            fr = (int) stop;
                        }
                        canvas.rotate(360F/frs.length * i, startX, startY);
                        canvas.drawRoundRect(startX - 3, startY + fr, startX + 3, startY, 20, 20, paint);
                        canvas.rotate(-360F/frs.length * i, startX, startY);
                        circle.setColor(0xFFC5C5C5);
                        circle.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(startX, startY, stop/3, circle);
                        circle.setStyle(Paint.Style.STROKE);
                        circle.setStrokeWidth(5);
                        circle.setColor(paint.getColor());
                        canvas.drawCircle(startX, startY, stop/3, circle);
                    }
                    if(counter == 130){
                        counter = 0;
                    }
                }
            }
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
            System.out.println(SystemClock.uptimeMillis() - time);
        }
    }

    public static void drawLogo(int i){
        if(surfaceHolder != null){
            canvas = surfaceHolder.lockCanvas(null);
            synchronized (surfaceHolder) {
                if (canvas != null) {
                    canvas.rotate(11.25F*i, startX, startY);
                    canvas.drawBitmap(logo, startX- (logo.getWidth() >> 1), startY- (logo.getHeight() >> 1), new Paint());
                    canvas.rotate(-11.25F*i, startX, startY);
                }
            }
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
