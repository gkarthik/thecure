define([
	'jquery',
	'marionette',
	//Views
	'app/views/AddRootNodeView',
	//Templates
	'text!app/templates/EmptyNodeCollection.html'
    ], function($, Marionette, AddRootNodeView, EmptyNodeCollectionTemplate) {
emptyLayout = Marionette.Layout.extend({
    template: EmptyNodeCollectionTemplate,
    regions: {
      AddRootNode: "#AddRootNodeWrapper"
    },
    onRender: function(){
    	if(!Cure.helpText){
        	Cure.helpText = $("#HelpText").html();	
        	Cure.utils.ToggleHelp(true, Cure.helpText);
    	}
    	this.AddRootNode.on("show", function(view){
    		window.setTimeout(function(){
    			Cure.initTour.start();
    		},600);
    	});
    	this.AddRootNode.show(new AddRootNodeView());
  		
    }
});
return emptyLayout;
});
