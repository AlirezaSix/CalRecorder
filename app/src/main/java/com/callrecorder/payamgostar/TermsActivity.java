package com.callrecorder.payamgostar;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

public class TermsActivity extends Activity {

	public TextView mTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.terms_layout);

		mTextView = (TextView) findViewById(R.id.txtTerms2);
	}
}
