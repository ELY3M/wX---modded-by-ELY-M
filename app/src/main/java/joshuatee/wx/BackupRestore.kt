package joshuatee.wx

import android.app.AlertDialog.Builder
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.SharedPreferences.Editor
import android.content.pm.PackageManager
import android.os.Environment
import android.preference.PreferenceManager
import android.text.Html
import android.text.Spanned
import android.util.Log
import java.nio.channels.FileChannel
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.SAXException

import androidx.core.content.ContextCompat
import java.io.*
import java.nio.charset.Charset

/*
*
* Thanks to Joe Jurecka / Pykl3 for this!
*
*
* */


class BackupRestore {


        private val TAG = "joshuatee BackupRestore"
        private val verbose = true
        //private static String FilesPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/wX/";
        val backupFilePath = File(MyApplication.BackupFilesPath)
        //val pykl3path = Environment.getExternalStorageDirectory().absolutePath + "/wXBackup/"

        //BACKUP
        fun backupPrefs(context: Context) {
            val error: String?
            Log.i(TAG, "File: " + context.filesDir.path)
            //val fileCopiedToPath = Environment.getExternalStorageDirectory().absolutePath + "/wXBackup/"
            //new File(fileCopiedToPath).mkdirs();
            //checking for perms before check dir//
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "storage perms are good!")
                ///}
                val dir = File(MyApplication.BackupFilesPath)
                if (!dir.exists()) {
                    Log.i(TAG, "making backup dir")
                    dir.mkdirs()
                }

                val paldir = File(MyApplication.BackupPalFilesPath)
                if (!paldir.exists()) {
                    Log.i(TAG, "making backup pal dir")
                    paldir.mkdirs()
                }

