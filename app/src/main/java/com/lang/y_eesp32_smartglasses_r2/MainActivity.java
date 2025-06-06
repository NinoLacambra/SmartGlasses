package com.lang.y_eesp32_smartglasses_r2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    /*
     * Using - https://www.polidea.com/blog/Emberlight_turns_light_bulbs_into_smart_ones/
     *
     *
     *
     *
     * Interesting - https://github.com/Polidea/RxAndroidBle/
     *
     *
     * TODO Google Directions API - get hints how to get to destianation
     *
     *
     * */

    static final String sESP32_MAC = "94:54:C5:B2:3A:2E";
    //    static final String sESP32_MAC = "94:54:C5:B2:3A:2C";                                           // test
    static final String sCHARACTERISTIC_UUID = "6d96843a-cbce-4bad-8c6a-693787fa94c2";

    static SharedPreferences sharedPreferences;
    final static String SP_TITLE = "SP_TITLE";
    final static String SP_LIST = "SP_LIST";

    static boolean isConnected = false;

    final static String TAG = "MyInfo";

    private BluetoothAdapter mBluetoothAdapter;

    final int REQUEST_ENABLE_BT = 1;

    Intent iNotificationService = null;
    Intent iMainService = null;

    static boolean shouldRunning = false;


    TextView tvStatus;
    Button bStart;
    Button bStop;

    EditText etCmd;
    Button bSendCmd;

    Button bWebUpdate;
    Button bRestart;

    Button bTestScreen1, bTestScreen2, bTestScreen3, bTestScreen4, bTestScreen5, bTestScreen6;

    Button bOpenList;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        sharedPreferences = getPreferences(MODE_PRIVATE);

        bStart = findViewById(R.id.b_Start);
        bStop = findViewById(R.id.b_Stop);
        tvStatus = findViewById(R.id.tv_Status);

        etCmd = findViewById(R.id.et_Cmd);
        bSendCmd = findViewById(R.id.b_SendCmd);

        bWebUpdate = findViewById(R.id.b_WebUpdate);
        bRestart = findViewById(R.id.b_ESPRestart);

        bTestScreen1 = findViewById(R.id.b_TestScreen1);
        bTestScreen2 = findViewById(R.id.b_TestScreen2);
        bTestScreen3 = findViewById(R.id.b_TestScreen3);
        bTestScreen4 = findViewById(R.id.b_TestScreen4);
        bTestScreen5 = findViewById(R.id.b_TestScreen5);
        bTestScreen6 = findViewById(R.id.b_TestScreen6);

        bOpenList = findViewById(R.id.b_OpenList);


        // If bluetooth supported
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        // Request enabling? Bluetooth if turned off
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // ------------- Notification Listener Permission - Msg/Notification screen - MSG from notifications --------------------
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
            startActivity(intent);
        }

        // --- MSG/Notifications - MSG from notifications - Service Start
        iNotificationService = new Intent(MainActivity.this, NotificationService.class);
        startService(iNotificationService);

        // --- MainService - Start
        iMainService = new Intent(MainActivity.this, MainService.class);
        startService(iMainService);


        // Start
        bStart.setOnClickListener(view -> MainService.ESP32.connect(sESP32_MAC));
        bStop.setOnClickListener(view -> MainService.ESP32.connect(sESP32_MAC));

        bSendCmd.setOnClickListener(view-> {
            if (etCmd.getText() != null && !etCmd.getText().toString().isEmpty()) {
                MainService.ESP32.writeToESP(etCmd.getText().toString());
            }
        });
        bWebUpdate.setOnClickListener(view -> MainService.ESP32.writeToESP("#OM=1"));
        bRestart.setOnClickListener(view -> MainService.ESP32.writeToESP("#RESTART"));


        // --- Test Buttons ---
        bTestScreen1.setOnClickListener(view -> {
            MainService.ESP32.mainScreen("14", "35", "13 sty", "125", "23C");
        });

        bTestScreen2.setOnClickListener(view -> {
            MainService.ESP32.msgNotiScreen("156", "Heyho!", "Hello, guys are you able to see me? Are you here?! Man... Are you or not? If yes leave a like and subscription.... Eeee :) it's just text");
        });

        bTestScreen3.setOnClickListener(view -> {
            MainService.ESP32.callScreen("Tata");
        });

        bTestScreen4.setOnClickListener(view -> {
            MainService.ESP32.navScreen("120 km/h", "500 m", "54 km", "76");
        });

        bTestScreen5.setOnClickListener(view -> {
            MainService.ESP32.listScreen("257", "Shopping list", "221", "Cheese and other nice things buyyy");
        });

        bTestScreen6.setOnClickListener(view -> {
            MainService.ESP32.musicScreen("182", "Tymek - Anioly i Demony", "210", "214");
        });

        bOpenList.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, ListActivity.class)));

    }

    static void saveSharedPreferencesTitle(String s) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SP_TITLE, s);
        editor.apply();
    }

    static String readSharedPreferencesTitle() {
        return sharedPreferences.getString(SP_TITLE, "");
    }

    static void saveSharedPreferencesList(String s) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SP_LIST, s);
        editor.apply();
    }

    static String readSharedPreferencesList() {
        return sharedPreferences.getString(SP_LIST, "");
    }



    // --- Music Player ---
