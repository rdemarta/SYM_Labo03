// Authors: Robin Demarta, Loïc Dessaules, Chau Ying Kot

package com.example.sym_labo03.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.sym_labo03.R;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

public class NfcActivity extends AppCompatActivity {

    private NfcAdapter mNfcAdapter;

    private ArrayList<String> readNfcValues = new ArrayList<>();
    private boolean nfcOk = false;

    private static final String TAG = "NFC_ACTIVITY";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "secret";
    private static final String[] NFC_PWD = new String[]{"test", "é è ê ë ē", "♤ ♡ ♢ ♧"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nfc);

        // Inputs
        Button btnConnect = findViewById(R.id.nfc_connect_btn);
        EditText etUsername = findViewById(R.id.nfc_username_input);
        EditText etPassword = findViewById(R.id.nfc_password_input);
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
        setupForegroundDispatch();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopForegroundDispatch();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndef = Ndef.get(tag);
        NdefMessage ndefMessage = ndef.getCachedNdefMessage();

        for (NdefRecord ndefRecord : ndefMessage.getRecords()) {
            if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                try {
                    // Read and store string
                    String readValue = readText(ndefRecord);
                    readNfcValues.add(readValue);

                    // Check value correctness
                    for (String s : NFC_PWD) {
                        if (readValue.equals(s)) {
                            nfcOk = true;
                            break;
                        }
                    }

                    Log.e(TAG, readValue);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }

        Toast.makeText(this, nfcOk ? R.string.nfc_read_ok : R.string.nfc_read_wrong, Toast.LENGTH_SHORT).show();
    }

    // Source: https://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278
    private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * http://www.nfc-forum.org/specs/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

        byte[] payload = record.getPayload();

        // Get the Text Encoding
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

        // Get the Language Code
        int languageCodeLength = payload[0] & 0063;

        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        // e.g. "en"

        // Get the Text
        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
    }

    private void setupForegroundDispatch() {
        if(mNfcAdapter == null)
            return;

        final Intent intent = new Intent(this.getApplicationContext(),this.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent =
                PendingIntent.getActivity(this.getApplicationContext(), 0, intent, 0);
        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // We only want NDEF format tags
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType("text/plain");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            Log.e(TAG, "MalformedMimeTypeException", e);
        }
        mNfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, techList);
    }

    private void stopForegroundDispatch() {
        if(mNfcAdapter != null)
            mNfcAdapter.disableForegroundDispatch(this);
    }
}