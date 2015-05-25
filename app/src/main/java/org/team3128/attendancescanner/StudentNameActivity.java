package org.team3128.attendancescanner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import org.team3128.attendancescanner.database.AttendanceDatabase;
import org.team3128.attendancescanner.database.StudentsCursorAdaptor;
import org.team3128.attendancescanner.database.Tables;


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

		//getActionBar().show();
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_student_name, menu);
        return true;
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

	private void showAddStudentDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		// layout and inflater
		LayoutInflater inflater = getLayoutInflater();
		View content = inflater.inflate(R.layout.dialog_add_student, null);
		builder.setView(content);

        final EditText studentIDText = (EditText) content.findViewById(R.id.studentIDText);
        final EditText firstNameText = (EditText) content.findViewById(R.id.firstNameText);
		final EditText lastNameText = (EditText) content.findViewById(R.id.lastNameText);

		builder.setTitle(R.string.add_student);

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
				int studentID = Integer.parseInt(studentIDText.getText().toString());

				//if the person didn't enter a first or last name, keep those fields null so that
				//the student's student ID is displayed in the attendance view.
				//It doesn't make much sense that anyone would only enter the student id
				//in this view, but I'm trying to prepare for it if it happens.
				if(newFirstName.isEmpty() && newLastName.isEmpty())
				{
					attendanceDatabase.addStudent(studentID, null, null);
				}
				else
				{
					attendanceDatabase.addStudent(studentID, newFirstName, newLastName);
				}

				updateList();

      		}
		});

	  	final AlertDialog dialog = builder.create();

	  	//add a listener to enable the positive button if the student ID is valid
		studentIDText.addTextChangedListener(new TextWatcher()
		{
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after)
			{

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count)
			{

			}

			@Override
			public void afterTextChanged(Editable s)
			{
				boolean valid = false;
				try
				{
				  Integer.parseInt(s.toString());
				  //if(s.length() > 0)
				  //{
					valid = true;
				  //}
				}
				catch(NumberFormatException e)
				{
				  //not valid
				}

				dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(valid);

			}
		});

	  	dialog.show();
	  	dialog.getButton(Dialog.BUTTON_POSITIVE).setEnabled(false);

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
								AlertDialog.Builder confirmationBuilder = new AlertDialog.Builder(StudentNameActivity.this);
	              confirmationBuilder.setTitle(R.string.remove_student_title);
	              confirmationBuilder.setMessage(R.string.remove_student_message);
	              confirmationBuilder.setNegativeButton(android.R.string.cancel, null);
	              confirmationBuilder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
	              {
		              @Override
		              public void onClick(DialogInterface dialog, int which)
		              {
			              attendanceDatabase.removeStudent(studentID);
			              updateList();
		              }
	              });
	            confirmationBuilder.show();
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
                    attendanceDatabase.updateStudent(studentID, newFirstName, newLastName);
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
		if (id == R.id.action_add_student)
		{
            showAddStudentDialog();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
