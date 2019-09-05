package com.practice.phuc.wifisharing.view.fragment;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.practice.phuc.wifisharing.MainActivity;
import com.practice.phuc.wifisharing.R;
import com.practice.phuc.wifisharing.model.SecurityType;
import com.practice.phuc.wifisharing.presenter.GeneratorPresenter;
import com.practice.phuc.wifisharing.presenter.GeneratorPresenterImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GeneratorFragment extends Fragment implements GeneratorView {
    private Context context;
    private WifiManager wifiManager;
    private GeneratorPresenter presenter;

    private List<String> ssidsValues;
    private ArrayAdapter<String> ssidsAdapter;
    private List<SecurityType> securityTypeValues;
    private ArrayAdapter<SecurityType> securityAdapter;

    private TextView tvSsid;
    private Spinner spSsid;
    private TextView tvPassword;
    private EditText etPassword;
    private TextView tvSecurity;
    private Spinner spSecurity;
    private Button btGenerate;
    private Button btSave;
    private AppCompatImageView ivQrCode;
    private ProgressBar pbGenerate;

    public static GeneratorFragment newInstance(Context context) {
        GeneratorFragment instance = new GeneratorFragment();
        instance.context = context;
        instance.ssidsValues = new ArrayList<>();
        instance.securityTypeValues = new ArrayList<>();
        instance.securityTypeValues.add(SecurityType.WPA);
        instance.securityTypeValues.add(SecurityType.WPA2);
        instance.securityTypeValues.add(SecurityType.WEP);

        return instance;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                if (intent.getAction().equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
                    presenter.refreshScanResult(wifiManager.getScanResults());

                } else if (intent.getAction().equals(MainActivity.ACTION_REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE_GRANTED)) {
                    wifiManager.startScan();
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.fragment_generator, container, false);
        bindUI(view);

        wifiManager = (WifiManager) Objects.requireNonNull(getActivity())
                .getApplicationContext()
                .getSystemService(Service.WIFI_SERVICE);
        Log.d("DEBUG", "onCreateView: " + wifiManager.getConnectionInfo().getSSID());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        context.registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(receiver, new IntentFilter(MainActivity.ACTION_REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE_GRANTED));
        presenter = new GeneratorPresenterImpl(this);

        btGenerate.setOnClickListener(view -> {
            if (presenter != null) {
                String ssid = "HappyHouse123";

                presenter.generateQrCode(
                        ssid,
                        "12345",
                        (SecurityType) spSecurity.getSelectedItem()
                );
            }
        });

        ssidsAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, ssidsValues);
        ssidsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSsid.setAdapter(ssidsAdapter);

        securityAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, securityTypeValues);
        securityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSecurity.setAdapter(securityAdapter);

//        MainActivity.checkPermissionAndAskIfItIsNeeded(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        MainActivity.checkPermissionAndAskIfItIsNeeded(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION);
//        MainActivity.checkPermissionAndAskIfItIsNeeded(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION);

        wifiManager.startScan();
    }

    @Override
    public void onStop() {
        presenter = null;
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        context.unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    public void showGeneratedQrCode(Bitmap qrCode) {
        pbGenerate.setVisibility(View.GONE);
        ivQrCode.setVisibility(View.VISIBLE);
        ivQrCode.setImageBitmap(qrCode);
    }

    @Override
    public void showProgressBar() {
        ivQrCode.setVisibility(View.GONE);
        pbGenerate.setVisibility(View.VISIBLE);
    }

    @Override
    public void enableButtons(Boolean shouldBeEnabled) {
        btGenerate.setEnabled(shouldBeEnabled);
        btSave.setEnabled(shouldBeEnabled);
    }

    @Override
    public void notifySsidsChanged(List<String> ssids) {
        ssidsValues = ssids;
        ssidsAdapter.notifyDataSetChanged();

        if (ssids.size() > 0) {
            spSsid.setSelection(0);

            String temp = "";
            if (wifiManager.getConfiguredNetworks().size() > 0) {
                for (int i = 0; i < wifiManager.getConfiguredNetworks().size(); i++){
                    temp = tvSsid.getText().toString() + "\n" +
                            wifiManager.getConfiguredNetworks().get(i)
                            .SSID + ";" +
                            wifiManager.getConfiguredNetworks().get(i)
                            .preSharedKey;
                    tvSsid.setText(temp);
                }
            }
        }
    }

    private void bindUI(View view) {
        pbGenerate = view.findViewById(R.id.generator_image_view_progress_bar);
        ivQrCode = view.findViewById(R.id.qr_code_generated_image_view);
        spSsid = view.findViewById(R.id.ssid_spinner);
        btGenerate = view.findViewById(R.id.qr_code_generate_button);
        btSave = view.findViewById(R.id.qr_code_save_button);
        spSecurity = view.findViewById(R.id.security_spinner);
        etPassword = view.findViewById(R.id.password_edit_text);
        tvPassword = view.findViewById(R.id.password_text_label);
        tvSecurity = view.findViewById(R.id.security_text_label);
        tvSsid = view.findViewById(R.id.ssid_text_label);
    }
}

