package qwerty.dropbox.folders_copy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.preference.Preference;
import android.preference.PreferenceManager.OnActivityResultListener;
import android.util.AttributeSet;

public class DropboxFolderPreference extends Preference {
	
	Context ctx;
		
	public DropboxFolderPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		ctx=context;
	}
	
	@Override
	protected void onClick() {
		OpenDropboxDialog();
		super.onClick();
	}

	 private void OpenDropboxDialog()
	    {
	    	Intent intent = new Intent(ctx, FileDialog.class);
	        intent.putExtra(FileDialog.START_PATH, "/");
	        
	        //can user select directories or not
	        intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
	        intent.putExtra(FileDialog.IS_DROPBOX, true);
	        
	        ((PrefActivity)ctx).startActivityForResult(intent, DbApi.REQUEST_Dropbox);
	        
	    }
	 
	 
	 

}
