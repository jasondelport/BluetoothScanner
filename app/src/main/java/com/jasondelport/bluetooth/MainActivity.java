package com.jasondelport.bluetooth;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class MainActivity extends AppCompatActivity {

    private List<BTDeviceData> mDevices;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private SampleScanCallback mScanCallback;
    private BluetoothAdapter.LeScanCallback mLeScanCallback = (device, rssi, scanRecord) -> {
        BTDeviceData btDevice = new BTDeviceData();
        btDevice.setBluetoothDevice(device);
        btDevice.setRSSI(rssi);
        btDevice.setScanRecordData(scanRecord);
        addDevice(btDevice);
    };
    private ListView mList;
    private DeviceAdapter mAdapter;
    private ProgressBar mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDevices = new ArrayList<>();
        mList = (ListView) findViewById(R.id.list);
        mAdapter = new DeviceAdapter(mDevices);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        mList.setAdapter(mAdapter);

        RxPermissions.getInstance(this)
                .request(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                .subscribe(granted -> {
                    if (granted) {
                        mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
                                .getAdapter();
                        scan();
                    }
                });

    }

    public void addDevice(BTDeviceData newDevice) {
        boolean add = true;

        for (BTDeviceData oldDevice : mDevices) {
            if (newDevice.getBluetoothDevice().getAddress().equals(oldDevice.getBluetoothDevice().getAddress())) {
                if (newDevice.getBeacon() instanceof EddyStone) {
                    if (((EddyStone) newDevice.getBeacon()).getEddyStoneBeaconType() == ((EddyStone) oldDevice.getBeacon()).getEddyStoneBeaconType()) {
                        add = false;
                        break;
                    }
                } else {
                    add = false;
                    break;
                }
            }
        }

        if (add) {
            mDevices.add(newDevice);
        }
    }

    public void scan() {
        mDevices.clear();
        mAdapter.notifyDataSetChanged();
        mList.setVisibility(View.INVISIBLE);
        mProgress.setVisibility(View.VISIBLE);
        if (mBluetoothAdapter != null) {

            if (mBluetoothAdapter.isEnabled()) {

                // below can get names of classic devices but is resource hungry and unreliable
                // mBluetoothAdapter.startDiscovery();

                if (android.os.Build.VERSION.SDK_INT >= 21) {
                    new Handler().postDelayed(() -> stopScan(), 10000);

                    mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
                    mScanCallback = new SampleScanCallback();
                    mBluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), mScanCallback);
                } else if (android.os.Build.VERSION.SDK_INT <= 18) {
                    {
                        new Handler().postDelayed(() -> {
                            mBluetoothAdapter.stopLeScan(mLeScanCallback);
                            showResults();
                        }, 10000);
                        mBluetoothAdapter.startLeScan(mLeScanCallback);
                    }
                }
            } else {
                // Prompt user to turn on Bluetooth (logic continues in onActivityResult()).
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }

        }
    }

    private void showResults() {
        mAdapter.notifyDataSetChanged();
        mList.setVisibility(View.VISIBLE);
        mProgress.setVisibility(View.INVISIBLE);
    }

    @TargetApi(21)
    private void stopScan() {
        mBluetoothLeScanner.stopScan(mScanCallback);
        mScanCallback = null;
        showResults();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mDevices.clear();
        mDevices = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                scan();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (resultCode == RESULT_OK) {
                    mBluetoothAdapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE))
                            .getAdapter();
                    scan();
                }
            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @TargetApi(21)
    private List<ScanFilter> buildScanFilters() {
        List<ScanFilter> scanFilters = new ArrayList<>();

        ScanFilter.Builder builder = new ScanFilter.Builder();
        scanFilters.add(builder.build());

        return scanFilters;
    }

    @TargetApi(21)
    private ScanSettings buildScanSettings() {
        ScanSettings.Builder builder = new ScanSettings.Builder();
        builder.setScanMode(ScanSettings.SCAN_MODE_LOW_POWER);
        return builder.build();
    }


    class DeviceAdapter extends BaseAdapter {

        private List<BTDeviceData> mDevices;


        public DeviceAdapter(List<BTDeviceData> mDevices) {
            this.mDevices = mDevices;
        }


        public int getCount() {
            return mDevices.size();
        }

        public Object getItem(int position) {
            return mDevices.get(position);
        }


        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            LayoutInflater inflater = getLayoutInflater();
            View row;
            row = inflater.inflate(R.layout.row, parent, false);
            TextView address = (TextView) row.findViewById(R.id.address);
            TextView type = (TextView) row.findViewById(R.id.type);
            TextView rssi = (TextView) row.findViewById(R.id.rssi);
            // ibeacon
            TextView uuid = (TextView) row.findViewById(R.id.uuid);
            TextView major = (TextView) row.findViewById(R.id.major);
            TextView minor = (TextView) row.findViewById(R.id.minor);
            TextView tx = (TextView) row.findViewById(R.id.txPower);
            TextView distance = (TextView) row.findViewById(R.id.distance);
            // eddystone
            TextView eType = (TextView) row.findViewById(R.id.eddystone_type);
            TextView eContent = (TextView) row.findViewById(R.id.eddystone_content);

            // set defaults
            uuid.setVisibility(View.GONE);
            major.setVisibility(View.GONE);
            minor.setVisibility(View.GONE);
            tx.setVisibility(View.GONE);
            distance.setVisibility(View.GONE);
            eType.setVisibility(View.GONE);
            eContent.setVisibility(View.GONE);

            BTDeviceData device = (BTDeviceData) getItem(position);
            address.setText(device.getBluetoothDevice().getAddress() + " (" + device.getDeviceTypeName() + ")");
            int strength = device.getRSSI();
            rssi.setText("RSSI: " + strength + "dBm");

            Beacon beacon = device.getBeacon();
            if (beacon != null) {
                type.setText("Beacon Type: " + beacon.getBeaconTypeName() + " Company ID: " + beacon.getCompanyID());
            } else {
                type.setText("Not a known beacon type");
            }

            if (beacon != null && beacon instanceof IBeacon) {
                IBeacon iBeacon = (IBeacon) beacon;
                uuid.setVisibility(View.VISIBLE);
                major.setVisibility(View.VISIBLE);
                minor.setVisibility(View.VISIBLE);
                tx.setVisibility(View.VISIBLE);
                distance.setVisibility(View.VISIBLE);
                uuid.setText("UUID: " + iBeacon.getUuid());
                major.setText("Major: " + iBeacon.getMajor());
                minor.setText(" Minor: " + iBeacon.getMinor());
                tx.setText("TX Power: " + iBeacon.getTxPower() + "dBm");
                distance.setText("Estimated Distance: " + iBeacon.getDistance() + "m");

            } else if (beacon != null && beacon instanceof EddyStone) {
                EddyStone eddyStone = (EddyStone) beacon;
                eType.setVisibility(View.VISIBLE);
                tx.setVisibility(View.VISIBLE);
                distance.setVisibility(View.VISIBLE);
                eType.setText("Eddystone Type: " + eddyStone.getEddyStoneTypeName());
                tx.setText("TX Power: " + eddyStone.getTxPower() + "dBm");
                distance.setText("Estimated Distance: " + eddyStone.getDistance() + "m");
                if (eddyStone.getEddyStoneBeaconType() == EddyStone.UID) {
                    eContent.setVisibility(View.VISIBLE);
                    eContent.setText("UUID: " + eddyStone.getContent());
                } else if (eddyStone.getEddyStoneBeaconType() == EddyStone.URL) {
                    eContent.setVisibility(View.VISIBLE);
                    eContent.setText("URL: " + eddyStone.getContent());
                }
            }

            return row;
        }
    }

    @TargetApi(21)
    private class SampleScanCallback extends ScanCallback {

        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BTDeviceData btDevice = new BTDeviceData();
            btDevice.setBluetoothDevice(result.getDevice());
            btDevice.setRSSI(result.getRssi());
            btDevice.setScanRecord(result.getScanRecord());
            addDevice(btDevice);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Timber.e("Scan failed");
        }
    }

}
