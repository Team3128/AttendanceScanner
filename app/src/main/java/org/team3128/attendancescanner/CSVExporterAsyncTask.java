package org.team3128.attendancescanner;

import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.AsyncTask;
import android.widget.Toast;

import com.opencsv.CSVWriter;

import org.team3128.attendancescanner.database.AttendanceDatabase;

import java.io.IOException;

/**
 * AsyncTask to export the scans database to the provided CSV writer.
 *
 * Closes the CSV writer when finished.
 *
 * execute() args: CSVWriter, AttendanceDatabase, ProgressBar
 * Created by Jamie on 8/20/2015.
 */
public class CSVExporterAsyncTask extends AsyncTask<Object, Integer, Object>
{
	CSVWriter writer;
	Cursor scansCursor;
	ProgressDialog progressDialog;


	@Override
	protected Object doInBackground(Object... params)
	{
		try
		{
			writer = (CSVWriter) params[0];
			scansCursor = ((AttendanceDatabase) params[1]).getScansForCSV();
			progressDialog = (ProgressDialog) params[2];
		}
		catch(ClassCastException ex)
		{
			throw new IllegalArgumentException(ex);
		}

		int studentIDColumn = scansCursor.getColumnIndexOrThrow("studentID");
		int inTimeStringColumn = scansCursor.getColumnIndexOrThrow("inTimeString");
		int outTimeStringColumn = scansCursor.getColumnIndexOrThrow("outTimeString");

		final int totalRows = scansCursor.getCount();

		int index = 1;
		do
		{
			String[] line = new String[]{Integer.toString(scansCursor.getInt(studentIDColumn)), scansCursor.getString(inTimeStringColumn), scansCursor.getString(outTimeStringColumn)};
			writer.writeNext(line);

			publishProgress(index, totalRows);
			++index;
		}
		while (scansCursor.moveToNext());

		scansCursor.close();

		return null;
	}

	private void closeResources()
	{
		if(writer != null)
		{
			try
			{
				writer.close();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		if(scansCursor != null)
		{
			scansCursor.close();
		}
	}

	@Override
	protected void onProgressUpdate(Integer... values)
	{
		progressDialog.setProgress(values[0]);
		progressDialog.setMax(values[1]);
	}


	@Override
	protected void onCancelled(Object o)
	{
		super.onCancelled(o);

		closeResources();
	}

	@Override
	protected void onPostExecute(Object o)
	{
		super.onPostExecute(o);

		closeResources();

		Toast.makeText(progressDialog.getContext(), "Export finished", Toast.LENGTH_SHORT).show();
	}
}
