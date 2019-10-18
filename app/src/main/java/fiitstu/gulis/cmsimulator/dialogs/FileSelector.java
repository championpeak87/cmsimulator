package fiitstu.gulis.cmsimulator.dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;
import fiitstu.gulis.cmsimulator.R;
import fiitstu.gulis.cmsimulator.database.FileHandler;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that shows a dialog that allows navigating the file system and selecting a file
 *
 * Created by Jakub Sedlář on 14.01.2018.
 */
public class FileSelector {

    public interface FileSelectedListener {
        void onFileSelected(String filePath, FileHandler.Format format);
    }

    public interface ExceptionListener {
        void onException(Exception e);
    }

    //log tag
    private static final String TAG = FileSelector.class.getName();

    /**
     * The path to the current folder
     */
    private File path;
    /**
     * True if the currently selected folder is on top level and we cannot navigate up
     */
    private boolean firstLvl;
    /**
     * The list of currently displayed files
     */
    private List<FileItem> fileList;
    /**
     * Adapter for displaying the list of available files and folders
     */
    private ListAdapter adapter;

    /**
     * Action executed after file is selected
     */
    private FileSelectedListener fileSelectedListener;
    /**
     * Action executed if exception happened during the selection
     */
    private ExceptionListener exceptionListener;

    /**
     * Shows a dialog for selecting the file
     * @param context the context
     */
    public void selectFile(Context context) {
        //start in the default folder if it exists
        String defaultFolder = context.getResources().getString(R.string.default_folder);
        path = new File(Environment.getExternalStorageDirectory() + "/" + defaultFolder);
        if (!path.exists()) {
            path = Environment.getExternalStorageDirectory();
            firstLvl = true;
        }
        loadFileList(context);
        generateDialog(context);
    }

    /**
     * Sets the action to be executed after a file is successfully selected
     * @param fileSelectedListener the action to be executed after a file is successfully selected
     */
    public void setFileSelectedListener(FileSelectedListener fileSelectedListener) {
        this.fileSelectedListener = fileSelectedListener;
    }

    /**
     * Sets the action that should be executed if an exception occurs while selecting the file.
     * The action should not perform logging (this is already done by the FileSelector instance,
     * and the exception would be logged twice)
     * @param exceptionListener the action to be performed if an exception occurs while a file is being selected
     */
    public void setExceptionListener(ExceptionListener exceptionListener) {
        this.exceptionListener = exceptionListener;
    }

    /**
     * Prepares the list of files available in the current folder
     * @param context the context
     */
    private void loadFileList(final Context context) {
        try {
            if (path.exists()) {
                FilenameFilter filter = new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        File sel = new File(dir, filename);
                        // Filters based on whether the file is hidden or not
                        return (sel.isFile() || sel.isDirectory())
                                && !sel.isHidden();

                    }
                };

                String[] fList = path.list(filter);
                if (fList != null) {
                    fileList = new ArrayList<>();
                    if (!firstLvl) {
                        fileList.add(new FileItem("Up", R.drawable.directory_up));
                    }

                    for (String fileName : fList) {
                        // Convert into file path
                        File sel = new File(path, fileName);
                        // Set drawables
                        if (sel.isDirectory()) {
                            fileList.add(new FileItem(fileName, R.drawable.directory_icon));
                            Log.d(TAG, "DIRECTORY " + fileList.get(fileList.size() - 1).file);
                        } else {
                            fileList.add(new FileItem(fileName, R.drawable.file_icon));
                            Log.d(TAG, "FILE " + fileList.get(fileList.size() - 1).file);
                        }
                    }

                    adapter = new ArrayAdapter<FileItem>(context,
                            android.R.layout.select_dialog_item, android.R.id.text1,
                            fileList) {

                        @NonNull
                        @Override
                        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                            // creates view
                            View view = super.getView(position, convertView, parent);
                            TextView textView = view.findViewById(android.R.id.text1);

                            // put the image on the text view
                            textView.setCompoundDrawablesWithIntrinsicBounds(
                                    fileList.get(position).icon, 0, 0, 0);

                            // add margin between image and text (support various screen
                            // densities)
                            int dp5 = (int) (5 * context.getResources().getDisplayMetrics().density + 0.5f);
                            textView.setCompoundDrawablePadding(dp5);

                            return view;
                        }
                    };
                } else {
                    Log.e(TAG, "fileList was not created");
                }
            } else {
                Log.e(TAG, "path does not exist");
            }
        } catch (SecurityException e) {
            Log.e(TAG, "unable to write on the sd card", e);
        }
    }

    /**
     * Creates and shows the dialog
     * @param context the context
     */
    private void generateDialog(final Context context) {
        if (fileList == null) {
            Log.e(TAG, "no files loaded");
        } else {
            new AlertDialog.Builder(context).setTitle(R.string.choose_file)
                    .setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int i) {
                            String chosenFile = fileList.get(i).file;
                            File sel = new File(path + "/" + chosenFile);
                            //'up' picked
                            if (!firstLvl && i == 0) {
                                // path modified to exclude present directory
                                path = new File(path.toString().substring(0,
                                        path.toString().lastIndexOf("/")));
                                fileList = null;

                                // if there are no more directories in the list, then
                                // it's the first level
                                if (path.equals(Environment.getExternalStorageDirectory())) {
                                    firstLvl = true;
                                }

                                loadFileList(context);
                                generateDialog(context);
                                Log.d(TAG, path.getAbsolutePath());
                                //directory picked
                            } else if (sel.isDirectory()) {
                                firstLvl = false;

                                // Adds chosen directory to list
                                path = new File(sel + "");
                                fileList = null;

                                loadFileList(context);
                                generateDialog(context);
                                Log.d(TAG, path.getAbsolutePath());
                                //file picked
                            } else {
                                try {
                                    String filePath = sel.toString();
                                    //get file extension
                                    String fileExtension = filePath.substring(filePath.lastIndexOf(".") + 1);

                                    //authenticate if supported fileFormat
                                    FileHandler.Format format;
                                    format = FileHandler.Format.fromExtension(fileExtension);
                                    if (format == null) {
                                        throw new Exception("Unknown file extension \"" + fileExtension + "\"");
                                    }

                                    if (fileSelectedListener != null) {
                                        fileSelectedListener.onFileSelected(filePath, format);
                                    }
                                } catch (Exception e) {
                                    Log.e(TAG, "error occurred while reading file", e);
                                    if (exceptionListener != null) {
                                        exceptionListener.onException(e);
                                    }
                                }
                            }
                        }
                    }).show();
        }
    }

    /**
     * A displayed file in the list
     */
    private class FileItem {
        private final String file;
        @DrawableRes
        private final int icon;

        FileItem(String file, @DrawableRes int icon) {
            this.file = file;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return file;
        }
    }
}
