/**
 * 
 */
package org.scripps.combo.model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hp.hpl.jena.mem.ArrayBunch;
import com.mysql.jdbc.PreparedStatement;

import java.security.MessageDigest;
import java.sql.ResultSetMetaData;
import java.text.SimpleDateFormat;

public class CustomClassifier {
	public HashMap buildOrCreateClassifier(String[] featureDbIds, int classifierType, Boolean exists, String name, String description,  int player_id, Weka weka, String dataset) throws Exception{
		HashMap mp = new HashMap();
		Instances data = weka.getTrain();
		String att_name = "";
		String indices = new String();
		int custom_classifier_id = 0;
		for(String featureDbId : featureDbIds){
			List<Attribute> atts = Attribute.getByFeatureDbId(featureDbId);
			if(atts!=null&&atts.size()>0){
				for(Attribute att : atts){
					att_name = att.getName();
				}
				indices += String.valueOf(data.attribute(att_name).index())+",";
			}
		}
		if(!exists){
			//Insert into Database
			PreparedStatement statement = null;
			JdbcConnection conn = new JdbcConnection(); 
			statement = (PreparedStatement) conn.connection.prepareStatement("insert into custom_classifier(name,type,description,player_id) values(?,?,?,?)", Statement.RETURN_GENERATED_KEYS);
	        statement.setString(1, name);
	        statement.setInt(2, classifierType);
	        statement.setString(3, description);
	        statement.setInt(4, player_id);
	        custom_classifier_id = statement.executeUpdate();
	        if (custom_classifier_id == 0) {
	            throw new SQLException("Creating custom classifier failed, no rows affected.");
	        }
			for(String id: featureDbIds){
				statement = (PreparedStatement) conn.connection.prepareStatement("insert into custom_classifier_feature(custom_classifier_id, feature_id) values(?,?)", Statement.RETURN_GENERATED_KEYS);
		        statement.setString(1, String.valueOf(custom_classifier_id));
		        statement.setString(2, id);
		        int affectedRows = statement.executeUpdate();
		        if (affectedRows == 0) {
		            throw new SQLException("Creating custom classifier failed, no rows affected.");
		        }
			}
			conn.connection.close();
		}
		Remove rm = new Remove();
		rm.setAttributeIndices(indices+"last");
		rm.setInvertSelection(true);
		// build a classifier using only these attributes
		FilteredClassifier fc = new FilteredClassifier();
		fc.setFilter(rm);
		switch(classifierType){
		case 1:
			fc.setClassifier(new SMO());
			break;
		default:
			fc.setClassifier(new J48());
		}
		fc.buildClassifier(data);
		mp.put("classifier", fc);
		mp.put("id", custom_classifier_id);
		return mp;
	}
	
	public HashMap getOrCreateClassifierId(List entrezIds, int classifierType, String name, String description,  int player_id, Weka weka, String dataset) throws SQLException{
		int index = 0;
		String query = "select * from custom_classifier";
		JdbcConnection conn = new JdbcConnection();
		ResultSet rslt = conn.executeQuery(query);
		ResultSet rslt2 = null;
		Boolean exists = false;
		String[] featureDbIds = new String[entrezIds.toArray().length];
		Feature f;
		int ctr = 0;
		for(Object entrezId : entrezIds.toArray()){
			f = Feature.getByUniqueId(entrezId.toString());
			featureDbIds[ctr] = String.valueOf(f.getId());
		}
		while(rslt.next()){
			query = "select * from custom_classifier_feature where custom_classifier_id="+rslt.getInt("id");
			if(rslt.getInt("type")==classifierType){
				rslt2 = conn.executeQuery(query);
				ResultSetMetaData metaData = rslt.getMetaData();
				int count = metaData.getColumnCount();
				if(count==featureDbIds.length){
					Boolean flag = true;
					while(rslt2.next()){
						for(String featureDbId : featureDbIds){
							if(!featureDbId.equals(rslt.getString("feature_id"))){
								flag = false;
							}
						}
					}
					if(flag){
						exists = true;
					}
				}
			}
		}
		HashMap mp = new HashMap();
		try {
			mp = buildOrCreateClassifier(featureDbIds, classifierType, exists, name, description, 0, weka, dataset);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		HashMap results = new HashMap();
		query = "select * from custom_feature where id="+(int) mp.get("id");
		System.out.println(query);
		rslt = conn.executeQuery(query);
		while(rslt.next()){
			results.put("name",rslt.getString("name"));
			results.put("description",rslt.getString("description"));
			results.put("type",rslt.getInt("type"));
			results.put("id",rslt.getInt("id"));
			results.put("exists",exists);
		}
		
		return results;
	}
	
	//On init
	public Classifier[] getClassifiersfromDb(Weka weka, String dataset) throws SQLException{
		String query = "select * from custom_classifier";
		JdbcConnection conn = new JdbcConnection();
		ResultSet rslt = conn.executeQuery(query);
		ResultSetMetaData metaData = rslt.getMetaData();
		int count = metaData.getColumnCount();
		Classifier[] listOfClassifiers = new Classifier[count];
		Classifier c = null;
		int id = -1;
		int classifierType = 0;
		int ctr = 0;
		while(rslt.next()){
			id = rslt.getInt("id");
			classifierType = rslt.getInt("type");
			try {
				listOfClassifiers[ctr] = getClassifierByDbId(id, classifierType, weka, dataset);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ctr++;
		}
		conn.connection.close();
		return listOfClassifiers;
	}
	
	public Classifier getClassifierByDbId(int id, int classifierType, Weka weka, String dataset) throws Exception{
		Classifier c = null;
		String query = "select * from custom_classifier_feature where custom_classifier_id="+id;
		JdbcConnection conn = new JdbcConnection();
		ResultSet rslt = conn.executeQuery(query);
		ResultSetMetaData metaData = rslt.getMetaData();
		int count = metaData.getColumnCount();
		String[] featuresDbId = new String[count];
		int ctr = 0;
		while(rslt.next()){
			featuresDbId[ctr] = rslt.getString("id");
			ctr++;
		}
		HashMap mp = buildOrCreateClassifier(featuresDbId, classifierType, true, "", "", 0, weka, dataset);
		return (Classifier) mp.get("classifier");
	}
}
