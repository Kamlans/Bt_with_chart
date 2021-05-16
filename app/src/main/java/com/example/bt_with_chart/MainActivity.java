package com.example.bt_with_chart;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {


    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECEIVED = 5;
    private static final String TAG = "kamlans";
    private static final String APP_NAME = "BTChat";
    private static final UUID MY_UUID = UUID.fromString("8ce255c0-223a-11e0-ac64-0803450c9a66");
    private static LineChart lineChart;
    private static TextView status;
    private static SendReceive sendReceive;
    private static final Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    status.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    status.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    status.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuff = (byte[]) msg.obj;
                    String tempMsg = new String(readBuff, 0, msg.arg1);
                    createChart(tempMsg);
                    Log.d(TAG, "msg is: " + tempMsg);
                    break;
            }

        }
    };
    int REQUEST_ENABLE_BLUETOOTH = 1;
    private EditText input;
    private final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private RecyclerView recyclerView;
    private RecViewAdapter recViewAdapter;
    private final List<Model> list = new ArrayList<>();
    private Button pairedDevices, listen, send;

    public static void createChart(String y) {


        float Y_value = Float.parseFloat(y);
        float X_value = 5.0f;

        ArrayList<Entry> entryArrayList = new ArrayList<>();
        entryArrayList.add(new Entry(X_value, Y_value));
        LineDataSet lineDataSet = new LineDataSet(entryArrayList, "Bt chart");
        lineDataSet.setColors(ColorTemplate.JOYFUL_COLORS);
        lineDataSet.setFillAlpha(110);

        LineData lineData = new LineData(lineDataSet);
        lineChart.setData(lineData);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleXEnabled(true);
        lineChart.setScaleYEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDoubleTapToZoomEnabled(true);
        lineChart.invalidate();
        lineChart.animateX(2500);


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BLUETOOTH);
        }


        pairedDevices = findViewById(R.id.pairedDevice);
        listen = findViewById(R.id.listenBtn);
        send = findViewById(R.id.sendBtn);
        input = findViewById(R.id.input);
        lineChart = findViewById(R.id.lineChart);


        listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ServerClass serverClass = new ServerClass();
                serverClass.start();
            }
        });


        status = findViewById(R.id.status);
        recyclerView = findViewById(R.id.deviceList);
        recViewAdapter = new RecViewAdapter(getApplicationContext(), list);
        pairedDevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();


                if (bt.size() > 0) {
                    for (BluetoothDevice device : bt) {
                        list.add(new Model(device, device.getName(), device.getAddress()));
                        recViewAdapter.notifyDataSetChanged();
                    }

                }
            }
        });


        recyclerView.setAdapter(recViewAdapter);
        recViewAdapter.notifyDataSetChanged();
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = String.valueOf(input.getText());
                sendReceive.write(string.getBytes());
                input.setText("");
            }
        });


    }

    public static class ClientClass extends Thread {
        private final BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass(BluetoothDevice device1) {
            device = device1;

            try {
                socket = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.d(TAG, "ClientClass: "+e);
                e.printStackTrace();
            }
        }

        public void run() {
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive = new SendReceive(socket);
                sendReceive.start();

            } catch (IOException e) {
                e.printStackTrace();
                Log.d(TAG, "run: "+e);
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
    }

    private static class SendReceive extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inputStream = tempIn;
            outputStream = tempOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ServerClass extends Thread {
        private BluetoothServerSocket serverSocket;

        public ServerClass() {
            try {
                serverSocket = bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME, MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            BluetoothSocket socket = null;

            while (socket == null) {
                try {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTING;
                    handler.sendMessage(message);

                    socket = serverSocket.accept();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                }

                if (socket != null) {
                    Message message = Message.obtain();
                    message.what = STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendReceive = new SendReceive(socket);
                    sendReceive.start();

                    break;
                }
            }
        }
    }
}