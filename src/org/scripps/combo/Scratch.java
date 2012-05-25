package org.scripps.combo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

import weka.attributeSelection.ChiSquaredAttributeEval;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;



public class Scratch {
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException  {

		//buildrankedListofGenesForEnrichmentTesting();
		//String out = "/Users/bgood/genegames/go_group_trees_prefiltered_with_pairs.txt";
		//testAllGoClassesAsFeatureSets(out);
		testAllGOForest();
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

		//track the sets of features for the forest
		Set<String> indices = new HashSet<String>();
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
			
			if(found>1&&found<100){
				J48 wekamodel = new J48();
				wekamodel.setUnpruned(false); 
				Weka.execution e = baseweka.pruneAndExecute(atts, wekamodel);
				double correct = e.eval.pctCorrect();
				if(correct>69){
					GoWeka s = new GoWeka();
					ngo++;
					//System.out.println(ngo+"\n"+e.toString());
					indices.add(atts);
					//run the forest
					metaExecution em = s.executeNonRandomForest(indices);
					if(em!=null){
						System.out.println(ngo+"\ttree:\t"+e.toString()+"\tforest\t"+em.toString());
					}else{
						System.out.println(ngo);
					}
					s = null;
				}
				
			}
		}


	}

		public static void testAllGoClassesAsFeatureSets(String out) throws IOException{
			GoWeka s = new GoWeka();
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
			f.write("go	pctcorrect	genes	att name	att id");
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
					Weka.execution e = s.pruneAndExecute(atts, wekamodel);
					f.write(go+"\t"+e.eval.pctCorrect()+"\t"+n_atts+"\t"+geness+"\t"+names+"\t"+atts+"\n");
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
						Weka.execution e = s.pruneAndExecute(atts, wekamodel);
						f.write(go2go+"\t"+e.eval.pctCorrect()+"\t"+n_atts+"\t"+geness+"\t"+names+"\t"+atts+"\n");
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
