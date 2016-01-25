package org.team3128.attendancescanner.scanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import org.team3128.attendancescanner.AdminActivity;
import org.team3128.attendancescanner.Pair;
import org.team3128.attendancescanner.R;
import org.team3128.attendancescanner.TotalAttendanceActivity;
import org.team3128.attendancescanner.database.AttendanceDatabase;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class ScannerActivity extends Activity
{
	AttendanceDatabase database;

	//kept as class variable because users can press back to get out of the USB scanner mode
	CameraScannerFragment cameraScannerFragment;

	USBScannerFragment usbScannerFragment;

	boolean currentFragmentIsUSB = false;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scanner);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		database = new AttendanceDatabase(this);

		cameraScannerFragment = new CameraScannerFragment();
		usbScannerFragment = new USBScannerFragment();

		if(savedInstanceState == null)
		{
			getFragmentManager().beginTransaction().add(R.id.scanner_fragment_container, cameraScannerFragment).commit();
		}
	}

	/**
	 * Processes a scanned ID number.
	 *
	 * It is public and static so that it can be used by other activities
	 * @param barcodeNumber the string form of the input student ID
	 * @param scanTime the time that the student scanned in or out.  If null, the current time will be used.
	 * @return the result message which should be shown to the user
	 */
	public static String processScan(AttendanceDatabase database, String barcodeNumber, Date scanTime)
	{

		String result = null;

		if(scanTime == null)
		{
			//get the current UTC time
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			scanTime = cal.getTime();
		}


		if(barcodeNumber != null)
		{
			try
			{
				int id = Integer.parseInt(barcodeNumber);
				String name = database.getStudentName(id);

				//if the last scan in was before this time, we will assume that they forgot to scan out and start a new scan
				Calendar recentScanCutoff = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				recentScanCutoff.add(Calendar.HOUR, -12);

				Pair<Long, Date> recentScanData = database.getMostRecentScanIn(id, recentScanCutoff.getTime());

				result = "Scanned " + (name != null ? name : id) + (recentScanData != null ? " out.\n" : " in.\n");

				if(recentScanData != null)
				{
					//make sure the user hasn't backdated the out scan to be earlier than the in scan
					if(recentScanData.right.compareTo(scanTime) > 0)
					{
						return "Scan out time is earlier than the in time (" + DateFormat.getTimeInstance().format(recentScanData.right) + ")! Are you a time traveler, or did you backdate wrong?";
					}
					else
					{
						database.addScanOut(recentScanData.left, scanTime);
					}
				}
				else
				{
					database.addScanIn(id, scanTime);
				}
			}
			catch(NumberFormatException ex)
			{
				result = "Invalid student ID!";
			}
		}
		else
		{
			result = "Scan Error";
		}

		return result;
	}

	@Override
	public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
	{

		if((keyCode != KeyEvent.KEYCODE_MENU && keyCode != KeyEvent.KEYCODE_BACK) && (keyCode != KeyEvent.KEYCODE_SEARCH && keyCode != KeyEvent.KEYCODE_APP_SWITCH))
		{
			if (!currentFragmentIsUSB)
			{
				swapInUSBFragment();
			}
			return usbScannerFragment.onKeyDown(keyCode, event);
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == R.id.action_admin_interface)
		{
			startActivity(new Intent(this, AdminActivity.class));
		}
		else if(item.getItemId() == R.id.action_view_totals)
		{
			startActivity(new Intent(this, TotalAttendanceActivity.class));
		}
		return super.onOptionsItemSelected(item);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_scanner, menu);
		return true;
	}

	void swapInCameraFragment()
	{
		getFragmentManager().beginTransaction().replace(R.id.scanner_fragment_container, cameraScannerFragment).commit();
		currentFragmentIsUSB = false;
	}

	void swapInUSBFragment()
	{
		getFragmentManager().beginTransaction().replace(R.id.scanner_fragment_container, usbScannerFragment).addToBackStack(null).commit();

		currentFragmentIsUSB = true;
	}
}
