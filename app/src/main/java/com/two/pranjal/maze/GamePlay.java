package com.two.pranjal.maze;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class GamePlay extends AppCompatActivity implements SensorEventListener {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;
    private Integer images[] = {R.drawable.pic1, R.drawable.pic2, R.drawable.pic3, R.drawable.pic4};
    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;

    private View mContentView;
    private View mControlsView;
    private boolean mVisible;


    private static final String TAG = "Jon";
    private BluetoothAdapter mBluetoothAdapter = null;
    private BluetoothSocket btSocket = null;
    private OutputStream outStream = null;
    private static String address = "20:15:03:17:39:66";
    private static final UUID MY_UUID = UUID
            .fromString("00001101-0000-1000-8000-00805F9B34FB");
    private long lastUpdate = 0;



    private SensorManager mgr;      //sensor manager
    private Sensor acele;            //accelerometer
    private TextView text;


    ImageView imageView;
    ImageView imageView2;
    ImageView imageView3;
    ImageView imageView4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_game_play);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });


        mgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        acele = mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        text = (TextView) findViewById(R.id.text);



        CheckBt();
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        Log.e("Jon", device.toString());
        Connect();

        imageView = (ImageView) findViewById(R.id.image1);
        imageView.setImageResource(images[0]);
        imageView2 = (ImageView) findViewById(R.id.image2);
        imageView2.setImageResource(images[1]);
        imageView3 = (ImageView) findViewById(R.id.image3);
        imageView3.setImageResource(images[2]);
        imageView4 = (ImageView) findViewById(R.id.image4);
        imageView4.setImageResource(images[3]);
        imageView.setVisibility(View.INVISIBLE);
        imageView2.setVisibility(View.INVISIBLE);
        imageView3.setVisibility(View.INVISIBLE);
        imageView4.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    @Override
    protected void onResume()
    {
        mgr.registerListener(this, acele, SensorManager.SENSOR_DELAY_GAME);
        super.onResume();
    }


    @Override
    protected void onPause()
    {
        mgr.unregisterListener(this, acele);
        super.onPause();
    }

    //required by the listener
    public void onAccuracyChanged(Sensor sensor, int accuracy)
    {

    }

    //figuring out x and y direction based on the readings from the accelerometer
    public void onSensorChanged(SensorEvent event)
    {
        double alpha = 0.8;
        double[] gravity = new double[3];

        // Isolate the force of gravity with the low-pass filter.
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];

        // Remove the gravity contribution with the high-pass filter.
        double mSensorX = event.values[0] - gravity[0];
        double mSensorY = event.values[1] - gravity[1];


        String message;
        String m1;
        String m2;
        String display1="Center";
        String display2="Center";
        String displayMessage;
        if(((int)mSensorX)>-1)
        {
            m1= "0"+Integer.toString((int) mSensorX);
            if(((int)mSensorX)==0)
            {
                imageView.setVisibility(View.INVISIBLE);
                imageView2.setVisibility(View.INVISIBLE);
                display1="Center";
            }
            else {
                imageView2.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.INVISIBLE);
                display1="Bottom";
            }
        }
        else
        {
            m1= Integer.toString((int) mSensorX);
            imageView.setVisibility(View.VISIBLE);
            imageView2.setVisibility(View.INVISIBLE);
            display1="Top";
        }
        if(((int)mSensorY)>-1)
        {
            m2= "0"+Integer.toString((int) mSensorY);
            if(((int)mSensorY)==0)
            {
                imageView4.setVisibility(View.INVISIBLE);
                imageView3.setVisibility(View.INVISIBLE);
                display2="Center";
            }
            else {
                imageView4.setVisibility(View.VISIBLE);
                imageView3.setVisibility(View.INVISIBLE);
                display2="Right";
            }
        }
        else
        {
            m2= Integer.toString((int) mSensorY);
            imageView3.setVisibility(View.VISIBLE);
            imageView4.setVisibility(View.INVISIBLE);
            display2="Left";
        }
        message = m1+ m2;
        displayMessage = display1+ " - "+ display2 + "\n\n" + "X: " + (int)mSensorX + " Y: " +(int)mSensorY;


        long curTime = System.currentTimeMillis();

        if ((curTime - lastUpdate) > 1000) {                        //change this 1000 to anything you want
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;
            output(message);
        }
        Button FinalDisplay= (Button)findViewById(R.id.textView);
        FinalDisplay.setText(displayMessage);

    }


    private void output(String message)
    {
        try
        {
            outStream= btSocket.getOutputStream();
        }
        catch (IOException e)
        {
            Log.d(TAG,"Bug can't send stuff",e);
        }

        byte[] msgBuffer = message.getBytes();

        try
        {
            outStream.write(msgBuffer);
        }
        catch (IOException e)
        {
            Log.d(TAG, "Bug while sending stuff", e);
        }
    }




    private void CheckBt() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Enabling Bluetooth and Connecting",
                    Toast.LENGTH_SHORT).show();
            Connect();
        }

        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),
                    "Bluetooth null !", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void Connect() {
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        Log.d("", "Connecting to ... " + device);
        mBluetoothAdapter.cancelDiscovery();
        try
        {
            btSocket = device.createRfcommSocketToServiceRecord(MY_UUID); //Here is the part the connection is made, by asking the device to create a RfcommSocket (Unsecure socket I guess), It map a port for us or something like that
            btSocket.connect();
            Log.d("", "Connection made.");
            Toast.makeText(getApplicationContext(), "Connection Made",Toast.LENGTH_LONG).show();
        }
        catch (IOException e)
        {
            try
            {
                btSocket.close();
            }
            catch (IOException e2)
            {
                Log.d("", "Unable to end the connection");
            }
            Log.d("", "Socket creation failed");
        }

        //beginListenForData();     // this is a method used to read what the Arduino says for example when you write Serial.print("Hello world.") in your Arduino code


    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };

    private final Handler mHideHandler = new Handler();
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
