package com.example.simone.arduino;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class ledSimone extends Activity {

    private static String btDeviceAddress = "20:14:08:13:26:79";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothSocket btSocket = null;
    private Handler handler = new Handler(); // serial stuff
    private InputStream inStream = null;
    private OutputStream outStream = null;
    private byte[] readBuffer = new byte[1024];
    private int readBufferPosition = 0;
    private byte lineDelimiter = 10;
    private boolean pauseSerialWorker = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_simone);
        SeekBar led=(SeekBar)findViewById(R.id.led);
        led.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.led_simone, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void writeData(String data) {
        if (this.btSocket == null) {
            return;
        } try { outStream = btSocket.getOutputStream(); }
        catch (IOException e) {  }
        try { outStream.write(data.getBytes()); }
        catch (IOException e) { }
    }




}
