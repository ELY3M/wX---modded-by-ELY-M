/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801

 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */

@file:Suppress("SpellCheckingInspection")

package joshuatee.wx.radar

import android.content.Context
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.EOFException
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import joshuatee.wx.util.UCARRandomAccessFile
import joshuatee.wx.util.UtilityIO
import joshuatee.wx.util.UtilityLog
import joshuatee.wx.externalBzip2.CBZip2InputStream

internal object NexradLevel2Util {

    /**
     * Size of the file header, aka title
     */
    private const val FILE_HEADER_SIZE = 24

    fun writeDecodedFile(context: Context, fileName: String, radialStart: ByteBuffer, binWord: ByteBuffer, days: ByteBuffer, milliSeconds: ByteBuffer) {
        radialStart.position(0)
        binWord.position(0)
        days.position(0)
        milliSeconds.position(0)
        try {
            val fos = context.openFileOutput(fileName, Context.MODE_PRIVATE)
            val wChannel = fos.channel
            while (days.hasRemaining()) {
                wChannel.write(days)
            }
            while (milliSeconds.hasRemaining()) {
                wChannel.write(milliSeconds)
            }
            while (radialStart.hasRemaining()) {
                wChannel.write(radialStart)
            }
            while (binWord.hasRemaining()) {
                wChannel.write(binWord)
            }
            wChannel.close()
            fos.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    fun readDecodedFile(context: Context, fileName: String, radialStart: ByteBuffer, binWord: ByteBuffer, days: ByteBuffer, milliSeconds: ByteBuffer) {
        radialStart.position(0)
        binWord.position(0)
        days.position(0)
        milliSeconds.position(0)
        try {
            val file = File(context.filesDir, fileName)
            val rChannel = FileInputStream(file).channel
            while (days.hasRemaining()) {
                rChannel.read(days)
            }
            while (milliSeconds.hasRemaining()) {
                rChannel.read(milliSeconds)
            }
            while (radialStart.hasRemaining()) {
                rChannel.read(radialStart)
            }
            while (binWord.hasRemaining()) {
                rChannel.read(binWord)
            }
            rChannel.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        }
    }

    fun decompress(context: Context, srcPath: String, dstPath: String, productCode: Int) {
        try {
            val ucarRandomAccessFile = UCARRandomAccessFile(UtilityIO.getFilePath(context, srcPath))
            ucarRandomAccessFile.bigEndian = true
            ucarRandomAccessFile.seek(0)
            val dis2 = uncompress(context, ucarRandomAccessFile, dstPath, productCode)
            ucarRandomAccessFile.close()
            dis2.close()
        } catch (e: Exception) {
            UtilityLog.handleException(e)
        } catch (ooo: OutOfMemoryError) {
            UtilityLog.handleException(ooo)
        }
    }

    /**
     * Write equivalent uncompressed version of the file.
     * @param inputRaf  file to uncompress
     * *
     * @param ufilename write to this file
     * *
     * @return raf of uncompressed file
     * *
     * @throws IOException on read error
     */
    @Throws(IOException::class)
    private fun uncompress(context: Context, inputRaf: UCARRandomAccessFile, ufilename: String, productCode: Int): UCARRandomAccessFile {
        val outputRaf = UCARRandomAccessFile(File(context.filesDir, ufilename).absolutePath, "rw")
        outputRaf.bigEndian = true
        val loopCntBreak = if (productCode == 153) {
            5
        } else {
            11
        }
        val refDecompSize = 827040
        val velDecompSize = 460800
        var loopCnt = 0
        try {
            inputRaf.seek(0)
            val header = ByteArray(FILE_HEADER_SIZE)
            val bytesRead = inputRaf.read(header)
            if (bytesRead != header.size) {
                throw IOException("Error reading NEXRAD2 header -- got " + bytesRead + " rather than" + header.size)
            }
            outputRaf.write(header)
            var eof = false
            var numCompBytes: Int
            val ubuff = ByteArray(100000)
            var obuff = ByteArray(100000)
            var bis: ByteArrayInputStream? = null
            while (!eof) {
                try {
                    numCompBytes = inputRaf.readInt()
                    if (numCompBytes == -1) {
                        break
                    }
                } catch (ee: EOFException) {
                    Log.i("wx", "got EOFException")
                    break // assume this is ok
                }
                /*
				 * For some reason, the last block seems to
				 * have the number of bytes negated.  So, we just
				 * assume that any negative number (other than -1)
				 */
                if (numCompBytes < 0) {
                    numCompBytes = -numCompBytes
                    eof = true
                }
                val buf = ByteArray(numCompBytes)
                inputRaf.readFully(buf)
                bis = ByteArrayInputStream(buf, 2, numCompBytes - 2)
                val cbzip2 = CBZip2InputStream(bis)
                var total = 0
                var nread: Int
                try {
                    nread = cbzip2.read(ubuff)
                    while (nread != -1) {
                        if (total + nread > obuff.size) {
                            val temp = obuff
                            obuff = ByteArray(temp.size * 2)
                            System.arraycopy(temp, 0, obuff, 0, temp.size)
                        }
                        System.arraycopy(ubuff, 0, obuff, total, nread)
                        total += nread
                        nread = cbzip2.read(ubuff)
                    }
//                    if (obuff.size >= 0) {
                    outputRaf.write(obuff, 0, total)
//                    }
                } catch (e: Exception) {
                    UtilityLog.handleException(e)
                }
                if (total == refDecompSize || total == velDecompSize) {
                    loopCnt += 1
                }
                if (loopCnt > loopCntBreak) {
                    break
                }
                cbzip2.close()
            }
            bis?.close()
            outputRaf.flush()
        } catch (e: IOException) {
            UtilityLog.handleException(e)
        }
        return outputRaf
    }
}
