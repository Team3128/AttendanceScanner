package org.team3128.attendancescanner.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
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

		db.close();
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

		db.execSQL("UPDATE scanTimes SET outTime=? WHERE rowid=?", new String[]{Long.toString(currentDate.getTime()), Long.toString(scanInRowID)});
		db.close();
	}

	public boolean studentExists(int studentID)
	{
		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor cursor = db.rawQuery("SELECT firstName FROM Students WHERE studentID=?", new String[]{Integer.toString(studentID)});

		boolean retval = cursor.getCount() > 0;

		cursor.close();
		db.close();

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

		Cursor cursor = db.rawQuery("SELECT firstName, lastName FROM students WHERE studentID=?", new String[]{Integer.toString(studentID)});

		cursor.moveToFirst();

		//student not in the database
		if(cursor.getCount() <= 0)
		{
			return null;
		}

		String nameString = cursor.getString(cursor.getColumnIndexOrThrow("firstName")) + ' ' + cursor.getString(cursor.getColumnIndexOrThrow("lastName"));

		if(nameString.equals(" "))
		{
			return null;
		}

		cursor.close();
		db.close();

		return nameString;
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
		db.execSQL("INSERT INTO Students(studentID, firstName, lastName) VALUES (?, ?, ?)", new String[]{Integer.toString(studentID), firstName, lastName});

		db.close();
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

		db.execSQL("UPDATE Students SET firstName=?, lastName=? WHERE studentID=?", new String[]{firstName, lastName, Integer.toString(studentID)});

		db.close();
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

		db.execSQL("DELETE FROM Students WHERE studentID=?", new String[]{Integer.toString(studentID)});

		db.close();
	}

	/**
	 * Get a cursor to the Students table.
	 * @return
	 */
	public Cursor getAllStudents()
	{
		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor results =  db.rawQuery("SELECT rowid AS _id, studentId, firstName, lastName FROM Students", null);

		return results;
	}

	/**
	 * Get a cursor containing in and out times for students, as well their ID numbers.
	 *
	 * Takes the year, month, and day in local time as provided by the Android date selector.
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
		Cursor results =  db.rawQuery("SELECT \n" +
				"    ScanTimes.rowid AS _id, \n" +
				"    Students.studentID, \n" +
				"    Students.firstName, \n" +
				"    Students.lastName,\n" +
				"    ScanTimes.inTime,\n" +
				"    ScanTimes.outTime\n" +
				"FROM \n" +
				"    Students\n" +
				"    INNER JOIN ScanTimes ON(Students.studentID = ScanTimes.studentID)\n" +
				"WHERE\n" +
				"    ScanTimes.inTime BETWEEN ? AND ?\n" +
				"ORDER BY\n" +
				"    ScanTimes.inTime\n", new String[]{Long.toString(startDate.getTime()), Long.toString(endDate.getTime())});

		return results;
	}

	/**
	 * Get a cursor containing total attendance times for students, as well as their names and ID numbers.
	 *
	 * Takes the start and end year, month, and day in local time as provided by the Android date selector.
	 * @return
	 */
	public Cursor getStudentTotalAttendanceTimes(int startYear, int startMonth, int startDay, int endYear, int endMonth, int endDay)
	{
		Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
		calendar.set(startYear, startMonth, startDay, 0, 0);
		Date startDate = calendar.getTime();
		calendar.set(endYear, endMonth, endDay, 23, 59);
		Date endDate = calendar.getTime();

		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor results =  db.rawQuery("SELECT \n" +
				"    Students.rowid AS _id, \n" +
				"    Students.studentID, \n" +
				"    Students.firstName, \n" +
				"    Students.lastName,\n" +
				"    SUM(ScanTimes.outTime - ScanTimes.inTime) AS totalTime\n" +
				"FROM \n" +
				"    Students\n" +
				"    INNER JOIN ScanTimes ON(Students.studentID = ScanTimes.studentID)\n" +
				"WHERE\n" +
				"    ScanTimes.inTime BETWEEN ? AND ?\n" +
				"GROUP BY\n" +
				"    Students.studentId\n" +
				"ORDER BY\n" +
				"    totalTime DESC", new String[]{Long.toString(startDate.getTime()), Long.toString(endDate.getTime())});

		return results;
	}

	/**
	 * Get a cursor containing total attendance times for students, as well as their names and ID numbers.
	 * @return
	 */
	public Cursor getStudentTotalAttendanceTimes()
	{
		SQLiteDatabase db = helper.getReadableDatabase();
		Cursor results = db.rawQuery("SELECT \n" +
				"    Students.rowid AS _id, \n" +
				"    Students.studentID, \n" +
				"    Students.firstName, \n" +
				"    Students.lastName,\n" +
				"    SUM(ScanTimes.outTime - ScanTimes.inTime) AS totalTime\n" +
				"FROM \n" +
				"    Students\n" +
				"    INNER JOIN ScanTimes ON(Students.studentID = ScanTimes.studentID)\n" +
				"GROUP BY\n" +
				"    Students.studentId\n" +
				"ORDER BY\n" +
				"    totalTime DESC", new String[0]);
		return results;
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

		db.close();

		return mostRecentScan;
	}

	/**
	 * Get the rowid of the most recent scan in by the provided student.
	 * If there is no scan or the scan is before recentScanCutoff, then
	 * returns null
	 */
	public Long getMostRecentScanIn(int studentID, Date recentScanCutoff)
	{
		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor cursor = db.rawQuery("SELECT rowid FROM scanTimes WHERE " +
				"studentID = ? AND outTime IS NULL AND inTime > ?", new String[]{Integer.toString(studentID), Long.toString(recentScanCutoff.getTime())});

		cursor.moveToFirst();
		Long rowid = null;

		if(cursor.getCount() > 0)
		{
			rowid = cursor.getLong(cursor.getColumnIndexOrThrow("rowid"));
		}

		cursor.close();

		db.close();

		return rowid;
	}

	/**
	 * Remove all scans from the scans table.
	 */
	public void clearScansTable()
	{
		SQLiteDatabase db = helper.getWritableDatabase();

		db.execSQL("DELETE FROM scanTimes");

		db.close();

	}

	/**
	 * Get the scans table in a format suitable for CSV export.
	 * @return
	 */
	public Cursor getScanTmesForCSV()
	{
		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor results = db.rawQuery("SELECT studentID, " +
					"DATETIME(inTime/1000, 'unixepoch') AS inTimeString," +
					"DATETIME(outTime/1000, 'unixepoch') AS outTimeString " +
				"FROM " +
					"scanTimes " +
				"ORDER BY " +
					"outTime DESC", new String[0]);

		return results;

	}

	public void addScansFromDatabase(File otherDatabase)
	{
		SQLiteDatabase db = helper.getWritableDatabase();

		//attach the other database
		db.execSQL("ATTACH DATABASE '" + otherDatabase.getAbsolutePath() + "' AS otherDB");

		//add any missing students
		db.execSQL("INSERT OR IGNORE INTO students " +
				"SELECT * FROM otherDB.students");

		//copy the scans in, ignoing duplicate rows
		//see http://stackoverflow.com/questions/10703752/skip-over-ignore-duplicate-rows-on-insert
		db.execSQL("INSERT OR IGNORE INTO scanTimes " +
				"SELECT otherTimes.studentID, otherTimes.inTime, otherTimes.outTime FROM otherDB.scanTimes AS otherTimes " +
				"WHERE NOT EXISTS " +
				"(SELECT 1 FROM scanTimes AS currTimes WHERE otherTimes.studentID=currTimes.studentID " +
				"AND otherTimes.inTime=currTimes.inTime " +
				"AND otherTimes.outTime=currTimes.outTime)");

		//detach the other DB
		db.execSQL("DETACH DATABASE otherDB");

		db.close();
	}
}
