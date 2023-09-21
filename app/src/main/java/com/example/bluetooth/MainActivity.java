package com.example.bluetooth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;


import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> deviceArrayAdapter;
    private List<String> desiredMacAddresses;
    private DatabaseReference databaseReference;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase Realtime Database 초기화
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference("bluetooth_devices");

        // 원하는 MAC 주소 목록 초기화
        desiredMacAddresses = new ArrayList<>();
        desiredMacAddresses.add("MAC_ADDRESS_1"); // 원하는 MAC 주소 1 추가
        desiredMacAddresses.add("MAC_ADDRESS_2"); // 원하는 MAC 주소 2 추가

        // BluetoothAdapter 초기화
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        ListView deviceListView = findViewById(R.id.deviceListView);
        deviceListView.setAdapter(deviceArrayAdapter);

        // 블루투스 장치 검색 시작
        startDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothReceiver);
    }

    @SuppressLint("MissingPermission")
    private void startDiscovery() {
        // 블루투스 장치 검색을 위한 BroadcastReceiver 등록
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothReceiver, filter);

        // 블루투스 검색 시작
        bluetoothAdapter.startDiscovery();
    }

    // 블루투스 장치 검색 결과를 처리하는 BroadcastReceiver
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

                // 디바이스의 MAC 주소 확인
                String deviceMacAddress = device.getAddress();
                if (deviceMacAddress != null && desiredMacAddresses.contains(deviceMacAddress)) {
                    // 원하는 MAC 주소 목록에 있는 디바이스 정보를 리스트에 추가
                    String deviceInfo = device.getName() + "\n" + deviceMacAddress + "\nRSSI: " + rssi;
                    deviceArrayAdapter.add(deviceInfo);

                    // Firebase Realtime Database에 업로드
                    String currentTime = getCurrentTime();
                    uploadData(device.getName(), deviceMacAddress, rssi, currentTime);
                }
            }
        }
    };

    // Firebase Realtime Database에 데이터 업로드
    private void uploadData(String deviceName, String macAddress, int rssi, String currentTime) {
        // Firebase Realtime Database에 데이터 업로드
        DatabaseReference deviceTimeRef = databaseReference.child(currentTime); // 현재 시간을 폴더 이름으로 사용
        DatabaseReference deviceNameRef = deviceTimeRef.child(deviceName); // deviceName을 폴더 이름으로 사용
        deviceNameRef.child("mac_address").setValue(macAddress);
        deviceNameRef.child("rssi").setValue(rssi);
    }

    // 현재 시간을 가져오는 메서드
    private String getCurrentTime() {
        return dateFormat.format(new Date());
    }
}