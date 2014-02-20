define([
	'jQuery',
	'marionette',
	//Views
	'app/views/AddRootNodeView'
    ], function($, Marionette, Comment) {
emptyLayout = Marionette.Layout.extend({
    template: "#Empty-Layout-Template",
    regions: {
      AddRootNode: "#AddRootNodeWrapper"
    },
    onBeforeRender: function(){
    	//Cure.ToggleHelp(false);
    },
    onRender: function(){
    	if(!Cure.helpText){
        	Cure.helpText = $("#HelpText").html();	
        	Cure.ToggleHelp(true);
    	}
    	var newAddRootNodeView = new AddRootNodeView(); 
    	this.AddRootNode.show(newAddRootNodeView);
    },
    onBeforeClose: function(){
    }
});
return emptyLayout;
});
