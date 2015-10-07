package org.team3128.attendancescanner.scanner;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.team3128.attendancescanner.MainActivity;
import org.team3128.attendancescanner.R;
import org.team3128.attendancescanner.database.AttendanceDatabase;

import java.util.Calendar;
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
	 * @return the result message which should be shown to the user
	 */
	public static String processScan(AttendanceDatabase database, String barcodeNumber)
	{

		String result = null;

		if(barcodeNumber != null)
		{
			try
			{
				int id = Integer.parseInt(barcodeNumber);
				String name = database.getStudentName(id);

				//if the last scan in was before this time, we will assume that they forgot to scan out and start a new scan
				Calendar recentScanCutoff = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
				recentScanCutoff.add(Calendar.HOUR, -12);

				Long scanInRowID = database.getMostRecentScanIn(id, recentScanCutoff.getTime());

				result = "Scanned " + (name != null ? name : id) + (scanInRowID != null ? " out.\n" : " in.\n");

				if(scanInRowID != null)
				{
					database.addScanOut(scanInRowID);
				}
				else
				{
					database.addScanIn(id);
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
		if(!currentFragmentIsUSB)
		{
			swapInUSBFragment();
		}
		return usbScannerFragment.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == R.id.action_admin_interface)
		{
			startActivity(new Intent(this, MainActivity.class));
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
