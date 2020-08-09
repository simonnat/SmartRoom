package com.example.simone.arduino;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class Main extends Activity {

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
     // Bluetooth initialization


       final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }




       final Button connect=(Button)findViewById(R.id.btnConnect);
       final Button ledGiuseppeOn=(Button)findViewById(R.id.ledGiuseppeOn);
       final Button ledSimoneOn=(Button)findViewById(R.id.ledSimoneOn);
        final Button ledGiuseppeOff=(Button)findViewById(R.id.ledGiuseppeOff);
        final Button ledSimoneOff=(Button)findViewById(R.id.ledSimoneOff);
        //Button ledAccesi=(Button)findViewById(R.id.ledAccesi);
       // Button ledSpenti=(Button)findViewById(R.id.ledSpenti);
       final Button Temperatura=(Button)findViewById(R.id.temperatura);
        final SeekBar LedGiuseppe=(SeekBar)findViewById(R.id.ledGiuseppe);
        final Button EsciGiuseppe=(Button)findViewById(R.id.fatto);
        final SeekBar LedSimone=(SeekBar)findViewById(R.id.ledSimone);
       final Button EsciSimone=(Button)findViewById(R.id.fatto1);
        final Button Disconnetti=(Button)findViewById(R.id.disconnect);
        final Button bluetoothOff=(Button)findViewById(R.id.bluetoothOff);
        final Button chargecell=(Button)findViewById(R.id.charge);
        final Button chargeComp=(Button)findViewById(R.id.chargeComp);



        LedGiuseppe.setEnabled(false);
        LedSimone.setEnabled(false);
        LedGiuseppe.setProgress(0);
        LedSimone.setProgress(0);
        EsciGiuseppe.setEnabled(false);
        EsciSimone.setEnabled(false);
        ledGiuseppeOff.setEnabled(false);
        ledSimoneOff.setEnabled(false);
        Disconnetti.setEnabled(false);
        ledSimoneOn.setEnabled(false);
        ledGiuseppeOn.setEnabled(false);
        Temperatura.setEnabled(false);
        bluetoothOff.setEnabled(false);


        this.registerReceiver(this.batteryInfoReceiver,	new IntentFilter(Intent.ACTION_BATTERY_CHANGED));



        chargecell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeData("y");
                inCharge=true;
            }
        });




        chargeComp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeData("w");
                AlertDialog.Builder alert = new AlertDialog.Builder(Main.this);
                alert.setTitle("Carica pc");
                alert.setMessage("Durata carica in minuti");

                final EditText input = new EditText(getApplicationContext());
                input.setBackgroundColor(Color.alpha(Color.WHITE));
                input.setTextColor(Color.BLACK);
                alert.setView(input);

                alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }

                });

                alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //finish();
                    }
                });
                AlertDialog a=alert.create();
                a.show();
            }


        });





        LedSimone.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                writeData(Integer.toString(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        bluetoothOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeData("s");

                LedGiuseppe.setEnabled(false);
                LedSimone.setEnabled(false);
                LedGiuseppe.setProgress(0);
                LedSimone.setProgress(0);
                EsciGiuseppe.setEnabled(false);
                EsciSimone.setEnabled(false);
                ledGiuseppeOff.setEnabled(false);
                ledSimoneOff.setEnabled(false);
                Disconnetti.setEnabled(false);
                connect.setEnabled(true);
              resetConnection();
                try {
                    btSocket.close();
                }catch(Exception e){}
            }
        });

       EsciSimone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeData("Q");
                LedSimone.setEnabled(false);

                if(ledGiuseppeAcceso==false)
                ledGiuseppeOn.setEnabled(true);
                    else
                ledGiuseppeOff.setEnabled(true);

                EsciSimone.setEnabled(false);
                ledSimoneOff.setEnabled(true);
                Temperatura.setEnabled(true);
            }
        });


        EsciGiuseppe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeData("D");
                LedGiuseppe.setEnabled(false);
                ledGiuseppeOff.setEnabled(true);
                EsciGiuseppe.setEnabled(false);

                if(ledSimoneAcceso==false)
                    ledSimoneOn.setEnabled(true);
                else
                    ledSimoneOff.setEnabled(true);
                    Temperatura.setEnabled(true);

            }
        });


        LedGiuseppe.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                writeData(Integer.toString(i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });



        Disconnetti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetConnection();
                LedGiuseppe.setEnabled(false);
                LedSimone.setEnabled(false);
                LedGiuseppe.setProgress(0);
                LedSimone.setProgress(0);
                EsciGiuseppe.setEnabled(false);
                EsciSimone.setEnabled(false);
                ledGiuseppeOff.setEnabled(false);
                ledSimoneOff.setEnabled(false);
                Disconnetti.setEnabled(false);
                connect.setEnabled(true);
                try {
                    btSocket.close();
                }catch(Exception e){}
            }
        });


        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!mBluetoothAdapter.isEnabled()) {
                    mBluetoothAdapter.enable();
                    SystemClock.sleep(3000);
                }

                boolean connected=bluetoothConnect();
                if(connected==true) {
                    Toast.makeText(getApplicationContext(), "Connected!", Toast.LENGTH_LONG).show();
                    connect.setEnabled(false);
                    Disconnetti.setEnabled(true);
                    ledGiuseppeOn.setEnabled(true);
                    ledSimoneOn.setEnabled(true);
                    Temperatura.setEnabled(true);
                    bluetoothOff.setEnabled(true);
                }else{
                    Toast.makeText(getApplicationContext(), "Unable to connect", Toast.LENGTH_LONG).show();

                }
                //beginListenForData();
            }
        });

        ledGiuseppeOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeData("A");
                LedGiuseppe.setEnabled(true);
                LedGiuseppe.setProgress(25);
                ledGiuseppeOff.setEnabled(false);
                ledGiuseppeOn.setEnabled(false);
                ledSimoneOn.setEnabled(false);
                ledSimoneOff.setEnabled(false);
                EsciGiuseppe.setEnabled(true);
                ledGiuseppeAcceso=true;
                Temperatura.setEnabled(false);

            }
        });
        ledSimoneOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeData("B");
                LedSimone.setEnabled(true);
                LedSimone.setProgress(25);
                ledGiuseppeOff.setEnabled(false);
                ledGiuseppeOn.setEnabled(false);
                ledSimoneOn.setEnabled(false);
                ledSimoneOff.setEnabled(false);
                EsciSimone.setEnabled(true);
                ledSimoneAcceso=true;
                Temperatura.setEnabled(false);

            }
        });
        ledGiuseppeOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeData("a");
                ledGiuseppeAcceso=false;
                LedGiuseppe.setProgress(0);
                ledGiuseppeOff.setEnabled(false);
                ledGiuseppeOn.setEnabled(true);
            }
        });
        ledSimoneOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                writeData("b");
               ledSimoneOff.setEnabled(false);
                ledSimoneOn.setEnabled(true);
                LedSimone.setProgress(0);
                ledSimoneAcceso=false;
            }
        });


        Temperatura.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                writeData("T");
                beginListenForData();
            }
        });


    }


    private BroadcastReceiver batteryInfoReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            Button chargecell=(Button)findViewById(R.id.charge);

            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
            int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);
            collegata=plugged;


            if(plugged==1)
                chargecell.setEnabled(false);
            else
                chargecell.setEnabled(true);

            BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


            if (level >= 95 && plugged==1) {
                if(isStandBy ||inCharge) {
                    if (!mBluetoothAdapter.isEnabled()) {
                        mBluetoothAdapter.enable();
                    }
                    SystemClock.sleep(3000);
                    bluetoothConnect();
                    SystemClock.sleep(2000);
                    isStandBy=false;
                    inCharge=false;
                    writeData("Y");
                    writeData("s");
                    SystemClock.sleep(2000);
                    Intent i = new Intent(getApplicationContext(), carica.class);
                    stopService(i);
                    resetConnection();
                    mBluetoothAdapter.disable();
                    finish();

                }else {
                    writeData("Y");
                    writeData("s");
                    Intent i = new Intent(getApplicationContext(), carica.class);
                    stopService(i);
                }

            }
        }
    };


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



    void beginListenForData() {
        final TextView txt=(TextView)findViewById(R.id.txt);

        try {
            inStream = btSocket.getInputStream(); }
        catch (IOException e) { }

                    try {
                        int bytesAvailable = inStream.available();
                        if (bytesAvailable > 0)
                        { byte[] packetBytes = new byte[bytesAvailable];
                            inStream.read(packetBytes);
                            for (int i = 0; i < bytesAvailable; i++)
                            {
                                byte b = packetBytes[i];
                                if (b == lineDelimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;


                                    handler.post(new Runnable() {
                                        public void run() {

                                            txt.setText("Ext: "+data+"°C");


                                        }
                                    });
                                } else readBuffer[readBufferPosition++] = b; }
                        }
                    }
                    catch (IOException ex) { pauseSerialWorker = true; }
                }





    @Override
    protected void onDestroy() {
        super.onDestroy();



    }

    @Override
    protected void onPause(){
        super.onPause();

    }

    @Override
    protected void onStop(){
        super.onStop();
        isStandBy=true;
        if(inCharge && collegata==1){
            Intent i = new Intent(getApplicationContext(), carica.class);
            startService(i);
        }
        resetConnection();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter.disable();

    }


    @Override
    protected void onRestart() {
        super.onRestart();
        isStandBy=false;





    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
            menu.add("Spegni Carica Cell").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    Intent i=new Intent(getApplicationContext(),carica.class);
                    writeData("Y");
                    inCharge=false;
                    stopService(i);
                    return false;

                }
            });

        return true;
    }

}