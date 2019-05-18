package artem.surodeev.musicvisualizer;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private boolean isWav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            try {
                String name = FileUtil.from(getApplicationContext(), uri).getName();
                String ext = "";
                for (int i = name.length()-1; i >= 0; i--) {
                    if(name.charAt(i) == '.'){
                        for (int j = i+1; j < name.length(); j++) {
                            ext+= name.charAt(j);
                        }
                        break;
                    }
                }
                System.out.println(name + ";;;" + ext);
                if(!ext.equals("wav")){
                    final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
                    alertDialog.setTitle("Ошибка");
                    alertDialog.setMessage("Выберите файл с расширением .wav");
                    alertDialog.setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    alertDialog.show();
                }else{
                    Intent intent = new Intent(this, SongPlay.class);
                    intent.putExtra("path", uri.toString());
                    startActivity(intent);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
