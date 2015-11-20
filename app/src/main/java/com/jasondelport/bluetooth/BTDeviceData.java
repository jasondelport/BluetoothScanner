package com.jasondelport.bluetooth;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;


/**
 * Created by jasondelport on 03/11/15.
 */
public class BTDeviceData {
    private BluetoothDevice mBluetoothDevice;
    private int mRSSI;
    private byte[] mScanRecordData;
    private ScanRecord mScanRecord;

    public void setBluetoothDevice(BluetoothDevice bluetoothDevice) {
        mBluetoothDevice = bluetoothDevice;
    }

    public void setRSSI(int RSSI) {
        mRSSI = RSSI;
    }

    @TargetApi(21)
     public void setScanRecord(android.bluetooth.le.ScanRecord scanRecord) {
        mScanRecordData = scanRecord.getBytes();
        mScanRecord = ScanRecord.parseFromBytes(scanRecord.getBytes());
    }

    public void setScanRecord(ScanRecord scanRecord) {
        mScanRecord = scanRecord;
    }

    public void setScanRecordData(byte[] scanRecordData) {
        mScanRecordData = scanRecordData;
        mScanRecord = ScanRecord.parseFromBytes(scanRecordData);
    }

    public String getDeviceTypeName() {
        String result = null;
        switch (mBluetoothDevice.getType()) {
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                result = "CLASSIC";
                break;
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                result = "DUAL";
                break;
            case BluetoothDevice.DEVICE_TYPE_LE:
                result = "LE";
                break;
            case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
                result = "UNKNOWN";
                break;
        }
        return result;
    }

    public BluetoothDevice getBluetoothDevice() {
        return mBluetoothDevice;
    }

    public int getRSSI() {
        return mRSSI;
    }

    public ScanRecord getScanRecord() {
        return mScanRecord;
    }

    public byte[] getScanRecordData() {
        return mScanRecordData;
    }

    public Beacon getBeacon() {
        /*
        Timber.d(BluetoothUtils.getPrintableByteData(mScanRecordData));
        byte[] bb = getScanRecord().getManufacturerSpecificData(BluetoothAssignedNumbers.APPLE);
        Timber.d("data -> %s", BluetoothUtils.getPrintableByteData(bb));

        byte[] gh = Arrays.copyOfRange(getScanRecordData(), 5, 6);
        String ss = BluetoothUtils.bytesToHex(gh);
        int dec = Integer.parseInt(ss, 16);
        Timber.d("company -> %s", ss);
        Timber.d("company -> %d", dec);
        */

        Beacon beacon = null;
        for (int startByte = 0; startByte < mScanRecordData.length; startByte++) {

            if (mScanRecordData.length-startByte > 19) { // need at least 19 bytes for Eddystone-UID
                if (mScanRecordData[startByte+0] == (byte)0xaa && mScanRecordData[startByte+1] == (byte) 0xfe &&
                        mScanRecordData[startByte+2] == (byte)0x00) {
                    beacon = new EddyStone();
                    beacon.setRSSI(mRSSI);
                    beacon.setScanRecordData(mScanRecordData);
                    beacon.setScanRecord(mScanRecord);
                    beacon.setStartByte(startByte);
                    beacon.setType(Beacon.EDDYSTONE);
                    break;

                }
            }

            if (mScanRecordData.length-startByte > 24) { // need at least 24 bytes for AltBeacon
                if (mScanRecordData[startByte+2] == (byte)0xbe && mScanRecordData[startByte+3] == (byte)0xac) {
                    beacon = new Beacon();
                    beacon.setScanRecordData(mScanRecordData);
                    beacon.setScanRecord(mScanRecord);
                    beacon.setRSSI(mRSSI);
                    beacon.setStartByte(startByte);
                    beacon.setType(Beacon.ALTBEACON);
                    break;
                }
            }
            if (startByte <= 5) {
                if (((int) mScanRecordData[startByte + 2] & 0xff) == 0x02 &&
                        ((int) mScanRecordData[startByte + 3] & 0xff) == 0x15) {
                    beacon = new IBeacon();
                    beacon.setScanRecordData(mScanRecordData);
                    beacon.setScanRecord(mScanRecord);
                    beacon.setRSSI(mRSSI);
                    beacon.setStartByte(startByte);
                    beacon.setType(Beacon.IBEACON);
                    break;
                }
            }

        }
        return beacon;
    }

}
