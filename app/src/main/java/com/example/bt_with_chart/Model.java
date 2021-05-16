package com.example.bt_with_chart;

import android.bluetooth.BluetoothDevice;

public class Model {
    String name , macAddress;
    BluetoothDevice device;

    public Model(BluetoothDevice device , String name, String macAddress) {
        this.device = device;
        this.name = name;
        this.macAddress = macAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    @Override
    public String toString() {
        return "Model{" +
                "name='" + name + '\'' +
                ", macAddress='" + macAddress + '\'' +
                '}';
    }
}
