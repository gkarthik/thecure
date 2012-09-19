/**
 * 
 */
package org.scripps.combo.weka;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.paukov.combinatorics.CombinatoricsVector;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.combination.simple.SimpleCombinationGenerator;
import org.scripps.combo.Boardroom;
import org.scripps.combo.Boardroom.boardview;
import org.scripps.combo.model.Board;
import org.scripps.combo.weka.Weka.card;
import org.scripps.combo.weka.Weka.execution;

/**
 * Find the max possible training set score for 5 gene combinations on each board
 * @author bgood
 *
 */
public class BarneyBot {

	/**
	 * @param args
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException  {
		getMaxTrainingSetScores();
		//testComboGenerator();
	}

	public static void getMaxTrainingSetScores() throws FileNotFoundException{
		int hand_size = 3;
		//get weka ready
		String train_file = "/Users/bgood/workspace/athecure/WebContent/WEB-INF/data/dream/Exprs_CNV_2500genes.arff" ;
		String metadatafile = "/Users/bgood/workspace/athecure/WebContent/WEB-INF/data/dream/id_map.txt"; 
		Weka weka = new Weka(train_file);
		weka.setEval_method("training_set");
		weka.loadMetadata(new FileInputStream(metadatafile), true);
		//lookup the boards
		Boardroom b = new Boardroom();
		b.buildBoardView("bgood", "dream_breast_cancer");
		List<boardview> bviews = b.getBoardviews();
		int bn = 0; 
		for(boardview v : bviews){
			bn++;
			Board board = v.getBoard();
			List<String> genes = board.getEntrez_ids();

			//run through all the combos..
			// create combinatorics vector
			CombinatoricsVector<String> initialVector = new CombinatoricsVector<String>(genes);
			// create simple combination generator to generate -combination
			Generator<String> gen = new SimpleCombinationGenerator<String>(initialVector , hand_size);	    
			// create iterator
			Iterator<CombinatoricsVector<String>> itr = gen.createIterator();	    

			// go through each combo and test it
			float max = 0;
			long t = System.currentTimeMillis();
			while (itr.hasNext()) {
				CombinatoricsVector<String> combination = itr.next();
				for(String gh : combination.getVector()){
					List<card> cards = weka.geneid_cards.get(gh);
					//test it
					execution base = weka.pruneAndExecute(cards);
					if(base.eval.pctCorrect()>max){
						max = (float)base.eval.pctCorrect();
					}
				}
			}
			t = (System.currentTimeMillis()-t)/1000/60;
			board.setMax_score(max);
			board.updateMaxScore();
			System.out.println(bn+"\t"+board.getId()+"\t"+max+"\t"+t);
		}
	}

	public static void testComboGenerator(){
		// create array of initial items
		ArrayList<String> array = new ArrayList<String>();
		array.add("red");
		array.add("black");
		array.add("white");
		array.add("green");
		array.add("blue");

		// create combinatorics vector
		CombinatoricsVector<String> initialVector = new CombinatoricsVector<String>(array);

		// create simple combination generator to generate 3-combination
		Generator<String> gen = new SimpleCombinationGenerator<String>(initialVector , 3);

		// create iterator
		Iterator<CombinatoricsVector<String>> itr = gen.createIterator();

		// print the number of combinations
		System.out.println("Number of combinations is: " + gen.getNumberOfGeneratedObjects());

		// go through the iterator
		while (itr.hasNext()) {
			CombinatoricsVector<String> combination = itr.next();
			System.out.println(combination);
		}
	}

}
