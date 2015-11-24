package com.jasondelport.bluetooth;

import com.neovisionaries.bluetooth.ble.util.UUIDCreator;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import timber.log.Timber;

/**
 * Created by jasondelport on 24/11/15.
 */
public class AdvertisingData {

    private int mLength;
    private int mType;
    private byte[] mData;
    private int mCompanyId;

    public AdvertisingData(int length, int type, byte[] data) {
        mLength = length;
        mType = type;
        mData = data;
        if (data.length >= 2) {
            byte[] bytes = Arrays.copyOfRange(data, 0, 1);
            String hex = BluetoothUtils.bytesToHex(bytes);
            mCompanyId = Integer.parseInt(hex, 16);
        }

        try {
            UUID Uuid = UUIDCreator.from128(data, 4, false);
            Timber.i(Uuid.toString());
        } catch (Exception e) {

        }
    }


    public static List<AdvertisingData> parseScanRecord(byte[] scanRecord) {
        List<AdvertisingData> records = new ArrayList<>();

        int index = 0;
        while (index < scanRecord.length) {
            int length = scanRecord[index++];
            //Done once we run out of records
            if (length == 0) break;

            int type = scanRecord[index] & 0xFF;
            //Done if our record isn't a valid type
            if (type == 0) break;

            byte[] data = Arrays.copyOfRange(scanRecord, index + 1, index + length);

            records.add(new AdvertisingData(length, type, data));
            //Advance
            index += length;
        }

        return records;
    }

    @Override
    public String toString() {
        String decodedData = "";
        try {
            decodedData = new String(mData, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String data = "AdvertisingData\n";
        data += "length -> " + mLength + "\n";
        data += "type -> " + mType + "\n";
        data += "company id -> " + mCompanyId + "\n";
        data += "data -> " + decodedData;
        return data;
    }
}