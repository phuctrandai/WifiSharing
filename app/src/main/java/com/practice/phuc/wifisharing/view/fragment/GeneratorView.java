package com.practice.phuc.wifisharing.view.fragment;

import android.graphics.Bitmap;

import java.util.List;

public interface GeneratorView {
    void showGeneratedQrCode(Bitmap qrCode);
    void showProgressBar();
    void enableButtons(Boolean shouldBeEnabled);
    void notifySsidsChanged(List<String> ssids);
}
