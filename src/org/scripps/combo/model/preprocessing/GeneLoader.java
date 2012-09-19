package org.scripps.combo.model.preprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.scripps.combo.model.Feature;

/**
 * Populate the feature table with data about genes
 * @author bgood
 *
 */
public class GeneLoader {

	public static void main(String args[]){
		String gene_info_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/gene/Homo_sapiens.gene_info"; //gene info file from entrez ftp://ftp.ncbi.nih.gov/gene/DATA/
		BufferedReader f;
		try {			
			f = new BufferedReader(new FileReader(gene_info_file));
			String line = f.readLine(); 
			int c = 0;
			while(line!=null){
				c++;
				if(!line.startsWith("#")){
					String[] items = line.split("\t");
					Feature gene = new Feature();
					gene.setUnique_id(items[1]);
					gene.setShort_name(items[2]);
					gene.setLong_name(items[11]);
					gene.setDescription(items[8]);
					//chromosome 6, map location 7
					try {
						gene.insert();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				line = f.readLine(); 
				System.out.println(c);
			}
			f.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
