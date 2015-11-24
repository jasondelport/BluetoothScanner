package com.jasondelport.bluetooth;

/**
 * Created by jasondelport on 04/11/15.
 */
public class IBeacon extends Beacon {
    private String mUuid;
    private int mMajor;
    private int mMinor;
    private int mTxPower;

    public int getMajor() {
        /*
        data = advertising data
        BluetoothUtils.parseBE2BytesAsInt(data, 20);
        */
        this.mMajor = (getScanRecordData()[getStartByte() + 20] & 0xff) * 0x100 + (getScanRecordData()[getStartByte() + 21] & 0xff);
        return mMajor;
    }

    public int getMinor() {
        /*
        data = advertising data
        BluetoothUtils.parseBE2BytesAsInt(data, 20);
        */
        this.mMinor = (getScanRecordData()[getStartByte() + 22] & 0xff) * 0x100 + (getScanRecordData()[getStartByte() + 23] & 0xff);
        return mMinor;
    }

    public int getTxPower() {
        /*
        data = advertising data
        data[24];
        */
        this.mTxPower = (int) getScanRecordData()[getStartByte() + 24]; // this one is signed
        return mTxPower;
    }

    public String getUuid() {

        /*
        try {
            UUID Uuid = UUIDCreator.from128(data, 4, false);
            Timber.i(Uuid.toString());
        } catch (Exception e) {

        }
         */
        byte[] uuidBytes = new byte[16];
        System.arraycopy(getScanRecordData(), getStartByte() + 4, uuidBytes, 0, 16);
        String hexString = BluetoothUtils.bytesToHex(uuidBytes);


        this.mUuid = hexString.substring(0, 8) + "-" +
                hexString.substring(8, 12) + "-" +
                hexString.substring(12, 16) + "-" +
                hexString.substring(16, 20) + "-" +
                hexString.substring(20, 32);

        return mUuid;
    }

    public double getDistance() {
        return BluetoothUtils.round(BluetoothUtils.calculateDistance(getTxPower(), (double) getRSSI()), 2);
    }
}
