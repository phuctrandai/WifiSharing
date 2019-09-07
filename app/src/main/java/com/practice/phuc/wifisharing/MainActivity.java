package com.practice.phuc.wifisharing;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.practice.phuc.wifisharing.view.fragment.DiscoveryFragment;
import com.practice.phuc.wifisharing.view.fragment.GeneratorFragment;

public class MainActivity extends AppCompatActivity {

    public static final String ACTION_REQUEST_PERMISSION_CAMERA = "ACTION_REQUEST_PERMISSION_CAMERA";
    public static final String ACTION_REQUEST_PERMISSION_CHANGE_WIFI_STATE_GRANTED = "ACTION_REQUEST_PERMISSION_CHANGE_WIFI_STATE_GRANTED";
    public static final String ACTION_REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE_GRANTED = "ACTION_REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE_GRANTED";
    public static final String ACTION_REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = "ACTION_REQUEST_PERMISSION_ACCESS_COARSE_LOCATION";
    public static final String ACTION_REQUEST_PERMISSION_ACCESS_FINE_LOCATION = "ACTION_REQUEST_PERMISSION_ACCESS_FINE_LOCATION";

    public static final int REQUEST_PERMISSION_CAMERA = 0;
    public static final int REQUEST_PERMISSION_CHANGE_WIFI_STATE = 1;
    public static final int REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE = 2;
    public static final int REQUEST_PERMISSION_ACCESS_COARSE_LOCATION = 3;
    public static final int REQUEST_PERMISSION_ACCESS_FINE_LOCATION = 4;

    private GeneratorFragment mGeneratorFragment = GeneratorFragment.newInstance(this);
    private DiscoveryFragment mDiscoveryFragment = DiscoveryFragment.newInstance(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.tb_main);
        setSupportActionBar(toolbar);
        setTitle(getString(R.string.title_discover));

        initBottomNavigationView();

        checkPermissionAndAskIfItIsNeeded(Manifest.permission.ACCESS_FINE_LOCATION);
        checkPermissionAndAskIfItIsNeeded(Manifest.permission.ACCESS_COARSE_LOCATION);

//        mDiscoveryFragment.scanWifi();
    }

    public void checkPermissionAndAskIfItIsNeeded(String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat
                    .requestPermissions(this, new String[]{permission}, getRightRequestCode(permission));
        } else {
            LocalBroadcastManager.getInstance(this)
                    .sendBroadcast(new Intent(getRightActionRequest(permission)));
        }
    }

    private int getRightRequestCode(String permission) {
        switch (permission) {
            case Manifest.permission.CAMERA:
                return REQUEST_PERMISSION_CAMERA;
            case Manifest.permission.CHANGE_WIFI_STATE:
                return REQUEST_PERMISSION_CHANGE_WIFI_STATE;
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE;
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return REQUEST_PERMISSION_ACCESS_COARSE_LOCATION;
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return REQUEST_PERMISSION_ACCESS_FINE_LOCATION;
            default:
                throw new IllegalArgumentException("It's not the right permission !");
        }
    }

    private String getRightActionRequest(String permission) {
        switch (permission) {
            case Manifest.permission.CAMERA:
                return ACTION_REQUEST_PERMISSION_CAMERA;
            case Manifest.permission.CHANGE_WIFI_STATE:
                return ACTION_REQUEST_PERMISSION_CHANGE_WIFI_STATE_GRANTED;
            case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                return ACTION_REQUEST_PERMISSION_WRITE_EXTERNAL_STORAGE_GRANTED;
            case Manifest.permission.ACCESS_COARSE_LOCATION:
                return ACTION_REQUEST_PERMISSION_ACCESS_COARSE_LOCATION;
            case Manifest.permission.ACCESS_FINE_LOCATION:
                return ACTION_REQUEST_PERMISSION_ACCESS_FINE_LOCATION;
            default:
                throw new IllegalArgumentException("It's not the right permission !");
        }
    }

    private void initBottomNavigationView(){
        BottomNavigationView mNavigationView = findViewById(R.id.navigation);
        mNavigationView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener);
        mNavigationView.setSelectedItemId(R.id.navigation_discover);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener onNavigationItemSelectedListener = menuItem ->
    {
        switch (menuItem.getItemId()) {
            case R.id.navigation_discover:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, mDiscoveryFragment).commit();
                setTitle(getString(R.string.title_discover));
                return true;
            case R.id.navigation_share:
                getSupportFragmentManager().beginTransaction().replace(R.id.main_fragment, mGeneratorFragment).commit();
                setTitle(getString(R.string.title_share));
                return true;
            default:
                return false;
        }
    };
}
