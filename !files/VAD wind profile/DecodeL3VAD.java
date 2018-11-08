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

package gov.noaa.ncdc.wct.decoders.nexrad;

import java.util.Vector;

public class DecodeL3VAD {
   
   private DecodeL3Header header;
   
   private Vector<VADWindBarbPacket> vWindBarbs = new Vector<VADWindBarbPacket>();
   private Vector<VADLinePacket> vLines = new Vector<VADLinePacket>();
   private Vector<VADTextPacket> vText = new Vector<VADTextPacket>();

   private int maxi, maxj;

   /**
    * Empty constructor
    */
   public DecodeL3VAD() {
   }
   
   /**
    *  Decodes the Level-III NEXRAD VAD Wind Profile Product
    *
    * @param  header  DecodeL3Header object for the file
    */
   public void decodeVAD(DecodeL3Header header) {

      this.header = header;

      clearAllVectors();

      ucar.unidata.io.RandomAccessFile f = header.getRandomAccessFile();
      
      
//String[] str = header.getDataThresholdStringArray();
//for (int n=0; n<str.length; n++) {
//   System.out.println("DTSA["+n+"] = "+str[n]);
//}
      
      try {
          f.seek(0);
          // ADVANCE PAST WMO HEADER
          while (f.readShort() != -1) {
              ;
          }
          // ADVANCE TO BEGINNING OF PRODUCT SYMBOLOGY BLOCK (BLOCK DIVIDER)
         while (f.readShort() != -1) {
            ;
         }
         
         // BUGFIX for 2018-era data.  It appears we can't assume product symbology block is always next, 
         // so we have to check blockID and skip to correct block if needed.
         short blockID = f.readShort();
         while (blockID != 1) {
             while (f.readShort() != -1) {
                 ;
             }
             blockID = f.readShort();
         }
         
         
         int blockLen = f.readInt();
         short numLayers = f.readShort();

//System.out.println("VAD blockID: "+blockID);         
//System.out.println("VAD blockLen: "+blockLen);         
//System.out.println("VAD numLayers: "+numLayers);         
         
         // ADVANCE TO LAYER DIVIDER
         while (f.readShort() != -1) {
            ;
         }
         int layerLen = f.readInt();

         //-------------------------------------------------------------------
         // BLOCK 1
         //-------------------------------------------------------------------

         short packetCode;

         //-------------------------------------------------------------------
         // BLOCK 1
         //-------------------------------------------------------------------

         short packetLen;

         //-------------------------------------------------------------------
         // BLOCK 1
         //-------------------------------------------------------------------

         short num;
         short ipos;
         short jpos;
         short ipos1;
         short jpos1;
         short ipos2;
         short jpos2;
         short rms;
         short direction;
         short speed;
         int curLength = 0;
         int m;
         byte[] text = null;

//System.out.println("layerLen "+layerLen);

         while (curLength < layerLen) {
            packetCode = f.readShort();
            packetLen = f.readShort();
//System.out.println("PG1 PACKET CODE = "+packetCode);

            // Wind Barb Packet
            if (packetCode == 4) {
               rms = f.readShort();
//System.out.println("RMS: "+rms);               
               ipos = f.readShort();
               jpos = f.readShort();
               direction = f.readShort();
//direction-=90;
               speed = f.readShort();
               vWindBarbs.addElement(new VADWindBarbPacket(rms, ipos, jpos, direction, speed));

               // get max values
               if (ipos > maxi) {
                  maxi = ipos;
               }
               if (jpos > maxj) {
                  maxj = jpos;
               }

            }
            else if (packetCode == 10) {
               /*
                *  System.out.println("PACKET LEN: "+packetLen);
                *  for (int i=0; i<60; i++) {
                *  System.out.println("NEXT SHORT: "+i+" "+f.readShort());
                *  }
                *  return;
                */
               num = f.readShort();
               // unknown
//System.out.println("NUM: "+num);
//System.out.println("PACKET LEN: "+packetLen);
               //for(int j=0;j<packetLen-2; j=j+8) {
               m = (packetLen - 2) / 8;
               if ((packetLen - 2) % 8 != 0) {
                  System.out.println("========== LOST FILE LOCATION ==========");
                  return;
               }
               for (int j = 0; j < m; j++) {
                  ipos1 = f.readShort();
                  jpos1 = f.readShort();
                  ipos2 = f.readShort();
                  jpos2 = f.readShort();
/*
                  // Adjust preferred size
                  if (ipos1 > preferredSize.width) {
                     preferredSize.width = ipos1;
                  }
                  if (jpos1 > preferredSize.height) {
                     preferredSize.height = jpos1;
                  }
                  if (ipos2 > preferredSize.width) {
                     preferredSize.width = ipos2;
                  }
                  if (jpos2 > preferredSize.height) {
                     preferredSize.height = jpos2;
                  }
*/
                  //System.out.println(ipos1+" - "+ipos2+" , "+jpos1+" - "+jpos2);
                  vLines.addElement(new VADLinePacket(ipos1, jpos1, ipos2, jpos2));

                  // get max values
                  if (ipos1 > maxi) {
                     maxi = ipos1;
                  }
                  if (jpos1 > maxj) {
                     maxj = jpos1;
                  }
                  if (ipos2 > maxi) {
                     maxi = ipos2;
                  }
                  if (jpos2 > maxj) {
                     maxj = jpos2;
                  }
               }
            }
            else if (packetCode == 8) {
//System.out.println("NUM: "+num);
//System.out.println("PACKET LEN: "+packetLen);
               num = f.readShort();
               // unknown
               ipos = f.readShort();
               jpos = f.readShort();
               text = new byte[packetLen - 6];
               f.read(text);
               //System.out.println(" TEXT:  "+text);
               vText.addElement(new VADTextPacket(ipos, jpos, new String(text)));

               // get max values
               if (ipos > maxi) {
                  maxi = ipos;
               }
               if (jpos > maxj) {
                  maxj = jpos;
               }
            }
            else {
               System.out.println("UNEXPECTED VAD PACKET CODE (" + packetCode + ") -- EXITING");
               return;
            }
            curLength += packetLen + 2;

         }

      } catch (Exception e) {
         e.printStackTrace();
      }
   }
   // END method decodeVAD

   
   /**
    * Return the max I location value
    */
   public int getMaxI() {
      return maxi;
   }
   
