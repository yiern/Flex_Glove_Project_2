package com.example.bluetooth_testing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;

import android.media.MediaPlayer;
import android.media.SoundPool;
import android.media.SoundPool.Builder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static android.content.ContentValues.TAG;

public class PlayActivity extends Activity{

    private SoundPlayer sound;

    private int mMaxChars = 50000;//Default
    private int maximumTime = 10;

    int i = 0, score = 0;
    private BluetoothDevice mDevice;
    private UUID mDeviceUUID;
    private ProgressDialog progressDialog;
    BluetoothDataReceiver receiver;

    private ScrollView scrollView;
    private TextView mTxtReceive;
    private TextView levelHeader;
    private TextView instruction;

    //counter of time since app started, a background task
    private long mStartTime = 0L;
    private TextView mTimeLabel;
    Long difference;
    //handler to handle the message to the timer task
    private Handler mHandler = new Handler();

    Button thumb,index,middle,ring,pinky,endButton;
    TextView start,text,title;
    //Finger gif
    String  gifImagePath,tone;
    WebView fingerImage;

    //The required degree for easy level
    ArrayList<HashMap<Integer,Double>> easyDegree;

    //The required degree for medium level
    ArrayList<ArrayList<Double>> mediumDegree;

    //Keep track the current level and current difficulty level
    //Difficulty = 1 = easy
    //Difficulty = 1 = medium
    Integer currentLevel, difficulty_level, sequence;

    Boolean isPlaying,isPlaying_piano;
    Button startButton,mary;
    int user_id,failed_attempts;
    //Keep track start datetime
    Date startDatetime;
    Long StartTime;
    Long StartTime_nano = System.nanoTime();
    CountDownTimer timer, timer_piano;
    int c_flag, d_flag, e_flag, f_flag, g_flag, timer_flag;
    String hand_orientation;
    String[] melody_MaryHadALittleLamb = {"E", "D", "C", "D", "E", "E", "E", "D", "D", "D", "E", "E", "E","E","D","C","D","E","E","E","E","D","D","E","D","C"};
    private boolean finish_game, isFailed;

    MediaPlayer c_note, d_note, e_note, f_note, g_note;

