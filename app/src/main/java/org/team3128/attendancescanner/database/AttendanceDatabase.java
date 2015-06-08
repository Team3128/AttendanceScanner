package org.team3128.attendancescanner.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

/**
 * Created by Jamie on 5/20/2015.
 */
public class AttendanceDatabase
{
	AttendanceOpenHelper helper;

	public AttendanceDatabase(Context context)
	{
		helper = new AttendanceOpenHelper(context);
	}

	public void addScanIn(int studentID)
	{
		if(!studentExists(studentID))
		{
			Log.d("AttendanceDatabase", "Auto-adding student with ID " + studentID);
			addStudent(studentID, "", "");
		}
		//get the current UTC time
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		Date currentDate = cal.getTime();

		// Gets the data repository in write mode
		SQLiteDatabase db = helper.getWritableDatabase();

		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put("studentID", studentID);
		values.put("inTime", currentDate.getTime());

		db.insert(Tables.ScanTimes.TABLE_NAME, null, values);
	}

	/**
	 * Scans the student out.  Takes the rowid of their last scan in as a parameter
	 * @param scanInRowID
	 */
	public void addScanOut(long scanInRowID)
	{
		//get the current UTC time
		Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		Date currentDate = cal.getTime();

		// Gets the data repository in write mode
		SQLiteDatabase db = helper.getWritableDatabase();

		db.rawQuery("UPDATE scanTimes SET outTime=? WHERE rowid=?", new String[]{Long.toString(currentDate.getTime()), Long.toString(scanInRowID)}).close();
	}

	public boolean studentExists(int studentID)
	{
		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor cursor = db.rawQuery("SELECT firstName FROM Students WHERE studentID=?", new String[]{Integer.toString(studentID)});

		boolean retval = cursor.getCount() > 0;

		cursor.close();

		return retval;
	}

	/**
	 * Get a string containing the full name of the student with the provided ID, or null if the student has no name set.
	 * @param studentID
	 * @return
	 */
	public String getStudentName(int studentID)
	{
		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor cursor = db.rawQuery("SELECT PRINTF(\"%s %s\", firstName, lastName) as name FROM students WHERE studentID=?", new String[]{Integer.toString(studentID)});

		cursor.moveToFirst();

		if(cursor.getCount() <= 0)
		{
			return null;
		}

		String retval = cursor.getString(cursor.getColumnIndexOrThrow("name"));

		cursor.close();

		return retval;
	}

	/**
	 * Add a student to the database.  Assumes that the student doesn't already exist.
	 * @param studentID
	 * @param firstName
	 * @param lastName
	 */
	public void addStudent(int studentID, String firstName, String lastName)
	{
		// Gets the data repository in write mode
		SQLiteDatabase db = helper.getWritableDatabase();

		// Create a new map of values, where column names are the keys
		db.rawQuery("INSERT INTO Students(studentID, firstName, lastName) VALUES(?, ?, ?)", new String[]{Integer.toString(studentID), firstName, lastName}).close();
	}

	/**
	 * Change the student with the given ID's name to the given one.
	 * @param studentID
	 * @param firstName
	 * @param lastName
	 */
	public void updateStudent(int studentID, String firstName, String lastName)
	{

		// Gets the data repository in write mode
		SQLiteDatabase db = helper.getWritableDatabase();

		db.rawQuery("UPDATE Students SET firstName=?, lastName=? WHERE studentID=?", new String[]{firstName, lastName, Integer.toString(studentID)}).close();
	}

	/**
	 * Remove the student with the given ID.
	 *
	 * This also removes any scan data associated with this student.
	 * @param studentID
	 */
	public void removeStudent(int studentID)
	{
		SQLiteDatabase db = helper.getWritableDatabase();

		db.rawQuery("DELETE FROM Students WHERE studentID=?", new String[]{Integer.toString(studentID)}).close();
	}

	/**
	 * Get a cursor to the Students table.
	 * @return
	 */
	public Cursor getAllStudents()
	{
		SQLiteDatabase db = helper.getReadableDatabase();

		return db.rawQuery("SELECT rowid AS _id, studentId, firstName, lastName FROM Students", null);
	}

	/**
	 * Get a cursor containing in and out, and total attendance times for students, as well their ID numbers.
	 *
	 * Takes the year, month, and day in local time as provided by the Android date selector.
	 * @param year
	 * @param month
	 * @param day
	 * @return
	 */
	public Cursor getStudentScanTimes(int year, int month, int day)
	{

		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		calendar.set(year, month, day - 1, 23, 59);
		Date startDate = calendar.getTime();
		calendar.set(year, month, day, 23, 59);
		Date endDate = calendar.getTime();

		SQLiteDatabase db = helper.getReadableDatabase();
		return db.rawQuery("SELECT Students.rowid AS _id, Students.studentID, Students.firstName, Students.lastName, "+
						"ScanTimes.inTime, ScanTimes.outTime, Totals.totalTime " +
						"FROM Students INNER JOIN ScanTimes ON(Students.studentID = ScanTimes.studentID) INNER JOIN ( SELECT \n" +
							"    studentID," +
							"    time(SUM(Sessions.length) / 1000, 'unixepoch') AS totalTime " +
							"FROM " +
							"    Students, " +
							"        (SELECT " +
							"            ScanTimes.inTime - ScanTimes.outTime  AS length " +
							"FROM " +
							"scanTimes " +
							"INNER JOIN Students ON(scanTimes.studentID = Students.studentID) " +
							"WHERE" +
							"            scanTimes.outTime <= " + endDate.getTime() +
						"    ) as Sessions) Totals ON Totals.studentID=Students.studentID " +
						" WHERE ScanTimes.outTime BETWEEN " + startDate.getTime() + " AND " + endDate.getTime() + " " +
						"ORDER BY ScanTimes.inTime", null);
	}

	/**
	 * Get a calendar object for the most recent scan that was done by any student.
	 */
	public Calendar getMostRecentScanTime()
	{
		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor cursor = db.rawQuery("SELECT MAX(inTime) AS maxTime FROM scanTimes", null);

		cursor.moveToFirst();
		Calendar mostRecentScan = new GregorianCalendar();

		if (cursor.getCount() > 0)
		{
			mostRecentScan.setTimeInMillis(cursor.getLong(cursor.getColumnIndexOrThrow("maxTime")));
		}

		cursor.close();

		return mostRecentScan;
	}

	/**
	 * Get the rowid of the most recent scan in by the provided student.
	 * If there is no scan or the scan is before recentScanCutoff than
	 */
	public Long getMostRecentScanIn(int studentID, Date recentScanCutoff)
	{
		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor cursor = db.rawQuery("SELECT rowid FROM scanTimes WHERE " +
				"studentID = ? AND outTime = NULL AND inTime > ?", new String[]{Integer.toString(studentID), Long.toString(recentScanCutoff.getTime())});

		cursor.moveToFirst();
		Long rowid = null;

		if(cursor.getCount() > 0)
		{
			rowid = cursor.getLong(cursor.getColumnIndexOrThrow("rowid"));
		}

		cursor.close();

		return rowid;
	}
}
