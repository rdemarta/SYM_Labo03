package com.example.sym_labo03.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.sym_labo03.R;

import java.sql.Time;
import java.sql.Timestamp;

public class NfcConnectedActivity extends AppCompatActivity {

    private Timestamp lastScan;

    private static final String TAG = "NFC_CONNECTED_ACTIVITY";
    private static final int MAX_SECURITY_TIMEOUT_S = 10;
    private static final int MED_SECURITY_TIMEOUT_S = 20;
    private static final int MIN_SECURITY_TIMEOUT_S = 30;

    private enum SecurityLevel {
        MAX, MED, MIN
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_connected);

        Button btnMax = findViewById(R.id.nfcConnected_max_btn);
        Button btnMed = findViewById(R.id.nfcConnected_med_btn);
        Button btnMin = findViewById(R.id.nfcConnected_min_btn);

        lastScan = new Timestamp(System.currentTimeMillis());

        btnMax.setOnClickListener(v -> displaySecurityCheck(SecurityLevel.MAX));
        btnMed.setOnClickListener(v -> displaySecurityCheck(SecurityLevel.MED));
        btnMin.setOnClickListener(v -> displaySecurityCheck(SecurityLevel.MIN));

        // TODO rescan NFC
    }

    /**
     * Check if the given level is currently reached or not.
     * @param targetLevel level to be verified
     * @return true if the level is reached.
     */
    private boolean checkSecurity(SecurityLevel targetLevel) {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        long elapsedSeconds = (currentTimestamp.getTime() - lastScan.getTime()) / 1000;
        boolean ok = true;

        Log.d(TAG, "*********************************");
        Log.d(TAG, "lastScan: " + lastScan + " now: " + currentTimestamp);
        Log.d(TAG, "elapsed: " + elapsedSeconds);

        switch (targetLevel) {
            case MAX:
                if(elapsedSeconds > MAX_SECURITY_TIMEOUT_S) ok = false;
                break;
            case MED:
                if(elapsedSeconds > MED_SECURITY_TIMEOUT_S) ok = false;
                break;
            case MIN:
                if(elapsedSeconds > MIN_SECURITY_TIMEOUT_S) ok = false;
                break;
        }

        return ok;
    }

    /**
     * Shows a Toast indicating wether the current security level is ok or not.
     * @param targetLevel level to be verified.
     */
    private void displaySecurityCheck(SecurityLevel targetLevel) {
        Toast.makeText(
                NfcConnectedActivity.this,
                checkSecurity(targetLevel) ? R.string.nfc_sufficient_level : R.string.nfc_insufficient_level,
                Toast.LENGTH_SHORT)
                .show();
    }
}