package team3128.org.attendancescanner.database;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

import team3128.org.attendancescanner.R;

/**
 * Cursor adaptor to put attendance data into a ListView.
 *
 * Should be used with AttendanceDatabase.getStudentScanTimes()
 * Created by Jamie on 5/22/2015.
 */
public class AttendanceCursorAdaptor extends CursorAdapter
{
	public AttendanceCursorAdaptor(Cursor cursor, Context context)
	{
		super(context, cursor, 0);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent)
	{
		return LayoutInflater.from(context).inflate(R.layout.attendance_row, parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		TextView nameView = (TextView) view.findViewById(R.id.nameTextView);
		TextView timeInView = (TextView) view.findViewById(R.id.timeInTextView);
		TextView timeOutView = (TextView) view.findViewById(R.id.timeOutTextView);

		String firstName = cursor.getString(cursor.getColumnIndexOrThrow(Tables.Students.STUDENT_FIRST_NAME));
		String lastName = cursor.getString(cursor.getColumnIndexOrThrow(Tables.Students.STUDENT_LAST_NAME));

		String fullName;
		if(firstName == null && lastName == null)
		{
			//if the person scanned does not have a name set, use their student ID instead
			fullName = Integer.toString(cursor.getInt(cursor.getColumnIndexOrThrow(Tables.Students.STUDENT_ID)));
		}
		else
		{
			fullName = firstName + " " + lastName;
		}

		nameView.setText(fullName);

		SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm");

		int timeInIndex = cursor.getColumnIndexOrThrow("timeIn");
		if(!cursor.isNull(timeInIndex))
		{
			timeInView.setText(dateFormat.format(new Date(cursor.getLong(timeInIndex))));
		}

		int timeOutIndex = cursor.getColumnIndexOrThrow("timeOut");
		if(!cursor.isNull(timeOutIndex))
		{
			timeOutView.setText(dateFormat.format(new Date(cursor.getLong(timeOutIndex))));
		}

	}
}