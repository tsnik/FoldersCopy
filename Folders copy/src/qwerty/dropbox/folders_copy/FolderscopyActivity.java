package qwerty.dropbox.folders_copy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.UploadRequest;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;

public class FolderscopyActivity extends Activity {
    /** Called when the activity is first created. */
	final static private String APP_KEY = "znstq6vnd5q250r";
	final static private String APP_SECRET = "mk829kdx0bf3l3z";
	
	final static private AccessType ACCESS_TYPE = AccessType.DROPBOX;
	
	Context ctx=this;
	
	// In the class declaration section:
	private DropboxAPI<AndroidAuthSession> mDBApi;
	
	// You don't need to change these, leave them alone.
    final static private String ACCOUNT_PREFS_NAME = "prefs";
    final static private String ACCESS_KEY_NAME = "ACCESS_KEY";
    final static private String ACCESS_SECRET_NAME = "ACCESS_SECRET";
	private static final int REQUEST_LOAD = 0;
    
    int REQUEST_SAVE=1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        AppKeyPair appKeys = new AppKeyPair(APP_KEY, APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys, ACCESS_TYPE);
        mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        
        AccessTokenPair access = getStoredKeys();
        if(access.key!="0" && access.secret!="0")
        {
        mDBApi.getSession().setAccessTokenPair(access);
        try {
			mDBApi.accountInfo();
		} catch (DropboxException e) {
			mDBApi.getSession().startAuthentication(FolderscopyActivity.this);
		}
        }
        else
        {
     // MyActivity below should be your activity class name
        mDBApi.getSession().startAuthentication(FolderscopyActivity.this);
        }
        Button select_folder = (Button)findViewById(R.id.select_folder_button);
        
