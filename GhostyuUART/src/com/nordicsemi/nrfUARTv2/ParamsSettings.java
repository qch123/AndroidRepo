package com.nordicsemi.nrfUARTv2;

import android.app.ActionBar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashSet;
import java.util.Set;

public abstract class ParamsSettings extends PreferenceFragment {
	
	DataManager mDataManager;
	RTUData mData;
	
	static final int VALUE_TYPE_INTEGER = 0;
	static final int VALUE_TYPE_STRING = 1;
	static final int VALUE_TYPE_HEX = 2;
	
	void setupSwitchEditTextPreference(String key) {
		final SwitchEditTextPreference preference = (SwitchEditTextPreference) findPreference(key);
		preference.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);
		final RTU rtu = mData.getRTU(key);
		byte[] data = getValue(key);
//		byte[] data = Utils.toHexBytes("00000020");
		int value = Utils.toInteger(data);
		if (value == 0) {
			preference.setShouldChecked(false);
		} else {
			preference.setShouldChecked(true);
		}
		preference.setText(String.valueOf(value));
		preference.setSummary(getSummary(value, rtu));
		preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference pref, Object value) {
				String text = value.toString();
				setValue(pref.getKey(), Integer.valueOf(text),0,4);
				preference.setText(text);
				preference.setSummary(getSummary(text, rtu));
				return true;
			}
		});
	}
	
	String getSummary(String summary,RTU rtu) {
		int value = Integer.valueOf(summary);
		return getSummary(value, rtu);
	}
	
	String getSummary(int value,RTU rtu) {
		String unit = rtu.getUnit();
		String summary = String.valueOf(value * rtu.getUnitMeasure());
		
		if (rtu.getFlag() == RTU.FLAG_DOUBLE) {
			summary = String.valueOf(value * rtu.getMeasure());
		}
		
		if (!TextUtils.isEmpty(unit)) {
			summary += rtu.getUnit();
		} 
		
		return summary;
	}
	
	void setupMySwitchPreference(String key) {
		final MySwitchPreference preference = (MySwitchPreference) findPreference(key);
		byte[] data = getValue(key);
		String value = Utils.toIntegerString(data);
		if (value.equals("0")) {
			preference.setShouldChecked(false);
		} else {
			preference.setShouldChecked(true);
		}
		preference
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference pref,
							Object value) {
						int result = 0;
						if (Boolean.valueOf(value.toString()).equals(true)) {
							result = 1;
						}
						setValue(pref.getKey(), result,0,4);
						return true;
					}
				});
	}
	
	void setupSwitchPreference(String key) {
		setupSwitchPreference(key,0,4);
	}
	
	void setupSwitchPreference(String key,final int from,final int len) {
		SwitchPreference preference = (SwitchPreference) findPreference(key);
		byte[] data = getValue(key);
		String value = Utils.toIntegerString(data,from,len);
		if (value.equals("0")) {
			preference.setChecked(false);
		} else {
			preference.setChecked(true);
		}
		preference
				.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {

					@Override
					public boolean onPreferenceChange(Preference pref,
							Object value) {
						int data = 0;
						if (value.equals(true)) {
							data = 1;
						}
						setValue(pref.getKey(), data,from,len);
						return true;
					}
				});
	}
	
	void setupEditTextPreference(String key,final int from,final int len) {
		final EditTextPreference preference = (EditTextPreference) findPreference(key);
		preference.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);
		final RTU rtu = mData.getRTU(key);
		byte[] data = getValue(key);
		String value = Utils.toIntegerString(data,from,len);
		preference.setText(value);
		preference.setSummary(getSummary(value, rtu));
		preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference pref, Object value) {
				setValue(pref.getKey(), Integer.valueOf(value.toString()),from,len);
				preference.setText(value.toString());
				preference.setSummary(getSummary(value.toString(), rtu));
				return true;
			}
		});
	}
	
	void setupEditTextPreference(String key) {
		setupEditTextPreference(key,0,4);
	}
	
	void setupListPreference(String key,int from,int len) {
		setupListPreference(key,VALUE_TYPE_INTEGER,from,len);
	}
	
	void setupListPreference(String key) {
		setupListPreference(key, VALUE_TYPE_INTEGER,0,4);
	}
	
	protected byte[] getValue(String key) {
		return mData.getValue(key);
	}
	
	protected void setValue(String key,byte[] value) {
		mData.setValue(key,value);
	}
	
	protected void setValue(String key, int intValue, int from, int len) {
		mData.setValue(key,intValue,from,len);
	}
	
	protected void setValue(String key,byte[] value,int from,int len) {
		mData.setValue(key,value,from,len);
	}
	
	void setupListPreference(String key,final int valueType,final int from,final int len) {
		final ListPreference preference = (ListPreference) findPreference(key);
		byte[] datas = getValue(key);
		switch (valueType) {
		case VALUE_TYPE_INTEGER:
			String value = Utils.toIntegerString(datas,from,len);
			setPreferenceIndex(preference, value);
			break;
		case VALUE_TYPE_STRING:
			break;
		case VALUE_TYPE_HEX:
			value = Utils.toHexString(datas,from,len);
			setPreferenceIndex(preference, value);
			break;
		}
		preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference arg0, Object value) {
				switch (valueType) {
				case VALUE_TYPE_INTEGER:
					setValue(preference.getKey(), Integer.valueOf(value.toString()),from,len);
					setPreferenceIndex(preference, String.valueOf(value));
					break;
				case VALUE_TYPE_HEX:
					setValue(preference.getKey(), Utils.toHexBytes(value.toString()),from,len);
					setPreferenceIndex(preference, String.valueOf(value));
					break;
				default:
					break;
				}
				
				return false;
			}
		});
	}

	 void setPreferenceIndex(ListPreference preference,
			String value) {
		CharSequence values[] = preference.getEntryValues();
		int length = values.length;
		for (int i = 0; i < length; i++) {
			if (values[i].toString().equalsIgnoreCase(value)) {
				preference.setValueIndex(i);
				preference.setSummary(preference.getEntries()[i]);
				return;
			}
		}
	}
	 
	void setupMultiSelectPreference(String key) {
		final MultiSelectListPreference preference = (MultiSelectListPreference) findPreference(key);
		byte data[] = getValue(key);
		int len = 2;
		byte[] value = new byte[len];
		System.arraycopy(data, 2, value, 0, len);
		int intValue = ((value[0] & 0xff) << 8) + (value[1] & 0xff);
		// boolean values[] = new boolean[len * 8];
		Set<String> valueSet = new HashSet<String>();
		int mask = 0x8000;
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < len * 8; i++) {
			if ((intValue & mask) == mask) {
				valueSet.add(Integer.toHexString(mask));
				int index = preference.findIndexOfValue(Integer.toHexString(mask));
				if (index == -1) {
					assert false;
				}
				buffer.append(preference.getEntries()[index]).append(",");
			}
			mask = mask >> 1;
		}
		preference.setValues(valueSet);
		String summary = buffer.toString();
		if (summary.endsWith(",")) {
			summary = summary.substring(0,summary.length()-1);
		}
		preference.setSummary(summary);
		
		preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			
			@Override
			public boolean onPreferenceChange(Preference arg0, Object arg1) {
				HashSet<String> set = (HashSet<String>) arg1;
				int value = 0;
				StringBuffer buffer = new StringBuffer();
				for (String setValue: set) {
					value |= Utils.toInteger(Utils.toHexBytes(setValue));
					int index = preference.findIndexOfValue(setValue);
					buffer.append(preference.getEntries()[index]).append(",");
				}
				preference.setValues(set);
				String summary = buffer.toString();
				if (summary.endsWith(",")) {
					summary = summary.substring(0,summary.length()-1);
				}
				preference.setSummary(summary);
				setValue(preference.getKey(), value,2,2);
				return false;
			}
		});
	}

	public static class SensorParamsSettings extends ParamsSettings {

		@Override
		protected void setupResource() {
			addPreferencesFromResource(R.xml.sensor_settings);
		}

		@Override
		void load() {
			setupEditTextPreference(RTUData.KEY_SENSOR_PREHEAT_TIME);
			setupMySwitchPreference(RTUData.KEY_SENSOR_CHANNELS1);
			setupMySwitchPreference(RTUData.KEY_SENSOR_CHANNELS2);
			setupMySwitchPreference(RTUData.KEY_SENSOR_CHANNELS3);
			setupMySwitchPreference(RTUData.KEY_SENSOR_CHANNELS4);
			setupMySwitchPreference(RTUData.KEY_SENSOR_CHANNELS5);
			setupMySwitchPreference(RTUData.KEY_SENSOR_CHANNELS6);
			setupMySwitchPreference(RTUData.KEY_SENSOR_CHANNELS7);
			setupMySwitchPreference(RTUData.KEY_SENSOR_CHANNELS8);
			setupMySwitchPreference(RTUData.KEY_SENSOR_CHANNELS9);
			setupMySwitchPreference(RTUData.KEY_SENSOR_CHANNELS10);
			setupMySwitchPreference(RTUData.KEY_SENSOR_CHANNELS11);
			setupMySwitchPreference(RTUData.KEY_SENSOR_CHANNELS12);
			setupMySwitchPreference(RTUData.KEY_SENSOR_CHANNELS13);
			setupMySwitchPreference(RTUData.KEY_SENSOR_CHANNELS14);
			setupMySwitchPreference(RTUData.KEY_SENSOR_CHANNELS15);
			setupMySwitchPreference(RTUData.KEY_SENSOR_CHANNELS16);
		}

		@Override
		public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
				Preference preference) {
			if (!RTUData.KEY_SENSOR_PREHEAT_TIME.equals(preference.getKey())) {
				Intent intent = new Intent();
				intent.putExtra("key", preference.getKey());
				preference.setIntent(intent);
			}
			return super.onPreferenceTreeClick(preferenceScreen, preference);
		}
		
		
	}

	public static class TimerReport extends ParamsSettings {

		@Override
		protected void setupResource() {
			addPreferencesFromResource(R.xml.timer_reporter);
		}

		@Override
		void load() {
			setupListPreference(RTUData.KEY_SELF_REPORTER_TYPE);
			setupEditTextPreference(RTUData.KEY_SELF_REPORTER_INTERVAL,2,2);
		}

		@Override
		void setupListPreference(final String key) {
			final ListPreference preference = (ListPreference) findPreference(key);
//			byte datas[] = Utils.toHexBytes("00020000");
//			byte data = datas[1];
			byte data = getValue(key)[1];//2nd byte
			int value = (data & 0x04) >> 2; //mask -> 0000 0100;
			
			setPreferenceIndex(preference, String.valueOf(value));
			preference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
				
				@Override
				public boolean onPreferenceChange(Preference arg, Object newValue) {
					setPreferenceIndex(preference, newValue.toString());
					int value = Utils.toInteger(getValue(key));
					int data = Integer.valueOf(newValue.toString());
					value = (value | 0x40000) & (data << 18);
					setValue(key, value,0,4);
					return false;
				}
			});
		}

		@Override
		String getSummary(int value, RTU rtu) {
			if (value == 0) {
				return getString(R.string.shutdown);
			}
			return super.getSummary(value, rtu);
		}
		
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActionBar bar = getActivity().getActionBar();
		bar.setDisplayOptions(ActionBar.DISPLAY_HOME_AS_UP
				| ActionBar.DISPLAY_SHOW_TITLE);
		
		setHasOptionsMenu(true);
		
		mDataManager = DataManager.getInstance(getActivity());
		mData = mDataManager.getRTUData();
		setupResource();
		
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getActivity());
		if (true || p.getBoolean("preference_update", false)) {
			try {
				Utils.log("load...");
				load();
			} finally {
				SharedPreferences.Editor editor = p.edit();
				editor.putBoolean("preference_update", false);
				editor.commit();
			}
		}
	}

	void load(){
		//do nothing
		//child class can override to load datas
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		menu.clear();
		if (Utils.debugOn()) {
			inflater.inflate(R.menu.null_menu, menu); 
		}
	}
	
	

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.show_cache_menu:
			mData.showCache();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Utils.log("EntiretyParamsSettings.onActivityCreated");
	}
	
	@Override
	public void onStart() {
		Utils.log("EntiretyParamsSettings.onStart.before");
		super.onStart();
		Utils.log("EntiretyParamsSettings.onStart");
	}

	protected abstract void setupResource();

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

//	String getDataValue(String key) {
//		return mDataManager.getDataValue(key);
//	}
}
