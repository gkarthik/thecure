define([
        'jquery',
      	'bootstrapTour'
      ], function($) {
var AddRootNodeTour = new Tour({
	name: 'inittour',
	  steps: [
	          {
	    element: "#gene_query",
	    title: "#1 Selecting A Gene",
	    content: "You can select a gene by typing in the text box and select a gene from the dropdown that appears."
	  },
	  {
	    element: "#showCf",
	    title: "#2 Switching  To Clinical Features",
	    content: "Click on the icon to switch between choosing clinical features and gene names.",
	    placement: "left",
	    onNext: function(){
	    	$("#showCf").trigger('click');
	    }
	  },
	  {
	  	element: "#cf_query",
	    title: "#3 Selecting a Clinical Feature",
	    content: "You can select Clinical Feature by clicking on the textbox and choosing from the list that appears. You can also search by typing into the textbox.",
	    onNext: function(){
	    	$("#hideCf").trigger('click');
	    }
	  },
	  {
	  	element: "#gene_query",
	    title: "Choose a gene!",
	    content: "Lets begin! Start typing and select a gene from the dropdown that appears.",
	    onNext: function(){
	    	if(!$(".node")){
	    		$("#gene_query").genequery_autocomplete("search","MRPL13",function(){
	    			$(".ui-menu-item").trigger('click');
	    		});
	    	}
	   }
	  }
	  ],
	  storage: window.localStorage
});

return AddRootNodeTour;
});

/*
 * {
	    element: "#gene_query",
	    title: "#1 Selecting A Gene",
	    content: "You can select a gene by typing in the text box and select a gene from the dropdown that appears."
	  },
	  {
	    element: "#showCf",
	    title: "#2 Switching  To Clinical Features",
	    content: "Click on the icon to switch between choosing clinical features and gene names.",
	    onNext: function(){
	    	$("#showCf").trigger('click');
	    }
	  },
	  {
	  	element: "#hideCf",
	    title: "#3 Selecting a Clinical Feature",
	    content: "You can select Clinical Feature by clicking on the textbox and choosing from the list that appears. You can also search by typing into the textbox.",
	    onNext: function(){
	    	$("#hideCf").trigger('click');
	    }
	  },
	  {
	  	element: "#gene_query",
	    title: "Choose a gene!",
	    content: "Lets begin! Start typing and select a gene from the dropdown that appears.",
	    onNext: function(){
	    	if(!$(".node")){
	    		$("#gene_query").genequery_autocomplete("search","MRPL13",function(){
	    			$(".ui-menu-item").trigger('click');
	    		});
	    	}
	   }
	  }
 */