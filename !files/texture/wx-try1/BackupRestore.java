package joshuatee.wx;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import androidx.core.content.ContextCompat;

/*
*
* Thanks to Joe Jurecka / Pykl3 for this!
*
*
* */


public class BackupRestore {
    private static String TAG = "joshuatee BackupRestore";
    private static boolean verbose = true;
    //private static String FilesPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wX/";


    //BACKUP
    public static void backupPrefs(Context context) {
        String error;
        Log.i(TAG, "File: "+context.getFilesDir().getPath());
        String fileCopiedToPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wXBackup/";
        //new File(fileCopiedToPath).mkdirs();
        //checking for perms before check dir//
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "storage perms are good!");
            ///}
            File dir = new File(fileCopiedToPath);
            if (!dir.exists()) {
                Log.i(TAG, "making backup dir");
                dir.mkdirs();
            }
        }
        File fileCopiedTo = new File(fileCopiedToPath + "preferenceBackup.xml");
        File fileCopiedFrom = new File(context.getFilesDir().getAbsolutePath() + "/../shared_prefs/" + context.getPackageName() + "_preferences.xml");
        Log.d(TAG, "First Trying  " + fileCopiedFrom.getAbsolutePath());
        if (!fileCopiedFrom.exists()) {
            fileCopiedFrom = new File("/dbdata/databases/" + context.getPackageName() + "/../shared_prefs/" + context.getPackageName() + "_preferences.xml");
            Log.d(TAG, "Next Trying  " + fileCopiedFrom.getAbsolutePath());
            if (!fileCopiedFrom.exists()) {
                DialogBox("Backup failed", Html.fromHtml("wX was unable to detect the location of your preferences on your device"), context);
                return;
            }
        }
        Log.d(TAG, "Android Preference File " + fileCopiedTo.getAbsolutePath());
        Log.d(TAG, "Where prefs copied to " + fileCopiedFrom.getAbsolutePath());
        try {
            FileChannel src = new FileInputStream(fileCopiedFrom).getChannel();
            FileChannel dst = new FileOutputStream(fileCopiedTo).getChannel();
            dst.transferFrom(src, 0, src.size());
            src.close();
            dst.close();
            File backupFilePath = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/wXBackup/");
            String pykl3path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wXBackup/";
            Log.d(TAG, "wXPATH=" + pykl3path);
            Log.d(TAG, "where backup files are written to " + backupFilePath.getAbsolutePath());

            DialogBox("Backup performed", Html.fromHtml("Backed up user preferences to " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/wXBackup/"), context);
            return;
        } catch (FileNotFoundException e) {
            error = e.getMessage();
            e.printStackTrace();
        } catch (IOException e2) {
            error = e2.getMessage();
            e2.printStackTrace();
        }
        DialogBox("Backup failed", Html.fromHtml("Failed to Back up user preferences to " + fileCopiedTo.getAbsolutePath() + " - " + error), context);
    }

    //RESTORE
    public static boolean restorePrefs(Context context) {
        String storageDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wXBackup/";
        File backupFile = new File(storageDirectory, "preferenceBackup.xml");
        String error = "";
        try {
            Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
            for (Node child = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new FileInputStream(backupFile)).getDocumentElement().getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child.getNodeType() == (short) 1) {
                    Element element = (Element) child;
                    String type = element.getNodeName();
                    String name = element.getAttribute("name");
                    if (type.equals("string")) {
                        editor.putString(name, element.getTextContent());
                    }
                    if (type.equals("float")) {
                        try {
                            editor.putFloat(name, Float.parseFloat(element.getAttribute("value").trim()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    if (type.equals("long")) {
                        try {
                            editor.putLong(name, Long.parseLong(element.getAttribute("value").trim()));
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                    if (type.equals("int")) {
                        try {
                            editor.putInt(name, Integer.parseInt(element.getAttribute("value").trim()));
                        } catch (Exception e22) {
                            e22.printStackTrace();
                        }
                    } else if (type.equals("boolean")) {
                        editor.putBoolean(name, element.getAttribute("value").equals("true"));
                    }
                }
            }
            editor.commit();


        } catch (FileNotFoundException e4) {
            e4.printStackTrace();
            DialogBox("Restore failed", Html.fromHtml("Failed to restore user prefs from " + backupFile.getAbsolutePath() + " - " + error), context);
            return false;
        } catch (ParserConfigurationException e5) {
            e5.printStackTrace();
            DialogBox("Restore failed", Html.fromHtml("Failed to restore user prefs from " + backupFile.getAbsolutePath() + " - " + error), context);
            return false;
        } catch (SAXException e6) {
            e6.printStackTrace();
            DialogBox("Restore failed", Html.fromHtml("Failed to restore user prefs from " + backupFile.getAbsolutePath() + " - " + error), context);
            return false;
        } catch (IOException e7) {
            e7.printStackTrace();
            DialogBox("Restore failed", Html.fromHtml("Failed to restore user prefs from " + backupFile.getAbsolutePath() + " - " + error), context);
            return false;
        }

        DialogBox("Next steps...", Html.fromHtml("Restored user preferences from " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/wXBackup/<br><br><b>Important:</b><br>- Please exit the program. <br>- After the first restart, the program will immediately close.  <br>- Then, restart the program a second time and the restore will be complete"), context);
        return true;
    }


    public static void DialogBox(String title, Spanned spanned, Context context) {
        if (verbose) {
            Log.i(TAG, "Displaying Dialog Box ");
            Log.i(TAG, "Title=" + title);
            Log.i(TAG, "Dialog BOX=" + spanned);
        }
        Builder builder = new Builder(context);
        builder.setTitle(title);
        builder.setIcon(R.drawable.wx);
        builder.setMessage(spanned).setCancelable(true).setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }
}