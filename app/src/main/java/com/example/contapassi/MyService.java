package com.example.contapassi;

import android.app.Dialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class MyService extends Service {
    private static final String TAG = "FacchinService";
    private Thread t;
    private static boolean isRunning  = false;
    private int i;
    private SecondActivity sa;

    public static boolean Running () {
        return isRunning;
    }

    public MyService() {

    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Servizio avviato.", Toast.LENGTH_LONG).show();
        Log.i(TAG, "Service onStartCommand");
        isRunning = true;

        t = new Thread(new Runnable() {
            @Override
            public void run() {

                Handler handler = new Handler(Looper.getMainLooper());

                String fullstops = "";

                for (i = 60; i > 0; i--) {

                    try {
                        Thread.sleep(1000);
                    } catch (Exception e) {
                    }

                    fullstops = fullstops + ".";
                    if(isRunning){
                        Log.i(TAG, "Service running"+fullstops);
                    }
                    else{
                        break;
                    }
                    handler.post(new Runnable()
                    {
                        @Override
                        public void run() {
                            sa.setText(i+"");
                        }
                    });
                }
                handler.post(new Runnable() { // the runnable object
                    @Override
                    public void run() { /*
                        AlertDialog.Builder builder = new AlertDialog.Builder(MyService.this);
                        builder.setTitle("Avviso");
                        builder.setMessage("Tempo scaduto");
                        builder.setCancelable(false);

                        //neutral button
                        builder.setNeutralButton("Chiudi", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                sa.finish();
                            }
                        });
                        AlertDialog alert = builder.create();
                        alert.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
                        */
                    }
                });
                stopSelf();
            }
        });
        t.start();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service onDestroy");
        isRunning = false;
    }
}