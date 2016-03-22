package org.team3128.attendancescanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import org.team3128.attendancescanner.database.AttendanceDatabase;
import org.team3128.attendancescanner.database.AttendanceOpenHelper;
import org.team3128.attendancescanner.scanner.ScannerActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class AdminActivity extends Activity
{

	private final static int IMPORT_FILE_PICKER_REQUEST_CODE = 1;
	private final static int MERGE_FILE_PICKER_REQUEST_CODE = 2;

	AttendanceDatabase database;

	SharedPreferences preferences;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		database = new AttendanceDatabase(this);
		preferences = getPreferences(Context.MODE_PRIVATE);

		String password = preferences.getString("password", "");

		if (!password.isEmpty())
		{
			//close the activity unless the user enters the password
			PasswordDialog.show(this, savedInstanceState, getLayoutInflater(), Base64.decode(password, Base64.NO_WRAP), new Runnable()
			{
				@Override
				public void run()
				{
					finish();
				}
			});
		}
	}

	@Override
	protected void onStart()
	{
		super.onStart();

	}

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		PasswordDialog.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent)
	{
		//check if the user pressed cancel
		if(intent != null)
		{
			if (requestCode == IMPORT_FILE_PICKER_REQUEST_CODE)
			{

				//get the file that was returned
				Uri fileUri = intent.getData();
				File file = new File(fileUri.getPath());
				Log.v("AdminActivity", "Loading new database " + file.getPath());
				Log.v("AdminActivity", "File exists: " + file.exists());
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


			} else if (requestCode == MERGE_FILE_PICKER_REQUEST_CODE)
			{
				Uri fileUri = intent.getData();
				File otherDB = new File(fileUri.getPath());
				Log.v("AdminActivity", "Merging database " + otherDB.getPath());
				database.mergeDatabase(otherDB);

				Toast.makeText(this, "Merged database successfully!", Toast.LENGTH_SHORT).show();
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

		Log.d("AdminActivity", "Attempting to copy " + database.getPath() + " to " + backupLocation.getPath());
		if(database.exists())
		{
			try
			{
				copyFiles(database, backupLocation);
				Toast.makeText(this, "Backed up attendance database to " + backupLocation.getPath(), Toast.LENGTH_LONG).show();
			}
			catch (IOException e)
			{
				Log.e("AdminActivity", "Error backing up database to " + backupLocation.getPath());
				e.printStackTrace();

				Toast.makeText(this, "Error backing up database: Can't copy file.", Toast.LENGTH_LONG).show();

			}

		}
		else
		{
			Log.e("AdminActivity", "Could not back up database: database not found or can't write to output file.");

			Toast.makeText(this, "Error backing up database: Database doesn't exist.", Toast.LENGTH_LONG).show();
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
		//I feel like this does more harm than good.
		//backupDatabase(null);

		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("file/*");
		intent.putExtra("com.estrongs.intent.extra.TITLE", "Import Database");

		startActivityForResult(intent, IMPORT_FILE_PICKER_REQUEST_CODE);
	}

	public void manualInput(View view)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		// layout and inflater
		View content = getLayoutInflater().inflate(R.layout.dialog_manual_id_input, null);
		builder.setView(content);

		builder.setTitle(R.string.manual_enter_student_id_title);

		final EditText studentIDText = (EditText) content.findViewById(R.id.studentIDText);
		final CheckBox backdateCheckbox = (CheckBox) content.findViewById(R.id.backdateCheckbox);
		final TimePicker backdateTimePicker = (TimePicker) content.findViewById(R.id.backdateTimePicker);

		backdateCheckbox.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				backdateTimePicker.setVisibility(((CheckBox)v).isChecked() ? View.VISIBLE : View.GONE);
			}
		});


		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String studentID = studentIDText.getText().toString();

				Date scanOutTime = null;

				if(backdateCheckbox.isChecked())
				{
					Calendar cal = Calendar.getInstance(TimeZone.getDefault());
					cal.set(Calendar.HOUR_OF_DAY, backdateTimePicker.getCurrentHour());
					cal.set(Calendar.MINUTE, backdateTimePicker.getCurrentMinute());
					cal.set(Calendar.SECOND, 0);

					Log.d("AdminActivity", "Backdating student at " + DateFormat.getTimeInstance().format(cal.getTime()));

					scanOutTime = cal.getTime();
				}

				String message = ScannerActivity.processScan(database, studentID, scanOutTime);

				Toast.makeText(AdminActivity.this, message, Toast.LENGTH_LONG).show();
			}
		});

		builder.setNegativeButton(android.R.string.cancel, null);

		Dialog dialog = builder.show();

		//force show keyboard
		dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}
	public void clearDatabase(View view)
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setTitle(R.string.areyousure);
		builder.setMessage(R.string.delete_all_records);

		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);

				builder.setTitle(R.string.areyoureallysure);
				builder.setMessage(R.string.might_want_this_someday);

				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
				{

					@Override
					public void onClick(DialogInterface dialog, int which)
					{
						database.clearScansTable();

						Toast.makeText(AdminActivity.this, "Scan database cleared.", Toast.LENGTH_SHORT).show();
					}
				});

				builder.setNegativeButton(android.R.string.cancel, null);

				builder.show();
			}
		});

		builder.setNegativeButton(android.R.string.cancel, null);

		builder.show();
	}

	//AlertDialogs can't have instance variables, so this is neccesary
	private static int selectedOption = -1;

	public void exportToCSV(View view)
	{
		selectedOption = -1;

		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.select_export_type);
		builder.setSingleChoiceItems(R.array.csv_export_options, -1, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				selectedOption = which;

				((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(true);
			}
		});

		builder.setNegativeButton(android.R.string.cancel, null);
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				runCSVExport(selectedOption == 0);
			}
		});

		AlertDialog dialog = builder.show();
		dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);

	}

	private void runCSVExport(boolean exportTotals)
	{
		ProgressDialog dialog = new ProgressDialog(this);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		dialog.setTitle(R.string.exporting_records);

		File exportLocation = new File(Environment.getExternalStorageDirectory(), "attendancerecords.csv");
		Toast.makeText(this, "Exporting attendance data to " + exportLocation.getAbsolutePath(), Toast.LENGTH_SHORT).show();

		try
		{
			CSVWriter csvWriter = new CSVWriter(new FileWriter(exportLocation));

			CSVExporterAsyncTask exporter = new CSVExporterAsyncTask();
			dialog.show();
			exporter.execute(csvWriter, database, dialog, exportTotals);
		}
		catch(Exception ex)
		{
			Toast.makeText(this, "Error exporting", Toast.LENGTH_SHORT).show();
			ex.printStackTrace();
		}
	}

	public void mergeDatabase(View view)
	{
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("file/*");
		intent.putExtra("com.estrongs.intent.extra.TITLE", "Merge Database");

		startActivityForResult(intent, MERGE_FILE_PICKER_REQUEST_CODE);
	}


}
