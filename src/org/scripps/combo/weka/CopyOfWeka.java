package org.scripps.combo.weka;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;


import org.scripps.util.Gene;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.AttributeEvaluator;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.ReliefFAttributeEval;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Range;
import weka.core.SelectedTag;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.AttributeSelection;
import weka.filters.unsupervised.attribute.Remove;


public class CopyOfWeka {

	private Instances train = null;
	Instances test = null;
	Random rand;
	String eval_method;
	public Map<String, CopyOfWeka.card> att_meta;
	public Map<String, List<CopyOfWeka.card>> geneid_cards; //could be multiple cards per gene if multiple reporters

	public CopyOfWeka(){
	}

	public CopyOfWeka(String train_file) throws FileNotFoundException{
		InputStream train_stream = new FileInputStream(train_file);
		buildWeka(train_stream);
	}

	public CopyOfWeka(InputStream train_stream) throws FileNotFoundException{
		buildWeka(train_stream);
	}

	public void buildWeka(InputStream train_stream){
		//get the data 
		DataSource source;
		try {
			source = new DataSource(train_stream);
			setTrain(source.getDataSet());
			if (getTrain().classIndex() == -1){
				getTrain().setClassIndex(getTrain().numAttributes() - 1);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		rand = new Random(1);
		//specify how hands evaluated {cross_validation, test_set, training_set}
		eval_method = "cross_validation";//"training_set";
	}

	public CopyOfWeka(String train_file, String test_file, String meta_file) throws Exception{
		InputStream train_stream = new FileInputStream(train_file);
		InputStream test_stream = new FileInputStream(test_file);
		InputStream meta_stream = new FileInputStream(meta_file);
		buildWeka(train_stream, test_stream, meta_stream);
	}

	public void buildWeka(InputStream train_stream, InputStream test_stream, InputStream meta_stream) throws Exception{
		//get the data 
		DataSource source = new DataSource(train_stream);
		setTrain(source.getDataSet());
		if (getTrain().classIndex() == -1){
			getTrain().setClassIndex(getTrain().numAttributes() - 1);
		}
		train_stream.close();
		if(test_stream!=null){
			source = new DataSource(test_stream);
			test = source.getDataSet();
			if (test.classIndex() == -1){
				test.setClassIndex(test.numAttributes() - 1);
			} 
			test_stream.close();
		}
		rand = new Random(1);
		//specify how hands evaluated {cross_validation, test_set, training_set}
		eval_method = "cross_validation";//"training_set";
		if(meta_stream!=null){
			loadMetadata(meta_stream, true);
			meta_stream.close();
		}
	}

	/**
	 * This reads a three column file that maps probeset or other dataset ids to entrez gene ids and gene symbols
	 * @param config
	 * @param weka
	 * @param metadatafile
	 * @return
	 */
	public void loadMetadata(InputStream metadata, boolean add_power){
		loadAttributeMetadata(metadata);
		//add power
		if(add_power){
			ASEvaluation eval_method = new ReliefFAttributeEval();
			setCardPower(eval_method);
		}
		//only use genes with metadata
		filterForGeneIdMapping();
		//map the names so the trees look right..
		remapAttNames();
		return;
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


	public void filterTrainAndTestSetForNonZeroInfoGainAttsInTrain(){
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
			Instances filteredtest = new Instances(getTest());
			Remove remove = new Remove();
			Enumeration<Attribute> atts = filtered.enumerateAttributes();
			List<Integer> keepers = new ArrayList<Integer>();
			while(atts.hasMoreElements()){
				Attribute f = atts.nextElement();
				Attribute keep = filteredtest.attribute(f.name());
				keepers.add(keep.index());
			}
			//keep the class index
			keepers.add(getTest().classIndex());
			setTrain(filtered);
			remove.setInvertSelection(true);
			int[] karray = new int[keepers.size()];
			int c = 0;
			for(Integer i : keepers){
				karray[c] = i;
				c++;
			}
			remove.setAttributeIndicesArray(karray);
			try {
				remove.setInputFormat(filteredtest);
				setTest(Filter.useFilter(filteredtest, remove));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//			System.out.println("launching with F "+getTrain().numAttributes()+" train attributes.");
			//			System.out.println("launching with F "+getTest().numAttributes()+" test attributes.");
			//			//test 
			//			J48 j = new J48();
			//			Evaluation eval = new Evaluation(getTrain());
			//			j.buildClassifier(filtered);
			//			eval.evaluateModel(j, filteredtest);
			//			System.out.println(eval.toSummaryString());
		} catch (Exception e) {
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

	public void setCardPower(ASEvaluation eval_method){
		if(att_meta==null){
			att_meta = new HashMap<String, card>();
		}
		//weka.filters.Filter.
		AttributeSelection as = new AttributeSelection();
		Ranker ranker = new Ranker();
		//keep all
		//	String[] options = {"-T","0.0","-N","-1"};
		as.setEvaluator(eval_method);
		as.setSearch(ranker);
		try {
			as.setInputFormat(getTrain());
			//ranker.setOptions(options);
			Instances filtered = Filter.useFilter(getTrain(), as); 			
			double[][] ranked = ranker.rankedAttributes();
			//add the scores to the gene cards
			for(int att=0; att<ranked.length; att++){
				int att_id = (int)ranked[att][0];
				float att_value = (float)ranked[att][1];
				Attribute tmp = getTrain().attribute(att_id);
				card c = att_meta.get(tmp.name());
				if(c==null){
					c = new card(tmp.index(), tmp.name(), tmp.name(), "_");
				}
				c.setPower(att_value);
				att_meta.put(tmp.name(), c);
			}
			setTrain(filtered);
			//	System.out.println(ranked[0][0]+" "+ranked[0][1]);
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
				if(meta==null||meta.unique_id==null||meta.unique_id.equals("_")||meta.unique_id.equals("NA")){
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
			if(getTest()!=null){
				setTest(Filter.useFilter(getTest(), remove));
			}
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
			//			if(c.getUnique_id().equals("1999")){
			//				System.out.println("ELF3333 "+c.getName()+" "+c.getAtt_name());
			//			}
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
					String treename = symbol;
					if(a.name().startsWith("ILMN")){
						treename+="_expr";
					}else{
						treename+="_cnv";
					}
					getTrain().renameAttribute(a, treename);
					if(getTest()!=null){
						getTest().renameAttribute(a, symbol);
					}
				}
			}
		}

	}


	public void loadAttributeMetadata(InputStream metadatastream) {
		//int count = 0;
		att_meta = new HashMap<String, card>();	
		BufferedReader f;
		try {
			f = new BufferedReader(new InputStreamReader(metadatastream));
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
		public int display_loc;
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
		public int getDisplay_loc() {
			return display_loc;
		}
		public void setDisplay_loc(int display_loc) {
			this.display_loc = display_loc;
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
		if(n>train.numAttributes()){
			List<card> cards = new ArrayList<card>();
			for(int i=0;i<train.numAttributes()-1;i++){
				Attribute a = getTrain().attribute(i);
				card c = new card(a.index(),a.name(),"","");
				if(att_meta!=null){
					if(att_meta.get(a.name())!=null){
						card tmp = att_meta.get(a.name());
						c.name = tmp.name;
						c.unique_id = tmp.unique_id;
						c.setPower(tmp.getPower());
					}
				}
				cards.add(c);
			}
			return cards;
		}

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
				if(att_meta!=null){
					if(att_meta.get(a.name())!=null){
						card tmp = att_meta.get(a.name());
						c.name = tmp.name;
						c.unique_id = tmp.unique_id;
						c.setPower(tmp.getPower());
					}
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
		public double avg_percent_correct;
		public execution(FilteredClassifier model, Evaluation eval, double avg_percent_correct) {
			super();
			this.model = model;
			this.eval = eval;
			this.avg_percent_correct = avg_percent_correct;
		}//model.getClassifier().toString()+
		public String toString(){
			return "Tree Accuracy on test set:"+avg_percent_correct;
		}
	}

	public class metaExecution{
		public Classifier model;
		public Evaluation eval;
		public double avg_percent_correct;
		public metaExecution(Classifier model, Evaluation eval, double avg_percent_correct) {
			super();
			this.model = model;
			this.eval = eval;
			this.avg_percent_correct = avg_percent_correct;			
		}
		public metaExecution(Evaluation eval) {

			this.eval = eval;
		}
		public String toString(){
			return "Meta Accuracy on test set:"+eval.pctCorrect();
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
		double avg_pct_correct = 0;
		try {
			fc.buildClassifier(getTrain());
			// evaluate classifier and print some statistics
			eval = new Evaluation(getTrain());
			if(eval_method.equals("cross_validation")){
				//	for(int r=0; r<10; r++){
				int r = 0;
				Random keep_same = new Random();
				keep_same.setSeed(r);
				eval.crossValidateModel(fc, getTrain(), 10, keep_same);
				avg_pct_correct += eval.pctCorrect();
				//	}
				avg_pct_correct = avg_pct_correct/10;
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

		return new execution(fc,eval, avg_pct_correct);
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
		double avg_pct_correct = 0;
		try {
			fc.buildClassifier(getTrain());
			// evaluate classifier and print some statistics
			eval = new Evaluation(getTrain());
			if(eval_method.equals("cross_validation")){
				//this makes the game more stable in terms of scores
				//		for(int r=0; r<10; r++){
				int r = 0;
				Random keep_same = new Random();
				keep_same.setSeed(r);
				eval.crossValidateModel(fc, getTrain(), 10, keep_same);
				avg_pct_correct += eval.pctCorrect();
				//		}
				avg_pct_correct = avg_pct_correct/10;
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
		//	String tree = fc.getClassifier().toString()+"\n\n"+eval.toSummaryString("\nResults\n======\n", false);
		//	double correct = eval.pctCorrect();
		//	System.out.println("pae "+indices+" "+correct);
		return new execution(fc,eval, avg_pct_correct);
	}

	/***
	 * build and test a meta classifier - a non-random forest in this case
	 * input, a set of feature sets and a classifier model
	 * output an execution result for the whole thing
	 */
	public metaExecution executeNonRandomForest(Set<String> indicesoff1){
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
			//128,224,91,21,
			rm.setAttributeIndices(indices+"last");
			rm.setInvertSelection(true);
			// build a classifier using only these attributes
			FilteredClassifier fc = new FilteredClassifier();
			fc.setFilter(rm);
			fc.setClassifier(new J48());
			classifiers[i] = fc;
			i++;
		}


		//		//build the non-random forest
		Vote voter = new Vote();
		//		//-R <AVG|PROD|MAJ|MIN|MAX|MED>
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
		// train and evaluate 
		Evaluation eval = null;
		double avg_pct_correct = 0;
		try {
			voter.buildClassifier(getTrain());
			// evaluate classifier and print some statistics
			eval = new Evaluation(getTrain());
			if(eval_method.equals("cross_validation")){
				for(int r=0; r<10; r++){
					Random keep_same = new Random();
					keep_same.setSeed(r);
					eval.crossValidateModel(voter, getTrain(), 10, keep_same);
					avg_pct_correct += eval.pctCorrect();
				}
				avg_pct_correct = avg_pct_correct/10;
				//				//this makes the game more stable in terms of scores
				//				Random keep_same = new Random();
				//				keep_same.setSeed(0);
				//				eval.crossValidateModel(voter, getTrain(), 10, keep_same);
			}
			else if(eval_method.equals("test_set")){
				eval.evaluateModel(voter, test);
			}else {
				eval.evaluateModel(voter, getTrain());
			}
			//	System.out.println(eval.toSummaryString("\nResults\n======\n", false));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new metaExecution(voter,eval,avg_pct_correct);

	}

	/***
	 * Choose which of the input attribute sets to include in the meta classifier using an evaluation step
	 * included within the cross-validation
	 * @param indicesoff1
	 * @return
	 * @throws Exception 
	 */
	public metaExecution executeNonRandomForestWithInternalCVparamselection(Set<String> indicesoff1) throws Exception{
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

		//start outer cross-validation loop
		int numFolds = 10;
		// Make a copy of the data we can reorder
		Instances data = new Instances(getTrain());
		Evaluation eval = new Evaluation(data);
		data.randomize(getRand());
		if (data.classAttribute().isNominal()) {
			data.stratify(numFolds);
		}

		// Do the folds
		for (int i = 0; i < numFolds; i++) {
			Instances thistrain = data.trainCV(numFolds, i, getRand());
			eval.setPriors(thistrain);
			//execute attribute selection filter here
			int n_trees = 7;
			Classifier voter = getCVSelectedVoterBest(thistrain, indices_set, n_trees);
			Classifier copiedClassifier = Classifier.makeCopy(voter);
			copiedClassifier.buildClassifier(thistrain);
			Instances thistest = data.testCV(numFolds, i);
			eval.evaluateModel(copiedClassifier, thistest);
		}

		return new metaExecution(eval);


	}


	/**
	 * Given a particular training set (e.g. the training set for one fold of a cross-validation run)
	 * generate a voter classifier using only the subclassifiers that perform better than min_pctCorrect
	 * in 10-f cross-validation within this dataset.
	 * 
	 * If no subclassifiers meet the threshold, return the single best tree
	 * @param thistrain
	 * @param classifiers
	 * @return
	 */
	public Classifier getCVSelectedVoterThresholded___(Instances thistrain, Classifier[] classifiers) {
		int min = 69;
		//first select only the finest component trees 
		List<Classifier> selected = new ArrayList<Classifier>();
		int best_index = 0; double best = 0;
		for(int i=0; i<classifiers.length; i++){
			Evaluation e;
			try {
				e = new Evaluation(thistrain);
				e.crossValidateModel(classifiers[i], thistrain, 10, getRand());
				if(e.pctCorrect()>min){
					selected.add(classifiers[i]);
				}
				if(e.pctCorrect()>best){
					best = e.pctCorrect();
					best_index = i;
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if(selected.size()<1){
			return classifiers[best_index];
		}
		//now build the non-random forest
		Vote voter = new Vote();
		//		//-R <AVG|PROD|MAJ|MIN|MAX|MED>
		String[] options = new String[2];
		options[0] = "-R"; options[1] = "MAJ"; //avg and maj seem to work better..
		try {
			voter.setOptions(options);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		voter.setClassifiers(selected.toArray(new Classifier[selected.size()]));	
		return voter;
	}
	/**
	 * Given a particular training set (e.g. the training set for one fold of a cross-validation run)
	 * generate a voter classifier using only the best n_trees subclassifiers as determined by 10-f 
	 * cross-validation within this dataset.
	 * 
	 * @param thistrain
	 * @param classifiers
	 * @return
	 */
	public Classifier getCVSelectedVoterBest(Instances thistrain, Set<String> indices_set, int n_trees) {
		//create an array of classifiers that differ from each other based on the features that they use
		Classifier[] classifiers = new Classifier[indices_set.size()];
		int ii = 0;
		for(String indices : indices_set){
			// set a specific set of attributes to use to train the model
			Remove rm = new Remove();
			//don't remove the class attribute
			//128,224,91,21,
			rm.setAttributeIndices(indices+"last");
			rm.setInvertSelection(true);
			// build a classifier using only these attributes
			FilteredClassifier fc = new FilteredClassifier();
			fc.setFilter(rm);
			fc.setClassifier(new J48());
			classifiers[ii] = fc;
			ii++;
		}

		if(classifiers.length<n_trees){
			n_trees = classifiers.length;
		}
		//first select only the finest component trees 
		Map<Integer, Double> selected = new HashMap<Integer, Double>();
		for(int i=0; i<classifiers.length; i++){
			Evaluation e;
			try {
				e = new Evaluation(thistrain);
				e.crossValidateModel(classifiers[i], thistrain, 10, getRand());
				selected.put(i, e.pctCorrect());
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		//now build the non-random forest
		Vote voter = new Vote();
		//		//-R <AVG|PROD|MAJ|MIN|MAX|MED>
		String[] options = new String[2];
		options[0] = "-R"; options[1] = "MAJ"; //avg and maj seem to work better..
		try {
			voter.setOptions(options);
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		List<Integer> sorted_indexes = org.scripps.util.MapFun.sortMapByValue(selected);
		Collections.reverse(sorted_indexes);
		List<Classifier> keepers = new ArrayList<Classifier>();
		for(int i=0; i<n_trees; i++){
			keepers.add(classifiers[(int)sorted_indexes.get(i)]);
		}
		voter.setClassifiers(keepers.toArray(new Classifier[keepers.size()]));	
		return voter;
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


	/**
	 * Simple, manual attribute filtering.  Follows basic pattern of Vant'Veer 2002 and other early array processing approaches
	 * Remove attributes that don't have at least n_samples_over_min with min_expression_change.
	 * If outlier_deletion, remove any attributes that contain values over the outlier_threshold.
	 * @param min_expression_change
	 * @param n_samples_over_min
	 * @param outlier_threshold
	 * @param remove_atts_with_outliers
	 */

	public void executeManualAttFiltersTrainTest(float min_expression_change, int n_samples_over_min, int outlier_threshold, boolean remove_atts_with_outliers){
		//reduce N genes by eliminating genes not significantly regulated in at least three samples
		//	System.out.println("Train start n atts = "+getTrain().numAttributes());
		Enumeration<Attribute> atts = getTrain().enumerateAttributes();
		List<Integer> keepers = new ArrayList<Integer>();
		while(atts.hasMoreElements()){
			Attribute att = atts.nextElement();
			//check if we want to keep it
			boolean keep = false;
			Enumeration<Instance> instances = getTrain().enumerateInstances();
			int n_sig_var = 0;
			while(instances.hasMoreElements()){
				Instance instance = instances.nextElement();
				double value = instance.value(att);
				if(value>min_expression_change||value<(-1*min_expression_change)){
					n_sig_var++;
				}
				if(n_sig_var>2){
					keep = true;
				}
				if(value > outlier_threshold||value<(1*-outlier_threshold)){
					keep=false;
					break;
				}
			}
			if(keep){
				keepers.add(att.index());				
			}
		}
		//keep the class index
		keepers.add(getTrain().classIndex());
		//		System.out.println("Manual filter reduces atts to: "+keepers.size());
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
			remove.setInputFormat(getTrain());
			setTrain(Filter.useFilter(getTrain(), remove));
			if(getTest()!=null){
				remove.setInputFormat(getTest());
				setTest(Filter.useFilter(getTest(), remove));
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return;
	}

	/**
	 * Convert all continuous variables into binary, nominal attributes (yes or no..)
	 */
	public void binarize(){

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



	public Map<String, CopyOfWeka.card> getAtt_meta() {
		return att_meta;
	}



	public void setAtt_meta(Map<String, CopyOfWeka.card> att_meta) {
		this.att_meta = att_meta;
	}



	public Map<String, List<CopyOfWeka.card>> getGeneid_cards() {
		return geneid_cards;
	}



	public void setGeneid_cards(Map<String, List<CopyOfWeka.card>> geneid_cards) {
		this.geneid_cards = geneid_cards;
	}



}
