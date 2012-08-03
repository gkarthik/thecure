package org.scripps.util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ImageTiles {

	/**
	 * Renames the product of the TilePhotos application (which splits up an input image into a bunch of tiles)
	 * so that tiles are ordered with 0,0 in the top left rather than the bottom left.
	 * Making it easier to work with them in a loop generated table
	 * @param args
	 */
	public static void main(String[] args) {
		ImageTiles it = new ImageTiles();
		String input_dir = "/Users/bgood/workspace/combo/WebContent/images/cube/";
		String img_name = "4486446823_efe70a3d12.jpg";
		String new_name = "cube_bots";
		it.convertTilephotosOutputToList(input_dir, "png", img_name, new_name);
	}

	public class OnlyExt implements FilenameFilter { 
		String ext; 
		public OnlyExt(String ext) { 
			this.ext = "." + ext; 
		} 
		public boolean accept(File dir, String name) { 
			return name.endsWith(ext); 
		} 
	}
	
	public void convertTilephotosOutputToList(String input_dir, String ext, String img_name, String new_name_root){
		File f = new File(input_dir);
		FilenameFilter png = new OnlyExt(ext);
		if(f.isDirectory()){
			int max_x = 0;
			File[] files = f.listFiles(png);
			for(File img : files){
				String index = img.getName().substring(img_name.length(), img.getName().lastIndexOf(ext)-1);
				String[] indexes = index.split("_");
				int x = Integer.parseInt(indexes[1]);
				if(x>max_x){max_x = x;}
			}
			for(File img : files){
				String index = img.getName().substring(img_name.length(), img.getName().lastIndexOf(ext)-1);
				String[] indexes = index.split("_");
				int x = Integer.parseInt(indexes[1]);
				int y = Integer.parseInt(indexes[2]);
				int new_x = Math.abs(x-max_x);
				String newname = new_name_root+"_"+new_x+"_"+y+"."+ext;
				img.renameTo(new File(input_dir+newname));			
				System.out.println(img.getName()+" "+newname);
			}
		}
	}
}
