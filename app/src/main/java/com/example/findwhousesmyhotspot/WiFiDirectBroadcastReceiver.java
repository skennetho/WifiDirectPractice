/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.findwhousesmyhotspot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.util.Log;

/**
 * 중요한 Wi-Fi P2P 이벤트를 알려주는 BroadcastReceiver
 * */
public class WiFiDirectBroadcastReceiver extends BroadcastReceiver {

    final static String TAG = "DeviceListFragment";
    private WifiP2pManager manager;
    private Channel channel;
    private WiFiDirectActivity activity;

    /**
     * @param manager WifiP2pManager system service
     * @param channel Wifi p2p channel (어프리케이션을 Wifi p2p 프레임워크에 연결)
     * @param activity activity associated with the receiver(receiver가 연결된 activity)
     *
     */
    public WiFiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel, WiFiDirectActivity activity) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.activity = activity;
    }

    /*
     * (non-Javadoc)
     * @see android.content.BroadcastReceiver#onReceive(android.content.Context,
     * android.content.Intent)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            // UI update to indicate wifi p2p status. wifi p2p 상태
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                //  Wifi Direct 모드가 가능하면 WiFiDirectActivity 클래스 IsWifiP2pEnabled를 true로
                activity.setIsWifiP2pEnabled(true);
            } else {
                activity.setIsWifiP2pEnabled(false);
                activity.resetData();

            }
            Log.d(TAG, "P2P state changed - " + state);
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            /**
             *  Wi-Fi P2P manager에서 사용할 수 있는 상대방을 요청한다.
             *  비동기로 호출하고, 호출 액티비티를 PeerListListener.onPeersAvailable()에
             *  콜백으로 통지한다.
             */
            if (manager != null) {
                manager.requestPeers(channel, (PeerListListener) activity.getSupportFragmentManager().findFragmentById(R.id.frag_list));
            }
            Log.d(WiFiDirectActivity.TAG, "P2P peers changed");
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) {
                // 그룹 오너 IP를 찾기 위해 접속 정보를 요청한다.
                // we are connected with the other device, request connection
                // info to find group owner IP

                DeviceDetailFragment fragment = (DeviceDetailFragment) activity
                        .getSupportFragmentManager().findFragmentById(R.id.frag_detail);
                manager.requestConnectionInfo(channel, fragment);
            } else {
                // 연결을 끊는다
                activity.resetData();
            }
            // 디바이스 정보의 변경을 나타내는 인텐트 action을 전달한다.
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            DeviceListFragment fragment = (DeviceListFragment) activity.getSupportFragmentManager()
                    .findFragmentById(R.id.frag_list);
            fragment.updateThisDevice((WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE));
        }
    }
}
