package com.example.fiazm.dodgegame;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    // 600 x 1024
    // 50 x 105

    GameSurface gameSurface;
    float x;
    int seconds;
    Canvas canvas;
    Paint paintProperty;
    Paint paintProperty2;
    Paint crashColor;
    int score = 0;
    int speed = 7;

    @SuppressLint("ClickableViewAccessibility")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gameSurface = new GameSurface(this);
        gameSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(speed == 7)
                    speed = 14;
                else if(speed == 14)
                    speed = 7;
            }
        });
        setContentView(gameSurface);

        SensorManager sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        new CountDownTimer(31000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                seconds = (int)(millisUntilFinished / 1000) - 1;
            }

            @Override
            public void onFinish() {
                finish();
            }
        }.start();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onPause(){
        super.onPause();
        gameSurface.pause();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume(){
        super.onResume();
        gameSurface.resume();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        x = event.values[0];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public class GameSurface extends SurfaceView implements Runnable {

        Thread gameThread;
        SurfaceHolder holder;
        volatile boolean running = false;
        Bitmap myImage;
        Bitmap image2;

        int screenWidth;
        int screenHeight;

        int size = 300;
        int cord = 300;
        int rand = ThreadLocalRandom.current().nextInt(25, 525+ 1);

        Rect car = new Rect();
        Rect enemy = new Rect();

        boolean difCrash = true;

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public GameSurface(Context context) {
            super(context);

            holder=getHolder();

            myImage = BitmapFactory.decodeResource(getResources(),R.drawable.image);
            image2 = BitmapFactory.decodeResource(getResources(),R.drawable.image2);

            Display screenDisplay = getWindowManager().getDefaultDisplay();
            Point sizeOfScreen = new Point();
            screenDisplay.getSize(sizeOfScreen);
            screenWidth=sizeOfScreen.x;
            screenHeight=sizeOfScreen.y;

            paintProperty= new Paint();
            paintProperty.setTextSize(75);
            paintProperty.setColor(Color.WHITE);

            paintProperty2= new Paint();
            paintProperty2.setTextSize(50);
            paintProperty2.setColor(Color.WHITE);

            crashColor = new Paint();
            ColorFilter colorFilter = new LightingColorFilter(Color.BLACK, 0);
            crashColor.setColorFilter(colorFilter);
        }

        @Override
        public void run() {
            while (running){

                if (!holder.getSurface().isValid())
                    continue;

                if(seconds == 0)
                    finish();

                canvas= holder.lockCanvas();
                canvas.drawRGB(255,0,0);
                canvas.drawText("Time Left: " + String.valueOf(seconds) + " second(s)",25,200,paintProperty2);
                canvas.drawText("Score: " + score,25,100,paintProperty);

                if(cord <= (1000)) {
                    cord += speed;
                    canvas.drawBitmap(image2, rand, cord, null);
                }
                else {
                    if(difCrash)
                        score++;
                    cord = 300;
                    rand = ThreadLocalRandom.current().nextInt(25, 525+ 1);
                    canvas.drawBitmap(image2, rand, cord, null);
                    difCrash = true;
                }
                if(x > 0.1) {
                    if(size > 6) {
                        size -= 7;
                    }
                }
                if(x < -0.1) {
                    if(size + 50 < 595) {
                        size += 7;
                    }
                }

                if(difCrash)
                    canvas.drawBitmap(myImage,size,700,null);
                else
                    canvas.drawBitmap(myImage,size,700,crashColor);

                car.set(size, 700, size + 50, 700 + 105);
                enemy.set(rand, cord, rand + 50, cord + 114);

                if((car.intersect(enemy))) {
                    if(difCrash) {
                        SoundPool soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
                        int sound = soundPool.load(getContext(), R.raw.car_crash, 1);
                        soundPool.play(sound, 1, 1, 0, 0, 1);
                        MediaPlayer mediaPlayer = MediaPlayer.create(getContext(), R.raw.car_crash);
                        mediaPlayer.start();
                        score -= 1;
                        difCrash = false;
                    }
                }

                holder.unlockCanvasAndPost(canvas);
            }
        }

        public void resume(){
            running=true;
            gameThread=new Thread(this);
            gameThread.start();
        }

        public void pause() {
            running = false;
            while (true) {
                try {
                    gameThread.join();
                } catch (InterruptedException e) {
                }
            }
        }
    }
}