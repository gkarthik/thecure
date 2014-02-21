define([
	'jquery',
	'marionette',
	//Views
	'app/views/AddRootNodeView'
    ], function($, Marionette, AddRootNodeView) {
emptyLayout = Marionette.Layout.extend({
    template: "#Empty-Layout-Template",
    regions: {
      AddRootNode: "#AddRootNodeWrapper"
    },
    onBeforeRender: function(){
    	//Cure.ToggleHelp(false);
    },
    helpText : "",
    onRender: function(){
    	if(!this.helpText){
        	this.helpText = $("#HelpText").html();	
        	Cure.utils.ToggleHelp(true, this.helpText);
    	}
    	var newAddRootNodeView = new AddRootNodeView(); 
    	this.AddRootNode.show(newAddRootNodeView);
    },
    onBeforeClose: function(){
    }
});
return emptyLayout;
});
