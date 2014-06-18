define([
	'jquery',
	'marionette',
	//Views
	'app/views/GeneCollectionView',
	'app/views/GenePoolCollectionView',
	//Templates
	'text!app/templates/AggregateNodeLayout.html'
    ], function($, Marionette, GeneCollectionView, GenePoolCollectionView, AggNodeTmpl) {
PathwayLayout = Marionette.Layout.extend({
    template: AggNodeTmpl,
    className: "panel panel-default",
    url: base_url+"MetaServer",
    events: {
    	'click .close-aggnode': 'closeAggNode'
    },
    regions: {
      GeneCollectionRegion: "#Aggnode_GeneCollectionRegion"
    },
    closeAggNode: function(){
    	Cure.sidebarLayout.AggNodeRegion.close();
    },
    onShow: function(){
    	
    }
});
return PathwayLayout;
});
