package com.example.bluetooth_testing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.LongDef;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;

/**
 * Created by kangs on 23/02/2020.
 */

public class LevelSelectionActivity extends Activity
{

    //Create button variable
    private Button btnLevelOne, btnLevelTwo, btnlevelThree,btnLevelFour;
    public static final String DIFFICULTY = "DIFFICULTY_LEVEL";
    int level_count;
    int level_check;
    StringRequest stringRequest_post;
    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_level_selection);
        Bundle extras = getIntent().getExtras();
        final int user_id = extras.getInt("user_id");


        //Initialize the button
        btnLevelOne = (Button) findViewById(R.id.btnOne);
        btnLevelTwo = (Button) findViewById(R.id.btnTwo);
        btnlevelThree = (Button) findViewById(R.id.btnThree);
        btnLevelFour = (Button) findViewById(R.id.btnFour);



      check_level(1);


        //Easy mode
        btnLevelOne.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                Intent intent = new Intent(getApplicationContext(), BluetoothManagementActivity.class);
                intent.putExtra(HomeActivity.INTENT_ACTION, "Play");
                intent.putExtra(DIFFICULTY, "1");
                intent.putExtra("user_id",user_id);
                savetoDB("1",user_id);
                startActivity(intent);
            }
        });

        //Medium mode
        btnLevelTwo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                Intent intent = new Intent(getApplicationContext(), BluetoothManagementActivity.class);
                intent.putExtra(HomeActivity.INTENT_ACTION, "Play");
                intent.putExtra(DIFFICULTY, "2");
                intent.putExtra("user_id",user_id);
                savetoDB("2",user_id);
                startActivity(intent);
            }
        });

        btnlevelThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BluetoothManagementActivity.class);
                intent.putExtra(HomeActivity.INTENT_ACTION, "Play");
                intent.putExtra(DIFFICULTY, "3");
                intent.putExtra("user_id",user_id);
                savetoDB("3",user_id);
                startActivity(intent);

            }
        });

        btnLevelFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), BluetoothManagementActivity.class);
                intent.putExtra(HomeActivity.INTENT_ACTION, "Play");
                intent.putExtra(DIFFICULTY, "4");
                intent.putExtra("user_id",user_id);
                savetoDB("4",user_id);
                startActivity(intent);

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }

    private void check_level(final int condition) {
        queue = Volley.newRequestQueue(this);

        String url_post = "http://yiern.atspace.cc/getData.php";
        Bundle extras = getIntent().getExtras();
        final int user_id = extras.getInt("user_id");

        Log.d(TAG, "level: " + condition + " User_id: " + user_id);

        stringRequest_post = new StringRequest(Request.Method.POST, url_post, new Response.Listener<String>() {     //https://developer.android.com/training/volley/simple,  https://www.itsalif.info/content/android-volley-tutorial-http-get-post-put

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: "+ response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    level_count = jsonObject.getInt("level");
                    Log.d(TAG, "level from json decode: " + level_count);

                } catch (JSONException e) {
                    Log.e(TAG, "onResponse: from json: ",e );
                }

                //find_level_condition(level_count);
                if(level_count < 5 )
                {
                    if(condition == 1)
                    {
                        btnLevelOne.setEnabled(true);
                        btnLevelTwo.setEnabled(false);
                        btnlevelThree.setEnabled(false);
                        btnLevelFour.setEnabled(false);
                        onStop();
                    }
                    else if (condition == 2)
                    {
                        btnLevelOne.setEnabled(true);
                        btnLevelTwo.setEnabled(true);
                        btnlevelThree.setEnabled(false);
                        btnLevelFour.setEnabled(false);
                        onStop();
                    }
                    else if (condition == 3)
                    {
                        btnLevelOne.setEnabled(true);
                        btnLevelTwo.setEnabled(true);
                        btnlevelThree.setEnabled(true);
                        btnLevelFour.setEnabled(false);
                        onStop();
                    }
                    else
                    {
                        btnLevelOne.setEnabled(true);
                        btnLevelTwo.setEnabled(true);
                        btnlevelThree.setEnabled(true);
                        btnLevelFour.setEnabled(true);
                        onStop();
                    }
                }
                else
                {
                    check_level(condition+1);
                }


            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "onErrorResponse: ",error );
            }
        })
        {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("level", String.valueOf(condition));
                params.put("user_id", String.valueOf(user_id));
                return params;
            }
        };
        stringRequest_post.setTag(TAG);
        queue.add(stringRequest_post);



    }




    private void savetoDB(String s, int user_id) {
        String url = "http://yiern.atspace.cc/AddLevelHistory.php";
        //Insert into database when play button is pressed to keep number of play record
        JSONObject dataJson = new JSONObject();
        String s_user_id = String.valueOf(user_id);
        try
        {
            dataJson.put("level", s);
            dataJson.put("user_id", s_user_id);
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

            }

        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {

                error.printStackTrace();
            }
        });

        requestQueue.add(json_obj_req);
    }
}
