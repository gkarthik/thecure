package org.scripps.combo.weka.viz;

import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.gui.treevisualizer.PlaceNode2;
import weka.gui.treevisualizer.TreeVisualizer;


import java.applet.*;
import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.FileReader;


public class TreeVisApplet extends Applet {

	J48 j48;
	TreeVisualizer tv;
	
	public void init()
	{
		j48 = new J48();
		BufferedReader in;
		Instances data;
		try {
			in = new BufferedReader(new FileReader("/Users/bgood/programs/Weka-3-6/data/breast_labor.arff"));
			data = new Instances(in);
			data.setClassIndex(data.numAttributes()-1);
			j48.buildClassifier(data);
			System.out.println(j48.toString());
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} 
		setSize(500,400);
		BorderLayout flowLayout1 = new BorderLayout();
		this.setLayout(flowLayout1);		
		try {
			tv = new TreeVisualizer(null,  j48.graph(),  new PlaceNode2());
			//graph is the graph representation of j48 from ClassifierTree
			this.add(tv);
			this.setVisible(true);
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}//end catch

	}//end init
	
	public void start() 
	{
		tv.fitToScreen();		
	}//end start
}//end applet
