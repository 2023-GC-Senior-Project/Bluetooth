package com.example.bluetooth;

import static java.lang.System.currentTimeMillis;
import static java.lang.Thread.sleep;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_BLUETOOTH_SCAN = 1001;
    private static final int REQUEST_LOCATION_SCAN = 123;
    private static final int REQUEST_CONNECT_SCAN = 456;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> deviceArrayAdapter;
    private List<String> desiredMacAddresses;
    private DatabaseReference databaseReference;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    long start = currentTimeMillis();
    long end = currentTimeMillis();
    int catchNum = 0;
    int catchTmp = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Firebase Realtime Database 초기화
//        FirebaseDatabase database = FirebaseDatabase.getInstance();
//        databaseReference = database.getReference();

        // 원하는 MAC 주소 목록 초기화
        desiredMacAddresses = new ArrayList<>();
        desiredMacAddresses.add("24:11:53:FF:38:54"); // Galaxy Watch5 (DH1H) injae
        desiredMacAddresses.add("24:11:53:19:77:0B"); // Galaxy Watch5 (T2WL) kwanil
        catchNum = desiredMacAddresses.size();

        // BluetoothAdapter 초기화
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        deviceArrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        ListView deviceListView = findViewById(R.id.deviceListView);
        deviceListView.setAdapter(deviceArrayAdapter);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 권한 요청 대화 상자 표시
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.BLUETOOTH_SCAN}, REQUEST_BLUETOOTH_SCAN);
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 권한 요청 대화 상자 표시
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_SCAN);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // 권한이 없으면 권한 요청 대화 상자 표시
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, REQUEST_CONNECT_SCAN);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        && ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
            startDiscovery();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_SCAN) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 부여된 경우 스캔 시작
                //startDiscovery();
            } else {
                // 권한이 거부된 경우 사용자에게 메시지 표시 또는 작업 수행
            }
        }
        if (requestCode == REQUEST_LOCATION_SCAN) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 권한이 부여된 경우 스캔 시작
                //startDiscovery();
            } else {
                // 권한이 거부된 경우 사용자에게 메시지 표시 또는 작업 수행
            }
        }
        if ( requestCode == REQUEST_CONNECT_SCAN){

        }

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
        Log.d("test","\nSTART");
        start = currentTimeMillis();
        bluetoothAdapter.startDiscovery();
    }

    // 블루투스 장치 검색 결과를 처리하는 BroadcastReceiver
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.d("test","find something");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);

                // 디바이스의 MAC 주소 확인
                String deviceMacAddress = device.getAddress();
                if (deviceMacAddress != null && desiredMacAddresses.contains(deviceMacAddress)) {
                    Log.d("test","find match");
                    // 원하는 MAC 주소 목록에 있는 디바이스 정보를 리스트에 추가
                    String deviceInfo = device.getName() + "\n" + deviceMacAddress + "\nRSSI: " + rssi;
                    deviceArrayAdapter.add(deviceInfo);

                    // Firebase Realtime Database에 업로드
                    String currentTime = getCurrentTime();
                    Log.d("test",deviceInfo);
                    //uploadData(device.getName(), deviceMacAddress, rssi, currentTime);

                    //mac디바이스 1개 탐색 추가
                    catchTmp++;
                    //등록된 mac디바이스들을 모두 찾으면 재 스캔(무한)
                    if(catchNum == catchTmp){
                        catchTmp=0;
                        loop();
                    }

                }
            }

        }
    };

    //무한하게 블루투스 스캔 (과부하 방지를 위해 0.3s 씩 휴식)
    @SuppressLint("MissingPermission")
    public void loop(){
        end = currentTimeMillis();
        Log.d("test","CANCEL");
        bluetoothAdapter.cancelDiscovery();
        Log.d("test","sleep 100s");
        try{
            sleep(100);
        } catch (InterruptedException e) {
            Log.d("test","ERR");
            throw new RuntimeException(e);
        }
        startDiscovery();
    }

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