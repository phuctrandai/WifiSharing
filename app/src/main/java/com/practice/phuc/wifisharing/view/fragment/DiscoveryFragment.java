package com.practice.phuc.wifisharing.view.fragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.practice.phuc.wifisharing.R;
import com.practice.phuc.wifisharing.adapter.WifiAdapter;
import com.practice.phuc.wifisharing.model.WiFi;

import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

public class DiscoveryFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    public static DiscoveryFragment newInstance(Context context) {
        DiscoveryFragment fragment = new DiscoveryFragment();
        fragment.mContext = context;
        return fragment;
    }

    private Context mContext;
    private WifiManager mWifiManager;
    private BroadcastReceiver mWifiScanReceiver;

    private WifiAdapter mWifiAdapter;
    private RecyclerView mWifiRecyclerView;
    private SwipeRefreshLayout mWifiDiscoverySwipeRefreshLayout;
    private Switch mSBtnWifiToggle;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.fragment_discovery, container, false);

        bindUI(view);

        return view;
    }

    @SuppressLint("WifiManagerPotentialLeak")
    @Override
    public void onStart() {
        super.onStart();

        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);

        initWifiSwipeRefreshLayout();

        initWifiToggleBtn();

        initReceiver();

        registerReceiver();

        initWifiRecyclerView();

        scanWifi();
    }

    @Override
    public void onStop() {
        super.onStop();

        mContext.unregisterReceiver(mWifiScanReceiver);
    }

    @Override
    public void onRefresh() {
        mWifiDiscoverySwipeRefreshLayout.setRefreshing(true);

        scanWifi();

        new Handler().postDelayed(() -> mWifiDiscoverySwipeRefreshLayout.setRefreshing(false), 2000);
    }

    public void scanWifi() {
        if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
            mWifiManager.startScan();
        }
    }

    private void scanSuccess() {
        List<ScanResult> results = mWifiManager.getScanResults();

        if (results.size() > 0) {
            mWifiAdapter.notifyDataSetChanged(results);
        }
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = mWifiManager.getScanResults();

        Log.d(TAG, "scanFailure: Scan wifi failure !");
    }

    private void initReceiver() {
        mWifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                boolean success = false;

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false);
                }

                if (success) {
                    scanSuccess();
                } else {
                    scanFailure();
                }
            }
        };
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        mContext.registerReceiver(mWifiScanReceiver, intentFilter);
    }

    private void initWifiRecyclerView() {
        mWifiAdapter = new WifiAdapter(mContext, new ArrayList<>());

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mWifiRecyclerView.setHasFixedSize(true);
        mWifiRecyclerView.setLayoutManager(layoutManager);
        mWifiRecyclerView.setAdapter(mWifiAdapter);
    }

    private void initWifiSwipeRefreshLayout() {
        mWifiDiscoverySwipeRefreshLayout.setOnRefreshListener(this::initWifiSwipeRefreshLayout);
    }

    private void initWifiToggleBtn() {
        if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
            mSBtnWifiToggle.setChecked(true);
        }

        mSBtnWifiToggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (mWifiManager != null) {
                if (isChecked) {
                    registerReceiver();

                    mWifiManager.setWifiEnabled(true);

                    scanWifi();

                } else {
                    mWifiManager.setWifiEnabled(false);
                    mWifiAdapter.notifyDataSetChanged(new ArrayList<>());
                    mContext.unregisterReceiver(mWifiScanReceiver);
                }
            }
        });
    }

    private void bindUI(View view) {
        mWifiRecyclerView = view.findViewById(R.id.rv_wifi);
        mWifiDiscoverySwipeRefreshLayout = view.findViewById(R.id.srl_wifi_discovery);
        mSBtnWifiToggle = view.findViewById(R.id.sbtn_wifi_toggle);
    }
}