   /**
    * Return the max J location value
    */
   public int getMaxJ() {
      return maxj;
   }
   
   
   
   
   
   
   
   
   

   /**
    *  Clears the vectors of data
    */
   public void clearAllVectors() {
      vWindBarbs.clear();
      vLines.clear();
      vText.clear();
   }


   
   
   
   
   
   /**
    *  Gets the windBarbs attribute of the NexradVADPanel object
    *
    * @return    The windBarbs value
    */
   public Vector<VADWindBarbPacket> getWindBarbs() {
      return vWindBarbs;
   }
   // END method getWindBarbs


   /**
    *  Gets the lines attribute of the NexradVADPanel object
    *
    * @return    The lines value
    */
   public Vector<VADLinePacket> getLines() {
      return vLines;
   }
   // END method getWindBarbs


   /**
    *  Gets the text attribute of the NexradVADPanel object
    *
    * @return    The text value
    */
   public Vector<VADTextPacket> getText() {
      return vText;
   }
   // END method getWindBarbs

   
   

   /**
    *  Description of the Class
    *
    * @author     steve.ansari
    * @created    August 30, 2004
    */
   public class VADWindBarbPacket {
      /**
       *  Description of the Field
       */
      public short rms, ipos, jpos, direction, speed;


      /**
       *Constructor for the VADWindBarbPacket object
       *
       * @param  rms        Description of the Parameter
       * @param  ipos       Description of the Parameter
       * @param  jpos       Description of the Parameter
       * @param  direction  Description of the Parameter
       * @param  speed      Description of the Parameter
       */
      public VADWindBarbPacket(short rms, short ipos, short jpos, short direction, short speed) {
         this.rms = rms;
         this.ipos = ipos;
         this.jpos = jpos;
         this.direction = direction;
         this.speed = speed;
      }
      
      
   }
   // END class VADWindBarb


   /**
    *  Description of the Class
    *
    * @author     steve.ansari
    * @created    August 30, 2004
    */
   public class VADLinePacket {
      /**
       *  Description of the Field
       */
      public short ipos1, jpos1, ipos2, jpos2;


      /**
       *Constructor for the VADLinePacket object
       *
       * @param  ipos1  Description of the Parameter
       * @param  jpos1  Description of the Parameter
       * @param  ipos2  Description of the Parameter
       * @param  jpos2  Description of the Parameter
       */
      public VADLinePacket(short ipos1, short jpos1, short ipos2, short jpos2) {
         this.ipos1 = ipos1;
         this.jpos1 = jpos1;
         this.ipos2 = ipos2;
         this.jpos2 = jpos2;
      }
   }
   // END class VADWindBarb


   /**
    *  Description of the Class
    *
    * @author     steve.ansari
    * @created    August 30, 2004
    */
   public class VADTextPacket {
      /**
       *  Description of the Field
       */
      public short ipos, jpos;
      /**
       *  Description of the Field
       */
      public String textString;


      /**
       *Constructor for the VADTextPacket object
       *
       * @param  ipos        Description of the Parameter
       * @param  jpos        Description of the Parameter
       * @param  textString  Description of the Parameter
       */
      public VADTextPacket(short ipos, short jpos, String textString) {
         this.ipos = ipos;
         this.jpos = jpos;
         this.textString = textString;
      }
   }
   // END class VADWindBarb

   
   
   
}
