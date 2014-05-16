define([
	'jquery',
	'marionette',
	//Models
	'app/models/DistributionData',
	//Templates
	'text!app/templates/distributionChart.html'
    ], function($, Marionette, DistributionData, distributionTmpl) {
DistChartView = Marionette.ItemView.extend({
	model: DistributionData,
	template: distributionTmpl,
	initialize: function(){
		this.model.set("cid",this.cid);
	},
	ui: {
		distributionChart: ".distribution-chart"
	},
	onShow: function(){
			var data;
			var thisModel = this.model;
			try{
				data = this.model.get("dataArray");
			} catch (e) {
				data = [];
			}
			var id = $(this.ui.distributionChart).attr("id");
			var globalHeight = 200;
			var globalWidth = 400;
			d3.select("#"+id).select(".chartGroup").remove();
			var SVG = d3.select("#"+id).attr({"height":globalHeight,"width":globalWidth}).append("svg:g").attr("class","chartGroup");
			//Create JSON for value and frequency of value
			var xLength = globalWidth-40;
			var yLength = globalHeight-40;
			var plotValues = [];
			var frequencies = [];
			var isNominal = this.model.get("isNominal");
			var splitPoint = this.model.get('splitNode').get('options').get('split_point');
			var lowerLimit = data[1].value;
			var upperLimit = data[data.length-2].value;
			//Check if numeric or nominal attribute
			if(data.length>0){
				if(!isNominal){
					var range = data[0].value;
					for(var i = 0; i < 11; i++){
						plotValues.push({
							"value": range,
							"frequency": [0,0]//y,n
						});
						range += parseFloat((data[data.length-1].value - data[0].value)/10);
					}
					
					for(var temp in data){
						for(var i = 1; i<11; i ++){
							if(data[temp].value <= plotValues[i].value){
								if(data[temp].classprob==Cure.posNodeName){
									plotValues[i].frequency[0]++;
								} else if(data[temp].classprob==Cure.negNodeName) {
									plotValues[i].frequency[1]++;
								}
								break;
							}
						}
					}
				} else {
					var putInArray = 1;
					var arrayIndex = 0;
					for(var temp in data){
						putInArray = 1;
						arrayIndex = 0;
						for(var i in plotValues){
							if(plotValues[i].value == data[temp].value){
								putInArray = 0;
								arrayIndex = i;
								break;
							}
						}
						if(putInArray){
							plotValues.push({
								"value": data[temp].value,
								"frequency": [0,0]//y,n
							});
						} else {
							if(data[temp].classprob==Cure.posNodeName){
								plotValues[arrayIndex].frequency[0]++;
							} else if(data[temp].classprob==Cure.negNodeName) {
								plotValues[arrayIndex].frequency[1]++;
							}
						}
					}
				}
			}
			var total = 0;
			for(var temp in plotValues){
				total = 0;
				for(var i in plotValues[temp].frequency){
					total += plotValues[temp].frequency[i];
				}
				frequencies.push(total);
			}
			frequencies.sort(function(a,b){return a-b;});
			var rectWidth = (globalWidth-40)/plotValues.length;
			if(isNominal){
				rectWidth = rectWidth/2;
			}
			var valueScale = d3.scale.ordinal().domain(plotValues.map(function(d){return isNominal ? d.value : Math.round(d.value*100)/100;})).rangeBands([0,xLength]);
			var frequencyScale = d3.scale.linear().domain([0,frequencies[frequencies.length-1]]).range([yLength,0]);
			var xAxis = d3.svg.axis().scale(valueScale).orient("bottom");
			var yAxis = d3.svg.axis().scale(frequencyScale).orient("left");
			SVG.append("g").attr("class","axis xaxis").attr("transform", "translate(30,"+(globalHeight-30)+")").call(xAxis);
			SVG.append("g").attr("class","axis yaxis").attr("transform", "translate(30,10)").call(yAxis);
			
			SVG.append("svg:g").attr("class","distLayerGroup");
			var layer = SVG.selectAll(".distLayerGroup").selectAll(".distLayer").data(plotValues);
			
			var layerEnter = layer.enter().append("g").attr("class","distLayer")
			.attr("transform",function(d){
				var translateX = (30 - (rectWidth/2)) + ((globalWidth-40)/(plotValues.length*2)) + parseFloat(valueScale(d.value));
				if(!isNominal){
					translateX = (30 - (rectWidth/2)) + parseFloat(valueScale(d.value));
				}
				var total = 0;
				for(var i in plotValues[temp].frequency){
					total += plotValues[temp].frequency[i];
				}
				return "translate("+translateX+",10)";
			});
			
			//Insert rect on layerEnter
			var totalVal = 0;
			var layerRect = layerEnter.selectAll(".distRect").data(function(d){return d.frequency; });
			layerRect.enter().append("rect").attr("class","distRect")
			.attr("width", rectWidth)
	    .style("fill", function(d,i) { return (i==0) ? "blue" : "red"; })
	    .attr("y",function(d,i){
	    	if(i==0){
	    		totalVal = 0;
	    	}
	    	totalVal += frequencyScale(frequencies[frequencies.length-1] - d);
	    	return parseFloat((globalHeight-40)-totalVal);
	    })
	    .attr("height", 0);
			
			layer.selectAll(".distRect").data(function(d){return d.frequency; }).transition().duration(function(d,i){
				return Cure.duration*(i);
			}).attr("height",function(d){
	    	return frequencyScale(frequencies[frequencies.length-1] - d);
			});
			
			if(!isNominal){
				xLength = xLength - ((globalWidth-40)/(plotValues.length));
				var splitScale = d3.scale.linear().domain([plotValues[0].value, plotValues[plotValues.length-1].value]).range([0,xLength]);
				var reverseSplitScale = d3.scale.linear().domain([0,xLength]).range([plotValues[0].value, plotValues[plotValues.length-1].value]);
				var splitPointIndicator = SVG.append("g").attr("class","split_point_indicator")
				.attr("transform",function(){
					var translateX = 30 + ((globalWidth-40)/(plotValues.length*2)) + parseFloat(splitScale(splitPoint));
					var total = 0;
					for(var i in plotValues[temp].frequency){
						total += plotValues[temp].frequency[i];
					}
					return "translate("+translateX+",10)";
				});
				splitPointIndicator.append("svg:rect").attr("height",globalHeight-40).attr("width",2).attr("fill","green");
				splitPointIndicator.append("svg:text").attr("fill","green").text(Math.round(splitPoint*100)/100).attr("text-anchor","middle").style("font-size","10px");
				var splitPointGroup = SVG.append("g").attr("class","split_point")
				.attr("transform",function(){
					var translateX = 30 + ((globalWidth-40)/(plotValues.length*2)) + parseFloat(splitScale(splitPoint));
					var total = 0;
					for(var i in plotValues[temp].frequency){
						total += plotValues[temp].frequency[i];
					}
					return "translate("+translateX+",10)";
				});
				var origin = [0,0];
				var drag = d3.behavior.drag().origin(function() { 
					var t = d3.select(this).attr("transform");
					var origin = String(t).match(/-?[0-9\.]+/g);
					return { x: parseFloat(origin[0]), y: parseFloat(origin[1]) }; 
				}).on("dragstart", function(d) {
					var t = d3.select(this).attr("transform");
	        var origin = String(t).match(/-?[0-9\.]+/g);
	        this.__customorigin__ = { x: parseFloat(origin[0]), y: parseFloat(origin[1]) };
	      }).on("drag", function(){
	      	var o = this.__customorigin__;
	        var min = 30+((globalWidth-40)/(plotValues.length*2));
	        splitPoint = reverseSplitScale(Math.min(Math.max(d3.event.x-min, splitScale(lowerLimit)), splitScale(upperLimit)));
	        SVG.selectAll(".splitValueLabel").text(Math.round(splitPoint*100)/100);
	        var translateX =  Math.min(splitScale(upperLimit)+min,  Math.max(splitScale(lowerLimit)+min,  d3.event.x));
	        d3.select(this).attr("transform","translate("+translateX+","+o.y+")");
		    }).on("dragend",function(){
		    	var options = thisModel.get('splitNode').get('options');
		    	options.set('split_point', splitPoint);
		    	Cure.PlayerNodeCollection.sync();
		    	delete this.__customorigin__;
		    });
				SVG.select(".split_point").call(drag);
				splitPointGroup.append("svg:rect").attr("height",globalHeight-40).attr("width",2).attr("fill","steelblue");
				splitPointGroup.append("svg:text").attr("class","splitValueLabel").attr("fill","steelblue").text(Math.round(splitPoint*100)/100).attr("text-anchor","middle").style("font-size","10px");				
				var dragHolder = splitPointGroup.append("svg:g").attr("transform","translate(-18,"+((globalHeight-40)/2)+")").attr("class","dragSplitPoint");
				dragHolder.append("svg:rect").attr("height",15).attr("width",40).attr("fill","steelblue").attr("transform","translate(0,-10)");
				dragHolder.append("svg:text").attr("fill","white").text("DRAG").style("font-size","10px").attr("transform","translate(2,2)");
			}
	}
});

return DistChartView;
});
