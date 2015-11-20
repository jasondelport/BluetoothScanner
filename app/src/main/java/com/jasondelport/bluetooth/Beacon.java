package com.jasondelport.bluetooth;

import java.util.Arrays;

/**
 * Created by jasondelport on 04/11/15.
 */
public class Beacon extends BTDeviceData {
    public final static int ALTBEACON = 0;
    public final static int IBEACON = 1;
    public final static int EDDYSTONE = 2;
    public final static int UNKNOWN = 3;


    private int mStartByte;
    private int mType;

    public int getStartByte() {
        return mStartByte;
    }


    public int getCompanyID() {
        // https://www.bluetooth.org/en-us/specification/assigned-numbers/company-identifiers
        byte[] bytes = Arrays.copyOfRange(getScanRecordData(), 5, 6);
        String hex = BluetoothUtils.bytesToHex(bytes);
        return Integer.parseInt(hex, 16);
    }


    public void setStartByte(int startByte) {
        mStartByte = startByte;
    }

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public String getBeaconTypeName() {
        String result;
        switch (mType) {
            case Beacon.EDDYSTONE:
                result = "EDDYSTONE";
                break;
            case Beacon.IBEACON:
                result = "IBEACON";
                break;
            case Beacon.ALTBEACON:
                result = "ALTBEACON";
                break;
            default:
                result = "UNKNOWN";
                break;
        }
        return result;
    }

}
