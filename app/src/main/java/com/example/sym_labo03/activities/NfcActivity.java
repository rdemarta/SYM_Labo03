// Authors: Robin Demarta, Loïc Dessaules, Chau Ying Kot

package com.example.sym_labo03.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sym_labo03.NfcReader;
import com.example.sym_labo03.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class NfcActivity extends AppCompatActivity {

    private NfcAdapter mNfcAdapter;
    private NfcHandler handler;
    private TextView nfcLabel;
    private boolean nfcOk = false;

    private static final String TAG = "NFC_ACTIVITY";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "secret";

    private static class NfcHandler extends Handler {
        private final WeakReference<NfcActivity> mActivity;
        public NfcHandler(NfcActivity activity) {
            super(Looper.getMainLooper());
            mActivity = new WeakReference<>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            NfcActivity activity = mActivity.get();
            if (activity != null) {
                // Give NFC results to activity and proceed to verification
                activity.verifyNfcValues(msg.getData().getStringArrayList("results"));
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        // NFC reading handler
        handler = new NfcHandler(NfcActivity.this);

        // Inputs
        Button btnConnect = findViewById(R.id.nfc_connect_btn);
        EditText etUsername = findViewById(R.id.nfc_username_input);
        EditText etPassword = findViewById(R.id.nfc_password_input);
        nfcLabel = findViewById(R.id.nfc_label);
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            Toast.makeText(this, R.string.nfc_not_supported, Toast.LENGTH_LONG).show();
        }

        // Connect button: verify all inputs
        btnConnect.setOnClickListener(v -> {
            // Verify username and password
            if(etUsername.getText().toString().equals(USERNAME) && etPassword.getText().toString().equals(PASSWORD)) {
                // Verify NFC
                if(nfcOk) {
                    // Go to next activity
                    Intent intent = new Intent(NfcActivity.this, NfcConnectedActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, R.string.nfc_read_wrong, Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(this, R.string.nfc_bad_credentials, Toast.LENGTH_SHORT).show();
            }
        });
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
     * Verify if the given list of messages contains a correct pass and update UI.
     * @param values list of String value to be verified.
     */
    private void verifyNfcValues(ArrayList<String> values) {
        nfcOk = NfcReader.verifyValues(values);

        // Grant access if value found
        if(nfcOk) {
            nfcLabel.setText(R.string.nfc_read_ok);
            nfcLabel.setTextColor(Color.GREEN);
        } else {
            nfcLabel.setText(R.string.nfc_read_wrong);
            nfcLabel.setTextColor(Color.RED);
        }
    }

}