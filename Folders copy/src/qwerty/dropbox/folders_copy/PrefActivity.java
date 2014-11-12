package qwerty.dropbox.folders_copy;

import qwerty.dropbox.folders_copy.R;
import android.os.Bundle;
import android.preference.PreferenceActivity;


public class PrefActivity extends PreferenceActivity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref);
	}

}
