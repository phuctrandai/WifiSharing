package com.practice.phuc.wifisharing.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.practice.phuc.wifisharing.R;

import java.util.List;

import static android.content.ContentValues.TAG;

public class WifiAdapter extends RecyclerView.Adapter<WifiAdapter.WifiViewHolder> {
    private Context mContext;
    private List<ScanResult> mWifis;

    public WifiAdapter(Context context, List<ScanResult> wiFis) {
        mWifis = wiFis;
        mContext = context;
    }

    public void notifyDataSetChanged(List<ScanResult> wiFis) {
        mWifis.clear();
        mWifis.addAll(wiFis);
        notifyDataSetChanged();
    }

    private void connectWiFi(ScanResult scanResult, String password) {
        try {
            Log.v(TAG, "Item clicked, SSID " + scanResult.SSID + " Security : " + scanResult.capabilities);

            String networkSSID = scanResult.SSID;

            WifiConfiguration conf = new WifiConfiguration();
            conf.SSID = "\"" + networkSSID + "\"";

            if (scanResult.capabilities.toUpperCase().contains("WEP")) {
                Log.v(TAG, "Configuring WEP");
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

                if (password.matches("^[0-9a-fA-F]+$")) {
                    conf.wepKeys[0] = password;
                } else {
                    conf.wepKeys[0] = "\"".concat(password).concat("\"");
                }
                conf.wepTxKeyIndex = 0;
            } else if (scanResult.capabilities.toUpperCase().contains("WPA")) {
                Log.v(TAG, "Configuring WPA");
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                conf.preSharedKey = "\"" + password + "\"";
            } else {
                Log.v(TAG, "Configuring OPEN network");
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                conf.allowedAuthAlgorithms.clear();
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            }

            WifiManager wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            int netId = wifiManager.addNetwork(conf);

            Log.v(TAG, "Add result " + netId);

            wifiManager.disconnect();
            wifiManager.enableNetwork(netId, true);
            wifiManager.reconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayConnectDialog(ScanResult scanResult) {
        @SuppressLint("InflateParams")
        View connectDialogLayout = LayoutInflater.from(mContext).inflate(R.layout.dialog_custom_layout, null);
        final EditText etPassword = connectDialogLayout.findViewById(R.id.et_password);
        final CheckBox cbShowPassword = connectDialogLayout.findViewById(R.id.cb_show_password);

        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setTitle(scanResult.SSID);
        alert.setView(connectDialogLayout);
        alert.setCancelable(true);
        alert.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        alert.setPositiveButton("Connect", (dialog, which) -> {
            String pass = etPassword.getText().toString();
            connectWiFi(scanResult, pass);
            dialog.dismiss();
        });
        AlertDialog dialog = alert.create();
        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        etPassword.requestFocus();

        /* Init event */
        cbShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked)
                etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            else
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);

            etPassword.setSelection(etPassword.length());
        });

        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (TextUtils.isEmpty(s)) {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull WifiViewHolder wifiViewHolder, int pos) {
        wifiViewHolder.mWifiName.setText(mWifis.get(pos).SSID);

        wifiViewHolder.setItemClickListener((view, position, isLongClick) -> {
            if (!isLongClick) {
                displayConnectDialog(mWifis.get(pos));
            }
        });
    }

    @NonNull
    @Override
    public WifiViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.item_wifi, viewGroup, false);

        return new WifiViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mWifis.size() > 0 ? mWifis.size() : 0;
    }

    class WifiViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private ItemClickListener mItemClickListener;

        private TextView mWifiName;

        WifiViewHolder(@NonNull View itemView) {
            super(itemView);

            mWifiName = itemView.findViewById(R.id.tv_wifi_name);

            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        void setItemClickListener(ItemClickListener itemClickListener) {
            mItemClickListener = itemClickListener;
        }

        @Override
        public void onClick(View v) {
            mItemClickListener.onClick(v, getAdapterPosition(), false);
        }

        @Override
        public boolean onLongClick(View v) {
            mItemClickListener.onClick(v, getAdapterPosition(), true);
            return true;
        }

    }

    public interface ItemClickListener {
        void onClick(View view, int position, boolean isLongClick);
    }
}
