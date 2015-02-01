package qwerty.dropbox.folders_copy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import com.dropbox.client2.exception.DropboxException;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * Activity para escolha de arquivos/diretorios.
 * 
 * @author android
 * 
 */
public class FileDialog extends ListActivity {

	/**
	 * Chave de um item da lista de paths.
	 */
	private static final String ITEM_KEY = "key";

	/**
	 * Imagem de um item da lista de paths (diretorio ou arquivo).
	 */
	private static final String ITEM_IMAGE = "image";

	/**
	 * Diretorio raiz.
	 */
	private static final String ROOT = "/";

	/**
	 * Parametro de entrada da Activity: path inicial. Padrao: ROOT.
	 */
	public static final String START_PATH = "START_PATH";

	/**
	 * Parametro de entrada da Activity: filtro de formatos de arquivos. Padrao:
	 * null.
	 */
	public static final String FORMAT_FILTER = "FORMAT_FILTER";

	/**
	 * Parametro de saida da Activity: path escolhido. Padrao: null.
	 */
	public static final String RESULT_PATH = "RESULT_PATH";

	/**
	 * Parametro de entrada da Activity: tipo de selecao: pode criar novos paths
	 * ou nao. Padrao: nao permite.
	 * 
	 * @see {@link SelectionMode}
	 */
	public static final String SELECTION_MODE = "SELECTION_MODE";

	/**
	 * Parametro de entrada da Activity: se e permitido escolher diretorios.
	 * Padrao: falso.
	 */
	public static final String CAN_SELECT_DIR = "CAN_SELECT_DIR";

	public static final String IS_DROPBOX = "IS_DROPBOX";

	public static final String UPDATE_PREFERENCE = "UPDATE_PREFERENCE";

	public static final String UPDATE_PREFERENCE_NAME = "UPDATE_PREFERENCE_NAME";

	private List<String> path = null;
	private TextView myPath;
	private EditText mFileName;
	private ArrayList<HashMap<String, Object>> mList;

	private Button selectButton;
	private Button cancelButton;

	private LinearLayout layoutSelect;
	private LinearLayout layoutCreate;
	private InputMethodManager inputManager;
	private String parentPath;
	private String currentPath = ROOT;
	private String startPath = "";

	private int selectionMode = SelectionMode.MODE_CREATE;

	private String[] formatFilter = null;

	private boolean canSelectDir = false;
	private boolean isDropbox = false;
	private boolean updatePref = false;
	private String updatePrefName = "";

	private Context ctx;

	private String selectedFile;
	private HashMap<String, Integer> lastPositions = new HashMap<String, Integer>();

