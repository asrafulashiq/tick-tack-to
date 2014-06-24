package com.example.tictack;

import java.util.Random;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Game extends Activity {

	private static final String TAG = "BluetoothChat";
    private static final boolean D = true;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_PLAYER_SELECTION = 6; // selection for which player you are - 1 or 2 ?    
    
    
    public boolean game_started = false;
    public boolean my_turn = false;
    public String my_token = null;
    public String opponent_token=null;

    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    public static final int PLAYER_ONE = -1;
    public static final int PLAYER_TWO = -2;
    public static final String PLAYER_SELECTION = "your player :";
    public static final int GAME_WON = -3;
    public static final int GAME_DRAW = -4;
    public static final int GAME_LOST = -5;
    public static final int GAME_CONTINUE = -6;
    public static final int NONE = 0;
    
    public  int MY_PLAYER ;
    public  int OPPONENT_PLAYER;
    public Button[] buttons = new Button[9];
    
    
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;
	public static final String READ = "read";
	
	
	// game play varibles 
	private int[] buttonTag = new int[9];
	

    // Name of the connected device
    private String mConnectedDeviceName = null;
    private StringBuffer mOutStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter mBluetoothAdapter = null;
    // Member object for the chat services
    private CommunicationService mChatService = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        
        
        for(int i=0;i<9;i++){
        	this.buttonTag[i]=this.NONE;
        	
			int resID = getResources().getIdentifier("Button"+(i+1),"id",getPackageName());
			this.buttons[i] = (Button)this.findViewById(resID);

        }

		
		
	}
	
	@Override
	 public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (mChatService == null) {
            	this.setUp();
            }
        }
    }
	
	public void setUp(){
		this.mChatService = new CommunicationService(this,this.mHandler);
        mOutStringBuffer = new StringBuffer("");

	}
	
	 @Override
	    public synchronized void onResume() {
	        super.onResume();
	        if(D) Log.e(TAG, "+ ON RESUME +");

	        // Performing this check in onResume() covers the case in which BT was
	        // not enabled during onStart(), so we were paused to enable it...
	        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
	        if (mChatService != null) {
	            // Only if the state is STATE_NONE, do we know that we haven't started already
	            if (mChatService.getState() == CommunicationService.STATE_NONE) {
	              // Start the Bluetooth chat services
	              mChatService.start();
	            }
	        }
	    }

	
	
	private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
            .getString(DeviceList.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }
	
	

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }
	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.option_menu, menu);
		return true;
	}
	
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent serverIntent = null;
        switch (item.getItemId()) {
        case R.id.secure_connect_scan:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceList.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            return true;
        case R.id.insecure_connect_scan:
            // Launch the DeviceListActivity to see devices and do scan
            serverIntent = new Intent(this, DeviceList.class);
            startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
            return true;
        case R.id.discoverable:
            // Ensure this device is discoverable by others
            ensureDiscoverable();
            return true;
        }
        return false;
    }

	
    /**
     * Sends a message.
     * @param playerTwo  A string of text to send.
     */
    private void sendMessage(int in) {

        // Check that we're actually connected before trying anything
        if (mChatService.getState() != CommunicationService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
       
            // Get the message bytes and tell the BluetoothChatService to write
        	String message = in+"";
            byte[] send =  message.getBytes() ;//message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
          //  mOutStringBuffer.setLength(0);
           // mOutEditText.setText(mOutStringBuffer);
        
        
    }
	
	
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode) {
        case REQUEST_CONNECT_DEVICE_SECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, true);
            }
            break;
        case REQUEST_CONNECT_DEVICE_INSECURE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                connectDevice(data, false);
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
                // Bluetooth is now enabled, so set up a chat session
                //setupChat();
            } else {
                // User did not enable Bluetooth or an error occurred
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }
	
    private final void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(resId);
       
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(subTitle);
    }
	
	private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1) {
                case CommunicationService.STATE_CONNECTED:
                    setStatus(getString(R.string.title_connected_to, mConnectedDeviceName));
                   // mConversationArrayAdapter.clear();
                    startGame();
                    break;
                case CommunicationService.STATE_CONNECTING:
                    setStatus(R.string.title_connecting);
                    break;
                case CommunicationService.STATE_LISTEN:
                case CommunicationService.STATE_NONE:
                    setStatus(R.string.title_not_connected);
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                //mConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
            	
            	byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                int m = new Integer((readMessage).trim()).intValue();
            	readMessage(m);
            	
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_PLAYER_SELECTION:
            	if( (msg.getData().getString(PLAYER_SELECTION)).equals("two") ){
            		
            	}
            	
            }
        }
    };

	protected void startGame() {
		// TODO Auto-generated method stub
		((GridLayout) this.findViewById(R.id.gridLayout)).setVisibility(View.VISIBLE);
		this.game_started = true;
		//this.setStatus(this.getString(R.string.game_started_dialog));
		Toast.makeText(getApplicationContext(), this.getString(R.string.game_started_dialog), Toast.LENGTH_SHORT).show();
		
		
		
	}
	
	
	protected void readMessage(int l) {
		// TODO Auto-generated method stub
		if(l==this.PLAYER_ONE){
			this.MY_PLAYER=this.PLAYER_ONE;
			this.OPPONENT_PLAYER = this.PLAYER_TWO;
			this.my_token="X";
			this.opponent_token="0";
			this.setStatus(this.getString(R.string.player1_turn));
			( (ImageView) this.findViewById(R.id.imageView1)).setClickable(false);
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.my_turn=true;

		}
		else if(l==this.PLAYER_TWO){
			this.MY_PLAYER = this.PLAYER_TWO;
			this.OPPONENT_PLAYER = this.PLAYER_ONE;
			this.my_turn=false;
			this.my_token="0";
			this.opponent_token="X";
			Toast.makeText(getApplicationContext(), "You are player 2", Toast.LENGTH_LONG).show();
			this.setStatus(this.getString(R.string.opponent_turn));
		}
		
		else if(l==this.GAME_DRAW){
			this.my_turn=false;
			this.finishGame(this.GAME_DRAW);
			//this.setStatus(this.getString(R.string.game_draw));
			
		}
		else if(l==this.GAME_WON){
			this.my_turn=false;
			this.finishGame(this.GAME_LOST);
			//this.setStatus(this.getString(R.string.you_lost));
		}
		else{
			int pos = l;
			this.buttons[pos].setText(this.opponent_token);
			this.my_turn = true;
			this.setStatus(this.getString(R.string.your_turn));
		}
		
		
		
	}

	/**
	 * 
	 * @param view view for the button clicked
	 */
	public void buttonClicked(View view){
		if(this.game_started==true){
			if(this.my_turn==true){
				int pos = Integer.valueOf((String)view.getTag());
				
				
				if(this.buttonTag[pos]==this.NONE){
					
					this.buttonTag[pos]=this.MY_PLAYER;
					this.my_turn=false;
					
					((Button)view).setText(this.my_token);
					this.sendMessage(pos);
					// send game result
					if(this.gameResult(this.MY_PLAYER)==this.GAME_WON){
						this.sendMessage(this.GAME_WON);
						
						this.finishGame(this.GAME_WON);
					}
					else if(this.gameResult(this.MY_PLAYER)==this.GAME_DRAW){
						this.sendMessage(this.GAME_DRAW);
						
						this.finishGame(this.GAME_DRAW);
					}
					else{
						this.setStatus(this.getString(R.string.opponent_turn));
					}
					
					
				}
				else{
					Toast.makeText(getApplicationContext(), "Already filled\n Click to another box", Toast.LENGTH_SHORT).show();
				}
			}
		}
		
	}
	private void finishGame(int mode) {
		// TODO Auto-generated method stub
		if(mode==this.GAME_DRAW){
			this.setStatus(R.string.game_draw);
		}
		else if(mode==this.GAME_LOST){
			this.setStatus(R.string.you_lost);
		}
		else if(mode==this.GAME_WON){
			this.setStatus(R.string.you_won);
		}
		
	}

	/*
	 * check if game is finished
	 */
	private int gameResult(int token) {
		if( 
				(this.buttonTag[0]==token && this.buttonTag[1]==token && this.buttonTag[1]==token) ||
				(this.buttonTag[3]==token && this.buttonTag[4]==token && this.buttonTag[5]==token) ||
				(this.buttonTag[6]==token && this.buttonTag[7]==token && this.buttonTag[8]==token) ||
				(this.buttonTag[0]==token && this.buttonTag[3]==token && this.buttonTag[6]==token) ||
				(this.buttonTag[1]==token && this.buttonTag[4]==token && this.buttonTag[9]==token) ||
				(this.buttonTag[2]==token && this.buttonTag[5]==token && this.buttonTag[8]==token) ||
				(this.buttonTag[0]==token && this.buttonTag[4]==token && this.buttonTag[8]==token) ||
				(this.buttonTag[2]==token && this.buttonTag[4]==token && this.buttonTag[6]==token) 
				)
		{
			return this.GAME_WON;
		}
		
		else {
			
			for(int i=0;i<9;i++){
				if(this.buttonTag[i]==this.NONE)
					return this.GAME_CONTINUE;
			}
			return this.GAME_DRAW;
			
		}
		
		//return 0;
	}


	/**
	 * initialize who is player 1 or two
	 */
	public void initialize_player(View view){
		if(this.game_started==true){
		Random r = new Random();
		if(r.nextBoolean()){
			//this.setStatus("You are player 1");
			this.sendMessage(this.PLAYER_TWO);
			this.MY_PLAYER=this.PLAYER_ONE;
			this.OPPONENT_PLAYER = this.PLAYER_TWO;
			this.my_token="X";
			this.opponent_token="0";
			this.setStatus(this.getString(R.string.player1_turn));
			
			/*try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			
			this.my_turn=true;
			
		}
		else{
			this.setStatus("You are player 2");
			this.sendMessage(this.PLAYER_ONE);
			this.MY_PLAYER = this.PLAYER_TWO;
			this.OPPONENT_PLAYER=this.PLAYER_ONE;
			this.my_turn=false;
			this.my_token="0";
			this.opponent_token="X";
			//this.setStatus(this.getString(R.string.opponent_turn));
			
		}
		
	}
	}

	

}
