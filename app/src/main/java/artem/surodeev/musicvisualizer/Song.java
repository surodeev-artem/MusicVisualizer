package artem.surodeev.musicvisualizer;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import java.io.IOException;

public class Song extends AppCompatActivity {
    String path;
    MediaPlayer player;
    ImageView image;
    MediaPlayer mediaPlayer;
    static boolean musicPlays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song);


        image = findViewById(R.id.image);

        path = getIntent().getStringExtra("path");
        final Uri uri = Uri.parse(path);
        new Thread(new Runnable() {
            @Override
            public void run() {
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                mediaPlayer.start();
                musicPlays = true;
            }
        }).start();
        FFT.setSongIsPlaying(true);
        FFT.setContext(this);
        FFT.setImageView(image);
        FFT.setupAnim();
        try {
            FFT.startFFT(FileUtil.from(getApplicationContext(), uri));
        } catch (IOException e) {
            e.printStackTrace();
        }
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicPlays = !musicPlays;
                if(!musicPlays){
                    if(mediaPlayer != null && mediaPlayer.isPlaying()){
                        mediaPlayer.pause();
                    }
                }else{
                    if(mediaPlayer != null && !mediaPlayer.isPlaying()){
                        mediaPlayer.start();
                    }
                }
            }
        });
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
}
