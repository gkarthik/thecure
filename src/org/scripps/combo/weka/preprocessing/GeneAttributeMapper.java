/**
 * 
 */
package org.scripps.combo.weka.preprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import org.scripps.util.MyGeneInfo;

import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * Generate an attribute metadata file that can be used for display purposes in games
 * 
 * This will probably be a bunch of one-offs to deal with each dataset
 * @author bgood
 *
 */
public class GeneAttributeMapper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String input = "/Users/bgood/programs/Weka-3-6/data/VantVeer/breastCancer-train.arff";
		String input_meta = "/Users/bgood/programs/Weka-3-6/data/VantVeer/genes.csv";
		String output = "/Users/bgood/programs/Weka-3-6/data/VantVeer/breastCancer-train_meta.txt";
		convertVantVeer(input, input_meta, output);
	}

	/**
	 * We have a mapping file that connects the attribute ids (e.g. contig.. to gene symbols)
	 * Use this to get the symbols and then use mygene.info to get the geneids for the symbols
	 * Could cache more data at this stage if useful..
	 * @param input_arff
	 * @param output_meta
	 */
	public static void convertVantVeer(String input_arff, String input_meta, String output_meta){
		//get the data 
		DataSource source = null;
		try {
			source = new DataSource(input_arff);
			Instances output = source.getDataSet();
			if (output.classIndex() == -1){
				output.setClassIndex(output.numAttributes() - 1);
			}
			//
			BufferedReader f;
			try {
				FileWriter writer = new FileWriter(output_meta);
				writer.write("attribute\tsymbol\tgeneid\n");
				f = new BufferedReader(new FileReader(input_meta));
				String line = f.readLine();
				line = f.readLine(); //skip header
				int c = 0;
				while(line!=null){
					c++;
					String[] item = line.split(",");
					if(item!=null&&item.length>1){

						String attribute = item[0];
						String symbol = "_";
						String geneid = "_";
						if(item[1]!=null){
							symbol = item[1];
							Set<String> ids = MyGeneInfo.mapGeneSymbol2NCBIGene(symbol);
							if(ids!=null&&ids.size()>0){
								geneid = ids.iterator().next();
							}						
						}
						writer.write(attribute+"\t"+symbol+"\t"+geneid+"\n");
					}
					line = f.readLine();
					System.out.println(c+" "+line);
				}
				writer.close();
				f.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
