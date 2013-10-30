/**
 * 
 */
package org.scripps.combo.evaluation;

import java.io.BufferedReader;
import java.io.FileInputStream;
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

import org.scripps.combo.evaluation.Stats.classifierTestResult;
import org.scripps.combo.model.Feature;
import org.scripps.combo.weka.Weka;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ChiSquaredAttributeEval;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.classifiers.Classifier;
import weka.classifiers.trees.DecisionStump;
import weka.core.Attribute;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;

/**
 * @author bgood
 *
 */
public class SingleGene {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		measureGenePredictivePower();
	}

	public static void measureGenePredictivePower(){
		String rankingf = "/Users/bgood/workspace/aacure/database/stats/generankings/OneYear/all_players.txt";
		Map<Integer, generank> gene_rank = readRanking(rankingf);
		String out = "/Users/bgood/workspace/aacure/database/stats/singlegene/all_players_stump_griffith.txt";
		int skip_first = 0;
		String indices_keep = "";
		String dataset = "griffith_breast_cancer_full_train";
		String train_file = "/Users/bgood/workspace/aacure/WebContent/WEB-INF/pubdata/griffith/griffith_breast_cancer_2.arff";		
		String test_file = "/Users/bgood/workspace/aacure/WebContent/WEB-INF/pubdata/griffith/full_test.arff";	
		Classifier model = new DecisionStump();
		int ncv = 1; int nsimforp = 1; long seed = 1;
		Weka weka = new Weka();		
		System.out.println("loading... "+train_file);
		boolean setFeatures = false;
		try {
			weka.buildWeka(new FileInputStream(train_file), new FileInputStream(test_file), dataset, setFeatures);
			System.out.println("Weka initialized");
			//set the training_set derived feature power scores
			ASEvaluation relief = new ReliefFAttributeEval();
			System.out.println("Running ReliefF");
			Map<Integer, Double[]> gene_relief = getAttevalScore(relief, weka);
			ASEvaluation chisquared = new ChiSquaredAttributeEval();
			System.out.println("Running chi-squared");
			Map<Integer, Double[]> gene_chi = getAttevalScore(chisquared, weka);
			ASEvaluation infogain = new InfoGainAttributeEval();
			System.out.println("Running info gain");
			Map<Integer, Double[]> gene_info = getAttevalScore(infogain, weka);
			System.out.println("Running classifier eval");
			FileWriter f = new FileWriter(out);
			String header = "gr.entrez_id\tgr.symbol\tgr.frequency\tgr.simP\t"
				+"result.train_acc\tresult.cv_acc\tresult.test_acc\t+" +
				"gene_relief.get(gene_id)[0]\tgene_relief.get(gene_id)[1]\t"+
				"gene_chi.get(gene_id)[0]\tgene_chi.get(gene_id)[1]\t"+
				"gene_info.get(gene_id)[0]\tgene_info.get(gene_id)[1]\t";
			System.out.println(header);
			f.write(header+"\n");

			for(Integer gene_id : gene_rank.keySet()){
				Set<Integer> testgenes = new HashSet<Integer>();
				testgenes.add(gene_id);
				classifierTestResult result = Stats.computeClassifierEvaluation(skip_first, model, indices_keep, dataset, ncv, nsimforp, seed, 
						testgenes, weka, "");
				generank gr = gene_rank.get(gene_id);
				String row =gr.entrez_id+"\t"+gr.symbol+"\t"+gr.frequency+"\t"+gr.simP+"\t"
				+result.train_acc+"\t"+result.cv_acc+"\t"+result.test_acc+"\t";
				if(gene_relief.get(gene_id)!=null){
					row+=gene_relief.get(gene_id)[0]+"\t"+gene_relief.get(gene_id)[1]+"\t";
					row+=gene_chi.get(gene_id)[0]+"\t"+gene_chi.get(gene_id)[1]+"\t";
					row+=gene_info.get(gene_id)[0]+"\t"+gene_info.get(gene_id)[1]+"\t";
				}else{
					row+="?\t?\t";
					row+="?\t?\t";
					row+="?\t?\t";
				}
				System.out.println(row);
				f.write(row+"\n");
			}
			f.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * get a gene-centric feature ranking 
	 * @param evaluator
	 * @param weka
	 * @return map from entrez gene id to array with [0] = best probe val for relief and [1] = average across all related probes
	 */
	public static Map<Integer, Double[]> getAttevalScore(ASEvaluation evaluator, Weka weka){
		Map<Integer, List<Double>> gene_scores = new HashMap<Integer, List<Double>>();
		AttributeSelection as = new AttributeSelection();
		Ranker ranker = new Ranker();
		as.setEvaluator(evaluator);
		as.setSearch(ranker);
		try {
			as.setInputFormat(weka.getTrain());
			Instances filtered = Filter.useFilter(weka.getTrain(), as); 			
			double[][] ranked = ranker.rankedAttributes();
			for(int att=0; att<ranked.length; att++){
				int att_id = (int)ranked[att][0];
				double att_value = (double)ranked[att][1];
				weka.core.Attribute tmp = weka.getTrain().attribute(att_id);
				org.scripps.combo.model.Attribute dbatt = org.scripps.combo.model.Attribute.getByAttNameDataset(tmp.name(), weka.getDataset());
				if(dbatt!=null){
					Feature f = Feature.getByDbId(dbatt.getFeature_id());
					int entrez = Integer.parseInt(f.getUnique_id());
					List<Double> probe_scores = gene_scores.get(entrez);
					if(probe_scores==null){
						probe_scores = new ArrayList<Double>();
					}
					probe_scores.add(att_value);
					gene_scores.put(entrez, probe_scores);
				}else{
					// no mapping to gene System.out.println(tmp.name());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<Integer, Double[]> gene_pscores = new HashMap<Integer, Double[]>();
		for(Integer k : gene_scores.keySet()){
			List<Double> vals = gene_scores.get(k);
			double max = -100; double avg = 0;
			for(Double d : vals){
				if(d>max){
					max = d;
				}
				avg+=d;
			}
			avg = (double)(avg/vals.size());
			Double[] pscore = new Double[2];
			pscore[0] = max; pscore[1] = avg;
			gene_pscores.put(k, pscore);
		}

		return gene_pscores;
	}

	//entrez_id	local_id	symbol	views	votes	frequency	SimP

	static class generank {
		int entrez_id; int	local_id; String	symbol; double	views; double	votes;	double frequency; double	simP;
		double reliefF; double chisquared; double infogain;
	}

	public static Map<Integer, generank> readRanking(String file){
		Map<Integer, generank> grs = new HashMap<Integer, generank>();
		try {
			BufferedReader f = new BufferedReader(new FileReader(file));
			String line = f.readLine().trim();
			while(line!=null){				
				if(!line.startsWith("#")&&line.length()>0&&!line.startsWith("Gene")&&!line.startsWith("\"Gene Id")&&!line.startsWith("entrez")){
					String[] s = line.split("\t");
					if(s!=null&&s.length>5){
						int entrez = Integer.parseInt(s[0]);
						int local = Integer.parseInt(s[1]);
						String symbol = s[2];
						double views = Double.parseDouble(s[3]);
						double votes = Double.parseDouble(s[4]);
						double freq = Double.parseDouble(s[5]);
						double simp = Double.parseDouble(s[6]);
						generank g = new generank();
						g.entrez_id = entrez; 
						g.local_id = local;
						g.symbol = symbol;
						g.views = views;
						g.votes = votes;
						g.frequency = freq;
						g.simP = simp;
						grs.put(entrez, g);
					}
				}
				line = f.readLine();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return grs;
	}

}
