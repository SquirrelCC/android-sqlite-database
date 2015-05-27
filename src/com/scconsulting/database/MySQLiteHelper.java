package com.scconsulting.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Custom implementation of SQLiteOpenHelper
 * 
 * @author Shirley Christenson
 *
 */
public class MySQLiteHelper extends SQLiteOpenHelper {
	
	private static final String DATABASE_NAME = "classdb.db";
	public static final String TABLE_NAME_1 = "employee";

	// employee table creation SQL statement
	// Every table in SQLite must have a primary key.
	// By setting the key to autoincrement, _id is set automatically.
	private static final String CREATE_EMPL = "create table " + TABLE_NAME_1 +
			" (_id integer primary key autoincrement, " +
			"firstname text not null default '', " +
			"lastname text not null default '', " +
			"title text not null default '', " +
			"salary integer not null default 0);";
	
	// Sample Version 2 upgrade SQL
	private static final String V2_EMPLOYEE_HOUSE_NUMBER =
			"ALTER TABLE employee " +
			"ADD COLUMN housenumber text not null default '';";
	private static final String V2_EMPLOYEE_STREET =
			"ALTER TABLE employee " +
			"ADD COLUMN street text not null default '';";
	private static final String V2_EMPLOYEE_CITY =
			"ALTER TABLE employee " +
			"ADD COLUMN city text not null default '';";
	
	// Sample Version 3 upgrade SQL
	private static final String V3_EMPLOYEE_STATE =
			"ALTER TABLE employee " +
			"ADD COLUMN state text not null default '';";
	private static final String V3_EMPLOYEE_POSTALCODE =
			"ALTER TABLE employee " +
			"ADD COLUMN postalcode text not null default '';";

	/**
	 * The MySQLiteHelper constructor
	 * 
	 * Create or open the database, and set the version number.
	 * 
	 * @param context	The context to use to open or create the database
	 * @param version	Database version number. If greater than current version,
	 * 		onUpgrade will be called in the super() method.
	 */
	public MySQLiteHelper(Context context, int version) {
		super(context, DATABASE_NAME, null, version);

	}
	
	/**
	 * Called when the database is created for the first time.
	 * Create tables here.
	 */
	@Override
	public void onCreate(SQLiteDatabase database) {

		// Create the employee table
		database.execSQL(CREATE_EMPL);
 		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		/*
		 * Called automatically when a new version number is passed into the constructor
		 *   from your Activity.
		 * (This method need not be called in the Activity using this helper class.)
		 * 
		 * Iterate through version upgrades to handle upgrading from version
		 *   1 to 3 at once, for example.
		 */
		int upgradeTo = oldVersion + 1;
        while (upgradeTo <= newVersion)
        {
            switch (upgradeTo)
            {
                case 2:
                    db.execSQL(V2_EMPLOYEE_HOUSE_NUMBER);
                    db.execSQL(V2_EMPLOYEE_STREET);
                    db.execSQL(V2_EMPLOYEE_CITY);
                    break;
                case 3:
                    db.execSQL(V3_EMPLOYEE_STATE);
                    db.execSQL(V3_EMPLOYEE_POSTALCODE);
                    break;
            }
            upgradeTo++;
        }
	}
}