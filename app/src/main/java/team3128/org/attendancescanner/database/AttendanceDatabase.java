package team3128.org.attendancescanner.database;

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

	public void addScan(int studentID)
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
		values.put(Tables.ScanTimes.STUDENT_ID, studentID);
		values.put(Tables.ScanTimes.TIME, currentDate.getTime());

		db.insert(Tables.ScanTimes.TABLE_NAME, null, values);
	}

	public boolean studentExists(int studentID)
	{
		SQLiteDatabase db = helper.getReadableDatabase();

		
		Cursor cursor = db.query(Tables.Students.TABLE_NAME,
						new String[]{Tables.Students.STUDENT_ID},
						Tables.Students.STUDENT_ID + " = ?",
						new String[]{Integer.toString(studentID)},
						null, null, null);

		return cursor.getCount() > 0;
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
		ContentValues values = new ContentValues();
		values.put(Tables.Students.STUDENT_ID, studentID);
		values.put(Tables.Students.STUDENT_FIRST_NAME, firstName);
		values.put(Tables.Students.STUDENT_FIRST_NAME, lastName);

		db.insert(Tables.Students.TABLE_NAME, null, values);
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

		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(Tables.Students.STUDENT_FIRST_NAME, firstName);
		values.put(Tables.Students.STUDENT_LAST_NAME, lastName);

		db.update(Tables.Students.TABLE_NAME,
						values,
						Tables.Students.STUDENT_ID + " = ?",
						new String[]{Integer.toString(studentID)});
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

		//remove associated scans
		db.delete(Tables.ScanTimes.TABLE_NAME,
						Tables.ScanTimes.STUDENT_ID + " = ?",
						new String[]{Integer.toString(studentID)});

		//remove the actual student
		db.delete(Tables.Students.TABLE_NAME,
						Tables.Students.STUDENT_ID + " = ?",
						new String[]{Integer.toString(studentID)});
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
	 * Get a cursor containing in and out times for students, as well their ID numbers.
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
		return db.rawQuery("SELECT Students.rowid AS _id, Students.studentID, Students.firstName, Students.lastName, MIN(ScanTimes.scanTime) AS timeIn, MAX(ScanTimes.scanTime) AS timeOut " +
						"FROM Students INNER JOIN ScanTimes ON(Students.studentID = ScanTimes.studentID) " +
						"WHERE ScanTimes.scanTime BETWEEN " + startDate.getTime() + " AND " + endDate.getTime() + " " +
						"GROUP BY Students.studentID", null);
	}

	/**
	 * Get a date object for the most recent scan that was done.
	 * @return
	 */
	public Calendar getMostRecentScanTime()
	{
		SQLiteDatabase db = helper.getReadableDatabase();

		Cursor cursor = db.rawQuery("SELECT MAX(scanTime) AS maxTime FROM scanTimes", null);

		cursor.moveToFirst();
		Calendar mostRecentScan = new GregorianCalendar();

		if(cursor.getCount() > 0)
		{
			mostRecentScan.setTimeInMillis(cursor.getLong(cursor.getColumnIndexOrThrow("maxTime")));
		}

		return mostRecentScan;
	}
}
