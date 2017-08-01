package com.example.yufu.remotebt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.support.annotation.IdRes;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity {

    // Hint: If you are connecting to a Bluetooth serial board then try using the following well-known SPP UUID:
    private static final UUID mUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // ___ Voice Input ___ //
    private static final int SPEECH_REQUEST_CODE = 0;
    BluetoothAdapter bluetoothAdapter;
    String serverMsg = "";
    RadioGroup radioGroup;
    TextView msgTxV;
    Button voiceIpBtn;
    Button btConnectOffBtn;
    Button leftBtn;
    Button fwdBtn;
    Button revBtn;
    Button rightBtn;
    String address = "00:00:00:00:00:00"; // MAC Address.
    //String address = "00:21:13:00:3D:68"; // MAC Address.
    String macAddressSetting = "00:00:00:00:00:00";
    boolean inSettings;
    boolean btExitSetting;
    String spokenText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // On activity creation:
        getPrefs(); // retrieve Settings Preferences (MAC address & Bluetooth Exit Setting).

        Button btBtn = (Button) findViewById(R.id.btBtn);
        btBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDevices();
            }
        });

        voiceIpBtn = (Button) findViewById(R.id.voiceIpBtn);
        voiceIpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displaySpeechRecognizer();
            }
        });

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        msgTxV = (TextView) findViewById(R.id.serverMsgTxV);


        leftBtn = (Button) findViewById(R.id.leftBtn);

        fwdBtn = (Button) findViewById(R.id.fwdBtn);

        revBtn = (Button) findViewById(R.id.revBtn);

        rightBtn = (Button) findViewById(R.id.rightBtn);


        // Toggle RadioGroup:
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

                switch(checkedId)
                {
                    case R.id.sendRadBtn:

                        break;
                    case R.id.receiveRadBtn:

                        break;
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        // After returning back to activity from Settings, reload preference settings.
        getPrefs();

        inSettings = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.w("onStop", "invoked!");

        bluetoothAdapter.cancelDiscovery();

        if (btExitSetting && !inSettings) {
            bluetoothAdapter.disable();
        }

    }

    /* ___ Finding paired devices ___ */
    private void pairedDevices() {
        // - Querying paired devices -
        final Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                Log.v("Paired Devices", device.getName() + "\n");
                Log.v("Paired Devices",device.getAddress() + "\n");

                final ConnectThread connectThread = new ConnectThread(device);

                Log.e("MAC Add", address);

                if (device.getAddress().equals(address)) {
                    connectThread.start();
                }

                btConnectOffBtn = (Button) findViewById(R.id.btConnectOffBtn);
                btConnectOffBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        connectThread.close();
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_main, menu);


        View view = menu.findItem(R.id.action_connect).getActionView();
        Switch connect_switch = (Switch) view.findViewById(R.id.connect_switch);
        connect_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                pairedDevices();
                //ConnectedThread connectedThread = new ConnectedThread();
                //connectedThread.closeConnection();
            }
        });


        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.action_settings) {
            Intent settingIntent = new Intent(this, Settings.class);
            startActivity(settingIntent);

            inSettings = true;
        }

        if (item.getItemId() == R.id.action_exit) {
            onStop();
            finish();
        }

        return true;
    }

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
    // This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            spokenText = results.get(0); // The resulting speech text.
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Retrieve MAC address and Exit-setting entered into "Settings" Activity from Preferences.
    public void getPrefs() {
        //if (getIntent().getExtras() != null) { // <-- This conditional statement - stops mac address to be returned as intended !
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        btExitSetting = sharedPref.getBoolean("pref_key_exit_bluetooth", false);
        Log.v("Bundle", String.valueOf(btExitSetting));

        macAddressSetting = sharedPref.getString("pref_key_MAC", macAddressSetting);
        address = macAddressSetting;
        //}
    }

    /* ___ Connecting to BT remote device. ___*/
    private class ConnectThread extends Thread {

        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        ConnectedThread connectedThread;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // UUID
                tmp = device.createRfcommSocketToServiceRecord(mUUID);

                Log.d("Connecting","To BT Device");

            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            bluetoothAdapter.cancelDiscovery();

            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();
                Log.d("Connected", "Sucessful Connection!!!");
            }
            catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                cancel();
            }

            // Do work to manage the connection (in a separate thread).
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.start();

            radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.sendRadBtn) {
                        Log.d("RadioButton", "Receive");
                        ///connectedThread.write();
                    }
                    else if (checkedId == R.id.receiveRadBtn) {
                        Log.d("RadioButton", "Send");
                        ///connectedThread.read();
                        ///msgTxV.setText(serverMsg);
                        ///Log.i("Server MSG", serverMsg);
                    }


                    /*
                    if (spokenText != null) {
                        spokenText = spokenText.trim(); // remove spaces - if not, String comparison methods "contains"/"equals" won't catch String with whitespaces.
                        //if (spokenText.contains("on")) {
                        if (spokenText.equalsIgnoreCase("one")) {
                            connectedThread.write();
                            Log.i("ON Voice", spokenText);
                        }
                        //else if (spokenText.contains("of")) {
                        else if (spokenText.equalsIgnoreCase("zero") || spokenText.equalsIgnoreCase("0")) {
                            connectedThread.read();
                            Log.i("OFF Voice", spokenText);
                        }
                        Log.i("General Voice",spokenText);
                        msgTxV.setText(spokenText);
                    }
                    */

                }
            });

            // -----------------------------------------------------------------------------------//
            // -----------------------------------------------------------------------------------//
            // ***---Buttons event handling.---*** //
            /*
            *** 1) onTouch is used instead of onClick - To access ACTION_DOWN & ACTION_UP.
            *** 2) ACTION_DOWN & ACTION_UP are used - To only trigger events when a button is being pressed.
            *** 3) When a button is pressed down -> A byte is passed to micro-controller (MCU). -> As a notification of a button press event. <- This is where the MCU can set the pins connected to the motor to high/low.
            *** 4) When the button is unpressed (action_up event) -> Another byte is passed to the micro-controller (MCU). -> As a notification of the end of the button press event.
             */
            leftBtn.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Log.v("Button","DOWN---");
                        connectedThread.left("1");
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        Log.v("Button","UP---");
                        connectedThread.left("0");
                    }
                    return true;
                }
            });

            fwdBtn.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Log.v("Button","DOWN---");
                        connectedThread.fwd("2");
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        Log.v("Button","UP---");
                        connectedThread.fwd("0");
                    }
                    return true;
                }
            });

            revBtn.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Log.v("Button","DOWN---");
                        connectedThread.rev("3");
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        Log.v("Button","UP---");
                        connectedThread.rev("0");
                    }
                    return true;
                }
            });

            rightBtn.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Log.v("Button","DOWN---");
                        connectedThread.right("4");
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        Log.v("Button","UP---");
                        connectedThread.right("0");
                    }
                    return true;
                }
            });
            // -----------------------------------------------------------------------------------//
            // -----------------------------------------------------------------------------------//

        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }

        private void close() {
            connectedThread.closeConnection();
        }
    }

    /* Managing Communication. */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        PrintWriter outputStream;
        BufferedReader inputStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();

                outputStream = new PrintWriter(socket.getOutputStream(), true);
                Log.d("Output Stream","Initialised");
                inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
        }


        // -----------------------------------------------------------------------------------//
        // -----------------------------------------------------------------------------------//
        // ***---Methods for handling directional movement.---*** //
            /*
            *** 1) An String argument is passed onto these methods.
            *** 2) The String message is converted to Bytes, and outputted.
            *** 3) Each movement is designated a distinct message (1,2,3,4) to distinguish each state.
            *** 4) The byte message will be received by the MCU, where the MCU will be programmed to control pin states based on the message sent from the Android remote.
             */
        private void left(String msg) {
            //String message = "1";
            byte[] msgBuffer = msg.getBytes(); // Encode a given String into a sequence of bytes and return as an array of bytes.
            try {
                mmOutStream.write(msgBuffer); // Write Byte Array to the OutputStream.
            } catch (IOException e) {
                Log.d(TAG, "...Error send: " + e.getMessage() + "...");
            }
        }
        private void fwd(String msg) {
            //String message = "2";
            byte[] msgBuffer = msg.getBytes(); // Encode a given String into a sequence of bytes and return as an array of bytes.
            try {
                mmOutStream.write(msgBuffer); // Write Byte Array to the OutputStream.
                Log.e("Invoke","FWD---");
            } catch (IOException e) {
                Log.d(TAG, "...Error send: " + e.getMessage() + "...");
            }
        }
        private void rev(String msg) {
            //String message = "3";
            Log.d("Message", "... " + msg + " ...");
            byte[] msgBuffer = msg.getBytes(); // Encode a given String into a sequence of bytes and return as an array of bytes.
            try {
                mmOutStream.write(msgBuffer); // Write Byte Array to the OutputStream.
                mmOutStream.flush(); //
            } catch (IOException e) {
                Log.d(TAG, "...Error send: " + e.getMessage() + "...");
            }
            ///return msg;
        }
        private void right(String msg) {
            //String message = "4";
            byte[] msgBuffer = msg.getBytes(); // Encode a given String into a sequence of bytes and return as an array of bytes.
            try {
                mmOutStream.write(msgBuffer); // Write Byte Array to the OutputStream.
            } catch (IOException e) {
                Log.d(TAG, "...Error send: " + e.getMessage() + "...");
            }
        }
        // -----------------------------------------------------------------------------------//
        // -----------------------------------------------------------------------------------//


        /*
        private void write() {
            // To ensure that the spoken text is added to the output stream - add a conditional statement to check if spoken text variable is not null.
            ///if (spokenText != null) {
                ///outputStream.println(spokenText);
                ///Log.d("Output Stream", spokenText);
                ///Log.d("Output Stream", "Written to Server");


                String message = "1";
                byte[] msgBuffer = message.getBytes(); // Encode a given String into a sequence of bytes and return as an array of bytes.
                try {
                    mmOutStream.write(msgBuffer); // Write Byte Array to the OutputStream.
                } catch (IOException e) {
                    Log.d(TAG, "...Error send: " + e.getMessage() + "...");
                }


            ///}
        }
        */

        /*
        private void read() {

            try {

                ///serverMsg = inputStream.readLine();
                ///Log.i("Server MSG", serverMsg);
                ///Log.v("InputStream", "Read");



                String message = "0";
                byte[] msgBuffer = message.getBytes(); // Encode a given String into a sequence of bytes and return as an array of bytes.
                mmOutStream.write(msgBuffer); // Write Byte Array to the OutputStream.


            }
            catch(IOException e){
                e.printStackTrace();
            }

        }
        */

        /* Shutdown the connection */
        public void closeConnection() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }

    }


}
