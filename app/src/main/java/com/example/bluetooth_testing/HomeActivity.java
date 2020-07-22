package com.example.bluetooth_testing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class HomeActivity extends Activity
{
    //Create button variable
    private Button btnPlay, btnViewData, btnInstruction, btnQuit;
    private TextView WelcomeMessage;
    public static final String INTENT_ACTION = "INTENT_ACTION";
    public static String URL;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        //Initiate the url
        URL ="https://yiern.atspace.cc/rest.php";//"http://192.168.1.236/game/rest.php"; "http://192.168.43.66:81/game/rest.php"

        //Initialize the button
        btnPlay = (Button) findViewById(R.id.btnPlay);
        btnViewData = (Button) findViewById(R.id.btnViewData);
        btnInstruction = (Button) findViewById(R.id.btnInstruction);
        btnQuit = (Button) findViewById(R.id.btnQuit);
        WelcomeMessage = (TextView) findViewById(R.id.tvWelcome);
        Bundle extras = getIntent().getExtras();
        final int user_id = extras.getInt("user_id");
        final String name = extras.getString("name");

        WelcomeMessage.setText("Welcome Back, "+name+ "!");
        //Start the game
        btnPlay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                //Insert into database when play button is being pressed to keep number of play record
                saveToDb();

                Intent intent = new Intent(getApplicationContext(), LevelSelectionActivity.class);
                intent.putExtra("user_id",user_id);
                startActivity(intent);
            }
        });

        //View data
        btnViewData.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                Intent intent = new Intent(getApplicationContext(), ViewDataActivity.class);
                intent.putExtra("user_id",user_id);
                startActivity(intent);
            }
        });

        //View instruction
        btnInstruction.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                Intent intent = new Intent(getApplicationContext(), InstructionActivity.class);
                intent.putExtra("user_id",user_id);
                startActivity(intent);
            }
        });

        //Quit app
        btnQuit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                //References - https://stackoverflow.com/questions/2478517/how-to-display-a-yes-no-dialog-box-on-android
                new AlertDialog.Builder(HomeActivity.this).setTitle("Logout?")
                        .setMessage("Are you sure?")
                        .setPositiveButton("YES",
                                new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which)
                                    {
                                        //Perform Action & Dismiss dialog
                                        dialog.dismiss();
                                        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
                                        HomeActivity.this.startActivity(intent);
                                        //finish();
                                        //System.exit(0);
                                    }
                                })
                        .setNegativeButton("NO", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                // Do nothing
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
        });

    }

    //Save results to database
    private void saveToDb()
    {
        Bundle extras = getIntent().getExtras();
        final int user_id = extras.getInt("user_id");
        String url = "http://yiern.atspace.cc/AddPlayHistory.php";
        //Insert into database when play button is pressed to keep number of play record
        JSONObject dataJson = new JSONObject();
        try
        {
            dataJson.put("user_id", user_id);
        }
        catch(JSONException e)
        {

        }

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Retrieve records from backend database
        JsonObjectRequest json_obj_req = new JsonObjectRequest(Request.Method.POST, url, dataJson, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                //Log.d("test5", response.toString()); for debugging
            }

        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                //Log.d("testt6", error.toString()); for debugging
                error.printStackTrace();
            }
        });

        requestQueue.add(json_obj_req);
    }
}
