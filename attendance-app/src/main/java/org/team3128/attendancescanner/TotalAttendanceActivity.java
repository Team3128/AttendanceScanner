package org.team3128.attendancescanner;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;

import org.team3128.attendancescanner.database.AttendanceDatabase;
import org.team3128.attendancescanner.database.TotalsCursorAdaptor;

import java.util.Calendar;

/**
 * Activity for viewing the attendance of club members.
 */
public class TotalAttendanceActivity extends Activity
{
	ListView attendanceList;

	Button selectStartDateButton;
	Button selectEndDateButton;


	// date to be shown
	private int startYear, startMonth, startDay;
	private int endYear, endMonth, endDay;

	private AttendanceDatabase attendanceDatabase;

	private TotalsCursorAdaptor attendanceCursorAdaptor;

	private DatePicker lastStartDatePicker;
	private DatePicker lastEndDatePicker;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_totals);

		attendanceList = (ListView) findViewById(R.id.attendanceList);
		selectStartDateButton = (Button) findViewById(R.id.selectStartDateButton);
		selectEndDateButton = (Button) findViewById(R.id.selectEndDateButton);

		attendanceDatabase = new AttendanceDatabase(this);

		//if the date was not already set, set it to the date of the most recent scan.
		if (savedInstanceState != null)
		{
			Log.i("AttendanceActivity", "Loading saved filter.");
			startYear = savedInstanceState.getInt("startYear");
			startMonth = savedInstanceState.getInt("startMonth");
			startDay = savedInstanceState.getInt("startDay");

			endYear = savedInstanceState.getInt("endYear");
			endMonth = savedInstanceState.getInt("endMonth");
			endDay = savedInstanceState.getInt("endDay");
		}
		else
		{
			Calendar mostRecentScan = attendanceDatabase.getMostRecentScanTime();
			endYear = mostRecentScan.get(Calendar.YEAR);
			endMonth = mostRecentScan.get(Calendar.MONTH);
			endDay = mostRecentScan.get(Calendar.DAY_OF_MONTH);

			startYear = 1970;
			startMonth = 0;
			startDay = 1;
		}

		setDateFilter();


	}

	@Override
	protected void onSaveInstanceState(Bundle state)
	{
		state.putInt("startYear", startYear);
		state.putInt("startMonth", startMonth);
		state.putInt("startDay", startDay);
		
		state.putInt("endYear", endYear);
		state.putInt("endMonth", endMonth);
		state.putInt("endDay", endDay);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_attendance, menu);
		return true;
	}


	public void selectStartDateCallback(View view)
	{
		DatePickerDialog datePickerDialog = new DatePickerDialog(this, DatePickerDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener()
		{
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
			{
				startYear = year;
				startMonth = monthOfYear;
				startDay = dayOfMonth;

				setDateFilter();
			}
		}, startDay, startMonth, startYear);
        datePickerDialog.getDatePicker().setCalendarViewShown(true);
		datePickerDialog.getDatePicker().setSpinnersShown(false);
		datePickerDialog.show();
	}

	public void selectEndDateCallback(View view)
	{
		DatePickerDialog datePickerDialog = new DatePickerDialog(this, DatePickerDialog.THEME_HOLO_LIGHT, new DatePickerDialog.OnDateSetListener()
		{
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
			{
				endYear = year;
				endMonth = monthOfYear ;
				endDay = dayOfMonth;

				setDateFilter();
			}
		}, endDay, endMonth, endYear);
		datePickerDialog.getDatePicker().setCalendarViewShown(true);
		datePickerDialog.getDatePicker().setSpinnersShown(false);
		datePickerDialog.show();
	}

	/**
	 * Set the currently shown date to the one in the class variables.  Also changes the text of the button.
	 */
	private void setDateFilter()
	{
		Cursor cursor = attendanceDatabase.getStudentTotalAttendanceTimes(startYear, startMonth, startDay, endYear, endMonth, endDay);
		if(attendanceCursorAdaptor == null)
		{
			attendanceCursorAdaptor = new TotalsCursorAdaptor(cursor, this);
			attendanceList.setAdapter(attendanceCursorAdaptor);
		}
		else
		{
			attendanceCursorAdaptor.changeCursor(cursor);
		}

		selectStartDateButton.setText(String.format("%d/%d/%d", startMonth + 1, startDay, startYear));
		selectEndDateButton.setText(String.format("%d/%d/%d", endMonth + 1, endDay, endYear));

	}
}
