/**
 * 
 */
package org.scripps.combo.model;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jasper.tagplugins.jstl.core.Set;
import org.scripps.combo.weka.Weka;
import org.scripps.combo.weka.Weka.execution;
import org.scripps.combo.weka.viz.JsonTree;
import org.scripps.util.JdbcConnection;
import org.scripps.combo.model.Attribute;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.SMO;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.ManualTree;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.unsupervised.attribute.AddExpression;
import weka.filters.unsupervised.attribute.Remove;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hp.hpl.jena.mem.ArrayBunch;
import com.mysql.jdbc.PreparedStatement;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;

public class CustomClassifier {
	
	public static void main(String args[]) throws FileNotFoundException, Exception{
		CustomClassifier c = new CustomClassifier();
		List entrezIds = new ArrayList();
		entrezIds.add("8459");//Tpst2
		entrezIds.add("1960");//Egr3
		entrezIds.add("6790");//Aurka
		entrezIds.add("7155");//Top2B
		entrezIds.add("675");//brca2
		Weka weka = new Weka();
		String dataset="metabric_with_clinical";
		String train_file = "/home/karthik/workspace/cure/WebContent/WEB-INF/data/Metabric_clinical_expression_DSS_sample_filtered.arff";
		HashMap<String,FilteredClassifier> _c = new HashMap<String,FilteredClassifier>();
		try {
			weka.buildWeka(new FileInputStream(train_file), null, dataset);
			//System.out.println(c.getOrCreateClassifierId(entrezIds, 0, "test", "testing classifier",  -1, weka, dataset,_c));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * On init of MetaServer, this function used to build all classifiers from db.
	 * After that function used to create classifiers and add them to global object, custom_classifiers 
	 */
	
	public int insertandAddCustomClassifier(String[] featureDbIds, int classifierType, String name, String description,  int player_id, Weka weka, String dataset, HashMap<String,Classifier> custom_classifiers) throws Exception{
		HashMap mp = new HashMap();
		int custom_classifier_id = 0;
		FilteredClassifier fc = buildCustomClasifier(weka, featureDbIds, classifierType);
			//Insert into Database
			PreparedStatement statement = null;
			JdbcConnection conn = new JdbcConnection(); 
			statement = (PreparedStatement) conn.connection.prepareStatement("insert into custom_classifier(name,type,description,player_id) values(?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
	        statement.setString(1, name);
	        statement.setInt(2, classifierType);
	        statement.setString(3, description);
	        statement.setInt(4, player_id);
	        int affectedRows = statement.executeUpdate();
	        if (affectedRows == 0) {
	            throw new SQLException("Creating custom classifier failed, no rows affected.");
	        }
	        ResultSet generatedKeys = statement.getGeneratedKeys();
	        if(generatedKeys.next()){
	        	custom_classifier_id = generatedKeys.getInt(1);
	        }
	        if (custom_classifier_id == 0) {
	            throw new SQLException("Creating custom classifier failed, no rows affected.");
	        }
			for(String id: featureDbIds){
				statement = (PreparedStatement) conn.connection.prepareStatement("insert into custom_classifier_feature(custom_classifier_id, feature_id) values(?,?)", Statement.RETURN_GENERATED_KEYS);
		        statement.setString(1, String.valueOf(custom_classifier_id));
		        statement.setString(2, id);
		        affectedRows = statement.executeUpdate();
		        if (affectedRows == 0) {
		            throw new SQLException("Creating custom classifier failed, no rows affected.");
		        }
			}
			conn.connection.close();
			custom_classifiers.put("custom_classifier_"+custom_classifier_id,fc);
		return custom_classifier_id;
	}
	
	public FilteredClassifier buildCustomClasifier(Weka weka, String[] featureDbIds, int classifierType){
		Instances data = weka.getTrain();
		String att_name = "";
		String indices = new String();
		for(String featureDbId : featureDbIds){
			List<Attribute> atts = Attribute.getByFeatureDbId(featureDbId);
			if(atts!=null&&atts.size()>0){
				for(Attribute att : atts){
					att_name = att.getName();
				}
				indices += String.valueOf(data.attribute(att_name).index()+1)+",";
			}
		}
		System.out.println("Building Classifier");
		System.out.println(indices);
		Remove rm = new Remove();
		rm.setAttributeIndices(indices+"last");
		rm.setInvertSelection(true);		// build a classifier using only these attributes
		FilteredClassifier fc = new FilteredClassifier();
		fc.setFilter(rm);
		switch(classifierType){
		case 0:
			fc.setClassifier(new J48());
			System.out.println("J48");
			break;
		case 1:
			fc.setClassifier(new SMO());
			System.out.println("SMO");
			break;
		}
		try {
			fc.buildClassifier(data);
			System.out.println("Built Classifier");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return fc;
	}
	
	public HashMap getOrCreateClassifierId(List entrezIds, int classifierType, String name, String description,  int player_id, Weka weka, String dataset, HashMap<String,Classifier> custom_classifiers) throws SQLException{
		int index = 0;
		int row_id = -1;
		String query = "select * from custom_classifier";
		JdbcConnection conn = new JdbcConnection();
		ResultSet rslt = conn.executeQuery(query);
		ResultSet rslt2 = null;
		Boolean exists = false;
		String[] featureDbIds = new String[entrezIds.toArray().length];
		Feature f;
		String message = "Classifier training completed.";
		int ctr = 0;
		for(Object entrezId : entrezIds.toArray()){
			f = new Feature();
			System.out.println(entrezId.toString());
			f = Feature.getByUniqueId(entrezId.toString());
			System.out.println(f.getId());
			featureDbIds[ctr] = String.valueOf(f.getId());
			ctr++;
		}
		while(rslt.next()){
			if(rslt.getString("name").equals(name)){
				exists = true;
				row_id = rslt.getInt("id");
				message = "Classifier with same name already exists.";
				break;
			}
			query = "select * from custom_classifier_feature where custom_classifier_id="+rslt.getInt("id");
			if(rslt.getInt("type")==classifierType){
				rslt2 = conn.executeQuery(query);
				rslt2.last();
				int count = rslt2.getRow();
				rslt2.beforeFirst();
				int match = 0;
				HashSet hs;
				HashSet hs_orig;
				if(count==featureDbIds.length){
					hs = new HashSet();
					hs_orig = new HashSet(Arrays.asList(featureDbIds));
					while(rslt2.next()){
						hs.add(rslt2.getString("feature_id"));
					}
					if(hs.containsAll(hs_orig)){
						exists = true;
						row_id = rslt.getInt("id");
						message = "Classifier with same attributes already exists.";
						break;
					}
				}
			}
		}
		HashMap mp = new HashMap();
		if(!exists){
			try {
				row_id = insertandAddCustomClassifier(featureDbIds, classifierType, name, description, player_id, weka, dataset, custom_classifiers);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		query = "select * from custom_classifier where id="+row_id;
		rslt = conn.executeQuery(query);
		HashMap results = new HashMap();
		while(rslt.next()){
			results.put("name",rslt.getString("name"));
			results.put("description",rslt.getString("description"));
			results.put("type",rslt.getInt("type"));
			results.put("id",rslt.getInt("id"));
			results.put("exists",exists);
			results.put("message", message);
		}
		return results;
	}
	
	//On init
	public LinkedHashMap<String, Classifier> getClassifiersfromDb(Weka weka, String dataset) throws SQLException{
		String query = "select * from custom_classifier";
		JdbcConnection conn = new JdbcConnection();
		ResultSet rslt = conn.executeQuery(query);
		LinkedHashMap<String, Classifier> listOfClassifiers = new LinkedHashMap<String, Classifier>();
		int id = -1;
		int classifierType = 0;
		while(rslt.next()){
			id = rslt.getInt("id");
			classifierType = rslt.getInt("type");
			try {
				listOfClassifiers.put("custom_classifier_"+id, getClassifierByDbId(id, classifierType, weka, dataset));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		conn.connection.close();
		return listOfClassifiers;
	}
	
	public FilteredClassifier getClassifierByDbId(int id, int classifierType, Weka weka, String dataset) throws Exception{
		String query = "select * from custom_classifier_feature where custom_classifier_id="+id;
		JdbcConnection conn = new JdbcConnection();
		ResultSet rslt = conn.executeQuery(query);
		rslt.last();
		int count = rslt.getRow();
		rslt.beforeFirst();
		String[] featuresDbId = new String[count];
		int ctr = 0;
		while(rslt.next()){
			featuresDbId[ctr] = rslt.getString("feature_id");
			ctr++;
		} 
		return buildCustomClasifier(weka, featuresDbId, classifierType);
	}
	
	public HashMap getClassifierDetailsByDbId(int id, String dataset, LinkedHashMap<String, Classifier> custom_classifiers) throws Exception{
		HashMap mp = new HashMap();
		String query = "select * from custom_classifier where id="+id;
		JdbcConnection conn = new JdbcConnection();
		ResultSet rslt = conn.executeQuery(query);
		ResultSetMetaData metaData = rslt.getMetaData();
		int count = metaData.getColumnCount();
		while(rslt.next()){
			for (int i = 1; i <= count; i++)
			{
				mp.put(metaData.getColumnLabel(i),rslt.getObject(i));
			}
		}
		query = "select feature.short_name, attribute.name from custom_classifier_feature, feature, attribute where custom_classifier_id = "+id+" and feature.id = custom_classifier_feature.feature_id and attribute.feature_id = feature.id and attribute.dataset='"+dataset+"'";
		rslt = conn.executeQuery(query);
		HashMap features = new HashMap();
		while(rslt.next()){
			features.put(rslt.getString("short_name"),rslt.getString("name"));
		}
		String classifierString = custom_classifiers.get("custom_classifier_"+id).toString();
		mp.put("features", features);
		mp.put("classifierString",classifierString);
		conn.connection.close();
		return mp;
	}
	
	public ArrayList searchCustomClassifiers(String query){
		ArrayList results = new ArrayList();
		HashMap mp = new HashMap();
		String statement = "select * from custom_classifier where name like '%"+query+"%' or description like '%"+query+"%'";
		JdbcConnection conn = new JdbcConnection();
		ResultSet rslt = conn.executeQuery(statement);
		try {
			while(rslt.next()){
				mp = new HashMap();
				mp.put("name",rslt.getString("name"));
				mp.put("description",rslt.getString("description"));
				mp.put("id", "custom_classifier_"+rslt.getInt("id"));
				results.add(mp);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}
}
