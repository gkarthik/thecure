package org.scripps.util;

import java.awt.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.scripps.combo.model.Tree;
 
public class cpdbToMySQL
{
	/**
	 * To create mysql table cpdb_pathway from cpdb pathway tab delimited dump.
	 * Takes a while.
	 * 
	 * Last update:
	 * May 23, 2014
	 * 148654 rows added to table.
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		JdbcConnection conn = new JdbcConnection();
		ResultSet generatedKeys = null;
		String insert = "insert into cpdb_pathway (name, source_db, entrez_id) values(?,?,?)";
		PreparedStatement p = null;
		String[] terms = null;
		String[] entrezIds = null;
		try (BufferedReader br = new BufferedReader(new FileReader("/home/karthik/Downloads/CPDB_pathways_genes.tab")))
		{
			String sCurrentLine;
			while ((sCurrentLine = br.readLine()) != null){
				terms = sCurrentLine.split("\t");
				entrezIds = terms[2].toString().split(",");
				//System.out.println((java.util.Arrays.toString(entrezIds)));
				for( int i = 0; i < entrezIds.length; i++)
				{
					try {
						p = conn.connection.prepareStatement( insert, Statement.RETURN_GENERATED_KEYS);
						p.setString(1, terms[0]);
						p.setString(2, terms[1]);
						p.setString(3, entrezIds[i]);
						int affectedRows = p.executeUpdate();
						if (affectedRows == 0) {
							throw new SQLException("Creating tree failed, no rows affected.");
						}
						generatedKeys = p.getGeneratedKeys();
						while(generatedKeys.next()){
							System.out.println(generatedKeys.getInt(1));
						}
					} catch(SQLException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}