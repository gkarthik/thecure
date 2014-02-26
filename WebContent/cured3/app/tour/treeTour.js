define([
        'jquery',
      	'bootstrapTour'
      ], function($) {
	var TreeTour = new Tour({
		name: 'treeTour',
		steps:[
	  {
	  	element: ".split_node",
	    title: "Split Node",
	    content: "This node is called a split node. You can click on the name of the gene shown to view information regarding the gene such as Gene Rifs, Molucular FUnctions etc.",
   },
   {
	  	element: ".split_value",
	    title: "Split Value",
	    content: "This node is called a split value node. It shows the level of gene expression or in the case of clinical features, an indication of their values.",
  },
  {
  	element: ".leaf_node",
    title: "Leaf Node",
    content: "This node is called a leaf node. It shows the final classification of cases based on the split nodes added above it. Y represents a favourable condition and N represents an unfavourable situation.",
},
{
	element: ".addchildren",
  title: "Add a Node",
  content: "Clicking on the + Add Node button shows the textbox with controls to add genes or clinical features.",
  onShow: function(){
  	$($(".addchildren")[0]).trigger('click');
  },
  onNext: function(){
  	$("body").trigger('');
  }
},
   {
	  	element: ".chart",
	    title: "Data Chart",
	    content: "This chart represents the number of cases that fall under each class. You can click on these charts to view accuracy and size values.",
  },
  {
	  	element: "#score",
	    title: "Score",
	    content: "You can see the score of your tree here.",
	    placement: "left"
  },
  {
  	element: "#ScoreSVG",
    title: "Score Chart",
    content: "You can see the various factors contributing to your score in this radar chart.",
    placement: "left"
},
{
	element: ".novelty-row",
  title: "Score Change Summary",
  content: "You can see how the last node you added changed your score.",
  placement: "left"
},
{
	element: "#control-panel .togglePanel",
  title: "Show Save Options",
  content: "You can click on Toggle Save Options to view options to save your current tree and to enter a comment.",
  placement: "left"
},
{
	element: "#explanation-panel .togglePanel",
  title: "Explain the Tree",
  content: "You can click on Toggle Explanation to view a textual description of the tree that you have built.",
  placement: "left",
  onNext: function(){
  	$("#explanation-panel .togglePanel").trigger('click');
  }
},
{
	element: "#explanation-panel .highlightBranch",
  title: "Highlight Relevant Nodes",
  content: "To highlight the nodes that are being described click on Show in Tree",
  placement: "left",
  onNext: function(){
  	$($(".highlightBranch")[0]).trigger('click');
  }
},
{
	element: ".highlightNode",
  title: "Highlighted Nodes",
  content: "These are the nodes that are being described in the first explanation.",
  onNext: function(){
  	$("#explanation-panel .togglePanel").trigger('click');
  }
},
{
	element: "#scoreboard-panel .togglePanel",
  title: "Show Score Board",
  content: "You can click on Toggle Score Board to view the score board and compare your tree. You can view other trees by clicking on their respective scores.",
  placement: "left"
}
  ],
	  storage: window.localStorage
});	
	
	return TreeTour;
});