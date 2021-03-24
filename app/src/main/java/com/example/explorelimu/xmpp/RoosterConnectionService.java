package com.example.explorelimu.xmpp;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.explorelimu.R;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;

import java.io.IOException;
import java.util.Random;

public class RoosterConnectionService extends Service {
    public static final String PRESENCE_UPDATE = "presence_update";
    public static final String BUNDLE_PRESENCE_STATUS = "presence_status";
    public static final String SENT_MESSAGE = "sent_message";
    public static final String BUNDLE_MESSAGE_RECEIPT_ID = "msg_receipt_ID";
    private static final String TAG ="RoosterService";

    public static final String SEND_MESSAGE = "sendmessage";
    public static final String BUNDLE_MESSAGE_BODY = "b_body";
    public static final String BUNDLE_MESSAGE_ID = "b_id";

    public static final String BUNDLE_MESSAGE_OBJ = "message_obj";

    public static final String MESSAGE_UPDATE = "message_update";

    public static final String NEW_MESSAGE = "newmessage";
    public static final String BUNDLE_FROM_JID = "b_from";
    public static final String BUNDLE_TO_JID = "b_to";

    public static final String DISPLAY_NAME = "display_name";
    public static final String CONNECTION_TYPE = "conn_type";

    public static final String SERVICE_REQUEST = "service_request";
    public static final String AVATAR_BYTES = "avatar_bytes";

    public static final String SEND_STATUS = "send_status";
    public static final String STATUS_UPDATE = "status_update";

    public static final String LOGOUT = "logout";

    public static final String START_INTENT = "start_intent";
    public static final String START_MANUAL = "start_manual";

    private Intent intent;

    public static RoosterConnection.ConnectionState sConnectionState;
    public static RoosterConnection.LoggedInState sLoggedInState;
    private boolean mActive;//Stores whether or not the thread is active
    private Thread mThread;
    private Handler mTHandler;//We use this handler to post messages to
    //the background thread.
    private RoosterConnection mConnection;

    public static final String UI_AUTHENTICATED = "uiauthenticated";

    int error = 0;
    boolean isReg = false;

    public RoosterConnectionService() {

    }

    //Instance of inner class created to provide access  to public methods in this class
    private final IBinder MyBinder = new MyBinder();
    public static RoosterConnection.ConnectionState getState()
    {
        if (sConnectionState == null)
        {
            return RoosterConnection.ConnectionState.DISCONNECTED;
        }
        return sConnectionState;
    }

    public static RoosterConnection.LoggedInState getLoggedInState()
    {
        if (sLoggedInState == null)
        {
            return RoosterConnection.LoggedInState.LOGGED_OUT;
        }
        return sLoggedInState;
    }

    private void initConnection()
    {
//        if (intent == null){
//
//        }

        if( getmRoosterConnection() == null)
        {
            setmConnection(new RoosterConnection(this));
        }

        login();


//        String intentMsg = intent.getStringExtra(CONNECTION_TYPE);
//        Log.d(TAG,"initConnection()");
//
//        if(intentMsg.equals("login"))
//        {
//            if( getmRoosterConnection() == null)
//            {
//                mConnection = new RoosterConnection(this);
//            }
//            login();
//        }
//            singUp();
    }

