package com.practice.phuc.wifisharing.view.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import com.practice.phuc.wifisharing.R;
import com.practice.phuc.wifisharing.adapter.WifiAdapter;
import com.practice.phuc.wifisharing.constants.Constants;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class DiscoveryFragment extends Fragment {

    public static DiscoveryFragment newInstance(Context context) {
        DiscoveryFragment fragment = new DiscoveryFragment();
        fragment.mContext = context;
        return fragment;
    }

    private final int mWifiScanningDelay = 20 * 1000;
    private final int mMaxWifiScanningCount = 4;

    private Context mContext;
    private WifiManager mWifiManager;
    private BroadcastReceiver mWifiScanningReceiver;
    private BroadcastReceiver mWifiConnectingReceiver;
    private WifiAdapter mWifiAdapter;
    private Handler mWifiScanningHandler;
    private Runnable mWifiScanningRunnable;

    private int mWifiScanningCount;

    private RecyclerView mWifiRecyclerView;
    private Switch mSBtnWifiToggle;
    private ProgressBar mPbWifiScanning;
    private View mLConnectedWifi;
    private TextView mTvWifiName;
    private TextView mTvWifiStatus;
    private View mLWifiDisabledNotification;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mWifiScanningHandler = new Handler();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.fragment_discovery, container, false);

        bindUI(view);

        initWifiRecyclerView();

        initWifiToggleBtn();

        showWifiStatusLayout(isWifiConnected());

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        initWifiScanningReceiver();

        registerWifiScanningReceiver();

        initWifiConnectingReceiver();

        registerWifiConnectingReceiver();

        scanWifi();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (getWifiManager().isWifiEnabled()) {
            showWifiDisabledNotification(false);

            mWifiScanningCount = 1;
            mWifiScanningHandler.postDelayed(mWifiScanningRunnable = () -> {
                mPbWifiScanning.setVisibility(View.VISIBLE);
                mWifiScanningCount += 1;
                scanWifi();

                if (mWifiScanningCount < mMaxWifiScanningCount)
                    mWifiScanningHandler.postDelayed(mWifiScanningRunnable, mWifiScanningDelay);

            }, mWifiScanningDelay);
        } else {
            showWifiDisabledNotification(true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        mWifiScanningHandler.removeCallbacks(mWifiScanningRunnable);
    }

    @Override
    public void onStop() {
        super.onStop();

        mContext.unregisterReceiver(mWifiScanningReceiver);
        mContext.unregisterReceiver(mWifiConnectingReceiver);
    }

    public void scanWifi() {
        if (getWifiManager().isWifiEnabled()) {
            getWifiManager().startScan();
        }
    }

    /* Private methods region */

    private void showWifiDisabledNotification(boolean isShow) {
        if (isShow) {
            mWifiRecyclerView.setVisibility(View.GONE);
            mLWifiDisabledNotification.setVisibility(View.VISIBLE);
        } else {
            mWifiRecyclerView.setVisibility(View.VISIBLE);
            mLWifiDisabledNotification.setVisibility(View.GONE);
        }
    }

    private void showWifiStatusLayout(boolean isShow){
        if (isShow) {
            String rawSsid = getWifiManager().getConnectionInfo().getSSID();
            String ssid = rawSsid.substring(1, rawSsid.length() - 1);

            mTvWifiName.setText(ssid);
            mTvWifiStatus.setText(Constants.WifiStatus.Connected);
            mLConnectedWifi.setVisibility(View.VISIBLE);

        } else {
            mLConnectedWifi.setVisibility(View.GONE);
        }
    }

    private boolean isWifiConnected() {
        ConnectivityManager connManager = (ConnectivityManager) mContext.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        return wifi.isConnected();
    }

    private void scanSuccess() {
        List<ScanResult> results = getWifiManager().getScanResults();

        if (results.size() > 0) {
            if (isWifiConnected()) {
                String rawSsid = getWifiManager().getConnectionInfo().getSSID();
                String currentSsid = rawSsid.substring(1, rawSsid.length() - 1);
                String currentBssid = getWifiManager().getConnectionInfo().getBSSID();

                int size = results.size();
                int pos = -1;

                for (int i = 0; i < size; i++) {
                    if (results.get(i).SSID.equalsIgnoreCase(currentSsid)
                            && results.get(i).BSSID.equalsIgnoreCase(currentBssid)) {
                        pos = i;
                        break;
                    }
                }

                if (pos != -1) {
                    results.remove(pos);
                }
            }

            if (results.size() > 0) {
                mWifiAdapter.notifyDataSetChanged(results);
            }
        }
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        Log.d(TAG, "scanFailure: Scan wifi failure !");
    }

    private void initWifiScanningReceiver() {
        mWifiScanningReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean success = false;

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                }

                Log.d(TAG, "onReceive: Scanning receiver: " + success);

                mPbWifiScanning.setVisibility(View.INVISIBLE);

                if (success) {
                    scanSuccess();
                } else {
                    scanFailure();
                }
            }
        };
    }

    private void registerWifiScanningReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mContext.registerReceiver(mWifiScanningReceiver, intentFilter);
    }

    private void initWifiConnectingReceiver() {
        mWifiConnectingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null) {
                    if (intent.getAction().equalsIgnoreCase(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                        int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);

                        if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
                            Log.d(TAG, "Connecting Receiver: Wifi disabled");

                        } else if (wifiState == WifiManager.WIFI_STATE_DISABLING) {
                            Log.d(TAG, "Connecting Receiver: Wifi disabling");

                        } else if (wifiState == WifiManager.WIFI_STATE_ENABLING) {
                            Log.d(TAG, "Connecting Receiver: Wifi enabling");

                        } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
                            Log.d(TAG, "Connecting Receiver: Wifi enabled");
                        }
                    } else if (intent.getAction().equalsIgnoreCase(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
                        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                        if (info != null && info.isConnected()) {
                            showWifiStatusLayout(true);

                            String rawSsid = getWifiManager().getConnectionInfo().getSSID();
                            String currentSsid = rawSsid.substring(1, rawSsid.length() - 1);
                            String currentBssid = getWifiManager().getConnectionInfo().getBSSID();
                            mWifiAdapter.updateConnectedWifi(currentSsid, currentBssid);

                            Log.d(TAG, "Connecting Receiver: Connected to " + currentSsid);
                        }
                    }
                }
            }
        };
    }

    private void registerWifiConnectingReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        intentFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mContext.registerReceiver(mWifiConnectingReceiver, intentFilter);
    }

    private void initWifiRecyclerView() {
        mWifiAdapter = new WifiAdapter(mContext, new ArrayList<>());

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mWifiRecyclerView.setHasFixedSize(true);
        mWifiRecyclerView.setLayoutManager(layoutManager);
        mWifiRecyclerView.setAdapter(mWifiAdapter);
    }

    private void initWifiToggleBtn() {
        if (getWifiManager().isWifiEnabled()) {
            mSBtnWifiToggle.setChecked(true);
            mPbWifiScanning.setVisibility(View.VISIBLE);
        } else {
            mSBtnWifiToggle.setChecked(false);
            mPbWifiScanning.setVisibility(View.INVISIBLE);
        }

        mSBtnWifiToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                getWifiManager().setWifiEnabled(true);
                mPbWifiScanning.setVisibility(View.VISIBLE);
                new Handler().postDelayed(this::scanWifi, 2000);
                showWifiDisabledNotification(false);
            } else {
                getWifiManager().setWifiEnabled(false);
                mPbWifiScanning.setVisibility(View.INVISIBLE);
                mWifiAdapter.notifyDataSetChanged(new ArrayList<>());
                mWifiScanningHandler.removeCallbacks(mWifiScanningRunnable);
                showWifiStatusLayout(false);
                showWifiDisabledNotification(true);
            }
        });
    }

    private WifiManager getWifiManager() {
        if (mWifiManager == null) {
            mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        }

        return mWifiManager;
    }

    private void bindUI(View view) {
        mWifiRecyclerView = view.findViewById(R.id.rv_wifi);
        mSBtnWifiToggle = view.findViewById(R.id.sbtn_wifi_toggle);
        mPbWifiScanning = view.findViewById(R.id.pb_wifi_scanning);
        mLConnectedWifi = view.findViewById(R.id.layout_connected_wifi);
        mTvWifiName = view.findViewById(R.id.tv_wifi_name);
        mTvWifiStatus = view.findViewById(R.id.tv_wifi_status);
        mLWifiDisabledNotification = view.findViewById(R.id.layout_wifi_disabled_notification);
    }

    /* End private methods region */
}
