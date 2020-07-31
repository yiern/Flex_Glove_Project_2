package com.example.bluetooth_testing;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;


public class BluetoothManagementActivity extends Activity
{
    private Button search;
    private Button connect;
    private ListView listView;
    private BluetoothAdapter mBTAdapter;
    private static final int BT_ENABLE_REQUEST = 10; // This is the code we use for BT Enable
    private static final int SETTINGS = 20;
    private UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private int mBufferSize = 50000; //Default
    public static final String DEVICE_EXTRA = "com.example.bluetooth_testing.SOCKET";
    public static final String DEVICE_UUID = "com.example.bluetooth_testing.uuid";
    private static final String DEVICE_LIST = "com.example.bluetooth_testing.devicelist";
    private static final String DEVICE_LIST_SELECTED = "com.example.bluetooth_testing.devicelistselected";
    public static final String BUFFER_SIZE = "com.example.bluetooth_testing.buffersize";
    private static final String TAG = "BluetoothManagementA";
    private int user_id_2, left_hand_flag,right_hand_flag;
    Button left_hand_button,right_hand_button;
    BluetoothDevice Left_hand_MAC,right_hand_MAC;
    String handOrientation;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        connect = findViewById(R.id.connect);
        left_hand_button = findViewById(R.id.bt_bt4);
        left_hand_button.setEnabled(false);
        right_hand_button = findViewById(R.id.button7);
        right_hand_button.setEnabled(false);
        connect.setEnabled(false);

        listView = findViewById(R.id.listview);

        Bundle extras = getIntent().getExtras();
        final int user_id = extras.getInt("user_id");
        user_id_2=user_id;

        if (savedInstanceState != null)
        {
            ArrayList<BluetoothDevice> list = savedInstanceState.getParcelableArrayList(DEVICE_LIST);
            if (list != null)
            {
                initList(list);
                MyAdapter adapter = (MyAdapter) listView.getAdapter();
                int selectedIndex = savedInstanceState.getInt(DEVICE_LIST_SELECTED);
                if (selectedIndex != -1)
                {
                    adapter.setSelectedIndex(selectedIndex);
                    connect.setEnabled(true);
                }
            }
            else
            {
                initList(new ArrayList<BluetoothDevice>());
            }

        }
        else
        {
            initList(new ArrayList<BluetoothDevice>());
        }

        mBTAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBTAdapter == null)
        {
            Toast.makeText(getApplicationContext(), "Bluetooth not found", Toast.LENGTH_SHORT).show();
        }
        else if (!mBTAdapter.isEnabled())
        {
            Intent enableBT = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBT, BT_ENABLE_REQUEST);
        }
        else
        {
            new SearchDevices().execute();
        }

        connect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                Intent intent = getIntent();
                Bundle b = intent.getExtras();
                String action = b.getString(HomeActivity.INTENT_ACTION);

                BluetoothDevice device = ((MyAdapter) (listView.getAdapter())).getSelectedItem();

                Intent newIntent;
                //Determine to redirect to which page
                //Monitor or Play
                if(action.equals("Monitor"))
                {
                    newIntent = new Intent(getApplicationContext(), MonitoringScreen.class);
                }
                else
                {
                    newIntent = new Intent(getApplicationContext(), PlayActivity.class);
                    newIntent.putExtra(LevelSelectionActivity.DIFFICULTY, b.getString(LevelSelectionActivity.DIFFICULTY));
                    newIntent.putExtra("user_id",user_id);
                }

                newIntent.putExtra(DEVICE_EXTRA, device);
                newIntent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                newIntent.putExtra(BUFFER_SIZE, mBufferSize);
                finish();//Kill current activity
                startActivity(newIntent);
            }
        });

        left_hand_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                Bundle b = intent.getExtras();
                String action = b.getString(HomeActivity.INTENT_ACTION);



                Intent newIntent;
                //Determine to redirect to which page
                //Monitor or Play
                if(action.equals("Monitor"))
                {
                    newIntent = new Intent(getApplicationContext(), MonitoringScreen.class);
                }
                else
                {
                    newIntent = new Intent(getApplicationContext(), PlayActivity.class);
                    newIntent.putExtra(LevelSelectionActivity.DIFFICULTY, b.getString(LevelSelectionActivity.DIFFICULTY));
                    newIntent.putExtra("user_id",user_id);
                }
                newIntent.putExtra("hand orientation",handOrientation);
                newIntent.putExtra(DEVICE_EXTRA, Left_hand_MAC);
                newIntent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                newIntent.putExtra(BUFFER_SIZE, mBufferSize);
                finish();//Kill current activity
                startActivity(newIntent);
            }
        });
        right_hand_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = getIntent();
                Bundle b = intent.getExtras();
                String action = b.getString(HomeActivity.INTENT_ACTION);

                BluetoothDevice device = ((MyAdapter) (listView.getAdapter())).getSelectedItem();

                Intent newIntent;
                //Determine to redirect to which page
                //Monitor or Play
                if(action.equals("Monitor"))
                {
                    newIntent = new Intent(getApplicationContext(), MonitoringScreen.class);
                }
                else
                {
                    newIntent = new Intent(getApplicationContext(), PlayActivity.class);
                    newIntent.putExtra(LevelSelectionActivity.DIFFICULTY, b.getString(LevelSelectionActivity.DIFFICULTY));
                    newIntent.putExtra("user_id",user_id);
                }
                newIntent.putExtra("hand orientation",handOrientation);
                newIntent.putExtra(DEVICE_EXTRA, right_hand_MAC);
                newIntent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                newIntent.putExtra(BUFFER_SIZE, mBufferSize);
                finish();//Kill current activity
                startActivity(newIntent);

            }
        });
    }

    protected void onPause()
    {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onStop()
    {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case BT_ENABLE_REQUEST:
                if (resultCode == RESULT_OK)
                {
                    msg("Bluetooth Enabled successfully");
                    new SearchDevices().execute();
                }
                else
                {
                    msg("Bluetooth couldn't be enabled");
                }

                break;
            case SETTINGS: //If the settings have been updated
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                String uuid = prefs.getString("prefUuid", "Null");
                mDeviceUUID = UUID.fromString(uuid);
                Log.d(TAG, "UUID: " + uuid);
                String bufSize = prefs.getString("prefTextBuffer", "Null");
                mBufferSize = Integer.parseInt(bufSize);

                String orientation = prefs.getString("prefOrientation", "Null");
                //Log.d("test4", "Orientation: " + orientation); for debugging
                if (orientation.equals("Landscape"))
                {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
                else if (orientation.equals("Portrait"))
                {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                }
                else if (orientation.equals("Auto"))
                {
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Quick way to call the Toast
     * @param str
     */
    private void msg(String str)
    {
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }

    /**
     * Initialize the List of BT adapter Devices
     * @param objects
     */
    private void initList(List<BluetoothDevice> objects)
    {
        final MyAdapter adapter = new MyAdapter(getApplicationContext(), R.layout.list_item, R.id.lstContent, objects);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                adapter.setSelectedIndex(position);
                connect.setEnabled(true);
            }
        });
    }

    /**
     * Searches for paired devices. Only devices which are paired through Settings->Bluetooth
     */
    private class SearchDevices extends AsyncTask<Void, Void, List<BluetoothDevice>>
    {
        @Override
        protected List<BluetoothDevice> doInBackground(Void... params)
        {
            Set<BluetoothDevice> pairedDevices = mBTAdapter.getBondedDevices();
            List<BluetoothDevice> listDevices = new ArrayList<BluetoothDevice>();
            for (BluetoothDevice device : pairedDevices)
            {
                if(device.getName().equals("BT04-A"))
                {
                    left_hand_flag =1;
                    Left_hand_MAC = device;
                    handOrientation = "L";

                }
                if(device.getName().equals("BT5"))
                {
                    right_hand_flag = 1;
                    right_hand_MAC = device;
                    handOrientation = "R";
                }
                listDevices.add(device);
            }
            return listDevices;
        }

        @Override
        protected void onPostExecute(List<BluetoothDevice> listDevices)
        {
            super.onPostExecute(listDevices);
            if (listDevices.size() > 0)
            {
                MyAdapter adapter = (MyAdapter) listView.getAdapter();
                adapter.replaceItems(listDevices);
                if(left_hand_flag ==1)
                {
                    left_hand_button.setEnabled(true);

                }
                if (right_hand_flag == 1)
                {
                    right_hand_button.setEnabled(true);
                }
            }
            else
            {
                msg("No paired devices found, please pair your serial BT device and try again");
            }
        }

    }

    /**
     * Custom adapter to show the current devices in the list.
     */
    private class MyAdapter extends ArrayAdapter<BluetoothDevice> {
        private int selectedIndex;
        private Context context;
        private int selectedColor = Color.parseColor("#abcdef");
        private List<BluetoothDevice> myList;

        public MyAdapter(Context ctx, int resource, int textViewResourceId, List<BluetoothDevice> objects) {
            super(ctx, resource, textViewResourceId, objects);
            context = ctx;
            myList = objects;
            selectedIndex = -1;
        }

        public void setSelectedIndex(int position)
        {
            selectedIndex = position;
            notifyDataSetChanged();
        }

        public BluetoothDevice getSelectedItem()
        {
            return myList.get(selectedIndex);
        }

        @Override
        public int getCount()
        {
            return myList.size();
        }

        @Override
        public BluetoothDevice getItem(int position)
        {
            return myList.get(position);
        }

        @Override
        public long getItemId(int position)
        {
            return position;
        }

        private class ViewHolder
        {
            TextView tv;
        }

        public void replaceItems(List<BluetoothDevice> list)
        {
            myList = list;
            notifyDataSetChanged();
        }

        public List<BluetoothDevice> getEntireList()
        {
            return myList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View vi = convertView;
            ViewHolder holder;
            if (convertView == null)
            {
                vi = LayoutInflater.from(context).inflate(R.layout.list_item, null);
                holder = new ViewHolder();

                holder.tv = vi.findViewById(R.id.lstContent);

                vi.setTag(holder);
            }
            else
            {
                holder = (ViewHolder) vi.getTag();
            }

            if (selectedIndex != -1 && position == selectedIndex)
            {
                holder.tv.setBackgroundColor(selectedColor);
            }
            else
            {
                holder.tv.setBackgroundColor(Color.WHITE);
            }
            BluetoothDevice device = myList.get(position);
            holder.tv.setText(device.getName() + "\n " + device.getAddress());
            Button connect = findViewById(R.id.connect);
            connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = getIntent();
                    Bundle b = intent.getExtras();
                    String action = b.getString(HomeActivity.INTENT_ACTION);

                    BluetoothDevice device = ((MyAdapter) (listView.getAdapter())).getSelectedItem();

                    Intent newIntent;

                    //Determine to redirect to which page
                    //Monitor or Play
                    if(action.equals("Monitor"))
                    {
                        newIntent = new Intent(getApplicationContext(), MonitoringScreen.class);
                    }
                    else
                    {
                        newIntent = new Intent(getApplicationContext(), PlayActivity.class);
                        newIntent.putExtra(LevelSelectionActivity.DIFFICULTY, b.getString(LevelSelectionActivity.DIFFICULTY));
                        newIntent.putExtra("user_id",user_id_2);
                    }

                    newIntent.putExtra(DEVICE_EXTRA, device);
                    newIntent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                    newIntent.putExtra(BUFFER_SIZE, mBufferSize);
                    finish();//Kill current activity
                    startActivity(newIntent);
                }
            });

            return vi;
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.homescreen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_settings:
                Intent intent = new Intent(BluetoothManagementActivity.this, PreferencesActivity.class);
                startActivityForResult(intent, SETTINGS);
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}