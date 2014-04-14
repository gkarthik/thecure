define([
        'jquery',
      	'bootstrapTour'
      ], function($) {
var AddRootNodeTour = new Tour({
	name: 'inittour',
	onStart: function(){
		$("body").append("<div class='tourOverlay'><h2>TOUR</h2></div>");
	},
	onShow: function(){
		if($(".tourOverlay").length==0){
			$("body").append("<div class='tourOverlay'><h2>TOUR</h2></div>");
		}
	},
	onEnd: function(){
		$(".tourOverlay").remove();
	},
	  steps: [
{
  title: "<h4 style='color:red;'>Disclaimers</h4>",
  content: "<b><ol><li>This resource is intended for purely research, educational and entertainment purposes. It should not be used for medical or professional advice.</li><li>Unless otherwise noted, all non-personally identifiable data entered into this site is stored in a database that will be publicly accessible.</ol></b>",
  	orphan: true
},
{
  title: "<b>Objective</b>",
  content: "<b>The objective is to build a decision tree that predicts 10 year survival for breast cancer patients",
  	orphan: true
},
	          {
	    element: "#gene_query",
	    title: "#1 Selecting A Gene",
	    content: "You can pick a gene by typing the gene name or function in the text box and selecting one from the dropdown that appears."
	  },
	  {
	    element: ".switch-wrapper",
	    title: "#2 Switching  To Clinical Features",
	    content: "Click on the toggle to switch between choosing clinical features and gene names.",
	    placement: "left",
	    onNext: function(){
	    	$(".switch-wrapper input[type='checkbox']").bootstrapSwitch('toggleState');
	    }
	  },
	  {
	  	element: "#cf_query",
	    title: "#3 Selecting a Clinical Feature",
	    content: "You can select Clinical Feature by clicking on the textbox and choosing from the list that appears. You can also search by typing into the textbox.",
	    onNext: function(){
	    	$(".switch-wrapper input[type='checkbox']").bootstrapSwitch('toggleState');
	    }
	  },
	  {
	    title: "Choose a gene!",
	    content: "Lets begin! Start typing 'AURKA' and select 'AURKA: aurora kinase A' from the dropdown that appears.",
	    orphan: true
	  }
	  ],
	  storage: window.localStorage
});

return AddRootNodeTour;
});