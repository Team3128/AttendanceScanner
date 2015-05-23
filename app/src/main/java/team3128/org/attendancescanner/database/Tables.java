package team3128.org.attendancescanner.database;

import android.provider.BaseColumns;

/**
 * This class defines constants for the row names of the tables.
 *
 * Note: some of this code taken from the example at http://developer.android.com/training/basics/data-storage/databases.html
 */
public class Tables
{
	private Tables() {}

	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";

	/* Inner class that defines the table contents */
	public static abstract class ScanTimes implements BaseColumns
	{
		public static final String TABLE_NAME = "scanTimes";

		//use SQLite's built-in rowid
		public static final String _ID = "rowid";

		public static final String STUDENT_ID = "studentID";
		public static final String TIME = "scanTime";

		public static final String SQL_CREATE_TABLE =
						"CREATE TABLE " + TABLE_NAME + " (" +
										STUDENT_ID + " INTEGER" + COMMA_SEP +
										TIME + " INTEGER " +  COMMA_SEP +//stored as seconds since start of UNIX epoch
										"FOREIGN KEY(" + STUDENT_ID + ") REFERENCES " + Students.TABLE_NAME + "(" + Students.STUDENT_ID + ")" +
										" )";
	}

	/* Inner class that defines the table contents */
	public static abstract class Students implements BaseColumns
	{
		public static final String TABLE_NAME = "students";
		public static final String STUDENT_ID = "studentID";
		public static final String STUDENT_FIRST_NAME = "firstName";
		public static final String STUDENT_LAST_NAME = "lastName";


		public static final String SQL_CREATE_TABLE =
						"CREATE TABLE " + TABLE_NAME + " (" +
										STUDENT_ID + " INTEGER PRIMARY KEY" + COMMA_SEP +
										STUDENT_FIRST_NAME + TEXT_TYPE + COMMA_SEP +
										STUDENT_LAST_NAME + TEXT_TYPE +
										" )";
	}
}
