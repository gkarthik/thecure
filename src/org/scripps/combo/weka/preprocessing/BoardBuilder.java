package org.scripps.combo.weka.preprocessing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.scripps.MapFun;
import org.scripps.combo.Board;
import org.scripps.combo.weka.Weka;
import org.scripps.combo.weka.Weka.card;
import org.scripps.combo.weka.Weka.execution;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.Attribute;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

public class BoardBuilder {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String train_file = "/Users/bgood/workspace/athecure/WebContent/WEB-INF/data/dream/Exprs_CNV_2500genes.arff" ;
		String metadatafile = "/Users/bgood/workspace/athecure/WebContent/WEB-INF/data/dream/id_map.txt"; 
		int n_per_board = 25;
		int total = 99;
		int topper = 1; //the number of highly ranked genes to prepopulate in each board
		try {
			//createInterestingBoardsByGenes(train_file, metadatafile, n_per_board, total, topper);
			String progenes = "1164,51203,9055,9833,332,983,9768,9133,10112,9232,6790,991,51659,4085,11065,1163,9212,8318,51514,7153,3148,7298,3020,5341,9582";
			loadSelectedGeneBoard(train_file, metadatafile,progenes);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Use an attribute ranking system to generate hopefully useful boards..
	 * @param train_file
	 * @param metadatafile
	 * @param n_per_board
	 * @throws FileNotFoundException
	 */
	public static void createInterestingBoardsByGenes(String train_file, String metadatafile, int n_per_board, int total, int topper) throws FileNotFoundException{	
		String[] badgenids = {"100129828","11039","642567","100133941","1159","730415","2495","3105","3020","23117","7381","1880","100130526","3119","NA"};
		List<String> bad = Arrays.asList(badgenids);
		Weka weka = new Weka(train_file);
		weka.loadMetadata(new FileInputStream(metadatafile), true);
		//all genes
		List<String> gene_ids = new ArrayList<String>(weka.geneid_cards.keySet());
		//produce the list of unique entrez gene ids
		System.out.println("N gene ids: "+gene_ids.size());
		gene_ids.removeAll(bad);
		System.out.println("N filtered gene ids: "+gene_ids.size());

		//get the genes sorted by max power of related probes etc.
		Map<String, Double> power_map = new HashMap<String, Double>();
		for(String gene : gene_ids){
			List<card> cards = weka.geneid_cards.get(gene);
			double max_power = -10;
			for(card c : cards){
				if(c.getPower()>max_power){
					max_power = c.getPower();
				}
			}
			power_map.put(gene, max_power);
		}
		List<String> sorted_genes = MapFun.sortMapByValue(power_map);
		//best on top
		Collections.reverse(sorted_genes);
		//randomize the other list
		Collections.shuffle(gene_ids);
				
		//build the boards by sampling 2 from the top, then the rest at random
		for(int board_id =1; board_id<=total; board_id++){
			Board board = new Board();
			List<card> bcards = new ArrayList<card>();
			board.setPhenotype("dream_breast_cancer");

			List<String> boardids = new ArrayList<String>();
			//pop 3 good ones
			for(int t=0;t<topper; t++){
				boardids.add(sorted_genes.get(0));
				//remove it from both lists
				gene_ids.remove(sorted_genes.get(0));
				sorted_genes.remove(0);
			}

			//add the rest
			for(int r=topper; r<n_per_board;r++){
				boardids.add(gene_ids.get(0));
				sorted_genes.remove(gene_ids.get(0));
				gene_ids.remove(0);
			}
			//save the board
			for(String gene : boardids){
				board.getEntrez_ids().add(gene);
				List<card> cards = weka.geneid_cards.get(gene);
				bcards.addAll(cards);
				String gene_symbol = "";
				for(card c : cards){
					gene_symbol = c.getName();
					board.getAttribute_names().add(c.getAtt_name());
				}
				board.getGene_symbols().add(gene_symbol);
			}
			//test it
			execution base = weka.pruneAndExecute(bcards);
			float base_score = (float)base.eval.pctCorrect();
			board.setBase_score(base_score);
			board.insert();
			System.out.println(board_id+"\t"+base_score+"\t"+gene_ids.size()+"\t"+sorted_genes.size()+"\t"+board.getGene_symbols());
		}
	}
	
	
	
		public static void loadSelectedGeneBoard(String train_file, String metadatafile, String csv_geneids) throws FileNotFoundException{
			Weka weka = new Weka(train_file);
			weka.loadMetadata(new FileInputStream(metadatafile), false);
			//produce the list of unique entrez gene ids
			//Set<String> gene_ids = weka.geneid_cards.keySet();

			String[] gene_ids = csv_geneids.split(",");
			System.out.println("N gene ids: "+gene_ids.length);
			Board board = new Board();
			List<card> bcards = new ArrayList<card>();
			board.setPhenotype("dream_breast_cancer");
			for(String gene : gene_ids){
				board.getEntrez_ids().add(gene);
				List<card> cards = weka.geneid_cards.get(gene);
				bcards.addAll(cards);
				String gene_symbol = "";
				for(card c : cards){
					gene_symbol = c.getName();
					board.getAttribute_names().add(c.getAtt_name());
				}
				board.getGene_symbols().add(gene_symbol);
			}
			execution base = weka.pruneAndExecute(bcards);
			float base_score = (float)base.eval.pctCorrect();
			board.setBase_score(base_score);
			board.insert();
			board = new Board();
			board.setPhenotype("dream_breast_cancer");
			bcards = new ArrayList<card>();

		}

		/**
		 * Loads the database with randomly generated genesets to use for boards
		 * @param train_file
		 * @param metadatafile
		 * @param n_per_board
		 * @param outfile
		 * @throws FileNotFoundException
		 */
		public static void createRandomBoardsByGenes(String train_file, String metadatafile, int n_per_board) throws FileNotFoundException{	
			String[] badgenids = {"100129828","11039","642567","100133941","1159","730415","2495","3105","3020","23117","7381","1880","100130526","3119","NA"};
			List<String> bad = Arrays.asList(badgenids);
			Weka weka = new Weka(train_file);
			weka.loadMetadata(new FileInputStream(metadatafile), false);
			//produce the list of unique entrez gene ids
			//Set<String> gene_ids = weka.geneid_cards.keySet();
			List<String> gene_ids = new ArrayList<String>(weka.geneid_cards.keySet());
			System.out.println("N gene ids: "+gene_ids.size());
			gene_ids.removeAll(bad);
			System.out.println("N filtered gene ids: "+gene_ids.size());
			int board_id = 1;
			Collections.shuffle(gene_ids);
			Board board = new Board();
			List<card> bcards = new ArrayList<card>();
			board.setPhenotype("dream_breast_cancer");
			for(String gene : gene_ids){
				board.getEntrez_ids().add(gene);
				List<card> cards = weka.geneid_cards.get(gene);
				bcards.addAll(cards);
				String gene_symbol = "";
				for(card c : cards){
					gene_symbol = c.getName();
					board.getAttribute_names().add(c.getAtt_name());
				}
				board.getGene_symbols().add(gene_symbol);

				if(board_id%n_per_board==0){
					execution base = weka.pruneAndExecute(bcards);
					float base_score = (float)base.eval.pctCorrect();
					board.setBase_score(base_score);
					board.insert();
					board = new Board();
					board.setPhenotype("dream_breast_cancer");
					bcards = new ArrayList<card>();
				}
				board_id++;
			}

		}


		/** 
		 * For a given training dataset, divide up the features into random groups of 25.
		 * Rate each of these 'boards' for difficulty based on a cross-validation test with all the features on the board
		 * @throws FileNotFoundException 
		 */
		public static void createRandomBoardsByFeatures(String train_file, int n_per_board, String outfile) throws FileNotFoundException{

			System.out.println("board_id\tAttributes\tAtt_indexes\tcv_accuracy\tauc");
			FileWriter f = null;
			try {
				f = new FileWriter(outfile);
				f.write("board_id\tAttributes\tAtt_indexes\tcv_accuracy\tauc\n");
				f.close();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
			Weka weka = new Weka(train_file);
			Instances realtrain = new Instances(weka.getTrain());
			//round off so that all boards are the same size even if you lose a couple features
			double max_board = n_per_board*Math.floor((realtrain.numAttributes()-1)/n_per_board);

			for(int r=0;r<max_board;r+=n_per_board){
				weka.setTrain(new Instances(realtrain));
				List<Integer> keepers = new ArrayList<Integer>();
				String names = ""; String ids = "";
				for(int g=r; g<(r+n_per_board); g++){
					keepers.add(g);
					names+=realtrain.attribute(g).name()+",";
					ids+=(g+1)+","; // for easy weka inspecting
				}
				//keep the class index
				keepers.add(weka.getTrain().classIndex());
				//remove the rest
				Remove remove = new Remove();
				remove.setInvertSelection(true);
				int[] karray = new int[keepers.size()];
				int c = 0;

				for(Integer i : keepers){
					karray[c] = i;
					c++;
				}
				remove.setAttributeIndicesArray(karray);
				try {
					remove.setInputFormat(weka.getTrain());
					weka.setTrain(Filter.useFilter(weka.getTrain(), remove));
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				J48 classifier = new J48();

				try {
					//cross-validation
					Evaluation eval_cross = new Evaluation(weka.getTrain());
					eval_cross.crossValidateModel(classifier, weka.getTrain(), 10, weka.getRand());
					//				//training set 
					//				Evaluation eval_train = new Evaluation(weka.getTrain());
					//				classifier.buildClassifier(weka.getTrain());
					System.out.println(r+"\t"+names+"\t"+ids+"\t"+eval_cross.pctCorrect()+"\t"+eval_cross.areaUnderROC(0));
					f = new FileWriter(outfile, true);					
					f.write(r+"\t"+names+"\t"+ids+"\t"+eval_cross.pctCorrect()+"\t"+eval_cross.areaUnderROC(0)+"\n");
					f.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}


		}
	}
