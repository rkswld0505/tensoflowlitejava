package com.tnqkr98.tensoflowlitejava;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.os.HandlerCompat;
import androidx.appcompat.widget.Toolbar;
import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.AudioRecord;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
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

    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private NotificationManager mNotificationManager;
    private static final int NOTIFICATION_ID = 0;

    private AudioClassifier mAudioClassifier;
    private AudioRecord mAudioRecord;
    private long classficationInterval = 1000;       // 1초 sec (샘플링 주기)
    private Handler mHandler;

    String silence = "Silence";
    String siren = "Police car (siren)";
    String speech = "Speech";
    String car = "Vehicle horn, car horn, honking";
    String baby = "Baby cry, infant cry";
    String dog = "Dog";
    String f;

    int flag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        if (flag == 1) {
//            ImageView LoadImg = (ImageView) findViewById(R.id.load_img); //iv.setImageResource(R.drawable.img);
//            Glide.with(LoadImg).load(R.drawable.loading).into(LoadImg);

        }
        HandlerThread handlerThread = new HandlerThread("backgroundThread");
        handlerThread.start();
        mHandler = HandlerCompat.createAsync(handlerThread.getLooper());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 4);

        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (permission == PackageManager.PERMISSION_DENIED) ;
        createNotificationChannel();
//        findViewById(R.id.detect_button).setOnClickListener(StartClick); // 스타트 리스너
//        findViewById(R.id.stop_button).setOnClickListener(StopClick);


    }

