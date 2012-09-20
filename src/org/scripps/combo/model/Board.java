package org.scripps.combo.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.scripps.util.MapFun;
import org.scripps.combo.weka.Weka;
import org.scripps.combo.weka.Weka.execution;
import org.scripps.util.JdbcConnection;

/**
 * A pre-computed set of features (e.g. genes) for building board games.
 * create table board (id int(10) NOT NULL AUTO_INCREMENT, dataset varchar(50), room varchar(25), n_players int, n_wins int, avg_score float, max_score float, base_score float, created Date, updated timestamp, primary key (id));
	create table board_feature (board_id int, feature_id int);
 * @author bgood
 *
 */
public class Board {

	int id;
	String dataset;
	String room;
	List<Feature> features;
	int n_players;
	int n_wins;
	float avg_score;
	float max_score;
	float base_score;
	Date created;
	Timestamp updated;

	public static void main(String args[]) throws Exception{
		//setupCureV2();		
	}

	/**
	 * For second iteration of the cure, build 100 boards with this
	 */
	public static void setupCureV2(){
		String dataset = "dream_breast_cancer";
		String train_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/dream/Exprs_CNV_2500genes.arff";
		int nper = 36; int nboards = 50; int topper = 2; String room = "2";
		try {
			for(int i=0; i<2; i++){
				createAndSaveInterestingBoards(train_file, nper, dataset, room, nboards, topper);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//also build the mammal boards
		dataset = "mammal";
		train_file = "/Users/bgood/workspace/acure/WebContent/WEB-INF/data/zoo_mammals.arff";
		List<String> feature_ids = MapFun.string2list("mammal_1,mammal_10", ",");
		//List<String> feature_ids = MapFun.string2list("mammal_3,mammal_9", ",");
		//List<String> feature_ids = MapFun.string2list("mammal_9,mammal_7,mammal_6,mammal_14", ",");
		//List<String> feature_ids = MapFun.string2list("mammal_4,mammal_3,mammal_1,mammal_13", ",");
		try {
			createAndSaveSpecificBoard(train_file, dataset, "training_1", feature_ids);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Use an attribute ranking system to generate hopefully useful boards..
	 * @param train_file
	 * @param metadatafile
	 * @param n_per_board
	 * @throws Exception 
	 */
	public static void createAndSaveInterestingBoards(String train_file, int n_per_board, String dataset, String room, int total, int topper) throws Exception{	
		Weka weka = new Weka();
		weka.buildWeka(new FileInputStream(train_file), null, dataset);

		//produce the list of unique entrez gene ids

		List<String> gene_ids = new ArrayList<String>(weka.getFeatures().keySet());
		System.out.println("N gene ids: "+gene_ids.size());


		//get the genes sorted by max power of related probes etc.
		Map<String, Double> power_map = new HashMap<String, Double>();
		for(String gene : gene_ids){
			List<Attribute> atts = weka.getFeatures().get(gene).getDataset_attributes();
			double max_power = -10;
			for(Attribute a : atts){
				if(a.getReliefF()>max_power){
					max_power = a.getReliefF();
				}
			}
			power_map.put(gene, max_power);
		}
		List<String> sorted_genes = MapFun.sortMapByValue(power_map);
		//best on top
		Collections.reverse(sorted_genes);
		//randomize the other list
		Collections.shuffle(gene_ids);
				
		//build the boards by sampling topper from the top, then the rest at random
		for(int board_id =1; board_id<=total; board_id++){
			Board board = new Board();
			board.setDataset(dataset);
			board.setRoom(room);
			List<Feature> bfs = new ArrayList<Feature>();
			List<String> unique_ids = new ArrayList<String>();		
			//pop topper good ones
			for(int t=0;t<topper; t++){
				unique_ids.add(sorted_genes.get(0));
				//remove it from both lists
				gene_ids.remove(sorted_genes.get(0));
				sorted_genes.remove(0);
			}
			//add the rest
			for(int r=topper; r<n_per_board;r++){
				unique_ids.add(gene_ids.get(0));
				sorted_genes.remove(gene_ids.get(0));
				gene_ids.remove(0);
			}
			//add the features			
			for(String gene : unique_ids){
				bfs.add(weka.getFeatures().get(gene));
			}
			board.setFeatures(bfs);
			//test it
			execution base = weka.pruneAndExecute(unique_ids, null);
			float base_score = (float)base.eval.pctCorrect();
			board.setBase_score(base_score);
			board.insert();
			System.out.println(board_id+"\t"+base_score+"\t"+gene_ids.size()+"\t"+sorted_genes.size()+"\t");
		}
	}
	
	
	public static void createAndSaveRandomBoards(String train_file, int n_per_board, String dataset, String room, int n_boards) throws Exception{	
		Weka weka = new Weka();
		weka.buildWeka(new FileInputStream(train_file), null, dataset);

		//produce the list of unique entrez gene ids

		List<String> gene_ids = new ArrayList<String>(weka.getFeatures().keySet());
		System.out.println("N gene ids: "+gene_ids.size());
		for(int b=0; b< n_boards; b++){
			Collections.shuffle(gene_ids);
			Board board = new Board();
			board.setRoom(room);
			board.setDataset(dataset);
			List<Feature> bfs = new ArrayList<Feature>();
			List<String> unique_ids = new ArrayList<String>();
			for(int i=0; i<n_per_board;i++){
				unique_ids.add(gene_ids.get(i));
				bfs.add(weka.getFeatures().get(gene_ids.get(i)));
			}
			execution base = weka.pruneAndExecute(unique_ids, null);
			float base_score = (float)base.eval.pctCorrect();
			board.setBase_score(base_score);
			board.setFeatures(bfs);
			//System.out.println(base_score+" \n "+base.model.getClassifier().toString());
			board.insert();
		}

	}
	
	public static void createAndSaveSpecificBoard(String train_file, String dataset, String room, List<String> feature_ids) throws Exception{	
		Weka weka = new Weka();
		weka.buildWeka(new FileInputStream(train_file), null, dataset);	
		Board board = new Board();
		board.setRoom(room);
		board.setDataset(dataset);
		List<Feature> bfs = new ArrayList<Feature>();
		for(String fid : feature_ids){
			Feature f = weka.getFeatures().get(fid);
			f.getAllMetadataFromDb();
			bfs.add(f);
		}
		execution base = weka.pruneAndExecute(feature_ids, null);
		float base_score = (float)base.eval.pctCorrect();
		board.setBase_score(base_score);
		board.setFeatures(bfs);
		System.out.println(base_score+" \n "+base.model.getClassifier().toString());
		board.insert();
		
	}
	



	/**
	 * Insert a new board.
	 * @throws SQLException 
	 */
	public int insert() throws SQLException{
		int newid = 0;
		JdbcConnection conn = new JdbcConnection();
		ResultSet generatedKeys = null; PreparedStatement pst = null;
		try {
			pst = conn.connection.prepareStatement(
					"insert into board (id, dataset, room, base_score, created) values(?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS);
			pst.clearParameters();
			if(id>0){
				pst.setInt(1,getId());
			}else{
				pst.setString(1, null);
			}
			pst.setString(2,getDataset());
			pst.setString(3, getRoom());
			pst.setFloat(4,getBase_score());
			pst.setDate(5, getCreated());

			int affectedRows = pst.executeUpdate();
			if (affectedRows == 0) {
				throw new SQLException("Creating board failed, no rows affected.");
			}
			generatedKeys = pst.getGeneratedKeys();
			if (generatedKeys.next()) {
				setId(generatedKeys.getInt(1));
				newid = generatedKeys.getInt(1);
				for(Feature f : getFeatures()){
					conn.executeUpdate("insert into board_feature values("+newid+","+f.getId()+")");
				}
			} else {
				throw new SQLException("Creating board failed, no generated key obtained.");
			}
		} finally {
			if (generatedKeys != null) try { generatedKeys.close(); } catch (SQLException logOrIgnore) {}
			if (pst != null) try { pst.close(); } catch (SQLException logOrIgnore) {}
			if (conn.connection != null) try { conn.connection.close(); } catch (SQLException logOrIgnore) {}
		}
		return newid;
	}




	public void updateMaxScore(){
		if(getId()<1){
			System.out.println("Can't update without an id");
			return;
		}
		JdbcConnection conn = new JdbcConnection();
		try {
			PreparedStatement pst = conn.connection.prepareStatement("update board set max_score = ? where id = "+getId());
			pst.clearParameters();
			pst.setFloat(1,getMax_score());			
			pst.executeUpdate();
			pst.close();
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public List<Integer> getBoardScoresfromDb(int board_id){
		List<Integer> board_scores = new ArrayList<Integer>();
		String gethands = "select phenotype, game_type, board_id, score, training_accuracy, cv_accuracy, win from hand where board_id = '"+board_id+"' and player_name != 'anonymous_hero'";
		JdbcConnection conn = new JdbcConnection();
		try {
			PreparedStatement p = conn.connection.prepareStatement(gethands);
			ResultSet hands = p.executeQuery();
			while(hands.next()){
				int training = hands.getInt("training_accuracy");
				int cv = hands.getInt("cv_accuracy");
				int win = hands.getInt("win");				
				if(win>0){
					if(training<0){
						training = cv;
					}
					board_scores.add(training);
				}
			}
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return board_scores;
	}

	public static Map<String,List<Integer>> getPlayerBoardScoresForWins(int board_id){
		Map<String,List<Integer>> player_board_scores = new HashMap<String,List<Integer>>();
		String gethands = "select player_name, phenotype, game_type, board_id, score, training_accuracy, cv_accuracy, win from hand where win > 0" +
		" and board_id = '"+board_id+"' and player_name != 'anonymous_hero' ";
		JdbcConnection conn = new JdbcConnection();
		try {
			PreparedStatement p = conn.connection.prepareStatement(gethands);
			ResultSet hands = p.executeQuery();
			while(hands.next()){
				int training = hands.getInt("training_accuracy");
				int cv = hands.getInt("cv_accuracy");
				String player_name = hands.getString("player_name");
				if(training<0){
					training = cv;
				}
				List<Integer> board_scores = player_board_scores.get(player_name);
				if(board_scores==null){
					board_scores = new ArrayList<Integer>();
				}
				board_scores.add(training);
				player_board_scores.put(player_name, board_scores);
			}
			conn.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return player_board_scores;
	}	

		
		public static List<Board> getBoardsByDataset(String dataset){
			List<Board> boards = new ArrayList<Board>();
			JdbcConnection conn = new JdbcConnection();
			ResultSet rslt = conn.executeQuery("select * from board where dataset = '"+dataset+"' order by base_score desc");
			try {
				while(rslt.next()){
					Board board = new Board();
					board.setAvg_score(rslt.getFloat("avg_score"));
					board.setId(rslt.getInt("id"));
					board.setMax_score(rslt.getFloat("max_score"));
					board.setN_players(rslt.getInt("n_players"));
					board.setN_wins(rslt.getInt("n_wins"));
					board.setDataset(dataset);
					board.setRoom(rslt.getString("room"));
					board.setUpdated(rslt.getTimestamp("updated"));
					board.setBase_score(rslt.getFloat("base_score"));
					
					ResultSet f_list = conn.executeQuery("select * from board_feature where board_id = "+board.getId());
					List<Feature> fs = new ArrayList<Feature>();
					while(f_list.next()){
						fs.add(Feature.getByDbId(f_list.getInt("feature_id")));
					}
					board.setFeatures(fs);
					boards.add(board);
				}
				rslt.close();
				conn.connection.close();	
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return boards;
		}
	
	//	public Board getBoardById(String id){
	//		JdbcConnection conn = new JdbcConnection();
	//		ResultSet rslt = conn.executeQuery("select * from board where id = '"+id+"'");
	//		try {
	//			if(rslt.next()){
	//				Board board = new Board();
	//				board.setAverage_score(rslt.getFloat("average_score"));
	//				board.setEntrez_ids(string2list(rslt.getString("entrez_ids")));
	//				board.setGene_symbols(string2list(rslt.getString("gene_symbols")));
	//				board.setId(rslt.getInt("id"));
	//				board.setMax_score(rslt.getFloat("max_score"));
	//				board.setN_players(rslt.getInt("n_players"));
	//				board.setN_wins(rslt.getInt("n_wins"));
	//				board.setPhenotype(dataset);
	//				board.setUpdated(rslt.getTimestamp("updated"));
	//				board.setAttribute_names(string2list(rslt.getString("attribute_names")));
	//				board.setBase_score(rslt.getFloat("base_score"));
	//				return board;
	//			}
	//		} catch (SQLException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//		return null;
	//	}
	//	
	
	public List<String> getFeatureIds() {
		if(getFeatures()!=null){
			List<String> ids = new ArrayList<String>();
			for(Feature f : getFeatures()){
				ids.add(f.getUnique_id());
			}
		}
		return null;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getN_players() {
		return n_players;
	}
	public void setN_players(int n_players) {
		this.n_players = n_players;
	}
	public int getN_wins() {
		return n_wins;
	}
	public void setN_wins(int n_wins) {
		this.n_wins = n_wins;
	}
	public float getAvg_score() {
		return avg_score;
	}
	public void setAvg_score(float avg_score) {
		this.avg_score = avg_score;
	}
	public float getMax_score() {
		return max_score;
	}
	public void setMax_score(float max_score) {
		this.max_score = max_score;
	}

	public Timestamp getUpdated() {
		return updated;
	}

	public void setUpdated(Timestamp updated) {
		this.updated = updated;
	}

	public float getBase_score() {
		return base_score;
	}


	public void setBase_score(float base_score) {
		this.base_score = base_score;
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

	public List<Feature> getFeatures() {
		return features;
	}

	public void setFeatures(List<Feature> features) {
		this.features = features;
	}


	public String getRoom() {
		return room;
	}


	public void setRoom(String room) {
		this.room = room;
	}






}
