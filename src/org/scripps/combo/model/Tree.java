/**
 * 
 */
package org.scripps.combo.model;

import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scripps.util.JdbcConnection;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Represent a decision tree entered by players of The Cure
 * 
  CREATE TABLE `tree` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `player_id` int(11) NOT NULL,
  `ip` varchar(50),
  `json_tree` text not NULL,
  `comment` text,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
)

  CREATE TABLE `tree_feature` (
  `tree_id` int(11) NOT NULL,
  `feature_id` int(11) NOT NULL,
  UNIQUE KEY `tree_feature` (`feature_id`,`tree_id`)
)

  CREATE TABLE `tree_dataset_score` (
  `tree_id` int(11) NOT NULL,
  `dataset` varchar(100) NOT NULL,
  `percent_correct` float,
  `size` float,
  `novelty` float,
  `score` float,
  UNIQUE KEY `tree_score` (`tree_id`,`dataset`)
)
 * 
 * @author bgood
 *
 */
public class Tree {

	int player_id;
	int id;
	String ip;
	List<Feature> features; 
	String json_tree; // this is the blob used by the javascript client to render the tree and by the ManualTree class to evaluate it
	Date created;
	Map<String, TreeScore> dataset_score; // trees could be tried on multiple datasets
	String comment;
	int user_saved;
	String player_name;
	int rank;
	int privateflag;

	public class TreeScore{
		float size;
		float novelty;
		float percent_correct;
		float score;
		
		public String toString(){
			String s = size+"\t"+novelty+"\t"+percent_correct+"\t"+score;
			return s;
		}
	}
	
	public Tree(){
		this.player_id = 0;
		this.id = 0;
		this.features = new ArrayList<Feature>();
	}

