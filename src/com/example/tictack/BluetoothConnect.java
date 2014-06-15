package com.example.tictack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class BluetoothConnect extends Activity implements OnClickListener{

	public static final int REQUEST_ENABLE_BT = 1;
	
	private BluetoothAdapter mBluetoothAdapter;
	
	private ListView discover_list_view ;
	private ListView pair_list_view;
	
	private ArrayAdapter<String> mPairedDevicesArrayAdapter ;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_bluetooth_connect);
		
		this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		this.discover_list_view = (ListView)findViewById(R.id.discoveredDevices);
		this.pair_list_view = (ListView)this.findViewById(R.id.pairedDevices);
		this.mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		this.mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1);
		
		Button b1 = (Button)this.findViewById(R.id.pair);
		Button b2 = (Button)this.findViewById(R.id.discover);
		
		b1.setOnClickListener(this);
		b2.setOnClickListener(this);
		
		
		
		
		if(this.mBluetoothAdapter==null){
			// device doesn't support bluetooth
			Toast.makeText(this, "Device does not support bluetooth", Toast.LENGTH_LONG).show();
			this.finish();
		}
		else{
			
			if(!this.mBluetoothAdapter.isEnabled()){
				// enable bluetooth
				Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				this.startActivityForResult(intent, this.REQUEST_ENABLE_BT);
				
			}
			
		}
		
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}
	
	/*
	 * show lists of paired devices in list view
	 */
	public void pairing(){
		Set<BluetoothDevice> pairedDevicesSet = this.mBluetoothAdapter.getBondedDevices();
		if(!this.mPairedDevicesArrayAdapter.isEmpty()){
			this.mPairedDevicesArrayAdapter.clear();
		}
		
		for(BluetoothDevice device:pairedDevicesSet){
			this.mPairedDevicesArrayAdapter.add(device.getName()+"\n"+device.getAddress());
		}
		
		this.pair_list_view.setAdapter(mPairedDevicesArrayAdapter);
		
		
	}
	
	/*
	 * show list of discovered devices in list view
	 */
	public void discoverDevices(){
		
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);
		this.mBluetoothAdapter.startDiscovery();
		
		
	}
	
	
	// create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver(){

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			String action = intent.getAction();
			
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				mNewDevicesArrayAdapter.add(device.getName()+"\n"+device.getAddress());
			}
			
			discover_list_view.setAdapter(mNewDevicesArrayAdapter);
		}
		
	};
	
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
		case R.id.pair:
			this.pairing();
			break;
			
		case R.id.discover:
			this.discoverDevices();
			break;
		}
		
	}

	

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode==this.REQUEST_ENABLE_BT){
			if(resultCode==RESULT_OK){
				Toast.makeText(getApplicationContext(), "bluetooth is enabled", Toast.LENGTH_SHORT).show();
				// bluetooth has been enabled
				
			}
			else {
				Toast.makeText(getApplicationContext(), "Bluetooth is not enabled",Toast.LENGTH_SHORT).show();
				this.finish();
			}
		}
	}
	
	
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		this.unregisterReceiver(mReceiver);
	}

	

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}


	
	

	
	
}
	
	
	

