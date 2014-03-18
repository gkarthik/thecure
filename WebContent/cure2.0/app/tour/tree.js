define([
        'jquery',
      	'bootstrapTour'
      ], function($) {
var TreeTour = new Tour({
	name: 'treetour',
	  steps: [
	  {
			element: ".split_node",
		  title: "Split Node",
		  content: "This node is called a split node. You can click on the name of the gene shown to view information regarding the gene such as Gene Rifs, Molucular Functions etc.",
		},
		{
			element: ".split_value",
		  title: "Split Value",
		  content: "This node is called a split value node. It shows the level of gene expression or in the case of clinical features, an indication of their values.",
		},
		{
		element: ".leaf_node",
		title: "Leaf Node",
		content: "This node is called a leaf node. It shows the final classification of cases based on the split attributes that are picked. Y(Yes) represents a favourable prediction and N(No) represents an unfavourable prediction.",
		},
		{
		element: ".addchildren",
		title: "Add a Node",
		content: "Clicking on the + Add Node button shows the textbox with controls to add genes or clinical features.",
		onShow: function(){
		$($(".addchildren")[0]).trigger('click');
		},
		onNext: function(){
		$(document).trigger('mouseup');
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
		element: ".showSVG",
		title: "Score Chart",
		content: "Click to expand. You can see the various factors contributing to your score in a radar chart.",
		placement: "left"
		},
		{
		element: ".showChangeSummary",
		title: "Score Change Summary",
		content: "Click to see how the last node you added changed your score.",
		placement: "left"
		},
		{
		element: "#tree-explanation-button",
		title: "Explain the Tree",
		content: "You can click on Show Tree Explanation to view a textual description of the tree that you have built.",
		placement: "left",
		onNext: function(){
		$(".showTreeExp").trigger('click');
		}
		},
		{
		element: ".highlightBranch",
		title: "Highlight Relevant Nodes",
		content: "Click here to highlight the split attributes that contributed to each prediction.",
		placement: "left",
		onNext: function(){
		$($(".highlightBranch")[0]).trigger('click');
		}
		},
		{
		element: ".highlightNode",
		title: "Highlighted Nodes",
		content: "These are the split nodes that contributed to the first prediction.",
		onNext: function(){
		$(".closeTreeExp").trigger('click');
		}
		},
		{
		element: "#my-tree-collection-link",
		title: "My Tree Collection",
		content: "Click to see the collection of trees you have built. You can also browse trees built by other users on the page that opens.",
		onNext: function(){
		$("#tree-explanation-button").trigger('click');
		},
		placement: 'left'
		},
		{
		element: "#zoom-controls",
		title: "Zoom Controls",
		content: "Click on + and - to zoom in and out. You can uncheck the 'fit to screen' option if you don't want the tree to scale to the screen. You might have to use the scroll to view your entire tree if you uncheck this option.",
		onNext: function(){
		$("#tree-explanation-button").trigger('click');
		}
		}
	  ],
	  storage: window.localStorage
});

return TreeTour;
});