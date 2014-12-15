package se.kth.shaketest;

import java.io.PrintWriter;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
	private final float shakeThreshold = 0.75f;
	ImageView flowerView;
	int[] flowers;
	int currentFlower;
	int shakeCounter = 0;
	boolean fallHasCome = false;

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

		flowers = new int[] { R.drawable.flower0, R.drawable.flower10,
				R.drawable.flower20, R.drawable.flower30, R.drawable.flower40,
				R.drawable.flower50 };
		currentFlower = 0;
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

		// ? Set a timer/check file size to prevent file from draining memory

		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		Sensor accelerometer = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensorManager.registerListener(sensorEventListener, accelerometer,
				sensorFrequency);
		// Alt: SensorManager.SENSOR_DELAY_FASTEST, SENSOR_DELAY_NORMAL,
		// SENSOR_DELAY_UI

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

		if (writer != null) {
			try {
				writer.close();
				Log.i("stopListening()", "writer closed");
			} catch (Exception e) {
				Log.e("onPause", e.toString());
				System.err.println("Error closing writer");
				e.printStackTrace(System.err);
			}
		}
	}

	private void updateUI() {
		if (fallHasCome) {
			return;
		}
		if (isAccelerationChanged(last_x, last_y, last_z, x, y, z)) {
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

		if (currentFlower != (int) x2) {
			currentFlower = (int) x2;
			switch ((int) x) {
			case 0:
				flowerView
						.setBackground(getResources().getDrawable(flowers[0]));
				flowerView.invalidate();
				break;
			case 1:
				flowerView
						.setBackground(getResources().getDrawable(flowers[1]));
				flowerView.invalidate();
				break;
			case 2:
				flowerView
						.setBackground(getResources().getDrawable(flowers[2]));
				flowerView.invalidate();
				break;
			case 3:
				flowerView
						.setBackground(getResources().getDrawable(flowers[3]));
				flowerView.invalidate();
				break;
			case 4:
				flowerView
						.setBackground(getResources().getDrawable(flowers[4]));
				flowerView.invalidate();
				break;
			case 5:
				flowerView
						.setBackground(getResources().getDrawable(flowers[5]));
				flowerView.invalidate();
				break;
			default:
				if (x > 5) {
					flowerView.setBackground(getResources().getDrawable(
							flowers[5]));
					flowerView.invalidate();
				} else {
					flowerView.setBackground(getResources().getDrawable(
							flowers[0]));
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

	private SensorManager sensorManager;
	private boolean firstMeasure = true;
	private double x2;
	private double x, y, z;
	private double last_x, last_y, last_z;
	private Timer updateTimer = null;
	private PrintWriter writer;

	// private boolean startDelay;
	private int sensorFrequency;

	private final SensorEventListener sensorEventListener = new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			last_x = x;
			last_y = y;
			last_z = z;
			if (firstMeasure) {
				x = event.values[0];
				y = event.values[1];
				z = event.values[2];
				x2 = x;
				firstMeasure = false;
			} else {
				x = (event.values[0] * 0.9) + (x * 0.1);
				y = (event.values[1] * 0.9) + (y * 0.1);
				z = (event.values[2] * 0.9) + (z * 0.1);

				x2 = (event.values[0] * 0.1) + (x * 0.9);

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

	private void showToast(CharSequence msg) {
		Toast toast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
		toast.show();
	}

}