package team3128.org.attendancescanner.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
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
			addStudent(studentID, null);
		}

		// Gets the data repository in write mode
		SQLiteDatabase db = helper.getWritableDatabase();

		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(Tables.ScanTimes.COLUMN_NAME_STUDENT_ID, studentID);
		values.put(Tables.ScanTimes.COLUMN_NAME_TIME, System.currentTimeMillis());

		db.insert(Tables.ScanTimes.TABLE_NAME, null, values);
	}

	public boolean studentExists(int studentID)
	{
		SQLiteDatabase db = helper.getReadableDatabase();

		
		Cursor cursor = db.query(Tables.Students.TABLE_NAME,
						new String[]{Tables.Students.COLUMN_NAME_STUDENT_ID},
						Tables.Students.COLUMN_NAME_STUDENT_ID + " = ?",
						new String[]{Integer.toString(studentID)},
						null, null, null);

		return cursor.getCount() > 0;
	}

	/**
	 * Add a student to the database.  Assumes that the student doesn't already exist.
	 * @param studentID
	 * @param name
	 */
	public void addStudent(int studentID, String name)
	{
		// Gets the data repository in write mode
		SQLiteDatabase db = helper.getWritableDatabase();

		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(Tables.Students.COLUMN_NAME_STUDENT_ID, studentID);
		values.put(Tables.Students.COLUMN_NAME_STUDENT_NAME, name);

		db.insert(Tables.Students.TABLE_NAME, null, values);
	}

	/**
	 * Change the student with the given ID's name to the given one.
	 * @param studentID
	 * @param name
	 */
	public void updateStudent(int studentID, String name)
	{

		// Gets the data repository in write mode
		SQLiteDatabase db = helper.getWritableDatabase();

		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(Tables.Students.COLUMN_NAME_STUDENT_NAME, name);

		db.update(Tables.Students.TABLE_NAME,
						values,
						Tables.Students.COLUMN_NAME_STUDENT_ID + " = ?",
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
						Tables.ScanTimes.COLUMN_NAME_STUDENT_ID + " = ?",
						new String[]{Integer.toString(studentID)});

		//remove the actual student
		db.delete(Tables.Students.TABLE_NAME,
						Tables.Students.COLUMN_NAME_STUDENT_ID + " = ?",
						new String[]{Integer.toString(studentID)});
	}

	/**
	 * Get a cursor to the Students table.
	 * @return
	 */
	public Cursor getAllStudents()
	{
		SQLiteDatabase db = helper.getReadableDatabase();

		return db.rawQuery("SELECT * FROM ?", new String[]{Tables.Students.TABLE_NAME});
	}
}
