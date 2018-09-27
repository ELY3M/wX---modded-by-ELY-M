/**
 * NOAA's National Climatic Data Center
 * NOAA/NESDIS/NCDC
 * 151 Patton Ave, Asheville, NC  28801
 *
 * THIS SOFTWARE AND ITS DOCUMENTATION ARE CONSIDERED TO BE IN THE
 * PUBLIC DOMAIN AND THUS ARE AVAILABLE FOR UNRESTRICTED PUBLIC USE.
 * THEY ARE FURNISHED "AS IS." THE AUTHORS, THE UNITED STATES GOVERNMENT, ITS
 * INSTRUMENTALITIES, OFFICERS, EMPLOYEES, AND AGENTS MAKE NO WARRANTY,
 * EXPRESS OR IMPLIED, AS TO THE USEFULNESS OF THE SOFTWARE AND
 * DOCUMENTATION FOR ANY PURPOSE. THEY ASSUME NO RESPONSIBILITY (1)
 * FOR THE USE OF THE SOFTWARE AND DOCUMENTATION; OR (2) TO PROVIDE
 * TECHNICAL SUPPORT TO USERS.
 */

package joshuatee.wx.radar;

import android.content.Context;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;

import joshuatee.wx.util.BZip2ReadException;
import joshuatee.wx.util.CBZip2InputStream;
import joshuatee.wx.util.UCARRandomAccessFile;
import joshuatee.wx.util.UtilityLog;

public class UtilityWXOGLPerfL2 {


    public static  int bzipwrapfull (Context c, String src_path, String dst_path, int product_code)
    {


        UCARRandomAccessFile dis;
        UCARRandomAccessFile dis2;

        try {

            dis = new UCARRandomAccessFile(c.getFileStreamPath(src_path).getAbsolutePath(), "r");
            dis.bigEndian = true;
            dis.seek(0);

          /*  Integer k = 0;
            for (k = 0; k < 8; k++) // 65 82 50 86 48 48 48  54 "AR2V0006" IOWA  MESO
            {
                Byte b = dis.readByte();
            }

            dis.skipBytes(4);
            int title_julianDay = dis.readInt(); // since 1/1/70
            int title_msecs = dis.readInt();
            dis.skipBytes(4);

            //see if we have to uncompress
            //if (dataFormat.equals(AR2V0001) || dataFormat.equals(AR2V0003)
            //       || dataFormat.equals(AR2V0004) || dataFormat.equals(AR2V0006)  || dataFormat.equals(AR2V0007) ) {
            dis.skipBytes(4);

            byte[] magic = new byte[3];
            magic[0] = 'B';
            magic[1] = 'Z';
            magic[2] = 'h';
            dis.skipBytes(2);*/

            dis2 = uncompress(c, dis, dst_path, product_code);
            dis.close();
            dis2.close();

        } catch (Exception e)
        {
            UtilityLog.HandleException(e);
        }

        // return full_read;
        return 0;
    }

    /**
     * Write equivilent uncompressed version of the file.
     *
     * @param inputRaf  file to uncompress
     * @param ufilename write to this file
     * @return raf of uncompressed file
     * @throws IOException on read error
     */
    private static UCARRandomAccessFile uncompress(Context c,
                                                   UCARRandomAccessFile inputRaf, String ufilename, int product_code
    ) throws IOException {

        UCARRandomAccessFile outputRaf = new UCARRandomAccessFile(new File(c.getFilesDir(),ufilename).getAbsolutePath(), "rw");
        outputRaf.bigEndian = true;

        int loop_cnt_break;
        if ( product_code == 153 )
            loop_cnt_break = 5; // was 6 when msg data was 1
        else
            loop_cnt_break = 11; // was 12

        int ref_decomp_size = 827040;
        int vel_decomp_size = 460800;

        int loop_cnt=0;

        try {
            inputRaf.seek(0);
            byte[] header = new byte[UtilityNexradLevel2Record.FILE_HEADER_SIZE];
            int bytesRead = inputRaf.read(header);
            if (bytesRead != header.length)
            {
                throw new IOException("Error reading NEXRAD2 header -- got " +
                        bytesRead + " rather than" + header.length);
            }
            outputRaf.write(header);

            boolean eof = false;
            int numCompBytes;
            byte[] ubuff = new byte[40000];
            byte[] obuff = new byte[40000];

            CBZip2InputStream cbzip2 = new CBZip2InputStream();
            ByteArrayInputStream bis=null;

            //Long byte_cnt = (long) 0;
            while (!eof) {
                try {
                    numCompBytes = inputRaf.readInt();
                    //byte_cnt++;
                    if (numCompBytes == -1) {
                        break;
                    }
                } catch (EOFException ee) {
                    Log.i("wx","got EOFException");
                    break; // assume this is ok
                }

               /* UtilityLog.d("wx","reading compressed bytes "
                        + numCompBytes + " input starts at "
                        + inputRaf.getFilePointer() + "; output starts at " + outputRaf.getFilePointer());
                UtilityLog.d("wx","decomp bytes " + Long.toString(byte_cnt));*/


				/*
				 * For some stupid reason, the last block seems to
				 * have the number of bytes negated.  So, we just
				 * assume that any negative number (other than -1)
				 * is the last block and go on our merry little way.
				 */
                if (numCompBytes < 0) {
                    numCompBytes = -numCompBytes;
                    eof = true;
                }
                byte[] buf = new byte[numCompBytes];
                inputRaf.readFully(buf);
                bis = new ByteArrayInputStream(buf, 2,
                        numCompBytes - 2);

                cbzip2 = new CBZip2InputStream(bis);

                int total = 0;
                int nread;

                try {
                    while ((nread = cbzip2.read(ubuff)) != -1) {
                        if (total + nread > obuff.length) {
                            byte[] temp = obuff;
                            obuff = new byte[temp.length * 2];
                            System.arraycopy(temp, 0, obuff, 0, temp.length);
                        }
                        System.arraycopy(ubuff, 0, obuff, total, nread);
                        total += nread;
                    }
                    if (obuff.length >= 0)
                        outputRaf.write(obuff, 0, total);

                } catch (BZip2ReadException ioe) {
                    Log.i("wx","Nexrad2IOSP.uncompress ");
                }

                /*float nrecords = (float) (total / 2432.0);
                UtilityLog.d("wx","  unpacked " + Integer.toString(total) + " num bytes " +
                        Float.toString(nrecords) + " records; ouput ends at " + Long.toString(outputRaf.getFilePointer()));*/

                if ( total == ref_decomp_size || total == vel_decomp_size)
                    loop_cnt++;

                if (loop_cnt>loop_cnt_break)  // 12 break if velocity has been decomp
                    break;

            }
            cbzip2.close();
            bis.close();
            outputRaf.flush();
        } catch (IOException e) {
            UtilityLog.HandleException(e);
        }
        return outputRaf;
    }
}
