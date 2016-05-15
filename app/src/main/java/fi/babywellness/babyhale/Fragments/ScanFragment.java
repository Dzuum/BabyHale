package fi.babywellness.babyhale.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.welcu.android.zxingfragmentlib.BarCodeScannerFragment;

import fi.babywellness.babyhale.ProductActivity;
import fi.babywellness.babyhale.R;

public class ScanFragment extends BarCodeScannerFragment {

    private String vnr;

    public static ScanFragment newInstance() {
        return new ScanFragment();
    }

    public ScanFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstance) {
        View view = inflater.inflate(R.layout.capture, container, false);

        TextView scanHelpText = (TextView) view.findViewById(R.id.scan_help_text);
        scanHelpText.setText(R.string.search_scan_help);

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setmCallBack(new IResultCallback() {
            @Override
            public void result(Result lastResult) {
                onSearch(lastResult.getBarcodeFormat(), lastResult.toString());
            }
        });
    }

    private void onSearch(BarcodeFormat format, String barcode) {
        vnr = extractVNR(format, barcode);
        Intent intent = new Intent(getContext(), ProductActivity.class);
        intent.putExtra(ProductActivity.VNR_SEARCH, vnr);
        startActivity(intent);
    }

    //TODO: Javadoc, jossa linkki VnrWikiin
    //TODO: Also kattella, oisko muita formaattei käytetty
    private String extractVNR(BarcodeFormat format, String result) {
        String vnr = "";

        if (format == BarcodeFormat.EAN_13 || format == BarcodeFormat.EAN_8)
            vnr = result.substring(result.length() - 7, result.length() - 1);
        else if (format == BarcodeFormat.CODE_39)
            vnr = String.valueOf(result); //TODO: Varmistaa, että miten kopioida arvo oikeellisesti

        return vnr;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getView() != null) {
            getView().post(new Runnable() {
                @Override
                public void run() {
                    try {
                        getCameraManager().setManualFramingRect(getView().getMeasuredWidth(), getView().getMeasuredHeight());
                    } catch (NullPointerException npe) {
                    }
                }
            });
        }
    }

    @Override
    public int getRequestedCameraId() {
        return -1;
    }
}
