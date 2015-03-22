package edu.cg;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class Retargeter {

	private BufferedImage img;
	private boolean isVertical;
	private int width;
	private int height;
	private int[][] grayscale;
	private int[][] gradi;
	private int[][] cost;
	private int[][] origPos;
	private int[][] seamOrder;
	
	public Retargeter(BufferedImage img, boolean isVertical, int k) {
		this.img = img;
		this.isVertical = isVertical;
		if (isVertical) //vertical - "transpose" image by swapping height and width
		{
			width = img.getHeight();
			height = img.getWidth();
		}
		else //horizontal
		{
			height = img.getHeight();
			width = img.getWidth();
		}
		//create gray-scale image matrix
		grayscale = new int[width][height];
		grayscale = ImageProc.fillGray(img);
		//create gradient matrix
		BufferedImage fullgradi = ImageProc.gradientMagnitude(img);
		gradi = new int[width][height];
		for (int x = 0; x < height; x++) {
			for (int y = 0; y < width; y++) {
				gradi[x][y] = new Color(fullgradi.getRGB(x, y)).getBlue();
			}
		}
		//create original positions matrix
	    origPos = new int[width][height];
	    for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				origPos[x][y] = x; //original column
			}
		}
	    //initialize seamOrder matrix
	    seamOrder = new int[width][height];
	    for (int y = 0; y < height; y++) {
	    	for (int x = 0; x < width; x++) {
	    		seamOrder[x][y] = Integer.MAX_VALUE;
	    	}
	    }
		calculateSeamsOrderMatrix(k);
	}

	public void getSeamsOrderMatrix() {
		//you can implement this (change the output type)
	}

	public void getOrigPosMatrix() {
		//you can implement this (change the output type)
	}

	public BufferedImage retarget(int newSize) {	
		//create new image with original configuration - 
		//if image is transposed, it must be turned back
		//int newImageHeight, newImageWidth;
		BufferedImage newImage;
		if (isVertical) //original height and weight were switched
	    {
	      newImage = new BufferedImage(height, newSize, img.getType());
	      //newImageHeight = newSize;
	      //newImageWidth = height;
	    }
	    else 
	    {
	      newImage = new BufferedImage(newSize, height, img.getType());
	      //newImageHeight = height;
	      //newImageWidth = newSize;
	    }
		//number of seams to remove/insert
		int k = (int)Math.abs(width - newSize);
		int pos;
		//option 1: reduce size
		if (newSize <= width){
			int index = 0;
			for (int x = 0; x < height - 1; x++) {
				for (int y = 0; y < width - 1; y++) {
					if (seamOrder[y][x] >= k) { 
						if (isVertical)
						{
							pos = origPos[index][x];
							newImage.setRGB(x, index, img.getRGB(x,pos));
						}
						else
						{
							pos = origPos[index][x];
							newImage.setRGB(index, x, img.getRGB(pos,x));
						}
						index++;
					}
				}
			}
		}
		//option 2: enlarge size
		else {
			for (int x = 0; x < height - 1; x++) {
				int index = 0; // this is the newImage index
				for (int y = 0; y < width - 1; y++) {
					if (isVertical)
					{
						
						newImage.setRGB(x, index, img.getRGB(x,y));
						index++;
						if (seamOrder[y][x] < k){
							newImage.setRGB(x, index, img.getRGB(x,y));
							index++;
						}
					}
					else
					{
						newImage.setRGB(index, x, img.getRGB(y,x));
						index++;
						if (seamOrder[y][x] < k){
							newImage.setRGB(index, x, img.getRGB(y,x));
							index++;
						}
					}
				}
			}
		}
		
		
	    return newImage;
	}


	private void calculateSeamsOrderMatrix(int k) {
		for (int i = 0; i < k; i++){
			calculateCostsMatrix(width-i);
			int minCost = Integer.MAX_VALUE;
			int minLoc = -1;
			//locate minimal cost of bottom row
			for(int j = 0; j < width-i; j++) {
				if(cost[j][height-1] < minCost) {
					minCost = cost[j][height-1];
					minLoc = j;
				}
			}
			seamOrder[minLoc][height-1] = i;
//			removeCurrent(cost, minLoc, height-1);
//			//removeCurrent(img, minLoc, height-1);
//			removeCurrent(origPos, minLoc, height-1);
//			
			for (int j = height - 1; j > 0; j--) {
				//int origCol = origPos[minLoc][j];
				//seamOrder[origCol][j]=i;
				removeCurrent(cost, minLoc, j);
				//removeCurrent(img, minLoc, j);
				removeCurrent(origPos, minLoc, j);
				
				if ((minLoc > 0) && (cost[minLoc - 1][j-1] < cost[minLoc][j-1])) {
					if ((minLoc < width-1) && (cost[minLoc - 1][j-1] < cost[minLoc + 1][j-1])) {
						seamOrder[minLoc - 1][j-1] = i;
						minLoc--;
					} else {
						seamOrder[minLoc + 1][j-1] = i;
						minLoc++;
					}
				} else if ((minLoc < width-1) && (cost[minLoc][j-1] < cost[minLoc + 1][j-1])) {
					seamOrder[minLoc][j-1] = i;
				} else {
					seamOrder[minLoc + 1][j-1] = i;
					minLoc++;
				}
				
			}
			seamOrder[minLoc][0] = i;

			removeCurrent(cost, minLoc, 0);
			//removeCurrent(img, minLoc, 0);
			removeCurrent(origPos, minLoc, 0);
			
		}
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				System.out.format("%10d ", seamOrder[x][y]);
			}
			System.out.println();
		}
	} 

	private void removeCurrent(int[][] array, int x, int y){
		for(int i = x; i < (array.length - 1); i++){
			array[i][y] = array[i+1][y];
		}
	}
	
	private void calculateCostsMatrix(int w) {
		cost = new int[w][height];
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < w; x++) {
				if(y == 0) { //top row
					cost[x][y] = gradi[x][y];
				}
				else {
					if(x == 0) { //leftmost edge
						cost[x][y] = Math.min(cost[x][y-1] + 0,
								cost[x+1][y-1]  
								+ Math.abs(grayscale[x][y-1] - grayscale[x+1][y]));
					}
					else if (x == w - 1) { //rightmost edge
						cost[x][y] = Math.min(cost[x][y-1] + 0,
								cost[x-1][y-1] + 
								+ Math.abs(grayscale[x][y-1] - grayscale[x-1][y]));
					}
					else {
						cost[x][y] = Math.min(cost[x][y-1] + Math.abs( grayscale[x + 1][y] - grayscale[x - 1][y]),
								cost[x-1][y-1] + Math.abs(grayscale[x+1][y] - grayscale[x-1][y]) 
								+ Math.abs(grayscale[x][y-1] - grayscale[x-1][y]) +
								cost[x+1][y-1] + Math.abs(grayscale[x+1][y] - grayscale[x-1][y]) 
								+ Math.abs(grayscale[x][y-1] - grayscale[x+1][y]));
					}
				}
			}
		}
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < w; x++) {
				System.out.format("%10d ", cost[x][y]);
			}
			System.out.println();
		}
	}


}
