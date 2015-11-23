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

import com.neovisionaries.bluetooth.ble.advertising.ADPayloadParser;
import com.neovisionaries.bluetooth.ble.advertising.ADStructure;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneUID;
import com.neovisionaries.bluetooth.ble.advertising.EddystoneURL;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class NVActivity extends AppCompatActivity {

    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLeScanner;
    private SampleScanCallback mScanCallback;
    private List<ADStructure> mStructures;
    private BluetoothAdapter.LeScanCallback mLeScanCallback = (device, rssi, scanRecord) -> {
        mStructures =
                ADPayloadParser.getInstance().parse(scanRecord);
    };
    private ListView mList;
    private DeviceAdapter mAdapter;
    private ProgressBar mProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mList = (ListView) findViewById(R.id.list);
        mProgress = (ProgressBar) findViewById(R.id.progress);


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

    public void scan() {
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
                } else if (android.os.Build.VERSION.SDK_INT >= 18) {
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
        mAdapter = new DeviceAdapter(mStructures);
        mList.setAdapter(mAdapter);
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
        mStructures = null;
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

        private List<ADStructure> mDevices;


        public DeviceAdapter(List<ADStructure> mDevices) {
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
            // ibeacon
            TextView uuid = (TextView) row.findViewById(R.id.uuid);
            TextView major = (TextView) row.findViewById(R.id.major);
            TextView minor = (TextView) row.findViewById(R.id.minor);
            // eddystone
            //TextView eType = (TextView) row.findViewById(R.id.eddystone_type);
            TextView eContent = (TextView) row.findViewById(R.id.eddystone_content);
            // both
            //TextView distance = (TextView) row.findViewById(R.id.distance);
            TextView tx = (TextView) row.findViewById(R.id.txPower);

            // set defaults
            uuid.setVisibility(View.GONE);
            major.setVisibility(View.GONE);
            minor.setVisibility(View.GONE);
            tx.setVisibility(View.GONE);
            //distance.setVisibility(View.GONE);
            //eType.setVisibility(View.GONE);
            eContent.setVisibility(View.GONE);

            ADStructure device = (ADStructure) getItem(position);

            if (device instanceof com.neovisionaries.bluetooth.ble.advertising.IBeacon) {
                Timber.d("ibeacon");
                com.neovisionaries.bluetooth.ble.advertising.IBeacon iBeacon = (com.neovisionaries.bluetooth.ble.advertising.IBeacon) device;
                uuid.setVisibility(View.VISIBLE);
                major.setVisibility(View.VISIBLE);
                minor.setVisibility(View.VISIBLE);
                tx.setVisibility(View.VISIBLE);
                //distance.setVisibility(View.VISIBLE);
                uuid.setText("UUID: " + iBeacon.getUUID());
                major.setText("Major: " + iBeacon.getMajor());
                minor.setText(" Minor: " + iBeacon.getMinor());
                tx.setText("TX Power: " + iBeacon.getPower() + "dBm");
                //distance.setText("Estimated Distance: " + iBeacon.getDistance() + "m");

            } else if (device != null && device instanceof com.neovisionaries.bluetooth.ble.advertising.Eddystone) {
                Timber.d("eddystone");
                com.neovisionaries.bluetooth.ble.advertising.Eddystone eddyStone = (com.neovisionaries.bluetooth.ble.advertising.Eddystone) device;

                //distance.setText("Estimated Distance: " + eddyStone.getDistance() + "m");
                if (eddyStone instanceof EddystoneUID) {
                    Timber.d("EddystoneUID");
                    eContent.setVisibility(View.VISIBLE);
                    EddystoneUID es = (EddystoneUID)device;
                    eContent.setText("UUID: " + es.getInstanceIdAsString() + "-" + es.getNamespaceIdAsString());
                    tx.setText("TX Power: " + es.getTxPower() + "dBm");
                } else if (eddyStone instanceof EddystoneURL) {
                    eContent.setVisibility(View.VISIBLE);
                    EddystoneURL es = (EddystoneURL)device;
                    eContent.setText("URL: " + es.getURL());
                    tx.setText("TX Power: " + es.getTxPower() + "dBm");
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
            Timber.d("adding device");
            mStructures =
                    ADPayloadParser.getInstance().parse(result.getScanRecord().getBytes());
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Timber.e("Scan failed");
        }
    }

}
