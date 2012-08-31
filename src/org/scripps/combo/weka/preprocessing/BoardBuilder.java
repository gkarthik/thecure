package org.scripps.combo.weka.preprocessing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;

import org.scripps.combo.Board;
import org.scripps.combo.weka.Weka;
import org.scripps.combo.weka.Weka.card;

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
		String train_file = "/Users/bgood/workspace/athecure/WebContent/WEB-INF/data/dream/Exprs_CNV_10yr.arff" ;
		String metadatafile = "/Users/bgood/workspace/athecure/WebContent/WEB-INF/data/dream/Illumina2entrez.txt"; 
		int n_per_board = 25;
		try {
			createRandomBoardsByGenes(train_file, metadatafile, n_per_board);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
/**
 * Loads the database with pre-generated genesets to use for boards
 * @param train_file
 * @param metadatafile
 * @param n_per_board
 * @param outfile
 * @throws FileNotFoundException
 */
	public static void createRandomBoardsByGenes(String train_file, String metadatafile, int n_per_board) throws FileNotFoundException{	
		Weka weka = new Weka(train_file);
		weka.loadMetadata(new FileInputStream(metadatafile));
		//produce the list of unique entrez gene ids
		//Set<String> gene_ids = weka.geneid_cards.keySet();
		List<String> gene_ids = new ArrayList<String>(weka.geneid_cards.keySet());
		System.out.println(gene_ids.size());
		int board_id = 1;
		Collections.shuffle(gene_ids);
		Board board = new Board();
		board.setPhenotype("dream_breast_cancer");
		for(String gene : gene_ids){
			board.getEntrez_ids().add(gene);
			List<card> cards = weka.geneid_cards.get(gene);
			String gene_symbol = "";
			for(card c : cards){
				gene_symbol = c.getName();
				board.getAttribute_names().add(c.getAtt_name());
			}
			board.getGene_symbols().add(gene_symbol);
			
			if(board_id%n_per_board==0){
				board.insert();
				board = new Board();
				board.setPhenotype("dream_breast_cancer");
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
