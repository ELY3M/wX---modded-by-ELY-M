/*

    Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019  joshua.tee@gmail.com

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
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;

import joshuatee.wx.util.UCARRandomAccessFile;
import joshuatee.wx.util.UtilityLog;
import joshuatee.wx.util.bzip2.Compression;

class UtilityWXOGLPerf {

    //private final static float  PI = (float)Math.PI;

    private final static float  M_180_div_PI = (float)(180.0/Math.PI);
    private final static float M_PI_div_4=(float)(Math.PI/4.0);
    private final static float M_PI_div_360=(float)(Math.PI/360.0);
    private final static float twicePi = (float)(2.0f * Math.PI);

    public static int Decode8BitAndGenRadials (Context c, String src,
                                               ByteBuffer rad_buff, ByteBuffer color_buff, float bin_size, int bg_color,
                                               ByteBuffer colormap_r, ByteBuffer colormap_g, ByteBuffer colormap_b)
    {
        UCARRandomAccessFile dis = null;
        int total_bins = 0;
        DataInputStream dis2=null;

        try
        {
            dis = new UCARRandomAccessFile(c.getFileStreamPath(src).getAbsolutePath(), "r");
            dis.bigEndian = true;

        } catch (Exception e) {
            UtilityLog.HandleException(e);
        }

        try {

            // ADVANCE PAST WMO HEADER
            while (dis.readShort() != -1) {
                // while (dis.readUnsignedShort() != 16) {
            }

            // the following chunk was added to analyze the header so that status info could be extracted
            // index 4 is radar height
            // index 0,1 is lat as Int
            // index 2,3 is long as Int

            /*final double latitude_of_radar = dis.readInt() / 1000.0;
            final double longitude_of_radar = dis.readInt() / 1000.0;
            final short height_of_radar = (short) dis.readUnsignedShort();
            final short product_code = (short) dis.readUnsignedShort();
            final short operational_mode = (short) dis.readUnsignedShort();
            final short volume_scan_pattern = (short) dis.readUnsignedShort();
            final short sequence_number = (short) dis.readUnsignedShort();
            final short volume_scan_number = (short) dis.readUnsignedShort();
            final short volume_scan_date = (short) dis.readUnsignedShort();
            final Integer volume_scan_time = dis.readInt();*/

            dis.skipBytes(26); // 3 int ( 4 byte ) and 7 short ( 2 byte ) == 12 + 14

            dis.skipBytes(74);

            byte[] magic = new byte[3];
            magic[0] = 'B';
            magic[1] = 'Z';
            magic[2] = 'h';

            Compression compression = Compression.getCompression(magic);

            long compressedFileSize = dis.length() - dis.getFilePointer();
            byte[] buf = new byte[(int) compressedFileSize];

            dis.read(buf);
            dis.close();

            InputStream decompStream = compression.decompress(new ByteArrayInputStream(buf));
            dis2 = new DataInputStream(new BufferedInputStream(decompStream));

            /*short blockDivider = dis2.readShort();
            short blockID = dis2.readShort();
            int blockLen = dis2.readInt();
            short numLayers = dis2.readShort();*/

            dis2.skipBytes(10); // 3 short , 1 int == 10

            dis2.skipBytes(6);

            //int packetCode = dis2.readUnsignedShort();
            //int  index_of_first_range_bin  = dis2.readUnsignedShort();
            dis2.skipBytes(4);

            //number_of_range_bins = dis2.readUnsignedShort();
            // int   i_center_of_sweep   = dis2.readShort() ;
            // int  j_center_of_sweep   = dis2.readShort() ;
            // int  scale_factor  = dis2.readUnsignedShort() ;
            //int number_of_radials = dis2.readUnsignedShort();
            dis2.skipBytes(10);
        } catch (Exception e)
        {
            UtilityLog.HandleException(e);
        }

        int r;
        int number_of_rle_halfwords=0;
        colormap_r.put(0, (byte) bg_color);
        colormap_g.put(0, (byte) bg_color);
        colormap_b.put(0, (byte) bg_color);
        rad_buff.position(0);
        color_buff.position(0);

        float angle=0f;  // this and one below were init assigned 0.0f
        float angle_v;
        byte level;
        int level_count;
        float bin_start;  // was init assigned 0.0f
        int bin;
        int color_for;
        int c_i = 0;
        int r_i = 0;
        byte cur_level=(byte)0;

        float angle_sin;
        float angle_cos;
        float angle_v_sin;
        float angle_v_cos;

        float angle_next=0f;
        float angle_0=0f;

        for ( r=0; r<360;r++) // was number_of_radials
        {
            try {
                number_of_rle_halfwords = dis2.readUnsignedShort();
                angle =  (450f - (dis2.readUnsignedShort() / 10f));
                dis2.skipBytes(2);

                if (r<359) {
                    dis2.mark(100000);
                    dis2.skipBytes(number_of_rle_halfwords + 2);
                    angle_next = (450f - (dis2.readUnsignedShort() / 10f));
                    dis2.reset();
                }
            } catch (Exception e)
            {
                UtilityLog.HandleException(e);
            }

            //level=(byte)(0 & 0xFF);
            level=(byte)(0);
            level_count = 0;
            bin_start = bin_size;

            if (r==0)
                angle_0=angle;

            if (r<359)
                angle_v = angle_next;
            else
                angle_v = angle_0;

            for ( bin=0; bin<number_of_rle_halfwords ;bin++)
            {
                try {
                    cur_level = (byte) (dis2.readUnsignedByte() & 0xFF);
                } catch ( Exception e) {
                    UtilityLog.HandleException(e);
                }

                if (bin==0)
                    level=cur_level;

                if ( cur_level == level )
                    level_count++; // was ++level_count for some reason
                else {

                    angle_v_cos = (float)Math.cos((angle_v)/M_180_div_PI);
                    angle_v_sin = (float)Math.sin((angle_v)/M_180_div_PI);

                    rad_buff.putFloat(r_i,(bin_start * angle_v_cos));
                    r_i += 4;
                    rad_buff.putFloat(r_i,bin_start * angle_v_sin );
                    r_i += 4;

                    rad_buff.putFloat(r_i,(bin_start + ( bin_size * level_count )) * angle_v_cos);
                    r_i += 4;
                    rad_buff.putFloat(r_i,(bin_start + ( bin_size * level_count )) * angle_v_sin);
                    r_i += 4;

                    angle_cos=(float)Math.cos(angle/M_180_div_PI);
                    angle_sin=(float)Math.sin(angle/M_180_div_PI);

                    rad_buff.putFloat(r_i,(bin_start + ( bin_size * level_count)) * angle_cos);
                    r_i += 4;
                    rad_buff.putFloat(r_i,(bin_start + ( bin_size * level_count)) * angle_sin);
                    r_i += 4;

                    rad_buff.putFloat(r_i,bin_start * angle_cos);
                    r_i += 4;
                    rad_buff.putFloat(r_i,bin_start * angle_sin);
                    r_i += 4;

                    for ( color_for = 0; color_for<4;color_for++ )
                    {
                        /*color_buff.put(c_i,(colormap_r.get(level& 0xFF)));
                        c_i++;
                        color_buff.put(c_i,(colormap_g.get(level& 0xFF)));
                        c_i++;
                        color_buff.put(c_i,(colormap_b.get(level& 0xFF)));
                        c_i++;*/

                        color_buff.put(c_i++,(colormap_r.get(level& 0xFF)));
                        color_buff.put(c_i++,(colormap_g.get(level& 0xFF)));
                        color_buff.put(c_i++,(colormap_b.get(level& 0xFF)));

                    }
                    //++total_bins;
                    total_bins++;
                    level = cur_level;
                    bin_start   = bin * bin_size;
                    level_count = 1;
                }
            } // end loop over bins in one radial
        }

        try {
            dis2.close();
        }catch(Exception e)
        {
            UtilityLog.HandleException(e);
        }

        // support tilts above first
        //if ( number_of_range_bins % 2 != 0 )
        //    number_of_range_bins++;

        return total_bins;
    }

    public static  int GenRadials(ByteBuffer rad_buff, ByteBuffer color_buff, ByteBuffer bin_buff,
                                  ByteBuffer radial_start, int number_of_radials, int num_range_bins, float bin_size, int bg_color,
                                  ByteBuffer colormap_r, ByteBuffer colormap_g, ByteBuffer colormap_b)

    {
        colormap_r.put(0,(byte)bg_color);
        colormap_g.put(0,(byte)bg_color);
        colormap_b.put(0,(byte)bg_color);

        int total_bins =0;
        int g;
        float angle;  // this and one below were init assigned 0.0f
        float angle_v;
        int level;
        int level_count;
        float bin_start;  // was init assigned 0.0f
        int bin;
        int color_for;
        int b_i=0;
        int c_i=0;
        int r_i=0;
        int cur_level;

        float angle_sin;
        float angle_cos;
        float angle_v_sin;
        float angle_v_cos;

        for (g=0;g<number_of_radials;g++)
        {
            angle = radial_start.getFloat(g*4);
            level =  bin_buff.get(b_i);
            level_count = 0;
            bin_start = bin_size;

            if (g<(number_of_radials-1))
                angle_v = radial_start.getFloat(g*4+4);
            else
                angle_v = radial_start.getFloat(0);

            for  (bin=0;bin<num_range_bins;bin++ ) {

                cur_level = bin_buff.get(b_i);
                b_i++;

                if ( cur_level == level )
                    level_count++;
                else {
                    angle_v_cos = (float)Math.cos((angle_v)/M_180_div_PI);
                    angle_v_sin = (float)Math.sin((angle_v)/M_180_div_PI);

                    rad_buff.putFloat(r_i,(bin_start * angle_v_cos));
                    r_i += 4;
                    rad_buff.putFloat(r_i,bin_start * angle_v_sin );
                    r_i += 4;

                    rad_buff.putFloat(r_i,(bin_start + ( bin_size * level_count )) * angle_v_cos);
                    r_i += 4;
                    rad_buff.putFloat(r_i,(bin_start + ( bin_size * level_count )) * angle_v_sin);
                    r_i += 4;

                    angle_cos=(float)Math.cos(angle/M_180_div_PI);
                    angle_sin=(float)Math.sin(angle/M_180_div_PI);

                    rad_buff.putFloat(r_i,(bin_start + ( bin_size * level_count)) * angle_cos);
                    r_i += 4;
                    rad_buff.putFloat(r_i,(bin_start + ( bin_size * level_count)) * angle_sin);
                    r_i += 4;

                    rad_buff.putFloat(r_i,bin_start * angle_cos);
                    r_i += 4;
                    rad_buff.putFloat(r_i,bin_start * angle_sin);
                    r_i += 4;

                    for ( color_for = 0; color_for<4;color_for++ )
                    {
                        color_buff.put(c_i++,(colormap_r.get(level& 0xFF)));
                        color_buff.put(c_i++,(colormap_g.get(level& 0xFF)));
                        color_buff.put(c_i++,(colormap_b.get(level& 0xFF)));
                    }
                    total_bins++;
                    level = cur_level;
                    bin_start   = bin * bin_size;
                    level_count = 1;
                }
            }
        }
        return total_bins;
    }

    public static void GenMercato (ByteBuffer in_buff, ByteBuffer out_buff, float center_x, float center_y, float x_image_center_pixels,
                                   float y_image_center_pixels,  float one_degree_scale_factor, int count)
    {
        for (int i_count = 0; i_count < count; i_count = i_count + 2)
        {
            out_buff.putFloat(i_count*4+4,  -1.0f *( -(((M_180_div_PI * (float)Math.log(Math.tan(M_PI_div_4+in_buff.getFloat(i_count*4)*M_PI_div_360)))
                    - (M_180_div_PI * (float)Math.log(Math.tan(M_PI_div_4+center_x*M_PI_div_360)))) *  one_degree_scale_factor ) + y_image_center_pixels));
            out_buff.putFloat(i_count*4 , -((in_buff.getFloat(i_count*4+4) - center_y ) * one_degree_scale_factor ) + x_image_center_pixels);
        }
    }

    public static void GenIndex (ByteBuffer index_buff, int len, int break_size)
    {
        int incr;
        int remainder;
        int chunk_count = 1;
        int i_count=0;

        if (len<break_size)
        {
            break_size=len;
            remainder = break_size;
        } else {
            chunk_count = len/break_size;
            remainder = len - break_size*chunk_count;
            chunk_count++;
        }

        int chunk_index;
        int j;

        for (chunk_index=0;chunk_index<chunk_count;chunk_index++)
        {
            incr=0;
            if (chunk_index == (chunk_count-1))
                break_size = remainder;

            for (j=0;j<break_size;j++)
            {
                index_buff.putShort(i_count,(short)(incr));
                i_count += 2;
                index_buff.putShort(i_count,(short)(1 +incr));
                i_count += 2;
                index_buff.putShort(i_count,(short)(2 +incr));
                i_count += 2;
                index_buff.putShort(i_count,(short)(incr));
                i_count += 2;
                index_buff.putShort(i_count,(short)(2 +incr));
                i_count += 2;
                index_buff.putShort(i_count,(short)(3 +incr));
                i_count += 2;

                incr += 4;
            }
        }
    }

    public static void GenIndexLine (ByteBuffer index_buff, int len, int break_size)
    {
        int incr;
        int remainder;
        int chunk_count = 1;
        int total_bins = len/4;
        int i_count=0;

        if (total_bins<break_size)
        {
            break_size=total_bins;
            remainder = break_size;
        } else {
            chunk_count = total_bins/break_size;
            remainder = total_bins - break_size*chunk_count;
            chunk_count++;
        }

        int j;
        index_buff.position(0);
        for (int chunk_index=0;chunk_index<chunk_count;chunk_index++)
        {
            incr=0;
            if (chunk_index == (chunk_count-1))
                break_size = remainder;

            for (j=0;j<break_size;j++)
            {
                index_buff.putShort(i_count,(short)(incr));
                i_count +=2;
                index_buff.putShort(i_count,(short)(1 +incr));
                i_count +=2;

                incr += 2;
            }
        }
    }

    public static void GenTriangle (ByteBuffer loc_buff, ByteBuffer index_buff,
                                    float center_x, float center_y, float x_image_center_pixels,
                                    float y_image_center_pixels,  float one_degree_scale_factor,
                                    double[] x,double[] y,int count,float len)
    {
        double point_x;
        double point_y;
        float pix_y_d;
        float pix_x_d;
        int i_count;
        int ix_count=0;
        float test1;
        float test2;

        loc_buff.position(0);
        index_buff.position(0);

        for (i_count = 0; i_count < count; i_count++)
        {
            point_x = x[i_count];
            point_y = y[i_count];

            test1 = M_180_div_PI * (float)Math.log(Math.tan(M_PI_div_4 + point_x*M_PI_div_360)) ;
            test2 = M_180_div_PI * (float)Math.log(Math.tan(M_PI_div_4 + center_x*M_PI_div_360));

            pix_y_d =  -((test1 - test2) *  one_degree_scale_factor ) + y_image_center_pixels;
            pix_x_d = (float)( -((point_y - center_y ) * one_degree_scale_factor ) + x_image_center_pixels);

            loc_buff.putFloat(pix_x_d);
            loc_buff.putFloat( -pix_y_d);

            loc_buff.putFloat(pix_x_d - len);
            loc_buff.putFloat(-pix_y_d + len);

            loc_buff.putFloat(pix_x_d + len);
            loc_buff.putFloat( -pix_y_d + len);

            index_buff.putShort((short)ix_count);
            index_buff.putShort((short)(ix_count+1));
            index_buff.putShort((short)(ix_count+2));

            ix_count +=  3;
        }
    }

    public static void GenCircle (ByteBuffer loc_buff,  ByteBuffer index_buff,
                                  float center_x, float center_y, float x_image_center_pixels,
                                  float y_image_center_pixels,
                                  float one_degree_scale_factor,double[] x,double[] y,int count,
                                  float len,  int triangleAmount)
    {
        double point_x;
        double point_y;
        float pix_y_d;
        float pix_x_d;
        int i_count;
        int ix_count=0;
        float test1;
        float test2;
        len = len * 0.50f;
        int i_i=0;
        int l_i=0;

        for (i_count = 0; i_count < count; i_count++)
        {
            point_x = x[i_count];
            point_y = y[i_count];

            test1 = M_180_div_PI * (float)Math.log(Math.tan( M_PI_div_4 + point_x * M_PI_div_360));
            test2 = M_180_div_PI * (float)Math.log(Math.tan( M_PI_div_4 + center_x * M_PI_div_360));

            pix_y_d =  -((test1 - test2) *  one_degree_scale_factor ) + y_image_center_pixels;
            pix_x_d =  (float)(-((point_y - center_y ) * one_degree_scale_factor ) + x_image_center_pixels);

            for(int i = 0; i < triangleAmount;i++)
            {
                loc_buff.putFloat(l_i, pix_x_d);
                l_i += 4;
                loc_buff.putFloat(l_i,  -pix_y_d);
                l_i += 4;

                loc_buff.putFloat(l_i,  pix_x_d + (len * (float)Math.cos(i *  twicePi / triangleAmount)));
                l_i += 4;
                loc_buff.putFloat(l_i,  -pix_y_d  + (len * (float)Math.sin(i * twicePi / triangleAmount)));
                l_i += 4;

                loc_buff.putFloat(l_i,  pix_x_d + (len * (float)Math.cos((i+1) *  twicePi / triangleAmount)));
                l_i += 4;
                loc_buff.putFloat(l_i, -pix_y_d  + (len * (float)Math.sin((i+1) * twicePi / triangleAmount)));
                l_i += 4;

                index_buff.putShort(i_i,(short)(ix_count));
                i_i += 2;
                index_buff.putShort(i_i,(short)(ix_count+1));
                i_i += 2;
                index_buff.putShort(i_i,(short)( ix_count+2));
                i_i += 2;

                ix_count +=  3;
            }
        }
    }

    public static void GenCircleLocdot (ByteBuffer loc_buff, ByteBuffer index_buff,
                                        float center_x, float center_y, float x_image_center_pixels,
                                        float y_image_center_pixels,
                                        float one_degree_scale_factor,double x,double y,float len, int triangleAmount)
    {
        loc_buff.position(0);
        index_buff.position(0);

        float pix_y_d;
        float pix_x_d;
        int ix_count=0;
        float test1;
        float test2;
        // controls diametar of circle
        len = len * 2f;

        test1 = M_180_div_PI * (float)Math.log(Math.tan(M_PI_div_4 + x * M_PI_div_360));
        test2 = M_180_div_PI * (float)Math.log(Math.tan(M_PI_div_4 + center_x * M_PI_div_360));

        pix_y_d =  -((test1 - test2) *  one_degree_scale_factor ) + y_image_center_pixels;
        pix_x_d =  (float)(-((y - center_y ) * one_degree_scale_factor ) + x_image_center_pixels);

        for( int  i = 0; i < triangleAmount;i++)
        {
            loc_buff.putFloat(  pix_x_d + (len * (float)Math.cos(i *  twicePi / triangleAmount)));
            loc_buff.putFloat(  -pix_y_d  + (len * (float)Math.sin(i * twicePi / triangleAmount)));

            loc_buff.putFloat(  pix_x_d + (len * (float)Math.cos((i+1) *  twicePi / triangleAmount)));
            loc_buff.putFloat( -pix_y_d  + (len * (float)Math.sin((i+1) * twicePi / triangleAmount)));

            index_buff.putShort((short)(ix_count));
            index_buff.putShort((short)(ix_count+1));

            ix_count +=  2;
        }
    }

    /*public static short ReadDecodedFile(Context c, String fn, ByteBuffer radial_start,  ByteBuffer bin_word)
    {

        // first 1440 bytes for radial angle ( 4*360 )
        // next 500400 for bin_word (360*1390)

        radial_start.position(0);
        bin_word.position(0);
        ByteBuffer nrb =  ByteBuffer.allocate(4);
        nrb.position(0);
        try
        {

            File file  = new File(c.getFilesDir(),fn);
            FileChannel rChannel = new FileInputStream(file).getChannel();

            int numOfBytesRead=0;
            while(nrb.hasRemaining()) {
                numOfBytesRead = rChannel.read(nrb);
            }
            while(radial_start.hasRemaining()) {
                numOfBytesRead = rChannel.read(radial_start);
            }
            while(bin_word.hasRemaining()) {
                numOfBytesRead = rChannel.read(bin_word);

            }
            rChannel.close();
        } catch (Exception e) {
            UtilityLog.HandleException(e);
        }
        nrb.position(0);
        return (short)nrb.getInt();
    }*/

  /*  public static void WriteDecodedFile(Context c, String fn, int num_range_bins, ByteBuffer radial_start,  ByteBuffer bin_word)
    {

        ByteBuffer nrb =  ByteBuffer.allocate(4);
        nrb.putInt(num_range_bins);
        nrb.position(0);

        radial_start.position(0);
        bin_word.position(0);

        try {
            FileOutputStream fos = c.openFileOutput(fn, Context.MODE_PRIVATE);
            FileChannel wChannel = fos.getChannel();
            int bytes_written;
            while(nrb.hasRemaining()) {
                bytes_written = wChannel.write(nrb);
            }
            while(radial_start.hasRemaining()) {
                bytes_written = wChannel.write(radial_start);
            }
            while(bin_word.hasRemaining()) {
                bytes_written = wChannel.write(bin_word);
            }
            wChannel.close();
            fos.close();
        } catch (Exception e)
        {
            UtilityLog.HandleException(e);
        }
    }
*/

    /*public static short Decode8Bit (Context c, String src, ByteBuffer radial_start_angle, ByteBuffer bin_word)
    {
        UCARRandomAccessFile dis = null;
        int  number_of_range_bins=0;

        try
        {
            dis = new UCARRandomAccessFile(c.getFileStreamPath(src).getAbsolutePath(), "r");
            dis.bigEndian = true;

        } catch (Exception e) {
            UtilityLog.HandleException(e);
        }

        try {

            // ADVANCE PAST WMO HEADER
            while (dis.readShort() != -1) {
                // while (dis.readUnsignedShort() != 16) {
            }

            // the following chunk was added to analyze the header so that status info could be extracted
            // index 4 is radar height
            // index 0,1 is lat as Int
            // index 2,3 is long as Int

            *//*final double latitude_of_radar = dis.readInt() / 1000.0;
            final double longitude_of_radar = dis.readInt() / 1000.0;
            final short height_of_radar = (short) dis.readUnsignedShort();
            final short product_code = (short) dis.readUnsignedShort();
            final short operational_mode = (short) dis.readUnsignedShort();
            final short volume_scan_pattern = (short) dis.readUnsignedShort();
            final short sequence_number = (short) dis.readUnsignedShort();
            final short volume_scan_number = (short) dis.readUnsignedShort();
            final short volume_scan_date = (short) dis.readUnsignedShort();
            final Integer volume_scan_time = dis.readInt();*//*

            dis.skipBytes(26); // 3 int ( 4 byte ) and 7 short ( 2 byte ) == 12 + 14

            dis.skipBytes(74);

            byte[] magic = new byte[3];
            magic[0]='B';
            magic[1]='Z';
            magic[2]='h';

            Compression compression = Compression.getCompression(magic);

            long compressedFileSize = dis.length()-dis.getFilePointer();
            byte[] buf = new byte[(int)compressedFileSize];

            dis.read(buf);
            dis.close();

            InputStream decompStream = compression.decompress(new ByteArrayInputStream(buf));
            DataInputStream dis2 = new DataInputStream(new BufferedInputStream(decompStream));

            *//*short blockDivider = dis2.readShort();
            short blockID = dis2.readShort();
            int blockLen = dis2.readInt();
            short numLayers = dis2.readShort();*//*

            dis2.skipBytes(10); // 3 short , 1 int == 10

            dis2.skipBytes(6);

            //int packetCode = dis2.readUnsignedShort();
            //int  index_of_first_range_bin  = dis2.readUnsignedShort();
            dis2.skipBytes(4);

            number_of_range_bins   = dis2.readUnsignedShort() ;
            // int   i_center_of_sweep   = dis2.readShort() ;
            // int  j_center_of_sweep   = dis2.readShort() ;
            // int  scale_factor  = dis2.readUnsignedShort() ;
            dis2.skipBytes(6);

            int  number_of_radials  = dis2.readUnsignedShort() ;

            int r;
            int number_of_rle_halfwords;
            bin_word.position(0);
            radial_start_angle.position(0);
            int s;

            for ( r=0; r<number_of_radials;r++)
            {
                number_of_rle_halfwords = dis2.readUnsignedShort() ;
                radial_start_angle.putFloat((float)(450 - (dis2.readUnsignedShort()/10)));
                dis2.skipBytes(2);
                for ( s=0; s<number_of_rle_halfwords  ;s++)
                    bin_word.put((byte)(dis2.readUnsignedByte() & 0xFF));
            }
            dis2.close();
        } catch (Exception e)
        {
            UtilityLog.HandleException(e);
        }

        // support tilts above first
        if ( number_of_range_bins % 2 != 0 )
            number_of_range_bins++;

        //if (! src.contains("nids")) {
            //bin_word.position(0);
            //radial_start_angle.position(0);
            //WriteDecodedFile(c, src + ".decomp", number_of_range_bins, radial_start_angle, bin_word);
        //}

        return (short)number_of_range_bins;
    }*/

    public static short Decode8bitWX (Context c, String src, ByteBuffer radial_start_angle, ByteBuffer bin_word)
    {
        UCARRandomAccessFile dis = null;
        int  number_of_range_bins=0;

        try
        {
            dis = new UCARRandomAccessFile(c.getFileStreamPath(src).getAbsolutePath(), "r");
            dis.bigEndian = true;

        } catch (Exception e) {
            UtilityLog.HandleException(e);
        }

        try {

            if (dis!=null) {

                // ADVANCE PAST WMO HEADER
                while (dis.readShort() != -1) {
                    // while (dis.readUnsignedShort() != 16) {
                }

                // the following chunk was added to analyze the header so that status info could be extracted
                // index 4 is radar height
                // index 0,1 is lat as Int
                // index 2,3 is long as Int

          /*  final double latitude_of_radar = dis.readInt() / 1000.0;
            final double longitude_of_radar = dis.readInt() / 1000.0;
            final short height_of_radar = (short) dis.readUnsignedShort();
            final short product_code = (short) dis.readUnsignedShort();
            final short operational_mode = (short) dis.readUnsignedShort();
            final short volume_scan_pattern = (short) dis.readUnsignedShort();
            final short sequence_number = (short) dis.readUnsignedShort();
            final short volume_scan_number = (short) dis.readUnsignedShort();
            final short volume_scan_date = (short) dis.readUnsignedShort();
            final Integer volume_scan_time = dis.readInt();*/
                dis.skipBytes(26);

                dis.skipBytes(74);

                byte[] magic = new byte[3];
                magic[0] = 'B';
                magic[1] = 'Z';
                magic[2] = 'h';

                Compression compression = Compression.getCompression(magic);
                long compressedFileSize = dis.length() - dis.getFilePointer();
                byte[] buf = new byte[(int) compressedFileSize];

                dis.read(buf);
                dis.close();

                InputStream decompStream = compression.decompress(new ByteArrayInputStream(buf));
                DataInputStream dis2 = new DataInputStream(new BufferedInputStream(decompStream));

            /*short blockDivider = dis2.readShort();
            short blockID = dis2.readShort();
            int blockLen = dis2.readInt();
            short numLayers = dis2.readShort();*/
                dis2.skipBytes(10);

                dis2.skipBytes(6);

            /*int packetCode = dis2.readUnsignedShort();
            int  index_of_first_range_bin  = dis2.readUnsignedShort() ;*/
                dis2.skipBytes(4);

                number_of_range_bins = dis2.readUnsignedShort();

            /*int   i_center_of_sweep   = dis2.readShort() ;
            int  j_center_of_sweep   = dis2.readShort() ;
            int  scale_factor  = dis2.readUnsignedShort() ;*/
                dis2.skipBytes(6);

                int number_of_radials = dis2.readUnsignedShort();

                int r;
                int number_of_rle_halfwords;
                bin_word.position(0);
                radial_start_angle.position(0);
                int tn_mod10;
                int tn;
                int s;

                for (r = 0; r < number_of_radials; r++) {
                    number_of_rle_halfwords = dis2.readUnsignedShort();
                    tn = dis2.readUnsignedShort();

                    // the code below must stay as drawing to canvas is not as precise as opengl directly for some reason

                    if (tn % 2 == 1)
                        tn++;

                    tn_mod10 = tn % 10;
                    if (tn_mod10 > 0 && tn_mod10 < 5)
                        tn = tn - tn_mod10;
                    else if (tn_mod10 > 6)
                        tn = tn - tn_mod10 + 10;

                    radial_start_angle.putFloat((float) (450 - (tn / 10)));
                    dis2.skipBytes(2);
                    for (s = 0; s < number_of_rle_halfwords; s++)
                        bin_word.put((byte) (dis2.readUnsignedByte() & 0xFF));
                }
                dis2.close();
            } // end dis null check
        } catch (Exception e)
        {
            UtilityLog.HandleException(e);
        }

        bin_word.position(0);
        radial_start_angle.position(0);

        return (short)number_of_range_bins;
    }

    public static void rect8bitwx(ByteBuffer r_buff, float bin_start, float bin_size, int level_count ,
                                  float angle, float angle_v, int center_x, int center_y)
    {
        r_buff.position(0);
        r_buff.putFloat((bin_start * (float)Math.cos(angle/(M_180_div_PI))) + center_x);
        r_buff.putFloat(((bin_start * (float)Math.sin(angle/(M_180_div_PI)))- center_y ) * -1);
        r_buff.putFloat(((bin_start + ( bin_size * level_count )) * (float)Math.cos(angle/(M_180_div_PI))) + center_x );
        r_buff.putFloat((((bin_start + ( bin_size * level_count )) * (float)Math.sin(angle/(M_180_div_PI)))- center_y ) * -1);
        r_buff.putFloat(((bin_start + ( bin_size * level_count )) * (float)Math.cos((angle - angle_v )/(M_180_div_PI))) + center_x );
        r_buff.putFloat((((bin_start + ( bin_size * level_count )) * (float)Math.sin((angle - angle_v )/(M_180_div_PI)))- center_y ) * -1);
        r_buff.putFloat((bin_start * (float)Math.cos((angle - angle_v)/(M_180_div_PI)))+ center_x);
        r_buff.putFloat(((bin_start * (float)Math.sin((angle - angle_v)/(M_180_div_PI)))- center_y ) * -1);
    }
}
