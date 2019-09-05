package com.practice.phuc.wifisharing.presenter;

import android.net.wifi.ScanResult;

import com.practice.phuc.wifisharing.model.SecurityType;

import java.util.List;

public interface GeneratorPresenter {
    void refreshScanResult(List<ScanResult> results);

    void generateQrCode(String ssid, String password, SecurityType securityType);
}
