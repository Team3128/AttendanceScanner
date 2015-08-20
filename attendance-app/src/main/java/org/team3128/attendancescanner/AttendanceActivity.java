package org.team3128.attendancescanner;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.ListView;

import org.team3128.attendancescanner.database.AttendanceCursorAdaptor;
import org.team3128.attendancescanner.database.AttendanceDatabase;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Activity for viewing the attendance of club members.
 */
public class AttendanceActivity extends Activity implements CalendarView.OnDateChangeListener
{
	ListView attendanceList;
	Button selectDateButton;

	DateFormat buttonDateFormat;

	// date to be shown
	private Calendar date;

	private AttendanceDatabase attendanceDatabase;

	private AttendanceCursorAdaptor attendanceCursorAdaptor;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_attendance);

		attendanceList = (ListView) findViewById(R.id.attendanceList);
		selectDateButton = (Button) findViewById(R.id.selectDateButton);

		attendanceDatabase = new AttendanceDatabase(this);

		//if the date was not already set, set it to the date of the most recent scan.
		if (savedInstanceState != null)
		{
			Log.i("AttendanceActivity", "Loading saved filter.");
			date.setTimeInMillis(savedInstanceState.getLong("date"));

		}
		else
		{
			date = attendanceDatabase.getMostRecentScanTime();
		}

		buttonDateFormat = DateFormat.getDateInstance();

		setDateFilter();
	}

	@Override
	protected void onSaveInstanceState(Bundle state)
	{
		state.putDouble("date", date.getTimeInMillis());
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_attendance, menu);
		return true;
	}


	public void selectDateCallback(View view)
	{
		DatePickerDialog datePickerDialog = new DatePickerDialog(this, DatePickerDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener()
		{
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
			{
				date.set(year, monthOfYear, dayOfMonth);

				setDateFilter();
			}
		}, date.get(Calendar.DAY_OF_MONTH), date.get(Calendar.MONTH), date.get(Calendar.YEAR));
		datePickerDialog.getDatePicker().setCalendarViewShown(true);
		datePickerDialog.getDatePicker().setSpinnersShown(false);
		datePickerDialog.show();
	}

	/**
	 * Set the currently shown date to the one in the class variables.  Also changes the text of the button.
	 */
	private void setDateFilter()
	{
		Cursor cursor = attendanceDatabase.getStudentScanTimes(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
		if(attendanceCursorAdaptor == null)
		{
			attendanceCursorAdaptor = new AttendanceCursorAdaptor(cursor, this);
			attendanceList.setAdapter(attendanceCursorAdaptor);
		}
		else
		{
			attendanceCursorAdaptor.changeCursor(cursor);
		}
		selectDateButton.setText(buttonDateFormat.format(date.getTime()));

	}

	@Override
	public void onSelectedDayChange(CalendarView view, int year, int monthOfYear, int dayOfMonth)
	{
		date.set(year, monthOfYear, dayOfMonth);
		setDateFilter();
	}
}
