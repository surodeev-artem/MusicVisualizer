package artem.surodeev.musicvisualizer;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.io.IOException;

public class Song extends AppCompatActivity {
    private static MediaPlayer mediaPlayer;
    private static boolean musicPlays;
    private static float startX;
    private static float startY;
    private static float stop;
    private static SurfaceHolder surfaceHolder;
    private static Bitmap logo;
    private MySurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        surfaceView = new MySurfaceView(this);
        setContentView(surfaceView);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        String path = getIntent().getStringExtra("path");
        final Uri uri = Uri.parse(path);
        logo = drawableToBitmap(getResources().getDrawable(R.drawable.logo, getTheme()));
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        FFT.setSongIsPlaying(true);
        try {
            FFT.startFFT(FileUtil.from(getApplicationContext(), uri));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void startMusic() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                    mediaPlayer.start();
                }
            }
        }).start();
    }

    public static void pauseMusic() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                }
            }
        }).start();
    }

    private static void stopMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
    }

    public static boolean getStatus() {
        return musicPlays;
    }


    public static void setStatus(boolean status) {
        musicPlays = status;
    }

    @Override
    protected void onDestroy() {
        stopMusic();
        FFT.setSongIsPlaying(false);
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
        }
        super.onDestroy();
    }

    public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

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
            stop = (surfaceHolder.getSurfaceFrame().right >> 1) - 10;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            boolean retry = true;
            while (retry) {
                try {
                    drawThread.join();
                    retry = false;
                } catch (InterruptedException ignored) {
                }
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            float radius = stop / 3;

            if (x >= startX - radius && x <= startX + radius && y > startY - radius && y < startY + radius) {
                System.out.println(true);
                if (Song.getStatus()) {
                    Song.setStatus(false);
                    Song.pauseMusic();
                } else {
                    Song.setStatus(true);
                    Song.startMusic();
                }
            } else {
                System.out.println(false);
            }
            return true;
        }
    }

    class DrawThread extends Thread {
        DrawThread(SurfaceHolder surfaceHolder) {
            Song.surfaceHolder = surfaceHolder;
            startX = surfaceHolder.getSurfaceFrame().right >> 1;
            startY = surfaceHolder.getSurfaceFrame().bottom >> 1;
        }
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    private static int counter = 0;

    public static void draw(int[] frs, Paint paint) {
        if (surfaceHolder != null) {
            Canvas canvas = surfaceHolder.lockCanvas(null);
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
                        fr *= 50;
                        if (fr > stop) {
                            fr = (int) stop;
                        }
                        canvas.rotate(360F / frs.length * i, startX, startY);
                        canvas.drawRoundRect(startX - 3, startY + fr, startX + 3, startY, 20, 20, paint);
                        canvas.rotate(-360F / frs.length * i, startX, startY);
                        circle.setColor(0xFFC5C5C5);
                        circle.setStyle(Paint.Style.FILL);
                        canvas.drawCircle(startX, startY, stop / 3, circle);
                        circle.setStyle(Paint.Style.STROKE);
                        circle.setStrokeWidth(5);
                        circle.setColor(paint.getColor());
                        canvas.drawCircle(startX, startY, stop / 3, circle);
                        canvas.drawBitmap(logo, startX - (logo.getWidth() >> 1), startY - (logo.getHeight() >> 1), new Paint());
                    }
                    if (counter == 130) {
                        counter = 0;
                    }
                }
            }
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }
}