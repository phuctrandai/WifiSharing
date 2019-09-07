package com.practice.phuc.wifisharing.presenter;

import com.practice.phuc.wifisharing.model.SecurityType;
import com.practice.phuc.wifisharing.model.WiFi;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class GeneratorPresenterImpl {

    public void generateQrCode(String ssid, String password, SecurityType securityType) {
        WiFi wifi = new WiFi(ssid, password, securityType);
        QRGEncoder encoder = new QRGEncoder(wifi.toJson(), null, QRGContents.Type.TEXT, 1000);

//        try {
////            mView.showGeneratedQrCode(encoder.encodeAsBitmap());
//        } catch (WriterException e) {
//            e.printStackTrace();
//        }
    }
}