	/**
	 * Called when the activity is first created. Configura todos os parametros
	 * de entrada e das VIEWS..
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ctx = this;
		setResult(RESULT_CANCELED, getIntent());

		setContentView(R.layout.file_dialog_main);
		myPath = (TextView) findViewById(R.id.path);

		inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

		selectButton = (Button) findViewById(R.id.fdButtonSelect);
		//selectButton.setEnabled(false);
		selectButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (selectedFile != null) {
					getIntent().putExtra(RESULT_PATH, selectedFile);
					if (updatePref) {
						SharedPreferences sp = PreferenceManager
								.getDefaultSharedPreferences(ctx);
						sp.edit()
						.putString(updatePrefName, selectedFile)
						.commit();
						
					}
					setResult(RESULT_OK, getIntent());
					finish();
				}
			}
		});

		cancelButton = (Button) findViewById(R.id.fdButtonCancel);
		cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED, getIntent());
				finish();

			}
		});

		selectionMode = getIntent().getIntExtra(SELECTION_MODE,
				SelectionMode.MODE_CREATE);

		formatFilter = getIntent().getStringArrayExtra(FORMAT_FILTER);

		canSelectDir = getIntent().getBooleanExtra(CAN_SELECT_DIR, false);

		isDropbox = getIntent().getBooleanExtra(IS_DROPBOX, false);

		updatePref = getIntent().getBooleanExtra(UPDATE_PREFERENCE, false);

		updatePrefName = getIntent().getStringExtra(UPDATE_PREFERENCE_NAME);

		layoutSelect = (LinearLayout) findViewById(R.id.fdLinearLayoutSelect);
		layoutCreate = (LinearLayout) findViewById(R.id.fdLinearLayoutCreate);
		layoutCreate.setVisibility(View.GONE);

		startPath = getIntent().getStringExtra(START_PATH);
		startPath = startPath != null ? startPath : ROOT;
		selectedFile=startPath;
		if (isDropbox) {
			getDropboxDir(startPath);
			return;
		}
		if (canSelectDir) {
			selectedFile = startPath;
			selectButton.setEnabled(true);
		}
		getDir(startPath);
	}

	private void getDir(String dirPath) {
		if (isDropbox) {
			getDropboxDir(dirPath);
			return;
		}
		boolean useAutoSelection = dirPath.length() < currentPath.length();

		Integer position = lastPositions.get(parentPath);

		getDirImpl(dirPath);

		if (position != null && useAutoSelection) {
			getListView().setSelection(position);
		}

	}

	private void getDropboxDir(String dirPath) {

		boolean useAutoSelection = dirPath.length() < currentPath.length();

		Integer position = lastPositions.get(parentPath);

		getDropboxDirImpl(dirPath);

		if (position != null && useAutoSelection) {
			getListView().setSelection(position);
		}

	}

	private void getDropboxDirImpl(final String dirPath) {
		currentPath = dirPath;

		final List<String> item = new ArrayList<String>();
		path = new ArrayList<String>();
		mList = new ArrayList<HashMap<String, Object>>();

		try {
			Entry f = DbApi.mDBApi.metadata(currentPath, 0, null, true, null);
			List<Entry> files = f.contents;

			myPath.setText(getText(R.string.location) + ": " + currentPath);

			if (!currentPath.equals(ROOT)) {

				item.add(ROOT);
				addItem(ROOT, R.drawable.folder);
				path.add(ROOT);

				item.add("../");
				addItem("../", R.drawable.folder);
				path.add(f.parentPath());
				parentPath = f.parentPath();

			}

			TreeMap<String, String> dirsMap = new TreeMap<String, String>();
			TreeMap<String, String> dirsPathMap = new TreeMap<String, String>();
			TreeMap<String, String> filesMap = new TreeMap<String, String>();
			TreeMap<String, String> filesPathMap = new TreeMap<String, String>();
			for (Entry file : files) {
				if (file.isDir) {
					String dirName = file.fileName();
					dirsMap.put(dirName, dirName);
					dirsPathMap.put(dirName, file.path);
				} else {
					final String fileName = file.fileName();
					final String fileNameLwr = fileName.toLowerCase();
					// se ha um filtro de formatos, utiliza-o
					if (formatFilter != null) {
						boolean contains = false;
						for (int i = 0; i < formatFilter.length; i++) {
							final String formatLwr = formatFilter[i]
									.toLowerCase();
							if (fileNameLwr.endsWith(formatLwr)) {
								contains = true;
								break;
							}
						}
						if (contains) {
							filesMap.put(fileName, fileName);
							filesPathMap.put(fileName, file.path);
						}
						// senao, adiciona todos os arquivos
					} else {
						filesMap.put(fileName, fileName);
						filesPathMap.put(fileName, file.path);
					}
				}
			}

			item.addAll(dirsMap.tailMap("").values());
			item.addAll(filesMap.tailMap("").values());
			path.addAll(dirsPathMap.tailMap("").values());
			path.addAll(filesPathMap.tailMap("").values());

			SimpleAdapter fileList = new SimpleAdapter(this, mList,
					R.layout.file_dialog_row, new String[] { ITEM_KEY,
							ITEM_IMAGE }, new int[] { R.id.fdrowtext,
							R.id.fdrowimage });

			for (String dir : dirsMap.tailMap("").values()) {
				addItem(dir, R.drawable.folder);
			}

			for (String file : filesMap.tailMap("").values()) {
				addItem(file, R.drawable.file);
			}

			fileList.notifyDataSetChanged();

			setListAdapter(fileList);

		} catch (DropboxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Monta a estrutura de arquivos e diretorios filhos do diretorio fornecido.
	 * 
	 * @param dirPath
	 *            Diretorio pai.
	 */
	private void getDirImpl(final String dirPath) {

		currentPath = dirPath;

		final List<String> item = new ArrayList<String>();
		path = new ArrayList<String>();
		mList = new ArrayList<HashMap<String, Object>>();

		File f = new File(currentPath);
		File[] files = f.listFiles();
		if (files == null) {
			currentPath = ROOT;
			f = new File(currentPath);
			files = f.listFiles();
		}
		myPath.setText(getText(R.string.location) + ": " + currentPath);

		if (!currentPath.equals(ROOT)) {

			item.add(ROOT);
			addItem(ROOT, R.drawable.folder);
			path.add(ROOT);

			item.add("../");
			addItem("../", R.drawable.folder);
			path.add(f.getParent());
			parentPath = f.getParent();

		}

		TreeMap<String, String> dirsMap = new TreeMap<String, String>();
		TreeMap<String, String> dirsPathMap = new TreeMap<String, String>();
		TreeMap<String, String> filesMap = new TreeMap<String, String>();
		TreeMap<String, String> filesPathMap = new TreeMap<String, String>();
		for (File file : files) {
			if (file.isDirectory()) {
				String dirName = file.getName();
				dirsMap.put(dirName, dirName);
				dirsPathMap.put(dirName, file.getPath());
			} else {
				final String fileName = file.getName();
				final String fileNameLwr = fileName.toLowerCase();
				// se ha um filtro de formatos, utiliza-o
				if (formatFilter != null) {
					boolean contains = false;
					for (int i = 0; i < formatFilter.length; i++) {
						final String formatLwr = formatFilter[i].toLowerCase();
						if (fileNameLwr.endsWith(formatLwr)) {
							contains = true;
							break;
						}
					}
					if (contains) {
						filesMap.put(fileName, fileName);
						filesPathMap.put(fileName, file.getPath());
					}
					// senao, adiciona todos os arquivos
				} else {
					filesMap.put(fileName, fileName);
					filesPathMap.put(fileName, file.getPath());
				}
			}
		}
		item.addAll(dirsMap.tailMap("").values());
		item.addAll(filesMap.tailMap("").values());
		path.addAll(dirsPathMap.tailMap("").values());
		path.addAll(filesPathMap.tailMap("").values());

		SimpleAdapter fileList = new SimpleAdapter(this, mList,
				R.layout.file_dialog_row,
				new String[] { ITEM_KEY, ITEM_IMAGE }, new int[] {
						R.id.fdrowtext, R.id.fdrowimage });

		for (String dir : dirsMap.tailMap("").values()) {
			addItem(dir, R.drawable.folder);
		}

		for (String file : filesMap.tailMap("").values()) {
			addItem(file, R.drawable.file);
		}

		fileList.notifyDataSetChanged();

		setListAdapter(fileList);

	}

