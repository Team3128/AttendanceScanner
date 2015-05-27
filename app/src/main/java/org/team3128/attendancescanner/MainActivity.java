package org.team3128.attendancescanner;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

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

	public TextView consoleTextView;

	public Button scanButton;

	public AttendanceDatabase database;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		consoleTextView = (TextView) findViewById(R.id.consoleTextView);
		scanButton = (Button) findViewById(R.id.scanButton);
		scanButton.setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				invokeScan();
			}
		});

		database = new AttendanceDatabase(this);
	}

	@Override
	protected void onResume()
	{
		super.onResume();
	}

	private void invokeScan()
	{
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.addExtra("SCAN_WIDTH", 800);
		integrator.addExtra("SCAN_HEIGHT", 300);
		integrator.addExtra("RESULT_DISPLAY_DURATION_MS", 0);
		integrator.addExtra("PROMPT_MESSAGE", "Scan Student ID");
		integrator.initiateScan(IntentIntegrator.ONE_D_CODE_TYPES);
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

			return;

		}

		IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
		if (result != null)
		{
			String contents = result.getContents();
			if(contents != null)
			{
				try
				{
					int id = Integer.parseInt(contents);
					String name = database.getStudentName(id);
					consoleTextView.append("Scanned " + (name != null ? name : id) + "\n");
					database.addScan(id);
				}
				catch(NumberFormatException ex)
				{
					consoleTextView.append("Invalid student ID!\n");
				}
			}
            else
            {
                consoleTextView.append("Scan Error\n");
            }
		}
		else
		{
			consoleTextView.append("Scan Cancelled\n");
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return true;
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

	private void backupDatabase()
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		if(id == R.id.action_student_names)
		{
			startActivity(new Intent(this, StudentNameActivity.class));
			return true;
		}
		else if(id == R.id.action_view_attendance)
		{
			startActivity(new Intent(this, AttendanceActivity.class));
			return true;
		}
		else if(id == R.id.action_export_database)
		{
			backupDatabase();
		}
		else if(id == R.id.action_import_database)
		{
			backupDatabase();
			Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
			intent.setType("file/*");
			intent.putExtra("com.estrongs.intent.extra.TITLE", "Import Database");

			startActivityForResult(intent, FILE_PICKER_REQUEST_CODE);
		}
		return super.onOptionsItemSelected(item);
	}
}
