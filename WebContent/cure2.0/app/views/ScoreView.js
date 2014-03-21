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
		_.bindAll(this, 'updateScore', 'updateAxis');
		this.model.bind("change:size", this.updateScore);
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
		var yLimit = 80000;
		var xLength = Cure.Scorewidth-50;
		var tempSplitNodeArray = Cure.PlayerNodeCollection.getSplitNodeArray();
		var splitNodeArray = this.splitNodeArray;
		var pushCount = 0;
		for(var temp in splitNodeArray){
			var i = tempSplitNodeArray.length;
			while( i-- ) {
			    if( tempSplitNodeArray[i].cid ==  splitNodeArray[temp].cid ) break;
			}
			if(i==-1){
				splitNodeArray.splice(temp,1);
			}
		}
		for(var temp in tempSplitNodeArray){
			var i = splitNodeArray.length;
			while( i-- ) {
			    if( splitNodeArray[i].cid ==  tempSplitNodeArray[temp].cid ) break;
			}
			if(i==-1){
				splitNodeArray.push(tempSplitNodeArray[temp]);
				pushCount++;
			}
		}
		if(pushCount > 1) {
			splitNodeArray.splice(0,splitNodeArray.length-1);
		}
		if(splitNodeArray.length>0){
			splitNodeArray[splitNodeArray.length-1].score = [parseFloat(1000 * this.model.get('pct_correct')), parseFloat(750 * (1/this.model.get('size'))), parseFloat(500 * this.model.get('novelty'))];
		}
		
		if(thisModel.get('size')>0){
			yLimit = this.model.get('score')+10000;
			xLength = (splitNodeArray.length+1) * 50;
		}
		var	xAxisScale = d3.scale.ordinal().domain(splitNodeArray.map(function(d){ return d.cid;})).rangeRoundBands([0, xLength], 1);
		var yAxisScale = d3.scale.linear().domain([0, yLimit]).range([Cure.Scoreheight-30, 0 ]);
		//Update axis
		var xAxis = d3.svg.axis().scale(xAxisScale).orient("bottom").tickFormat(function(d){
			var i = splitNodeArray.length;
			while( i-- ) {
		    if( splitNodeArray[i].cid ==  d ) break;
			}
			return splitNodeArray[i].name; 
		});
		var yAxis = d3.svg.axis().scale(yAxisScale).orient("left").tickFormat(function (d) {
    	var prefix = d3.formatPrefix(d);
    	return prefix.scale(d) + "k";
		});
		this.xaxis.transition().duration(Cure.duration).call(xAxis);
		this.yaxis.transition().duration(Cure.duration).call(yAxis);
		
		var layer = this.SVG.selectAll(".layer").data(splitNodeArray).enter().append("g").attr("class","layer")
		.attr("transform",function(d){
			return "translate("+parseFloat(xAxisScale(d.cid)+15)+",0)";
		});
		
		var y=Cure.Scoreheight-20;
		layer.selectAll("scoreRect").data(function(d){return d.score; }).enter().append("rect").attr("class","scoreRect")
		.attr("width",30)
    .style("fill", function(d, i) { return Cure.colorScale(i); })
    .attr("y", function(d) { return y; })
    .attr("height", 0)
		.transition().duration(Cure.duration)
		.attr("y", function(d) { y-=yAxisScale(yLimit-d);return y; })
    .attr("height", function(d) { return yAxisScale(yLimit-d); });
		
		this.SVG.selectAll(".layer").data(splitNodeArray).exit().transition().duration(Cure.duration).attr("height",0).remove();
	},
	updateScore : function() {
		this.updateAxis();
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
