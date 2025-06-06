package com.lang.y_eesp32_smartglasses_r2;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainService extends Service {

    final static String TAG = "MyInfoService";
    static Context context;

    private static BluetoothAdapter mBluetoothAdapter;

    static boolean isConnected = false;
    static BluetoothGattCharacteristic foundCharacteristic = null;
    static BluetoothGatt bluetoothGattC = null;

    private static final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {

        @Override public void onCharacteristicChanged(BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic) {
            String value_status = characteristic.getStringValue(0);
            Log.i(TAG, "onCharacteristicChanged: " + value_status);

            if ( value_status!=null && value_status.equals("1") ) {
                ESP32.ScreenState.TIME_LAST_INVOKE = System.currentTimeMillis();
                ESP32.STATUS = ESP32Status.OLED_OFF;
            }
            else if (value_status!=null) ESP32.onInterrupt(value_status);
        }

        @Override public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i(TAG, "Attempting to start service discovery:" + gatt.discoverServices());
                isConnected=true;
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.i(TAG, "Disconnected from GATT server.");
                isConnected=false;
            }
        }

        @Override public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Discovered services");
                //tvStatus.setText("Connected");

                bluetoothGattC = gatt;

                List<BluetoothGattService> lServices = gatt.getServices();
                for (BluetoothGattService service : lServices) {

                    UUID uuid = UUID.fromString(MainActivity.sCHARACTERISTIC_UUID);
                    BluetoothGattCharacteristic localCharacteristic = service.getCharacteristic(uuid);
                    if (localCharacteristic != null) {
                        foundCharacteristic = localCharacteristic;
                        break;
                    }
                }

                gatt.setCharacteristicNotification(foundCharacteristic, true);

                // *** Startup commands ***
                ESP32.startupSection();

            } else {
                Log.i(TAG, "Discovering services failed");
                // tvStatus.setText("Disconnected");
            }
        }
    };



    @Nullable @Override public IBinder onBind(Intent intent) {
        return null;
    }

    @Override public void onCreate() {
        super.onCreate();

        context = this;

        final BluetoothManager mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();
    }


    static class ESP32Status {
        static final int OLED_ON = 0;
        static final int OLED_OFF = 1;
    }

    static class ESP32 {
        static int STATUS=ESP32Status.OLED_ON;

        // Saved last evoked screen type (when user go to one of the screens)
        static class ScreenState {
            static final long TIME_IDLE_GO_HOME = 10000; // After this time with OLED off go back to mainscreen
            static long TIME_LAST_INVOKE = 0;

            static final int SCREEN_MAIN = 0;
            static final int SCREEN_MUSI = 1;
            static final int SCREEN_MSGN = 2;
            static final int SCREEN_CALL = 3;
            static final int SCREEN_LIST = 4;

            static final int SCREEN_NAVI = 5;                                                       // For now disabled maybe upgrade in future? :) Or YOU gonna do it? Are you? .. Do it :P

            static int CURRENT_SCREEN = SCREEN_MAIN;

            static String
                    prev_MsgScreen  = "#1|280|None|No recent msg",
                    prev_CallScreen = "#2|No recent calls",
                    prev_NaviScreen = "#3|0|0|0|280",
                    prev_ListScreen = "#4|257|No List|280|No list",
                    prev_MusicScreen= "#5|182|No music playing|210|214";
        }


        //TODO  ********************************** Startup section - write cmds which will be send after successful connection ***************************************8
        static void startupSection() {
            new Thread() {
                public void run() {
                    try {
                        writeToESP("#MX=20"     ); sleep(1000);
                        writeToESP("#TST=100"   ); sleep(1000);


                    } catch (Exception e) { e.printStackTrace(); }
                }
            }.start();
        }
        //TODO ******************************************************************************************

        static boolean connect(String address) {
            Log.i(TAG, "Connecting to: " + address);
            if (mBluetoothAdapter == null || address == null) {
                Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
                return false;
            }

            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);

            BluetoothGatt bluetoothGatt = device.connectGatt(context, false, mGattCallback);
            Log.d(TAG, "Trying to create a new connection.");

            return true;
        }

        static void disconnect() {
            if (bluetoothGattC != null) {
                bluetoothGattC.disconnect();
                new Handler().postDelayed(new Runnable() {
                    @Override public void run() {
                        bluetoothGattC.close();
                        bluetoothGattC = null;
                        isConnected=false;
                    }
                }, 1000);
            }
        }

        static boolean isConnected() {
            return foundCharacteristic != null && bluetoothGattC != null && isConnected;
        }

        static void writeToESP(String text) {
            ScreenState.TIME_LAST_INVOKE = System.currentTimeMillis();

            byte value[] = text.getBytes();

            if (isConnected()) {
                foundCharacteristic.setValue(value);
                bluetoothGattC.writeCharacteristic(foundCharacteristic);
                Log.i(TAG, "Write OK");
                return;
            }
            Log.i(TAG, "Problems while writing");
        }

        /* --- On Touch Click on ESP32 --- */
        static void onInterrupt(String interruptName) {
            Log.i(TAG, "Interrupt: " + interruptName);

            /* --- All Actions --- */

            // After time of idle go back to home
            if ( (System.currentTimeMillis() - ScreenState.TIME_LAST_INVOKE) >= ScreenState.TIME_IDLE_GO_HOME ) ScreenState.CURRENT_SCREEN = ScreenState.SCREEN_MAIN;

            // If oled off 1 click turn on last viewed screen
            if (interruptName.equals("#TS1") && STATUS == 1) {
                showLastScreen(ScreenState.CURRENT_SCREEN);

            } else if (interruptName.equals("#TS0")) {      // Long click changes the screens
                if (ScreenState.CURRENT_SCREEN >= 4) ScreenState.CURRENT_SCREEN=-1;
                showLastScreen(++ScreenState.CURRENT_SCREEN);
            }

            // Screen Actions
            else if (ScreenState.CURRENT_SCREEN == ScreenState.SCREEN_MUSI) {
                if (interruptName.equals("#TS1")) {
                    boolean isplaying = Music.musicToggle  ();

//                    "#5|182|No music playing|210|214"
                    if (isplaying)  musicScreen("182", "Music", "210", "214");
                    else            musicScreen("182", "Music", "211", "214");
                }
                else if (interruptName.equals("#TS2")) { Music.musicNext    (); }
            } else if (ScreenState.CURRENT_SCREEN == ScreenState.SCREEN_LIST) {
                if (interruptName.equals("#TS1")) {
                    currentListPos++;
                    onList();
                }
            }



            // Awoken - nvm which key pattern
            STATUS = 0;
        }

        static void onHome() {
//            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String HH = new SimpleDateFormat("HH", Locale.getDefault()).format(new Date());
            String mm = new SimpleDateFormat("mm", Locale.getDefault()).format(new Date());

//            sdf = new SimpleDateFormat("dd MMM", Locale.getDefault());
            String date = new SimpleDateFormat("dd MMM", Locale.getDefault()).format(new Date());

            mainScreen(HH, mm, date, "-1", "");
        }

        static int currentListPos=0;
        static void onList() {
            String list = MainActivity.readSharedPreferencesList();

            ArrayList<String> aList = new ArrayList<>();

            Pattern p = Pattern.compile("-(.*)");
            Matcher m = p.matcher(list);

            while (m.find()) {
                aList.add(m.group(1));
            }

            if (currentListPos >= aList.size()) currentListPos=0;

            listScreen("257", MainActivity.readSharedPreferencesTitle(), "221", aList.get(currentListPos).trim());
        }

        static void showLastScreen(int screenId) {
            switch (screenId) {
                case ScreenState.SCREEN_MAIN: onHome(); break;
//                case ScreenState.SCREEN_NAVI: writeToESP(ScreenState.prev_NaviScreen) ; break;    // For now disabled maybe upgrade in future? :) Or YOU gonna do it? Are you? .. Do it :P

                case ScreenState.SCREEN_MSGN: writeToESP(ScreenState.prev_MsgScreen)  ; break;
                case ScreenState.SCREEN_CALL: writeToESP(ScreenState.prev_CallScreen) ; break;
                case ScreenState.SCREEN_LIST: onList(); break;
                case ScreenState.SCREEN_MUSI: writeToESP(ScreenState.prev_MusicScreen); break;
            }
        }


        /* --- Screens --- */
        static void mainScreen(String HH, String mm, String date, String symbol, String degrees) {
            ScreenState.CURRENT_SCREEN = ScreenState.SCREEN_MAIN;
            String t = "#0|" + HH + "|" + mm + "|" + date + "|" + symbol + "|" + degrees;
            writeToESP(t);
        }

        static void msgNotiScreen(String symbol, String from, String text) {
            ScreenState.CURRENT_SCREEN = ScreenState.SCREEN_MSGN;
            String t = "#1|" + symbol + "|" + from + "|" + text;
            ScreenState.prev_MsgScreen=t;
            writeToESP(t);
        }

        static void callScreen(String from) {
            ScreenState.CURRENT_SCREEN = ScreenState.SCREEN_CALL;
            String t="#2|" + from;
            ScreenState.prev_CallScreen=t;
            writeToESP(t);
        }

        static void navScreen(String maxSpeed, String distance, String distanceToDes, String symbol) {
            ScreenState.CURRENT_SCREEN = ScreenState.SCREEN_NAVI;
            String t = "#3|" + maxSpeed + "|" + distance + "|" + distanceToDes + "|" + symbol;
            ScreenState.prev_NaviScreen=t;
            writeToESP(t);
        }

        static void listScreen(String symbolMain, String title, String symbolSub, String text) {
            ScreenState.CURRENT_SCREEN = ScreenState.SCREEN_LIST;
            String t = "#4|" + symbolMain + "|" + title + "|" + symbolSub + "|" + text;
            ScreenState.prev_ListScreen=t;
            writeToESP(t);
        }

        static void musicScreen(String musicIcon, String title, String symbolPlayStop, String symbolNext) {
            ScreenState.CURRENT_SCREEN = ScreenState.SCREEN_MUSI;
            String t = "#5|" + musicIcon + "|" + title + "|" + symbolPlayStop + "|" + symbolNext;
            ScreenState.prev_MusicScreen=t;
            writeToESP(t);
        }

    }

    /* --- Music Player --- */
    static class Music {
        public static final String CMDTOGGLEPAUSE = "togglepause";
        public static final String CMDPAUSE = "pause";
        public static final String CMDPREVIOUS = "previous";
        public static final String CMDNEXT = "next";
        public static final String CMDSTOP = "stop";

        public static final String Service = "com.android.music.musicservicecommand";
        public static final String Command = "command";

        static boolean isMusicPlaying=false;
        static boolean musicToggle() {
            if (isMusicPlaying) musicPause();
            else                musicPlay();

            isMusicPlaying = !isMusicPlaying;

            return isMusicPlaying;
        }

        static void musicPlay() {
            Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_PLAY));
            MainService.context.sendOrderedBroadcast(i, null);

            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,     KeyEvent.KEYCODE_MEDIA_PLAY));
            MainService.context.sendOrderedBroadcast(i, null);
        }

        static void musicPause() {
            Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_PAUSE));
            MainService.context.sendOrderedBroadcast(i, null);

            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,     KeyEvent.KEYCODE_MEDIA_PAUSE));
            MainService.context.sendOrderedBroadcast(i, null);
        }

        static void musicNext() {
            Intent i = new Intent(Intent.ACTION_MEDIA_BUTTON);
            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN,KeyEvent.KEYCODE_MEDIA_NEXT));
            MainService.context.sendOrderedBroadcast(i, null);

            i.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,     KeyEvent.KEYCODE_MEDIA_NEXT));
            MainService.context.sendOrderedBroadcast(i, null);
        }
    }





}
