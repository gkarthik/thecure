<%@ page language="java" contentType="text/html; charset=utf-8"
	pageEncoding="utf-8"%>

<!DOCTYPE html>
<html>
<head>
<meta http-equiv="Content-type" content="text/html; charset=utf-8">
<title>Welcome to COMBO, games of prediction and discovery</title>
<link rel="stylesheet" href="styles/styles.css" type="text/css"
	media="screen">
</head>
<body>
	<div id="content" class="container">
		<h1>Breast Cancer prognosis game, version 1, instructions</h1>
		<p>Game play: maximize your score by choosing the best combination of genes</p>
		<p>Click the buttons next to the gene cards to add them to your hand. Your score will be calculated each time your hand is changed.<br/>  
		Click a card in your hand to put it back on the board. Careful though!. Once you put it back on the board, you can't get it back in 
		your hand. <br/>
		The goal is to use gene expression levels to predict a short interval to distant metastases ('poor prognosis' signature) 
		in patients without tumour cells in local lymph nodes at diagnosis (lymph node negative).<br/>
		Hint:genes regulating cell cycle, invasion, metastasis and angiogenesis may be important.<br/>
		Your score is determined by using the genes that you select to train machine learning algorithms to classify real biological samples.  
		The better the genes reflect the phenotype, the better you will score in the game.
			</p>
	</div>
</body>
</html>