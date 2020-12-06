// Authors: Robin Demarta, Loïc Dessaules, Chau Ying Kot

package com.example.sym_labo03.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sym_labo03.R;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;

public class NfcActivity extends AppCompatActivity {

    private NfcAdapter mNfcAdapter;
    private NfcHandler handler;
    private TextView nfcLabel;
    private boolean nfcOk = false;

    private static final String TAG = "NFC_ACTIVITY";
    private static final String MIME_TEXT_PLAIN = "text/plain";
    private static final String USERNAME = "admin";
    private static final String PASSWORD = "secret";
    private static final String[] NFC_PWD = new String[]{"test", "é è ê ë ē", "♤ ♡ ♢ ♧"};

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

        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

                Thread readNfc = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Ndef ndef = Ndef.get(tag);

                        if (ndef != null) {
                            NdefMessage ndefMessage = ndef.getCachedNdefMessage();
                            NdefRecord[] records = ndefMessage.getRecords();
                            ArrayList<String> results = new ArrayList<>();

                            for (NdefRecord ndefRecord : records) { // Read every line
                                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                                    try {
                                        String result = readText(ndefRecord);
                                        results.add(result);
                                        Log.d(TAG, result);
                                    } catch (UnsupportedEncodingException e) {
                                        Log.e(TAG, "Unsupported Encoding", e);
                                    }
                                }
                            }

                            Message msg = handler.obtainMessage();
                            Bundle bundle = new Bundle();
                            bundle.putStringArrayList("results", results);
                            msg.setData(bundle);
                            handler.sendMessage(msg);
                        }
                    }
                });
                readNfc.start();

            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        } /*else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            String[] techList = tag.getTechList();
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                if (searchedTech.equals(tech)) {
                    //new NdefReaderTask().execute(tag); TODO ?
                    break;
                }
            }
        }*/
    }

    /**
     * Verify if the given list of messages contains a correct pass.
     * @param values list of String value to ve verified.
     */
    private void verifyNfcValues(ArrayList<String> values) {
        nfcOk = false;

        // Search for correct value
        for(String v : NFC_PWD) {
            if(values.contains(v)) {
                nfcOk = true;
                break;
            }
        }

        // Grant access if value found
        if(nfcOk) {
            nfcLabel.setText(R.string.nfc_read_ok);
            nfcLabel.setTextColor(Color.GREEN);
        } else {
            nfcLabel.setText(R.string.nfc_read_wrong);
            nfcLabel.setTextColor(Color.RED);
        }
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
        int languageCodeLength = payload[0] & 51;

        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        // e.g. "en"

        // Get the Text
        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
    }
}