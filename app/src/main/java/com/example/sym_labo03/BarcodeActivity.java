package com.example.sym_labo03;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.journeyapps.barcodescanner.DefaultDecoderFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class BarcodeActivity extends AppCompatActivity {
    private String lastText;

    private DecoratedBarcodeView barcodeView;

    private final ImageView preview = findViewById(R.id.barcode_preview_iv);

    private final TextView decodedText = findViewById(R.id.barcode_decoded_text_tv);

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() == null || result.getText().equals(lastText)) {
                // Prevent duplicate scans
                return;
            }

            lastText = result.getText();

            // Set the thumbnail
            preview.setImageBitmap(result.getBitmapWithResultPoints(Color.YELLOW));

            // Set the decrypted text
            decodedText.setText(result.getText());
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);

        barcodeView = findViewById(R.id.barcode_scanner);

        // Set the list of format that our scanner can handle
        Collection<BarcodeFormat> formats = Arrays.asList(BarcodeFormat.QR_CODE, BarcodeFormat.CODE_39);

        // Set the formats
        barcodeView.getBarcodeView().setDecoderFactory(new DefaultDecoderFactory(formats));

        // Init the barcode
        barcodeView.initializeFromIntent(getIntent());

        // Scan continuously
        barcodeView.decodeContinuous(callback);
    }

    @Override
    protected void onResume() {
        super.onResume();

        barcodeView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        barcodeView.pause();
    }


    // the application need this override to work correctly
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }
}

