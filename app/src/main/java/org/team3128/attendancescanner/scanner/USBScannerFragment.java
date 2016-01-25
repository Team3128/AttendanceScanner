package org.team3128.attendancescanner.scanner;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.team3128.attendancescanner.R;


public class USBScannerFragment extends Fragment
{
	//used to buffer barcodes as they're typed in
	private StringBuilder numberCollector = new StringBuilder();

	private TextView scanOutputText;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);

		setHasOptionsMenu(true);

		View layout = inflater.inflate(R.layout.fragment_usb_scanner, container, false);

		scanOutputText = (TextView) layout.findViewById(R.id.USBScanOutputText);

		return layout;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);

		inflater.inflate(R.menu.menu_usb_scanner, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{

		if(item.getItemId() == R.id.action_disable_usb_scanner)
		{
			((ScannerActivity)getActivity()).swapInCameraFragment();
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean onKeyDown(int keyCode, @NonNull KeyEvent event)
	{
		char pressed = (char)event.getUnicodeChar();

		//sent after a barcode has been typed in by the scanner
		if(keyCode == KeyEvent.KEYCODE_SPACE)
		{
			scanOutputText.setText(ScannerActivity.processScan(((ScannerActivity)getActivity()).database, numberCollector.toString(), null));
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


