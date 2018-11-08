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

package gov.noaa.ncdc.nexradiv;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.Vector;

import javax.swing.JPanel;

import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3Header;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3VAD;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3VAD.VADLinePacket;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3VAD.VADTextPacket;
import gov.noaa.ncdc.wct.decoders.nexrad.DecodeL3VAD.VADWindBarbPacket;
import gov.noaa.ncdc.wct.decoders.nexrad.NexradColorFactory;

/**
 *  Renders the actual VAD Wind Profile on a JPanel Component
 *
 * @author     steve.ansari
 * @created    August 30, 2004
 */
 // BUG FIXES ------------------------------------------
 // 20050305 - subtract 90 from decoded direction and flipped sin & cos ops
public class NexradVADPanel extends JPanel {

   private DecodeL3Header header = null;
   private DecodeL3VAD decoder = new DecodeL3VAD();
   private Color[] c = null;
   private Vector<DecodeL3VAD.VADWindBarbPacket> vWindBarbs = new Vector<DecodeL3VAD.VADWindBarbPacket>();
   private Vector<DecodeL3VAD.VADLinePacket> vLines = new Vector<VADLinePacket>();
   private Vector<DecodeL3VAD.VADTextPacket> vText = new Vector<VADTextPacket>();

   // Drawing constants
   // -------------------------- Constants ----------------------------
   /*
    *  / StalkLen
    *  private double r0=0.4;
    *  / BarbSeparation
    *  private final static double rsep=0.045;
    *  / BarbLen(short stalk)
    *  private final static double b1=0.08;
    *  / BarbLen(long stalk)
    *  private final static double b2=2*b1;
    */
   // StalkLen
   private double r0 = 10;
   // BarbSeparation
   private final static double rsep = 3.35;
   // BarbLen(short stalk)
   private final static double b1 = -5.4;
   // BarbLen(long stalk)
   private final static double b2 = 2 * b1;


   private double theta;
   private double r, re, phi, a, b, rb;
   private int speed, holdspeed;
   private int trinum = 0;
   private int lbarbnum = 0;
   private int sbarbnum = 0;
   private double trifactor = 0.0;
   private int[] itri = new int[3];
   private int[] jtri = new int[3];
   private int i1, j1, i2, j2;

   private int maxi, maxj;
   private double iratio = 1.0, jratio = 1.0;
   
   
   
   private int lastWidth = -1;
   private int lastHeight = -1;
   BufferedImage bimage = null;
   
   
   

   /**
    *Constructor for the NexradVADPanel object
    */
   public NexradVADPanel() { }


   /**
    *Constructor for the NexradVADPanel object
    *
    * @param  header  Description of the Parameter
    */
   public NexradVADPanel(DecodeL3Header header) {
      setNexradHeader(header);
   }


   /**
    *  Sets the nexradHeader attribute of the NexradVADPanel object
    *
    * @param  header  The new nexradHeader value
    */
   public void setNexradHeader(DecodeL3Header header) {
      this.header = header;
      c = NexradColorFactory.getColors(header.getProductCode());
      decoder.decodeVAD(header);
      vWindBarbs = decoder.getWindBarbs();
      vLines = decoder.getLines();
      vText = decoder.getText();
   
      maxi = decoder.getMaxI();
      maxj = decoder.getMaxJ();
      //preferredSize.width++;
      //preferredSize.height = preferredSize.height - 8;
   }


