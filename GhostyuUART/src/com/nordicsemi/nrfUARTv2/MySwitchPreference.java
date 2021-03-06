package com.nordicsemi.nrfUARTv2;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

public class MySwitchPreference extends Preference implements OnCheckedChangeListener{
	
	private Switch mSwitch;
	private boolean mShouldChecked = false;
	
	public MySwitchPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onBindView(View view) {
		super.onBindView(view);
		setupSwitch(view);
	}
	
	private void setupSwitch(View parent) {
		mSwitch = (Switch) parent.findViewById(R.id.prf_switch);
		mSwitch.setOnCheckedChangeListener(this);
		mSwitch.setChecked(mShouldChecked);
	}

	@Override
	protected View onCreateView(ViewGroup parent) {
		Bundle b = getExtras();
		b.putString("key", getKey());
		return super.onCreateView(parent);
	}

	@Override
	public void onCheckedChanged(CompoundButton button, boolean isChecked) {
		mShouldChecked = isChecked;
		callChangeListener(isChecked);
	}
	
	public void setChecked(boolean on) {
		mSwitch.setChecked(on);
	}
	
	public boolean isSwitchOn() {
		return mSwitch.isChecked();
	}
	
	public void setShouldChecked(boolean checked){
		mShouldChecked = checked;
	}
}

