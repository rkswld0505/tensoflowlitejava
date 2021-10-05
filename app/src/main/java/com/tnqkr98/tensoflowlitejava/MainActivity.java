package com.tnqkr98.tensoflowlitejava;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.os.HandlerCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioRecord;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;


import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String MODEL_FILE = "yamnet.tflite";
    private static final float MINIMUM_DISPLAY_THRESHOLD = 0.6f;


    private AudioClassifier mAudioClassifier;
    private AudioRecord mAudioRecord;
    private long classficationInterval = 1000;       // 0.5 sec (샘플링 주기)
    private Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        HandlerThread handlerThread = new HandlerThread("backgroundThread");
        handlerThread.start();
        mHandler = HandlerCompat.createAsync(handlerThread.getLooper());

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO}, 4);

        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
            if (permission == PackageManager.PERMISSION_DENIED);

        findViewById(R.id.detect_button).setOnClickListener(StartClick); // 스타트 리스너
        findViewById(R.id.stop_button).setOnClickListener(StopClick);

    }
    Button.OnClickListener StartClick = new View.OnClickListener()
    {
        public void onClick(View v)  //start버튼 눌렀을때
        {
            ImageView LoadImg = (ImageView) findViewById(R.id.load_img); //iv.setImageResource(R.drawable.img);
            Glide.with(LoadImg).load(R.drawable.loading).into(LoadImg); //움직이는 로딩img

            //Button Btn = (Button) findViewById(R.id.detect_button);
            //Btn.setEnabled(false); //감지버튼 비활성화
            LoadImg.setVisibility(v.VISIBLE);

            startAudioClassification();

            Toast.makeText(getApplicationContext(), "감지가 시작되었습니다.", Toast.LENGTH_SHORT).show();
        }
    };

    View.OnClickListener StopClick=new View.OnClickListener()
    {
        public void onClick(View v) //stop버튼 눌렀을때
        {
            ImageView LoadImg = (ImageView)findViewById(R.id.load_img);
            Glide.with(LoadImg).load(R.drawable.loading).into(LoadImg);
            LoadImg.setVisibility(v.INVISIBLE);

            stopAudioClassfication();
            Toast.makeText(getApplicationContext(), "감지가 종료되었습니다.", Toast.LENGTH_SHORT).show();

            // TextView DetectText = (TextView)findViewById(R.id.detect_text);
            // Button testbtn = (Button)findViewById(R.id.detect_button);
            // DetectText.setVisibility(v.INVISIBLE);

        }
    };
    private void startAudioClassification()
    {


        if(mAudioClassifier != null) return;

        try {
            AudioClassifier classifier = AudioClassifier.createFromFile(this, MODEL_FILE);
            TensorAudio audioTensor = classifier.createInputTensorAudio();

            AudioRecord record = classifier.createAudioRecord();
            record.startRecording();

            Runnable run = new Runnable() {
                @Override
                public void run() {
                    audioTensor.load(record);
                    List<Classifications> output = classifier.classify(audioTensor);
                    List<Category> filterModelOutput = output.get(0).getCategories();
                    for(Category c : filterModelOutput) {
                        if (c.getScore() > MINIMUM_DISPLAY_THRESHOLD)
                            Log.d("tensorAudio_java", " label : " + c.getLabel() + " score : " + c.getScore());
                    }

                    mHandler.postDelayed(this,classficationInterval);
                }
            };

            mHandler.post(run);
            mAudioClassifier = classifier;
            mAudioRecord = record;
        }catch (IOException e){
            e.printStackTrace();
        }

    }

    private void stopAudioClassfication()
    {
        mHandler.removeCallbacksAndMessages(null);
        mAudioRecord.stop();
        mAudioRecord = null;
        mAudioClassifier = null;
    }


    @Override
    protected void onDestroy()
    {
        stopAudioClassfication();
        super.onDestroy();
    }
}