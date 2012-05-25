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
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Remove;


public class Weka {

	private Instances train = null;
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
	public Weka(boolean filtered) {
		//get the data 
		DataSource source = null;
		try {
			if(filtered){
				source = new DataSource("/usr/local/data/vantveer/breastCancer-train-filtered.arff");
			}else{
				source = new DataSource("/usr/local/data/vantveer/breastCancer-train.arff");
			}
			setTrain(source.getDataSet());
			if (getTrain().classIndex() == -1){
				getTrain().setClassIndex(getTrain().numAttributes() - 1);
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
		System.out.println("launching with "+getTrain().numAttributes()+" attributes.");
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
			as.setInputFormat(getTrain());
			ranker.setOptions(options);
			Instances filtered = Filter.useFilter(getTrain(), as); 			
			double[][] ranked = ranker.rankedAttributes();
			//add the scores to the gene cards
			for(int att=0; att<ranked.length; att++){
				int att_id = (int)ranked[att][0];
				float att_value = (float)ranked[att][1];
				Attribute tmp = getTrain().attribute(att_id);
				card c = att_meta.get(tmp.name());
				if(c==null){
					c = new card(0, tmp.name(), "_", "_");
				}
				c.setPower(att_value);
				att_meta.put(tmp.name(), c);
			}
			setTrain(filtered);
			System.out.println(ranked[0][0]+" "+ranked[0][1]);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void filterForGeneIdMapping(){
		Enumeration<Attribute> atts = getTrain().enumerateAttributes();
		String nodata = "";
		while(atts.hasMoreElements()){
			Attribute a = atts.nextElement();
			if(getTrain().classIndex()!=a.index()){
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
			remove.setInputFormat(getTrain());
			setTrain(Filter.useFilter(getTrain(), remove));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void remapAttNames(){
		Enumeration<Attribute> input = getTrain().enumerateAttributes();
		geneid_cards = new HashMap<String, List<card>>();
		while(input.hasMoreElements()){
			Attribute a = input.nextElement();
			card c = att_meta.get(a.name());
			if(c.getUnique_id().equals("1999")){
				System.out.println("ELF3333 "+c.getName()+" "+c.getAtt_name());
			}
			if(c!=null){
				//also sets index properly for the first time..
				c.setAtt_index(a.index());
				//put the card back in
				if(c.getUnique_id()!=null){
					List<card> gcards = geneid_cards.get(c.getUnique_id());
					if(gcards==null){
						gcards = new ArrayList<card>();
					}					
					gcards.add(c);
					geneid_cards.put(c.getUnique_id(), gcards);
				}
				att_meta.put(a.name(),c);
				String symbol = c.getName();
				if(symbol!=null){
					getTrain().renameAttribute(a, symbol);
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
		//int count = 0;
		att_meta = new HashMap<String, card>();	
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
			Attribute a = getTrain().attribute(Integer.parseInt(i));
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
			int randomNum = rand.nextInt(getTrain().numAttributes()-1);
			//internal attribute index starts at 0
			Attribute a = getTrain().attribute(randomNum);
			if(a.index()==getTrain().classIndex()||(u.contains(randomNum+""))){
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
		}//model.getClassifier().toString()+
		public String toString(){
			return "\nTree Accuracy on test set:"+eval.pctCorrect();
		}
	}

	public class metaExecution{
		public Classifier model;
		public Evaluation eval;
		public metaExecution(Classifier model, Evaluation eval) {
			super();
			this.model = model;
			this.eval = eval;
		}
		public String toString(){
			return "\nMeta Accuracy on test set:"+eval.pctCorrect();
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
			fc.buildClassifier(getTrain());
			// evaluate classifier and print some statistics
			eval = new Evaluation(getTrain());
			if(eval_method.equals("cross_validation")){
				Random keep_same = new Random();
				keep_same.setSeed(0);
				//	System.out.println("seed "+keep_same.nextInt());
				eval.crossValidateModel(fc, getTrain(), 10, keep_same);
			}else if(eval_method.equals("test_set")){
				eval.evaluateModel(fc, test);
			}else {
				eval.evaluateModel(fc, getTrain());
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
			fc.buildClassifier(getTrain());
			// evaluate classifier and print some statistics
			eval = new Evaluation(getTrain());
			if(eval_method.equals("cross_validation")){
				//this makes the game more stable in terms of scores
				Random keep_same = new Random();
				keep_same.setSeed(0);
				eval.crossValidateModel(fc, getTrain(), 10, keep_same);
			}else if(eval_method.equals("test_set")){
				eval.evaluateModel(fc, test);
			}else {
				eval.evaluateModel(fc, getTrain());
			}
			//System.out.println(fc.getClassifier().toString()+"\n\n"+eval.toSummaryString("\nResults\n======\n", false));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String tree = fc.getClassifier().toString()+"\n\n"+eval.toSummaryString("\nResults\n======\n", false);
		return new execution(fc,eval);
	}

	/***
	 * build and test a meta classifier - a non-random forest in this case
	 * input, a set of feature sets and a classifier model
	 * output an execution result for the whole thing
	 */
	public metaExecution executeNonRandomForest(Set<String> indicesoff1, Classifier wekamodel){
		Set<String> indices_set = new HashSet<String>();
		//remap indexes
		for(String indices_ : indicesoff1){
			String indices = "";
			for(String a : indices_.split(",")){
				if(!a.equals("")){
					int i = 1+Integer.parseInt(a);
					indices += i+",";
				}
			}
			indices_set.add(indices);
		}

		//create an array of classifiers that differ from each other based on the features that they use
		Classifier[] classifiers = new Classifier[indices_set.size()];
		int i = 0;
		for(String indices : indices_set){
			// set a specific set of attributes to use to train the model
			Remove rm = new Remove();
			//don't remove the class attribute
			rm.setAttributeIndices(indices+"last");
			rm.setInvertSelection(true);
			// build a classifier using only these attributes
			FilteredClassifier fc = new FilteredClassifier();
			fc.setFilter(rm);
			fc.setClassifier(wekamodel);
			classifiers[i] = fc;
			i++;
		}
		
		//build the non-random forest
		Vote voter = new Vote();
		//-R <AVG|PROD|MAJ|MIN|MAX|MED>
		String[] options = new String[2];
		options[0] = "-R"; options[1] = "MAJ"; //avg and maj seem to work better..
		try {
			voter.setOptions(options);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		voter.setClassifiers(classifiers);
		voter.setDebug(true);
		//TODO set this properly voter.setCombinationRule();
		// train and evaluate on the test set
		Evaluation eval = null;
		try {
			voter.buildClassifier(getTrain());
			// evaluate classifier and print some statistics
			eval = new Evaluation(getTrain());
			if(eval_method.equals("cross_validation")){
				//this makes the game more stable in terms of scores
				Random keep_same = new Random();
				keep_same.setSeed(0);
				eval.crossValidateModel(voter, getTrain(), 10, keep_same);
			}else if(eval_method.equals("test_set")){
				eval.evaluateModel(voter, test);
			}else {
				eval.evaluateModel(voter, getTrain());
			}
			//System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new metaExecution(voter,eval);
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



	public void setTrain(Instances train) {
		this.train = train;
	}



	public Instances getTrain() {
		return train;
	}



	public Instances getTest() {
		return test;
	}



	public void setTest(Instances test) {
		this.test = test;
	}



	public Random getRand() {
		return rand;
	}



	public void setRand(Random rand) {
		this.rand = rand;
	}



	public String getEval_method() {
		return eval_method;
	}



	public void setEval_method(String eval_method) {
		this.eval_method = eval_method;
	}



	public Map<String, Weka.card> getAtt_meta() {
		return att_meta;
	}



	public void setAtt_meta(Map<String, Weka.card> att_meta) {
		this.att_meta = att_meta;
	}



	public Map<String, List<Weka.card>> getGeneid_cards() {
		return geneid_cards;
	}



	public void setGeneid_cards(Map<String, List<Weka.card>> geneid_cards) {
		this.geneid_cards = geneid_cards;
	}

}
