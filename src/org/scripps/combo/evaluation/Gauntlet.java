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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.scripps.combo.evaluation.SingleGene.generank;
import org.scripps.combo.evaluation.Stats.classifierTestResult;
import org.scripps.combo.weka.Weka;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ChiSquaredAttributeEval;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.classifiers.Classifier;
import weka.classifiers.functions.SMO;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.RandomForest;

/**
 * @author bgood
 *
 */
public class Gauntlet {

	Map<String, Weka> wekas;

	public Gauntlet(){
		wekas = new HashMap<String, Weka>();
		String dataset = "griffith_breast_cancer_full_train";
		String train_file = "/Users/bgood/workspace/aacure/WebContent/WEB-INF/pubdata/griffith/griffith_breast_cancer_2.arff";		
		String test_file = "/Users/bgood/workspace/aacure/WebContent/WEB-INF/pubdata/griffith/full_test.arff";	
		Weka weka = new Weka();		
		System.out.println("loading... "+train_file);
		boolean setFeatures = false;
		try {
			weka.buildWeka(new FileInputStream(train_file), new FileInputStream(test_file), dataset, setFeatures);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wekas.put(dataset, weka);

		weka = new Weka();	
		dataset = "metabric_expression_all";
		train_file = "/Users/bgood/genegames/DataForCurePaper/Datasets/metabric_oslo/disease_specific/Metabric_clinical_expression_DSS_sample_filtered.arff";		
		test_file = "/Users/bgood/genegames/DataForCurePaper/Datasets/metabric_oslo/disease_specific/Oslo_clinical_expression_OS_sample_filt.arff";	
		//skip_first = 12; (these are the clinical features, only matters for simulations)
		try {
			weka.buildWeka(new FileInputStream(train_file), new FileInputStream(test_file), dataset, setFeatures);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		wekas.put(dataset, weka);

	}

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		measureRankingValue();
		//Gauntlet g = new Gauntlet();
		//g.runGauntlet();
	}


	public void runGauntlet() throws IOException{
		String rankingf = "/Users/bgood/workspace/aacure/database/stats/generankings/OneYear/only_cancer_and_phd_f.txt";
		Map<Integer, generank> gene_rank = readRanking(rankingf);
		String out = "/Users/bgood/workspace/aacure/database/stats/cancer_phd_top10_rf_gauntlet_merged_eval.txt";

		int ncv = 10; int nsimforp = 1; long seed = 1;
		Classifier model = null;
		//new RandomForest();
		//			((RandomForest) model).setSeed((int)seed);
		//			((RandomForest) model).setNumTrees(101);
		model = new SMO();	
		FileWriter f = new FileWriter(out);
		String header = "n_genes\tentrez_ids\tsymbols\tranks\t"
			+"cv_acc\ttest_acc\t";
		System.out.println(header);
		f.write(header+"\n");
		List<Integer> ranked_genes = new ArrayList<Integer>(gene_rank.keySet());
		int batch_size = 10;
		for(int i=0; i< ranked_genes.size()-batch_size; i+=batch_size){  //
			Set<Integer> testgenes = new HashSet<Integer>();
			String symbols = ""; String ranks = "";
			for(int b=0; b<batch_size; b++){
				Integer geneid = ranked_genes.get(i+b);
				testgenes.add(geneid);
				symbols+=gene_rank.get(geneid).symbol+","; ranks+=i+b+",";
			}
			String row =testgenes.size()+"\t"+testgenes+"\t"+symbols+"\t"+ranks+"\t";
			//build the multidataset evaluation (add multi-model?)
			////+result.train_acc+"\t"+result.cv_acc+"\t"+result.cv_auc+"\t"+result.test_acc+"\t"+result.test_auc;
			double cv_acc = 0; double test_acc = 0;
			for(String dataset: wekas.keySet()){
				Weka weka = wekas.get(dataset);
				classifierTestResult result = Stats.computeClassifierEvaluation(0, model, "", dataset, ncv, nsimforp, seed, 
						testgenes, weka, "");				
				cv_acc += result.cv_acc; test_acc+=result.test_acc;
			}
			cv_acc = cv_acc/wekas.keySet().size(); test_acc = test_acc/wekas.keySet().size();
			row+=cv_acc+"\t"+test_acc;
			System.out.println(row);
			f.write(row+"\n");
		}
		f.close();
}

public static void measureRankingValue(){
	String rankingf = "/Users/bgood/workspace/aacure/database/stats/generankings/OneYear/only_cancer_and_phd_f.txt";
	Map<Integer, generank> gene_rank = readRanking(rankingf);
	String out = "/Users/bgood/workspace/aacure/database/stats/cancer_phd_top10_rf_gauntlet_mb.txt";
	int skip_first = 0;
	String indices_keep = "";
	//		String dataset = "griffith_breast_cancer_full_train";
	//		String train_file = "/Users/bgood/workspace/aacure/WebContent/WEB-INF/pubdata/griffith/griffith_breast_cancer_2.arff";		
	//		String test_file = "/Users/bgood/workspace/aacure/WebContent/WEB-INF/pubdata/griffith/full_test.arff";	

	String dataset = "metabric_expression_all";
	//		//String indices_keep = ""; //"0,1,2,3,4,5,6,7,8,9,10,11,";
	String train_file = "/Users/bgood/genegames/DataForCurePaper/Datasets/metabric_oslo/disease_specific/Metabric_clinical_expression_DSS_sample_filtered.arff";		
	String test_file = "/Users/bgood/genegames/DataForCurePaper/Datasets/metabric_oslo/disease_specific/Oslo_clinical_expression_OS_sample_filt.arff";	
	skip_first = 12;
	int ncv = 10; int nsimforp = 1; long seed = 1;
	Classifier model = null;
	//new RandomForest();
	//		((RandomForest) model).setSeed((int)seed);
	//		((RandomForest) model).setNumTrees(101);
	model = new SMO();
	Weka weka = new Weka();		
	System.out.println("loading... "+train_file);
	boolean setFeatures = false;
	try {
		weka.buildWeka(new FileInputStream(train_file), new FileInputStream(test_file), dataset, setFeatures);
		System.out.println("Weka initialized");

		FileWriter f = new FileWriter(out);
		String header = "n_genes\tentrez_ids\tsymbols\tranks\t"
			+"train_acc\tcv_acc\tcv_auc\ttest_acc\ttest_auc\t";
		System.out.println(header);
		f.write(header+"\n");
		List<Integer> ranked_genes = new ArrayList<Integer>(gene_rank.keySet());
		int batch_size = 10;
		for(int i=0; i< ranked_genes.size()-batch_size; i+=batch_size){
			Set<Integer> testgenes = new HashSet<Integer>();
			String symbols = ""; String ranks = "";
			for(int b=0; b<batch_size; b++){
				Integer geneid = ranked_genes.get(i+b);
				testgenes.add(geneid);
				symbols+=gene_rank.get(geneid).symbol+","; ranks+=i+b+",";
			}
			classifierTestResult result = Stats.computeClassifierEvaluation(skip_first, model, indices_keep, dataset, ncv, nsimforp, seed, 
					testgenes, weka, "");				
			String row =testgenes.size()+"\t"+testgenes+"\t"+symbols+"\t"+ranks+"\t"+
			+result.train_acc+"\t"+result.cv_acc+"\t"+result.cv_auc+"\t"+result.test_acc+"\t"+result.test_auc;

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

static class generank {
	int entrez_id; int	local_id; String	symbol; double	views; double	votes;	double frequency; double	simP;
	double reliefF; double chisquared; double infogain;
}


//entrez_id	local_id	symbol	views	votes	frequency	SimP
public static Map<Integer, generank> readRanking(String file){
	Map<Integer, generank> grs = new LinkedHashMap<Integer, generank>();
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