    private void login()
    {
        Log.d(TAG,"login()");
        try
        {
            getmRoosterConnection().login();
        }catch (IOException e) {
            error = R.string.login_IO_error;
            Log.e(TAG,e.toString());
            e.printStackTrace();
        } catch (InterruptedException e) {
            error = R.string.login_Interruption_error;
            Log.e(TAG,e.toString());
            e.printStackTrace();
        } catch (SmackException e) {
            error = R.string.login_connection_error;
            Log.e(TAG,e.toString());
            e.printStackTrace();
        } catch (XMPPException e) {
            error = R.string.login_username_password_error;
            Log.e(TAG,e.toString());
            e.printStackTrace();
        }

        sendLoginSignUpState(error, isReg);

        if (error != 0){
            //Stop the service all together.
            stopSelf();
        } else {
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .edit()
                    .putBoolean("xmpp_logged_in", true)
                    .apply();
        }
    }

//    private void singUp()
//    {
//        Log.d(TAG,"signup()");
//        String displayName = intent.getStringExtra(DISPLAY_NAME);
//        mConnection = new RoosterConnection(this,displayName);
//
//        try {
//            getmRoosterConnection().registerUser();
//        } catch (InterruptedException e) {
//            error = R.string.sign_up_Interruption_error;
//            Log.e(TAG,e.toString());
//        } catch (XMPPException e) {
//            error = R.string.sign_up_phone_error;
//            Log.e(TAG,e.toString());
//        } catch (SmackException e) {
//            error = R.string.sign_up_connection_error;
//            Log.e(TAG,e.toString());
//        } catch (IOException e) {
//            error = R.string.sign_up_IO_error;
//            Log.e(TAG,e.toString());
//        }
//
//        if (error != 0){
//            sendLoginSignUpState(error, isReg);
//            //Stop the service all together.
//            stopSelf();
//        }else{
//            isReg = true;
//            login();
//        }
//    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return MyBinder;
    }

    /**
     This method is  Called when activity have disconnected from a particular interface published by the service.
     Note: Default implementation of the  method just  return false */
    @Override
    public boolean onUnbind(Intent intent) {
        stopSelf();
        return true;
    }

    /**
     * Called when an activity is connected to the service, after it had
     * previously been notified that all had disconnected in its
     * onUnbind method.  This will only be called by system if the implementation of onUnbind method was overridden to return true.
     */
    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate()");
    }


    public void start()
    {
        Log.d(TAG,"Service Start() function called.");

            if(!mActive) {
            mActive = true;
            if( mThread ==null || !mThread.isAlive())
            {
                mThread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        Looper.prepare();
                        mTHandler = new Handler();
                        initConnection();
                        //THE CODE HERE RUNS IN A BACKGROUND THREAD.
                        Looper.loop();

                    }
                });
                mThread.start();
            }

        }


    }

    public void stop()
    {
        Log.d(TAG,"stop()");
        mActive = false;
        if (mTHandler != null) {
            mTHandler.post(new Runnable() {
                @Override
                public void run() {
                    if( getmRoosterConnection() != null)
                    {
                        getmRoosterConnection().disconnect();
//                        PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
//                                .edit()
//                                .putBoolean("xmpp_logged_in", false)
//                                .apply();
                    }
                }
            });
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand()");
        this.intent = intent;
        start();
        return Service.START_STICKY;
        //RETURNING START_STICKY CAUSES OUR CODE TO STICK AROUND WHEN THE APP ACTIVITY HAS DIED.
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(),this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        startService(restartServiceIntent);
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy()");
        super.onDestroy();
        stop();
    }

    public void sendLoginSignUpState(int error, boolean isReg)
    {
        Intent intent = new Intent("com.example.explorelimu.START_MAIN");
//        intent.putExtra("loginError", error);
//        intent.putExtra("regFlag", isReg);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public void sendPicUpdateState(int error)
    {
        Intent intent = new Intent("picUpdateState");
        intent.putExtra("picUpdateError", error);
//        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    public int randomGenerator(){

        Random randomNumber = new Random();

        return randomNumber.nextInt();

    }
    public XMPPTCPConnection getmConnection()
    {
        if (getmRoosterConnection() == null) {
            start();
        }

        return getmRoosterConnection().getmConnection();
    }

    public RoosterConnection getmRoosterConnection() {
        return mConnection;
    }

    public void setmConnection(RoosterConnection mConnection) {
        this.mConnection = mConnection;
    }

    public class MyBinder extends Binder {

        public RoosterConnectionService getService() {
            return RoosterConnectionService.this;
        }
    }
}