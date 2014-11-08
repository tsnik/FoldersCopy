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
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.ProgressListener;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.exception.DropboxUnlinkedException;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session.AccessType;
import qwerty.dropbox.folders_copy.DbApi;

public class FolderscopyActivity extends Activity {
    /** Called when the activity is first created. */

	Context ctx=this;
	
    
    
    Button start_btn;
    TextView from_input;
    TextView dest_input;
    CheckBox overwrite_checkbox;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        AppKeyPair appKeys = new AppKeyPair(DbApi.APP_KEY, DbApi.APP_SECRET);
        AndroidAuthSession session = new AndroidAuthSession(appKeys,DbApi.ACCESS_TYPE);
        DbApi.mDBApi = new DropboxAPI<AndroidAuthSession>(session);
        
        AccessTokenPair access = getStoredKeys();
        if(access.key!="0" && access.secret!="0")
        {
        DbApi.mDBApi.getSession().setAccessTokenPair(access);
        try {
			DbApi.mDBApi.accountInfo();
		} catch (DropboxException e) {
			DbApi.mDBApi.getSession().startAuthentication(FolderscopyActivity.this);
		}
        }
        else
        {
     // MyActivity below should be your activity class name
        DbApi.mDBApi.getSession().startAuthentication(FolderscopyActivity.this);
        }
        start_btn = (Button)findViewById(R.id.start_button);
        from_input = (TextView)findViewById(R.id.from_input);
        dest_input = (TextView)findViewById(R.id.dest_input);
        overwrite_checkbox = (CheckBox)findViewById(R.id.overwrite_checkbox);
        
        from_input.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				OpenDialog();
			}
		});
        
        from_input.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus)
				{
					OpenDialog();
				}
				
			}
		});
        
        
        dest_input.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				OpenDropboxDialog();
			}
		});
        
        dest_input.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus)
				{
					OpenDropboxDialog();
				}
				
			}
		});
        
        start_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				String filePath = (String) from_input.getText();
                UploadFolderAsync upload= new UploadFolderAsync();
                upload.execute(filePath);
				
			}
		});
    }
    
    private void OpenDialog()
    {
    	Intent intent = new Intent(getBaseContext(), FileDialog.class);
        intent.putExtra(FileDialog.START_PATH, Environment.getExternalStorageDirectory().getPath());
        
        //can user select directories or not
        intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
        
        startActivityForResult(intent, DbApi.REQUEST_SAVE);
    }
    
    private void OpenDropboxDialog()
    {
    	Intent intent = new Intent(getBaseContext(), FileDialog.class);
        intent.putExtra(FileDialog.START_PATH, "/");
        
        //can user select directories or not
        intent.putExtra(FileDialog.CAN_SELECT_DIR, true);
        intent.putExtra(FileDialog.IS_DROPBOX, true);
        
        startActivityForResult(intent, DbApi.REQUEST_Dropbox);
    }
    
    private AccessTokenPair getStoredKeys() {
    	SharedPreferences prefs = getSharedPreferences(DbApi.ACCOUNT_PREFS_NAME, 0);
        return new AccessTokenPair(prefs.getString(DbApi.ACCESS_KEY_NAME, "0"), prefs.getString(DbApi.ACCESS_SECRET_NAME, "0"));
	}
	public synchronized void onActivityResult(final int requestCode,
            int resultCode, final Intent data) {

            if (resultCode == Activity.RESULT_OK) {

                    if (requestCode == DbApi.REQUEST_SAVE) {
                            System.out.println("Saving...");
                    } else if (requestCode == DbApi.REQUEST_LOAD) {
                            System.out.println("Loading...");
                    }
                    
                    String filePath = data.getStringExtra(FileDialog.RESULT_PATH);
                    if(requestCode == DbApi.REQUEST_Dropbox)
                    {
                    	dest_input.setText(filePath);
                    	return;
                    }
                    start_btn.setEnabled(filePath!="");
                    from_input.setText(filePath);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                    //Logger.getLogger(AccelerationChartRun.class.getName()).log(
                                    //Level.WARNING, "file not selected");
            }

    }
    protected void onResume() {
        super.onResume();

        // ...

        if (DbApi.mDBApi.getSession().authenticationSuccessful()) {
            try {
                // MANDATORY call to complete auth.
                // Sets the access token on the session
                DbApi.mDBApi.getSession().finishAuthentication();

                AccessTokenPair tokens = DbApi.mDBApi.getSession().getAccessTokenPair();

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
        SharedPreferences prefs = getSharedPreferences(DbApi.ACCOUNT_PREFS_NAME, 0);
        Editor edit = prefs.edit();
        edit.putString(DbApi.ACCESS_KEY_NAME, key);
        edit.putString(DbApi.ACCESS_SECRET_NAME, secret);
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
        	    Entry newEntry=DbApi.mDBApi.putFileOverwrite(Location, inputStream,
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