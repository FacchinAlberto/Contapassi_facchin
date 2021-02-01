package com.example.contapassi;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SecondActivity extends AppCompatActivity {

    private TextView txt_timer, txt_warning;
    private Intent t = getIntent();
    private int random_steps = t.getIntExtra("random_steps", 1);
    private int current_steps = t.getIntExtra("current_steps", 1);
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_second);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TextView txt_challenge = (TextView) findViewById(R.id.txt_challenge);
        txt_challenge.setText("Esegui "+random_steps+" passi");

        txt_timer = (TextView) findViewById(R.id.txt_timer);
        txt_timer.setBackgroundColor(getResources().getColor(R.color.yellow));

        TextView lbl_timer = (TextView) findViewById(R.id.lbl_timer);
        txt_warning = (TextView) findViewById(R.id.txt_warning);
        txt_warning.setText("Stai al passo");

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        Intent service = new Intent(SecondActivity.this, MyService.class);
        startService(service);
    }

    //metodo per settare il testo della TextView relativa al timer ad ogni iterazione del ciclo del thread del service
    public void setText(String s) {
        txt_timer.setText(s);
        progressBar.setProgress(Integer.parseInt(s));

        if (s.equals("0")) { //controllo se il tempo è scaduto
            stopService(new Intent(SecondActivity.this, MyService.class));

            //AlertDialog che fa visualizzare all'utente l'esito della sfida, se è stata superata oppure no
            AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
            builder.setTitle("Esito della sfida");
            builder.setCancelable(false);

            if (MainActivity.getTotalSteps() < (random_steps + current_steps)) {
                MainActivity.azzeraTotalSteps();
                builder.setMessage("Purtoppo la sfida non è stata superata");
            }
            else {
                builder.setMessage("Sfida superata con successo");
            }

            //neutral button
            builder.setNeutralButton("Chiudi", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();
        }
        else if (s.equals(String.valueOf(30))) {    //controllo se mancano 30 secondi
            txt_timer.setBackgroundColor(getResources().getColor(R.color.red));
            txt_warning.setText("Non rimane molto tempo...");
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Esci");
        builder.setMessage("Vuoi uscire?");
        builder.setCancelable(true);

        //neutral button
        builder.setNeutralButton("Chiudi", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}