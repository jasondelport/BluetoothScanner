package com.jasondelport.bluetooth;

/**
 * Created by jasondelport on 04/11/15.
 */
public class IBeacon extends Beacon {
    private String uuid;
    private int major;
    private int minor;
    private int txPower;

    public int getMajor() {
        this.major = (getScanRecordData()[getStartByte() + 20] & 0xff) * 0x100 + (getScanRecordData()[getStartByte() + 21] & 0xff);
        return major;
    }

    public int getMinor() {
        this.minor = (getScanRecordData()[getStartByte() + 22] & 0xff) * 0x100 + (getScanRecordData()[getStartByte() + 23] & 0xff);
        return minor;
    }

    public int getTxPower() {
        this.txPower = (int) getScanRecordData()[getStartByte() + 24]; // this one is signed
        return txPower;
    }

    public String getUuid() {

        byte[] uuidBytes = new byte[16];
        System.arraycopy(getScanRecordData(), getStartByte() + 4, uuidBytes, 0, 16);
        String hexString = BluetoothUtils.bytesToHex(uuidBytes);

        this.uuid = hexString.substring(0, 8) + "-" +
                hexString.substring(8, 12) + "-" +
                hexString.substring(12, 16) + "-" +
                hexString.substring(16, 20) + "-" +
                hexString.substring(20, 32);

        return uuid;
    }

    public double getDistance() {
        return BluetoothUtils.round(BluetoothUtils.calculateDistance(getTxPower(), (double) getRSSI()), 2);
    }
}
