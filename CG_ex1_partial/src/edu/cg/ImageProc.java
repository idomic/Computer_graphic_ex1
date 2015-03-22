/*
 * This class defines some static methods of image processing.
 */

package edu.cg;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class ImageProc {

	private static int[][] gray;

	public static BufferedImage scaleDown(BufferedImage img, int factor) {
		if (factor <= 0)
			throw new IllegalArgumentException();
		int newHeight = img.getHeight() / factor;
		int newWidth = img.getWidth() / factor;
		BufferedImage out = new BufferedImage(newWidth, newHeight,img.getType());
		for (int x = 0; x < newWidth; x++)
			for (int y = 0; y < newHeight; y++)
				out.setRGB(x, y, img.getRGB(x * factor, y * factor));
		return out;
		
	}

	public static BufferedImage grayScale(BufferedImage img) {
		BufferedImage answer = new BufferedImage(img.getWidth(),
				img.getHeight(), img.getType()); //create new image
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				Color temp = new Color(img.getRGB(x, y));
				int gray = (int) (temp.getRed() * 0.2989 + temp.getGreen()
						* 0.5870 + temp.getBlue() * 0.1140); //calculate gray-scale value of pixel
				answer.setRGB(x, y, new Color(gray, gray, gray).getRGB()); //set pixel of new gray-scale image
			}
		}
		return answer;
	}

	public static int[][] fillGray(BufferedImage img) {
		gray = new int[img.getWidth()][img.getHeight()]; //create new matrix
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				Color p = new Color(img.getRGB(x, y));
				//calculate gray-scale value of pixel and set pixel into new gray-scale matrix
				gray[x][y] = (int) (p.getRed() * 0.2989 + p.getGreen() * 0.5870 + p.getBlue() * 0.1140);
			}
		}
		return gray;
	}

	public static BufferedImage horizontalDerivative(BufferedImage img) {
		fillGray(img); //create gray-scale matrix
		BufferedImage answer = new BufferedImage(img.getWidth(),img.getHeight(), img.getType()); //create new image
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				if ((x == 0) || (x == img.getWidth() - 1)) { //left or right edge condition
					answer.setRGB(x, y, new Color(127, 127, 127).getRGB());
				} else { //calculate derivative
					int dx = (gray[x - 1][y] - gray[x + 1][y] + 255) / 2;
					answer.setRGB(x, y, new Color(dx, dx, dx).getRGB());
				}
			}
		}
		return answer;
	}

	public static BufferedImage verticalDerivative(BufferedImage img) {
		fillGray(img); //create gray-scale matrix
		BufferedImage answer = new BufferedImage(img.getWidth(),img.getHeight(), img.getType()); //create new image
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				if ((y == 0) || (y == img.getHeight() - 1)) { //top or bottom edge condition
					answer.setRGB(x, y, new Color(127, 127, 127).getRGB());
				} else { //calculate derivative
					int dy = (gray[x][y - 1] - gray[x][y + 1] + 255) / 2;
					answer.setRGB(x, y, new Color(dy, dy, dy).getRGB());
				}
			}
		}
		return answer;
	}

	public static BufferedImage gradientMagnitude(BufferedImage img) {
		fillGray(img); //create gray scale matrix
		BufferedImage gradi = new BufferedImage(img.getWidth(),img.getHeight(), img.getType()); //create new image
		for (int x = 0; x < img.getWidth(); x++) {
			for (int y = 0; y < img.getHeight(); y++) {
				if ((y == 0) || (y == img.getHeight() - 1) || (x == 0) || (x == img.getWidth() - 1)) { //all edge condition
					gradi.setRGB(x, y, new Color(127, 127, 127).getRGB());
				} else {
					int dx = gray[x - 1][y] - gray[x + 1][y]; //calculate horizontal derivative
					int dy = gray[x][y - 1] - gray[x][y + 1]; //calculate vertical derivative
					int der = (int)Math.sqrt(dx*dx + dy*dy); //calculate gradient
					if(der > 255) { //thresholding
						der = 255;
					}
					gradi.setRGB(x, y, new Color(der, der, der).getRGB());
				}
			}
		}
		return gradi;
	}

	public static BufferedImage retargetSize(BufferedImage img, int width, int height) {
		
		//step 1: retarget width of image by carving vertical seams
		BufferedImage retargetHori = new Retargeter(img,false,(int)Math.abs(img.getWidth()-width)).retarget(width);
		//step 2: retarget height of image by carving horizontal seams (image should be transposed)
		BufferedImage retargetVerti = new Retargeter(retargetHori,true,Math.abs(img.getHeight()-height)).retarget(height);
		return retargetVerti;
	}

	public static BufferedImage showSeams(BufferedImage img, int width,
			int height) {
		// TODO implement this
		return null;

	}

}

