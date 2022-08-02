//modded by ELY M. 
//done by ELY M. 

package joshuatee.wx.settings

import android.content.Context
import android.content.pm.PackageManager
import android.os.Environment
import android.preference.PreferenceManager
import android.util.Log
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.parsers.ParserConfigurationException
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.SAXException
import androidx.core.content.ContextCompat
import joshuatee.wx.R
import joshuatee.wx.common.GlobalVariables
import joshuatee.wx.util.UtilityAlertDialog
import java.io.*


object UtilityBackupRestore {


        val backupFilePath = File(GlobalVariables.BackupFilesPath)
        //BACKUP
        fun backupPrefs(context: Context) {
            val error: String?
            Log.i("backuprestore", "File: " + context.filesDir.path)

            //checking for perms before check dir//
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.i("backuprestore", "storage perms are good!")
                val dir = File(GlobalVariables.BackupFilesPath)
                if (!dir.exists()) {
                    Log.i("backuprestore", "making backup dir")
                    dir.mkdirs()
                }

                val paldir = File(GlobalVariables.BackupPalFilesPath)
                if (!paldir.exists()) {
                    Log.i("backuprestore", "making backup pal dir")
                    paldir.mkdirs()
                }

                if (dir.exists()) {
                    backupfiles()
                }


            }



            val fileCopiedTo = File(GlobalVariables.BackupFilesPath + "preferenceBackup.xml")
            var fileCopiedFrom = File(context.filesDir.absolutePath + "/../shared_prefs/" + context.packageName + "_preferences.xml")
            Log.d("backuprestore", "First Trying  " + fileCopiedFrom.absolutePath)
            if (!fileCopiedFrom.exists()) {
                fileCopiedFrom = File("/dbdata/databases/" + context.packageName + "/../shared_prefs/" + context.packageName + "_preferences.xml")
                Log.d("backuprestore", "Next Trying  " + fileCopiedFrom.absolutePath)
                if (!fileCopiedFrom.exists()) {
                    UtilityAlertDialog.showDialogBox("Backup failed", R.drawable.wx,"wX was unable to detect the location of your preferences on your device", context)
                    return
                }
            }
            Log.d("backuprestore", "Android Preference File " + fileCopiedTo.absolutePath)
            Log.d("backuprestore", "Where prefs copied to " + fileCopiedFrom.absolutePath)
            try {
                val src = FileInputStream(fileCopiedFrom).channel
                val dst = FileOutputStream(fileCopiedTo).channel
                dst.transferFrom(src, 0, src.size())
                src.close()
                dst.close()
                Log.d("backuprestore", "wXPATH=${GlobalVariables.BackupFilesPath}")
                Log.d("backuprestore", "where backup files are written to " + backupFilePath.absolutePath)
                UtilityAlertDialog.showDialogBox("Backup Done", R.drawable.wx, "Backed up user preferences and files to " + GlobalVariables.BackupFilesPath, context)
                return
            } catch (e: FileNotFoundException) {
                error = e.message
                e.printStackTrace()
            } catch (e: IOException) {
                error = e.message
                e.printStackTrace()
            }

            UtilityAlertDialog.showDialogBox("Backup failed", R.drawable.wx, "Failed to Back up user preferences and files to " + fileCopiedTo.absolutePath + " - " + error, context)
        }

        //RESTORE
        fun restorePrefs(context: Context): Boolean {
            val backupFile = File(GlobalVariables.BackupFilesPath, "preferenceBackup.xml")
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


            } catch (e: FileNotFoundException) {
                e.printStackTrace()
                UtilityAlertDialog.showDialogBox("Restore failed", R.drawable.wx, "Failed to restore user prefs from " + backupFile.absolutePath + " - " + error, context)
                return false
            } catch (e: ParserConfigurationException) {
                e.printStackTrace()
                UtilityAlertDialog.showDialogBox("Restore failed", R.drawable.wx,"Failed to restore user prefs from " + backupFile.absolutePath + " - " + error, context)
                return false
            } catch (e: SAXException) {
                e.printStackTrace()
                UtilityAlertDialog.showDialogBox("Restore failed", R.drawable.wx,"Failed to restore user prefs from " + backupFile.absolutePath + " - " + error, context)
                return false
            } catch (e: IOException) {
                e.printStackTrace()
                UtilityAlertDialog.showDialogBox("Restore failed", R.drawable.wx,"Failed to restore user prefs from " + backupFile.absolutePath + " - " + error, context)
                return false
            }

            restorefiles()

            UtilityAlertDialog.showDialogBox("Restore Done", R.drawable.wx,"Restored user preferences and files from " + Environment.getExternalStorageDirectory().absolutePath + "/wXBackup/ Important: Please restart wX app. After wX app restart, the restore will be complete.", context)
            return true
        }


    //backup our icons, pal files and stuff
    private fun backupfiles()
    {

        val s = File(GlobalVariables.FilesPath)
        val d = File(GlobalVariables.BackupFilesPath)
        s.listFiles().forEach {
            Log.i("backuprestore", "backing up: "+it.name+" to "+File(d.absolutePath).path+ '/' +it.name)
            if (it.exists()) {
                Log.i("backuprestore", "File Exists: "+File(d.absolutePath).path+ '/' +it.name)
            } else {
                it.copyRecursively(File(File(d.absolutePath).path + '/' + it.name), true)
            }
            }

    }

    //restore our icons, pal files and stuff
    private fun restorefiles()
    {
        val s = File(GlobalVariables.BackupFilesPath)
        val d = File(GlobalVariables.FilesPath)
        s.listFiles().forEach {
            Log.i("backuprestore", "backing up: "+it.name+" to "+File(d.absolutePath).path+ '/' +it.name)
            it.copyRecursively(File(File(d.absolutePath).path + '/' + it.name), true)
        }


    }


}


