package com.example.contapassi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class SecondActivity extends AppCompatActivity {

    private static TextView txt_timer, txt_warning, txt_challenge;
    private static int random_steps, total_steps;
    private boolean superata = true;    //indica se la sfida è stata superata

    public static void setTextWarning(String s) {
        txt_warning.setText(s);
    }

    //metodo per settare il testo della TextView relativa al timer ad ogni iterazione del ciclo del thread del service
    public static void setTextTimer(String s) {
        if (s.equals("0")){
            setTextWarning("Clicca qui");
            txt_warning.setTextSize(24);
        }
        txt_timer.setText(s);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_second);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent t = getIntent();
        ArrayList<Integer> aus = t.getIntegerArrayListExtra("aus");
        random_steps = aus.get(0);
        total_steps = aus.get(1);

        txt_challenge = (TextView) findViewById(R.id.txt_challenge);
        txt_challenge.setText("Esegui "+random_steps+" passi");

        txt_timer = (TextView) findViewById(R.id.txt_timer);
        txt_timer.setVisibility(View.INVISIBLE);
        Button btn_timer = (Button) findViewById(R.id.btn_timer);
        btn_timer.setVisibility(View.VISIBLE);
        Button btn_home = (Button) findViewById(R.id.btn_home);
        TextView lbl_timer = (TextView) findViewById(R.id.lbl_timer);
        txt_warning = (TextView) findViewById(R.id.txt_warning);
        txt_warning.setText("Stai al passo");

        btn_timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickStartService(v);
                txt_timer.setVisibility(View.VISIBLE);
                btn_timer.setVisibility(View.GONE);
            }
        });

        btn_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(SecondActivity.this);
                builder.setTitle("Esito sfida");
                builder.setCancelable(false);

                if (txt_timer.getText().equals("0")) {  //controlla se il tempo è esaurito
                    /*
                    controlla se i passi nel momento in cui finisce la sfida sono minori
                    dei passi prima della sida sommati ai passi da compiere
                    */
                    if (MainActivity.getCurrentSteps() < (total_steps + random_steps)) {
                        MainActivity.azzeraTotalSteps();    //azzera i passi totali
                        builder.setMessage("Sfida non superata");
                    } else {
                        builder.setMessage("Sfida superata");
                    }
                    //neutral button
                    builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            finish();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                else    //altrimenti impedisce all'utente di uscire finchè non finisce la sfida
                    Toast.makeText(SecondActivity.this, "Termina la sfida", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClickStartService(View widget)
    {
        startService(new Intent(SecondActivity.this, MyService.class));
    }

    @Override
    public void onBackPressed() {   //quando viene premuto il pulsante per tornare indietro viene verificata l'intenzionalità dell'azione
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Impossibile uscire");
        builder.setMessage("Premere home per uscire");
        builder.setCancelable(true);

        //neutral button
        builder.setNeutralButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}