        select_folder.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getBaseContext(), FileDialog.class);
                intent.putExtra(FileDialog.START_PATH, "/sdcard");
                
                //can user select directories or not
                intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
                
                //alternatively you can set file filter
                //intent.putExtra(FileDialog.FORMAT_FILTER, new String[] { "png" });
                startActivityForResult(intent, REQUEST_SAVE);
				
			}
		});
    }
    private AccessTokenPair getStoredKeys() {
    	SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        return new AccessTokenPair(prefs.getString(ACCESS_KEY_NAME, "0"), prefs.getString(ACCESS_SECRET_NAME, "0"));
	}
	public synchronized void onActivityResult(final int requestCode,
            int resultCode, final Intent data) {

            if (resultCode == Activity.RESULT_OK) {

                    if (requestCode == REQUEST_SAVE) {
                            System.out.println("Saving...");
                    } else if (requestCode == REQUEST_LOAD) {
                            System.out.println("Loading...");
                    }
                    
                    String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
                    UploadFolderAsync upload= new UploadFolderAsync();
                    upload.execute(filePath);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                    //Logger.getLogger(AccelerationChartRun.class.getName()).log(
                                    //Level.WARNING, "file not selected");
            }

    }
    protected void onResume() {
        super.onResume();

        // ...

        if (mDBApi.getSession().authenticationSuccessful()) {
            try {
                // MANDATORY call to complete auth.
                // Sets the access token on the session
                mDBApi.getSession().finishAuthentication();

                AccessTokenPair tokens = mDBApi.getSession().getAccessTokenPair();

                // Provide your own storeKeys to persist the access token pair
                // A typical way to store tokens is using SharedPreferences
                storeKeys(tokens.key, tokens.secret);
            } catch (IllegalStateException e) {
                Log.i("DbAuthLog", "Error authenticating", e);
            }
        }

        // ...
    }
    private void storeKeys(String key, String secret) {
    	// Save the access key for later
        SharedPreferences prefs = getSharedPreferences(ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(ACCESS_KEY_NAME, key);
        edit.putString(ACCESS_SECRET_NAME, secret);
        edit.commit();
    }
    class UploadFolderAsync extends AsyncTask<String, Integer, String>
    {
    	UploadFolderAsync ths=this;
    	DoubleProgressDialog pd = new DoubleProgressDialog(ctx);
    	@Override
    	protected void onPostExecute(String result) {
			pd.hide();
    		super.onPostExecute(result);
    	}
    	@Override
    	protected void onPreExecute() {
    		pd.setMessage(getResources().getText(R.string.Uploading_files));
    		pd.setCancelable(true);
    		pd.setButton(getResources().getText(R.string.cancel), new DialogInterface.OnClickListener() {
    		    @Override
    		    public void onClick(DialogInterface dialogInterface, int i) {
    		        dialogInterface.cancel();
    		        ths.cancel(true);
    		    }
    		});
    		pd.show();
    		super.onPreExecute();
    	}
    	
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			File folder = new File(params[0]);
			publishProgress(1,CountFiles(folder));
			UploadFolder(params[0]);
			return null;
		}
		@Override
		protected void onProgressUpdate(Integer... values) {
			// TODO Auto-generated method stub
			switch (values[0])
			{
			case 0:
				pd.incrementSecondaryProgressBy(1);
				break;
			case 1:
				pd.setSecondaryMax(values[1]);
				//pd.setIndeterminate(false);
				break;
			case 2:
				pd.setProgress(0);
				pd.setMax(values[1]);
				//pd.setIndeterminate(false);
				break;
			case 3:
				pd.setProgress(values[1]);
				break;
			}
			super.onProgressUpdate(values);
		}
    	int CountFiles(File folder)
    	{
    		if(!folder.isDirectory())return 0;
    		int count=0;
    		File [] folders=folder.listFiles();
    		if(folders!=null)
    		{
    		for (File file : folders) {
				if(file.isFile())count++;
				else if (file.isDirectory()) count+=CountFiles(file);
			}
    		}
    		return count;
    	}
    	int root_folder=0;
        private void UploadFolder(String folder_name)
        {
        	if(!isCancelled())
        	{
        	root_folder=folder_name.substring(0,folder_name.lastIndexOf('/', folder_name.length()-1)).length();
        	File folder = new File(folder_name);
        	File [] files=folder.listFiles();
        	if(files!=null)
        	{
        	for (File file : files) {
        		if(file!=null)
        		{
        		if(file.isDirectory())UploadFolder(file);
        		else if(file.isFile())UploadFile(file.getAbsolutePath(), file.getAbsolutePath().substring(root_folder));
        		}
    		}
        	}
        	}
        }
        private void UploadFolder(File folder)
        {
        	if(!isCancelled())
        	{
        	File [] files=folder.listFiles();
        	if(files!=null)
        	{
        	for (File file : files) {
        	if(file != null)
        	{
        		if(file.isDirectory())UploadFolder(file);
        		else if(file.isFile())UploadFile(file.getAbsolutePath(), file.getAbsolutePath().substring(root_folder));
        	}
    		}
        	}
        	}
        }
        private void UploadFile(String Path_to_file, String Location)
        {
        	if(!isCancelled())
    	    {
        	// Uploading content.
        	FileInputStream inputStream = null;
        	try {
        	    File file = new File(Path_to_file);
        	    inputStream = new FileInputStream(file);
        	    publishProgress(2,(int)(file.length()/1024));
        	    Entry newEntry=mDBApi.putFileOverwrite(Location, inputStream,
        	            file.length(), new ProgressListener() {
							
							@Override
							public void onProgress(long arg0, long arg1) {
								publishProgress(3, (int)(arg0/1024));
							}
						});
        	    Log.i("DbExampleLog", "The uploaded file's rev is: " + newEntry.rev);
        	} catch (DropboxUnlinkedException e) {
        	    // User has unlinked, ask them to link again here.
        	    Log.e("DbExampleLog", "User has unlinked.");
        	} catch (DropboxException e) {
        	    Log.e("DbExampleLog", "Something went wrong while uploading.");
        	} catch (FileNotFoundException e) {
        	    Log.e("DbExampleLog", "File not found.");
        	} finally {
        	    if (inputStream != null) {
        	        try {
        	            inputStream.close();
        	        } catch (IOException e) {}
        	    }
        	}
        	publishProgress(0);
    	    }
        }
    }
    
}