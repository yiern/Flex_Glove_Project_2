package com.example.bluetooth_testing;

import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import androidx.annotation.Nullable;

public class BluetoothDataService extends Service {

    private static boolean isRunning;

    private static final String TAG = "BlueTest5-BluetoothManagementActivity";
    private int mMaxChars = 50000;//Default
    private UUID mDeviceUUID;
    private BluetoothSocket mBTSocket;
    private ReadInput mReadThread = null;

    private boolean mIsUserInitiatedDisconnect = false;

    final int handlerState = 0;//used to identify handler message
    Handler bluetoothIn;
    private BluetoothAdapter btAdapter = null;

    private boolean mIsBluetoothConnected = false;

    private BluetoothDevice mDevice;

    private ProgressDialog progressDialog;

    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d("BT SERVICE", "SERVICE CREATED"+ mDevice);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        mDevice = Objects.requireNonNull(intent.getExtras()).getParcelable(BluetoothManagementActivity.DEVICE_EXTRA);
        mDeviceUUID = UUID.fromString(intent.getExtras().getString(BluetoothManagementActivity.DEVICE_UUID));
        mMaxChars = intent.getExtras().getInt(BluetoothManagementActivity.BUFFER_SIZE);

        Log.d("BT SERVICE", "SERVICE STARTED" + mDevice);
        new ConnectBT().execute();
        isRunning = true;
        return super.onStartCommand(intent, flags, startId);
    }

    private class DisConnectBT extends AsyncTask<Void, Void, Void>
    {
        @Override
        protected void onPreExecute()
        {

        }

        @Override
        protected Void doInBackground(Void... params)
        {

            if (mReadThread != null)
            {
                mReadThread.stop();
                while (mReadThread.isRunning()); // Wait until it stops
                mReadThread = null;
            }

            try
            {
                mBTSocket.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);
            mIsBluetoothConnected = false;
            if (mIsUserInitiatedDisconnect)
            {
                //finish();
            }
        }

    }

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy()
    {
        new DisConnectBT().execute();
        isRunning = false;
        super.onDestroy();
    }

    public static boolean isRunning()
    {
        return isRunning;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void>
    {
        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute()
        {
            //progressDialog = ProgressDialog.show(BluetoothDataService.this, "Hold on", "Connecting");// http://stackoverflow.com/a/11130220/1287554
        }

        @Override
        protected Void doInBackground(Void... devices)
        {
            try
            {
                if (mBTSocket == null || !mIsBluetoothConnected)
                {
                    mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mBTSocket.connect();
                }
            }
            catch (IOException e)
            {
                // Unable to connect to device
                e.printStackTrace();
                mConnectSuccessful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result)
        {
            super.onPostExecute(result);

            //Broadcast the received data to activity
            Intent sendLevel = new Intent();
            sendLevel.setAction("BLUETOOTH_UPDATE");
            sendLevel.putExtra( "DATA",mConnectSuccessful);
            sendBroadcast(sendLevel);

            if (!mConnectSuccessful)
            {
                Toast.makeText(getApplicationContext(), "Could not connect to device. Is it a Serial device? Also check if the UUID is correct in the settings", Toast.LENGTH_LONG).show();
            }
            else
            {
                msg("Connected to device");
                mIsBluetoothConnected = true;
                mReadThread = new ReadInput(); // Kick off input reader
            }
            //progressDialog.dismiss();
        }

    }

    public class ReadInput implements Runnable
    {
        private boolean bStop = false;
        private Thread t;

        public ReadInput()
        {
            t = new Thread(this, "Input Thread");
            t.start();
        }

        public boolean isRunning()
        {
            return t.isAlive();
        }

        @Override
        public void run()
        {
            InputStream inputStream;

            try
            {
                inputStream = mBTSocket.getInputStream();
                while (!bStop)
                {
                    byte[] buffer = new byte[256];
                    if (inputStream.available() > 0)
                    {
                        inputStream.read(buffer);
                        int i = 0;
                        /*
                         * This is needed because new String(buffer) is taking the entire buffer i.e. 256 chars on Android 2.3.4 http://stackoverflow.com/a/8843462/1287554
                         */
                        for (i = 0; i < buffer.length && buffer[i] != 0; i++)
                        {

                        }
                        final String strInput = new String(buffer, 0, i);

                        //Log.d("testing", strInput); for debugging

                        //Only send the signal if we receive completed json string from arduino
                        if(isValidData(strInput))
                        {
                            List<String> SensorDataList = Arrays.asList(strInput.split(","));

                            //Broadcast the received data to activity
                            Intent sendLevel = new Intent();
                            sendLevel.setAction("GET_BLUETOOTH_DATA");
                            sendLevel.putExtra( "DATA",strInput);
                            sendBroadcast(sendLevel);
                        }
                    }
                    Thread.sleep(500);
                }
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            catch (InterruptedException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        public void stop()
        {
            bStop = true;
        }

        //Check if the receive input is valid json string
        public boolean isValidData(String str)
        {
            List<String> DataList = Arrays.asList(str.split(",")); //conversion of array to list

            //Must have 4 element
            //1 & 2 = sensor 1
            //3 & 4 = sensor 2
            if(DataList.size() < 4)
            {
                return false;
            }
            return true;
        }

    }
}