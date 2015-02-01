package qwerty.dropbox.folders_copy;

import android.content.Context;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxServerException;
import com.dropbox.client2.session.Session.AccessType;

public class DbApi {
	
	final static public String APP_KEY = "znstq6vnd5q250r";
	final static public String APP_SECRET = "mk829kdx0bf3l3z";
	
	final static public AccessType ACCESS_TYPE = AccessType.DROPBOX;
	
	
	// In the class declaration section:
	public static DropboxAPI<AndroidAuthSession> mDBApi;
	
	// You don't need to change these, leave them alone.
    final static public String ACCOUNT_PREFS_NAME = "prefs";
    final static public String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static public String ACCESS_SECRET_NAME = "ACCESS_SECRET";
	public static final int REQUEST_LOAD = 0;
    
    public static final int REQUEST_SAVE=1;
    public static final int REQUEST_Dropbox=2;
    
    public static boolean IsFileExist(String path) throws DropboxException
    {
    	try
    	{
    		mDBApi.metadata(path, 1, null, false, null);
    	}
    	catch (DropboxServerException e)
    	{
    		if(e.error==404)
    		{
    			return false;
    		}
    		throw e;
    	}
    	return true;
    }

}
