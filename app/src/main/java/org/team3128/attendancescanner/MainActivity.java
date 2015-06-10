package org.team3128.attendancescanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.team3128.attendancescanner.database.AttendanceDatabase;
import org.team3128.attendancescanner.database.AttendanceOpenHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;


public class MainActivity extends Activity
{

	private final static int FILE_PICKER_REQUEST_CODE = 1;

	AttendanceDatabase database;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		database = new AttendanceDatabase(this);

		//close the activity unless the user enters the password
		// password disabled for ease of testing
//		PasswordDialog.show(this, getLayoutInflater(), new Runnable()
//		{
//			@Override
//			public void run()
//			{
//				finish();
//			}
//		});
	}

	@Override
	protected void onStart()
	{
		super.onStart();

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		if(requestCode == FILE_PICKER_REQUEST_CODE)
		{

			//check if the user pressed cancel
			if(intent != null)
			{
				//get the file that was returned
				Uri fileUri = intent.getData();
				File file = new File(fileUri.getPath());
				Log.v("MainActivity", "Loading new database " + file.getPath());
				Log.v("MainActivity", "File exists: " + file.exists());
				File database = new File(getApplicationInfo().dataDir + "/databases/" + AttendanceOpenHelper.DATABASE_NAME);
				try
				{
					copyFiles(file, database);
					Toast.makeText(this, "Database imported.", Toast.LENGTH_SHORT).show();
				}
				catch (IOException e)
				{
					Toast.makeText(this, "Error importing database.  Maybe look at the logcat?", Toast.LENGTH_SHORT).show();
					e.printStackTrace();
				}
			}

		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}

	/**
	 * Android is missing the Java file copy function.
	 * I found this one on StackOverflow.
	 * @param source
	 * @param destination
	 */
	private void copyFiles(File source, File destination) throws IOException
	{
		FileChannel src = new FileInputStream(source).getChannel();
		FileChannel dst = new FileOutputStream(destination).getChannel();
		dst.transferFrom(src, 0, src.size());
		src.close();
		dst.close();
	}

	public void backupDatabase(View view)
	{
		File backupLocation = new File(Environment.getExternalStorageDirectory(), AttendanceOpenHelper.DATABASE_NAME);
		File database = new File(getApplicationInfo().dataDir + "/databases/" + AttendanceOpenHelper.DATABASE_NAME);

		Log.d("MainActivity", "Attempting to copy " + database.getPath() + " to " + backupLocation.getPath());
		if(database.exists() && backupLocation.canWrite())
		{
			try
			{
				copyFiles(database, backupLocation);
				Toast.makeText(this, "Backed up attendance database to " + backupLocation.getPath(), Toast.LENGTH_LONG).show();
			}
			catch (IOException e)
			{
				Log.e("MainActivity", "Error backing up database");
				e.printStackTrace();
			}

		}
		else
		{
			Log.e("MainActivity", "Could not back up database: database not found or can't write to output file.");
		}
	}

	public void viewAttendance(View view)
	{
		startActivity(new Intent(this, AttendanceActivity.class));
	}

	public void viewTotalTimes(View view)
	{
		startActivity(new Intent(this, TotalAttendanceActivity.class));
	}

	public void editStudentNames(View view)
	{
		startActivity(new Intent(this, StudentNameActivity.class));
	}

	public void importDatabase(View view)
	{
		backupDatabase(null);
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("file/*");
		intent.putExtra("com.estrongs.intent.extra.TITLE", "Import Database");

		startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
	}

	public void manualInput(View view)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// layout and inflater
		View content = getLayoutInflater().inflate(R.layout.dialog_manual_id_input, null);
		builder.setView(content);

		builder.setTitle(R.string.manual_enter_student_id_title);

		final EditText studentIDText = (EditText) content.findViewById(R.id.studentIDText);

		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String studentID = studentIDText.getText().toString();
				String message = AutoScanActivity.processScan(database, studentID);

				Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
			}
		});

		builder.setNegativeButton(android.R.string.cancel, null);

		builder.show();
	}

}
