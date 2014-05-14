define([
  //Libraries
	'jquery',
	'marionette',
	'backbone',
	//templates
	'text!app/templates/chart.html',
    ], function($, Marionette, Backbone, chartTmpl) {
optionsView = Marionette.ItemView.extend({
	tagName: 'div',
	className: 'chartBorder',
	initialize : function() {
		this.listenTo(this.model,'change:accLimit', this.renderChart);
		this.listenTo(this.model,'change:viewWidth', this.renderChart);
	},
	template: chartTmpl,
	renderChart: function(){
		var id = "#chart"+this.model.get('cid');
		var radius = 4;
		var width = this.model.get('viewWidth')-20;
		radius = parseFloat((width - 4)/20);
		var limit = Cure.binScale(this.model.get('bin_size'));
		Cure.utils.drawChart(d3.selectAll(id), limit, this.model.get('accLimit'), radius, this.model.get('kind'), this.model.get('AttributeNode').get('name'));
		var classToChoose = {"className":" .posCircle","color":""};
		if(this.model.get('AttributeNode').get('name') == Cure.negNodeName){
			classToChoose["color"]= "red";
		} else{
			classToChoose["color"]= "blue";
		}
		d3.selectAll(id+classToChoose["className"]).style("fill",classToChoose["color"]);
	},
	onRender: function(){
		this.renderChart();
	},
	onShow: function(){
		this.renderChart();
	}
});

return optionsView;
});