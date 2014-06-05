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

import org.apache.jasper.tagplugins.jstl.core.Set;
import org.scripps.util.JdbcConnection;

import weka.core.AttributeExpression;

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
	
	public int getOrCreateCustomFeature(String name, String feature_exp, Feature[] features) throws Exception{
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
			}
		}
		if(!exists){
			insert(name, feature_exp,features);
		}
		conn.connection.close();
		return cFeatureId;
	}
	
	public int insert(String name, String feature_exp, Feature[] features) throws Exception{
		int id = 0;
		JdbcConnection conn = new JdbcConnection();		
		PreparedStatement statement = null;
	    ResultSet generatedKeys = null;
		String insert = "insert into custom_feature(name,expression) values(?,?)";
		statement = (PreparedStatement) conn.connection.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS);
        statement.setString(1, name);
        statement.setString(2, feature_exp);

        int affectedRows = statement.executeUpdate();
        if (affectedRows == 0) {
            throw new SQLException("Creating custom feature failed, no rows affected.");
        }

        generatedKeys = statement.getGeneratedKeys();
        if (generatedKeys.next()) {
            id = generatedKeys.getInt(1);
        }
		return id;
	}	
	
	public String getName(){
		return name;
	}
	
	public String getExpression(){
		return expression;
	}
}
