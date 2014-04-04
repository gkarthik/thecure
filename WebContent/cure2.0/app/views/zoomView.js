define([
	'jquery',
	'marionette',
	//Model
	'app/models/zoom',
	//Templates
	'text!app/templates/zoomControls.html'
    ], function($, Marionette, Zoom, ZoomTemplate) {
ZoomView = Marionette.ItemView.extend({
	model : Node,
	template: ZoomTemplate,
	initialize : function() {
		_.bindAll(this,'scaleLevelUpdate');
		this.listenTo(this.model,'change:scaleLevel', this.scaleLevelUpdate);
		this.listenTo(this.model,'change', this.render);
		this.listenTo(Cure.PlayerNodeCollection,'change', this.render);
		this.listenTo(Cure.PlayerNodeCollection,'remove', this.render);
	},
	ui: {
		'fitToScreen':'#toggle-fittoscreen',
		'zoomOut': '.zoomout',
		'zoomIn': '.zoomin'
	},
	events:{
		'click #toggle-fittoscreen': 'clickFitToScreen',
		'click .zoomin': 'zoomIn',
		'click .zoomout': 'zoomOut'
	},
	zoomIn: function(){
		if (Cure.PlayerNodeCollection.models.length > 0){
  		if(this.model.get('scaleLevel') <= 1.5){
  			this.model.set('scaleLevel',parseFloat(this.model.get('scaleLevel')+0.1));
    	}
  	}
	},
	zoomOut: function(){
  	if (Cure.PlayerNodeCollection.models.length > 0){
  		if(this.model.get('scaleLevel') >= 0.5){
  			this.model.set('scaleLevel',parseFloat(this.model.get('scaleLevel')-0.1));
    	}
  	} 
	},
	scaleLevelUpdate: function(){
		var transformArray = String(Cure.PlayerSvg.attr("transform")).match(/-?[0-9\.]+/g);
		var transformString= {};
		transformArray[0] = parseFloat(((Cure.width-100)/2)*(1-this.model.get('scaleLevel')));
		transformString.svg = "translate("+transformArray[0]+","+transformArray[1]+")scale("+this.model.get('scaleLevel')+")"; 
		transformString.treeregion = "translate("+transformArray[0]+"px,"+transformArray[1]+"px)scale("+this.model.get('scaleLevel')+")";
		Cure.PlayerSvg.attr("transform", transformString.svg);
		$("#PlayerTreeRegionTree").css({
		      "transform" : transformString.treeregion
		});
		if(this.model.get('fitToScreen')){
			$("html,body").scrollTop(0);
		}
	},
	clickFitToScreen: function(){
		if(Cure.PlayerNodeCollection.length>0){
  		if($(this.ui.fitToScreen).is(':checked')){
  			this.model.set('fitToScreen',true);
  			var scaleLevel = (window.innerHeight-100)/(d3.select("#PlayerTreeRegionSVG").attr("height"));
    		if(scaleLevel>1){
    			scaleLevel = 1;
    		}
    		this.model.set('scaleLevel',scaleLevel);
    	} else {
    		this.model.set('fitToScreen',false);
    	}
  	}
	}
});

return ZoomView;
});
		        
