package com.example.bluetooth_testing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class ViewHistoryActivity extends Activity
{
    private ActivityAdapater recordAdapter;

    ListView listView;
    ArrayList<HashMap<String, String>> recordArray;

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_game_history);

        listView = (ListView) findViewById(R.id.listView);

        recordArray = new ArrayList<>();

        retrieveFromDB();

        recordAdapter = new ActivityAdapater(this);
        listView.setAdapter(recordAdapter);

        progressDialog = ProgressDialog.show(ViewHistoryActivity.this, "Hold on", "Retrieving data from server");//references - http://stackoverflow.com/a/11130220/1287554
    }

    //Custom list view adapater
    private class ActivityAdapater extends BaseAdapter
    {
        private LayoutInflater inflater=null;
        private Context mContext;

        ActivityAdapater(Context _mContext)
        {
            mContext = _mContext;
            inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount()
        {
            return recordArray.size();
        }

        @Override
        public Object getItem(int position)
        {
            return recordArray.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            View view=convertView;
            if(convertView==null)
            {
                view = inflater.inflate(R.layout.list_game_history, null);
            }

            TextView tvTiming =(TextView)view.findViewById(R.id.tvTiming);
            TextView tvInstruction = (TextView)view.findViewById(R.id.tvInstruction);
            TextView tvResult = (TextView)view.findViewById(R.id.tvResult);

            tvTiming.setText(recordArray.get(position).get("timing"));
            tvInstruction.setText(recordArray.get(position).get("instruction"));

            //References - https://apps.timwhitlock.info/emoji/tables/unicode
            if(recordArray.get(position).get("result").equals("1"))
            {
                tvResult.setText(new String(Character.toChars(0x1F601)));//Smiley face
            }
            else
            {
                tvResult.setText(new String(Character.toChars(0x1F622)));//Sad face
            }

            return view;
        }

    }

    //Retrieve record from database
    private void retrieveFromDB()
    {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        JSONObject dataJson = new JSONObject();
        try
        {
            dataJson.put("action", "retrieve_game_history");
        }
        catch(JSONException e)
        {
            System.out.print(e);
        }

        JsonObjectRequest json_obj_req = new JsonObjectRequest(Request.Method.POST, HomeActivity.URL, dataJson, new Response.Listener<JSONObject>()
        {
            @Override
            public void onResponse(JSONObject response)
            {
                try
                {

                    //Log.d("testz", response.toString()); for debugging
                    if(response.getInt("status")==1)
                    {
                        // looping through records
                        for (int i = 0; i < response.getJSONArray("data").length(); i++)
                        {
                            JSONObject c = response.getJSONArray("data").getJSONObject(i);

                            Log.d("testx", c.toString()); //for debugging

                            // Storing each json item in variable
                            String instruction= c.getString("instruction");
                            Integer result  = c.getInt("result");
                            Integer timing  = c.getInt("time_spent");


                            // creating new HashMap
                            HashMap<String, String> record = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            record.put("timing", timing + "s");
                            record.put("instruction", instruction);
                            record.put("result", String.valueOf(result));

                            recordArray.add(record);

                            Log.d("testc", recordArray.toString()); //for debugging
                        }

                        recordAdapter.notifyDataSetChanged();
                    }
                }
                catch (JSONException e)
                {
                   // Log.d("testv", e.getMessage()); //for debugging
                    //e.printStackTrace();

                }

                progressDialog.dismiss();
            }

        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
                progressDialog.dismiss();
                new android.app.AlertDialog.Builder(ViewHistoryActivity.this)
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setTitle("Error")
                        .setMessage("Unable to retrieve records")
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
                error.printStackTrace();
            }

        });
        requestQueue.add(json_obj_req);
    }
}

