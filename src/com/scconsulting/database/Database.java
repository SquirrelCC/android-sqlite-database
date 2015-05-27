package com.scconsulting.database;

import java.util.ArrayList;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

public class Database extends Activity {
	
	public final static String TAG = Database.class.getSimpleName();
	
	private SQLiteDatabase database;
	// Make the database version number a public variable,
	//   so that it can be referenced in other activities.
	public static int databaseVersion = 1;
	
	private ArrayAdapter<String> adapter;
	
	private ArrayList<String> list;
	private ListView emplListView;
	
    private Cursor employee;
	private MySQLiteHelper dbHelper;
	private int employeeId = -1;
	private int listPosition = -1;
	private boolean readOnly = false;
	private int request_Code = 1;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.database);
        
        // Get an instance of our custom MySqliteHelper class.
        // For future upgrades, add the table modification SQL to the custom helper class
        //   and pass in a version number incremented by 1.
        dbHelper = new MySQLiteHelper(this, databaseVersion);
        
        // Create or open the database.
        // This call will actually return a writable database unless there is some problem,
        //   such as storage full.
        database = dbHelper.getReadableDatabase();
        
        // Check whether database is writable.
        // If there is a reasonable possibility of full storage, large database, etc.,
        //   it would be a good idea to check if readOnly is true
        //   before each insert, update, or delete.
        if (database.isReadOnly()) {
        	readOnly = true;
        	Toast.makeText(Database.this, Database.this.getString(R.string.not_write), Toast.LENGTH_LONG).show();
        }
        
        // Get a handle to the employee ListView, and set up the onItemClick() listener.
        // The ListView uses a custom layout for each row, named "list_item_checked.xml".
        emplListView = (ListView) findViewById( R.id.emplList );
        emplListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE); // Allow only 1 choice in the list
        emplListView.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	
            	// Toggle the check mark on/off on multiple clicks, same entry.
            	CheckedTextView textView = (CheckedTextView) view;
            	
            	if (listPosition == position) {
            		// This entry was previously selected.
            		// Set the clicked line checked if it was not checked, and vice versa.
            		textView.setChecked(!textView.isChecked());
            	}
            	
            	if (textView.isChecked()) {
	                listPosition = position;
	                employee.moveToPosition(position);
	            	employeeId = employee.getInt(0);
            	}
            	else {
            		employeeId = -1;
            		listPosition = -1;
            	}
            }
        });
        
        Button bNew = (Button) findViewById(R.id.button_new);
        bNew.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				
				// Set up an Intent to go to the Employee Activity, with no employee ID sent along.
				Intent intent = new Intent(v.getContext(), Employee.class);
           		startActivityForResult(intent, request_Code);
				
			}}
        );
        
        Button bDelete = (Button) findViewById(R.id.button_delete);
        bDelete.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				
				if (listPosition > -1) {
						
					// Set up an AlertDialog to ask user for confirmation of deletion.
					AlertDialog.Builder alert = new AlertDialog.Builder(Database.this);
		            alert.setTitle("Delete");
		            alert.setMessage("Are you sure you want to delete this employee?");
		            
		            alert.setPositiveButton("YES", new OnClickListener() {

		            	@Override
		                public void onClick(DialogInterface dialog, int which) {
		            		
		                    // Deletion was confirmed. First remove the AlertDialog.
		            		dialog.dismiss();
		                    
		            		// The employee is in the database and also showing in the ListView. 
		            		// Remove from ListView
							list.remove(listPosition);
							adapter.notifyDataSetChanged();
							// The next list entry gets set checked automatically. Uncheck it.
							emplListView.setItemChecked(listPosition, false);
							
							// Delete from database
							String strSql = "delete from employee" +
									" where _id = " + employeeId +
									";";
							database.execSQL(strSql);
							strSql = null;
							
							// Get a fresh employee Cursor without the deleted row.
							employeeQuery();
							employeeId = -1;
							listPosition = -1;
		                    
		                }
		            });
		            
		            alert.setNegativeButton("NO", new OnClickListener() {

		            	@Override
		                public void onClick(DialogInterface dialog, int which) {
		                	dialog.dismiss();
		                	// Do nothing
		                }
		            });

		            alert.show();

				}
				else {
					Toast.makeText(Database.this, Database.this.getString(R.string.not_selected), Toast.LENGTH_LONG).show();
				}
				
			}}
        );
        
        Button bEdit = (Button) findViewById(R.id.button_edit);
        bEdit.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				
				if (employeeId >= 0) {
					
					// Set up an Intent to go to the Employee Activity.
					Intent intent = new Intent(v.getContext(), Employee.class);
					// Put the employee ID into the Intent.
					intent.putExtra("id", employeeId);
					// Put the row position in the table into the Intent.
					// This is the same as the position in the ListView.
					intent.putExtra("pos", listPosition);
					
					startActivityForResult(intent, request_Code);
				
				}
				else {
					Toast.makeText(Database.this, Database.this.getString(R.string.not_selected), Toast.LENGTH_LONG).show();
				}
			}}
        );
    }
	
    public void setupList() {
    	
        list = new ArrayList<String>();
		while (!employee.isAfterLast()) {

			// Put employee ID, first name, last name into each list entry
			// These columns are at positions 0, 1, and 2 in the employee table.
			// The employee ID is stored as an integer,
			//   but if we get it as a String, it is converted automatically.
			list.add( padLeft(employee.getString(0), 5 ) + ".  " +
					employee.getString(1) + " " + employee.getString(2));
			employee.moveToNext();
			
		}

        // Set up new adapter for the ListView.
		// Use a CheckedTextView (in the layout list_item_checked)
		//   for the rows in the ListView.
		adapter = new ArrayAdapter<String>(this, R.layout.list_item_checked, list);
        emplListView.setAdapter(adapter);
        
    }
    
	private String padLeft(String s, int n) {
		// Some formatting stuff for left padding a String with spaces.
		return String.format("%1$" + n + "s", s);
	}

	private void employeeQuery() {
        if (employee != null) {
        	employee.close();
        }
        // Get new Cursor containing all rows and all columns in employee table.
        String strSql = "select * from employee;";
		employee = database.rawQuery(strSql, null);
		if (employee.getCount() > 0) {
			employee.moveToFirst();
		}
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == request_Code) {
			if (resultCode == RESULT_OK) {
				if (data != null) {
					
					// Get the information returned from the Activity called with
					//   startActivityForResult().
					// The extra information in the Intent identifies the new or edited employee.
					// Save the information to be handled in onResume(), which is fired
					//   just after onActivityResult().
					Bundle extras = data.getExtras();
					int id = extras.getInt("id");
		            employeeId = extras.getInt("id", -1);
		            listPosition = extras.getInt("pos", -1);
				}
			}
		}
    }
	
	@Override
	public void onResume() {
		super.onResume();
		
		if (!database.isOpen()) {
			database = dbHelper.getReadableDatabase();
		}
		
		employeeQuery();
        setupList();
        
        if (employee.getCount() > 0) {
        	if (employeeId > -1 && listPosition > -1) {
        		// employeeId and listPosition were returned from Employee Activity.
        		
        		// Move to the position in the employee cursor of the previously selected or
        		//   the new employee.
        		employee.moveToPosition(listPosition);
	        	emplListView.setSelection(listPosition);
	        	// Turn on the check mark.
	        	((ListView) emplListView).setItemChecked(listPosition, true);
        	
        	}
        }
        else {
        	employeeId = -1;
        	listPosition = -1;
        }
	}
	
	@Override
	public void onPause() {
		super.onPause();
		
		if (employee != null) {
        	employee.close();
        	employee = null;
        }
		database.close();
		
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.database, menu);
        return true;
    }
}
