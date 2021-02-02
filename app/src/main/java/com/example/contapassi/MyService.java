package com.example.contapassi;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

public class MyService extends Service {
    private static final String TAG = "FacchinService";
    // indica se il servizio e’ attivo
    private boolean isRunning  = false;

    private int i;

    // costruttore
    public MyService() {
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");
    }

    // collegamento con altre APP non necessario (ma il metodo onBind va implementato cmq)
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Service onBind");
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "Sfida iniziata", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Service onStartCommand");
        isRunning = true;
        // createMessageDialog().show();
        // Qui viene avviato il thread che implementa il servizio in background:
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Oggetto utilizzato per comunicare con il thread proncipale (UIThread)
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
                    // il metodo post serve a inviare un messaggio al UIThread
                    // (in questo caso viene inviato un oggetto eseguibile cioè Runnable)
                    handler.post(new Runnable() { // the runnable object
                        @Override
                        public void run() {
                            SecondActivity.setTextTimer(String.valueOf(i));
                            if (i == 30) {
                                SecondActivity.setTextWarning("Non fermarti");
                            }
                        }
                    });
                }
                handler.post(new Runnable() { // the runnable object
                    @Override
                    public void run() {
                        Toast.makeText(MyService.this, "Tempo scaduto", Toast.LENGTH_LONG).show();
                    }
                });

                //Il servizio si arresta una volta terminati i tasks
                stopSelf();
            }
        }).start();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service onDestroy");
        // qui deve essere terminato il thread che implementa il servizio in background
        isRunning = false;
    }
}