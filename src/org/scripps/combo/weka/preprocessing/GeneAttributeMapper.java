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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.scripps.combo.weka.Weka;
import org.scripps.combo.weka.Weka.card;
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
		//		String input = "/Users/bgood/programs/Weka-3-6/data/VantVeer/breastCancer-train.arff";
		//		String input_meta = "/Users/bgood/programs/Weka-3-6/data/VantVeer/genes.csv";
		//		String output = "/Users/bgood/programs/Weka-3-6/data/VantVeer/breastCancer-train_meta.txt";
		//		convertVantVeer(input, input_meta, output);

		//	String input = "/Users/bgood/genegames/craniosynostosis_1.txt";
		//	String meta_out = "/Users/bgood/genegames/craniosynostosis_1_meta.txt";
		//prepareCraniosynostosisMetadata(input, meta_out);

		//		String input = "/Users/bgood/genegames/cranio/craniosynostosis_1.txt";
		//		String out = "/Users/bgood/genegames/cranio/craniosynostosis_transposed_lambdoid_control.txt";
		//		prepareCraniosynostosisData(input, out);
		String clinical_input = "/Users/bgood/workspace/combo/WebContent/WEB-INF/data/griffith/filtered_combined_data_anno.final.test.2.txt";
		String input = "/Users/bgood/workspace/combo/WebContent/WEB-INF/data/griffith/processed_final2_test_survival_combined_gcrma.txt";
		String output = "/Users/bgood/workspace/combo/WebContent/WEB-INF/data/griffith/full_test.txt";
		//prepareGriffithBreastCancerData(clinical_input, input, output);
		filterGriffithDataUsingList("/Users/bgood/workspace/combo/WebContent/WEB-INF/data/griffith/full_gene_set_from_paper.txt",
				"/Users/bgood/workspace/combo/WebContent/WEB-INF/data/griffith/full_test.arff",
				"/Users/bgood/workspace/combo/WebContent/WEB-INF/data/griffith/full_filtered_test.arff");
	}

	public static void filterGriffithDataUsingList(String input_list, String arff_data, String output){
		BufferedReader f;
		Set<String> probes = new HashSet<String>();
		try {			
			f = new BufferedReader(new FileReader(input_list));
			String line = f.readLine(); 
			while(line!=null){
				String[] items = line.split(" ");
				String id = items[1].replace("(", ""); id = id.replace(")","");
				probes.add(id);
				line = f.readLine(); 
			}
			f.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//now filter
		Weka weka;
		try {
			weka = new Weka(arff_data);
			weka.loadAttributeMetadata("/Users/bgood/workspace/combo/WebContent/WEB-INF/data/griffith/griffith_meta.txt");
			//remove the metadata if gene not in the filtered list
			Map<String, Weka.card> att_meta = new HashMap<String, Weka.card>(weka.getAtt_meta());
			for(Entry<String, card> att_meta_ : weka.getAtt_meta().entrySet()){
				String attribute = att_meta_.getValue().getAtt_name();
				attribute = attribute.substring(attribute.indexOf("_")+1);
				String symbol = att_meta_.getValue().getName();
				if(!probes.contains(attribute)){
					att_meta.remove(att_meta_.getKey());
				}else{
					System.out.println("kept "+attribute+"/t"+symbol);
				}
			}
			weka.setAtt_meta(att_meta);
			//remove the genes not in the list
			weka.filterForGeneIdMapping();
			System.out.println("filtered n attributes = "+weka.getTrain().numAttributes());
			weka.exportArff(weka.getTrain(), output);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
	

	/**
	 * Transpose the file for weka, strip out unneeded info. Note prepended probe ids with local unique ids to ensure uniqueness
	 * Same for manually constructed metadata file
	 * @param input_tab
	 * @param output_tab
	 */
	public static void prepareGriffithBreastCancerData(String clinical_input, String input_tab, String output_tab){
		Weka weka = new Weka();
		weka.loadAttributeMetadata("/Users/bgood/workspace/combo/WebContent/WEB-INF/data/griffith/griffith_meta.txt");

		//read and map sample names to class values;
		Map<String, String> sample_class = new HashMap<String, String>();
		BufferedReader f;

		try {			
			f = new BufferedReader(new FileReader(clinical_input));
			f.readLine(); //skip header
			String line = f.readLine(); 
			while(line!=null){
				//need these to get the classes out of the other file
				String[] items = line.split("\t");
				String sample_id = items[3];
				String survival = items[18]; //NA, O, 1
				sample_class.put(sample_id, survival);
				line = f.readLine(); 
			}
			f.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	 	List<String[]> readrows = new ArrayList<String[]>();
		String[] sample_ids = null;
		try {			
			f = new BufferedReader(new FileReader(input_tab));
			String line = f.readLine();
			//need these to get the classes out of the other file
			sample_ids = line.split("\t");
			//first row of data
			line = f.readLine(); 
			int nr = 1; int skipped = 0;
			while(line!=null){
				String[] items = line.split("\t");
				boolean keep = true;
				if(nr>1){//remove probes with no related metadata
					if(weka.att_meta.get(items[0])==null){
						keep=false;
						skipped++;
					}
				}
				if(keep){
					readrows.add(items);
				}
				line = f.readLine();
				nr++;
			}
			f.close();
			System.out.println("No metadata for "+skipped);
			int new_row_count = readrows.get(0).length;
			FileWriter writer = new FileWriter(output_tab);
			//new_row_count
			for(int r=0;r<new_row_count; r++){
				String new_row = "";
				//String class_att = readrows.get(0)[r];
				String sample_id = sample_ids[r].replace(".CEL", "");
				String class_att = sample_class.get(sample_id);
				if(r==0){
					class_att = "Phenotype";
				}

				boolean keep = true;				
				if(class_att==null||class_att.equals("NA")){
					keep = false;
				}else if(class_att.equals("1")){
					class_att = "relapse";
				}else if(class_att.equals("0")){
					class_att = "no relapse";
				}
				if(r==0||r>2){
					if(r==0||keep){
						for(int c=0; c<readrows.size(); c++){
							new_row+=readrows.get(c)[r]+"\t";
						}
						writer.write(new_row+class_att+"\n");
					}
				}
			}
			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 
	}

	/**
	 * Transpose the file for weka, strip out unneeded info.
	 * @param input_tab
	 * @param output_tab
	 */
	public static void prepareCraniosynostosisData(String input_tab, String output_tab){
		Weka weka = new Weka();
		weka.loadAttributeMetadata("/Users/bgood/genegames/cranio/craniosynostosis_1_meta.txt");
		BufferedReader f;
		List<String[]> readrows = new ArrayList<String[]>();
		try {			
			f = new BufferedReader(new FileReader(input_tab));
			String line = f.readLine();
			line = f.readLine(); //skip column header and ids (we may need those someday, but not now)

			int nr = 1; int skipped = 0;
			while(line!=null){
				String[] items = line.split("\t");
				boolean keep = true;
				if(nr>2){//remove probes with no gene id
					if(weka.att_meta.get(items[0])==null){
						keep=false;
						skipped++;
					}
				}
				if(keep){
					readrows.add(items);
				}
				line = f.readLine();
				nr++;
			}
			f.close();
			System.out.println("No mapping for "+skipped);
			int new_row_count = readrows.get(0).length;
			FileWriter writer = new FileWriter(output_tab);
			//new_row_count
			for(int r=0;r<new_row_count; r++){
				String new_row = "";
				String class_att = readrows.get(0)[r];
				if(r==0){
					class_att = "Phenotype";
				}
				// all phenotypes				
				//				if(class_att.equalsIgnoreCase("M")){
				//					class_att = "Metopic";
				//				}else if(class_att.equalsIgnoreCase("C")){
				//					class_att = "Coronal";
				//				}else if(class_att.equalsIgnoreCase("S")){
				//					class_att = "Sagittal";
				//				}else if(class_att.equalsIgnoreCase("L")){
				//					class_att = "Lambdoid";
				//				}else if(class_att.equalsIgnoreCase("X")){
				//					class_att = "Control";
				//				}

				//case control				
				if(class_att.equalsIgnoreCase("X")){
					class_att = "Normal";
				}else{
					class_att = "Craniosynostosis";
				}

				boolean keep = true;				
				//metopic
				//				if(class_att.equalsIgnoreCase("X")){
				//					class_att = "Normal";
				//				}	
				//				else if(class_att.equalsIgnoreCase("M")){
				//					class_att = "Metopic";
				//				}else{
				//					keep = false;
				//				}

				//coronal
				//								if(class_att.equalsIgnoreCase("X")){
				//									class_att = "Normal";
				//								}	
				//								else if(class_att.equalsIgnoreCase("C")){
				//									class_att = "Coronal";
				//								}else{
				//									keep = false;
				//								}
				//sagital
				//				if(class_att.equalsIgnoreCase("X")){
				//					class_att = "Normal";
				//				}	
				//				else if(class_att.equalsIgnoreCase("S")){
				//					class_att = "Sagittal";
				//				}else{
				//					keep = false;
				//				}

				//lambdoid
				//				if(class_att.equalsIgnoreCase("X")){
				//					class_att = "Normal";
				//				}	
				//				else if(class_att.equalsIgnoreCase("L")){
				//					class_att = "Lamddoid";
				//				}else{
				//					keep = false;
				//				}


				if(r!=1){
					if(r==0||keep){
						for(int c=1; c<readrows.size(); c++){
							new_row+=readrows.get(c)[r]+"\t";
						}
						writer.write(new_row+class_att+"\n");
					}
				}
			}
			writer.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Input is tab-delimited file with preprocessed Affy Human Gene 1.0 array data and annotation data tacked on.
	 * Generate a meta-data table with gene ids.
	 * @param input_tab
	 * @param data_out
	 * @param meta_out
	 */
	public static void prepareCraniosynostosisMetadata(String input_tab, String meta_out){
		BufferedReader f;
		int nmissing_geneids = 0; int nfound = 0; int i = 0;
		try {
			FileWriter writer = new FileWriter(meta_out);
			writer.write("attribute\tsymbol\tgeneid\n");

			f = new BufferedReader(new FileReader(input_tab));
			String line = f.readLine();
			line = f.readLine(); //skip column header
			line = f.readLine(); // skip classes
			while(line!=null){
				String[] item = line.split("\t");
				String attribute = item[0];
				String gene_symbol = item[item.length-1].trim();
				String geneid = null;
				if(gene_symbol!=null){
					if(gene_symbol.contains("/")){
						gene_symbol = gene_symbol.split("/")[0]; //only look at the first one. (usually its just a repeat)
					}
					Set<String> ids = MyGeneInfo.mapGeneSymbol2NCBIGene(gene_symbol);
					if(ids!=null&&ids.size()>0){
						geneid = ids.iterator().next();
					}
					if(geneid!=null){
						writer.write(attribute+"\t"+gene_symbol+"\t"+geneid+"\n");
						nfound++;
					}else{
						nmissing_geneids++;
					}
				}
				if(i%100==0){
					System.out.println(i+"\t"+nfound+"\t"+nmissing_geneids);
				}
				i++;
				line = f.readLine();
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
	}

	/**
	 * map symbols to gene ids
	 * @param input_tab
	 * @param meta_out
	 */
	public static void mapMetadata(String input_tab, String meta_out){
		BufferedReader f;
		int nmissing_geneids = 0; int nfound = 0; int i = 0;
		try {
			FileWriter writer = new FileWriter(meta_out);
			writer.write("attribute\tsymbol\tgeneid\n");

			f = new BufferedReader(new FileReader(input_tab));
			String line = f.readLine();
			while(line!=null){
				String[] item = line.split("\t");
				String attribute = item[0];
				String gene_symbol = item[item.length-1].trim();
				String geneid = null;
				if(gene_symbol!=null){
					if(gene_symbol.contains("/")){
						gene_symbol = gene_symbol.split("/")[0]; //only look at the first one. (usually its just a repeat)
					}
					Set<String> ids = MyGeneInfo.mapGeneSymbol2NCBIGene(gene_symbol);
					if(ids!=null&&ids.size()>0){
						geneid = ids.iterator().next();
					}
					if(geneid!=null){
						writer.write(attribute+"\t"+gene_symbol+"\t"+geneid+"\n");
						nfound++;
					}else{
						nmissing_geneids++;
					}
				}
				if(i%100==0){
					System.out.println(i+"\t"+nfound+"\t"+nmissing_geneids);
				}
				i++;
				line = f.readLine();
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
