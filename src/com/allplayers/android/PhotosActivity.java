package com.allplayers.android;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class PhotosActivity extends Activity
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		TextView tv = new TextView(this);
		tv.setText("Photos Activity");
		setContentView(tv);
	}
}