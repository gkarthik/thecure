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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jasper.tagplugins.jstl.core.Set;
import org.scripps.combo.weka.Weka;
import org.scripps.util.JdbcConnection;

import weka.classifiers.trees.ManualTree;
import weka.core.AttributeExpression;
import weka.core.Instances;

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

public class CustomFeature {
	String name;
	String expression;	
	
	public ArrayList searchCustomFeatures(String query){
		ArrayList results = new ArrayList();
		HashMap mp = new HashMap();
		String statement = "select name, description, expression from custom_feature where name like '%"+query+"%' or description like '%"+query+"%'";
		JdbcConnection conn = new JdbcConnection();
		ResultSet rslt = conn.executeQuery(statement);
		try {
			while(rslt.next()){
				mp = new HashMap();
				mp.put("name",rslt.getString("name"));
				mp.put("description",rslt.getString("description"));
				mp.put("feature_exp",rslt.getString("expression"));
				results.add(mp);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return results;
	}
	
	public int findOrCreateCustomFeatureId(String name, String feature_exp, String description, int userid, List<Feature> features, Weka weka, String dataset) throws Exception{
		int cFeatureId = 0;
		JdbcConnection conn = new JdbcConnection();
		String getattr = "select * from custom_feature";
		ResultSet resultSet = conn.executeQuery(getattr);
		AttributeExpression _attrExp = new AttributeExpression();
		_attrExp.convertInfixToPostfix(feature_exp);
		String exp = "";
		Boolean exists = false;
		while(resultSet.next()){
			exp = resultSet.getString("expression");
			_attrExp.convertInfixToPostfix(exp);
			if(exp.equals(feature_exp)){
				exists = true;
				cFeatureId = resultSet.getInt("id");
			}
		}
		if(!exists){
			cFeatureId = insert(name, feature_exp, description, userid, features);
			ManualTree _t = new ManualTree();
			_t.evalAndAddNewFeatureValues("custom_feature_"+cFeatureId, feature_exp, weka.getTrain());
		}
		conn.connection.close();
		return cFeatureId;
	}
	
	public int insert(String name, String feature_exp, String description, int userid, List<Feature> features) throws Exception{
		int id = 0;
		JdbcConnection conn = new JdbcConnection();		
		PreparedStatement statement = null;
	    ResultSet generatedKeys = null;
		String insert = "insert into custom_feature(name,expression, description, player_id) values(?,?,?,?)";
		statement = (PreparedStatement) conn.connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, name);
        statement.setString(2, feature_exp);
        statement.setString(3, description);
        statement.setInt(4, userid);

        int affectedRows = statement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating custom feature failed, no rows affected.");
        }
        generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            id = generatedKeys.getInt(1);
        }
        
        for(Feature f : features){
        	insert="INSERT INTO custom_feature_feature(custom_feature_id,feature_id) VALUES("+id+","+f.getId()+")";
        	conn.executeUpdate(insert);
        }
        conn.connection.close();
		return id;
	}	
	
	public HashMap findOrCreateCustomFeature(String feature_name, String exp, String description, int user_id, Weka weka, String dataset){
		HashMap mp = new HashMap();
		mp.put("success", true);
		Instances data = weka.getTrain();
		Feature _feature = new Feature(); 
		List<Feature> allFeatures = new ArrayList<Feature>();
		Pattern p = Pattern.compile("@([A-Za-z0-9])+"); 
		Matcher m = p.matcher(exp);
		String entrezid = "";
		String att_name = "";
		int index = 0;
		while(m.find()){
			entrezid = m.group().replace("@", "");
			List<Attribute> atts = Attribute.getByFeatureUniqueId(entrezid,dataset);
			if(atts!=null&&atts.size()>0){
				for(Attribute att : atts){
					att_name = att.getName();
				}
				index = data.attribute(att_name).index();
				allFeatures.add(_feature.getByUniqueId(entrezid));
				index++;//WEKA AddExpression() accepts index starting from 1.
				exp = exp.replace("@"+entrezid, "a"+index);
			}else{
				mp.put("message", "Not Created. Couldn't map Genes to Dataset.");
				mp.put("success", false);
			}
		}
		CustomFeature cfeature = new CustomFeature();
		try {
			int cFeatureId = cfeature.findOrCreateCustomFeatureId(feature_name, exp, description, user_id, allFeatures, weka, dataset);
			mp.put("custom_feature_id", cFeatureId);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			mp.put("success", false);
		}
		return mp;
	}
	
	public String getName(){
		return name;
	}
	
	public String getExpression(){
		return expression;
	}
}
