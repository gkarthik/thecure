/**
 * 
 */
package org.scripps.combo.model;

import java.sql.Date;
import java.sql.Timestamp;

/**
 * Maps to a column in a weka data model.
 * @author bgood
 *create table attribute (id int(10) NOT NULL AUTO_INCREMENT, col_index int not null, name varchar(30), dataset varchar(50), created Date, updated timestamp, primary key (id));
 *
 */
public class Attribute {

	int id;
	int col_index;
	String name;
	String dataset;
	Date created;
	Timestamp updated;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDataset() {
		return dataset;
	}

	public void setDataset(String dataset) {
		this.dataset = dataset;
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

	public int getCol_index() {
		return col_index;
	}

	public void setCol_index(int col_index) {
		this.col_index = col_index;
	}

	
}
