package com.example.tictack;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RadioButton;

public class MainActivity extends Activity {

	public int mode;
	public final int SINGLE_MODE = 1;
	public final int MULTI_MODE = 2;
	
	public Dialog modeSelection;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//getActionBar().show();
		
		
	
	}
	
	public void button_clicked(View v){
		switch(v.getId()){
		case R.id.new_game:
			
			/*
			 * showing dialog for game selection mode
			 */
			modeSelection = new Dialog(this);
			modeSelection.setTitle("Game Mode");
			modeSelection.setContentView(R.layout.mode_selecting);
			
			((RadioButton)findViewById(R.id.single_player_radio)).setOnClickListener(
					new OnClickListener(){
						public void onClick(View v){
							mode = SINGLE_MODE;
							modeSelection.dismiss();
						}
					}
					
					);
			((RadioButton)findViewById(R.id.multi_player_radio)).setOnClickListener(
					new OnClickListener(){
						public void onClick(View v){
							mode = MULTI_MODE;
							modeSelection.dismiss();
						}
					}
					
					);
			// ***
			
			
			
			break;
		case R.id.continue_game:
			
			break;
		
		case R.id.about_game:
			
			break;
			
		}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
