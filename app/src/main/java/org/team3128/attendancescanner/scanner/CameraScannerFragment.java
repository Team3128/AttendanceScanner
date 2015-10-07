package org.team3128.attendancescanner.scanner;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;

import org.team3128.attendancescanner.MainActivity;
import org.team3128.attendancescanner.R;
import org.team3128.attendancescanner.database.AttendanceDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import me.dm7.barcodescanner.zxing.ZXingScannerView;


public class CameraScannerFragment extends Fragment implements ZXingScannerView.ResultHandler
{
	private ZXingScannerView scannerView;

	private boolean isAutoFocusing = true;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);

		setHasOptionsMenu(true);

		scannerView = new ZXingScannerView(getActivity());
		scannerView.setResultHandler(this); // Register ourselves as a handler for scan results.


		//student ID's have code 39 barcodes
		ArrayList<BarcodeFormat> acceptableFormats = new ArrayList<BarcodeFormat>();
		acceptableFormats.add(BarcodeFormat.CODE_39);
		scannerView.setFormats(acceptableFormats);
		scannerView.setAutoFocus(true);

		return scannerView;
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
			toastMessage = ScannerActivity.processScan(((ScannerActivity)getActivity()).database, result.getText());
		}
		else
		{
			toastMessage = "Scan Cancelled";
		}

		Toast.makeText(getActivity(), toastMessage, Toast.LENGTH_LONG).show();

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				scannerView.startCamera();
				scannerView.setAutoFocus(isAutoFocusing);
			}
		}, 250);
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
		return super.onOptionsItemSelected(item);
	}



	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		inflater.inflate(R.menu.menu_camera_scanner, menu);
	}
}
