package com.general.mediaplayer.loopvideo;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.crystal.crystalrangeseekbar.interfaces.OnSeekbarChangeListener;
import com.crystal.crystalrangeseekbar.widgets.CrystalSeekbar;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    @BindView(R.id.button1)
    ImageButton leftbottomBtn;

    @BindView(R.id.button2)
    ImageButton rightbottomBtn;

    private final String LOG_TAG = "MainActivity";
    VideoView videoView;

    @BindView(R.id.seekBar1)
    CrystalSeekbar seekbar;

    @BindView(R.id.volume_layout)
    LinearLayout volumeLayout;

    @BindView(R.id.touch_button)
    TextView touchBtn;

    AudioManager audioManager;
    int position = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);

        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        leftbottomBtn.setOnClickListener(this);
        rightbottomBtn.setOnClickListener(this);

        videoView =  findViewById(R.id.videoView);
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setLooping(true);
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Log.d("video", "setOnErrorListener ");
                return true;
            }
        });
        touchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (BuildConfig.FLAVOR.equals("loopvideo")){

                    if (volumeLayout.getVisibility() == View.VISIBLE)
                        volumeLayout.setVisibility(View.INVISIBLE);
                    else{
                        volumeLayout.setVisibility(View.VISIBLE);
                    }
                }
                else{

                    if (videoView.isPlaying()){
                        videoView.pause();
                        position = videoView.getCurrentPosition();
                    }
                    else {
                        videoView.seekTo(position);
                        videoView.start();
                    }
                }

            }
        });

        volumeLayout.setRotation(270.0f);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        seekbar.setMaxValue(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        seekbar.setMinStartValue(0);
        seekbar.apply();

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);

        seekbar.setOnSeekbarChangeListener(new OnSeekbarChangeListener() {
            @Override
            public void valueChanged(Number value) {

               audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value.intValue(), 0);
            }
        });

//        checkPermission();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        checkPermission();
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {//Can add more as per requirement

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
                    123);

        } else {

            getSDPathandPlay();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 123: {

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)     {

                    getSDPathandPlay();

                } else {

                    checkPermission();
                }
                return;
            }
        }
    }

    public static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public void getSDPathandPlay()
    {
        String removableStoragePath = "";
        File fileList[] = new File("/storage/").listFiles();
        for (File file : fileList)
        {
            if(!file.getAbsolutePath().equalsIgnoreCase(Environment.getExternalStorageDirectory().getAbsolutePath()) && file.isDirectory() && file.canRead())
            {
                removableStoragePath = file.getAbsolutePath();
            }
        }

        if (removableStoragePath.equals("")){

            Toast.makeText(this , "Cannot find SD card" ,Toast.LENGTH_SHORT).show();
        }
        else {

            Uri uri;
            if (BuildConfig.FLAVOR.equals("loopvideo")){
                uri = Uri.parse(removableStoragePath + "/looping_video.mp4");
            }
            else{
                uri = Uri.parse(removableStoragePath + "/looping_silent.mp4");
            }
            videoView.setVideoURI(uri);
            videoView.seekTo(0);
            videoView.start();
        }

    }

    private long mLastClickTIme = 0;

    @Override
    public void onClick(View v) {

        if (SystemClock.elapsedRealtime() - mLastClickTIme < 500)
        {
            // show CSR app
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.general.mediaplayer.csr");
            if (launchIntent != null) {
                startActivity(launchIntent);//null pointer check in case package name was not found
            }

            return;
        }

        mLastClickTIme = SystemClock.elapsedRealtime();

    }
}
