package com.example.simone.arduino;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class carica extends Service  {
    private NotificationManager notificationManager;

    ///////////////////////////////////////////////////////
    public static String btDeviceAddress = "20:14:08:13:26:79";
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public BluetoothAdapter bluetoothAdapter = null;
    public BluetoothSocket btSocket = null;
    public Handler handler = new Handler();
    public InputStream inStream = null;
    public OutputStream outStream = null;
    public byte[] readBuffer = new byte[1024];
    public int readBufferPosition = 0;
    public byte lineDelimiter = '.';
    public boolean pauseSerialWorker = false;
    public boolean ledGiuseppeAcceso=false,ledSimoneAcceso=false;
    public Thread thread;
    public boolean isStandBy=false;
    public boolean inCharge=false;
    public int collegata;
    /////////////////////////////////////////////////////////////////



   public Main a;
    public carica() {

    }

    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {


            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);

            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            Log.d("service","broadcast"+level);
            if (level >= 81 && plugged==1) {
                Intent i = new Intent(getApplicationContext(), carica.class);
                if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();
                }
                Log.d("service","if");
                SystemClock.sleep(3000);
                bluetoothConnect();
                SystemClock.sleep(2000);
                inCharge=false;
                writeData("Y");
                writeData("s");
                SystemClock.sleep(2000);
                stopService(i);
            }
        }
    };


    @Override
    public void onCreate(){
        notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                carica.this);
        notificationBuilder.setContentTitle("Carica Cell");
        notificationBuilder.setContentText("In esecuzione");
        notificationBuilder.setTicker("Carica Cell avviato");


        notificationBuilder.setSmallIcon(R.drawable.ic_launcher);

        Intent notificationIntent = new Intent(this, Main.class);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);

        notificationBuilder.setContentIntent(contentIntent);
        notificationBuilder.setOngoing(true);
        notificationManager.notify(1, notificationBuilder.build());
        this.registerReceiver(this.batteryInfoReceiver,	new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }


    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onDestroy(){
        notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(1);

    }





    ////////////////////////////////////////////////////////////////
    private void resetConnection() {

        if (inStream != null) {
            try {inStream.close();} catch (Exception e) {}
            inStream = null;
        }

        if (outStream != null) {
            try {outStream.close();} catch (Exception e) {}
            outStream = null;
        }

        if (btSocket != null) {
            try {btSocket.close();} catch (Exception e) {}
            btSocket = null;
        }

    }
    public boolean bluetoothConnect(){



        resetConnection();
        BluetoothDevice device = bluetoothAdapter.getDefaultAdapter().getRemoteDevice("20:14:08:13:26:79");

        // make sure peer is defined as a valid device based on their MAC. If not then do it.
        if (device == null)
            device = bluetoothAdapter.getRemoteDevice("20:14:08:13:26:79");

        // Make an RFCOMM binding.
        try {btSocket = device.createRfcommSocketToServiceRecord(MY_UUID);
        } catch (Exception e1) {

            return false;

        }



        try {
            btSocket.connect();
        } catch (Exception e) {

            return false;

        }


        try {
            outStream = btSocket.getOutputStream();
            inStream  = btSocket.getInputStream();
        } catch (Exception e) {

            return false;
        }

        return true;
    }
    public void writeData(String data) {

        if (this.btSocket == null) {
            return;
        } try { outStream = btSocket.getOutputStream(); }
        catch (IOException e) {  }
        try { outStream.write(data.getBytes()); }
        catch (IOException e) { }
    }
    ///////////////////////////////////////////////////





















}
