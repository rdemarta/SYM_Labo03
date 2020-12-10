package com.example.sym_labo03;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.sym_labo03.activities.BarcodeActivity;
import com.example.sym_labo03.activities.NfcActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button barcodeBtn = findViewById(R.id.main_barcode_btn);
        Button ibeaconBtn = findViewById(R.id.main_ibeacon_btn);
        Button nfcBtn = findViewById(R.id.main_nfc_btn);

        barcodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BarcodeActivity.class);
                startActivity(intent);
            }
        });

/*        ibeaconBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.class, )
                startActivity(intent);
            }
        });*/

        nfcBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NfcActivity.class);
                startActivity(intent);
            }
        });
    }
}