//    static class Music {
//        public static final String CMDTOGGLEPAUSE = "togglepause";
//        public static final String CMDPAUSE = "pause";
//        public static final String CMDPREVIOUS = "previous";
//        public static final String CMDNEXT = "next";
//        public static final String CMDSTOP = "stop";
//
//        public static final String Service = "com.android.music.musicservicecommand";
//        public static final String Command = "command";
//
//        static void musicToggle() {
//            Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
//            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
//            MainActivity.mainActivity.sendOrderedBroadcast(i, null);
//
//            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,     KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE));
//            MainActivity.mainActivity.sendOrderedBroadcast(i, null);
//        }
//
//        static void musicPlay() {
//            Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
//            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_PLAY));
//            MainActivity.mainActivity.sendOrderedBroadcast(i, null);
//
//            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,     KeyEvent.KEYCODE_MEDIA_PLAY));
//            MainActivity.mainActivity.sendOrderedBroadcast(i, null);
//        }
//
//        static void musicPause() {
//            Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
//            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_PAUSE));
//            MainActivity.mainActivity.sendOrderedBroadcast(i, null);
//
//            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,     KeyEvent.KEYCODE_MEDIA_PAUSE));
//            MainActivity.mainActivity.sendOrderedBroadcast(i, null);
//        }
//
//        static void musicNext() {
//            Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
//            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_NEXT));
//            MainActivity.mainActivity.sendOrderedBroadcast(i, null);
//
//            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,     KeyEvent.KEYCODE_MEDIA_NEXT));
//            MainActivity.mainActivity.sendOrderedBroadcast(i, null);
//        }
//    }
//
//
//    // !!!! Jezeli nie zdaze odczytac wiadomosci bo sie wlaczy powersavemode to po klikniecju nie wysiwalta sie glowny ekran tylko dalsza wiadomosc (np doc zasu 20 s od odebrasnia wiadomosci)
//
//    // --- Send Functions ---
//    static class ESP32SmartGlasses {
//        static int ESP_STATUS = 0;                                                                  // 1 - oled off
//
//
//        // Saved last evoked screen type (when user go to one of the screens)
//        static class ScreenState {
//            static final int SCREEN_MAIN = 0;
//            static final int SCREEN_MSGN = 1;
//            static final int SCREEN_CALL = 2;
//            static final int SCREEN_NAVI = 3;
//            static final int SCREEN_LIST = 4;
//            static final int SCREEN_MUSI = 5;
//
//            static int CURRENT_SCREEN = SCREEN_MAIN;
//
//
//            static String
//                    prev_MsgScreen  = "#1|280|None|No recent msg",
//                    prev_CallScreen = "#2|No recent calls",
//                    prev_NaviScreen = "#3|0|0|0|280",
//                    prev_ListScreen = "#4|257|No List|280|No list",
//                    prev_MusicScreen= "#5|182|No music playing|210|214";
//        }
//
//
//
//        public static void onHome() {
//            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
//            String time = sdf.format(new Date());
//            sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
//            String date = sdf.format(new Date());
//
//            mainScreen(time, date, "-1", "");
//        }
//
//
//        static void showLastScreen(int screenId) {
//            switch (screenId) {
//                case ScreenState.SCREEN_MAIN: onHome(); break;
//                case ScreenState.SCREEN_NAVI: writeToESP(ScreenState.prev_NaviScreen) ; break;
//
//                case ScreenState.SCREEN_MSGN: writeToESP(ScreenState.prev_MsgScreen)  ; break;
//                case ScreenState.SCREEN_CALL: writeToESP(ScreenState.prev_CallScreen) ; break;
//                case ScreenState.SCREEN_LIST: writeToESP(ScreenState.prev_ListScreen) ; break;
//                case ScreenState.SCREEN_MUSI: writeToESP(ScreenState.prev_MusicScreen); break;
//            }
//        }
//
//
//        /* --- On Touch Click on ESP32 --- */
//        public static void onInterrupt(String interruptName) {
//            Log.i(TAG, "Interrupt: " + interruptName);
//
//            // All Actions
//            // If oled off 1 click turn on last viewed screen
//            if (interruptName.equals("#TS1") && ESP_STATUS == 1) {
//                showLastScreen(ScreenState.CURRENT_SCREEN);
//                // Awoken
//                ESP_STATUS = 0;
//            } else if (interruptName.equals("#TS3")) {      // Tripple click changes the screens
//                showLastScreen(++ScreenState.CURRENT_SCREEN);
//                if (ScreenState.CURRENT_SCREEN >= 5) ScreenState.CURRENT_SCREEN=0;
//            }
//
//            // Screen Actions
//            if (ScreenState.CURRENT_SCREEN == ScreenState.SCREEN_MUSI) {
////                if      (interruptName.equals("#TS1")) { Music.musicToggle  (); }
////                else if (interruptName.equals("#TS2")) { Music.musicNext    (); }
//            }
//
//
//        }
//
//
//        static void mainScreen(String time, String date, String symbol, String degrees) {
//            ScreenState.CURRENT_SCREEN = ScreenState.SCREEN_MAIN;
//            String t = "#0|" + time + "|" + date + "|" + symbol + "|" + degrees;
//            writeToESP(t);
//        }
//
//
//        static void msgNotiScreen(String symbol, String from, String text) {
//            ScreenState.CURRENT_SCREEN = ScreenState.SCREEN_MSGN;
//            String t = "#1|" + symbol + "|" + from + "|" + text;
//            ScreenState.prev_MsgScreen=t;
//            writeToESP(t);
//        }
//
//        static void callScreen(String from) {
//            ScreenState.CURRENT_SCREEN = ScreenState.SCREEN_CALL;
//            String t="#2|" + from;
//            ScreenState.prev_CallScreen=t;
//            writeToESP(t);
//        }
//
//        static void navScreen(String maxSpeed, String distance, String distanceToDes, String symbol) {
//            ScreenState.CURRENT_SCREEN = ScreenState.SCREEN_NAVI;
//            String t = "#3|" + maxSpeed + "|" + distance + "|" + distanceToDes + "|" + symbol;
//            ScreenState.prev_NaviScreen=t;
//            writeToESP(t);
//        }
//
//        static void listScreen(String symbolMain, String title, String symbolSub, String text) {
//            ScreenState.CURRENT_SCREEN = ScreenState.SCREEN_LIST;
//            String t = "#4|" + symbolMain + "|" + title + "|" + symbolSub + "|" + text;
//            ScreenState.prev_ListScreen=t;
//            writeToESP(t);
//        }
//
//        static void musicScreen(String musicIcon, String title, String symbolPlayStop, String symbolNext) {
//            ScreenState.CURRENT_SCREEN = ScreenState.SCREEN_MUSI;
//            String t = "#5|" + musicIcon + "|" + title + "|" + symbolPlayStop + "|" + symbolNext;
//            ScreenState.prev_MusicScreen=t;
//            writeToESP(t);
//        }
//
//
//
//        public static boolean isConnected() {
//            return foundCharacteristic != null && bluetoothGattC != null && isConnected;
//        }
//
//
//
//        static void writeToESP(String text) {
////            if (ESP32SmartGlasses.ESP_STATUS == 1) return;                                          // ESP busy
//            byte value[] = text.getBytes();
//
//            if (foundCharacteristic != null && bluetoothGattC != null) {
//                foundCharacteristic.setValue(value);
//                bluetoothGattC.writeCharacteristic(foundCharacteristic);
//                Log.i(TAG, "Write OK");
//                return;
//            }
//            Log.i(TAG, "Problems while writing");
//        }
//    }
//    // --- Send Functions End ---
//
//
//
//
//
//    public boolean connect(String address) {
//        Log.i(TAG, "Connecting to: " + address);
//        if (mBluetoothAdapter == null || address == null) {
//            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
//            return false;
//        }
//
//        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
//
//        BluetoothGatt bluetoothGatt = device.connectGatt(this, false, mGattCallback);
//        Log.d(TAG, "Trying to create a new connection.");
//
//        return true;
//    }
//
//    /* --- BLE -------------------------------------------------------------------------------------*/
//    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
//
//        @Override public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
//            String value_status = characteristic.getStringValue(0);
//            Log.i(TAG, "onCharacteristicChanged: " + value_status);
//
//            if ( value_status!=null && (value_status.equals("0") || value_status.equals("1")) ) ESP32SmartGlasses.ESP_STATUS = Integer.parseInt(value_status);
//            else if (value_status!=null) ESP32SmartGlasses.onInterrupt(value_status);
//        }
//
//        @Override public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
//
//            // String address = gatt.getDevice().getAddress();
//
//            if (newState == BluetoothProfile.STATE_CONNECTED) {
//                Log.i(TAG, "Attempting to start service discovery:" + gatt.discoverServices());
//                isConnected=true;
//            } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
//                Log.i(TAG, "Disconnected from GATT server.");
//                isConnected=false;
//            }
//
//        }
//
//        @Override public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
//
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                Log.i(TAG, "Discovered services");
//                //tvStatus.setText("Connected");
//
//                bluetoothGattC = gatt;
//
//                List<BluetoothGattService> lServices = gatt.getServices();
//                for (BluetoothGattService service : lServices) {
//
//                    UUID uuid = UUID.fromString(sCHARACTERISTIC_UUID);
//                    BluetoothGattCharacteristic localCharacteristic = service.getCharacteristic(uuid);
//                    if (localCharacteristic != null) {
//                        foundCharacteristic = localCharacteristic;
//                        break;
//                    }
//                }
//
//                gatt.setCharacteristicNotification(foundCharacteristic, true);
//
//            } else {
//                Log.i(TAG, "Discovering services failed");
//               // tvStatus.setText("Disconnected");
//            }
//        }
//    };



    // --- Exit ---
    @Override protected void onPause() {
        super.onPause();
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        MainService.ESP32.disconnect();
        if (iNotificationService    != null) stopService(iNotificationService);
        if (iMainService            != null) stopService(iMainService);
    }

    @Override protected void onStop() {
        super.onStop();
    }
}