//    Button.OnClickListener StartClick = new View.OnClickListener()
//    {
//        public void onClick(View v)  //start버튼 눌렀을때
//        {
//
//            ImageView LoadImg = (ImageView) findViewById(R.id.load_img); //iv.setImageResource(R.drawable.img);
//            Glide.with(LoadImg).load(R.drawable.loading).into(LoadImg); //움직이는 로딩img
//
//            //Button Btn = (Button) findViewById(R.id.detect_button);
//            //Btn.setEnabled(false); //감지버튼 비활성화
//            LoadImg.setVisibility(v.VISIBLE);
//
//            startAudioClassification();
//
//            Toast.makeText(getApplicationContext(), "감지가 시작되었습니다.", Toast.LENGTH_SHORT).show();
//        }
//    };
//
//    View.OnClickListener StopClick=new View.OnClickListener()
//    {
//        public void onClick(View v) //stop버튼 눌렀을때
//        {
//            ImageView LoadImg = (ImageView)findViewById(R.id.load_img);
//            Glide.with(LoadImg).load(R.drawable.loading).into(LoadImg);
//            LoadImg.setVisibility(v.INVISIBLE);
//
//            stopAudioClassfication();
//            Toast.makeText(getApplicationContext(), "감지가 종료되었습니다.", Toast.LENGTH_SHORT).show();
//
//            // TextView DetectText = (TextView)findViewById(R.id.detect_text);
//            // Button testbtn = (Button)findViewById(R.id.detect_button);
//            // DetectText.setVisibility(v.INVISIBLE);
//
//        }
//    };
//    private void startAudioClassification()
//    {
//
//        if(mAudioClassifier != null) return;
//
//        try {
//            AudioClassifier classifier = AudioClassifier.createFromFile(this, MODEL_FILE);
//            TensorAudio audioTensor = classifier.createInputTensorAudio();
//
//            AudioRecord record = classifier.createAudioRecord();
//            record.startRecording();
//
//            Runnable run = new Runnable() {
//                @Override
//                public void run() {
//                    audioTensor.load(record);
//                    List<Classifications> output = classifier.classify(audioTensor);
//                    List<Category> filterModelOutput = output.get(0).getCategories();
//                    for(Category c : filterModelOutput) {
//                        if (c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(speech)==true)
//                        {
//                            f="speech";
//                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//                            vibrator.vibrate(2000);
//                            Log.d("tensorAudio_java", " label : " + c.getLabel() + " score : " + c.getScore());
//                            //Toast.makeText(getApplicationContext(), c.getLabel()+"소리입니다"+c.getScore(), Toast.LENGTH_SHORT).show();
//
//                            sendNotification();
//                        }
//                        else if(c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(siren)==true)
//                        {
//                            f="siren";
//                            Log.d("test", " 경찰소리 : " + c.getLabel() + " score : " + c.getScore());
//                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//                            vibrator.vibrate(2000);
//
//                            sendNotification();
//
//                        }
//                        else if(c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(car)==true)
//                        {
//                            f="car";
//                            Log.d("차경적", " 소리 : " + c.getLabel() + " score : " + c.getScore());
//                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//                            vibrator.vibrate(2000);
//
//                            sendNotification();
//                        }
//
//                        else if(c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(dog)==true)
//                        {
//                            f="dog";
//                            Log.d("강아지", " 소리 : " + c.getLabel() + " score : " + c.getScore());
//                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//                            vibrator.vibrate(2000);
//
//                            sendNotification();
//                        }
//                        else if(c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(baby)==true)
//                        {
//                            f="baby";
//                            Log.d("아기울음", " 소리 : " + c.getLabel() + " score : " + c.getScore());
//                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//                            vibrator.vibrate(2000);
//
//                            sendNotification();
//                        }
//                        /*
//                        else if(c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(silence)==false)
//                        {
//
//                            Log.d("text", " 소리 : " + c.getLabel() + " score : " + c.getScore());
//                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
//                            vibrator.vibrate(2000);
//                            Toast.makeText(getApplicationContext(), c.getLabel()+"소리입니다"+c.getScore(), Toast.LENGTH_SHORT).show();
//                            //sendNotification();
//                        }
//
//                         */
//
//                    }
//
//
//                    mHandler.postDelayed(this,classficationInterval);
//                }
//            };
//
//            mHandler.post(run);
//            mAudioClassifier = classifier;
//            mAudioRecord = record;
//        }catch (IOException e){
//            e.printStackTrace();
//        }
//
//    }
//
//    private void stopAudioClassfication()
//    {
//        mHandler.removeCallbacksAndMessages(null);
//        mAudioRecord.stop();
//        mAudioRecord = null;
//        mAudioClassifier = null;
//    }


    @Override
    protected void onDestroy() {
//        stopAudioClassfication();
        super.onDestroy();
    }

    public void createNotificationChannel() {
        //notification manager 생성
        mNotificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);
        // 기기(device)의 SDK 버전 확인 ( SDK 26 버전 이상인지 - VERSION_CODES.O = 26)
        if (Build.VERSION.SDK_INT
                >= Build.VERSION_CODES.O) {
            //Channel 정의 생성자( construct 이용 )
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID
                    , "Test Notification", mNotificationManager.IMPORTANCE_HIGH);
            //Channel에 대한 기본 설정
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Notification from Mascot");
            // Manager을 이용하여 Channel 생성
            mNotificationManager.createNotificationChannel(notificationChannel);

        }

    }


    // Notification Builder를 만드는 메소드
    private NotificationCompat.Builder getNotificationBuilder() {
        flag = 1;
        Intent intent = new Intent(this, MainActivity.class)

                .setAction(Intent.ACTION_MAIN)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        if (f.equals("speech")) {
            NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                    .setContentTitle("주위에서 말소리가 들려요")
                    .setContentText("")
                    .setSmallIcon(R.drawable.dogbark)
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true);

            return notifyBuilder;
        } else if (f.equals("siren")) {
            NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                    .setContentTitle("사이렌")
                    .setContentText("주위에 사이렌소리가 들러요")
                    .setSmallIcon(R.drawable.siren)
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true);

            return notifyBuilder;
        } else if (f.equals("car")) {
            NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                    .setContentTitle("차경적")
                    .setContentText("주위에 차경적소리가 들러요")
                    .setSmallIcon(R.drawable.honking)
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true);

            return notifyBuilder;
        } else if (f.equals("dog")) {
            NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                    .setContentTitle("강아지")
                    .setContentText("강아지가 짖고있어요")
                    .setSmallIcon(R.drawable.dogbark)
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true);

            return notifyBuilder;
        } else if (f.equals("baby")) {
            NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                    .setContentTitle("아기")
                    .setContentText("아기가 울고있어요")
                    .setSmallIcon(R.drawable.babycry)
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true);

            return notifyBuilder;
        }

        return null;
    }

    // Notification을 보내는 메소드
    public void sendNotification() {
        // Builder 생성
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();
        // Manager를 통해 notification 디바이스로 전달
        mNotificationManager.notify(NOTIFICATION_ID, notifyBuilder.build());
    }
}