   /**
    *  Description of the Method
    *
    * @param  g  Description of the Parameter
    */
   public void paintComponent(Graphics g) {
      super.paintComponent(g);
      //clears the background

      drawImage();
      g.drawImage(bimage, 0, 0, null);
   }
   
   
   public void drawImage() {
       
       
       if (bimage == null || lastWidth != getWidth() || lastHeight != getHeight()) {
           bimage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
           lastWidth = getWidth();
           lastHeight = getHeight();
       }
       Graphics g = bimage.getGraphics();
       
       // 1. clear the image
       g.setColor(this.getBackground());
       g.fillRect(0, 0, getWidth(), getHeight());
       
       
       
      //System.out.println("MAXI: "+maxi+"  MAXJ: "+maxj);
      //System.out.println("PS:  "+ getPreferredSize());
      //System.out.println("SZ:  "+ getSize());
      //System.out.println("PARENT PS:  "+ getParent().getParent().getPreferredSize());
      //System.out.println("PARENT SZ:  "+ getParent().getParent().getSize());
      
      iratio = (getParent().getParent().getWidth()-(maxi-488)) / (double)maxi;
      //jratio = (getParent().getParent().getHeight()-(maxj-522)) / (double)maxj;
      jratio = (getParent().getParent().getHeight()-(maxj-522)-50) / (double)maxj;
      
      //System.out.println("IRATIO: "+iratio);
      //System.out.println("JRATIO: "+jratio);
      
      if (iratio < 1) {
         iratio = 1.0;
      }
      //if (jratio < 1) {
      //   jratio = 1.0;
      //}
      if (jratio < .87) {
         jratio = .87;
      }
      
      DecodeL3VAD.VADWindBarbPacket windPacket = null;
      DecodeL3VAD.VADLinePacket linePacket = null;
      DecodeL3VAD.VADTextPacket textPacket = null;

      // Paint lines
      for (int n = 0; n < vLines.size(); n++) {
         linePacket = vLines.elementAt(n);
         if (linePacket.ipos1 < linePacket.ipos2) {
            i1 = linePacket.ipos1;
            i2 = linePacket.ipos2;
         }
         else {
            i2 = linePacket.ipos1;
            i1 = linePacket.ipos2;
         }
         if (linePacket.jpos1 < linePacket.jpos2) {
            j1 = linePacket.jpos1;
            j2 = linePacket.jpos2;
         }
         else {
            j2 = linePacket.jpos1;
            j1 = linePacket.jpos2;
         }
         g.setColor(new Color(100, 100, 100));
         //g.drawLine(i1, j1, i2, j2);
         g.drawLine((int)(iratio*i1), (int)(jratio*j1), (int)(iratio*i2), (int)(jratio*j2));
      }
      
      // Paint text
      for (int n = 0; n < vText.size(); n++) {
         textPacket = vText.elementAt(n);
         g.setColor(Color.black);
         g.drawString(textPacket.textString, (int)(iratio*textPacket.ipos), (int)(jratio*textPacket.jpos) + 9);
         //System.out.println("g.drawString("+textPacket.textString+", "+textPacket.ipos+", "+textPacket.jpos+");");
      }
      // Paint wind barbs
      for (int n = 0; n < vWindBarbs.size(); n++) {
//System.out.println("PAINTING BARB "+n+" of "+vWindBarbs.size());

         windPacket = vWindBarbs.elementAt(n);
         g.setColor(c[windPacket.rms]);
         // Start barb drawing
         int direction = windPacket.direction;
         //direction = direction - 90;
         //direction = 90 - direction;
         direction = 180 - direction;
         //theta = Math.toRadians(90-windPacket.direction);
         //theta = Math.toRadians(windPacket.direction);
         theta = Math.toRadians(direction);
         speed = windPacket.speed;
         // round speed value to nearest 5 knot value
         speed = ((int)((speed/5.0)+0.5))*5;
         
         holdspeed = speed;
         //if (speed > 0) System.out.println("SPEED1 "+speed);
         // Draw stalk

         g.drawLine((int)(iratio*windPacket.ipos), (int)(jratio*windPacket.jpos),
               ((int)(iratio* windPacket.ipos) + (int) (r0 * Math.sin(theta))),
               ((int)(jratio*windPacket.jpos) + (int) (r0 * Math.cos(theta))));


//           g.drawLine((int)(iratio*windPacket.ipos), (int)(jratio*windPacket.jpos),
//               ((int)(iratio* windPacket.ipos) + (int) (r0 * Math.cos(theta))),
//               ((int)(jratio*windPacket.jpos) + (int) (r0 * Math.sin(theta))));

               
               
         // Draw speed symbol
         trinum = 0;
         lbarbnum = 0;
         sbarbnum = 0;
         trifactor = 0.0;
         // Draw triangle markers
         while (speed > 45) {
            r = r0 - 1.5 * rsep * trinum - 1.5 * rsep;
            if (r < 2 * rsep) {
               r0 = r0 + 1.5 * rsep;
            }
            trinum = trinum + 1;
            speed = speed - 50;
         }
         // Draw full line markers
         while (speed > 5) {
            if (trinum > 0) {
               trifactor = 0.5 * rsep;
            }
            r = r0 - 1 * rsep * lbarbnum - 1.5 * rsep * trinum - trifactor;
            if (r < 2 * rsep) {
               r0 = r0 + 1 * rsep;
            }
            lbarbnum = lbarbnum + 1;
            speed = speed - 10;
         }
         // Draw half line markers
         while (speed > 0) {
            if (trinum > 0) {
               trifactor = 0.5 * rsep;
            }
            r = r0 - 1 * rsep * sbarbnum - 1 * rsep * lbarbnum - 1.5 * rsep * trinum - trifactor;
            if (r < 2 * rsep) {
               r0 = r0 + 1 * rsep;
            }
            sbarbnum = sbarbnum + 1;
            speed = speed - 5;
         }

//   --Draw the wind barbs --
         trinum = 0;
         lbarbnum = 0;
         sbarbnum = 0;
         trifactor = 0.0;
         speed = holdspeed;
         //if (speed > 0) System.out.println("SPEED2 "+speed);
//-------- 50 KNOT TRIANGLES --------------------------------
         while (speed > 45) {
            re = r0 - 1.5 * rsep * trinum;
            r = r0 - 1.5 * rsep * trinum - 1.5 * rsep;
            phi = Math.atan(b2 / re);
            a = Math.pow(re, 2);
            b = Math.pow(b2, 2);
            rb = Math.pow(a + b, 0.5);
            itri[0] = (int)(iratio*windPacket.ipos) + (int) (re * Math.sin(theta));
            jtri[0] = (int)(jratio*windPacket.jpos) + (int) (re * Math.cos(theta));
            itri[1] = (int)(iratio*windPacket.ipos) + (int) (rb * Math.sin(theta + phi));
            jtri[1] = (int)(jratio*windPacket.jpos) + (int) (rb * Math.cos(theta + phi));
            itri[2] = (int)(iratio*windPacket.ipos) + (int) (r * Math.sin(theta));
            jtri[2] = (int)(jratio*windPacket.jpos) + (int) (r * Math.cos(theta));
            //'draw polyf 'i1' 'j1' 'i2' 'j2' 'i3' 'j3' 'i1' 'j1
            g.fillPolygon(itri, jtri, 3);
            trinum = trinum + 1;
            speed = speed - 50;
         }
//-------- 10 KNOT LARGE BARB --------------------------------
         while (speed > 5) {
            if (trinum > 0) {
               trifactor = 0.5 * rsep;
            }
            r = r0 - 1 * rsep * lbarbnum - 1.5 * rsep * trinum - trifactor;
            phi = Math.atan(b2 / r);
            a = Math.pow(r, 2);
            b = Math.pow(b2, 2);
            rb = Math.pow(a + b, 0.5);
            i1 = (int)(iratio*windPacket.ipos) + (int) (r * Math.sin(theta));
            j1 = (int)(jratio*windPacket.jpos) + (int) (r * Math.cos(theta));
            i2 = (int)(iratio*windPacket.ipos) + (int) (rb * Math.sin(theta + phi));
            j2 = (int)(jratio*windPacket.jpos) + (int) (rb * Math.cos(theta + phi));
            //System.out.println("g.drawLine("+i1+", "+j1+", "+i2+", "+j2+");");
            g.drawLine(i1, j1, i2, j2);
            lbarbnum = lbarbnum + 1;
            speed = speed - 10;
         }
//-------- 5 KNOT SMALL BARB --------------------------------
         while (speed > 0) {
            if (trinum > 0) {
               trifactor = 0.5 * rsep;
            }
            r = r0 - 1 * rsep * sbarbnum - 1 * rsep * lbarbnum - 1.5 * rsep * trinum - trifactor;
            phi = Math.atan(b1 / r);
            a = Math.pow(r, 2);
            b = Math.pow(b1, 2);
            rb = Math.pow(a + b, 0.5);
            i1 = (int)(iratio*windPacket.ipos) + (int) (r * Math.sin(theta));
            j1 = (int)(jratio*windPacket.jpos) + (int) (r * Math.cos(theta));
            i2 = (int)(iratio*windPacket.ipos) + (int) (rb * Math.sin(theta + phi));
            j2 = (int)(jratio*windPacket.jpos) + (int) (rb * Math.cos(theta + phi));
//            i1 = (int)(iratio*windPacket.ipos) + (int) (r * Math.cos(theta));
//            j1 = (int)(jratio*windPacket.jpos) + (int) (r * Math.sin(theta));
//            i2 = (int)(iratio*windPacket.ipos) + (int) (rb * Math.cos(theta + phi));
//            j2 = (int)(jratio*windPacket.jpos) + (int) (rb * Math.sin(theta + phi));
            //System.out.println("g.drawLine("+i1+", "+j1+", "+i2+", "+j2+");");
            g.drawLine(i1, j1, i2, j2);
            sbarbnum = sbarbnum + 1;
            speed = speed - 5;
         }
      }
      windPacket = null;
      linePacket = null;
      textPacket = null;

   }
   // END paintComponent


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
    * Override the getPreferredSize method 
    * Gets the preferredSize attribute of the NexradVADPanel object
    *
    * @return    The preferredSize value
    */
    
