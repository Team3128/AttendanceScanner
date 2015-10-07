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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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


public class USBScannerFragment extends Fragment
{
	//used to buffer barcodes as they're typed in
	private StringBuilder numberCollector = new StringBuilder();

	private TextView scanOutputText;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);

		View layout = inflater.inflate(R.layout.fragment_usb_scanner, container, false);

		scanOutputText = (TextView) layout.findViewById(R.id.USBScanOutputText);

		return layout;
	}

	public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
	{
		char pressed = (char)event.getUnicodeChar();

		//sent after a barcode has been typed in by the scanner
		if(keyCode == KeyEvent.KEYCODE_SPACE)
		{
			scanOutputText.setText(ScannerActivity.processScan(((ScannerActivity)getActivity()).database, numberCollector.toString()));
			numberCollector.setLength(0); //reset string builder
		}
		else if(Character.isDigit(pressed))
		{
			numberCollector.append(pressed);
		}
		else
		{
			return false;
		}

		return true;
	}
}
