package info.plocharz.nextbikeclient;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import java.util.ArrayList;
import java.util.Collections;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

/**
 * Created by Krzysztof on 2017-03-09.
 */

@SuppressWarnings("deprecation")
public class BarcodeScannerFragment extends Fragment implements ZXingScannerView.ResultHandler {
    private static final int CAMERA_PERMISSION = 1;
    private ZXingScannerView mScannerView;
    private boolean started;
    private boolean permission_granted = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mScannerView = new ZXingScannerView(this.getContext());
        mScannerView.setFormats(new ArrayList<BarcodeFormat>(
                Collections.singletonList(BarcodeFormat.QR_CODE)
        ));
        return mScannerView;
    }

    @Override
    public void onStart() {
        super.onStart();
        this.requestPermission();
    }

    private void requestPermission(){
        int permission_status = ContextCompat.checkSelfPermission(
                this.getContext(),
                Manifest.permission.CAMERA);
        if (permission_status == PackageManager.PERMISSION_DENIED) {
            this.permission_granted = false;
            this.requestPermissions(new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION);
        } else {
            this.permission_granted = true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.start();
    }

    private void start(){
        if(this.permission_granted) {
            this.started = true;
            mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
            mScannerView.startCamera();
        }
    }

    private boolean is_permission_granted(int[] grantResults){
        return grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case CAMERA_PERMISSION: {
                if (this.is_permission_granted(grantResults)) {
                    this.permission_granted = true;
                    this.start();
                } else {
                    this.permission_granted = false;
                }
            }
        }
    }

    @Override
    public void onPause() {
        if (this.started) {
            mScannerView.stopCamera();
        }
        super.onPause();
    }

    @Override
    public void handleResult(Result rawResult) {
        Uri uri = Uri.parse(rawResult.getText());
        if(! ((MainActivity) this.getActivity()).processUri(uri, true) )
            mScannerView.resumeCameraPreview(this);
    }
}
