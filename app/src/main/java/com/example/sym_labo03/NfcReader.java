// Authors: Robin Demarta, Loïc Dessaules, Chau Ying Kot

package com.example.sym_labo03;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

public class NfcReader {

    private Intent intent;
    private Handler handler;

    private static final String TAG = "NFC_UTILS";
    private static final String MIME_TEXT_PLAIN = "text/plain";
    private static final String[] NFC_PWD = new String[]{"test", "é è ê ë ē", "♤ ♡ ♢ ♧"};

    public NfcReader(Intent intent, Handler handler) {
        this.handler = handler;
        this.intent = intent;

        handleIntentReadNfc();
    }

    /**
     * Check if the given list contains at least one of the secret String values.
     * @param values the list to be verified.
     * @return true if a String could be found.
     */
    public static boolean verifyValues(ArrayList<String> values) {
        for(String v : NFC_PWD) {
            if(values.contains(v)) {
                return true;
            }
        }
        return false;
    }

    // Source: https://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278
    public void handleIntentReadNfc() {
        String action = intent.getAction();
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        // Prepare reading thread
        Thread readNfc = new Thread(() -> {
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
        });

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {
                readNfc.start();
            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }
        }
    }

    // Source: https://code.tutsplus.com/tutorials/reading-nfc-tags-with-android--mobile-17278
    public String readText(NdefRecord record) throws UnsupportedEncodingException {
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


    public static void setupForegroundDispatch(Activity activity, NfcAdapter mNfcAdapter) {
        if(mNfcAdapter == null)
            return;

        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent =
                PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);
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
        mNfcAdapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    public static void stopForegroundDispatch(Activity activity, NfcAdapter nfcAdapter) {
        if(nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(activity);
    }
}
