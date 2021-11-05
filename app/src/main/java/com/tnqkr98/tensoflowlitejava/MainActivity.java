package com.tnqkr98.tensoflowlitejava;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.os.HandlerCompat;
import androidx.appcompat.widget.Toolbar;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.progressindicator.IndeterminateDrawable;
import com.google.android.material.progressindicator.LinearProgressIndicator;

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

    int vib;
    int vibx=1000;
    int flag;

    Dialog dilaog01;
    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        dilaog01 = new Dialog(MainActivity.this);       // Dialog 초기화
        dilaog01.requestWindowFeature(Window.FEATURE_NO_TITLE); // 타이틀 제거
        dilaog01.setContentView(R.layout.seek_bar);
        Window window = dilaog01.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        if (flag == 1) {

//            ImageView LoadImg = (ImageView) findViewById(R.id.circle_bar2); //iv.setImageResource(R.drawable.img);
//            ProgressBar LoadBar = (ProgressBar) findViewById(R.id.circle_bar2);
//            Glide.with(LoadBar).load(R.id.circle_bar2).into(LoadImg);

        }
        HandlerThread handlerThread = new HandlerThread("backgroundThread");
        handlerThread.start();
        mHandler = HandlerCompat.createAsync(handlerThread.getLooper());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 4);

        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (permission == PackageManager.PERMISSION_DENIED) ;
        createNotificationChannel();
        findViewById(R.id.start_button).setOnClickListener(StartClick); // 스타트 리스너
        findViewById(R.id.end_button).setOnClickListener(StopClick);
        findViewById(R.id.setting_menu).setOnClickListener(SettingClick);
    }

    Button.OnClickListener StartClick = new View.OnClickListener() {
        public void onClick(View v)  //start버튼 눌렀을때
        {
            Button Btn = (Button) findViewById(R.id.start_button);
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.circle_bar2);
            {
                progressBar.setVisibility(View.VISIBLE);
            }
            TextView guideText1 = (TextView) findViewById(R.id.guide_text1);
            TextView guideText2 = (TextView) findViewById(R.id.guide_text2);
            TextView guideText3 = (TextView) findViewById(R.id.guide_text3);
            {
                guideText1.setText("주변 소리를 듣고 있어요!");
                guideText2.setText("주변 소리 감지를 멈추려면");
                guideText3.setText("종료 버튼을 눌러주세요!");
            }

            //Btn.setEnabled(false); //감지버튼 비활성화
            startAudioClassification();
            Toast toast = Toast.makeText(getApplicationContext(), "감지가 시작되었습니다.", Toast.LENGTH_SHORT);
            toast.show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 1000);
        }
    };

    View.OnClickListener StopClick = new View.OnClickListener() {
        public void onClick(View v) //stop버튼 눌렀을때
        {
            stopAudioClassfication();
            Button Btn = (Button) findViewById(R.id.end_button);
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.circle_bar2);
            {
                progressBar.setVisibility(View.INVISIBLE);
            }
            TextView guideText1 = (TextView) findViewById(R.id.guide_text1);
            TextView guideText2 = (TextView) findViewById(R.id.guide_text2);
            TextView guideText3 = (TextView) findViewById(R.id.guide_text3);
            {
                guideText1.setText("주변 소리를 감지하고 있지 않습니다.");
                guideText2.setText("주변 소리 감지를 시작하려면");
                guideText3.setText("시작 버튼을 눌러주세요!");
            }
            Toast toast = Toast.makeText(getApplicationContext(), "감지가 종료되었습니다.", Toast.LENGTH_SHORT);
            toast.show();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.cancel();
                }
            }, 1000);

            // TextView DetectText = (TextView)findViewById(R.id.detect_text);
            // Button testbtn = (Button)findViewById(R.id.detect_button);
            // DetectText.setVisibility(v.INVISIBLE);

        }
    };
    
    //셋팅 다이얼로그부분
    View.OnClickListener SettingClick=new View.OnClickListener()
    {
        public void onClick(View v)
        {
            showDialog01();
            /*
            final EditText editText=new EditText(MainActivity.this);

            AlertDialog.Builder vibdig=new AlertDialog.Builder(MainActivity.this);
            vibdig.setTitle("진동");
            vibdig.setView(editText);

            vibdig.setPositiveButton("입력", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    vib= Integer.parseInt(editText.getText().toString());
                    vibx=vib*1000;
                }
            });
            vibdig.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {

                }
            });
            vibdig.show();
        }
        */
        }
    };

    private void startAudioClassification() {


        if (mAudioClassifier != null) return;

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
                    for (Category c : filterModelOutput) {
                        if (c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(speech) == true) {
                            f = "speech";
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(vibx);
                            Log.d("tensorAudio_java", " label : " + c.getLabel() + " score : " + c.getScore());
                            //Toast.makeText(getApplicationContext(), c.getLabel()+"소리입니다"+c.getScore(), Toast.LENGTH_SHORT).show();

                            sendNotification();
                        } else if (c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(siren) == true) {
                            f = "siren";
                            Log.d("test", " 경찰소리 : " + c.getLabel() + " score : " + c.getScore());
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(vibx);

                            sendNotification();

                        } else if (c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(car) == true) {
                            f = "car";
                            Log.d("차경적", " 소리 : " + c.getLabel() + " score : " + c.getScore());
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(vibx);

                            sendNotification();
                        } else if (c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(dog) == true) {
                            f = "dog";
                            Log.d("강아지", " 소리 : " + c.getLabel() + " score : " + c.getScore());
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(vibx);

                            sendNotification();
                        } else if (c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(baby) == true) {
                            f = "baby";
                            Log.d("아기울음", " 소리 : " + c.getLabel() + " score : " + c.getScore());
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(vibx);

                            sendNotification();
                        }
                        /*
                        else if(c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(silence)==false)
                        {

                            Log.d("text", " 소리 : " + c.getLabel() + " score : " + c.getScore());
                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(2000);
                            Toast.makeText(getApplicationContext(), c.getLabel()+"소리입니다"+c.getScore(), Toast.LENGTH_SHORT).show();
                            //sendNotification();
                        }

                         */

                    }


                    mHandler.postDelayed(this, classficationInterval);
                }
            };

            mHandler.post(run);
            mAudioClassifier = classifier;
            mAudioRecord = record;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void stopAudioClassfication() {
        super.onStop();
        mHandler.removeCallbacksAndMessages(null);
        mAudioClassifier = null;
        if (mAudioRecord != null) {
            mAudioRecord.stop();
            mAudioRecord = null;
        }
    }


    @Override
    protected void onDestroy() {
        stopAudioClassfication();
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

        Bitmap mLargicon= BitmapFactory.decodeResource(getResources(),R.drawable.dogbark); //이거 test이미지인데 바꾸셈 speech 이미지 아무꺼나
        Bitmap sirenbig= BitmapFactory.decodeResource(getResources(),R.drawable.siren);
        Bitmap carbig= BitmapFactory.decodeResource(getResources(),R.drawable.honking);
        Bitmap dogbig= BitmapFactory.decodeResource(getResources(),R.drawable.dogbark);
        Bitmap babybig= BitmapFactory.decodeResource(getResources(),R.drawable.babycry);

        Intent intent = new Intent(this, MainActivity.class)

                .setAction(Intent.ACTION_MAIN)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        if (f.equals("speech")) {
            NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                    .setContentTitle("주의!")
                    .setContentText("말하는소리가 들리고 있어요!")
                    .setSmallIcon(R.mipmap.ic_launcher) //smallicon 이 이미지를 이어센스 앱이미지 넣으면됨 밑에도 똑같이
                    .setContentIntent(pendingIntent)
                    .setLargeIcon(mLargicon)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true);

            return notifyBuilder;
        } else if (f.equals("siren")) {
            NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                    .setContentTitle("주의!")
                    .setContentText("사이렌소리가 들리고 있어요!")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(sirenbig)
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true);

            return notifyBuilder;
        } else if (f.equals("car")) {
            NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                    .setContentTitle("주의!")
                    .setContentText("차 경적 소리가 들리고 있어요!")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(carbig)
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true);

            return notifyBuilder;
        } else if (f.equals("dog")) {
            NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                    .setContentTitle("주의!")
                    .setContentText("강아지가 짖는 소리가 들려요!")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(dogbig)
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true);

            return notifyBuilder;
        } else if (f.equals("baby")) {
            NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                    .setContentTitle("주의!")
                    .setContentText("아기 우는 소리가 들리고 있어요!")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setLargeIcon(babybig)
                    .setContentIntent(pendingIntent)
                    .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                    .setAutoCancel(true);

            return notifyBuilder;
        }

        return null;
    }
    public void showDialog01() {

        dilaog01.show(); // 다이얼로그 띄우기
        final Button ok = (Button) dilaog01.findViewById(R.id.ok);
        final Button cancel = (Button) dilaog01.findViewById(R.id.cancel);
        SeekBar seekBar = (SeekBar) dilaog01.findViewById(R.id.sbar);
        TextView txtItem2 = (TextView)dilaog01.findViewById(R.id.txtItem2);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
        {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser)
            {
                txtItem2.setText(progress+"초");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                vib = seekBar.getProgress();
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vibx = vib * 1000;
                dilaog01.dismiss();
            }
        });

        // 취소 버튼
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dilaog01.dismiss();
            }

        });
    }
        // Notification을 보내는 메소드
    public void sendNotification() {
        // Builder 생성
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();
        // Manager를 통해 notification 디바이스로 전달
        mNotificationManager.notify(NOTIFICATION_ID, notifyBuilder.build());
    }
}