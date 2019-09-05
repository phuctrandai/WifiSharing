package com.practice.phuc.wifisharing.model;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.practice.phuc.wifisharing.adapter.WifiAdapter;

import org.json.JSONObject;

public class WiFi {
    private String ssid;
    private String password;
    private SecurityType securityType;

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public SecurityType getSecurityType() {
        return securityType;
    }

    public void setSecurityType(SecurityType securityType) {
        this.securityType = securityType;
    }

    public WiFi(String ssid, String password, SecurityType securityType) {
        this.ssid = ssid;
        this.password = password;
        this.securityType = securityType;
    }

    public WiFi(String ssid) {
        this.ssid = ssid;
    }

    public String toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("ssid", getSsid());
        jsonObject.addProperty("password", getPassword());
        jsonObject.addProperty("setSecurityType", getSecurityType().toString());
        return jsonObject.toString();
    }

    public static WiFi fromJson(String json) {
        JsonObject jsonObject;

        try {
            jsonObject = new JsonParser().parse(json).getAsJsonObject();
        } catch (JsonParseException e) {
            return null;
        }

        return new WiFi(jsonObject.get("ssid").getAsString(),
                jsonObject.get("password").getAsString(),
                SecurityType.toSecurityType(jsonObject.get("setSecurityType").getAsString()));
    }
}

