package team3128.org.attendancescanner;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;

import java.util.Calendar;

import team3128.org.attendancescanner.database.AttendanceCursorAdaptor;
import team3128.org.attendancescanner.database.AttendanceDatabase;

/**
 * Activity for viewing the attendance of club members.
 */
public class AttendanceActivity extends ActionBarActivity implements DatePickerDialog.OnDateSetListener
{
	ListView attendanceList;
	Button selectDateButton;

	// date to be shown
	private int year, month, day;

	private AttendanceDatabase attendanceDatabase;

	AttendanceCursorAdaptor attendanceCursorAdaptor;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_attendance);

		attendanceList = (ListView) findViewById(R.id.attendanceList);
		selectDateButton = (Button) findViewById(R.id.selectDateButton);

		attendanceDatabase = new AttendanceDatabase(this);

		//if the date was not already set, set it to the date of the most recent scan.
		if(savedInstanceState != null)
		{
			Log.i("AttendanceActivity", "Loading saved filter.");
			year = savedInstanceState.getInt("year");
			month = savedInstanceState.getInt("month");
			day = savedInstanceState.getInt("day");

		}
		else
		{
			Calendar mostRecentScan = attendanceDatabase.getMostRecentScanTime();
			year = mostRecentScan.get(Calendar.YEAR);
			month = mostRecentScan.get(Calendar.MONTH);
			day = mostRecentScan.get(Calendar.DAY_OF_MONTH);
		}

		setDateFilter();
	}

	@Override
	protected void onSaveInstanceState(Bundle state)
	{
		state.putInt("year", year);
		state.putInt("month", month);
		state.putInt("day", day);
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
		DatePickerDialog datePickerDialog = new DatePickerDialog(this, DatePickerDialog.THEME_HOLO_LIGHT, this, day, month, year);
        datePickerDialog.getDatePicker().setCalendarViewShown(true);
		datePickerDialog.show();
	}

	@Override
	/**
	 * Callback for the DatePickerDialog.
	 */
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth)
	{
		this.year = year;
		this.month = monthOfYear;
		this.day = dayOfMonth;

		setDateFilter();
	}

	/**
	 * Set the currently shown date to the one in the class variables.  Also changes the text of the button.
	 */
	private void setDateFilter()
	{
		Cursor cursor = attendanceDatabase.getStudentScanTimes(year, month, day);
		if(attendanceCursorAdaptor == null)
		{
			attendanceCursorAdaptor = new AttendanceCursorAdaptor(cursor, this);
			attendanceList.setAdapter(attendanceCursorAdaptor);
		}
		else
		{
			attendanceCursorAdaptor.changeCursor(cursor);
		}

		selectDateButton.setText(String.format("%d/%d/%d", month, day, year));

	}
}
