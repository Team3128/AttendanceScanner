package team3128.org.attendancescanner;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import team3128.org.attendancescanner.database.AttendanceDatabase;
import team3128.org.attendancescanner.database.StudentsCursorAdaptor;
import team3128.org.attendancescanner.database.Tables;


public class StudentNameActivity extends ListActivity
{

	private AttendanceDatabase attendanceDatabase;
	private StudentsCursorAdaptor adaptor;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		//create database cursor
	  attendanceDatabase = new AttendanceDatabase(this);
		Cursor cursor = attendanceDatabase.getAllStudents();

		adaptor = new StudentsCursorAdaptor(cursor, this);

		setListAdapter(adaptor);
	}

	private void updateList()
	{
		Cursor cursor = attendanceDatabase.getAllStudents();
		adaptor.changeCursor(cursor);
	}

	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		//get the cursor for the clicked position and use that to get the student ID
		Cursor cursor = ((Cursor)getListAdapter().getItem(position));
		int studentID = cursor.getInt(cursor.getColumnIndexOrThrow(Tables.Students.STUDENT_ID));
		showEditStudentDialog(studentID,
						cursor.getString(cursor.getColumnIndexOrThrow(Tables.Students.STUDENT_FIRST_NAME)),
						cursor.getString(cursor.getColumnIndexOrThrow(Tables.Students.STUDENT_LAST_NAME)));
	}

	private void showEditStudentDialog(final int studentID, String firstName, String lastName)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		// layout and inflater
		LayoutInflater inflater = getLayoutInflater();
		View content = inflater.inflate(R.layout.dialog_edit_student, null);
		builder.setView(content);

		final EditText firstNameText = (EditText) content.findViewById(R.id.firstNameText);
		final EditText lastNameText = (EditText) content.findViewById(R.id.lastNameText);

		builder.setTitle("Edit Student " + studentID);

		firstNameText.setText(firstName);
		lastNameText.setText(lastName);

		builder.setNeutralButton(R.string.remove, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{

				attendanceDatabase.removeStudent(studentID);
				updateList();
			}
		});

		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				//do nothing
			}
		});

		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String newFirstName = firstNameText.getText().toString();
				String newLastName = lastNameText.getText().toString();

				if(!(newFirstName.isEmpty() && newLastName.isEmpty()))
				{
					attendanceDatabase.updateStudent(studentID, firstNameText.getText().toString(), lastNameText.getText().toString());
					updateList();
				}
			}
		});

		builder.show();
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
