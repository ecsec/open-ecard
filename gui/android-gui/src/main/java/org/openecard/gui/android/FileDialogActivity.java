/****************************************************************************
 * Copyright (C) 2013 HS Coburg.
 * All rights reserved.
 * Contact: ecsec GmbH (info@ecsec.de)
 *
 * This file is part of the Open eCard App.
 *
 * GNU General Public License Usage
 * This file may be used under the terms of the GNU General Public
 * License version 3.0 as published by the Free Software Foundation
 * and appearing in the file LICENSE.GPL included in the packaging of
 * this file. Please review the following information to ensure the
 * GNU General Public License version 3.0 requirements will be met:
 * http://www.gnu.org/copyleft/gpl.html.
 *
 * Other Usage
 * Alternatively, this file may be used in accordance with the terms
 * and conditions contained in a signed written agreement between
 * you and ecsec GmbH.
 *
 ***************************************************************************/

package org.openecard.gui.android;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.os.Bundle;
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
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import org.openecard.common.I18n;
import org.openecard.gui.file.AcceptAllFilesFilter;
import org.openecard.gui.file.FileDialogResult;
import org.openecard.gui.file.FileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This Activity is used by {@link AndroidMessageDialog} to show a MessageDialog. It uses the parameters given in the
 * calling Intent to adapt the representation according to them.
 *
 * @author Dirk Petrautzki
 */
public class FileDialogActivity extends ListActivity {

    private final I18n lang = I18n.getTranslation("gui");
    private static final Logger logger = LoggerFactory.getLogger(FileDialogActivity.class);

    private static final String ITEM_FILENAME = "key";
    private static final String ITEM_IMAGE = "image";
    private static final String ROOT_DIRECTORY = "/";
    public static final String RESULT = "RESULT";
    public static final int RESULT_CODE = 3;

    private TextView textViewCurrentPath;
    private EditText mFileName;
    private ArrayList<HashMap<String, Object>> listItems = new ArrayList<HashMap<String, Object>>();
    private Button selectButton;
    private LinearLayout layoutSelect;
    private LinearLayout layoutCreate;
    private InputMethodManager inputManager;
    private String parentPath;
    private File currentPath;
    private File selectedFile;
    private Boolean folderSelectable;
    private Boolean showHiddenFiles;
    private String title;
    private ArrayList<FileFilter> fileFilters;
    private FileDialogType dialogType;
    private String approveButtonText;
    private TextView textViewFileName;
    private SimpleAdapter listItemsAdapter;
    // TODO pre- and multiselection are currently not supported
    private ArrayList<File> selectedFiles;
    private Boolean multiSelectionEnabled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setResult(RESULT_CODE, getIntent());

	// set up the layout
	setContentView(R.layout.file_dialog);
	textViewCurrentPath = (TextView) findViewById(R.id.path);
	textViewFileName = (TextView) findViewById(R.id.textViewFilename);
	textViewFileName.setText(lang.translationForKey("file_name"));
	mFileName = (EditText) findViewById(R.id.fdEditTextFile);
	inputManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

	getParameters();
	setTitle(title);
	setupButtons();

	// set up the list adapter
	String[] from = new String[] { ITEM_FILENAME, ITEM_IMAGE };
	int[] to = new int[] { R.id.fdrowtext, R.id.fdrowimage };
	listItemsAdapter = new SimpleAdapter(this, listItems, R.layout.file_dialog_row, from, to);
	setListAdapter(listItemsAdapter);

