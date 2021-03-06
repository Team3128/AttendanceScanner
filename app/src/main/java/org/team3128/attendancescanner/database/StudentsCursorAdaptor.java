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
 * Created by Jamie on 5/22/2015.
 */
public class StudentsCursorAdaptor extends CursorAdapter
{
	public StudentsCursorAdaptor(Cursor cursor, Context context)
	{
		super(context, cursor, 0);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent)
	{
		return LayoutInflater.from(context).inflate(R.layout.students_menu_row, parent, false);
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor)
	{
		TextView studentIDView = (TextView) view.findViewById(R.id.studentIDTextView);
		TextView firstNameView = (TextView) view.findViewById(R.id.firstNameTextView);
		TextView lastNameView = (TextView) view.findViewById(R.id.lastNameTextView);

		studentIDView.setText(Integer.toString(cursor.getInt(cursor.getColumnIndexOrThrow("studentID"))));
		firstNameView.setText(cursor.getString(cursor.getColumnIndexOrThrow("firstName")));
		lastNameView.setText(cursor.getString(cursor.getColumnIndexOrThrow("lastName")));
	}
}
