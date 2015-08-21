package org.team3128.attendancescanner.database;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.team3128.attendancescanner.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

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

		String firstName = cursor.getString(cursor.getColumnIndexOrThrow("firstName"));
		String lastName = cursor.getString(cursor.getColumnIndexOrThrow("lastName"));

		String fullName;
		if(firstName.isEmpty() && lastName.isEmpty())
		{
			//if the person scanned does not have a name set, use their student ID instead
			fullName = Integer.toString(cursor.getInt(cursor.getColumnIndexOrThrow("studentID")));
		}
		else
		{
			fullName = firstName + " " + lastName;
		}

		nameView.setText(fullName);

		//set in and out times

		SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.US);

		int timeInIndex = cursor.getColumnIndexOrThrow("inTime");
		int timeOutIndex = cursor.getColumnIndexOrThrow("outTime");

		long timeInStamp = cursor.getLong(timeInIndex);
		String timeInString = dateFormat.format(new Date(timeInStamp));

		String timeOutString = "";

		if(!cursor.isNull(timeOutIndex))
		{
			long timeOutStamp = cursor.getLong(timeOutIndex);
			timeOutString = dateFormat.format(new Date(timeOutStamp));
		}

		timeInView.setText(timeInString);
		timeOutView.setText(timeOutString);
	}
}
