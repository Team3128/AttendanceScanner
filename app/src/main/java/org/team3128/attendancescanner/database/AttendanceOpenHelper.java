package org.team3128.attendancescanner.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Jamie on 5/20/2015.
 */
public class AttendanceOpenHelper extends SQLiteOpenHelper
{
	// If you change the database schema, you must increment the database version.
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "Attendance.db";

	public AttendanceOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	public void onCreate(SQLiteDatabase db) {
		db.execSQL(Tables.ScanTimes.SQL_CREATE_TABLE);
		db.execSQL(Tables.Students.SQL_CREATE_TABLE);
	}
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// do nothing, for now
	}
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}
}
