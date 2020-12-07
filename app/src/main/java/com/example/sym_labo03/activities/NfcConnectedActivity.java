// Authors: Robin Demarta, Loïc Dessaules, Chau Ying Kot

package com.example.sym_labo03.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;
import android.widget.Toast;

import com.example.sym_labo03.NfcReader;
import com.example.sym_labo03.R;

import java.lang.ref.WeakReference;
import java.sql.Timestamp;

public class NfcConnectedActivity extends AppCompatActivity {

    private NfcAdapter mNfcAdapter;
    private NfcConnectedActivity.NfcHandler handler;
    private Timestamp lastScan;

    private static final String TAG = "NFC_CONNECTED_ACTIVITY";
    private static final int MAX_SECURITY_TIMEOUT_S = 10;
    private static final int MED_SECURITY_TIMEOUT_S = 20;
    private static final int MIN_SECURITY_TIMEOUT_S = 30;

    private enum SecurityLevel {
        MAX, MED, MIN
    }

    private static class NfcHandler extends Handler {
        private final WeakReference<NfcConnectedActivity> mActivity;
        public NfcHandler(NfcConnectedActivity activity) {
            super(Looper.getMainLooper());
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            NfcConnectedActivity activity = mActivity.get();

            // Check read values
            if (activity != null && NfcReader.verifyValues(msg.getData().getStringArrayList("results"))) {
                // Reset security to max
                activity.resetLastScan();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc_connected);

        // NFC reading handler
        handler = new NfcConnectedActivity.NfcHandler(NfcConnectedActivity.this);

        Button btnMax = findViewById(R.id.nfcConnected_max_btn);
        Button btnMed = findViewById(R.id.nfcConnected_med_btn);
        Button btnMin = findViewById(R.id.nfcConnected_min_btn);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        // Indicates timeouts in buttons' labels
        btnMax.setText(getString(R.string.nfc_max_btn, MAX_SECURITY_TIMEOUT_S));
        btnMed.setText(getString(R.string.nfc_max_btn, MED_SECURITY_TIMEOUT_S));
        btnMin.setText(getString(R.string.nfc_max_btn, MIN_SECURITY_TIMEOUT_S));

        resetLastScan();

        // Display different security levels status (sufficient or not)
        btnMax.setOnClickListener(v -> displaySecurityCheck(SecurityLevel.MAX));
        btnMed.setOnClickListener(v -> displaySecurityCheck(SecurityLevel.MED));
        btnMin.setOnClickListener(v -> displaySecurityCheck(SecurityLevel.MIN));
    }

    @Override
    protected void onResume() {
        super.onResume();
        NfcReader.setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        NfcReader.stopForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        new NfcReader(intent, handler);
    }

    /**
     * Check if the given level is currently reached or not by comparing last scan's timestamp with current one.
     * @param targetLevel level to be verified
     * @return true if the level is accessible.
     */
    private boolean checkSecurity(SecurityLevel targetLevel) {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        long elapsedSeconds = (currentTimestamp.getTime() - lastScan.getTime()) / 1000;

        switch (targetLevel) {
            case MAX:
                if(elapsedSeconds > MAX_SECURITY_TIMEOUT_S) return false;
            case MED:
                if(elapsedSeconds > MED_SECURITY_TIMEOUT_S) return false;
            case MIN:
                if(elapsedSeconds > MIN_SECURITY_TIMEOUT_S) return false;
        }

        return true;
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

    /**
     * Sets the lastScan timestamp to right now.
     */
    private void resetLastScan() {
        lastScan = new Timestamp(System.currentTimeMillis());
        Toast.makeText(this, "Security updated", Toast.LENGTH_SHORT).show();
    }
}