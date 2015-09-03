package org.team3128.attendancescanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import org.team3128.attendancescanner.database.AttendanceDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class AutoScanActivity extends Activity implements ZXingScannerView.ResultHandler
{
	public AttendanceDatabase database;

	private ZXingScannerView scannerView;

	private boolean isAutoFocusing = true;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scanner);

		scannerView = (ZXingScannerView) findViewById(R.id.scannerView);

		database = new AttendanceDatabase(this);

		scannerView.setResultHandler(this); // Register ourselves as a handler for scan results.


		//student ID's have code 39 barcodes
		ArrayList<BarcodeFormat> acceptableFormats = new ArrayList<BarcodeFormat>();
		acceptableFormats.add(BarcodeFormat.CODE_39);
		scannerView.setFormats(acceptableFormats);
		scannerView.setAutoFocus(true);


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
	public void onResume() {
		super.onResume();
		scannerView.startCamera();          // Start camera on resume
	}

	@Override
	public void onPause() {
		super.onPause();
		scannerView.stopCamera();           // Stop camera on pause
	}

	@Override
	public void handleResult(Result result)
	{
		String toastMessage;

		if (result != null)
		{
			toastMessage = processScan(database, result.getText());
		}
		else
		{
			toastMessage = "Scan Cancelled";
		}

		Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				scannerView.startCamera();
				scannerView.setAutoFocus(isAutoFocusing);
			}
		}, 500);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(item.getItemId() == R.id.action_toggle_autofocus)
		{
			if(isAutoFocusing)
			{
				isAutoFocusing = false;
				item.setTitle(R.string.enable_autofocus);

				scannerView.setAutoFocus(isAutoFocusing);
			}
			else
			{
				isAutoFocusing = true;
				item.setTitle(R.string.disable_autofocus);

				scannerView.setAutoFocus(isAutoFocusing);
			}

			return true;
		}
		else if(item.getItemId() == R.id.action_admin_interface)
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
}
