package com.practice.phuc.wifisharing.view.fragment;

import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.practice.phuc.wifisharing.MainActivity;
import com.practice.phuc.wifisharing.R;
import com.practice.phuc.wifisharing.model.SecurityType;
import com.practice.phuc.wifisharing.presenter.GeneratorPresenterImpl;

import java.util.List;

public class GeneratorFragment extends Fragment {
    private Context mContext;

    public static GeneratorFragment newInstance(Context context) {
        GeneratorFragment instance = new GeneratorFragment();
        instance.mContext = context;

        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.fragment_generator, container, false);
        bindUI(view);

        return view;
    }

    public void showGeneratedQrCode(Bitmap qrCode) {
    }

    private void bindUI(View view) {
    }
}

