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

package joshuatee.wx.radar;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import joshuatee.wx.util.UtilityLog;

class UtilityWXOGLPerfL3FourBit {

    // Used for Legacy 4bit radar - only SRM
    // was decode4bit
    public static short Decode4Bit(Context c, String fn,  ByteBuffer radial_start, ByteBuffer bin_word)
    {

        DataInputStream dis = null;
        short number_of_range_bins=(short)0;

        try
        {
            FileInputStream fis = c.openFileInput(fn);
            dis = new DataInputStream(new BufferedInputStream(fis));

        }	  catch (Exception e) {
            UtilityLog.HandleException(e);
        }

        try {
            dis.skipBytes(50);

           /* final double		latitude_of_radar = dis.readInt() / 1000.0;
            final double        longitude_of_radar = dis.readInt() / 1000.0;
            final short        height_of_radar  = (short) dis.readUnsignedShort();
            final short product_code    = (short) dis.readUnsignedShort();
            final short        operational_mode = (short) dis.readUnsignedShort();*/
            dis.skipBytes(14);
            dis.skipBytes(6);

            //final short       volume_scan_date     = (short) dis.readUnsignedShort();
            //final int       volume_scan_time          = dis.readInt() ;
            dis.skipBytes(6);


            //final long sec = new Long (((volume_scan_date-1)*60*60*24) +( volume_scan_time)); // removed *1000
            //final long milli  = sec*1000;

            //Calendar cal = Calendar.getInstance();
            //cal.setTimeInMillis(milli);
            //java.util.Date d = cal.getTime();


            dis.skipBytes(6);
            dis.skipBytes(56); // 28 ushorts above ( x2 )
            dis.skipBytes(32);
            number_of_range_bins  = (short)dis.readUnsignedShort() ;
            dis.skipBytes(6);
            final int  number_of_radials  = dis.readUnsignedShort() ;
            int r;
            int[] number_of_rle_halfwords = new int[number_of_radials];
            radial_start.position(0);
            int s;
            short bin;
            int num_of_bins;
            int u;

            for ( r=0; r<360;r++) // change from number_of_radials to 360
            {
                number_of_rle_halfwords[r] = dis.readUnsignedShort();
                radial_start.putFloat((float) (450 - (dis.readUnsignedShort()/10)));
                dis.skipBytes(2);
                for ( s=0; s<number_of_rle_halfwords[r] * 2 ;s++)
                {
                    bin = (short)dis.readUnsignedByte();
                    num_of_bins = (bin >> 4);
                    for ( u=0;u<num_of_bins;u++)
                        bin_word.put((byte)(bin%16));
                }
            }
            dis.close();
        } catch (IOException e) {
            UtilityLog.HandleException(e);
        }
        return number_of_range_bins;
    }
}
