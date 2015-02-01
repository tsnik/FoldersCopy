package qwerty.dropbox.folders_copy;

import qwerty.dropbox.folders_copy.R;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager.OnActivityResultListener;


public class PrefActivity extends PreferenceActivity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.pref);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Intent intent = getIntent();
		overridePendingTransition(0, 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();
		overridePendingTransition(0, 0);
		startActivity(intent);
		super.onActivityResult(requestCode, resultCode, data);
	}
}
