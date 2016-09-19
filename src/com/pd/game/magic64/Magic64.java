package com.pd.game.magic64;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class Magic64 extends Activity implements OnClickListener{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// Set up click listeners for all the buttons
		View continueButton = this.findViewById(R.id.continue_button);
		continueButton.setOnClickListener(this);
		View newButton = this.findViewById(R.id.new_button);
		newButton.setOnClickListener(this);
		View aboutButton = this.findViewById(R.id.about_button);
		aboutButton.setOnClickListener(this);
		View exitButton = this.findViewById(R.id.settings_button);
		exitButton.setOnClickListener(this);
		View openButton = this.findViewById(R.id.open_button);
		openButton.setOnClickListener(this);

	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.about_button:
			Intent i = new Intent(Magic64.this, About.class);
			startActivity(i);	
			break;
		case R.id.new_button:
			startActivity(new Intent(Magic64.this, Game.class));
			break;
		case R.id.settings_button:
			startActivity(new Intent(this, Settings.class));
			break;
		case R.id.continue_button:
			Intent continueGame = new Intent(Magic64.this, Game.class);
			continueGame.putExtra(Game.GAME_CONTINUE, true);
			startActivity(continueGame);
			break;
		case R.id.open_button:
			startActivity(new Intent(Magic64.this,OpenGame.class));
			break;


		}
	}

}