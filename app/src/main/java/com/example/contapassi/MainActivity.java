package com.example.contapassi;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private int daily_steps = 0, random_steps = 0, min, max;    //min e max servono per generare numeri random
    private static int total_steps = 0;
    private SensorManager sensorManager;
    private TextView lbl_daily, txt_daily, lbl_total, lbl_progress;
    private static TextView txt_total;  //TextView in cui vengono stamapti i passi totali
    private Random random;
    private ProgressBar progressBar;
    private int progresses = 0;
    private SharedPreferences ts, day, ds, pr;      //ts = total_steps, day = giorno corrente, = ds = daily_steps
    private Date currentTime = Calendar.getInstance().getTime();
    private int current_day;    //giorno corrente per il controllo dei passi giornalieri
    private Sensor counter;
    private boolean sa = false;

    public static void azzeraTotalSteps () {    //azzera i passi totali, utilizzata quando la sfida non viene superata
        total_steps = 0;
        txt_total.setText(String.valueOf(0));
    }

    public static int getCurrentSteps () {
        return total_steps;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);   //non fa andare lo schermo in blocco
        lbl_daily = (TextView) findViewById(R.id.lbl_daily);
        txt_daily = (TextView) findViewById(R.id.txt_daily);
        lbl_total = (TextView) findViewById(R.id.lbl_total);
        txt_total = (TextView) findViewById(R.id.txt_total);
        lbl_progress = (TextView) findViewById(R.id.lbl_progress);
        Button btn_azzera = (Button) findViewById(R.id.btn_azzera);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        counter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){
            //chiede il permesso di usare il sensore
            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

        ts = getPreferences(Context.MODE_PRIVATE);
        ds = getPreferences(Context.MODE_PRIVATE);
        day = getPreferences(Context.MODE_PRIVATE);
        pr = getPreferences(Context.MODE_PRIVATE);

        //bottone per azzerare i passi giornalieri o totali con alert dialog per la selezione
        btn_azzera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((total_steps != 0) || (daily_steps != 0)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("Azzera");
                    builder.setMessage("Quale passi si vogliono azzerare?");
                    builder.setCancelable(true);

                    //positive button
                    builder.setPositiveButton("Giornalieri", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (daily_steps != 0) {
                                daily_steps = 0;
                                txt_daily.setText(String.valueOf(daily_steps));
                            }
                            else {
                                Toast.makeText(MainActivity.this, "Variabile non azzerabile", Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        }
                    });

                    //negative button
                    builder.setNegativeButton("Totali", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (total_steps != 0) {
                                total_steps = 0;
                                txt_total.setText(String.valueOf(total_steps));
                            }
                            else {
                                Toast.makeText(MainActivity.this, "Variabile non azzerabile", Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                else
                    Toast.makeText(MainActivity.this, "Nessuna variabile da azzerare", Toast.LENGTH_SHORT).show();
            }
        });

        total_steps = ts.getInt("total_steps", total_steps);
        daily_steps = ds.getInt("daily_steps", daily_steps);
        txt_daily.setText(String.valueOf(daily_steps));
        txt_total.setText(String.valueOf(total_steps));
        current_day = day.getInt("current_day", current_day);
/*
        if (currentTime.getDay() != current_day)
            daily_steps = 0;
        else
            daily_steps = ds.getInt("daily_steps", daily_steps);

 */
        progresses = pr.getInt("progresses", progresses);
        progressBar.setProgress(total_steps%progressBar.getMax());
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {    //gestione del sensore, incrementa le variabili dei passi giornalieri e totali
        if (progresses == progressBar.getMax()) {  //controlla se la progressBar è arrivata alla fine
            random = new Random();
            max = 45;
            min = 15;
            random_steps = random.nextInt((max - min) + 1) + min; //numero randon di passgi generati tra min e max
            if (sa == false) {  //controllo se non è gia stata avviata una SecondActivity
                sa = true;
                Intent t = new Intent(MainActivity.this, SecondActivity.class);
                List<Integer> aus = new ArrayList<Integer>();   //ArrayList ausiliaria per passare le variabili alla SecondActivity
                aus.add(random_steps);
                aus.add(total_steps);
                t.putIntegerArrayListExtra("aus", (ArrayList<Integer>)aus);     //passa aus alla SecondActivity
                startActivity(t);   //parte activity della sfida
            }
            progresses = 0;
            progressBar.setProgress(progresses); //setta a 0 il valore dei progressi della progresssBar
        }
        else {  //altrimenti continua a contare i passi
            startCount();
            daily_steps++;  //incrementa i passi giornalieri
            total_steps++;  //passi totali
            progresses++;   //i progressi della progressBar
            txt_daily.setText(String.valueOf(daily_steps)); //setta il valore della TextView in base al valore dei passi
            txt_total.setText(String.valueOf(total_steps)); //in base al valore dei passi totali
            progressBar.setProgress(progresses);    //setta l'avanzamento della progressBar
        }
    }

    @Override
    protected void onStart() {  //recupero dei parametri salvati con le SharedPreferences
        super.onStart();
        sa = false;
    }

    //il sensore parte a rilevare
    protected void startCount () {
        if (counter != null)
            sensorManager.registerListener(this, counter, SensorManager.SENSOR_DELAY_FASTEST);
        else
            Toast.makeText(this, "Sensor not found", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume() { //continua a contare i passi
        super.onResume();
        sa = false;
        startCount();
    }

    @Override
    protected void onPause() {      //continua a contare i passi
        super.onPause();
        startCount();
    }

    @Override
    protected void onStop() {   //salvataggio delle variabili
        super.onStop();
        startCount();
    }

    @Override
    public void onBackPressed() {   //quando viene premuto il pulsante per tornare indietro viene verificata l'intenzionalità dell'azione
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Esci");
        builder.setMessage("Vuoi uscire?");
        builder.setCancelable(true);

        //neutral button
        builder.setNeutralButton("Chiudi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finishAffinity();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("total_steps", total_steps);
        outState.putInt("daily_steps", daily_steps);
        outState.putInt("progresses", progresses);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        total_steps = savedInstanceState.getInt("total_steps");
        txt_total.setText(String.valueOf(total_steps));
        daily_steps = savedInstanceState.getInt("daily_steps");
        txt_daily.setText(String.valueOf(daily_steps));
        progresses = savedInstanceState.getInt("progresses");
        progressBar.setProgress(progresses);
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
        //total_steps
        SharedPreferences.Editor editor_ts = ts.edit();
        editor_ts.putInt("total_steps", total_steps);
        editor_ts.commit();

        //daily_steps
        SharedPreferences.Editor editor_ds = ds.edit();
        editor_ds.putInt("daily_steps", daily_steps);
        editor_ds.commit();

        //giorno corrente
        SharedPreferences.Editor editor_day = day.edit();
        current_day = currentTime.getDay();
        editor_day.putInt("current_day", current_day);
        editor_day.commit();

        //progressi
        SharedPreferences.Editor editor_pr = pr.edit();
        editor_pr.putInt("progresses", progresses);
        editor_pr.commit();
    }
}