	public Tree (int id, int player_id, String ip, List<Feature> features, String json_tree, String comment, int user_saved, int privateflag) {
		this.player_id = player_id;
		this.id = id;
		this.ip = ip;
		this.features = features;
		this.json_tree = json_tree;
		this.comment = comment;
		this.user_saved = user_saved; 
		this.privateflag = privateflag;
	}

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		Tree tree_ = new Tree();
		/*
		List<Tree> trees = tree_.getAll(); //add controls to get by user, get all	
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode treess = tree_.getTreeListAsJson(trees, mapper);
		String json = mapper.writeValueAsString(treess);
		System.out.println(json);
		*/
		Tree _tree = new Tree();
		try {
			_tree.migrateDatabase();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public ObjectNode getTreeListAsJson(List<Tree> trees, ObjectMapper mapper){
		ObjectNode root = mapper.createObjectNode();
		root.put("n_trees", trees.size());
		ArrayNode treelist = mapper.createArrayNode();
		for(Tree tree : trees){
			ObjectNode treeobj = mapper.createObjectNode();
			treeobj.put("comment",tree.comment);
			treeobj.put("id",tree.id);
			treeobj.put("ip",tree.ip);
			String date = new SimpleDateFormat("EEE, d MMM yyyy 'at' HH:mm").format(tree.created);
			treeobj.put("created", date);
			treeobj.put("user_saved",tree.user_saved);
			treeobj.put("private",tree.privateflag);
			treeobj.put("player_id", tree.player_id);
			treeobj.put("rank", tree.rank);
			treeobj.put("player_name", tree.player_name);
			JsonNode jtree;
			try {
				jtree = mapper.readTree(tree.json_tree);
				treeobj.put("json_tree", jtree);
				treelist.add(treeobj);
			} catch (JsonProcessingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		root.put("trees",treelist);
		return root;
	}

	public List<Tree> getAll(){
		List<Tree> trees = new ArrayList<Tree>();
		String q = "select * from tree";
		JdbcConnection conn = new JdbcConnection();
		try {
			ResultSet ts = conn.executeQuery(q);
			while(ts.next()){
				Tree tree = new Tree(ts.getInt("id"), player_id, ts.getString("ip"), null, ts.getString("json_tree"), ts.getString("comment"), ts.getInt("user_saved"), ts.getInt("private"));
				tree.created = ts.getTimestamp("created");
				String fq = "select * from tree_feature where tree_id="+tree.id;
				ResultSet fs = conn.executeQuery(fq);
				List<Feature> features = new ArrayList<Feature>();
				while(fs.next()){
					int fid = fs.getInt("feature_id");
					Feature f = Feature.getByDbId(fid);
					if(f!=null){
						features.add(f);
					}
				}
				tree.features = features;
				//scores
				ResultSet scores = conn.executeQuery("select * from tree_dataset_score where tree_id="+tree.id);
				if(scores!=null){
					Map<String, TreeScore> data_score = new HashMap<String, TreeScore>();
					while(scores.next()){
						TreeScore score = new TreeScore();
						score.novelty = scores.getFloat("novelty");
						score.percent_correct = scores.getFloat("percent_correct");
						score.size = scores.getFloat("size");
						score.score = scores.getFloat("score");
						data_score.put(scores.getString("dataset"), score);
					}
					tree.dataset_score = data_score;
				}
				trees.add(tree);
			}
			ts.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return trees;
	}

	public List<Tree> getForPlayer(int player_id, boolean getPrivate){
		List<Tree> trees = new ArrayList<Tree>();
		JdbcConnection conn = new JdbcConnection();
		String q0 = "SET @i=0;";
		conn.executeQuery(q0);
		String q="";
		if(getPrivate){
			q = "select @i:=@i+1 as rank, tree.* from tree inner join tree_dataset_score on tree.id=tree_dataset_score.tree_id and tree_dataset_score.score!=0 and tree.user_saved=1 and tree.player_id="+player_id+" order by tree_dataset_score.score desc";
		} else {
			q = "select @i:=@i+1 as rank, tree.* from tree inner join tree_dataset_score on tree.id=tree_dataset_score.tree_id and tree_dataset_score.score!=0 and tree.user_saved=1 and tree.private=0 and tree.player_id="+player_id+" order by tree_dataset_score.score desc";
		}
		try {
			ResultSet ts = conn.executeQuery(q);
			while(ts.next()){
				Tree tree = new Tree(ts.getInt("id"), ts.getInt("player_id"), ts.getString("ip"), null, ts.getString("json_tree"), ts.getString("comment"), ts.getInt("user_saved"), ts.getInt("private"));
				tree.created = ts.getTimestamp("created");
				tree.rank = ts.getInt("rank");
				String player_name = "";
				try{
					ResultSet player = conn.executeQuery("select * from player where id="+ts.getInt("player_id"));
					while(player.next()){
						player_name = player.getString("name");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				tree.player_name = player_name;
				String fq = "select * from tree_feature where tree_id="+tree.id;
				ResultSet fs = conn.executeQuery(fq);
				List<Feature> features = new ArrayList<Feature>();
				while(fs.next()){
					int fid = fs.getInt("feature_id");
					Feature f = Feature.getByDbId(fid);
					if(f!=null){
						features.add(f);
					}
				}
				tree.features = features;
				//scores
				ResultSet scores = conn.executeQuery("select * from tree_dataset_score where tree_id="+tree.id);
				if(scores!=null){
					Map<String, TreeScore> data_score = new HashMap<String, TreeScore>();
					while(scores.next()){
						TreeScore score = new TreeScore();
						score.novelty = scores.getFloat("novelty");
						score.percent_correct = scores.getFloat("percent_correct");
						score.size = scores.getFloat("size");
						score.score = scores.getFloat("score");
						data_score.put(scores.getString("dataset"), score);
					}
					tree.dataset_score = data_score;
				}
				trees.add(tree);
			}
			ts.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return trees;
	}

	public List<Tree> getByIP(String ip){
		List<Tree> trees = new ArrayList<Tree>();
		String q = "select * from tree where ip="+ip;
		JdbcConnection conn = new JdbcConnection();
		try {
			ResultSet ts = conn.executeQuery(q);
			while(ts.next()){
				Tree tree = new Tree(ts.getInt("id"), player_id, ts.getString("ip"), null, ts.getString("json_tree"),ts.getString("comment"), ts.getInt("user_saved"), ts.getInt("private"));
				tree.created = ts.getTimestamp("created");
				//TODO stop being lazy and do this properly in SQL...
				String fq = "select * from tree_feature where tree_id="+tree.id;
				ResultSet fs = conn.executeQuery(fq);
				List<Feature> features = new ArrayList<Feature>();
				while(fs.next()){
					int fid = fs.getInt("feature_id");
					Feature f = Feature.getByDbId(fid);
					if(f!=null){
						features.add(f);
					}
				}
				tree.features = features;
				trees.add(tree);
			}
			ts.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return trees;
	}	
	
	public List<Tree> getById(String id){
		List<Tree> trees = new ArrayList<Tree>();
		String q = "select * from tree where id="+id;
		JdbcConnection conn = new JdbcConnection();
		try {
			ResultSet ts = conn.executeQuery(q);
			while(ts.next()){
				Tree tree = new Tree(ts.getInt("id"), ts.getInt("player_id"), ts.getString("ip"), null, ts.getString("json_tree"), ts.getString("comment"), ts.getInt("user_saved"), ts.getInt("private"));
				tree.created = ts.getTimestamp("created");
				String player_name = "";
				try{
					ResultSet player = conn.executeQuery("select * from player where id="+ts.getInt("player_id"));
					while(player.next()){
						player_name = player.getString("name");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				tree.player_name = player_name;
				String fq = "select * from tree_feature where tree_id="+tree.id;
				ResultSet fs = conn.executeQuery(fq);
				List<Feature> features = new ArrayList<Feature>();
				while(fs.next()){
					int fid = fs.getInt("feature_id");
					Feature f = Feature.getByDbId(fid);
					if(f!=null){
						features.add(f);
					}
				}
				tree.features = features;
				//scores
				ResultSet scores = conn.executeQuery("select * from tree_dataset_score where tree_id="+tree.id);
				if(scores!=null){
					Map<String, TreeScore> data_score = new HashMap<String, TreeScore>();
					while(scores.next()){
						TreeScore score = new TreeScore();
						score.novelty = scores.getFloat("novelty");
						score.percent_correct = scores.getFloat("percent_correct");
						score.size = scores.getFloat("size");
						score.score = scores.getFloat("score");
						data_score.put(scores.getString("dataset"), score);
					}
					tree.dataset_score = data_score;
				}
				trees.add(tree);
			}
			ts.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return trees;		
	}
	
	public int get_rank(int tree_id){
		JdbcConnection conn = new JdbcConnection();
		conn.executeQuery("SET @i = 0;");
		String q = "select rank from (select @i:=@i+1 as rank,tree.* from tree inner join tree_dataset_score on tree.id=tree_dataset_score.tree_id and tree_dataset_score.score!=0 and tree.user_saved=1 order by tree_dataset_score.score desc) as T where id="+tree_id;
		ResultSet ranks = conn.executeQuery(q);
		try{
			while(ranks.next()){
				rank = ranks.getInt("rank");
			}
		} catch(SQLException e){
			e.printStackTrace();
		}
		return rank;
	}
	
	public List<Tree> getWithLimit(String lowerLimit, String upperLimit, String orderby){
		List<Tree> trees = new ArrayList<Tree>();
		JdbcConnection conn = new JdbcConnection();
		conn.executeQuery("SET @i = 0;");
		String q = "";
		conn.executeQuery("set @i = "+lowerLimit);
		if(orderby.equals("score")){
			q = "select @i:=@i+1 as rank, tree.* from tree inner join tree_dataset_score on tree.id=tree_dataset_score.tree_id and tree_dataset_score.score!=0 and tree.user_saved=1 and tree.private=0 order by tree_dataset_score.score desc limit "+lowerLimit+","+upperLimit;
		} 
		try {
			ResultSet ts = conn.executeQuery(q);
			while(ts.next()){
				Tree tree = new Tree(ts.getInt("id"), ts.getInt("player_id"), ts.getString("ip"), null, ts.getString("json_tree"), ts.getString("comment"), ts.getInt("user_saved"), ts.getInt("private"));
				tree.created = ts.getTimestamp("created");
				tree.rank = ts.getInt("rank");
				String player_name = "";
				try{
					ResultSet player = conn.executeQuery("select * from player where id="+ts.getInt("player_id"));
					while(player.next()){
						player_name = player.getString("name");
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				tree.player_name = player_name;
				String fq = "select * from tree_feature where tree_id="+tree.id;
				ResultSet fs = conn.executeQuery(fq);
				List<Feature> features = new ArrayList<Feature>();
				while(fs.next()){
					int fid = fs.getInt("feature_id");
					Feature f = Feature.getByDbId(fid);
					if(f!=null){
						features.add(f);
					}
				}
				tree.features = features;
				//scores
				ResultSet scores = conn.executeQuery("select * from tree_dataset_score where tree_id="+tree.id);
				if(scores!=null){
					Map<String, TreeScore> data_score = new HashMap<String, TreeScore>();
					while(scores.next()){
						TreeScore score = new TreeScore();
						score.novelty = scores.getFloat("novelty");
						score.percent_correct = scores.getFloat("percent_correct");
						score.size = scores.getFloat("size");
						score.score = scores.getFloat("score");
						data_score.put(scores.getString("dataset"), score);
					}
					tree.dataset_score = data_score;
				}
				trees.add(tree);
			}
			ts.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return trees;
	}
	
	public List<Tree> getBySearch(String query){
		List<Tree> trees = new ArrayList<Tree>();
		JdbcConnection conn = new JdbcConnection();
		conn.executeQuery("SET @i = 0;");
		String q = "";
		q = "select @i:=@i+1 as rank, player.name, tree.* from tree inner join tree_dataset_score on tree.id=tree_dataset_score.tree_id and tree_dataset_score.score!=0 and tree.user_saved=1 inner join player on player.id = tree.player_id where tree.json_tree like '%"+query+"%' or tree.comment like '%"+query+"%' or player.name like '%"+query+"%' order by tree_dataset_score.score desc";
		try {
			ResultSet ts = conn.executeQuery(q);
			while(ts.next()){
				Tree tree = new Tree(ts.getInt("id"), ts.getInt("player_id"), ts.getString("ip"), null, ts.getString("json_tree"), ts.getString("comment"), ts.getInt("user_saved"), ts.getInt("private"));
				tree.created = ts.getTimestamp("created");
				tree.rank = ts.getInt("rank");
				tree.player_name = ts.getString("name");
				String fq = "select * from tree_feature where tree_id="+tree.id;
				ResultSet fs = conn.executeQuery(fq);
				List<Feature> features = new ArrayList<Feature>();
				while(fs.next()){
					int fid = fs.getInt("feature_id");
					Feature f = Feature.getByDbId(fid);
					if(f!=null){
						features.add(f);
					}
				}
				tree.features = features;
				//scores
				ResultSet scores = conn.executeQuery("select * from tree_dataset_score where tree_id="+tree.id);
				if(scores!=null){
					Map<String, TreeScore> data_score = new HashMap<String, TreeScore>();
					while(scores.next()){
						TreeScore score = new TreeScore();
						score.novelty = scores.getFloat("novelty");
						score.percent_correct = scores.getFloat("percent_correct");
						score.size = scores.getFloat("size");
						score.score = scores.getFloat("score");
						data_score.put(scores.getString("dataset"), score);
					}
					tree.dataset_score = data_score;
				}
				trees.add(tree);
			}
			ts.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return trees;
	}
	
	public static double getUniqueIdNovelty(List<String> unique_id, int PlayerId){
		double nov = 1;
		//select count(*) from card where unique_id = 2261 or unique_id = 1717 or unique_id = 9135;
		JdbcConnection conn = new JdbcConnection();
		String q = "select (select count(*) as total from card)+(select count(*) as total from tree_feature) as total";
		String q1 = "select ";
		String q2 = "select count(*) as n from card where user_id != "+PlayerId+" and (";
		String q3 = "select count(*) from (select tree.* from tree_feature, feature, tree where tree_feature.feature_id = feature.id and tree_feature.tree_id = tree.id and tree.player_id != '"+PlayerId+"' and (";
		for(String uid : unique_id){
			q2 += " unique_id = '"+uid+"' or ";// quotes('') for unique ids of clinical features like 'metabric_clinical_5'
			q3 += " feature.unique_id = '"+uid+"' or ";
		}
		q2 = q2.substring(0,q2.length()-3);
		q2+=")";
		q3 = q3.substring(0,q3.length()-3);
		q3+=" ) group by tree.player_id, tree_feature.feature_id) as n";
		q1 = q1 +"("+q2+")"+"+"+"("+q3+") as n";
		double base = 1; double n = 1;
		ResultSet rslt = conn.executeQuery(q);
		try {
			if(rslt.next()){
				base = rslt.getDouble("total");
			}
			rslt.close();
			if(!unique_id.isEmpty()){
				rslt = conn.executeQuery(q1);
				if(rslt.next()){//returns false if the cursor is not before the first record or if there are no rows in the ResultSet.
					n = rslt.getDouble("n");
				}
			} else {
				n=0;
			}
			if(base>0 && n > 0){
				nov = (1 - Math.log(n)/Math.log(base));
			}else if(base == 0 && n == 0){//With this condition, novelty = Infinity error resolved.
				nov = 1; //First time card used.
			}
			
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		return nov;
	}
	
	public int insert(int previous_tree_id, int privateflag) throws Exception{
		int newid = 0;
		JdbcConnection conn = new JdbcConnection();
		ResultSet generatedKeys = null;
		String insert = "insert into tree (player_id, ip, json_tree, comment, user_saved, prev_tree_id, private) values(?,?,?,?,?,?,?)";

		PreparedStatement p = null;
		try {
			p = conn.connection.prepareStatement( insert, Statement.RETURN_GENERATED_KEYS);
			p.setInt(1, player_id);
			p.setString(2, ip);
			p.setString(3, json_tree);
			p.setString(4, comment);
			p.setInt(5, user_saved);
			p.setInt(6, previous_tree_id);
			p.setInt(7, privateflag);
			int affectedRows = p.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Creating tree failed, no rows affected.");
			}
			generatedKeys = p.getGeneratedKeys();
			if (generatedKeys.next()) {
				id = generatedKeys.getInt(1);
				newid = id;
				if(features!=null){
					for(Feature f : features){
						conn.executeUpdate("insert into tree_feature values("+newid+",'"+f.getId()+"')");
						//tree_feature(Unique_Key) duplicated if same node added. Causes MySQL integrity error.
					}
				}else{
					throw new Exception("Creating tree failed, no features detected.");
				}
			} else {
				throw new SQLException("Creating tree failed, no generated key obtained.");
			}
		} finally {
			if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException logOrIgnore) {}
			if (p != null) try { p.close(); } catch (SQLException logOrIgnore) {}
			if (conn.connection != null) try { conn.connection.close(); } catch (SQLException logOrIgnore) {}
		}

		return newid;
	}

	//TODO add flag to indicate whether tree was saved purposefully by the user or captured autoamtically as part of the scoring process.
	public void insertScore(int tree_id, String dataset, float percent_correct, float size, float novelty, float score) throws Exception{
		JdbcConnection conn = new JdbcConnection();
		try {
			conn.executeUpdate("insert into tree_dataset_score " +
					"values("+tree_id+",'"+dataset+"',"+percent_correct+","+size+","+novelty+","+score+")");
		} finally {
			if (conn.connection != null) try { conn.connection.close(); } catch (SQLException logOrIgnore) {}
		}
		return;
	}
	
	//Change from id to unique_id as parameter to get attIndex.
	public int migrateDatabase() throws JsonProcessingException, IOException{
		JdbcConnection conn = new JdbcConnection();
		String query = "select json_tree,id from tree";
		ResultSet rslt = conn.executeQuery(query);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jtree;
		try {
			while(rslt.next()){ 
				jtree = mapper.readTree(rslt.getString("json_tree").replace("\n", "").replace("\r", ""));
				findOrCreateUniqueId(jtree.get("treestruct"));
				conn.executeUpdate("update tree set json_tree='"+jtree.toString().replace("\n", "").replace("\r", "")+"' where id="+rslt.getString("id"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1;
	}
	
	public void findOrCreateUniqueId(JsonNode node){
		ObjectNode options  = (ObjectNode) node.get("options");
		String kind = "";
		String id = "";
		if(options!=null){
			kind = options.get("kind").asText();
			if(kind.equals("split_node") || kind.equals("leaf_node")){
				if(options.has("id")){
					id = options.get("id").asText();
					options.remove("id");
					System.out.println(id);
					options.put("unique_id", id);
				}
			}
			if(node.get("children")!=null){
				for(JsonNode child : node.get("children")){
					findOrCreateUniqueId(child);
				}
			}
		}
	}

	public int getPlayer_id() {
		return player_id;
	}

	public void setPlayer_id(int player_id) {
		this.player_id = player_id;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public List<Feature> getFeatures() {
		return features;
	}

	public void setFeatures(List<Feature> features) {
		this.features = features;
	}

	public String getJson_tree() {
		return json_tree;
	}

	public void setJson_tree(String json_tree) {
		this.json_tree = json_tree;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Map<String, TreeScore> getDataset_score() {
		return dataset_score;
	}

	public void setDataset_score(Map<String, TreeScore> dataset_score) {
		this.dataset_score = dataset_score;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getUser_saved() {
		return user_saved;
	}

	public void setUser_saved(int user_saved) {
		this.user_saved = user_saved;
	}



}
