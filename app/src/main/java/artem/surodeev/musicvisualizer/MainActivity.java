package artem.surodeev.musicvisualizer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    AdView ad;
    InterstitialAd interstitialAd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        interstitialAd = new InterstitialAd(getApplicationContext());
        interstitialAd.setAdUnitId("ca-app-pub-3448655774227075/1595720546");

        ad = findViewById(R.id.ad);
        ad.loadAd(new AdRequest.Builder().addTestDevice("133F4B61D8C44D752740B660D89C8FC0").build());

        Button chooseSong = findViewById(R.id.chooseSong);
        chooseSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                permissionRequest();
            }
        });
    }

    private void getFile(){
        Intent musicGetter = new Intent(Intent.ACTION_GET_CONTENT);
        musicGetter.setType("audio/*");
        startActivityForResult(musicGetter, 1);
    }

    private void permissionRequest(){
        int response = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if(response == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }else{
            getFile();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 0){
            if(grantResults[0] == PackageManager.PERMISSION_DENIED){
                permissionRequest();
            }else{
                getFile();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1 && resultCode == Activity.RESULT_OK){
            Uri uri = data.getData();
            String extension = checkFileExtension(uri);
            if(!extension.equals("wav")){
                showDialogOfInvalidFileExtension();
            }else{
                Intent intent = new Intent(this, Song.class);
                intent.putExtra("path", uri.toString());
                startActivityForResult(intent, 555);
            }
        }else if(requestCode == 555 && resultCode == Activity.RESULT_CANCELED){
            interstitialAd.loadAd(new AdRequest.Builder().addTestDevice("133F4B61D8C44D752740B660D89C8FC0").build());
            interstitialAd.show();
        }
    }

    private String checkFileExtension(Uri uri){
        try {
            String name = FileUtil.from(getApplicationContext(), uri).getName();
            String extension = "";
            for (int i = name.length()-1; i >= 0; i--) {
                if(name.charAt(i) == '.'){
                    for (int j = i+1; j < name.length(); j++) {
                        extension+= name.charAt(j);
                    }
                    break;
                }
            }
            return extension;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void showDialogOfInvalidFileExtension(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Ошибка");
        alertDialog.setMessage("Выберите файл с расширением .wav");
        alertDialog.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getFile();
            }
        });
        alertDialog.setNeutralButton("Преобразовать в wav", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showTheLinkClickDialog();
            }
        });
        alertDialog.show();
    }

    private void showTheLinkClickDialog(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Подтверждение");
        alertDialog.setMessage("Сейчас Вы перейдете на сайт online-convert.com. Подтвердить?");
        alertDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://audio.online-convert.com/ru/convert-to-wav"));
                startActivity(intent);
            }
        });
        alertDialog.setNegativeButton("Нет", null);
        alertDialog.show();
    }

}
