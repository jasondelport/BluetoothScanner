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
    private static final byte UID_FRAME_TYPE = 0x00; // 0
    private static final byte URL_FRAME_TYPE = 0x10; // 16
    private static final byte TLM_FRAME_TYPE = 0x20;
    private byte[] mEddyStoneServiceData;
    private int mType = -1;
    private ScanRecord mScanRecord;
    private String mName;

    public EddyStone() {
    }


    /*

    11-23 15:52:46.536: I/System.out(19824): Key = 000081e7-0000-1000-8000-00805f9b34fb
    11-23 15:52:46.537: I/System.out(19824): Key = 0000feaa-0000-1000-8000-00805f9b34fb

     */

    public int getEddyStoneBeaconType() {
        mEddyStoneServiceData = getScanRecord().getServiceData(ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB"));

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
        mEddyStoneServiceData = getScanRecord().getServiceData(ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB"));
        String results = null;
        if (mType == -1) {
            mType = getEddyStoneBeaconType();
        }
        switch (mType) {
            case UID:

                byte[] namespaceIdentifierBytes = Arrays.copyOfRange(mEddyStoneServiceData, 2, 12);
                byte[] instanceIdentifierBytes = Arrays.copyOfRange(mEddyStoneServiceData, 12, 18);

                //byte[] uidBytes = Arrays.copyOfRange(mEddyStoneServiceData, 2, 18);
                //results = BluetoothUtils.bytesToHex(uidBytes);
                results = BluetoothUtils.bytesToHex(namespaceIdentifierBytes)
                        + "-" + BluetoothUtils.bytesToHex(instanceIdentifierBytes);
                break;
            case TLM:
                results = "TELEMETRY DATA";
                break;
            case URL:
                results = UrlUtils.decodeUrl(mEddyStoneServiceData);
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
        mEddyStoneServiceData = getScanRecord().getServiceData(ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB"));
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
