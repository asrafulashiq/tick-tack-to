package com.example.tictack;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothServerSocket;
import android.view.Menu;
import android.widget.LinearLayout;

public class Game extends Activity {

	private LinearLayout layout;
	private GamePanel panel;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game);
		
		
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game, menu);
		return true;
	}
	
	

}