                if (dir.exists()) {
                    backupfiles()
                }


            }



            val fileCopiedTo = File(MyApplication.BackupFilesPath + "preferenceBackup.xml")
            var fileCopiedFrom = File(context.filesDir.absolutePath + "/../shared_prefs/" + context.packageName + "_preferences.xml")
            Log.d(TAG, "First Trying  " + fileCopiedFrom.absolutePath)
            if (!fileCopiedFrom.exists()) {
                fileCopiedFrom = File("/dbdata/databases/" + context.packageName + "/../shared_prefs/" + context.packageName + "_preferences.xml")
                Log.d(TAG, "Next Trying  " + fileCopiedFrom.absolutePath)
                if (!fileCopiedFrom.exists()) {
                    DialogBox("Backup failed", Html.fromHtml("wX was unable to detect the location of your preferences on your device"), context)
                    return
                }
            }
            Log.d(TAG, "Android Preference File " + fileCopiedTo.absolutePath)
            Log.d(TAG, "Where prefs copied to " + fileCopiedFrom.absolutePath)
            try {
                val src = FileInputStream(fileCopiedFrom).channel
                val dst = FileOutputStream(fileCopiedTo).channel
                dst.transferFrom(src, 0, src.size())
                src.close()
                dst.close()
                //al backupFilePath = File(Environment.getExternalStorageDirectory().absolutePath + "/wXBackup/")
                //val pykl3path = Environment.getExternalStorageDirectory().absolutePath + "/wXBackup/"
                Log.d(TAG, "wXPATH=${MyApplication.BackupFilesPath}")
                Log.d(TAG, "where backup files are written to " + backupFilePath.absolutePath)
                DialogBox("Backup performed", Html.fromHtml("Backed up user preferences to " + MyApplication.BackupFilesPath), context)
                return
            } catch (e: FileNotFoundException) {
                error = e.message
                e.printStackTrace()
            } catch (e2: IOException) {
                error = e2.message
                e2.printStackTrace()
            }

            DialogBox("Backup failed", Html.fromHtml("Failed to Back up user preferences to " + fileCopiedTo.absolutePath + " - " + error), context)
        }

        //RESTORE
        fun restorePrefs(context: Context): Boolean {
            val backupFile = File(MyApplication.BackupFilesPath, "preferenceBackup.xml")
            val error = ""
            try {
                val editor = PreferenceManager.getDefaultSharedPreferences(context).edit()
                var child: Node? = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(FileInputStream(backupFile)).documentElement.firstChild
                while (child != null) {
                    if (child.nodeType == 1.toShort()) {
                        val element = child as Element?
                        val type = element!!.nodeName
                        val name = element.getAttribute("name")
                        if (type == "string") {
                            editor.putString(name, element.textContent)
                        }
                        if (type == "float") {
                            try {
                                editor.putFloat(name, java.lang.Float.parseFloat(element.getAttribute("value").trim { it <= ' ' }))
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }

                        }
                        if (type == "long") {
                            try {
                                editor.putLong(name, java.lang.Long.parseLong(element.getAttribute("value").trim { it <= ' ' }))
                            } catch (e2: Exception) {
                                e2.printStackTrace()
                            }

                        }
                        if (type == "int") {
                            try {
                                editor.putInt(name, Integer.parseInt(element.getAttribute("value").trim { it <= ' ' }))
                            } catch (e22: Exception) {
                                e22.printStackTrace()
                            }

                        } else if (type == "boolean") {
                            editor.putBoolean(name, element.getAttribute("value") == "true")
                        }
                    }
                    child = child.nextSibling
                }
                editor.commit()


            } catch (e4: FileNotFoundException) {
                e4.printStackTrace()
                DialogBox("Restore failed", Html.fromHtml("Failed to restore user prefs from " + backupFile.absolutePath + " - " + error), context)
                return false
            } catch (e5: ParserConfigurationException) {
                e5.printStackTrace()
                DialogBox("Restore failed", Html.fromHtml("Failed to restore user prefs from " + backupFile.absolutePath + " - " + error), context)
                return false
            } catch (e6: SAXException) {
                e6.printStackTrace()
                DialogBox("Restore failed", Html.fromHtml("Failed to restore user prefs from " + backupFile.absolutePath + " - " + error), context)
                return false
            } catch (e7: IOException) {
                e7.printStackTrace()
                DialogBox("Restore failed", Html.fromHtml("Failed to restore user prefs from " + backupFile.absolutePath + " - " + error), context)
                return false
            }

            restorefiles()

            DialogBox("Next steps...", Html.fromHtml("Restored user preferences from " + Environment.getExternalStorageDirectory().absolutePath + "/wXBackup/<br><br><b>Important:</b><br>- Please exit the program. <br>- After the first restart, the program will immediately close.  <br>- Then, restart the program a second time and the restore will be complete"), context)
            return true
        }


        fun DialogBox(title: String, spanned: Spanned, context: Context) {
            if (verbose) {
                Log.i(TAG, "Displaying Dialog Box ")
                Log.i(TAG, "Title=$title")
                Log.i(TAG, "Dialog BOX=$spanned")
            }
            val builder = Builder(context)
            builder.setTitle(title)
            builder.setIcon(R.drawable.wx)
            builder.setMessage(spanned).setCancelable(true).setPositiveButton("OK") { dialog, id -> dialog.cancel() }
            builder.create().show()
        }

    //backup our icons, pal files and stuff
    private fun backupfiles()
    {

        val s = File(MyApplication.FilesPath)
        val d = File(MyApplication.BackupFilesPath)
        s.listFiles().forEach {
            Log.i(TAG, "backing up: "+it.name+" to "+File(d.absolutePath).path+ '/' +it.name)
            it.copyRecursively(File(File(d.absolutePath).path + '/' + it.name), true)
        }

    }

    //restore our icons, pal files and stuff
    private fun restorefiles()
    {
        val s = File(MyApplication.BackupFilesPath)
        val d = File(MyApplication.FilesPath)
        s.listFiles().forEach {
            Log.i(TAG, "backing up: "+it.name+" to "+File(d.absolutePath).path+ '/' +it.name)
            it.copyRecursively(File(File(d.absolutePath).path + '/' + it.name), true)
        }


    }


}


