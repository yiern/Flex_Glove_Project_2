package com.example.bluetooth_testing;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * Created by kangs on 23/02/2020.
 */

public class ViewDataActivity extends Activity
{
    //Create button variable
    private Button btnViewPlayTimes, btnViewHistory, btnMonitor;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_data);

        //Initialize the button
        btnViewPlayTimes = (Button) findViewById(R.id.btnViewPlay);
        btnViewHistory = (Button) findViewById(R.id.btnViewHistory);
        btnMonitor = (Button) findViewById(R.id.btnMonitor);

        btnViewPlayTimes.setOnClickListener(new View.OnClickListener() //start calling ViewPlayTimes Activity when btn in app is pressed
        {
            @Override
            public void onClick(View arg0)
            {
                Intent intent = new Intent(getApplicationContext(), ViewPlayTimesActivity.class);
                startActivity(intent);
            }
        });

        btnViewHistory.setOnClickListener(new View.OnClickListener() //start calling ViewHistory Activity when btn in app is pressed
        {
            @Override
            public void onClick(View arg0)
            {
                Intent intent = new Intent(getApplicationContext(), ViewHistoryActivity.class);
                startActivity(intent);
            }
        });

        //Start monitoring
        btnMonitor.setOnClickListener(new View.OnClickListener() //start calling BTManagement>MonitoringScreen Activity when btn in app is pressed
        {
            @Override
            public void onClick(View arg0)
            {
                Intent intent = new Intent(getApplicationContext(), BluetoothManagementActivity.class);
                intent.putExtra(HomeActivity.INTENT_ACTION, "Monitor");
                startActivity(intent);
            }
        });
    }

}
