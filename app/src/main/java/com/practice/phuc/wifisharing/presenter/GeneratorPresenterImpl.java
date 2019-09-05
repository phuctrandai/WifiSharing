package com.practice.phuc.wifisharing.presenter;

import android.net.wifi.ScanResult;

import com.google.zxing.WriterException;
import com.practice.phuc.wifisharing.model.SecurityType;
import com.practice.phuc.wifisharing.model.WiFi;
import com.practice.phuc.wifisharing.view.fragment.GeneratorView;

import java.util.ArrayList;
import java.util.List;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class GeneratorPresenterImpl implements GeneratorPresenter {

    private GeneratorView mView;

    public GeneratorPresenterImpl(GeneratorView view) {
        mView = view;
    }

    @Override
    public void refreshScanResult(List<ScanResult> results) {
        List<String> ssids = new ArrayList<>();

        for (ScanResult item : results) {
            ssids.add(item.SSID);
        }

        mView.notifySsidsChanged(ssids);
    }

    @Override
    public void generateQrCode(String ssid, String password, SecurityType securityType) {
        mView.enableButtons(false);
        mView.showProgressBar();

        WiFi wifi = new WiFi(ssid, password, securityType);
        QRGEncoder encoder = new QRGEncoder(wifi.toJson(), null, QRGContents.Type.TEXT, 1000);

        try {
            mView.enableButtons(true);
            mView.showGeneratedQrCode(encoder.encodeAsBitmap());
        } catch (WriterException e) {
            e.printStackTrace();
        }
    }
}