	private void addItem(String fileName, int imageId) {
		HashMap<String, Object> item = new HashMap<String, Object>();
		item.put(ITEM_KEY, fileName);
		item.put(ITEM_IMAGE, imageId);
		mList.add(item);
	}

	/**
	 * Quando clica no item da lista, deve-se: 1) Se for diretorio, abre seus
	 * arquivos filhos; 2) Se puder escolher diretorio, define-o como sendo o
	 * path escolhido. 3) Se for arquivo, define-o como path escolhido. 4) Ativa
	 * botao de selecao.
	 */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {

		if (isDropbox) {

			setSelectVisible(v);
			selectButton.setEnabled(false);
			lastPositions.put(currentPath, position);
			selectedFile = path.get(position);
			getDropboxDir(path.get(position));
			v.setSelected(true);
			selectButton.setEnabled(true);

		} else {
			File file = new File(path.get(position));

			setSelectVisible(v);

			selectButton.setEnabled(false);
			if (file.canRead()) {
				lastPositions.put(currentPath, position);
				getDir(path.get(position));
				if (canSelectDir) {
					selectedFile = file.getAbsolutePath();
					v.setSelected(true);
					selectButton.setEnabled(true);
				}
			} else {
				new AlertDialog.Builder(this)
						.setIcon(R.drawable.icon)
						.setTitle(
								"[" + file.getName() + "] "
										+ getText(R.string.cant_read_folder))
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {

									}
								}).show();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			selectButton.setEnabled(false);

			if (layoutCreate.getVisibility() == View.VISIBLE) {
				layoutCreate.setVisibility(View.GONE);
				layoutSelect.setVisibility(View.VISIBLE);
			} else {
				if (!currentPath.equals(ROOT)) {
					getDir(parentPath);
				} else {
					return super.onKeyDown(keyCode, event);
				}
			}

			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * Define se o botao de CREATE e visivel.
	 * 
	 * @param v
	 */
	private void setCreateVisible(View v) {
		layoutCreate.setVisibility(View.VISIBLE);
		layoutSelect.setVisibility(View.GONE);

		inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
		selectButton.setEnabled(false);
	}

	/**
	 * Define se o botao de SELECT e visivel.
	 * 
	 * @param v
	 */
	private void setSelectVisible(View v) {
		layoutCreate.setVisibility(View.GONE);
		layoutSelect.setVisibility(View.VISIBLE);

		inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
		selectButton.setEnabled(false);
	}
}
