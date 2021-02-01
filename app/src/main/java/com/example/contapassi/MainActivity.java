package com.example.contapassi;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private int daily_steps = 0, bonus = 100, n, random_steps, min, max;    //min e max servono per generare numeri random
    private static int total_steps = 0;
    private SensorManager sensorManager;
    private TextView lbl_daily, txt_daily, lbl_total, lbl_progress;
    private static TextView txt_total;  //TextView in cui vengono stamapti i passi totali
    private boolean walking = false;
    private Random random;
    private ProgressBar progressBar;
    private SharedPreferences ts, day, ds;      //ts = total_steps, day = giorno corrente, = ds = daily_steps
    private Date currentTime = Calendar.getInstance().getTime();
    private int current_day;    //giorno corrente per il controllo dei passi giornalieri

    public static void azzeraTotalSteps () {    //azzera i passi totali, utilizzata quando la sfida non viene superata
        total_steps = 0;
        txt_total.setText(String.valueOf(0));
    }

    public static int getTotalSteps () {
        return total_steps;
    }

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
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        ts = getPreferences(Context.MODE_PRIVATE);
        ds = getPreferences(Context.MODE_PRIVATE);
        day = getPreferences(Context.MODE_PRIVATE);

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
                                progressBar.setProgress(daily_steps);
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
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {    //gestione del sensore, incrementa le variabili dei passi giornalieri e totali
        if (walking) {
            if ((total_steps%progressBar.getMax()) != 0) {  //controllo se il numero di passi non è multiplo di 200
                daily_steps++;
                total_steps++;
                txt_daily.setText(String.valueOf(daily_steps));
                txt_total.setText(String.valueOf(total_steps));
                progressBar.setProgress(daily_steps);
            }
            else {  //fa partire la SecondActivity relativa alla sfida
                Intent t = new Intent(MainActivity.this, SecondActivity.class);
                random = new Random();
                max = 40;
                min = 10;
                random_steps = random.nextInt((max - min) + 1) + min;
                t.putExtra("random_steps", random_steps);
                t.putExtra("current_steps", total_steps);   //passi totali nel momento in cui parte la sifda
                startActivity(t);
                progressBar.setProgress(0); //setta a 0 il valore dei progressi della progresssBar
            }
        }

        // se numero di passi è multiplo del valore della variabile bonus si aggiunge o si toglie un numero random di passi compreso tra min e max
        if ((total_steps%bonus == 0) || (daily_steps%bonus == 0)){
            random = new Random();
            max = 25;
            min = -25;
            n = random.nextInt((max - min) + 1) + min;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("BONUS");
            if (n < 0)
                builder.setMessage("Che sfortuna, "+n+" passi :(");
            else
                builder.setMessage("Hai guadagnato "+n+" passi :)");
            builder.setCancelable(false);

            //neutral button
            builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    total_steps += n;
                    txt_total.setText(String.valueOf(total_steps));
                    dialog.dismiss();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    protected void onStart() {  //recupero dei parametri salvati con le SharedPreferences
        super.onStart();
        total_steps = ts.getInt("total_steps", total_steps);
        txt_total.setText(String.valueOf(total_steps));

        if (currentTime.getDay() != day.getInt("current_day", current_day))
            daily_steps = 0;
        else
            daily_steps = ds.getInt("daily_steps", daily_steps);

        txt_daily.setText(String.valueOf(daily_steps));
        progressBar.setProgress(daily_steps);
    }

    @Override
    protected void onResume() { //continua a contare i passi
        super.onResume();
        walking = true;
        Sensor counter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (counter != null)
            sensorManager.registerListener(this, counter, SensorManager.SENSOR_DELAY_UI);
        else
            Toast.makeText(this, "Sensor not found", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onPause() {      //continua a contare i passi
        super.onPause();
        walking = true;
        Sensor counter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (counter != null)
            sensorManager.registerListener(this, counter, SensorManager.SENSOR_DELAY_UI);
        else
            Toast.makeText(this, "Sensor not found", Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStop() {   //salvataggio delle variabili
        super.onStop();
        walking = true;
        Sensor counter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (counter != null)
            sensorManager.registerListener(this, counter, SensorManager.SENSOR_DELAY_UI);
        else
            Toast.makeText(this, "Sensor not found", Toast.LENGTH_LONG).show();

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
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        total_steps = savedInstanceState.getInt("total_steps");
        txt_total.setText(String.valueOf(total_steps));
        daily_steps = savedInstanceState.getInt("daily_steps");
        txt_daily.setText(String.valueOf(daily_steps));
    }

    @Override
    protected void onDestroy () {
        super.onDestroy();
    }
}