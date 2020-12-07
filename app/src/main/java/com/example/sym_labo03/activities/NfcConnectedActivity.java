package com.example.sym_labo03.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
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
            if (activity != null && NfcReader.verifyValues(msg.getData().getStringArrayList("results"))) {
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

        resetLastScan();

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

    /**
     * Sets the lastScan timestamp to right now.
     */
    private void resetLastScan() {
        lastScan = new Timestamp(System.currentTimeMillis());
        Toast.makeText(this, "Security updated", Toast.LENGTH_SHORT).show();
    }
}