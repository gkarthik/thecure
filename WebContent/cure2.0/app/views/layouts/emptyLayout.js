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
    ui:{
    	"dropRootNode": "#drop-root-node"
    },
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
    			Cure.initTour.init();
    			if(Cure.startTour){
        			Cure.initTour.start();
    			}
    		},600);
    	});
    	this.AddRootNode.show(new AddRootNodeView());
  		$(this.ui.dropRootNode).droppable({
				accept: ".gene-pool-item",
				activeClass: "genepool-drop-active",
				hoverClass: "genepool-drop-hover",
				drop: function( event, ui ) {
					var index = $(ui.draggable).data("index");
					var ui = Cure.GeneCollection.at(index).toJSON();
					new Node({
						'name' : ui.short_name,
						"options" : {
							"unique_id" : ui.unique_id,
							"kind" : "split_node",
							"full_name" : ui.long_name
						}
					});
					Cure.PlayerNodeCollection.sync();
				}
			});
    }
});
return emptyLayout;
});
