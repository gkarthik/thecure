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
    onBeforeRender: function(){
    	//Cure.ToggleHelp(false);
    },
    onRender: function(){
    	if(!Cure.helpText){
        	Cure.helpText = $("#HelpText").html();	
        	Cure.utils.ToggleHelp(true, Cure.helpText);
    	}
    	var newAddRootNodeView = new AddRootNodeView(); 
    	this.AddRootNode.show(newAddRootNodeView);
    },
    onBeforeClose: function(){
    }
});
return emptyLayout;
});
