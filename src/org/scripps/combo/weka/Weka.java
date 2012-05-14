package org.scripps.combo.weka;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.scripps.MapFun;
import org.scripps.ontologies.go.Annotations;
import org.scripps.ontologies.go.GOterm;
import org.scripps.util.Gene;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Remove;


public class Weka {

	Instances train = null;
	Instances test = null;
	Random rand;
	String eval_method;
	public Map<String, Weka.card> att_meta;
	public Map<String, List<Weka.card>> geneid_cards; //could be multiple cards per gene if multiple reporters

	/**
	 * This class controls the processes of:
	 *  loading training and testing data, 
	 *  parameterizing and executing classifiers, 
	 *  backing interactions with dataset attributes (choosing random ones, name maps etc.)
	 */
	public Weka() {
		//get the data 
		DataSource source = null;
		try {
			//	source = new DataSource("/Users/bgood/programs/Weka-3-6/data/breast_labor.arff");
			source = new DataSource("/Users/bgood/programs/Weka-3-6/data/VantVeer/breastCancer-train.arff");
			//source = new DataSource("/usr/local/data/vantveer/breastCancer-train-filtered.arff");

			train = source.getDataSet();
			if (train.classIndex() == -1){
				train.setClassIndex(train.numAttributes() - 1);
			}
//						source = new DataSource("/Users/bgood/programs/Weka-3-6/data/VantVeer/breastCancer-test.arff");
//						test = source.getDataSet();
//						if (test.classIndex() == -1){
//							test.setClassIndex(test.numAttributes() - 1);
//						}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//load the gene name mapping file
		loadAttributeMetadata("/usr/local/data/vantveer/breastCancer-train_meta.txt");
		//filter zero information attributes
//		filterForNonZeroInfoGain();
		//only use genes with metadata
		filterForGeneIdMapping();
		System.out.println("launching with "+train.numAttributes()+" attributes.");
		//map the names so the trees look right..
		remapAttNames();
		//add the right indexes
		
		//export file
		//exportArff(train, "/Users/bgood/programs/Weka-3-6/data/VantVeer/breastCancer-train-filtered.arff");
		//get the random set up
		rand = new Random(1);
		//specify how hands evaluated {cross_validation, test_set, training_set}
		eval_method = "cross_validation";//"training_set";

	}

	public void exportArff(Instances dataset, String outfile){
		 ArffSaver saver = new ArffSaver();
		 saver.setInstances(dataset);
		 try {
			saver.setFile(new File(outfile));
			// saver.setDestination(new File("./data/test.arff"));   // **not** necessary in 3.5.4 and later
			saver.writeBatch();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void filterForNonZeroInfoGain(){
		//weka.filters.Filter.
		AttributeSelection as = new AttributeSelection();
		InfoGainAttributeEval infogain = new InfoGainAttributeEval();
		Ranker ranker = new Ranker();
		String[] options = {"-T","0.0","-N","-1"};
		as.setEvaluator(infogain);
		as.setSearch(ranker);
		try {
			as.setInputFormat(train);
			ranker.setOptions(options);
			Instances filtered = Filter.useFilter(train, as); 			
			double[][] ranked = ranker.rankedAttributes();
			//add the scores to the gene cards
			for(int att=0; att<ranked.length; att++){
				int att_id = (int)ranked[att][0];
				float att_value = (float)ranked[att][1];
				Attribute tmp = train.attribute(att_id);
				card c = att_meta.get(tmp.name());
				if(c==null){
					c = new card(0, tmp.name(), "_", "_");
				}
				c.setPower(att_value);
				att_meta.put(tmp.name(), c);
			}
			train = filtered;
			System.out.println(ranked[0][0]+" "+ranked[0][1]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void filterForGeneIdMapping(){
		Enumeration<Attribute> atts = train.enumerateAttributes();
		String nodata = "";
		while(atts.hasMoreElements()){
			Attribute a = atts.nextElement();
			if(train.classIndex()!=a.index()){
				String n = a.name();
				card meta = att_meta.get(n);
				if(meta==null||meta.unique_id==null||meta.unique_id.equals("_")){
					nodata+=(1+a.index())+",";				
				}
			}
		}
		Remove remove = new Remove();
	    remove.setAttributeIndices(nodata);
	 //    remove.setInvertSelection(new Boolean(args[2]).booleanValue());
	     try {
			remove.setInputFormat(train);
		     train= Filter.useFilter(train, remove);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void remapAttNames(){
		Enumeration<Attribute> input = train.enumerateAttributes();
		while(input.hasMoreElements()){
			Attribute a = input.nextElement();
			card c = att_meta.get(a.name());
			//also sets index properly for the first time..
			c.setAtt_index(a.index());
			if(c!=null){
				String symbol = c.getName();
				if(symbol!=null){
					train.renameAttribute(a, symbol);
				}
			}
		}
	}

	/**
	 * attribute       symbol  geneid
	 * Get data to use for displaying the attributes
	 * @param metadatafile
	 */
	public void loadAttributeMetadata(String metadatafile){
		int count = 0;
		att_meta = new HashMap<String, card>();	
		geneid_cards = new HashMap<String, List<card>>();
		BufferedReader f;
		try {
			f = new BufferedReader(new FileReader(metadatafile));
			String line = f.readLine();
			line = f.readLine(); //skip header
			while(line!=null){
				String[] item = line.split("\t");
				String attribute = item[0];
				String name = item[1];
				String id = item[2];
				if(item!=null&&item.length>1){
					card genecard = new card(0, attribute, name, id);
					att_meta.put(attribute, genecard);
					if(name!=null){
						att_meta.put(name,genecard);
					}
					if(id!=null){
						List<card> cards = geneid_cards.get(id);
						if(cards==null){
							cards = new ArrayList<card>();
						}
						cards.add(genecard);
						geneid_cards.put(id, cards);
						count++;
					}
				}
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
	}


	/**
	 * a card represents an attribute in a training set such as a gene in an array experiment
	 * @author bgood
	 *
	 */
	public class card{
		public int att_index;
		public String att_name;
		public String name;
		public String unique_id;
		public float power;
		//more to come here
		public card(int att_index, String att_name, String name,
				String unique_id) {
			super();
			this.att_index = att_index;
			this.att_name = att_name;
			this.name = name;
			this.unique_id = unique_id;
			//if we have name metadata use now
			if(att_meta!=null){
				if(name==""||unique_id.equals("_")){
					card tmp = att_meta.get(att_name);
					if(tmp!=null){
						name = tmp.name;
						unique_id = tmp.unique_id;
					}
				}
			}
		}
		public int getAtt_index() {
			return att_index;
		}
		public void setAtt_index(int att_index) {
			this.att_index = att_index;
		}
		public String getAtt_name() {
			return att_name;
		}
		public void setAtt_name(String att_name) {
			this.att_name = att_name;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getUnique_id() {
			return unique_id;
		}
		public void setUnique_id(String unique_id) {
			this.unique_id = unique_id;
		}
		public float getPower() {
			return power;
		}
		public void setPower(float power) {
			this.power = power;
		}


	}
	/**
	 * Get cards (with names..) from a list of index numbers
	 * @param indices
	 * @return
	 */
	public List<card> getCardsByIndices(String indices){
		List<card> cards = new ArrayList<card>();
		String[] index = indices.split(",");
		for(String i : index){
			Attribute a = train.attribute(Integer.parseInt(i));
			if(a!=null){
				card c = new card(a.index(),a.name(),"","");
				cards.add(c);
			}
		}
		return cards;
	}
	/**
	 * Get a random selection of n attributes
	 * @param n
	 * @return
	 */
	public List<card> getRandomCards(int n, int ranseed){
		rand.setSeed((long)ranseed);
		Set<String> u = new HashSet<String>();
		List<card> cards = new ArrayList<card>();
		for(int i=0;i<n;i++){
			int randomNum = rand.nextInt(train.numAttributes()-1);
			//internal attribute index starts at 0
			Attribute a = train.attribute(randomNum);
			if(a.index()==train.classIndex()||(u.contains(randomNum+""))){
				i--;
			}else{
				card c = new card(a.index(),a.name(),"","");
				if(att_meta.get(a.name())!=null){
					card tmp = att_meta.get(a.name());
					c.name = tmp.name;
					c.unique_id = tmp.unique_id;
					c.setPower(tmp.getPower());
				}
				cards.add(c);
				u.add(randomNum+"");
			}
		}
		return cards;
	}

	/**
	 * Hold the results of training and testing a FilteredClassifier
	 * @author bgood
	 *
	 */
	public class execution{
		public FilteredClassifier model;
		public Evaluation eval;
		public execution(FilteredClassifier model, Evaluation eval) {
			super();
			this.model = model;
			this.eval = eval;
		}
		public String toString(){
			return model.getClassifier().toString()+"\nAccuracy on test set:"+(int)eval.pctCorrect();
		}
	}

	/**
	 * Trains a j48 decision tree using just the attributes specified in indices
	 * @param indices for the attributes to use to train the model
	 * @return
	 */
	public execution pruneAndExecute(String indicesoff1){
		String indices = "";
		for(String a : indicesoff1.split(",")){
			if(!a.equals("")){
				int i = 1+Integer.parseInt(a);
				indices += i+",";
			}
		}

		//specify a classifier
		// classifier
		J48 j48 = new J48();
		j48.setUnpruned(true);        // using an unpruned J48		
		// set a specific set of attributes to use to train the model
		Remove rm = new Remove();
		//don't remove the class attribute
		rm.setAttributeIndices(indices+"last");
		rm.setInvertSelection(true);
		// build a classifier using only these attributes
		FilteredClassifier fc = new FilteredClassifier();
		fc.setFilter(rm);
		fc.setClassifier(j48);
		// train and evaluate on the test set
		Evaluation eval = null;
		try {
			fc.buildClassifier(train);
			// evaluate classifier and print some statistics
			eval = new Evaluation(train);
			if(eval_method.equals("cross_validation")){
				Random keep_same = new Random();
				keep_same.setSeed(0);
			//	System.out.println("seed "+keep_same.nextInt());
				eval.crossValidateModel(fc, train, 10, keep_same);
			}else if(eval_method.equals("test_set")){
				eval.evaluateModel(fc, test);
			}else {
				eval.evaluateModel(fc, train);
			}
			//System.out.println(fc.getClassifier().toString()+"\n\n"+eval.toSummaryString("\nResults\n======\n", false));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new execution(fc,eval);
	}

	public execution pruneAndExecute(String indicesoff1, Classifier wekamodel){
		String indices = "";
		for(String a : indicesoff1.split(",")){
			if(!a.equals("")){
				int i = 1+Integer.parseInt(a);
				indices += i+",";
			}
		}
		// set a specific set of attributes to use to train the model
		Remove rm = new Remove();
		//don't remove the class attribute
		rm.setAttributeIndices(indices+"last");
		rm.setInvertSelection(true);
		// build a classifier using only these attributes
		FilteredClassifier fc = new FilteredClassifier();
		fc.setFilter(rm);
		fc.setClassifier(wekamodel);
		// train and evaluate on the test set
		Evaluation eval = null;
		try {
			fc.buildClassifier(train);
			// evaluate classifier and print some statistics
			eval = new Evaluation(train);
			if(eval_method.equals("cross_validation")){
				//this makes the game more stable in terms of scores
				Random keep_same = new Random();
				keep_same.setSeed(0);
				eval.crossValidateModel(fc, train, 10, keep_same);
			}else if(eval_method.equals("test_set")){
				eval.evaluateModel(fc, test);
			}else {
				eval.evaluateModel(fc, train);
			}
			//System.out.println(fc.getClassifier().toString()+"\n\n"+eval.toSummaryString("\nResults\n======\n", false));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return new execution(fc,eval);
	}
	
	/***
	 * convenience method for running learning algorithms on a set of 'cards'
	 * @param cards
	 * @return
	 */
	public execution pruneAndExecute(List<card> cards){
		String indices = "";
		for(card c : cards){
			indices+=c.att_index+",";
		}
		return pruneAndExecute(indices);
	}

}
