package qwerty.dropbox.folders_copy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.util.AttributeSet;
import android.view.View;

public class DropboxFolderPreference extends Preference {

	public static final String IsDropboxA = "IsDropbox";
	public static final String StartPathA = "StartPath";
	public static final String ns = "http://schemas.android.com/apk/res/qwerty.dropbox.folders_copy";

	private Context ctx;
	private boolean IsDropbox;
	private String startPath;

	public DropboxFolderPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.FolderPreference, 0, 0);

		try {
			IsDropbox = a.getBoolean(R.styleable.FolderPreference_IsDropbox,
					false);
			startPath = a.getString(R.styleable.FolderPreference_StartPath);
		} finally {
			a.recycle();
		}
		if (startPath == null) {
			startPath = "";
			if (!IsDropbox) {
				startPath = Environment.getExternalStorageDirectory().getPath();
			}
		}
		ctx = context;
	}

	@Override
	protected void onBindView(View view) {
		setSummary(getPersistedString(""));
		super.onBindView(view);
	}

	@Override
	protected void onClick() {
		OpenDropboxDialog();
		super.onClick();
	}

	private void OpenDropboxDialog() {
		Intent intent = new Intent(ctx, FileDialog.class);
		intent.putExtra(FileDialog.START_PATH, startPath);

		// can user select directories or not
		intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
		intent.putExtra(FileDialog.IS_DROPBOX, IsDropbox);

		intent.putExtra(FileDialog.UPDATE_PREFERENCE, true);
		intent.putExtra(FileDialog.UPDATE_PREFERENCE_NAME, this.getKey());

		((PrefActivity) ctx).startActivityForResult(intent,
				DbApi.REQUEST_Dropbox);
	}

}
