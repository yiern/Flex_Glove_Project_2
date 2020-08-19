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
        btnMonitor = (Button) findViewById(R.id.btnMonitor);

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
