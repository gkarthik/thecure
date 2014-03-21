define([
  //Libraries
	'jquery',
	'marionette',
	'd3',
	//Templates
	'text!app/templates/Score.html',
	'text!app/templates/ScoreChangeSummary.html'
    ], function($, Marionette, d3, scoreTemplate, scoreChangeTemplate) {
ScoreView = Backbone.Marionette.ItemView.extend({
	initialize : function() {
		_.bindAll(this, 'updateScore');
		this.model.bind("change", this.updateScore);
	},
	ui : {
		'svg' : "#ScoreSVG",
		'scoreEL' : "#score",
		'scoreDetails': '#ScoreChangesWrapper'
	},
	events: {
		'click .showSVG': 'showSVG',
		'click .closeSVG': 'closeSVG'
	},
	template : scoreTemplate,
	showSVG: function(){
		$("#ScoreSVG").show();
		$(".showSVG").html('<i class="glyphicon glyphicon-resize-small"></i>Hide Chart');
		$(".showSVG").addClass("closeSVG");
		$(".showSVG").removeClass("showSVG");
	},
	closeSVG: function(){
		$("#ScoreSVG").hide();
		$(".closeSVG").html('<i class="glyphicon glyphicon-resize-full"></i>Explain Score');
		$(".closeSVG").addClass("showSVG");
		$(".closeSVG").removeClass("closeSVG");
	},
	drawAxis : function() {
		var thisModel = this.model;
		this.SVG = d3.selectAll(this.ui.svg).append("svg:g");		
		var xLimit = 10;
		var yLimit = 80000;
		if(thisModel.get('size')>0){
			xLimit = thisModel.get('size')+2;
			yLimit = thisModel.get('score')+10000;
		}
		var xAxisScale = d3.scale.linear().domain([0, xLimit]).range([0, Cure.Scorewidth-50]);
		var yAxisScale = d3.scale.linear().domain([0, yLimit]).range([Cure.Scorewidth-30, 0]);
		var xAxis = d3.svg.axis().scale(xAxisScale).orient("bottom");
		var yAxis = d3.svg.axis().scale(yAxisScale).orient("left").tickFormat(function (d) {
    	var prefix = d3.formatPrefix(d);
    	return prefix.scale(d) + "k";
		});
		
		this.xaxis = this.SVG.append("svg:g").attr("transform","translate(30,"+parseFloat(Cure.Scoreheight-20)+")")
											 .attr("class", "axis").call(xAxis);
		this.yaxis = this.SVG.append("svg:g").attr("transform","translate(30,10)")
		 .attr("class", "axis").call(yAxis);
	},
	splitNodeArray: [],
	updateAxis: function(){
		var thisModel = this.model;
		var xLimit = 10;
		var yLimit = 80000;
		var xLength = Cure.Scorewidth-50;
		var tempSplitNodeArray = Cure.PlayerNodeCollection.getSplitNodeArray();
		var splitNodeArray = this.splitNodeArray;
		var pushCount = 0;
		for(var temp in tempSplitNodeArray){
			if(this.splitNodeArray.indexOf(tempSplitNodeArray[temp].name)==-1){
				this.splitNodeArray.push(tempSplitNodeArray[temp].name);
				pushCount++;
			}
		}
		if(thisModel.get('size')>0){
			xLimit = splitNodeArray.length+1;
			yLimit = thisModel.get('score')+10000;
			xLength = splitNodeArray.length * 50;
		}
		var	xAxisScale = d3.scale.ordinal().domain(splitNodeArray.map(function(d){ return d;})).rangeRoundBands([0, xLength], 1);
		var yAxisScale = d3.scale.linear().domain([0, yLimit]).range([Cure.Scorewidth-30, 0]);
		//Update axis
		var xAxis = d3.svg.axis().scale(xAxisScale).orient("bottom");
		var yAxis = d3.svg.axis().scale(yAxisScale).orient("left").tickFormat(function (d) {
    	var prefix = d3.formatPrefix(d);
    	return prefix.scale(d) + "k";
		});
		this.xaxis.transition().duration(Cure.duration).call(xAxis);
		this.yaxis.transition().duration(Cure.duration).call(yAxis);
		
		this.SVG.append("svg:g").append("rect")
		.attr("width",50)
		.attr("x",function(d){
			return xAxisScale(splitNodeArray[splitNodeArray.length-1]);//Same Name Might cause problem!
		})
		.attr("y",function(){
			return Cure.Scoreheight-20;
		})
		.attr("height",function(){
			return 750 * (1 / thisModel.get("size"));
		}).style("fill","red");
		//750 * (1 / this.get("size")) + 500* this.get("novelty") + 1000 * this.get("pct_correct");
		
	},
	updateScore : function() {
		this.updateAxis();
		//Draw Rect
		$(this.ui.scoreEL).html(this.model.get("score"));
	},
	onShow: function(){
		this.drawAxis();
	},
	onRender : function() {
		Cure.ScoreSVG = d3.selectAll(this.ui.svg).attr("width", Cure.Scorewidth)
				.attr("height", Cure.Scoreheight);
	}
});
return ScoreView;
});
