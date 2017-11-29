package com.zw.archer.util;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
  
  
/** 
 * 图片处理工具类 
 *  
 * @author Daven 
 * 
 */  
public class ImgUtil {  
      
      
    public static void main(String[] args) {  
        ImgUtil.composePic("D:/back.png", "https://mp.weixin.qq.com/cgi-bin/showqrcod", "D:/output.png", 53, 161,50,50,false);  
    }  
      
      
    /** 
     * 合成图片(类似图片水印) 
     * @param backImage 背景图片对象 
     * @param headImage 用户头像对象 
     * @param outPutPath 输出路径（D：/img/out.png） 
     * @param leftX 距离目标图片左侧的偏移量 
     * @param leftY 距离目标图片左侧的偏移量 
     * @throws InterruptedException 
     * @throws IOException 
     */  
    public static void composePic(Image backImage, Image headImage,   
            String outPutPath, int leftX, int leftY,int hei,int wid)   
            throws InterruptedException, IOException {  
          
            // 图片的高/宽度  
            int bwidth = backImage.getWidth(null);  
            int bheight = backImage.getHeight(null);  
            int hwidth = headImage.getWidth(null); 
            if(wid!=0){
            	hwidth=wid;
            }
            int hheight = headImage.getHeight(null);  
            if(hei!=0){
            	hheight=hei;
            }
            int alphaType = BufferedImage.TYPE_INT_RGB;  
            /*if (hasAlpha(backImage)) { 
                alphaType = BufferedImage.TYPE_INT_ARGB; 
            }*/  
  
            // 画图  
            BufferedImage backgroundImage = new BufferedImage(bwidth, bheight, alphaType);  
            Graphics2D graphics2D = backgroundImage.createGraphics();  
            graphics2D.drawImage(backImage, 0, 0, null);  
            graphics2D.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 1));  
            graphics2D.drawImage(headImage, leftX, leftY, hwidth, hheight, null);  
  
            // 输出  
            ImageIO.write(backgroundImage, "jpg", new File(outPutPath));  
  
    }  
      
    /** 
     * 合成图片 
     * @param backPicPath 背景图片本地路径 
     * @param headPicUrl 用户头像网络url 
     * @param outPutPath 输出路径（D：/img/out.png） 
     * @param leftX 距离目标图片左侧的偏移量 
     * @param leftY 距离目标图片左侧的偏移量 
     */  
    public static void composePic(String backPicPath, String headPicUrl,   
            String outPutPath, int leftX, int leftY,int hei,int wid,boolean cir) {  
        try {  
            // 读取背景图片  
            Image backImage = ImgUtil.loadImageLocal(backPicPath);  
            
           
             
            // 读取头像图片  
            Image headImage = ImgUtil.loadImageUrl(headPicUrl,cir);  
            // 图像  
            ImgUtil.composePic(backImage, headImage, outPutPath, leftX, leftY,hei,wid);  
              
        } catch (IOException e) {  
            e.printStackTrace();  
        } catch (InterruptedException e) {  
            e.printStackTrace();  
        }  
    }  
    public static void composePicTowLoc(String backPicPath, String headPicUrl,   
    		String outPutPath, int leftX, int leftY,int hei,int wid) {  
    	try {  
    		// 读取背景图片  
    		Image backImage = ImgUtil.loadImageLocal(backPicPath);  
    		
    		
    		
    		// 读取头像图片  
    		Image headImage = ImgUtil.loadImageLocal(headPicUrl);  
    		// 图像  
    		ImgUtil.composePic(backImage, headImage, outPutPath, leftX, leftY,hei,wid);  
    		
    	} catch (IOException e) {  
    		e.printStackTrace();  
    	} catch (InterruptedException e) {  
    		e.printStackTrace();  
    	}  
    }  
      
    /** 
     * 图片上加文字（文字水印） 
     * @param pressText 水印文字 
     * @param targetImg 目标图片 
     * @param fontName 字体名称 
     * @param fontStyle 字体样式，如：粗体和斜体(Font.BOLD|Font.ITALIC) 
     * @param color 字体颜色 
     * @param fontSize 字体大小 
     * @param x 水印文字距离目标图片左侧的偏移量 
     * @param y 水印文字距离目标图片上侧的偏移量 
     * @param alpha 透明度(0.0 -- 1.0, 0.0为完全透明，1.0为完全不透明) 
     * @throws IOException  
     */  
    public static BufferedImage pressText(String pressText, Image targetImg, String fontName, int fontStyle,   
            Color color, int fontSize, int x, int y, float alpha) throws IOException {  
          
        // 图片宽高  
        int width = targetImg.getWidth(null);  
        int height = targetImg.getHeight(null);  
        //样式  
        Font font = new Font(fontName, fontStyle, fontSize);  
          
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);  
        Graphics2D g = image.createGraphics();  
        g.drawImage(targetImg, 0, 0, width, height, null);  
        g.setColor(color);// 颜色  
        g.setFont(font);  
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, alpha));  
        g.drawString(pressText, x, y);  
        g.dispose();  
          
        return image;  
    }  
  
    /** 
     * 是否开启alpha通道 
     *  
     * @param image 
     * @return 
     * @throws InterruptedException  
     */  
    public static boolean hasAlpha(Image image)   
            throws InterruptedException {  
          
        if (image instanceof BufferedImage) {  
            BufferedImage bimage = (BufferedImage) image;  
            return bimage.getColorModel().hasAlpha();  
        }  
          
        PixelGrabber pGrabber = new PixelGrabber(image, 0, 0, 1, 1, false);  
        pGrabber.grabPixels();  
        ColorModel colorModel = pGrabber.getColorModel();  
          
        return colorModel.hasAlpha();  
    }  
      
    public static Map<String, BufferedImage> tempbi=new HashMap<String, BufferedImage>();
    /** 
     * 导入本地图片到缓冲区 
     * @param imgPath 本地的图片地址 
     * @return 
     * @throws IOException 
     */  

    
    public static BufferedImage loadImageLocal(String imgPath)   
            throws IOException {  
    	/*if(tempbi.containsKey(imgPath)){
    		return tempbi.get(imgPath);
    	}*/
    	BufferedImage bi= ImageIO.read(new File(imgPath));
    	//tempbi.put(imgPath, bi);
        return bi;  
    }  
  
    /** 
     * 导入网络图片到缓冲区 
     * @param imgUrl 网络图片url 
     * @return 
     * @throws IOException 
     */  
    public static BufferedImage loadImageUrl(String imgUrl ,boolean cir)   
            throws IOException {  
    	  URL url = new URL(imgUrl);  
    	if(!cir){
    		
    	        return ImageIO.read(url);  
    	}
    	BufferedImage bi1;
    	bi1 = ImageIO.read(url);
        BufferedImage image = new BufferedImage(bi1.getWidth(), bi1.getHeight(),
                BufferedImage.TYPE_INT_ARGB);

        Ellipse2D.Double shape = new Ellipse2D.Double(0, 0, bi1.getWidth(), bi1
                .getHeight());
         
        Graphics2D g2 = image.createGraphics();
        image = g2.getDeviceConfiguration().createCompatibleImage(bi1.getWidth(), bi1.getHeight(), Transparency.TRANSLUCENT);
        g2 = image.createGraphics();
        g2.setComposite(AlphaComposite.Clear);
        g2.fill(new Rectangle(image.getWidth(), image.getHeight()));
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC, 1.0f));
        g2.setClip(shape);
        // 使用 setRenderingHint 设置抗锯齿
        g2.drawImage(bi1, 0, 0, null);
        g2.dispose();
        return image;  
    }  
      
    /** 
     * 16进制转Color对象 
     * @param str 
     * @return 
     */  
    public static Color String2Color(String str) {  
        int i =   Integer.parseInt(str.substring(1), 16);  
        return new Color(i);  
    }  
  
    /** 
     * Color对象转16进制 
     * @param color 
     * @return 
     */  
    public static String Color2String(Color color) {  
        String R = Integer.toHexString(color.getRed());  
        R = R.length()<2?('0'+R):R;  
        String B = Integer.toHexString(color.getBlue());  
        B = B.length()<2?('0'+B):B;  
        String G = Integer.toHexString(color.getGreen());  
        G = G.length()<2?('0'+G):G;  
        return '#'+R+B+G;  
    }  
  
     /** 
      * 获取字符长度，一个汉字作为 1 个字符, 一个英文字母作为 0.5 个字符 
      * @param text 
      * @return 字符长度，如：text="中国",返回 2 
      *         text="test",返回 2 
      *         text="中国ABC",返回 4 
      */  
    public static int getLength(String text) {  
        int textLength = text.length();  
        int length = textLength;  
        for (int i = 0; i < textLength; i++) {  
           if (String.valueOf(text.charAt(i)).getBytes().length > 1) {  
               length++;  
           }  
        }  
        return (length % 2 == 0) ? length / 2 : length / 2 + 1;  
     }  
      
}  