    @SuppressLint("ResourceAsColor")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {


        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        mDevice = b.getParcelable(BluetoothManagementActivity.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(b.getString(BluetoothManagementActivity.DEVICE_UUID));
        mMaxChars = b.getInt(BluetoothManagementActivity.BUFFER_SIZE);
        hand_orientation = b.getString("hand orientation");
        Log.d(TAG, "onCreate: what device?"+ mDevice);

        Bundle extras = getIntent().getExtras();
        user_id = extras.getInt("user_id");
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        difficulty_level = Integer.parseInt(b.getString(LevelSelectionActivity.DIFFICULTY));

        if(difficulty_level == 3 || difficulty_level==4)
        {
            isPlaying_piano = false;


            setContentView(R.layout.activity_piano);


            mTxtReceive = findViewById(R.id.txtReceive);
            thumb = (Button) findViewById(R.id.button);
            index = (Button) findViewById(R.id.button2);
            middle = (Button) findViewById(R.id.button3);
            ring = (Button) findViewById(R.id.button4);
            pinky = (Button) findViewById(R.id.button5);

            startButton = (Button) findViewById(R.id.Start_button);
            endButton = (Button) findViewById(R.id.button6);
            title = (TextView) findViewById(R.id.textView3);

            scrollView = (ScrollView) findViewById(R.id.viewScroll);

            thumb.setBackgroundResource(R.color.C_Block);
            index.setBackgroundResource(R.color.D_Block);
            middle.setBackgroundResource(R.color.E_Block);
            ring.setBackgroundResource(R.color.F_Block);
            pinky.setBackgroundResource(R.color.G_Block);
            isPlaying = false;
            finish_game = false;

            startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mDevice==null)
                    {
                        Snackbar.make(v,"not connected to any bluetooth device", BaseTransientBottomBar.LENGTH_LONG);
                    }
                    finish_game = false;
                    i = 0;
                    isPlaying = true;

                    startButton.setText("PLAYING");
                    startButton.setEnabled(false);
                    StartTime_nano = System.nanoTime();
                    StartTime = System.currentTimeMillis();
                    if(difficulty_level == 4) {
                        timer_piano.cancel();
                        timer_piano.start();
                    }

                }
            });
            endButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    timer_piano.cancel();
                    finish_game = true;
                    new android.app.AlertDialog.Builder(PlayActivity.this)
                            .setIcon(R.drawable.sad)
                            .setTitle("Game Ended")
                            .setMessage("Game ended")
                            .setPositiveButton("restart", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startButton.setEnabled(true);
                                    startButton.setText("Restart!");

                                }
                            })
                            .setCancelable(false)
                            .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            })
                            .show();


                }
            });

            timer_piano = new CountDownTimer(5000,1000)
            {
                @Override
                public void onTick(long millisUntilFinished) {
                    startButton.setText("Seconds Remaining: " + millisUntilFinished/1000);

                }

                @Override
                public void onFinish() {
                   Toast.makeText(PlayActivity.this,"timer finished",Toast.LENGTH_SHORT).show();
                    failed_attempts++;
                    //i ++;
                    timer_piano.cancel();
                    timer_piano.start();
                }
            };

        }
        else{
        setContentView(R.layout.activity_play);



        scrollView = (ScrollView) findViewById(R.id.viewScroll);
        mTxtReceive = (TextView) findViewById(R.id.txtReceive);
        levelHeader = (TextView) findViewById((R.id.levelHeader));
        instruction = (TextView) findViewById((R.id.gameInstruction));
        startButton = (Button) findViewById(R.id.btnStartGame);

        //References - https://stackoverflow.com/posts/34776689/revisions
        fingerImage = (WebView) findViewById(R.id.fingerImage);

        isPlaying = false;


        timer = new CountDownTimer(maximumTime * 1000, 1000) {
            public void onTick(long millisUntilFinished) //display timer on the start game button
            {
                startButton.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish()//trigger if timer exceeds 10 sec
            {
                //play fail sound
                sound.playFailSound();

                isPlaying = false;
                if(difficulty_level==1) {
                    new android.app.AlertDialog.Builder(PlayActivity.this)
                            .setIcon(R.drawable.sad)
                            .setTitle("Failed")
                            .setMessage("you did not succeed")
                            .setPositiveButton("Restart", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    currentLevel = 1;
                                    populateResources();
                                    startButton.setEnabled(true);
                                    startButton.setText("Restart");
                                    dialog.dismiss();

                                }
                            })
                            .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                    dialog.dismiss();
                                }
                            })
                            .setCancelable(false)
                            .show();

                    saveToDb(0, maximumTime, user_id, difficulty_level);
                }
                else if(difficulty_level == 2)


                    new android.app.AlertDialog.Builder(PlayActivity.this)
                            .setIcon(R.drawable.sad)
                            .setTitle("Failed")
                            .setMessage("you did not succeed")
                            .setPositiveButton("Restart", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    currentLevel = 1;
                                    populateResources();
                                    startButton.setEnabled(true);
                                    dialog.dismiss();

                                }
                            })
                            .setNegativeButton("Quit", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    finish();
                                    dialog.dismiss();
                                }
                            })
                            .setCancelable(false)
                            .show();

            }

        };

        //Start the game
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                timer.start();
                startButton.setText("PLAYING");
                startDatetime = new Date();
                isPlaying = true;
                startButton.setEnabled(false);
            }
        });



        easyDegree = new ArrayList<HashMap<Integer, Double>>();
        mediumDegree = new ArrayList<ArrayList<Double>>();

        currentLevel = 1;

        //Easy level

        //Add your degree into this array
        easyDegree.add(new HashMap<Integer, Double>());
        easyDegree.add(new HashMap<Integer, Double>());
        easyDegree.add(new HashMap<Integer, Double>());
        easyDegree.add(new HashMap<Integer, Double>());
        easyDegree.add(new HashMap<Integer, Double>());

        //Level 1 to level 5
        //Required sensor and required degree
        easyDegree.get(0).put(1, 30.0);
        easyDegree.get(1).put(2, 30.0);
        easyDegree.get(2).put(3, 30.0);
        easyDegree.get(3).put(4, 30.0);
        easyDegree.get(4).put(5, 30.0);

        //Medium level

        //Level 1 to level 5
        //Required degree for sensor 1 and sensor 2
        mediumDegree.add(new ArrayList<Double>());
        mediumDegree.add(new ArrayList<Double>());
        mediumDegree.add(new ArrayList<Double>());
        mediumDegree.add(new ArrayList<Double>());
        mediumDegree.add(new ArrayList<Double>());

        //Level 1
        mediumDegree.get(0).add(15.0); //Sensor1
        mediumDegree.get(0).add(25.0); //Sensor2

        //Level 2
        mediumDegree.get(1).add(20.0); //Sensor1
        mediumDegree.get(1).add(45.0); //Sensor2

        //Level 3
        mediumDegree.get(2).add(50.0); //Sensor1
        mediumDegree.get(2).add(25.0); //Sensor2

        //Level 4
        mediumDegree.get(3).add(50.0); //Sensor1
        mediumDegree.get(3).add(45.0); //Sensor2

        //Level 5
        mediumDegree.get(4).add(55.0); //Sensor1
        mediumDegree.get(4).add(50.0); //Sensor2

        //Log.d("testb", easyDegree.toString()); for debugging

        populateResources(); //call this function so that it can populate the overall outlook for the levels
    }
    }

    @Override
    public void onBackPressed(){
            finish();
    }

    @Override
    protected void onStart()
    {
        //Register a broadcast receiver to receive signal from bluetooth service
        //References - https://stackoverflow.com/questions/9128103/broadcastreceiver-with-multiple-filters-or-multiple-broadcastreceivers
        receiver = new PlayActivity.BluetoothDataReceiver();
        registerReceiver(receiver, new IntentFilter("GET_BLUETOOTH_DATA"));
        registerReceiver(receiver, new IntentFilter("BLUETOOTH_UPDATE"));
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));

        Intent bluetoothDataServiceIntent = new Intent(this,BluetoothDataService.class);
        bluetoothDataServiceIntent.putExtra(BluetoothManagementActivity.DEVICE_UUID, mDeviceUUID.toString());
        bluetoothDataServiceIntent.putExtra(BluetoothManagementActivity.DEVICE_EXTRA, mDevice);
        bluetoothDataServiceIntent.putExtra(BluetoothManagementActivity.BUFFER_SIZE, mMaxChars);
        if(!BluetoothDataService.isRunning())
        {
            progressDialog = ProgressDialog.show(PlayActivity.this, "Hold on", "Connecting");//References - http://stackoverflow.com/a/11130220/1287554
            startService(bluetoothDataServiceIntent);
        }

        super.onStart();
    }

    @Override
    protected void onStop()
    {
        //Unregister the broadcast receiver upon stopping activity
        unregisterReceiver(receiver);

        Intent myService = new Intent(this, BluetoothDataService.class);

        if(BluetoothDataService.isRunning())
        {
            stopService(myService);
        }

        super.onStop();
    }



    //Main logic

    private boolean gameLogic(String sensorData) {
        List<String> SensorDataList = Arrays.asList(sensorData.split(","));


        boolean success = false;

        //Easy mode logic
        if (difficulty_level == 1) {
            sound = new SoundPlayer(this);
            Integer requiredSensor = Integer.valueOf(easyDegree.get(currentLevel - 1).keySet().toArray()[0].toString());
            Double requiredDegree = Double.valueOf(easyDegree.get(currentLevel - 1).get(requiredSensor).toString());

            Double receivedDegree;


            /*
            if(currentLevel == 1)
            {
                receivedDegree = Double.valueOf(SensorDataList.get(0));
                if (receivedDegree >= requiredDegree) {
                    success = true;
                }
            }
            else if(currentLevel == 2)
            {
                receivedDegree = Double.valueOf(SensorDataList.get(1));
                if (receivedDegree >= requiredDegree) {
                    success = true;
                }
            }
            else if(currentLevel == 3)
            {
                receivedDegree = Double.valueOf(SensorDataList.get(2));
                if (receivedDegree >= requiredDegree) {
                    success = true;
                }
            }
            else if(currentLevel == 4)
            {
                receivedDegree = Double.valueOf(SensorDataList.get(3));
                if (receivedDegree >= requiredDegree) {
                    success = true;
                }
            }
            else if(currentLevel == 5)
            {
                receivedDegree = Double.valueOf(SensorDataList.get(4));
                if (receivedDegree >= requiredDegree) {
                    success = true;
                }
            }
            */

            receivedDegree = Double.valueOf(SensorDataList.get(currentLevel-1));
            if(receivedDegree > requiredDegree){
                success= true;
            }
            //Compare the received degree and the configured degree for current level

        }
        //Medium mode logic
        else if (difficulty_level == 2) {
            sound = new SoundPlayer(this);
            Double requiredDegree1 = Double.valueOf(mediumDegree.get(currentLevel - 1).get(0).toString());
            Double requiredDegree2 = Double.valueOf(mediumDegree.get(currentLevel - 1).get(1).toString());


            Double receivedDegree1 = Double.valueOf(SensorDataList.get(0));
            Double receivedDegree2 = Double.valueOf(SensorDataList.get(currentLevel));
            if (receivedDegree1 >= requiredDegree1 && receivedDegree2 >= requiredDegree2) {
                success = true;
            }


        }


        //piano free play
        else if (difficulty_level == 3)
        {
            MediaPlayer mp ;
            title.setText("Free Play" );
            Double requiredDegree = 60.0;

            c_note = MediaPlayer.create(this, R.raw.c_note);
            d_note = MediaPlayer.create(this, R.raw.d_note);
            e_note = MediaPlayer.create(this, R.raw.e_note);
            f_note = MediaPlayer.create(this, R.raw.f_note);
            g_note = MediaPlayer.create(this, R.raw.g_note);

            //Double thumb_reading = Double.valueOf(SensorDataList.get(6));
            try {
                Double thumb_reading = Double.valueOf(SensorDataList.get(0));
                Double index_reading = Double.valueOf(SensorDataList.get(1));
                Double middle_reading = Double.valueOf(SensorDataList.get(2));
                Double ring_reading = Double.valueOf(SensorDataList.get(3));
                Double pinky_reading = Double.valueOf(SensorDataList.get(4));

                if (thumb_reading >= requiredDegree)
                {
                    thumb.setBackgroundColor(Color.BLUE);
                    if(c_flag == 0) {
                        c_note.start();
                        c_flag = 1;
                    }
                    else
                        Log.d(TAG, "d flag raised");
                }
                else //(index_reading <= requiredDegree)
                {
                    thumb.setBackgroundResource(R.color.C_Block);
                    c_note.release();
                    c_flag=0;
                }
                if (index_reading >= requiredDegree)
                {
                    index.setBackgroundColor(Color.BLUE);
                    if(d_flag == 0) {
                        d_note.start();
                        d_flag = 1;
                    }
                    else
                        Log.d(TAG, "d flag raised");
                }
                else //(index_reading <= requiredDegree)
                {
                    index.setBackgroundResource(R.color.D_Block);
                    d_note.release();
                    d_flag=0;
                }
                if (middle_reading >= requiredDegree) {
                    middle.setBackgroundColor(Color.BLUE);
                    if(e_flag == 0) {
                        e_note.start();
                        e_flag = 1;
                    }

                }
                else // (middle_reading <= requiredDegree)
                {
                    middle.setBackgroundResource(R.color.E_Block);
                    e_note.release();
                    e_flag = 0;
                }

                if (ring_reading >= requiredDegree) {
                    ring.setBackgroundColor(Color.BLUE);
                    if(f_flag == 0) {
                        f_note.start();
                        f_flag = 1;
                    }

                }
                else // (ring_reading <= requiredDegree)
                {
                    ring.setBackgroundResource(R.color.F_Block);
                    f_note.release();
                    f_flag = 0;
                }

                if (pinky_reading >= requiredDegree) {
                    pinky.setBackgroundColor(Color.BLUE);
                    if(g_flag == 0) {
                        g_note.start();
                        g_flag = 1;
                    }

                }
                else// (pinky_reading <= requiredDegree)
                {
                    pinky.setBackgroundResource(R.color.G_Block);
                    g_note.release();
                    g_flag = 0;
                }

            }
            catch(NumberFormatException n)
            {
                Log.e(TAG, "gameLogic: incorrect number format", n);
            }


            endButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    finish_game = true;
                    //difference = System.currentTimeMillis() - StartTime;
                }
            });




        }
        else if(difficulty_level== 4)
        {

            Double requiredDegree = 40.0;

            try {
                Double thumb_Reading = Double.valueOf(SensorDataList.get(0));
                Double index_reading = Double.valueOf(SensorDataList.get(1));
                Double middle_reading = Double.valueOf(SensorDataList.get(2));
                Double ring_reading = Double.valueOf(SensorDataList.get(3));
                Double pinky_reading = Double.valueOf(SensorDataList.get(4));
                if (i == melody_MaryHadALittleLamb.length)
                {
                    finish_game=true;
                }




            if( i < melody_MaryHadALittleLamb.length || i != melody_MaryHadALittleLamb.length)
            {
                tone = melody_MaryHadALittleLamb[i];
                title.setText("Mary had a little lamp \n " + "Score: " + score +" \n" +" Times Failed: " + failed_attempts);

                if (tone.equals("C"))
                {
                    if(timer_flag == 0) {
                        timer_piano.start();
                        timer_flag = 1;
                    }
                    thumb.setBackgroundColor(Color.BLUE);
                    MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.c_note);
                    if (thumb_Reading >= requiredDegree)
                    {          //todo replace index_right reading with thumb_right reading
                        timer_flag = 0;

                        if(c_flag == 0) {
                            mp.start();
                            c_flag=1;
                        }
                        timer_piano.cancel();

                        thumb.setBackgroundResource(R.color.C_Block);
                        i++;
                        score++;
                        c_flag = 0;

                    }

                }

                if (tone.equals("D"))
                {
                    if(timer_flag == 0) {
                        timer_piano.start();
                        timer_flag = 1;
                    }
                    index.setBackgroundColor(Color.BLUE);
                    MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.d_note);
                    if (index_reading >= requiredDegree) {
                        timer_flag = 0;
                        timer_piano.cancel();

                        if(d_flag == 0) {
                            mp.start();
                            d_flag=1;
                        }

                        index.setBackgroundResource(R.color.D_Block);


                            i++;
                            score++;
                            d_flag=0;

                    }

                }

                if (tone == "E")
                {
                    if(timer_flag == 0) {
                        timer_piano.start();
                        timer_flag = 1;
                    }
                    middle.setBackgroundColor(Color.BLUE);
                    MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.e_note);
                    if (middle_reading >= requiredDegree) {
                        timer_flag = 0;
                        timer_piano.cancel();

                        middle.setBackgroundResource(R.color.E_Block);
                        if(e_flag == 0) {
                            mp.start();
                            e_flag=1;
                        }


                            i++;
                            score++;
                            e_flag=0;

                    }
                }


                if (tone == "F") {
                    if(timer_flag == 0) {
                        timer_piano.start();
                        timer_flag = 1;
                    }
                    ring.setBackgroundColor(Color.BLUE);
                    MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.f_note);
                    if (ring_reading >= requiredDegree) {
                        timer_flag = 0;
                        if(f_flag == 0) {
                            mp.start();
                            f_flag=1;
                        }

                        ring.setBackgroundResource(R.color.F_Block);


                            i++;
                            score++;
                            f_flag=0;


                    }
                }

                if (tone.equals("G")) {
                    if(timer_flag == 0) {
                        timer_piano.start();
                        timer_flag = 1;
                    }
                    double requiredDegree_pinky = 30.0;
                    pinky.setBackgroundColor(Color.BLUE);
                    MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.g_note);
                    if (pinky_reading >= requiredDegree_pinky) {
                        timer_flag = 0;
                        timer_piano.cancel();
                        if(g_flag == 0) {
                            mp.start();
                            g_flag=1;
                        }
                        pinky.setBackgroundResource(R.color.G_Block);


                            i++;
                            score++;
                            g_flag=0;

                    }
                }

            }
            else if (i == melody_MaryHadALittleLamb.length){
                finish_game=true;
            }

            }catch(NumberFormatException n)
            {
                Log.e(TAG, "gameLogic:  ",n );
            }


        }
        if(finish_game)
        {
            isPlaying=false;
            Bundle extras = getIntent().getExtras();
            final int user_id = extras.getInt("user_id");
            long diff = (System.nanoTime() - StartTime_nano)/1000000000;
            Log.d(TAG, "gameLogic: " + diff);
            if(difficulty_level == 3)
                saveToDb_piano(diff, user_id);
            if (difficulty_level == 4) {
                Log.d(TAG, "gameLogic: stored in level 4: "+ score);
                //Toast.makeText(this,String.valueOf(score),Toast.LENGTH_SHORT).show();
                saveToDb_piano_1(diff, user_id, score);
            }


                    new android.app.AlertDialog.Builder(PlayActivity.this)
                        .setIcon(R.drawable.congrats)
                        .setTitle("Completed")
                        .setMessage("Finished!")
                        .setPositiveButton("Restart", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startButton.setEnabled(true);
                                dialog.dismiss();

                            }
                        })
                        .setNegativeButton("Quit", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .show();


        }


        //If success then prompt message
        if(success)
        {
            Bundle extras = getIntent().getExtras();
            final int user_id = extras.getInt("user_id");
            //Stop the timer
            timer.cancel();

            //Reset the wording for start button
            startButton.setText("Start Game");


            Date currentDateTime = new Date();
            long diff = currentDateTime.getTime() - startDatetime.getTime();

            //Calculate the time spent
            long timeSpent = TimeUnit.SECONDS.convert(diff, TimeUnit.MILLISECONDS);
            if(difficulty_level == 1)
                saveToDb(1, timeSpent,user_id,1);
            else if(difficulty_level == 2)
                saveToDb(1, timeSpent,user_id,2);

            //If reach max level of the specified mode, give player option to quit or restart
            if(currentLevel == easyDegree.size())
            {
                new android.app.AlertDialog.Builder(PlayActivity.this)
                        .setIcon(R.drawable.congrats)
                        .setTitle("Completed")
                        .setMessage("Finished!")
                        .setPositiveButton("Restart", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                currentLevel = 1;
                                populateResources();
                                startButton.setEnabled(true);
                                dialog.dismiss();

                            }
                        })
                        .setNegativeButton("Quit", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                finish();
                                dialog.dismiss();
                            }
                        })
                        .setCancelable(false)
                        .show();
            }

            else //If haven't reach, display normal well done message and proceed to next level until hit the max level (level 5)
            {
//
                AlertDialog dialog = new AlertDialog.Builder(PlayActivity.this)
                        .setIcon(R.drawable.smiley)
                        .setTitle("Success")
                        .setMessage("You did it very well ! Time Spent : " + timeSpent + " second")
                        .setCancelable(false)
                        .create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    private static final int AUTO_DISMISS_MILLIS = 3000;
                    @Override
                    public void onShow(final DialogInterface dialog) {
                        new CountDownTimer(AUTO_DISMISS_MILLIS, 1000) {
                            @Override
                            public void onTick(long millisUntilFinished) {
                                ((AlertDialog) dialog).setTitle(String.format(Locale.getDefault(), "Success (%ds)", TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) + 1));
                            }
                            @Override
                            public void onFinish() {
                                if (((AlertDialog) dialog).isShowing()) {
                                    dialog.dismiss();
                                    currentLevel++;
                                    populateResources();
                                    startButton.performClick();
                                }
                            }
                        }.start();
                    }
                });
                dialog.show();
            }
            sound.playSuccessSound();
            isPlaying = false;
        }

        return false;
    }

    private void saveToDb_piano_1(long diff, int user_id, Integer score) {
        String url ="http://yiern.atspace.cc/AddPianoGameHistory.php";
        JSONObject dataJson = new JSONObject();

        try
        {
            dataJson.put("TimeSpent", diff);
            dataJson.put("user_id", user_id);
            dataJson.put("score",score);
            dataJson.put("failed",failed_attempts);
            dataJson.put("hand",hand_orientation);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //Retrieve records from database
        JsonObjectRequest json_obj_req = new JsonObjectRequest(Request.Method.POST, url, dataJson, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                Log.d("Retrieved - game4: ", response.toString());
            }

        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.d("Failed", error.getMessage());
                error.printStackTrace();
            }
        });

        requestQueue.add(json_obj_req);

    }


    private void saveToDb_piano(long diff_in_sec, int user_id) {
        String url = "http://yiern.atspace.cc/addPianoHistory.php";


        Log.d(TAG, "saveToDb_piano: number 2: "+ diff_in_sec);
        //Insert into database to keep number of play record
        JSONObject dataJson = new JSONObject();

        try
        {
            dataJson.put("TimeSpent", diff_in_sec);
            dataJson.put("user_id", user_id);
            dataJson.put("hand",hand_orientation);
        }
        catch(JSONException e)
        {
            Log.e(TAG, "saveToDb: error saving: ", e );
        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //Retrieve records from database
        JsonObjectRequest json_obj_req = new JsonObjectRequest(Request.Method.POST, url, dataJson, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                Log.d("Retrieved", response.toString());
            }

        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.d("Failed", error.getMessage());
                error.printStackTrace();
            }
        });

        requestQueue.add(json_obj_req);

    }


    //Save results to database
    private void saveToDb(final Integer result, final long timespent, final int user_id, final int level)
    {
        String url = "http://yiern.atspace.cc/AddGameHistory.php";
        //Insert into database to keep number of play record
        final JSONObject dataJson = new JSONObject();

        String str = instruction.getText().toString();

        String[] splitStr = str.split("\\s+");


        try
        {
            //dataJson.put("instruction", instruction.getText().toString());
            dataJson.put("finger", splitStr[1]);
            dataJson.put("degree", splitStr[3]);
            dataJson.put("result", result);
            dataJson.put("timespent", timespent);
            dataJson.put("user_id",user_id);
            dataJson.put("level",level);
            dataJson.put("hand",hand_orientation);
        }
        catch(JSONException e)
        {
            Log.e(TAG, "saveToDb: error saving: ", e );
        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        //Retrieve records from database
        JsonObjectRequest json_obj_req = new JsonObjectRequest(Request.Method.POST, url, dataJson, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                Log.d("Retrieved", response.toString());
            }

        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                Log.d("Failed", error.getMessage());
                error.printStackTrace();
            }
        });

        requestQueue.add(json_obj_req);

        if(level ==2 )
        {
            try
            {
                //dataJson.put("instruction", instruction.getText().toString());
                dataJson.put("finger", splitStr[6]);
                dataJson.put("degree", splitStr[8]);
                dataJson.put("result", result);
                dataJson.put("timespent", timespent);
                dataJson.put("user_id",user_id);
                dataJson.put("level",level);
                dataJson.put("hand",hand_orientation);
            }
            catch(JSONException e)
            {
                Log.e(TAG, "saveToDb: error saving: ", e );
            }

            RequestQueue requestQueue1 = Volley.newRequestQueue(this);
            //Retrieve records from database
            JsonObjectRequest json_obj_req1 = new JsonObjectRequest(Request.Method.POST, url, dataJson, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response)
                {
                    Log.d("Retrieved", response.toString());
                }

            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    Log.d("Failed", error.getMessage());
                    error.printStackTrace();
                }
            });
            requestQueue1.add(json_obj_req1);
        }
    }

    //Populate the level header and instruction text as well as the image
    private void populateResources()
    {
        Integer requiredSensor;
        Double requiredDegree1;
        Double requiredDegree2;

        levelHeader.setText("Level " + currentLevel);

        //Easy mode
        if(difficulty_level == 1)
        {

            requiredSensor = Integer.valueOf(easyDegree.get(currentLevel - 1).keySet().toArray()[0].toString());
            requiredDegree1 = Double.valueOf(easyDegree.get(currentLevel - 1).get(requiredSensor).toString());

            instruction.setText("Finger " + requiredSensor + " at " + requiredDegree1 + " degree");

            //Change the finger image based on level
            if (currentLevel == 1)
            {
                gifImagePath  = "<body><center><style>img{display: inline;max-height: 100%;max-width: 100%;}</style><img src = \"file:///android_res/drawable/thumb_right.gif\"/></center></body>";
                fingerImage.loadDataWithBaseURL("file:///android_asset/",gifImagePath,"text/html","UTF-8",null);
            }
            else if (currentLevel == 2)
            {
                gifImagePath  = "<body><center><style>img{display: inline;max-height: 100%;max-width: 100%;}</style><img src = \"file:///android_res/drawable/index_right.gif\"/></center></body>";
                fingerImage.loadDataWithBaseURL("file:///android_asset/",gifImagePath,"text/html","UTF-8",null);
            }
            else if (currentLevel == 3)
            {
                gifImagePath  = "<body><center><style>img{display: inline;max-height: 100%;max-width: 100%;}</style><img src = \"file:///android_res/drawable/middle_right.gif\"/></center></body>";
                fingerImage.loadDataWithBaseURL("file:///android_asset/",gifImagePath,"text/html","UTF-8",null);
            }
            else if (currentLevel == 4)
            {
                gifImagePath  = "<body><center><style>img{display: inline;max-height: 100%;max-width: 100%;}</style><img src = \"file:///android_res/drawable/ring_right.gif\"/></center></body>";
                fingerImage.loadDataWithBaseURL("file:///android_asset/",gifImagePath,"text/html","UTF-8",null);
            }
            else if (currentLevel == 5)
            {
                gifImagePath  = "<body><center><style>img{display: inline;max-height: 100%;max-width: 100%;}</style><img src = \"file:///android_res/drawable/pinky_right.gif\"/></center></body>";
                fingerImage.loadDataWithBaseURL("file:///android_asset/",gifImagePath,"text/html","UTF-8",null);
            }
            else
            {
                gifImagePath  = "<body><center><style>img{display: inline;max-height: 100%;max-width: 100%;}</style><img src = \"file:///android_res/drawable/easy.gif\"/></center></body>";
                fingerImage.loadDataWithBaseURL("file:///android_asset/",gifImagePath,"text/html","UTF-8",null);
            }
        }

        //Medium mode
        else if(difficulty_level == 2)
        {

            requiredDegree1 = Double.valueOf(mediumDegree.get(currentLevel - 1).get(0).toString());
            requiredDegree2 = Double.valueOf(mediumDegree.get(currentLevel - 1).get(1).toString());

            String requiredHand  = String.valueOf(currentLevel+1);

            instruction.setText("Finger 1 at " + requiredDegree1 + " degree\nFinger "+ requiredHand +" at " + requiredDegree2 + " degree");

            //Change the finger image based on level
            if(currentLevel == 1)
            {
                gifImagePath  = "<body><center><style>img{display: inline;max-height: 100%;max-width: 100%;}</style><img src = \"file:///android_res/drawable/indexthumb_right.gif\"/></center></body>";
                fingerImage.loadDataWithBaseURL("file:///android_asset/",gifImagePath,"text/html","UTF-8",null);
            }
            else if(currentLevel == 2)
            {
                gifImagePath  = "<body><center><style>img{display: inline;max-height: 100%;max-width: 100%;}</style><img src = \"file:///android_res/drawable/indexmiddle_right.gif\"/></center></body>";
                fingerImage.loadDataWithBaseURL("file:///android_asset/",gifImagePath,"text/html","UTF-8",null);
            }
            else if(currentLevel == 3)
            {
                gifImagePath  = "<body><center><style>img{display: inline;max-height: 100%;max-width: 100%;}</style><img src = \"file:///android_res/drawable/middlethumb_right.gif\"/></center></body>";
                fingerImage.loadDataWithBaseURL("file:///android_asset/",gifImagePath,"text/html","UTF-8",null);
            }
            else if(currentLevel == 4)
            {
                gifImagePath  = "<body><center><style>img{display: inline;max-height: 100%;max-width: 100%;}</style><img src = \"file:///android_res/drawable/fourthpinky_right.gif\"/></center></body>";
                fingerImage.loadDataWithBaseURL("file:///android_asset/",gifImagePath,"text/html","UTF-8",null);
            }
            else if(currentLevel == 5)
            {
                gifImagePath  = "<body><center><style>img{display: inline;max-height: 100%;max-width: 100%;}</style><img src = \"file:///android_res/drawable/fourthindex_right.gif\"/></center></body>";
                fingerImage.loadDataWithBaseURL("file:///android_asset/",gifImagePath,"text/html","UTF-8",null);
            }
            else
            {
                gifImagePath  = "<body><center><style>img{display: inline;max-height: 100%;max-width: 100%;}</style><img src = \"file:///android_res/drawable/medium.gif\"/></center></body>";
                fingerImage.loadDataWithBaseURL("file:///android_asset/",gifImagePath,"text/html","UTF-8",null);
            }
        }

    }

    class BluetoothDataReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            //Log.d("Received!", intent.getAction()); for debugging

            if(intent.getAction().equals("GET_BLUETOOTH_DATA"))
            {
                //Only process if the game is in playing status
                if(!isPlaying)
                {
                    return;
                }


                final String receivedDataString = intent.getStringExtra("DATA");

                mTxtReceive.post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        List<String> SensorDataList = Arrays.asList(receivedDataString.split(","));
                        Log.d(TAG, String.valueOf(SensorDataList.size()));
                        if(SensorDataList.size() == 5) {
                            mTxtReceive.append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()) + "\nThumb: " + SensorDataList.get(0) + "\nIndex : " + SensorDataList.get(1) + "\nMiddle : " + SensorDataList.get(2) + "\nRing : " + SensorDataList.get(3) + "\nPinky : " + SensorDataList.get(4) + "\n\n");//display the degree of both sensors index_right(0) && index_right(2) == Degree and index_right(1)&& index_right(3) == Ohms

                            int txtLength = mTxtReceive.getEditableText().length();
                            if (txtLength > mMaxChars) {
                                mTxtReceive.getEditableText().delete(0, txtLength - mMaxChars);
                            }

                            scrollView.post(new Runnable() //References - http://stackoverflow.com/a/4612082/1287554
                            {
                                @Override
                                public void run() {
                                    scrollView.fullScroll(View.FOCUS_DOWN);
                                }
                            });
                            gameLogic(receivedDataString);
                        }
                        else {
                            Log.d(TAG, "run: sensor size too small: "+SensorDataList.size());
                        }
                    }
                });

                //gameLogic(receivedDataString);

            }
            else if(intent.getAction().equals("BLUETOOTH_UPDATE"))
            {
                final boolean bluetoothConnected = intent.getBooleanExtra("DATA", false);

                //Log.d("test1222222", String.valueOf(bluetoothConnected)); for debugging

                progressDialog.dismiss();

                //If connected
                if(bluetoothConnected)
                {
                    new android.app.AlertDialog.Builder(PlayActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Success")
                            .setMessage("Successfully connected to the Bluetooth module")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.dismiss();
                                }

                            })
                            .setCancelable(false)
                            .show();
                }
                //Error occurred asdw
                else
                {
                    new android.app.AlertDialog.Builder(PlayActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Error")
                            .setMessage("Error connecting to the Bluetooth module")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                @Override
                                public void onClick(DialogInterface dialog, int which)
                                {
                                    dialog.dismiss();
                                }

                            })
                            .setCancelable(false)
                            .show();
                    Log.d(TAG, "onReceive: Error connecting");
                }
            }
            //Disconnected
            else if(intent.getAction() == BluetoothDevice.ACTION_ACL_DISCONNECTED)
            {


                
            }
        }

    }

}

