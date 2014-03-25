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
		_.bindAll(this, 'updateScore', 'updateAxis', 'drawAxis', 'zoom');
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
		var xLimit = 10;
		var yLimit = 8000;
		if(thisModel.get('size')>0){
			xLimit = thisModel.get('size')+2;
			yLimit = thisModel.get('score')+1000;
		}
		this.xAxisScale = d3.scale.linear().domain([0, xLimit]).range([0, Cure.Scorewidth-70]);
		this.yAxisScale = d3.scale.linear().domain([0, yLimit]).range([Cure.Scorewidth-30, 0]);
		
		var xAxis = d3.svg.axis().scale(this.xAxisScale).orient("bottom").ticks(5);
		var yAxis = d3.svg.axis().scale(this.yAxisScale).orient("left").tickFormat(function (d) {
    	var prefix = d3.formatPrefix(d);
    	return prefix.scale(d) + "k";
		}).ticks(5);
		var yAxis2 = d3.svg.axis().scale(this.yAxisScale).orient("right").tickFormat(function (d) {
    	var prefix = d3.formatPrefix(d);
    	return prefix.scale(d) + "k";
		}).ticks(5);
		
		this.SVG = d3.selectAll(this.ui.svg).call(d3.behavior.zoom().scaleExtent([1, 1]).on("zoom", this.zoom));
		this.SVG.append("svg:g").attr("class","layerGroup").attr("transform","translate(0,0)");
		
		this.xaxis = this.SVG.append("svg:g").attr("transform","translate(35,"+parseFloat(Cure.Scoreheight-20)+")")
											 .attr("class", "xaxis axis").call(xAxis);
		
		this.yaxis = this.SVG.append("svg:g").attr("transform","translate(35,10)")
		 .attr("class", "yaxis axis").call(yAxis);
		
		this.yaxis2 = this.SVG.append("svg:g").attr("transform","translate("+parseFloat(Cure.Scorewidth-35)+",10)")
		 .attr("class", "yaxis axis").call(yAxis2);
		this.splitTranslate = [30,Cure.Scorewidth-20,1];
	},
	splitNodeArray: [],
	splitTranslate: [],
	zoom: function(){
		var splitTranslate = this.splitTranslate;
    this.SVG.selectAll(".layerGroup").attr("transform", "translate(" + parseFloat(d3.event.translate[0]-30+parseFloat(splitTranslate[0])) + ",0)scale(" + d3.event.scale + ", 1)");
    this.SVG.selectAll(".xaxis").attr("transform", "translate(" + parseFloat(d3.event.translate[0] + parseFloat(splitTranslate[0])) + ","+ splitTranslate[1] +")scale(" + d3.event.scale + ", 1)");
	},
	updateAxis: function(){	
		var thisModel = this.model;
		var thisView = this;
		var yLimit = 80000;
		var yLowerLimit = 0;
		var xLength = Cure.Scorewidth-70;
		var tempSplitNodeArray = Cure.PlayerNodeCollection.getSplitNodeArray();
		var splitNodeArray = this.splitNodeArray;
		var pushCount = 0;
		
		//Sync splitNodeArray and tempSplitNodeArray
		for(var i=0;i<splitNodeArray.length;i++){
			var j = tempSplitNodeArray.length;
			while( j-- ) {
			    if( tempSplitNodeArray[j].cid ==  splitNodeArray[i].cid ) break;
			}
			if(j==-1){
				splitNodeArray.splice(i,1);
				i--;
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
			splitNodeArray[splitNodeArray.length-1].score = [{label:'Accuracy', value: parseFloat(Cure.scoreWeights.pct_correct*this.model.get('pct_correct'))}, {label: 'Size', value: parseFloat(Cure.scoreWeights.size * (1/this.model.get('size')))}, {label: 'Novelty', value: parseFloat(Cure.scoreWeights.novelty * this.model.get('novelty'))}];
			splitNodeArray[splitNodeArray.length-1].scoreValue = thisModel.get('score');
		}
		
		if(thisModel.get('size')>0){
			yLowerLimit = splitNodeArray[0].score[0].value;
			yLimit = splitNodeArray[0].scoreValue;
			for(var temp in splitNodeArray){
				if(splitNodeArray[temp].score[0].value<yLowerLimit){
					yLowerLimit = (splitNodeArray[temp].score[0].value*Cure.scoreWeights.pct_correct);
				}
				if(splitNodeArray[temp].scoreValue>yLimit){
					yLimit = splitNodeArray[temp].scoreValue;
				}
			}
			yLimit += 100;
			yLowerLimit -= 2000;
			xLength = (splitNodeArray.length+1) * 50;
		}
		thisView.xAxisScale = d3.scale.ordinal().domain(splitNodeArray.map(function(d){ return d.cid;})).rangeRoundBands([0, xLength], 1);
		thisView.yAxisScale = d3.scale.linear().domain([yLowerLimit, yLimit]).range([Cure.Scoreheight-30, 0 ]);
		thisView.yAxisKinkScale = d3.scale.linear().domain([0, yLimit]).range([Cure.Scoreheight-30, 0 ]);
		
		//Update axis
		var xAxis = d3.svg.axis().scale(thisView.xAxisScale).orient("bottom").tickFormat(function(d){
			var i = splitNodeArray.length;
			while( i-- ) {
		    if( splitNodeArray[i].cid ==  d ) break;
			}
			return splitNodeArray[i].name; 
		});
		
		var yAxis = d3.svg.axis().scale(thisView.yAxisScale).orient("left").tickFormat(function (d) {
    	var prefix = d3.formatPrefix(d);
    	if(d>=1000){
    		return prefix.scale(d)+"k";
    	}
    	return prefix.scale(d);
		}).ticks(5);
		
		var yAxis2 = d3.svg.axis().scale(thisView.yAxisScale).orient("right").tickFormat(function (d) {
    	var prefix = d3.formatPrefix(d);
    	if(d>=1000){
    		return prefix.scale(d)+"k";
    	}
    	return prefix.scale(d);
		}).ticks(5);
		
		this.xaxis.transition().duration(Cure.duration).call(xAxis);
		this.yaxis.transition().duration(Cure.duration).call(yAxis);
		this.yaxis2.transition().duration(Cure.duration).call(yAxis2);
		
		//Insert line breaks(<tspan>) in axis text
		this.SVG.selectAll('.xaxis g text').each(thisView.insertLinebreaks);
		
		var translateX = 0;
		var layer = this.SVG.selectAll(".layerGroup").selectAll(".layer").data(splitNodeArray);
		
		var layerEnter = layer.enter().append("g").attr("class","layer")
		.attr("transform",function(d){
			translateX = parseFloat(thisView.xAxisScale(d.cid)+25);
			return "translate("+parseFloat(thisView.xAxisScale(d.cid)+25)+",0)";
		});
		
		y=Cure.Scoreheight-20;
		var value = 0;
		
		//Insert rect on layerEnter
		var layerRect = layerEnter.selectAll(".scoreRect").data(function(d){return d.score; });
		
		layerRect.enter().append("rect").attr("class","scoreRect")
		.attr("width",30)
    .style("fill", function(d, i) { return Cure.colorScale(i); })
    .attr("y",function(d){
    	if(d.label=="Accuracy"){
				return thisView.yAxisScale(yLimit-d.value+yLowerLimit);
			} 
    	var h = (thisView.yAxisScale(yLimit-(d.value+value)+yLowerLimit)-thisView.yAxisScale(yLimit-value+yLowerLimit));
    	value+=d.value;
    	return h;
    })
    .attr("height", 0);
		
		//Update rect height based on changing scale.
		y = Cure.Scoreheight-20;
		value = 0;
		
		layer.selectAll(".scoreRect").data(function(d){return d.score; }).transition().duration(function(d,i){
			return Cure.duration*(i);
		}).attr("y",function(d, i){
			if(i==0){
				y = Cure.Scoreheight-20;
				value = 0;
			}
			if(d.label=="Accuracy"){
				y-=thisView.yAxisScale(yLimit-d.value+yLowerLimit);
			} else {
				y-=(thisView.yAxisScale(yLimit-(d.value+value)+yLowerLimit)-thisView.yAxisScale(yLimit-value+yLowerLimit));
			}
    	return y;
    }).attr("height", function(d,i) {
    	if(i==0){
				y = Cure.Scoreheight-20;
				value = 0;
			}
    	if(d.label=="Accuracy"){
				return thisView.yAxisScale(yLimit-d.value+yLowerLimit);
			} 
    	var h = (thisView.yAxisScale(yLimit-(d.value+value)+yLowerLimit)-thisView.yAxisScale(yLimit-value+yLowerLimit));
			value+=d.value;
			return h;
			});
		
		/*
		y=Cure.Scoreheight-20 - thisView.yAxisScale(yLimit-thisModel.get('score'));
		
		layerEnter.append("svg:text").html(function(d){
			var text = "";
			if(d.diff>=0){
				text+="+";
			} else {
				text+="-";
			}
			text+=" "+Math.abs(d.diff);
			return text;
		}).attr("transform",function(d,i){
			return "translate("+0+","+(y-5-(11*i))+")";
		})
		.style("font-size","10px")
		.style("font-weight","bold")
		.style("fill",function(d,i){
			return Cure.colorScale(i);
		});
		*/
		
		layer.exit().transition().duration(Cure.duration).attr("transform",function(d){
			return "translate("+Cure.Scorewidth+",0)";
		}).remove();
		
		if(translateX==0 && thisView.splitNodeArray.length>=1){
			translateX = thisView.xAxisScale(thisView.splitNodeArray[thisView.splitNodeArray.length-1].cid)+20;
		} else if(thisView.splitNodeArray.length==0){
			translateX = Cure.Scorewidth-95;
		}
		translateX=(Cure.Scorewidth-95)-translateX;
		
    this.SVG.selectAll(".layerGroup").attr("transform", "translate(" + translateX + ",0)scale(1)");
    var transformString = this.SVG.selectAll(".xaxis").attr("transform");
    var splitTranslate = String(transformString).match(/-?[0-9\.]+/g);
    this.SVG.selectAll(".xaxis").attr("transform", "translate(" + (translateX+35) + ","+ splitTranslate[1] +")scale(1)");
    transformString = this.SVG.selectAll(".xaxis").attr("transform");
    this.splitTranslate = String(transformString).match(/-?[0-9\.]+/g);
	},
	insertLinebreaks: function (d) {
	  var el = d3.select(this);
	  var words=d3.select(this).text().split(' ');

	  el.text('');

	  for (var i = 0; i < words.length; i++) {
	var tspan = el.append('tspan').text(words[i]);
	if (i > 0)
	      tspan.attr('x', 0).attr('dy', '15');
	  }
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
				.attr("height", Cure.Scoreheight+50);
	}
});
return ScoreView;
});
