package com.nordicsemi.nrfUARTv2;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class DataManager {
	
	private Context mContext;
	
    private UartService mService = null;
    private BluetoothDevice mDevice = null;
    private BluetoothAdapter mBtAdapter = null;

	private DataListener mDataListener;

	private static DataManager mDataManager;
	
	private RTUData mRtuData;
	
	public interface DataListener{
		public void onDataReciver(String action,Intent intent);
	}
	
	public void setDataListener(DataListener listener){
		mDataListener = listener;
	}
	
	public static DataManager getInstance(Context context){
		if (mDataManager == null) {
			mDataManager = new DataManager(context);
		}
		return mDataManager;
	}
	
	private DataManager(Context context) {
		mContext = context.getApplicationContext();
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		mRtuData = new RTUData();
	}

	public void service_init() {
		Intent bindIntent = new Intent(mContext, UartService.class);
		mContext.bindService(bindIntent, mServiceConnection,
				Context.BIND_AUTO_CREATE);

		LocalBroadcastManager.getInstance(mContext).registerReceiver(
				UARTStatusChangeReceiver, makeGattUpdateIntentFilter());
	}
	
	 private static IntentFilter makeGattUpdateIntentFilter() {
	        final IntentFilter intentFilter = new IntentFilter();
	        intentFilter.addAction(UartService.ACTION_GATT_CONNECTED);
	        intentFilter.addAction(UartService.ACTION_GATT_DISCONNECTED);
	        intentFilter.addAction(UartService.ACTION_GATT_SERVICES_DISCOVERED);
	        intentFilter.addAction(UartService.ACTION_DATA_AVAILABLE);
	        intentFilter.addAction(UartService.DEVICE_DOES_NOT_SUPPORT_UART);
	        return intentFilter;
	    }

	protected static final String TAG = "DataManager";

	private ServiceConnection mServiceConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className,
				IBinder rawBinder) {
			mService = ((UartService.LocalBinder) rawBinder).getService();
			Log.d(TAG, "onServiceConnected mService= " + mService);
			if (!mService.initialize()) {
				Log.e(TAG, "Unable to initialize Bluetooth");
			}

		}

		public void onServiceDisconnected(ComponentName classname) {
			// // mService.disconnect(mDevice);
			mService = null;
		}
	};

	public void readAllDatas() {

	}

	private final BroadcastReceiver UARTStatusChangeReceiver = new BroadcastReceiver() {

		public void onReceive(Context context, Intent intent) {
			mDataListener.onDataReciver(intent.getAction(), intent);
		}
	};
	
	public void onDestroy() {
		try {
        	LocalBroadcastManager.getInstance(mContext).unregisterReceiver(UARTStatusChangeReceiver);
        } catch (Exception ignore) {
            Log.e(TAG, ignore.toString());
        } 
        mContext.unbindService(mServiceConnection);
        mService.stopSelf();
        mService= null;
	}

	public void read() {
		
	}
	
	public void write(byte[] bytes) {
		mService.writeRXCharacteristic(bytes);
	}
	
	public BluetoothAdapter getBTAdapter(){
		return mBtAdapter;
	}
	
	public boolean isBTEnable() {
		return mBtAdapter != null && mBtAdapter.isEnabled();
	}
	
	public String getDeviceName(){
		return mDevice.getName();
	}
	
	public void enableTXNotification() {
		mService.enableTXNotification();
	}
	
	public void connect(String deviceAddress) {
		 
         mDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
        
         Log.d(TAG, "... onActivityResultdevice.address==" + mDevice + "mserviceValue" + mService);
         
         mService.connect(deviceAddress);
	}

	public void disconnect() {
		if (mService != null) {
			mService.disconnect();
			
		}
	}
	
	public void closeService() {
		mService.close();
	}

	public void fetchAll() {
		final int total = 376;
		final int length = 16;
		int times = total / length;
		for (int i=0;i<times;i++) {
			int address = length * i;
			fetch(String.valueOf(address), length);
		}
		
		fetch(String.valueOf(length * times), total - length * times);
		mRtuData.showCache();
	}
	
//	private void fetchAllInner() {
//		final int total = 376;
//		final int length = 16;
//		int times = total / length;
//		for (int i=0;i<times;i++) {
//			int address = length * i;
//			fetch(String.valueOf(address), length);
//		}
//		
//		fetch(String.valueOf(length * times), total - length * times);
//	}
	
	
	/**
	 * 
	 * @param addr   ��ʼ��ַ
	 * @param length �ֽڳ���
	 */
	public void fetch(String addr, int length) {
		SystemClock.sleep(1000);
		Utils.log("fetch "+addr+", "+length);
		StringBuffer buffer = new StringBuffer();
		String len = zeroFormat(String.valueOf(Integer.toHexString(length)), 2);
//		String data = zeroFormat("0", length * 2);
		String data = "";
		StringBuffer suffix = buffer.append(9).append(formatAddress(addr))
				.append(len).append(data);
		String checksum = Utils.checksum(suffix.toString());
		String command = suffix.append(checksum).toString();
		Utils.log("command : "+command);
		write(Utils.toHexBytes(command));
	}
	
	
	private String zeroFormat(String target,int length) {
		if (target.length() > length) {
			throw new IllegalArgumentException("target string length must be shorter," + target.length()+", "+length);
		}
		int zerolen = length - target.length();
		StringBuffer buffer = new StringBuffer();
		for (int i=0;i<zerolen;i++) {
			buffer.append(0);
		}
		return buffer.append(target).toString();
	}
	
	
	private String formatAddress(String addr) {
		return zeroFormat(addr, 3);
	}
	
	public void parse(byte[] txValue) {
		mRtuData.parse(txValue);
	}

	public String getDataValue(String key) {
		return null;
	}
}