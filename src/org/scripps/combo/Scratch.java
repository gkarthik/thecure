package org.scripps.combo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.scripps.combo.weka.GoWeka;
import org.scripps.combo.weka.Weka;
import org.scripps.combo.weka.Weka.card;
import org.scripps.combo.weka.Weka.metaExecution;
import org.scripps.ontologies.go.Annotations;
import org.scripps.ontologies.go.GOowl;
import org.scripps.ontologies.go.GOterm;

import weka.attributeSelection.AttributeEvaluator;
import weka.attributeSelection.ChiSquaredAttributeEval;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Remove;



public class Scratch {
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException  {

		//buildrankedListofGenesForEnrichmentTesting();
		//String out = "/Users/bgood/genegames/go_group_trees_filtered_with_pairs_with_test.txt";
		//testAllGoClassesAsFeatureSets(out);
		//testAllGOForest();
		make70geneClassifier();
	}

	public static void buildrankedListofGenesForEnrichmentTesting() {
		GoWeka gow = new GoWeka();
		//weka.filters.Filter.
		AttributeSelection as = new AttributeSelection();
		//	InfoGainAttributeEval infogain = new InfoGainAttributeEval();
		ChiSquaredAttributeEval infogain = new ChiSquaredAttributeEval();
		Ranker ranker = new Ranker();
		String[] options = {"-T","0.0","-N","-1"};
		as.setEvaluator(infogain);
		as.setSearch(ranker);
		try {
			as.setInputFormat(gow.getTrain());
			ranker.setOptions(options);
			Instances filtered = Filter.useFilter(gow.getTrain(), as); 			
			double[][] ranked = ranker.rankedAttributes();
			//add the scores to the gene cards
			for(int att=0; att<ranked.length; att++){
				int att_id = (int)ranked[att][0];
				float att_value = (float)ranked[att][1];
				Attribute tmp = gow.getTrain().attribute(att_id);
				card c = gow.getAtt_meta().get(tmp.name());
				//				if(c==null){
				//					c = new Weka.card(0, tmp.name(), "_", "_");
				//				}
				//				c.setPower(att_value);
				gow.getAtt_meta().put(tmp.name(), c);
				if(att_value > 0){
					if(c!=null){
						System.out.println(c.unique_id+"\t"+c.name+"\t"+att_value);
					}else{
						System.out.println("\tatt:"+att_value+"\t"+att_value);
					}
				}
			}
			//train = filtered;
			System.out.println(ranked[0][0]+" "+ranked[0][1]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void addTermInfoToGOtreeData(String infile, String outfile){
		String gos = infile;
		String gos2 = outfile;
		GOowl gowl = new GOowl();
		String goowlfile = "file:/users/bgood/data/ontologies/5-12-2012/go_daily-termdb.owl";
		gowl.init(goowlfile, false);
		BufferedReader f = null;
		try {
			FileWriter w = new FileWriter(gos2);
			f = new BufferedReader(new FileReader(gos));			
			String line = f.readLine();
			while(line!=null){
				String[] item = line.split("\t");
				GOterm t = gowl.makeGOterm(item[0]);
				if(t!=null){
					w.write(t.getRoot()+"\t"+t.getTerm()+"\t"+line+"\n");
				}else{
					w.write("\t\t"+line+"\n");
				}
				line = f.readLine();
			}
			w.close();
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
	 * check to see how well the GO categories segregate useful, independent feature groups	
	 * @param out
	 * @throws IOException
	 */
	public static void testAllGOForest() throws IOException{
		GoWeka baseweka = new GoWeka();
		GOowl gowl = new GOowl();
		String goowlfile = "file:/users/bgood/data/ontologies/5-12-2012/go_daily-termdb.owl";
		gowl.init(goowlfile, false);

		//	System.out.println(s.pruneAndExecute("1,2,"));
		int ngo = 0; 
		Set<String> missing_geneids = new HashSet<String>();
		Set<String> found_geneids = new HashSet<String>();

		float best_score = 0;
		List<String> keys = new ArrayList<String>(baseweka.acc2name.keySet());
		//only use go that map to at least one gene in the filtered dataset
		keys = baseweka.getGoAccsWithMapInFilteredData(keys);

		int p = keys.size();
		//		int p = s.go2genes.keySet().size();
		System.out.println(p+" usable go terms to test");
		System.out.println("ngo\tgo\tterm\tgo_pctCorrect()_cv\tgo_pctCorrect()_test\tforest_pctCorrect()_cv\tforest_pctCorrect()_test");

		//track the sets of features for the forest
		Set<String> indices = new HashSet<String>();
		Set<String> used_go = new HashSet<String>();
		for(String go : keys){
			p--;
			Set<String> genes = baseweka.go2genes.get(go);
			String atts = "";
			//	System.out.println(genes.length+" genes in set");
			int found = 0; int n_atts = 0;
			String names = "";
			String geness = "";
			for(String gene : genes){
				String id = gene;
				List<Weka.card> cards = baseweka.geneid_cards.get(id);
				if(cards!=null){
					found_geneids.add(gene);
					found++;
					for(Weka.card card : cards){
						atts+=card.getAtt_index()+",";
						n_atts++;
						geness+=gene+",";
						names+=card.getAtt_name()+",";
					}
				}else{
					missing_geneids.add(gene);
				}
			} 

			if(found>1&&found<25){
				J48 wekamodel = new J48();
				wekamodel.setUnpruned(false); 
				baseweka.setEval_method("cross_validation");
				Weka.execution e = baseweka.pruneAndExecute(atts, wekamodel);
				double correct = e.eval.pctCorrect();
				if(correct>0){
					ngo++;
					indices.add(atts);
					used_go.add(go);
					//run the forest in cv
					metaExecution em = baseweka.executeNonRandomForest(indices);
					//now test both on the test set
					baseweka.setEval_method("test_set");
					metaExecution emtestset = baseweka.executeNonRandomForest(indices);
					Weka.execution etestset = baseweka.pruneAndExecute(atts, wekamodel);
					GOterm got = gowl.makeGOterm(go);
					System.out.println(ngo+"\t"+go+"\t"+got.getTerm()+"\t"+e.eval.pctCorrect()+"\t"+etestset.eval.pctCorrect()+"\t"+em.eval.pctCorrect()+"\t"+emtestset.eval.pctCorrect());
				}			
			}
			//runs the forest using only internal cross-validation for GO-based attribute selection
			//(no cheating by peaking ahead...)
			//			if(found>1&&found<25){
			//				ngo++;
			//				indices.add(atts);
			//				used_go.add(go);
			//				//run the forest
			//				//metaExecution em = baseweka.executeNonRandomForest(indices);
			//				if(ngo%100==0){
			//					metaExecution em;
			//					try {
			//						em = baseweka.executeNonRandomForestWithInternalCVparamselection(indices);
			//						if(em!=null){
			//							System.out.println(ngo+"\t"+go+"\t"+em.eval.pctCorrect());
			//						}else{
			//							System.out.println(ngo);
			//						}
			//					} catch (Exception e1) {
			//						// TODO Auto-generated catch block
			//						e1.printStackTrace();
			//					}
			//				}
			//			}			
		}


	}

	/** try to reproduce the result
	 *  from the 2002 VantVeer paper
	 */
	public static void make70geneClassifier(){
		//load weka with full training and testing set
		Weka weka = new Weka();
		//reduce to about 5,000 genes by eliminating genes not significantly regulated in at least three samples
		System.out.println("Train start n atts = "+weka.getTrain().numAttributes());
		Enumeration<Attribute> atts = weka.getTrain().enumerateAttributes();
		List<Integer> keepers = new ArrayList<Integer>();
		while(atts.hasMoreElements()){
			Attribute att = atts.nextElement();
			//check if we want to keep it
			boolean keep = false;
			Enumeration<Instance> instances = weka.getTrain().enumerateInstances();
			int n_sig_var = 0;
			while(instances.hasMoreElements()){
				Instance instance = instances.nextElement();
				double value = instance.value(att);
				if(value>0.3||value<-0.3){
					n_sig_var++;
				}
				if(n_sig_var>2){
					keep = true;
				}
				if(value > 10||value<-10){
					keep=false;
					break;
				}
			}
			if(keep){
				keepers.add(att.index());
			}
		}
		//keep the class index
		keepers.add(weka.getTrain().classIndex());
		System.out.println("First filter reduces atts to: "+keepers.size());
		//remove the baddies
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
			remove.setInputFormat(weka.getTest());
			weka.setTest(Filter.useFilter(weka.getTest(), remove));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Train/test filtered n atts = "+weka.getTrain().numAttributes()+"/"+weka.getTest().numAttributes());
		
	//lump the following into one attribute selected classifier
		//calculate the correlation coefficient between each gene and disease outcome 
		//find 231 genes with correlation <-0.3 or >0.3
		//sort the 231 genes by amount of correlation
		//test top 70 genes (they used the correlations of the expression profile of the 'leave-one-out' sample with the mean expression levels of the remaining samples from the good and the poor prognosis patients, respectively)
		
		AttributeSelectedClassifier classifier = new AttributeSelectedClassifier();
		ChiSquaredAttributeEval evaluator = new ChiSquaredAttributeEval();
		Ranker search = new Ranker();
		search.setNumToSelect(70);
		classifier.setEvaluator(evaluator);
		classifier.setSearch(search);
		classifier.setClassifier(new J48());
		
		//now evaluate it in cv and test set
		try {
			//cross-validation
			Evaluation eval_cv = new Evaluation(weka.getTrain());
			eval_cv.crossValidateModel(classifier, weka.getTrain(), 10, weka.getRand());
			System.out.println("10f cross-validation\n"+eval_cv.toSummaryString());
			//test set
			Evaluation eval_test = new Evaluation(weka.getTrain());
			classifier.buildClassifier(weka.getTrain());
			eval_test.evaluateModel(classifier, weka.getTest());
			System.out.println("\nTest Set\n"+eval_test.toSummaryString());
			//training set 
			Evaluation eval_train = new Evaluation(weka.getTrain());
			classifier.buildClassifier(weka.getTrain());
			eval_train.evaluateModel(classifier, weka.getTrain());
			System.out.println("\nTraining set\n"+eval_train.toSummaryString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}

	public static void testAllGoClassesAsFeatureSets(String out) throws IOException{
		GoWeka s = new GoWeka();
		//filters the training and test sets based on the good parts of the training data
		//will over-estimate performance of subsequent cross-validation.
		//s.filterTrainAndTestSetForNonZeroInfoGainAttsInTrain();
		//s.remapAttNames();
		//	System.out.println(s.pruneAndExecute("1,2,"));
		int ngo = 0; 
		Set<String> missing_geneids = new HashSet<String>();
		Set<String> found_geneids = new HashSet<String>();
		Set<String> keepers = new HashSet<String>();
		J48 wekamodel = new J48();
		wekamodel.setUnpruned(true); 
		float best_score = 0;
		List<String> keys = new ArrayList<String>(s.acc2name.keySet());
		//only use go that map to at least one gene in the filtered dataset
		keys = s.getGoAccsWithMapInFilteredData(keys);
		int p = keys.size();
		//		int p = s.go2genes.keySet().size();
		System.out.println(p+" usable go terms to test");
		FileWriter f = new FileWriter(out);
		f.write("go	pctcorrect_cv	pctcorrect_test	genes	att name	att id");
		//		for(String go : s.go2genes.keySet()){
		for(String go : keys){
			p--;
			Set<String> genes = s.go2genes.get(go);
			String atts = "";
			//	System.out.println(genes.length+" genes in set");
			int found = 0; int n_atts = 0;
			String names = "";
			String geness = "";
			for(String gene : genes){
				String id = gene;
				List<Weka.card> cards = s.geneid_cards.get(id);
				if(cards!=null){
					found_geneids.add(gene);
					found++;
					for(Weka.card card : cards){
						atts+=card.getAtt_index()+",";
						n_atts++;
						geness+=gene+",";
						names+=card.getAtt_name()+",";
					}
				}else{
					missing_geneids.add(gene);
				}
			} 
			if(found>0&&found<10){
				ngo++;
				//System.out.println(ngo+" go "+go+" had "+genes.length+" found "+found+" atts: "+atts+" genes: "+geness);
				//run the tree on the set and score it
				s.setEval_method("cross_validation");
				Weka.execution e = s.pruneAndExecute(atts, wekamodel);
				s.setEval_method("test_set");
				Weka.execution e_test = s.pruneAndExecute(atts, wekamodel);
				f.write(go+"\t"+e.eval.pctCorrect()+"\t"+e_test.eval.pctCorrect()+"\t"+n_atts+"\t"+geness+"\t"+names+"\t"+atts+"\n");
				if(e.eval.pctCorrect()>70){
					keepers.add(go);
				}
				if(e.eval.pctCorrect()> best_score){
					best_score = (float)e.eval.pctCorrect();
					System.out.println(go+"\t"+e.eval.pctCorrect()+"\t"+n_atts+"\t"+geness+"\t"+names+"\t"+atts);
				}
			}
			if(p%100==0){
				System.out.println(1-((float)p/(float)s.go2genes.keySet().size())+" percent done "+p);
			}
		}
		//check combos
		p = keepers.size()*keepers.size();
		System.out.println(p+" usable combo go terms to test");
		Set<String> done = new HashSet<String>();
		for(String go1 : keepers){
			for(String go2 : new HashSet<String>(keepers)){
				p--;
				String go2go = go1+go2; String go2go2 = go2+go1;
				if(done.contains(go2go)||done.contains(go2go2)){
					continue;
				}
				done.add(go2go); done.add(go2go2);
				Set<String> genes = s.go2genes.get(go1);
				genes.addAll(s.go2genes.get(go2));

				String atts = "";
				//	System.out.println(genes.length+" genes in set");
				int found = 0; int n_atts = 0;
				String names = "";
				String geness = "";
				for(String gene : genes){
					String id = gene;
					List<Weka.card> cards = s.geneid_cards.get(id);
					if(cards!=null){
						found_geneids.add(gene);
						found++;
						for(Weka.card card : cards){
							atts+=card.getAtt_index()+",";
							n_atts++;
							geness+=gene+",";
							names+=card.getAtt_name()+",";
						}
					}else{
						missing_geneids.add(gene);
					}
				} 
				if(found>0&&found<21){
					ngo++;
					//System.out.println(ngo+" go "+go+" had "+genes.length+" found "+found+" atts: "+atts+" genes: "+geness);
					//run the tree on the set and score it
					s.setEval_method("cross_validation");
					Weka.execution e = s.pruneAndExecute(atts, wekamodel);
					s.setEval_method("test_set");
					Weka.execution e_test = s.pruneAndExecute(atts, wekamodel);								
					f.write(go2go+"\t"+e.eval.pctCorrect()+"\t"+e_test.eval.pctCorrect()+"\t"+n_atts+"\t"+geness+"\t"+names+"\t"+atts+"\n");
					if(e.eval.pctCorrect()> best_score){
						best_score = (float)e.eval.pctCorrect();
						System.out.println(go2go+"\t"+e.eval.pctCorrect()+"\t"+n_atts+"\t"+geness+"\t"+names+"\t"+atts);
					}
				}
				if(p%100==0){
					System.out.println(1-((float)p/(float)(keepers.size()*keepers.size()))+" percent done of"+p);
				}
			}
		}
		f.close();
		System.out.println("missing genes\t"+missing_geneids.size()+"\tfound ids\t"+found_geneids.size());

	}

	public static void cacheExpandedGoAnnotations(){
		//cache go2genes
		String goowlfile = "file:/users/bgood/data/ontologies/5-12-2012/go_daily-termdb.owl";
		String anno_file = "/users/bgood/data/ontologies/5-12-2012/gene2go";
		String outputfile = "/usr/local/data/go2gene_3_51.txt";// "/users/bgood/data/ontologies/5-12-2012/go2gene_3_51.txt";
		int min_set_size = 3; int max_set_size = 50;
		try {
			//Annotations.cacheGo2Genes(anno_file, "9606", false, true, goowlfile, outputfile, min_set_size, max_set_size);
			//read them in
			Map<String, Set<String>> go2genes = Annotations.readCachedGoAcc2Genes(outputfile);
			System.out.println(go2genes.get("GO:0070830")); //\tProcess\ttight junction assembly
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


}
