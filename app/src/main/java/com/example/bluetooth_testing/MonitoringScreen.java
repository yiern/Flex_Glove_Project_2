package com.example.bluetooth_testing;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MonitoringScreen extends Activity
{
    private  final String TAG = "BluetoothManagementA";
    private int mMaxChars = 50000;//Default
    private BluetoothDevice mDevice;
    private UUID mDeviceUUID;
    private ProgressDialog progressDialog;
    BluetoothDataReceiver receiver;

    // All controls here
    private TextView mTxtReceive;
    private Button mBtnClearInput;
    private ScrollView scrollView;
    private CheckBox chkScroll;
    private CheckBox chkReceiveText;


    private boolean mIsBluetoothConnected = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitoring_screen);
        ActivityHelper.initialize(this);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        mDevice = b.getParcelable(BluetoothManagementActivity.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(b.getString(BluetoothManagementActivity.DEVICE_UUID));
        mMaxChars = b.getInt(BluetoothManagementActivity.BUFFER_SIZE);

        mTxtReceive = (TextView) findViewById(R.id.txtReceive);
        chkScroll = (CheckBox) findViewById(R.id.chkScroll);
        chkReceiveText = (CheckBox) findViewById(R.id.chkReceiveText);
        scrollView = (ScrollView) findViewById(R.id.viewScroll);
        mBtnClearInput = (Button) findViewById(R.id.btnClearInput);
        mTxtReceive.setMovementMethod(new ScrollingMovementMethod());

        mBtnClearInput.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mTxtReceive.setText("");
            }
        });
    }

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause()
    {
        Log.d(TAG, "Paused");
        super.onPause();
    }

    @Override
    protected void onResume()
    {
        Log.d(TAG, "Resumed");
        super.onResume();
    }

    @Override
    protected void onStart()
    {
        //Register a broadcast receiver to receive signal from bluetooth service
        //https://stackoverflow.com/questions/9128103/broadcastreceiver-with-multiple-filters-or-multiple-broadcastreceivers
        receiver = new BluetoothDataReceiver();
        registerReceiver(receiver, new IntentFilter("GET_BLUETOOTH_DATA"));
        registerReceiver(receiver, new IntentFilter("BLUETOOTH_UPDATE"));
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED));
        registerReceiver(receiver, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED));

        Intent bluetoothDataServiceIntent = new Intent(this,BluetoothDataService.class);
        bluetoothDataServiceIntent.putExtra(BluetoothManagementActivity.DEVICE_UUID, mDeviceUUID.toString());
        bluetoothDataServiceIntent.putExtra(BluetoothManagementActivity.DEVICE_EXTRA, mDevice);
        bluetoothDataServiceIntent.putExtra(BluetoothManagementActivity.BUFFER_SIZE, mMaxChars);

        if(!BluetoothDataService.isRunning()){
            progressDialog = ProgressDialog.show(MonitoringScreen.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554
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

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);
    }

    class BluetoothDataReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {

            //Log.d("test1", intent.getAction()); for debugging

            if(intent.getAction().equals("GET_BLUETOOTH_DATA"))
            {
                final String receivedDataString = intent.getStringExtra("DATA");

                //Log.d("test2", receivedDataString); for debugging

                if (chkReceiveText.isChecked())
                {
                    mTxtReceive.post(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            mTxtReceive.append(receivedDataString);

                            int txtLength = mTxtReceive.getEditableText().length();
                            if(txtLength > mMaxChars)
                            {
                                mTxtReceive.getEditableText().delete(0, txtLength - mMaxChars);
                            }

                            if (chkScroll.isChecked()) // Scroll only if this is checked
                            {
                                scrollView.post(new Runnable() // Snippet from http://stackoverflow.com/a/4612082/1287554
                                {
                                    @Override
                                    public void run()
                                    {
                                        scrollView.fullScroll(View.FOCUS_DOWN);
                                    }
                                });
                            }
                        }
                    });
                }
            }
            else if(intent.getAction().equals("BLUETOOTH_UPDATE"))
            {
                final boolean bluetoothConnected = intent.getBooleanExtra("DATA", false);

                //Log.d("test3", String.valueOf(bluetoothConnected)); for debugging

                progressDialog.dismiss();

                //If connected
                if(bluetoothConnected)
                {
                    new android.app.AlertDialog.Builder(MonitoringScreen.this)
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
                else
                {
                    new android.app.AlertDialog.Builder(MonitoringScreen.this)
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
                }
            }
            else if(intent.getAction() == BluetoothDevice.ACTION_ACL_DISCONNECTED)//If disconnected
            {
                new android.app.AlertDialog.Builder(MonitoringScreen.this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Error")
                        .setMessage("Blueetooth Connection Disconnected")
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
        }

    }
}