package com.lukeonuke.dlawfabric.service;

public class HelperService {
    public static String cleanUUID(String uuid){
        return uuid.replace("-", "");
    }
}
