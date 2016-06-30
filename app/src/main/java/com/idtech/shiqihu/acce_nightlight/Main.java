package com.idtech.shiqihu.acce_nightlight;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import java.util.Timer;
import java.util.TimerTask;



public class Main extends Activity implements SensorEventListener {


	private float mLastX, mLastY, mLastZ;
	private boolean mInitialized;
	private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private double NOISE = (double) 0.3 ;
	public static Camera camera;
	private Camera.Parameters parameters = null;
	public boolean isFlash = false;
	private boolean isOff = false;
	private TextView errorMsg;
	public boolean timeOff = true;
	public PowerManager mgr;
	public Timer timer = new Timer();
	public Timer timer2 = new Timer();
	public int iClicks;
	public int delay_in_milliseconds = 1000;
	public WindowManager.LayoutParams params;




	// Call `launchTestService()` in the activity
	// to startup the service
	/*public void launchTestService() {
		// Construct our Intent specifying the Service
		Intent i = new Intent(this, Background.class);
		// Add extras to the bundle
		i.putExtra("foo", "bar");
		// Start the service
		startService(i);
	}
	*/




	public void highSensitivity(View view){
		NOISE = 0.1;
	}
	public void mediumSensitivity(View view){
		NOISE = 0.2;
	}
	public void lowSensitivity(View view){
		NOISE = 0.3;
	}

	public void increaseBr(View view){

		params.screenBrightness = -1;
		getWindow().setAttributes(params);
		timeOff = false;
		timer2.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable(){
					@Override
					public void run(){
						// task to be done every 1000 milliseconds
						int count = 0;
						count++;
						if (count > 2500 ){
							timeOff = true;
							count = 0;
						}
						
					}
				});

			}
		}, 0, delay_in_milliseconds);
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				runOnUiThread(new Runnable(){
					@Override
					public void run(){
						TextView txtClicks = (TextView) findViewById(R.id.txtClicks);

						// task to be done every 1000 milliseconds
						iClicks++;
						txtClicks.setText(String.valueOf(iClicks));
					}
				});

			}
		}, 0, delay_in_milliseconds);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		mgr = (PowerManager)getSystemService(Context.POWER_SERVICE);
		PowerManager.WakeLock wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
		wakeLock.acquire();


		TextView tvX= (TextView)findViewById(R.id.x_axis);
		TextView tvY= (TextView)findViewById(R.id.y_axis);
		TextView tvZ= (TextView)findViewById(R.id.z_axis);
		//ImageView iv = (ImageView)findViewById(R.id.image);
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		if (!mInitialized) {
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			tvX.setText("0.0");
			tvY.setText("0.0");
			tvZ.setText("0.0");
			mInitialized = true;
		} else {
			float deltaX = Math.abs(mLastX - x);
			float deltaY = Math.abs(mLastY - y);
			float deltaZ = Math.abs(mLastZ - z);
			if (deltaX < NOISE) deltaX = (float)0.0;
			if (deltaY < NOISE) deltaY = (float)0.0;
			if (deltaZ < NOISE) deltaZ = (float)0.0;
			mLastX = x;
			mLastY = y;
			mLastZ = z;
			tvX.setText(Float.toString(deltaX));
			tvY.setText(Float.toString(deltaY));
			tvZ.setText(Float.toString(deltaZ));
			/*iv.setVisibility(View.VISIBLE);
			if (deltaX > deltaY) {
				iv.setImageResource(R.drawable.horizontal);
			} else if (deltaY > deltaX) {
				iv.setImageResource(R.drawable.vertical);
			} else {
				iv.setVisibility(View.INVISIBLE);
			}
			*/
			if (iClicks > 2500 & timeOff ){
				params = getWindow().getAttributes();
				params.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
				params.screenBrightness = 0;
				getWindow().setAttributes(params);

			}


			//turn on/off flashlight
			if(isFlash){
				if(!isOff && (iClicks >= 2000 &&(deltaX == 0.0 && deltaY == 0.0 && deltaZ == 0.0))){
					parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
					camera.setParameters(parameters);
					camera.stopPreview();
					isOff = true;

				}
				else if(isOff && iClicks > 2000&&(deltaX == 0.0 && deltaY == 0.0 && deltaZ == 0.0)){

				}
				else if(iClicks< 2000 && (deltaX == 0.0 && deltaY == 0.0 && deltaZ == 0.0)){

				}

				else {
					parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
					camera.setParameters(parameters);
					camera.startPreview();
					iClicks = 0;
					isOff = false;

				}
			}
			else {
				errorMsg = (TextView)findViewById(R.id.error);
				errorMsg.setText("Flashlight not found");

			}
		}


	}
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


		//launchTestService(); // background service start
        mInitialized = false;
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(this, mAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

		if (getApplicationContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)){

			camera = Camera.open();
			parameters = camera.getParameters();
			isFlash = true;
			//parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
		}


    }

	@Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

	@Override
    protected void onPause() {
        super.onPause();

		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
		if(camera!=null){
			//camera.stopPreview();
			//camera.setPreviewCallback(null);

			//camera.release();
			//camera = null;
		}

        mSensorManager.unregisterListener(this);


    }

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// can be safely ignored for this demo
	}

	@Override
	protected void onStop(){
		super.onStop();
		parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
	}

	@Override
	protected void onDestroy(){
		super.onDestroy();


	}
}