package team3128.org.attendancescanner;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.MenuItem;

import team3128.org.attendancescanner.database.AttendanceDatabase;
import team3128.org.attendancescanner.database.StudentsCursorAdaptor;


public class StudentNameActivity extends ListActivity
{

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		//create database cursor
		AttendanceDatabase attendanceDatabase = new AttendanceDatabase(this);
		Cursor cursor = attendanceDatabase.getAllStudents();

		StudentsCursorAdaptor adaptor = new StudentsCursorAdaptor(cursor, this);

		setListAdapter(adaptor);
	}



	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings)
		{
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
