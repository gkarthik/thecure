package org.scripps.combo.model;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;

import org.scripps.util.JdbcConnection;

public class Pathway {
	
	public ArrayList searchPathways(String query) throws Exception {		
		ArrayList pathways = new ArrayList();
		JdbcConnection conn = new JdbcConnection();
		String searchString = "select * from cpdb_pathway where name LIKE '%"+query+"%' group by name";
		ResultSet rslt = conn.executeQuery(searchString);
		while (rslt.next()){
			pathways.add(rslt.getString("name"));
		}
		return pathways;
	}
	
	public ArrayList getGenesOfPathway(String pathwayName) throws Exception {		
		ArrayList pathways = new ArrayList();
		JdbcConnection conn = new JdbcConnection();
		String searchString = "select feature.short_name,feature.long_name,cpdb_pathway.entrez_id, cpdb_pathway.source_db from cpdb_pathway,feature where name='"+pathwayName+"' and cpdb_pathway.entrez_id=feature.unique_id group by cpdb_pathway.entrez_id";
		ResultSet rslt = conn.executeQuery(searchString);
		while (rslt.next()){
			HashMap row = new HashMap();
			row.put("unique_id", rslt.getString("entrez_id"));
			row.put("short_name", rslt.getString("short_name"));
			row.put("long_name", rslt.getString("long_name"));
			row.put("source", rslt.getString("source_db"));
			pathways.add(row);
		}
		return pathways;
	}
}
