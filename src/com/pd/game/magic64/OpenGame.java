package com.pd.game.magic64;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class OpenGame extends Activity{

	private ListView list;
	private String filePath;
	private File dir;
	String[] files = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.opengame);
		list = (ListView) findViewById(R.id.listOpenGame);
		files = init();

		list.setOnItemClickListener(new OnItemClickListener() {


			@Override
			public void onItemClick(AdapterView<?> parent, View v,
					int position, long id) {
				String selectedFileName;
				selectedFileName = files[position];
				openIt(selectedFileName);
				finish();
			}
		});

		//		list.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {
		//			
		//			@Override
		//			public void onCreateContextMenu(ContextMenu menu, View v,
		//					ContextMenuInfo menuInfo) {
		//				
		//			}
		//		});

		list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View v,
					final int position, long id) {
				AlertDialog.Builder alert = new AlertDialog.Builder(OpenGame.this);  
				alert.setTitle("Save Game");  
				alert.setIcon(R.drawable.save32);
				alert.setMessage("Do you want to delete it ?");  
				alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {  
					public void onClick(DialogInterface dialog, int whichButton) {
						if(!deleteIt(files[position]))
						{
			    			Toast.makeText(OpenGame.this, "Could not deleted.", Toast.LENGTH_SHORT).show();
						}
						init();
					}

					
				});
				alert.setNegativeButton("Cancel", null);
				alert.show();
				return true;
			}

		});
	}


	private String[] init() {
		filePath = "/data/data/"+this.getPackageName()+"/magic64data";
		dir = new File(filePath);
		String[] mFiles = null;
		if(dir.exists() && dir.isDirectory()){
			mFiles = dir.list();

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, android.R.id.text1, mFiles);

			// Assign adapter to ListView
			list.setAdapter(adapter);	

		}
		return mFiles;
	}

	protected void openIt(String selectedFileName) {
		Intent openGame = new Intent(OpenGame.this, Game.class);

		openGame.putExtra(Game.GAME_OPEN, selectedFileName);
		startActivity(openGame);
	}

	private boolean deleteIt(String selectedFileName) {
		return new File(filePath + "/" + selectedFileName).delete();
	}

}
