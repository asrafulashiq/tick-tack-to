package com.example.tictack;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class DeviceList extends Activity implements OnClickListener{

	public final int REQUEST_ENABLE_BT = 1;
	
	public static final String NAME="ASHIQ";
	public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	 
	private BluetoothAdapter mBluetoothAdapter;
	
	private ListView discover_list_view ;
	private ListView pair_list_view;
	//private Set<BluetoothDevice> discoveredDevicesSet=null;
	//private Set<BluetoothDevice> pairedDevicesSet=null;
	
	public  AcceptThread acceptThread=null;
	public ConnectThread connectThread=null;
	
	public boolean continueAccepting = true;
	public boolean continueConnecting = true;
	
	
	// temp
	TextView tmp;
	
	
	private ArrayAdapter<String> mPairedDevicesArrayAdapter ;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;
	
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_bluetooth_connect);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		
		this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		this.discover_list_view = (ListView)findViewById(R.id.discoveredDevices);
		this.pair_list_view = (ListView)this.findViewById(R.id.pairedDevices);
		this.mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_activated_1);
		this.mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_activated_1);
		
		// tmp
		tmp = (TextView)this.findViewById(R.id.tmp);
		
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
		
		//this.enableDiscoverity();
		
		this.discover_list_view.setOnItemClickListener(
				new OnItemClickListener(){

					@Override
					public void onItemClick(AdapterView<?> adapter, View view,
							int position, long id) {
						// TODO Auto-generated method stub
						String deviceIdentity = (String) adapter.getItemAtPosition(position);
						String address = getAddress(deviceIdentity);
						if(BluetoothAdapter.checkBluetoothAddress(address)){
							if(connectThread!=null){
								connectThread.cancel();
								connectThread=null;
							}
							connectThread = new ConnectThread(mBluetoothAdapter.getRemoteDevice(getAddress(deviceIdentity)));
							connectThread.start();
							Toast.makeText(getApplicationContext(), "initializing connection to "+deviceIdentity, Toast.LENGTH_SHORT).show();

							
						}
						else{
							Toast.makeText(getApplicationContext(), "wrong bluetooth address", Toast.LENGTH_SHORT).show();
						}
					}
					
				}
				);
		
		
		this.pair_list_view.setOnItemClickListener(
				new OnItemClickListener(){

					@Override
					public void onItemClick(AdapterView<?> adapter, View view,
							int position, long id) {
						// TODO Auto-generated method stub
						String deviceIdentity = (String) adapter.getItemAtPosition(position);
						//Toast.makeText(getApplicationContext(), "initializing connection to "+deviceIdentity, Toast.LENGTH_SHORT).show();

						String address = getAddress(deviceIdentity);
						if(BluetoothAdapter.checkBluetoothAddress(address)){
							if(connectThread!=null){
								connectThread.cancel();
								connectThread=null;
							}
							connectThread = new ConnectThread(mBluetoothAdapter.getRemoteDevice(getAddress(deviceIdentity)));
							Toast.makeText(getApplicationContext(), "initializing connection to "+deviceIdentity, Toast.LENGTH_SHORT).show();
							connectThread.start();

							
						}
						else{
							Toast.makeText(getApplicationContext(), "wrong bluetooth address", Toast.LENGTH_SHORT).show();
						}
						
						
					
					}
					
				}
				);
		
		
		
		this.acceptThread = new AcceptThread();
		this.acceptThread.start();
		
	}
	
	
	public String getAddress(String str){
		String t[]=str.split("\n");
		return t[1].trim();
		
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		return super.onOptionsItemSelected(item);
	}
	
	
	public void enableDiscoverity(){
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
		startActivity(discoverableIntent);
	}
	
	/*
	 * show lists of paired devices in list view
	 */
	public void pairing(){
		
	 Set<BluetoothDevice>  pairedDevicesSet = this.mBluetoothAdapter.getBondedDevices();
		/*if(!this.mPairedDevicesArrayAdapter.isEmpty()){
			this.mPairedDevicesArrayAdapter.clear();
		}*/
		
		for(BluetoothDevice device:pairedDevicesSet){
			this.mPairedDevicesArrayAdapter.add(device.getName()+"\n"+device.getAddress());
		}
		
		this.pair_list_view.setAdapter(this.mPairedDevicesArrayAdapter);
		
		
	}
	
	/*
	 * show list of discovered devices in list view
	 */
	public void discoverDevices(){
        setProgressBarIndeterminateVisibility(true);

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
				
				//
				//discoveredDevicesSet.add(device);
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
	
	private class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	 
	    public ConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device;
	 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
	        } catch (IOException e) { }
	        mmSocket = tmp;
	    }
	 
	    
		public void run() {
	        // Cancel discovery because it will slow down the connection
	        mBluetoothAdapter.cancelDiscovery();
	 
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	            Toast.makeText(getApplicationContext(), "Connected with "+mmDevice.getName(), Toast.LENGTH_SHORT).show();
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) { }
	            return;
	        }
	 
	 
	    }
	 
	    /** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	
	
	private class AcceptThread extends Thread {
	    private final BluetoothServerSocket mmServerSocket;
	 
	    public AcceptThread() {
	        // Use a temporary object that is later assigned to mmServerSocket,
	        // because mmServerSocket is final
	        BluetoothServerSocket tmp = null;
	        try {
	            // MY_UUID is the app's UUID string, also used by the client code
	            tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
	        } catch (IOException e) { }
	        mmServerSocket = tmp;
	    }
	 
	    public void run() {
	        BluetoothSocket socket = null;
	        // Keep listening until exception occurs or a socket is returned
	        while (true) {
	            try {
	                socket = mmServerSocket.accept();
	                
	                Toast.makeText(getApplicationContext(), "Received connection", Toast.LENGTH_LONG).show();
	            } catch (IOException e) {
	                break;
	            }
	            // If a connection was accepted
	            if (socket != null) {
	                // Do work to manage the connection (in a separate thread)
	                //manageConnectedSocket(socket);
	                try {
						mmServerSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	                break;
	            }
	        }
	    }
	}
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if(this.mBluetoothAdapter!=null){
			this.mBluetoothAdapter.cancelDiscovery();
		}
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
	
	
	

