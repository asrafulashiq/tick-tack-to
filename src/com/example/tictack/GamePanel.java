package com.example.tictack;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;

public class GamePanel extends View{

	public GamePanel(Context context) {
		super(context);

		
	}

	@Override
	public void invalidate(int l, int t, int r, int b) {
		// TODO Auto-generated method stub
		super.invalidate(l, t, r, b);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// TODO Auto-generated method stub
		super.onDraw(canvas);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		return super.onTouchEvent(event);
	}
	
	

}
