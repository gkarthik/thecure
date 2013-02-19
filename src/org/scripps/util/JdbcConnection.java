/**
 * 
 */
package org.scripps.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author bgood
 *
 */
public class JdbcConnection {
	public Connection connection;
	
	public JdbcConnection(){
		String serverLocation = "127.0.0.1";
		String db = "";
		String user = "";
		String password = "";
		
		try{
	        InputStream in = JdbcConnection.class.getResourceAsStream("/props/database.properties") ;
	        Properties props = new Properties();
	        props.load(in);
	        serverLocation = props.getProperty("serverLocation");
	        db = props.getProperty("db");
	        user = props.getProperty("user");
	        password = props.getProperty("password");
	       } 
	    catch(Exception e){
	        System.out.println("error" + e);
	       }	 
		
		createConnection(serverLocation, db, user, password);
	}
	
	public JdbcConnection(String serverLocation, String db, String user, String password){
		createConnection(serverLocation, db, user, password);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JdbcConnection c = new JdbcConnection();
		ResultSet r = c.executeQuery("select * from player limit 2");
		try {
			while(r.next()){
				System.out.println(r.getString(1));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	public void createConnection (String serverLocation, String db, String user, String password){
		//register driver
		try {
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} catch (InstantiationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IllegalAccessException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		//		 		hostName = "jdbc:mysql://" + serverLocation+"/"+db+"?autoReconnect=true";
		String hostName = "jdbc:mysql://" + serverLocation+"/"+db+"?autoReconnect=true&useUnicode=yes&characterEncoding=UTF-8";
		try {
			connection = DriverManager.getConnection(hostName,user,password);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

	public ResultSet executeQuery(String theQuery)	{
		ResultSet  response;
		Statement stmt;		

		try{
			stmt = connection.createStatement();
			response = stmt.executeQuery(theQuery); //	Send the request
			return response;
			}
		catch ( SQLException e ) {
			e.printStackTrace();
		}
		return null;
	}
	
	public boolean executeStatement(String theSQL)	{
		boolean  response;
		Statement stmt;		

		try{
			stmt = connection.createStatement();
			response = stmt.execute(theSQL); //	Send the request
			return response;
			}
		catch ( SQLException e ) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean executeUpdate(String theStatement)	{
		Statement stmt;		

		try{
			stmt = connection.createStatement();
			stmt.executeUpdate(theStatement); //	Send the request
			return true;
			}
		catch ( SQLException e ) {
			e.printStackTrace();
		}
		return false;
	}

}
