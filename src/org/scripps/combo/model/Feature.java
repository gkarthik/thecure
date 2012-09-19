package org.scripps.combo.model;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.List;

import org.scripps.util.JdbcConnection;

/**
 * Selectable features for the cure/combo game.
 * For the time being most of these are going to be genes..
 * @author bgood
 * create table feature (id int(10) NOT NULL AUTO_INCREMENT, unique_id varchar(50) not null, short_name varchar(30), long_name varchar(250), description text, created Date, updated timestamp, primary key (id));
 *
 */
public class Feature {

	int id; //database
	String unique_id; //public
	String short_name;
	String long_name;
	String description;
	//a single feature (gene) may map to multiple attributes (e.g. probe set ids)
	List<String> dataset_attributes; // >> dataset attribute
	Date created;
	Timestamp updated;
	
	public static void main(String args[]){
		Feature f = Feature.getByUniqueId("48");
		System.out.println(f.id+" "+f.unique_id+" "+f.description);
	}
	
	/**
	 * Insert a new feature.
	 * @throws SQLException 
	 */
	public boolean insert() throws SQLException{
		JdbcConnection conn = new JdbcConnection();
		ResultSet generatedKeys = null; PreparedStatement pst = null;
		try {
			pst = conn.connection.prepareStatement(
					"insert into feature (id, unique_id, short_name, long_name, description, created) values(null,?,?,?,?,?)",
					 Statement.RETURN_GENERATED_KEYS);
			pst.clearParameters();
			pst.setString(1,getUnique_id());
			pst.setString(2,getShort_name());
			pst.setString(3,getLong_name());
			pst.setString(4, getDescription());
			pst.setDate(5, new Date(System.currentTimeMillis()));
			
			int affectedRows = pst.executeUpdate();
	        if (affectedRows == 0) {
	            throw new SQLException("Creating feature failed, no rows affected.");
	        }
	        generatedKeys = pst.getGeneratedKeys();
	        if (generatedKeys.next()) {
	            setId(generatedKeys.getInt(1));
	            //use to link up the feature to the attribute ids
	            
	        } else {
	            throw new SQLException("Creating feature failed, no generated key obtained.");
	        }
	    } finally {
	        if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException logOrIgnore) {}
	        if (pst != null) try { pst.close(); } catch (SQLException logOrIgnore) {}
	        if (conn.connection != null) try { conn.connection.close(); } catch (SQLException logOrIgnore) {}
	    }
	    return true;
	}
	
	public static Feature getByDbId(int id){
		Feature f = null;
		JdbcConnection conn = new JdbcConnection();
		String q = "select * from feature where id = "+id;

		ResultSet rslt = conn.executeQuery(q);
		try {
			if(rslt.next()){
				f = new Feature();
				f.setCreated(rslt.getDate("created"));
				f.setUpdated(rslt.getTimestamp("updated"));
				f.setDescription(rslt.getString("description"));
				f.setId(id);
				f.setLong_name(rslt.getString("long_name"));
				f.setShort_name(rslt.getString("short_name"));
				f.setUnique_id(rslt.getString("unique_id"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return f;
	}
	
	public static Feature getByUniqueId(String unique_id){
		Feature f = null;
		JdbcConnection conn = new JdbcConnection();
		String q = "select * from feature where unique_id = "+unique_id;

		ResultSet rslt = conn.executeQuery(q);
		try {
			if(rslt.next()){
				f = new Feature();
				f.setCreated(rslt.getDate("created"));
				f.setUpdated(rslt.getTimestamp("updated"));
				f.setDescription(rslt.getString("description"));
				f.setId(rslt.getInt("id"));
				f.setLong_name(rslt.getString("long_name"));
				f.setShort_name(rslt.getString("short_name"));
				f.setUnique_id(unique_id);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return f;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}

	public String getShort_name() {
		return short_name;
	}
	public void setShort_name(String short_name) {
		this.short_name = short_name;
	}
	public String getLong_name() {
		return long_name;
	}
	public void setLong_name(String long_name) {
		this.long_name = long_name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public List<String> getDataset_attributes() {
		return dataset_attributes;
	}
	public void setDataset_attributes(List<String> dataset_attributes) {
		this.dataset_attributes = dataset_attributes;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Timestamp getUpdated() {
		return updated;
	}
	public void setUpdated(Timestamp updated) {
		this.updated = updated;
	}

	public String getUnique_id() {
		return unique_id;
	}

	public void setUnique_id(String unique_id) {
		this.unique_id = unique_id;
	}

	
	
}
