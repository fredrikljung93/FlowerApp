package se.kth.shaketest;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.widget.ImageView;

public class MainActivity extends Activity {
	private final float shakeThreshold = 0.75f;
	private ImageView flowerView;
	private Drawable[] flowerDrawables;
	private int currentFlower;
	private int shakeCounter = 0;
	private boolean fallHasCome = false;
	private SensorManager sensorManager;
	private boolean firstMeasure = true;
	private double lowpass_x;
	private double highpass_x, highpass_y, highpass_z;
	private double last_highpass_x, last_highpass_y, last_highpass_z;
	private Timer updateTimer = null;
	private int sensorFrequency;

	Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			updateUI();
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		flowerView = (ImageView) findViewById(R.id.imageView1);

		currentFlower = 0;

		flowerDrawables = new Drawable[] {
				getResources().getDrawable(R.drawable.flower0),
				getResources().getDrawable(R.drawable.flower10),
				getResources().getDrawable(R.drawable.flower20),
				getResources().getDrawable(R.drawable.flower30),
				getResources().getDrawable(R.drawable.flower40),
				getResources().getDrawable(R.drawable.flower50) };

	}

	public void onPause() {
		super.onPause();
		stopListening();
	}

	public void onResume() {
		super.onResume();
		startListening();
	}

	private void startListening() {
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		Sensor accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(sensorEventListener, accelerometer,
				sensorFrequency);

		updateTimer = new Timer("updateUI");
		TimerTask updateUITask = new TimerTask() {
			@Override
			public void run() {
				handler.sendMessage(new Message());
			}
		};
		updateTimer.scheduleAtFixedRate(updateUITask, 0, 200);
	}

	private void stopListening() {
		if (sensorManager != null) {
			sensorManager.unregisterListener(sensorEventListener);
			updateTimer.cancel();
		}
	}

	private void updateUI() {
		if (fallHasCome) {
			return;
		}
		if (isAccelerationChanged(last_highpass_x, last_highpass_y,
				last_highpass_z, highpass_x, highpass_y, highpass_z)) {
			shakeCounter++;
			if (shakeCounter == 4) {
				vibrate();
				flowerView.setBackgroundResource(R.anim.fallanimation);
				AnimationDrawable frameAnimation = (AnimationDrawable) flowerView
						.getBackground();
				frameAnimation.start();
				shakeCounter = 0;
				fallHasCome = true;
				return;
			}
		} else {
			shakeCounter = 0;
		}
		if (currentFlower != (int) lowpass_x) {
			currentFlower = (int) lowpass_x;
			switch ((int) highpass_x) {
			case 0:
				flowerView.setBackground(flowerDrawables[0]);
				flowerView.invalidate();
				break;
			case 1:
				flowerView.setBackground(flowerDrawables[1]);
				flowerView.invalidate();
				break;
			case 2:
				flowerView.setBackground(flowerDrawables[2]);
				flowerView.invalidate();
				break;
			case 3:
				flowerView.setBackground(flowerDrawables[3]);
				flowerView.invalidate();
				break;
			case 4:
				flowerView.setBackground(flowerDrawables[4]);
				flowerView.invalidate();
				break;
			case 5:
				flowerView.setBackground(flowerDrawables[5]);
				flowerView.invalidate();
				break;
			default:
				if (highpass_x > 5) {
					flowerView.setBackground(flowerDrawables[5]);
					flowerView.invalidate();
				} else {
					flowerView.setBackground(flowerDrawables[0]);
					flowerView.invalidate();
				}

			}
		}
	}

	private void vibrate() {
		Vibrator v = (Vibrator) getApplicationContext().getSystemService(
				Context.VIBRATOR_SERVICE);
		v.vibrate(100);
	}

	private final SensorEventListener sensorEventListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			last_highpass_x = highpass_x;
			last_highpass_y = highpass_y;
			last_highpass_z = highpass_z;
			if (firstMeasure) {
				highpass_x = event.values[0];
				highpass_y = event.values[1];
				highpass_z = event.values[2];
				lowpass_x = highpass_x;
				firstMeasure = false;
			} else {
				highpass_x = (event.values[0] * 0.9) + (highpass_x * 0.1);
				highpass_y = (event.values[1] * 0.9) + (highpass_y * 0.1);
				highpass_z = (event.values[2] * 0.9) + (highpass_z * 0.1);
				lowpass_x = (event.values[0] * 0.1) + (highpass_x * 0.9);

			}
		}
	};

	/*
	 * Method based on method from literature "Android cookbook"
	 * 
	 * If the values of acceleration have changed on at least two axises, we are
	 * probably in a shake motion
	 */
	private boolean isAccelerationChanged(double xPreviousAccel,
			double yPreviousAccel, double zPreviousAccel, double xAccel,
			double yAccel, double zAccel) {
		double deltaX = Math.abs(xPreviousAccel - xAccel);
		double deltaY = Math.abs(yPreviousAccel - yAccel);
		double deltaZ = Math.abs(zPreviousAccel - zAccel);
		return (deltaX > shakeThreshold && deltaY > shakeThreshold)
				|| (deltaX > shakeThreshold && deltaZ > shakeThreshold)
				|| (deltaY > shakeThreshold && deltaZ > shakeThreshold);
	}

}