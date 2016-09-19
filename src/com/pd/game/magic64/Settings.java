package com.pd.game.magic64;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class Settings extends PreferenceActivity {
	// Option names and default values

	private static final String OPT_HINTS = "hints" ;
	private static final boolean OPT_HINTS_DEF = true;

	private static final String OPT_BORDER = "border";
	private static final boolean OPT_BORDER_DEF = true;

	private static final String OPT_DENSITY = "density";
	private static final String OPT_DENSITY_DEF = "160";
	
	private static final String OPT_STATUS_BAR = "statusbar";
	private static final boolean OPT_STATUS_BAR_DEF = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
	}

	/** Get the current value of the hints option */
	public static boolean getHints(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(OPT_HINTS, OPT_HINTS_DEF);
	}

	public static boolean getBoarder(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(OPT_BORDER, OPT_BORDER_DEF);
	}

	public static int getDensity(Context context) {
		int density = Integer.parseInt(OPT_DENSITY_DEF) ;
		try {
			density = Integer.parseInt(PreferenceManager.getDefaultSharedPreferences(context)
					.getString(OPT_DENSITY, OPT_DENSITY_DEF));

			if(!(density <= 400 && density >= 120))
				density = Integer.parseInt(OPT_DENSITY_DEF) ;	

		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return density;
	}

	public static boolean getStatusBar(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context)
				.getBoolean(OPT_STATUS_BAR, OPT_STATUS_BAR_DEF);	
	}
}
