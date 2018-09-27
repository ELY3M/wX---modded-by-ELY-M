/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018  joshua.tee@gmail.com

    This file is part of wX.

    wX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    wX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with wX.  If not, see <http://www.gnu.org/licenses/>.

 */

package joshuatee.wx;

import java.io.FileOutputStream;
import java.io.InputStream;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import android.content.Context;

public class UtilityFTP {

	/*
	public static void GetNids(Context c, String url,String path) {

		int TENSECONDS  = 10*1000;
		int THIRTYSECONDS = 30*1000;

		// timeouts added May 2015

		int retry=3;
		int sleepTime=0;
		int backoff = 2000;
		if(url.equals(""))
		{
			return;
		}

		for (int i=0;i<retry;i++)
		{
			try {
				FTPClient ftp = new FTPClient();

				//String url = "tgftp.nws.noaa.gov";
				//String user = "ftp";
				//String pass = "anonymous";

				ftp.setConnectTimeout(TENSECONDS*2);
				ftp.setDefaultTimeout(TENSECONDS*2);
				ftp.connect(url);

				//30 seconds to log on.  Also 30 seconds to change to workingdirectory.
				ftp.setSoTimeout(THIRTYSECONDS);

				if (!ftp.login("ftp", "anonymous")) {
					ftp.logout();
				}
				ftp.setFileType(FTP.BINARY_FILE_TYPE);
				int reply = ftp.getReplyCode();
				if (!FTPReply.isPositiveCompletion(reply)) {
					ftp.disconnect();
				}
				ftp.enterLocalPassiveMode();

				// FIX ME attempted optimization
				//ftp.changeWorkingDirectory(path);

				FileOutputStream fos = c.openFileOutput("nids",Context.MODE_PRIVATE);


				//ftp.retrieveFile("sn.last", fos);

				ftp.retrieveFile(path + "/" + "sn.last", fos);
				//Log.i("wx", path + "/" + "sn.last");



				fos.close();
				ftp.logout();
				ftp.disconnect();
				return;

			} catch (Exception ex) {
				//ex.printStackTrace();
				backoff *= 2;
				sleepTime = backoff  + (int)(Math.random()*backoff);
				try { Thread.sleep(sleepTime); } catch (Exception ex2) { }
				continue;
			}
		}
	}
	*/


	/*public static void GetNids(Context c, String url,String path) {

		int TENSECONDS  = 10*1000;
		int THIRTYSECONDS = 30*1000;

		// timeouts added May 2015

		try {
			FTPClient ftp = new FTPClient();

			//String url = "tgftp.nws.noaa.gov";
			//String user = "ftp";
			//String pass = "anonymous";

			ftp.setConnectTimeout(TENSECONDS*2);
			ftp.setDefaultTimeout(TENSECONDS*2);

			ftp.connect(url);

			//30 seconds to log on.  Also 30 seconds to change to working directory.
			ftp.setSoTimeout(THIRTYSECONDS);

			if (!ftp.login("ftp", "anonymous")) {
				ftp.logout();
			}

			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			int reply = ftp.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
			}

			ftp.enterLocalPassiveMode();
			ftp.changeWorkingDirectory(path);
			//reply = ftp.getReplyCode();
			//String fn = "sn.last";

			FileOutputStream fos = c.openFileOutput("nids", Context.MODE_PRIVATE);
			ftp.retrieveFile("sn.last", fos);
			//reply = ftp.getReplyCode();
			fos.close();

			ftp.logout();
			ftp.disconnect();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	 */

	/*
	public static String[] GetNidsArrOLD(Context c, String url,String path, String frame_cnt_str) {

		int frame_cnt = Integer.parseInt(frame_cnt_str);
		String[] nids_arr = new String[frame_cnt];

		int TENSECONDS  = 10*1000;
		int THIRTYSECONDS = 30*1000;

		// timeouts added May 2015

		try {
			FTPClient ftp = new FTPClient();

			ftp.setConnectTimeout(TENSECONDS);
			ftp.setDefaultTimeout(TENSECONDS);

			//String user = "ftp";
			//String pass = "anonymous";

			ftp.connect(url);

			//30 seconds to log on.  Also 30 seconds to change to working directory.
			ftp.setSoTimeout(THIRTYSECONDS);

			if (!ftp.login("ftp", "anonymous")) {
				ftp.logout();
			}

			ftp.setFileType(FTP.BINARY_FILE_TYPE);
			int reply = ftp.getReplyCode();

			if (!FTPReply.isPositiveCompletion(reply)) {
				ftp.disconnect();
			}

			ftp.enterLocalPassiveMode();
			ftp.changeWorkingDirectory(path);
			//reply = ftp.getReplyCode();

			FTPFile[] ftpFiles = ftp.listFiles();

			//get newest .xml file name from ftp server
			java.util.Date lastMod = ftpFiles[0].getTimestamp().getTime();
			FTPFile choice = ftpFiles[0];

			for (FTPFile file : ftpFiles) {
				if (file.getTimestamp().getTime().after(lastMod) && ! file.getName().equals("sn.last")) {
					choice = file;
					lastMod = file.getTimestamp().getTime();
				}
			}

			int seq = Integer.parseInt(choice.getName().replace("sn.","")); // was ALl
			int j=0;
			int k = seq - frame_cnt+1;
			for ( j = 0; j < frame_cnt ; j++ )
			{
				// files range from 0000 to 0250, if num is negative add 251
				int tmp_k = k;

				if (tmp_k<0)
					tmp_k = tmp_k + 251;

				nids_arr[j] = "sn." + String.format("%4s",Integer.toString(tmp_k)).replace(' ','0');
				k++;
			}


			FileOutputStream fos;
			for ( j = 0; j < frame_cnt ; j++ )
			{
				fos = c.openFileOutput(nids_arr[j], Context.MODE_PRIVATE);
				ftp.retrieveFile(nids_arr[j], fos);
				fos.close();
			}


			ftp.logout();
			ftp.disconnect();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return nids_arr;

	} */



}
