package org.team3128.attendancescanner;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.team3128.attendancescanner.database.AttendanceDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;


public class AutoScanActivity extends Activity
{
	public AttendanceDatabase database;

	private ArrayList<String> codeTypes;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		database = new AttendanceDatabase(this);
		codeTypes = new ArrayList<String>();

		//student ID's have code 39 barcodes
		codeTypes.add("CODE_39");
		invokeScan();
	}


	private void invokeScan()
	{
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.addExtra("SCAN_WIDTH", 800);
		integrator.addExtra("SCAN_HEIGHT", 300);
		integrator.addExtra("RESULT_DISPLAY_DURATION_MS", 0);
		integrator.addExtra("PROMPT_MESSAGE", "Scan Student ID");
		integrator.initiateScan(codeTypes);
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
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{

		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);

		String toastMessage;

		if (result != null)
		{
			toastMessage = processScan(database, result.getContents());
		}
		else
		{
			toastMessage = "Scan Cancelled";
		}

		Toast.makeText(this, toastMessage, Toast.LENGTH_LONG).show();

		invokeScan();
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}
}
