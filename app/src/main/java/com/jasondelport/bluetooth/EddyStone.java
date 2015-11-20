package com.jasondelport.bluetooth;

import android.os.ParcelUuid;

import java.util.Arrays;

/**
 * Created by jasondelport on 05/11/15.
 */
public class EddyStone extends Beacon {
    public final static int UID = 0;
    public final static int TLM = 1;
    public final static int URL = 2;
    public final static int UNKNOWN = 3;
    private static final byte UID_FRAME_TYPE = 0x00;
    private static final byte URL_FRAME_TYPE = 0x10;
    private static final byte TLM_FRAME_TYPE = 0x20;
    private byte[] mEddyStoneServiceData;
    private int mType;
    private String mName;

    public EddyStone() {
        mEddyStoneServiceData = getScanRecord().getServiceData(ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB"));
    }

    public int getEddyStoneBeaconType() {
        if (mEddyStoneServiceData == null) {
            return UNKNOWN;
        }
        switch (mEddyStoneServiceData[0]) {
            case UID_FRAME_TYPE:
                mType = UID;
                break;
            case TLM_FRAME_TYPE:
                mType = TLM;
                break;
            case URL_FRAME_TYPE:
                mType =  URL;
                break;
            default:
                mType = UNKNOWN;
        }
        return mType;
    }

    public String getContent() {
        String results = null;
        switch (mType) {
            case UID:
                /*
                byte[] namespaceIdentifierBytes = Arrays.copyOfRange(scanRecord, startByte+4, startByte+13);
                byte[] instanceIdentifierBytes = Arrays.copyOfRange(scanRecord, startByte+14, startByte+19);
                 */
                byte[] uidBytes = Arrays.copyOfRange(mEddyStoneServiceData, 2, 18);
                results = BluetoothUtils.bytesToHex(uidBytes);
                break;
            case TLM:
                results = "TELEMETRY DATA";
                break;
            case URL:
                byte[] urlBytes = Arrays.copyOfRange(mEddyStoneServiceData, 2, 20);
                results = BluetoothUtils.bytesToHex(urlBytes);
                break;
            case UNKNOWN:
                results = "UNKNOWN";
                break;
        }
        return results;

    }

    public int getTxPower() {
        return (int) mEddyStoneServiceData[1];
    }

    public double getDistance() {
        return BluetoothUtils.round(BluetoothUtils.calculateDistance(getTxPower(), (double) getRSSI()), 2);
    }

    public String getEddyStoneTypeName() {
        switch (mEddyStoneServiceData[0]) {
            case UID_FRAME_TYPE:
                mName = "UID";
                break;
            case TLM_FRAME_TYPE:
                mName = "TLM";
                break;
            case URL_FRAME_TYPE:
                mName = "URL";
                break;
            default:
                mName = "UNKNOWN";
        }
        return mName;
    }
}
