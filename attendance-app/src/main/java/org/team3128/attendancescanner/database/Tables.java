package org.team3128.attendancescanner.database;

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

	/* Inner class that defines the table contents */
	public static abstract class ScanTimes implements BaseColumns
	{
		public static final String TABLE_NAME = "ScanTimes";

		//use SQLite's built-in rowid
		public static final String _ID = "rowid";

		public static final String SQL_CREATE_TABLE =
						"CREATE TABLE " + TABLE_NAME + " (studentID INTEGER, " +
			"inTime INTEGER, " +//stored as seconds since start of UNIX epoch
			"outTime INTEGER, " +//stored as seconds since start of UNIX epoch
			"FOREIGN KEY(studentID) REFERENCES " + Students.TABLE_NAME + "(studentID)" +
			" )";
	}

	/* Inner class that defines the table contents */
	public static abstract class Students implements BaseColumns
	{
		public static final String TABLE_NAME = "Students";

		public static final String SQL_CREATE_TABLE =
						"CREATE TABLE " + TABLE_NAME + " (studentID INTEGER PRIMARY KEY, " +
										"firstName TEXT, " +
										"lastName TEXT" +
										")";
	}
}
