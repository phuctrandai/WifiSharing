package com.practice.phuc.wifisharing.model;

public enum SecurityType {
    WPA,
    WPA2,
    WEP,
    NONE,
    BAD_SECURITY_TYPE;

    public static SecurityType toSecurityType(String name){
        switch (name.toLowerCase()){
            case "wpa": return WPA;
            case "wpa2": return WPA2;
            case "wep": return WEP;
            case "none": return NONE;
            default: return BAD_SECURITY_TYPE;
        }
    }
}