	// show contents of initial directory or root
	showDirectoryContents(currentPath);
    }

    /**
     * Set the text and OnClickListeners for the buttons of this Activity.
     */
    private void setupButtons() {
	selectButton = (Button) findViewById(R.id.fdButtonSelect);
	selectButton.setEnabled(false);
	selectButton.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		if (selectedFile != null) {
		    ArrayList<File> files = new ArrayList<File>();
		    files.add(selectedFile);
		    FileDialogResult res = new FileDialogResult(files);
		    getIntent().putExtra(RESULT, res);
		    setResult(RESULT_CODE, getIntent());
		    finish();
		}
	    }
	});

	if (FileDialogType.OPEN.equals(dialogType)) {
	    selectButton.setText(lang.translationForKey("button.open"));
	} else if (FileDialogType.SAVE.equals(dialogType)) {
	    selectButton.setText(lang.translationForKey("button.save"));
	} else {
	    selectButton.setText(approveButtonText);
	}

	final Button newButton = (Button) findViewById(R.id.fdButtonNew);
	newButton.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		setCreateVisible(v);
		inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
		mFileName.setText("");
		mFileName.requestFocus();
	    }
	});
	newButton.setText(lang.translationForKey("button.new"));

	layoutSelect = (LinearLayout) findViewById(R.id.fdLinearLayoutSelect);
	layoutCreate = (LinearLayout) findViewById(R.id.fdLinearLayoutCreate);
	layoutCreate.setVisibility(View.GONE);

	final Button cancelButton = (Button) findViewById(R.id.fdButtonCancel);
	cancelButton.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		setSelectVisible(v);
		inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
	    }

	});
	cancelButton.setText(lang.translationForKey("button.cancel"));

	final Button createButton = (Button) findViewById(R.id.fdButtonCreate);
	createButton.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		if (mFileName.getText().length() > 0) {
		    File f = new File(currentPath.getPath() + File.separator + mFileName.getText());
		    ArrayList<File> files = new ArrayList<File>();
		    files.add(f);
		    FileDialogResult res = new FileDialogResult(files);
		    getIntent().putExtra(RESULT, res);
		    setResult(RESULT_CODE, getIntent());
		    finish();
		}
	    }
	});
	createButton.setText(lang.translationForKey("button.save"));
    }

    /**
     * Load and show the contents of the given directory.
     *
     * @param directory Directory to show the contents for
     */
    private void showDirectoryContents(File directory) {
	currentPath = directory;

	listItems.clear();

	File[] files = currentPath.listFiles();
	if (files == null) {
	    currentPath = new File(ROOT_DIRECTORY);
	    files = currentPath.listFiles();
	}
	try {
	    textViewCurrentPath.setText(lang.translationForKey("location") + ": " + currentPath.getCanonicalPath());
	} catch (IOException e) {
	    logger.error("Couldn't get canonical file", e);
	}
	if (! ROOT_DIRECTORY.equals(currentPath.getAbsolutePath())) {
	    // add items for the root and the parent directory
	    addItem(ROOT_DIRECTORY, R.drawable.folder);

	    addItem("../", R.drawable.folder);
	    parentPath = currentPath.getParent();
	}

	filterAndAddFiles(files);

	listItemsAdapter.notifyDataSetChanged();
    }

    /**
     * Adds the given file to the list of items using the given imageId as icon.
     *
     * @param fileName name of the file
     * @param imageId id of the icon to use
     */
    private void addItem(String fileName, int imageId) {
	HashMap<String, Object> item = new HashMap<String, Object>();
	item.put(ITEM_FILENAME, fileName);
	item.put(ITEM_IMAGE, imageId);
	listItems.add(item);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
	File file = new File(currentPath + File.separator + listItems.get(position).get(ITEM_FILENAME));
	setSelectVisible(v);
	inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
	if (file.isDirectory()) {
	    selectButton.setEnabled(false);
	    if (file.canRead()) {
		showDirectoryContents(file);
		if (folderSelectable) {
		    try {
			selectedFile = file.getCanonicalFile();
		    } catch (IOException e) {
			logger.error("Couldn't get canonical file", e);
		    }
		    v.setSelected(true);
		    selectButton.setEnabled(true);
		}
	    } else {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setIcon(R.drawable.openecard);
		builder.setTitle(lang.translationForKey("no_read_access", file.getName()));
		builder.setPositiveButton(lang.translationForKey("button.ok"), new DialogInterface.OnClickListener() {

		    @Override
		    public void onClick(DialogInterface dialog, int which) {
			// just close
		    }
		});
		builder.show();
	    }
	} else {
	    try {
		selectedFile = file.getCanonicalFile();
	    } catch (IOException e) {
		logger.error("Couldn't get canonical file", e);
	    }
	    v.setSelected(true);
	    selectButton.setEnabled(true);
	}
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
	// Overridden to change the behavior if the user presses the BACK-key.
	if ((keyCode == KeyEvent.KEYCODE_BACK)) {
	    selectButton.setEnabled(false);

	    if (layoutCreate.getVisibility() == View.VISIBLE) {
		layoutCreate.setVisibility(View.GONE);
		layoutSelect.setVisibility(View.VISIBLE);
	    } else {
		if (! currentPath.equals(ROOT_DIRECTORY)) {
		    showDirectoryContents(new File(parentPath));
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
     * Alter the layout to enable the user to create a new file.
     */
    private void setCreateVisible(View v) {
	layoutCreate.setVisibility(View.VISIBLE);
	layoutSelect.setVisibility(View.GONE);
	selectButton.setEnabled(false);
    }

    /**
     * Alter the layout to enable the user to select an existing file.
     */
    private void setSelectVisible(View v) {
	layoutCreate.setVisibility(View.GONE);
	layoutSelect.setVisibility(View.VISIBLE);
	selectButton.setEnabled(false);
    }

    /**
     * Filters the directory contents according to the parameters and adds the remaining dirs and files to the item
     * list.
     *
     * @param filesn content of the directory
     */
    private void filterAndAddFiles(File[] files) {
	TreeMap<String, String> dirsMap = new TreeMap<String, String>();
	TreeMap<String, String> filesMap = new TreeMap<String, String>();
	for (File file : files) {
	    if (file.getName().startsWith(".") && ! showHiddenFiles) {
		continue;
	    }
	    if (file.isDirectory()) {
		String dirName = file.getName();
		dirsMap.put(dirName, dirName);
	    } else {
		String fileName = file.getName();
		for (FileFilter fileFilter : fileFilters) {
		    if (fileFilter.accept(file)) {
			filesMap.put(fileName, fileName);
			break;
		    }
		}
	    }
	}
	for (String dir : dirsMap.tailMap("").values()) {
	    addItem(dir, R.drawable.folder);
	}

	for (String file : filesMap.tailMap("").values()) {
	    addItem(file, R.drawable.file);
	}
    }

    /**
     * Extract the parameters from the calling intent.
     */
    private void getParameters() {
	Bundle extras = getIntent().getExtras();
	Serializable serializable = extras.getSerializable(AndroidFileDialog.CURRENT_DIR);
	if (serializable != null) {
	    currentPath = (File) serializable;
	} else {
	    currentPath = new File(ROOT_DIRECTORY);
	}

	title = extras.getString(AndroidFileDialog.TITLE);
	showHiddenFiles = extras.getBoolean(AndroidFileDialog.SHOW_HIDDEN_FILES, false);
	multiSelectionEnabled = extras.getBoolean(AndroidFileDialog.MULTI_SELECTION_ENABLED, false);
	folderSelectable = extras.getBoolean(AndroidFileDialog.FOLDER_SELECTABLE, false);

	fileFilters = (ArrayList<FileFilter>) extras.getSerializable(AndroidFileDialog.FILE_FILTERS);

	if (fileFilters.isEmpty()) {
	    fileFilters.add(new AcceptAllFilesFilter());
	}

	selectedFiles = (ArrayList<File>) extras.getSerializable(AndroidFileDialog.SELECTED_FILES);
	dialogType = (FileDialogType) extras.getSerializable(AndroidFileDialog.FILE_DIALOG_TYPE);
	approveButtonText = extras.getString(AndroidFileDialog.APPROVE_BUTTON_TEXT);
    }

}
