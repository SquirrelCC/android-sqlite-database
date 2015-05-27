package com.scconsulting.database;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class Employee extends Activity {
	
	public final static String TAG = Employee.class.getSimpleName();
	public boolean save = true;
	private int result;

    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private Cursor employee = null;
    private int position = -1;
    private int employeeId = -1;
    
	private EditText editFName;
    private EditText editLName;
    private EditText editTitle;
    private EditText editSalary;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // If employeeId was sent as extra in the Intent, this is an existing employee.
        // If no extra information in the Intent, this is a new employee.
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	employeeId = extras.getInt("id");
        	position = extras.getInt("pos");
        }
        
        setContentView(R.layout.employee);
        
        // Call the database helper, using the version number in our "Database" activity.
        dbHelper = new MySQLiteHelper(this, Database.databaseVersion);
        database = dbHelper.getWritableDatabase();
        
        editFName = (EditText) findViewById(R.id.editFName);
        editLName = (EditText) findViewById(R.id.editLName);
        editTitle = (EditText) findViewById(R.id.editTitle);
        editSalary = (EditText) findViewById(R.id.editSalary);
        
        Button bSave= (Button) findViewById(R.id.button_save);
        bSave.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				result = RESULT_OK;
				doSave();

			}}
        );
        
        Button bCancel= (Button) findViewById(R.id.button_cancel);
        bCancel.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				
				result = RESULT_CANCELED;
				Intent intent = new Intent();
		        setResult(result, intent);
		        finish();

			}}
        );
    }
	
	@Override
	public void onResume() {
		super.onResume();
		result = RESULT_OK;

		// Edit existing employee if employeeId >= 0 
		if (employeeId > -1) {
	        if (employee != null) {
	        	employee.close();
	        }
	        // Select by ID, using the ID as "args" (an argument, or parameter).
	        String strSql = "select * from employee" +
	        		" where _id = ?" + ";";
	        String[] args = new String[]{
			  		Integer.toString(employeeId)
			};
			employee = database.rawQuery(strSql, args);
			employee.moveToFirst();
			
			editFName.setText(employee.getString(1));
			editLName.setText(employee.getString(2));
			editTitle.setText(employee.getString(3));
			editSalary.setText(employee.getString(4));
			
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if (result == RESULT_OK) {
		
			String strSql;
			
			// If employee ID is -1, this is a new employee.
			// Insert the row (record) in the employee table.
			switch (employeeId) {
			case -1:
				
				insertEmployee();
				
				break;
				
			default:
				
		    	// Employee ID is > -1, so this is an existing employee.
				// Update the employee information.
				updateEmployee();
		    	
		    	break;
			}
		}
		
		if (employee != null) {
        	employee.close();
        	employee = null;
        }
		
		database.close();
		
	}
	
	@Override
	public void onBackPressed() {
		
		if (result == RESULT_OK) {
			if (employeeId < 0) {
				insertEmployee();
			}
		}

		Intent intent = new Intent();
		intent.putExtra("id", employeeId);
		intent.putExtra("pos", position);
		setResult(result, intent);
		// close the activity
		finish();
		
	}
	
	private void doSave() {
		if (employeeId > -1) {
			updateEmployee();
		}
		else {
			insertEmployee();
		}
	}
	
	private void insertEmployee() {
		String strSql = "INSERT INTO " + "employee" + 
				" (firstname, " +
				"lastname, " +
				"title, " +
				"salary) " +
	   	    "VALUES (" + 
	   	    	"'" + editFName.getText() + "'" + ", " +
	   	    	"'" + editLName.getText() + "'" + ", " +
	   	    	"'" + editTitle.getText() + "'" + ", " +
	   	    	"'" + editSalary.getText() + "'" +
	   	    ");";
		database.execSQL(strSql);
		strSql = null;
		
		// Get a Cursor containing only the _id column of each employee table entry.
		// The last row will be the new employee.
		// We need to get all rows to determine the position of the new row in the table.
		String sql = "select _id from employee;";
		Cursor c = database.rawQuery(sql, null);
		c.moveToLast();
		employeeId = c.getInt(0);
		position = c.getPosition();
		
		c.close();
		c = null;
		sql = null;
	}
	
	private void updateEmployee() {
		String strSql = "update employee set " +
    			"firstname = " + "'" + editFName.getText() + "'" + ", " +
    			"lastname = " + "'" + editLName.getText() + "'" + ", " +
    			"title = " + "'" + editTitle.getText() + "'" + ", " +
    			"salary = " + "'" + editSalary.getText() + "'" +
    		" where " +
    			"_id = " + employeeId + 
    		";";
		database.execSQL(strSql);
		strSql = null;
	}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.database, menu);
        return true;
    }
}