   public Dimension getPreferredSize() {
      double iratio = (getParent().getParent().getWidth()-(maxi-488)) / (double)maxi;
      //double jratio = (getParent().getParent().getHeight()-(maxj-522)) / (double)maxj;
      double jratio = (getParent().getParent().getHeight()-(maxj-522)-50) / (double)maxj;
      if (iratio < 1) {
         iratio = 1.0;
      }
//      if (jratio < 1) {
//         jratio = 1.0;
//      }
      if (jratio < .87) {
         jratio = .87;
      }
      
      //return preferredSize;
      //return new Dimension(preferredSize.width + 1, preferredSize.height - 8);
      //return new Dimension(maxi + 1, maxj - 8);
      return new Dimension((int)(maxi*iratio + 1), (int)(maxj*jratio + 1));
   }

   /**
    * Override the setPreferredSize method 
    * Sets the preferredSize attribute of the NexradVADPanel object
    *
    * @param size    The preferredSize value
    *
    */
    
   public void setPreferredSize(Dimension size) {
       
//       System.out.println("IN setPreferredSize");
       
      //return preferredSize;
      //return new Dimension(preferredSize.width + 1, preferredSize.height - 8);
      iratio = ((double)size.width) / maxi;
      jratio = ((double)size.height) / maxj;
      return ;
   }

   
   
   
   
   public static void main(String[] args) {
      try {
         DecodeL3Header header = new DecodeL3Header();
         header.decodeHeader(new java.io.File("H:\\ViewerData\\HAS999900001\\7000KCLE_SDUS31_NVWCLE_200211102200").toURL());
         NexradVADPanel vadPanel = new NexradVADPanel(header);
         javax.swing.JFrame frame = new javax.swing.JFrame("VAD TEST FRAME");
         frame.getContentPane().add(vadPanel);
         frame.pack();
         frame.show();
      }
      catch (Exception e) {
         e.printStackTrace();
      }
   }
   
}
// END class NexradVADPanel

