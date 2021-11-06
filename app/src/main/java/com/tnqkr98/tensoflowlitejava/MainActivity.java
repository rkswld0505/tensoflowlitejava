package com.tnqkr98.tensoflowlitejava;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.core.os.HandlerCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
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
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

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
    int vibx = 1000;
    int flag;

    Dialog dilaog01;


    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        
        //Dialog 초기화 & 타이틀 제거 & 화면 크기 조정
        dilaog01 = new Dialog(MainActivity.this);
        dilaog01.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dilaog01.setContentView(R.layout.seek_bar);
        Window window = dilaog01.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        
        //백그라운드 핸들러
        HandlerThread handlerThread = new HandlerThread("backgroundThread");
        handlerThread.start();
        mHandler = HandlerCompat.createAsync(handlerThread.getLooper());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            requestPermissions(new String[]{Manifest.permission.RECORD_AUDIO}, 4);

        int permission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (permission == PackageManager.PERMISSION_DENIED) ;
        createNotificationChannel();

        //리스너 부분
        findViewById(R.id.start_button).setOnClickListener(StartClick);
        findViewById(R.id.end_button).setOnClickListener(StopClick);
        findViewById(R.id.setting_menu).setOnClickListener(SettingClick);

    }

    //시작 버튼 클릭
    Button.OnClickListener StartClick = new View.OnClickListener() {
        public void onClick(View v)
        {
            Message msg1 = handler1.obtainMessage();
            handler1.sendMessage(msg1);
            startAudioClassification();
        }
    };


    //종료 버튼 클릭
    Button.OnClickListener StopClick = new View.OnClickListener() {
        public void onClick(View v)
        {
            Message msg2 = handler2.obtainMessage();
            handler2.sendMessage(msg2);
            stopAudioClassfication();
        }
    };


    //셋팅 다이얼로그부분
    View.OnClickListener SettingClick = new View.OnClickListener() {
        public void onClick(View v) {
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
                            Log.d("tensorAudio_java", " label : " + c.getLabel() + " score : " + c.getScore());

                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(vibx);

                            Message msg3 = handler3.obtainMessage();
                            handler3.sendMessage(msg3);

                            sendNotification();

                                if (c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(speech) == true) {
                                    Message msg4 = handler4.obtainMessage();
                                    handler4.sendMessage(msg4);
                                }

                        } else if (c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(siren) == true) {
                            f = "siren";
                            Log.d("사이렌", " 소리 : " + c.getLabel() + " score : " + c.getScore());

                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(vibx);

                            Message msg5 = handler5.obtainMessage();
                            handler5.sendMessage(msg5);

                            sendNotification();

                            if (c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(siren) == true) {
                                Message msg6 = handler6.obtainMessage();
                                handler6.sendMessage(msg6);
                            }


                        } else if (c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(car) == true) {
                            f = "car";
                            Log.d("차경적", " 경적소리 : " + c.getLabel() + " score : " + c.getScore());

                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(vibx);

                            Message msg7 = handler7.obtainMessage();
                            handler7.sendMessage(msg7);

                            sendNotification();

                            if (c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(car) == true) {
                                Message msg8 = handler8.obtainMessage();
                                handler8.sendMessage(msg8);
                            }

                        } else if (c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(dog) == true) {
                            f = "dog";
                            Log.d("강아지", " 소리 : " + c.getLabel() + " score : " + c.getScore());

                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(vibx);

                            Message msg9 = handler9.obtainMessage();
                            handler9.sendMessage(msg9);

                            sendNotification();

                            if (c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(dog) == true) {
                                Message msg10 = handler10.obtainMessage();
                                handler10.sendMessage(msg10);
                            }

                        } else if (c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(baby) == true) {
                            f = "baby";
                            Log.d("아기울음", " 소리 : " + c.getLabel() + " score : " + c.getScore());

                            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                            vibrator.vibrate(vibx);

                            Message msg11 = handler11.obtainMessage();
                            handler11.sendMessage(msg11);

                            sendNotification();

                            if (c.getScore() > MINIMUM_DISPLAY_THRESHOLD && c.getLabel().equals(baby) == true) {
                                Message msg12 = handler12.obtainMessage();
                                handler12.sendMessage(msg12);
                            }

                        }

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

        Bitmap speechbig = BitmapFactory.decodeResource(getResources(), R.drawable.speech);
        Bitmap sirenbig = BitmapFactory.decodeResource(getResources(), R.drawable.siren);
        Bitmap carbig = BitmapFactory.decodeResource(getResources(), R.drawable.honking);
        Bitmap dogbig = BitmapFactory.decodeResource(getResources(), R.drawable.dogbark);
        Bitmap babybig = BitmapFactory.decodeResource(getResources(), R.drawable.babycry);

        Intent intent = new Intent(this, MainActivity.class)

                .setAction(Intent.ACTION_MAIN)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addCategory(Intent.CATEGORY_LAUNCHER);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);


        if (f.equals("speech")) {
            NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                    .setContentTitle("주의!")
                    .setContentText("말하는소리가 들리고 있어요!")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pendingIntent)
                    .setLargeIcon(speechbig)
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

    
    //Dialog 부분
    public void showDialog01() {

        dilaog01.show(); // 다이얼로그 띄우기
        final Button ok = (Button) dilaog01.findViewById(R.id.ok);
        final Button cancel = (Button) dilaog01.findViewById(R.id.cancel);
        SeekBar seekBar = (SeekBar) dilaog01.findViewById(R.id.sbar);
        TextView txtItem2 = (TextView) dilaog01.findViewById(R.id.txtItem2);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                txtItem2.setText(progress + "초");
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

    //버튼 스탑 눌렀을때 핸들러
    final Handler handler1 = new Handler() {
        public void handleMessage(Message msg1) {
            Button Btn = (Button) findViewById(R.id.start_button);
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.circle_bar2);
            TextView guideText1 = (TextView) findViewById(R.id.guide_text1);
            TextView guideText2 = (TextView) findViewById(R.id.guide_text2);
            TextView guideText3 = (TextView) findViewById(R.id.guide_text3);
            {
                progressBar.setVisibility(View.VISIBLE);
                guideText1.setText("주변 소리를 듣고 있어요!");
                guideText2.setText("주변 소리 감지를 멈추려면");
                guideText3.setText("종료 버튼을 눌러주세요!");
            }
        }
    };


    //버튼 스탑 눌렀을때 핸들러
    final Handler handler2 = new Handler() {
        public void handleMessage(Message msg2) {
            Button Btn = (Button) findViewById(R.id.end_button);
            ImageView imageView = (ImageView) findViewById(R.id.dogbark_sound);
            ProgressBar progressBar = (ProgressBar) findViewById(R.id.circle_bar2);
            TextView guideText1 = (TextView) findViewById(R.id.guide_text1);
            TextView guideText2 = (TextView) findViewById(R.id.guide_text2);
            TextView guideText3 = (TextView) findViewById(R.id.guide_text3);
            {
                imageView.setVisibility(View.GONE);
                progressBar.setVisibility(View.INVISIBLE);
                guideText1.setText("주변 소리를 감지하고 있지 않습니다.");
                guideText2.setText("주변 소리 감지를 시작하려면");
                guideText3.setText("시작 버튼을 눌러주세요!");
            }
        }
    };

    
    //말하는 소리 인식 시작할때 핸들러 (테스트용)
    final Handler handler3 = new Handler() {
        public void handleMessage(Message msg3) {
            ImageView imageView = (ImageView) findViewById(R.id.speech_sound);
            TextView guideText1 = (TextView) findViewById(R.id.guide_text1);
            TextView soundText = (TextView) findViewById(R.id.sound_text);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
            {
                imageView.setVisibility(View.VISIBLE);
                guideText1.setVisibility(View.GONE);
                soundText.setVisibility(View.VISIBLE);
                soundText.setText("말하는 소리가 들려요!");
            }
        }
    };
    

    //말하는 소리 인식 끝날때 핸들러 (테스트용)
    final Handler handler4 = new Handler() {
        public void handleMessage(Message msg4) {
            ImageView imageView = (ImageView) findViewById(R.id.speech_sound);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
            Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
            TextView soundText = (TextView) findViewById(R.id.sound_text);
            TextView guideText1 = (TextView) findViewById(R.id.guide_text1);
            {
                animation.setDuration(vibx);
                animation2.setDuration(vibx);
                animation2.setStartOffset(vibx);
                imageView.setVisibility(View.GONE);
                imageView.setAnimation(animation);
                soundText.setVisibility(View.GONE);
                soundText.setAnimation(animation);
                guideText1.setVisibility(View.VISIBLE);
                guideText1.setAnimation(animation2);
            }
        }
    };

    //사이렌 소리 인식 시작할때 핸들러
    final Handler handler5 = new Handler() {
        public void handleMessage(Message msg5) {
            ImageView imageView = (ImageView) findViewById(R.id.siren_sound);
            TextView guideText1 = (TextView) findViewById(R.id.guide_text1);
            TextView soundText = (TextView) findViewById(R.id.sound_text);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
            {
                imageView.setVisibility(View.VISIBLE);
                guideText1.setVisibility(View.GONE);
                soundText.setVisibility(View.VISIBLE);
                soundText.setText("사이렌 소리가 들려요!");
            }
        }
    };


    //사이렌 소리 인식 끝날때 핸들러
    final Handler handler6 = new Handler() {
        public void handleMessage(Message msg6) {
            ImageView imageView = (ImageView) findViewById(R.id.siren_sound);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
            Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
            TextView soundText = (TextView) findViewById(R.id.sound_text);
            TextView guideText1 = (TextView) findViewById(R.id.guide_text1);
            {
                animation.setDuration(vibx);
                animation2.setDuration(vibx);
                animation2.setStartOffset(vibx);
                imageView.setVisibility(View.GONE);
                imageView.setAnimation(animation);
                soundText.setVisibility(View.GONE);
                soundText.setAnimation(animation);
                guideText1.setVisibility(View.VISIBLE);
                guideText1.setAnimation(animation2);
            }
        }
    };

    //차경적 소리 인식 시작할때 핸들러
    final Handler handler7 = new Handler() {
        public void handleMessage(Message msg7) {
            ImageView imageView = (ImageView) findViewById(R.id.car_sound);
            TextView guideText1 = (TextView) findViewById(R.id.guide_text1);
            TextView soundText = (TextView) findViewById(R.id.sound_text);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
            {
                imageView.setVisibility(View.VISIBLE);
                guideText1.setVisibility(View.GONE);
                soundText.setVisibility(View.VISIBLE);
                soundText.setText("차 경적 소리가 들려요!");
            }
        }
    };


    //차경적 소리 인식 끝날때 핸들러
    final Handler handler8 = new Handler() {
        public void handleMessage(Message msg8) {
            ImageView imageView = (ImageView) findViewById(R.id.car_sound);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
            Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
            TextView soundText = (TextView) findViewById(R.id.sound_text);
            TextView guideText1 = (TextView) findViewById(R.id.guide_text1);
            {
                animation.setDuration(vibx);
                animation2.setDuration(vibx);
                animation2.setStartOffset(vibx);
                imageView.setVisibility(View.GONE);
                imageView.setAnimation(animation);
                soundText.setVisibility(View.GONE);
                soundText.setAnimation(animation);
                guideText1.setVisibility(View.VISIBLE);
                guideText1.setAnimation(animation2);
            }
        }
    };

    //강아지 소리 인식 시작할때 핸들러
    final Handler handler9 = new Handler() {
        public void handleMessage(Message msg9) {
            ImageView imageView = (ImageView) findViewById(R.id.dogbark_sound);
            TextView guideText1 = (TextView) findViewById(R.id.guide_text1);
            TextView soundText = (TextView) findViewById(R.id.sound_text);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
            {
                imageView.setVisibility(View.VISIBLE);
                guideText1.setVisibility(View.GONE);
                soundText.setVisibility(View.VISIBLE);
                soundText.setText("강아지 짖는 소리가 들려요!");
            }
        }
    };


    //강아지 소리 인식 끝날때 핸들러
    final Handler handler10 = new Handler() {
        public void handleMessage(Message msg10) {
            ImageView imageView = (ImageView) findViewById(R.id.dogbark_sound);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
            Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
            TextView soundText = (TextView) findViewById(R.id.sound_text);
            TextView guideText1 = (TextView) findViewById(R.id.guide_text1);
            {
                animation.setDuration(vibx);
                animation2.setDuration(vibx);
                animation2.setStartOffset(vibx);
                imageView.setVisibility(View.GONE);
                imageView.setAnimation(animation);
                soundText.setVisibility(View.GONE);
                soundText.setAnimation(animation);
                guideText1.setVisibility(View.VISIBLE);
                guideText1.setAnimation(animation2);
            }
        }
    };

    //아기 소리 인식 시작할때 핸들러
    final Handler handler11 = new Handler() {
        public void handleMessage(Message msg11) {
            ImageView imageView = (ImageView) findViewById(R.id.baby_sound);
            TextView guideText1 = (TextView) findViewById(R.id.guide_text1);
            TextView soundText = (TextView) findViewById(R.id.sound_text);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
            {
                imageView.setVisibility(View.VISIBLE);
                guideText1.setVisibility(View.GONE);
                soundText.setVisibility(View.VISIBLE);
                soundText.setText("아기 우는 소리가 들려요!");
            }
        }
    };


    //아기 소리 인식 끝날때 핸들러
    final Handler handler12 = new Handler() {
        public void handleMessage(Message msg12) {
            ImageView imageView = (ImageView) findViewById(R.id.baby_sound);
            Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_out);
            Animation animation2 = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
            TextView soundText = (TextView) findViewById(R.id.sound_text);
            TextView guideText1 = (TextView) findViewById(R.id.guide_text1);
            {
                animation.setDuration(vibx);
                animation2.setDuration(vibx);
                animation2.setStartOffset(vibx);
                imageView.setVisibility(View.GONE);
                imageView.setAnimation(animation);
                soundText.setVisibility(View.GONE);
                soundText.setAnimation(animation);
                guideText1.setVisibility(View.VISIBLE);
                guideText1.setAnimation(animation2);
            }
        }
    };

}