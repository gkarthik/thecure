/**
 * 
 */
package org.scripps.combo.model.preprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;

import org.scripps.combo.model.Feature;

/**
 * Load up attributes from a weka dataset - make sure they map to rows in the feature table
 * One feature >> multiple attributes
 * @author bgood
 *
 */
public class AttributeLoader {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//somewhere there needs to be a mapping between the attribute id and the feature id
		String att_info_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/dream/id_map2.txt";
		//there also needs to be a weka-structured dataset so we can pull out the column index
		String weka_data = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/dream/Exprs_CNV_2500genes.arff";
		
		BufferedReader f;
		try {			
			f = new BufferedReader(new FileReader(att_info_file));
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
