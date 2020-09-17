package com.example.android.appp;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class BluetoothService {
    private static final String TAG = "BluetoothChatService";
    private static final boolean D = true;

    private static final String NAME = "BluetoothChat";

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final BluetoothAdapter mAdapter;
    private final Handler mHandler;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private int mState;

    public static final int STATE_NONE = 0;
    public static final int STATE_LISTEN = 1;
    public static final int STATE_CONNECTING = 2;
    public static final int STATE_CONNECTED = 3;

    public BluetoothService(Context context, Handler handler) {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mHandler = handler;
    }

    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        mHandler.obtainMessage(ChooseItem.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    public synchronized int getState() {
        return mState;
    }

    public synchronized void start() {
        if (D) Log.d(TAG, "start");

        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        setState(STATE_LISTEN);
    }

    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        }
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (D) Log.d(TAG, "connected");

        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}


        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = mHandler.obtainMessage(ChooseItem.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(ChooseItem.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");
        setState(STATE_NONE);
        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}
    }

    private void connectionFailed() {
        setState(STATE_LISTEN);

        Message msg = mHandler.obtainMessage(ChooseItem.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ChooseItem.TOAST, "无法连接设备");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private void connectionLost() {
        setState(STATE_LISTEN);

        Message msg = mHandler.obtainMessage(ChooseItem.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(ChooseItem.TOAST, "设备连接中断");
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread");
            setName("ConnectThread");

            mAdapter.cancelDiscovery();

            try {
                mmSocket.connect();
            } catch (IOException e) {
                connectionFailed();
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                BluetoothService.this.start();//restart
                return;
            }
            synchronized (BluetoothService.this) {
                mConnectThread = null;
            }
            connected(mmSocket, mmDevice);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        DataInputStream dis;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
            }

            dis=new DataInputStream(new BufferedInputStream(tmpIn));
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            int bytes;

            while (true) {
                try {
                    byte[] buffer = new byte[256];
                    bytes = mmInStream.read(buffer);
                    if(bytes>0){
                        mHandler.obtainMessage(ChooseItem.MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                    else
                    {
                        Log.e(TAG, "disconnected");
                        connectionLost();

                        if(mState != STATE_NONE){
                            Log.e(TAG, "disconnected");
                            BluetoothService.this.start();
                        }
                        break;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();

                    if(mState != STATE_NONE){
                        BluetoothService.this.start();
                    }
                    break;
                }
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
