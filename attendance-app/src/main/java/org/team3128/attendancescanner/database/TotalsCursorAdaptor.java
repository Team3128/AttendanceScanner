package org.team3128.attendancescanner.database;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import org.team3128.attendancescanner.R;

/**
 * Cursor adaptor to put total attendance times into a ListView.
 *
 * Should be used with AttendanceDatabase.getStudentScanTimes()
 * Created by Jamie on 5/22/2015.
 */
public class TotalsCursorAdaptor extends CursorAdapter
{
	public TotalsCursorAdaptor(Cursor cursor, Context context)
	{
		super(context, cursor, 0);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent)
	{
		return LayoutInflater.from(context).inflate(R.layout.totals_row, parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		TextView nameView = (TextView) view.findViewById(R.id.nameTextView);
		TextView totalTimeView = (TextView) view.findViewById(R.id.totalTimeTextView);

		String firstName = cursor.getString(cursor.getColumnIndexOrThrow("firstName"));
		String lastName = cursor.getString(cursor.getColumnIndexOrThrow("lastName"));

		String fullName;
		if(firstName == null && lastName == null)
		{
			//if the person scanned does not have a name set, use their student ID instead
			fullName = Integer.toString(cursor.getInt(cursor.getColumnIndexOrThrow("studentID")));
		}
		else
		{
			fullName = firstName + " " + lastName;
		}

		nameView.setText(fullName);

		//set total times
		//the date formatting is kind of tricky because the Java (<8) time library can't print
		//hour values greater than 24, so there's no way to print, say, a day and a half as 36:00 (which is what we want).
		//so we need to do it ourselves
		long totalMilliseconds = cursor.getLong(cursor.getColumnIndexOrThrow("totalTime"));
		long seconds = totalMilliseconds / 1000;
		int minutes = (int)(seconds / 60);
		int hours = minutes / 60;
		minutes %= 60;

		totalTimeView.setText(String.format("%d:%02d", hours, minutes));
	}
}
