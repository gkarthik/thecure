define([
	'jquery',
	'marionette',
	//Models
	'app/models/DistributionData',
	//Templates
	'text!app/templates/distributionChart.html',
	'jqueryui'
    ], function($, Marionette, DistributionData, distributionTmpl) {
DistChartView = Marionette.ItemView.extend({
	model: DistributionData,
	template: distributionTmpl,
	className: 'distribution-chart-wrapper',
	initialize: function(){
		this.model.set("cid",this.cid);
		this.listenTo(this.model, "change:globalHeight", this.onResize);
		this.listenTo(this.model, "change:globalWidth", this.onResize);
		this.listenTo(this.model, "change:range", this.drawChart);
	},
	ui: {
		distributionChart: ".distribution-chart",
		rangeInput: "#range-input"
	},
	events: {
		'change #range-input': 'changeRangeValue',
		'click #range-input': 'selectAllText'
	},
	onResize: function(){
			this.drawChart();
	},
	selectAllText: function(){
		$(this.ui.rangeInput).select();
	},
	onShow: function(){
		this.model.set('globalHeight',280,{'silent':true});
  	this.model.set('globalWidth', 450, {'silent':true});
		this.drawChart();
		var click = {
			x: 0,
		  y: 0
		};
		this.$el.draggable({ 
			handle: ".dragHandle" ,
			start: function(event) {
	        click.x = event.clientX;
	        click.y = event.clientY;
	    },
	    drag: function(event, ui) {
	        var zoom = Cure.Zoom.get('scaleLevel');
	        var original = ui.originalPosition;
	        ui.position = {
	            left: (event.clientX - click.x + original.left) / zoom,
	            top:  (event.clientY - click.y + original.top ) / zoom
	        };

	    }
		});
	},
	changeRangeValue: function(){
		var value = $(this.ui.rangeInput).val();
		if(!isNaN(value)){
			this.model.set('range',value);
		} else {
			Cure.utils.showAlert("Range > 0");
		}
	},
	drawChart: function(){
			var globalRange = parseFloat(this.model.get('range'));
			var data;
			var thisModel = this.model;
			try{
				data = this.model.get("dataArray");
			} catch (e) {
				data = [];
			}
			var id = $(this.ui.distributionChart).attr("id");
			var globalHeight = this.model.get('globalHeight');
			var globalWidth = this.model.get('globalWidth');
			d3.select("#"+id).select(".chartGroup").remove();
			var SVG = d3.select("#"+id).attr({"height":globalHeight,"width":globalWidth}).append("svg:g").attr("class","chartGroup");
			//Create JSON for value and frequency of value
			var xLength = globalWidth-60;
			var yLength = globalHeight-100;
			var marginX = 50;
			var plotValues = [];
			var frequencies = [];
			var isNominal = this.model.get("isNominal");
			var splitPoint = this.model.get('splitNode').get('options').get('split_point');
			var origSplitPoint = this.model.get('splitNode').get('options').get('orig_split_point');
			var lowerLimit = data[0].value;
			var upperLimit = data[data.length-1].value;
			var lowerFlag = true, upperFlag = true;
			
			//Get lowerLimit and upperLimit to ensure each class has at least 1 instance.
			for(var i = 0; i < data.length;i++){
				if(data[i].value>lowerLimit && lowerFlag){
					lowerLimit = data[i].value;
					lowerFlag = false;
				}
				if(data[data.length-1-i].value<upperLimit && upperFlag){
					upperLimit = data[data.length-1-i].value;
					upperFlag = false;
				}
				if(!lowerFlag && !upperFlag){
					break;
				}
			}
			
			var noOfRangeBands = 10;
			var range = parseFloat(((data[data.length-1].value) - data[0].value)/(noOfRangeBands-1));
			if(!isNaN(globalRange)){
				noOfRangeBands = ((data[data.length-1].value - data[0].value)/globalRange)+1;
				if(noOfRangeBands < 1){
					noOfRangeBands = 10;
				} else {
					range = globalRange;
				}
			}
			thisModel.set('range',range,{'silent':true});
			$(this.ui.rangeInput).val(parseInt(range*100)/100);
			//Check if numeric or nominal attribute
			if(data.length>0){
				if(!isNominal){
					var rangeIncrement = data[0].value;
					for(var i = 0; i <= noOfRangeBands; i++){
						plotValues.push({
							"value": rangeIncrement,
							"frequency": [0,0]//y,n
						});
						rangeIncrement += range;
					}
					for(var j=0; j<data.length; j++){
						for(var i = 0; i<noOfRangeBands; i++){
							if(data[j].value >= plotValues[i].value && data[j].value < (plotValues[i+1].value)){
								if(data[j].classprob==Cure.posNodeName){
									plotValues[i].frequency[0]++;
								} else if(data[j].classprob==Cure.negNodeName) {
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
			var rectWidth = (xLength)/plotValues.length;
			if(isNominal){
				rectWidth = rectWidth/2;
			}
			var valueScale = d3.scale.ordinal().domain(plotValues.map(function(d){return d.value;})).rangeBands([0,xLength]);
			var frequencyScale = d3.scale.linear().domain([0,frequencies[frequencies.length-1]]).range([yLength,0]);
			var xAxis = d3.svg.axis().tickFormat(function(d) { return isNominal ? d : Math.round(d*100)/100;}).scale(valueScale).orient("bottom");
			var yAxis = d3.svg.axis().scale(frequencyScale).orient("left");
			SVG.append("g").attr("class","axis xaxis").attr("transform", "translate("+marginX+","+(yLength+10)+")").call(xAxis)
				.append("svg:text").attr("transform","translate(0,30)").text(function(){
					if(thisModel.get('splitNode').get("options").get("unique_id").indexOf("metabric")==-1){
						return "Gene Expression Values";
					} else {
						return thisModel.get('splitNode').get("name");
					}
				}).style("fill","#808080");
			SVG.append("g").attr("class","axis yaxis").attr("transform", "translate("+marginX+",10)").call(yAxis)
				 .append("svg:text").text("Number of Instances").attr("transform","translate(-30,"+yLength+")rotate(-90)").style("fill","#808080");
			
			if(!isNominal){
				var rangeScale = d3.scale.linear().domain([0.1,data[data.length-1].value-data[0].value]).range([0,200]);
				var reverseRangeScale = d3.scale.linear().domain([0,200]).range([0.1,data[data.length-1].value-data[0].value]);
				var rangeAxis = d3.svg.axis().scale(rangeScale).orient("bottom");
				SVG.append("g").attr("class","axis range").attr("transform", "translate("+marginX+","+(yLength+80)+")").call(rangeAxis)
					 .append("svg:text").attr("transform","translate(-45,10)").text("Interval").style("fill","#3276B1");
				var newRange = 0;
				var rangeDrag = d3.behavior.drag().origin(function() { 
					var t = d3.select(this).attr("transform");
					var origin = String(t).match(/-?[0-9\.]+/g);
					return { x: parseFloat(origin[0]), y: parseFloat(origin[1]) }; 
				}).on("dragstart", function(d) {
					var t = d3.select(this).attr("transform");
	        var origin = String(t).match(/-?[0-9\.]+/g);
	        this.__customorigin__ = { x: parseFloat(origin[0]), y: parseFloat(origin[1]) };
	      }).on("drag", function(){
	      	var o = this.__customorigin__;
	        var min = marginX;
	        newRange = reverseRangeScale(Math.min(Math.max(d3.event.x-min, 0), 200));
	        SVG.selectAll(".rangeValue").text(Math.round(newRange*100)/100);
	        var translateX =  Math.min(200+min,  Math.max(min,  d3.event.x));
	        d3.select(this).attr("transform","translate("+translateX+","+o.y+")");
		    }).on("dragend",function(){
		    	thisModel.set('range',newRange);
		    	delete this.__customorigin__;
		    });
				
				var rangeKink = SVG.append("svg:g").attr("class","rangeKink").attr("transform", function(){
					var translateX = marginX + rangeScale(range);
					return "translate("+translateX+","+(yLength+70)+")";
				});
				rangeKink.append("svg:rect").attr("transform","translate(-20,-12)").attr("width","40").attr("height","15");
				rangeKink.append("path").attr("d", d3.svg.symbol().type("triangle-down")).attr("transform","translate(0,5)");
				rangeKink.append("svg:text").attr("class","rangeValue").attr("text-anchor","middle").text(parseInt(range*100)/100).style("fill","#FFF");
				rangeKink.call(rangeDrag);
			}
			
			SVG.append("svg:g").attr("class","distLayerGroup");
			var layer = SVG.selectAll(".distLayerGroup").selectAll(".distLayer").data(plotValues);
			
			
			var layerEnter = layer.enter().append("g").attr("class","distLayer")
			.attr("transform",function(d){
				var translateX = (marginX - (rectWidth/2)) + ((xLength)/(plotValues.length*2)) + parseFloat(valueScale(d.value));
				if(!isNominal){
						translateX = (marginX - (rectWidth/2)) + ((xLength)/(plotValues.length)) + parseFloat(valueScale(d.value));
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
	    	return parseFloat((yLength)-totalVal);
	    })
	    .attr("height", 0);
			
			layer.selectAll(".distRect").data(function(d){return d.frequency; }).transition().duration(function(d,i){
				return Cure.duration*(i);
			}).attr("height",function(d){
	    	return frequencyScale(frequencies[frequencies.length-1] - d);
			});
			
			if(!isNominal){
				xLength = xLength - ((xLength)/(plotValues.length));
				var splitScale = d3.scale.linear().domain([plotValues[0].value, plotValues[plotValues.length-1].value]).range([0,xLength]);
				var reverseSplitScale = d3.scale.linear().domain([0,xLength]).range([plotValues[0].value, plotValues[plotValues.length-1].value]);
				var splitPointIndicator = SVG.append("g").attr("class","split_point_indicator")
				.attr("transform",function(){
					var translateX = marginX + ((xLength)/(plotValues.length*2)) + parseFloat(splitScale(origSplitPoint));
					return "translate("+translateX+",10)";
				});
				splitPointIndicator.append("svg:rect").attr("height",yLength).attr("width",2).attr("fill","green");
				splitPointIndicator.append("svg:text").attr("fill","green").text(Math.round(origSplitPoint*100)/100).attr("text-anchor","middle").style("font-size","10px");
				var splitPointGroup = SVG.append("g").attr("class","split_point")
				.attr("transform",function(){
					var translateX = marginX + ((xLength)/(plotValues.length*2)) + parseFloat(splitScale(splitPoint));
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
	        var min = marginX+((xLength)/(plotValues.length*2));
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
				splitPointGroup.append("svg:rect").attr("height",yLength).attr("width",2).attr("fill","steelblue");
				splitPointGroup.append("svg:text").attr("class","splitValueLabel").attr("fill","steelblue").text(Math.round(splitPoint*100)/100).attr("text-anchor","middle").style("font-size","10px");				
				var dragHolder = splitPointGroup.append("svg:g").attr("transform","translate(-18,"+((yLength)/2)+")").attr("class","dragSplitPoint");
				dragHolder.append("svg:rect").attr("height",15).attr("width",40).attr("fill","steelblue").attr("transform","translate(0,-10)");
				dragHolder.append("svg:text").attr("fill","white").text("DRAG").style("font-size","10px").attr("transform","translate(2,2)");
			}
			var newWidth = globalWidth, newHeight = globalHeight;
			var resizedrag = d3.behavior.drag().origin(function(){return {x: 0, y: 0}; }).on("drag", function(){
      	newWidth=globalWidth + d3.event.x;
      	newHeight=globalHeight+d3.event.y;
      	d3.select("#"+id).attr("width",newWidth).attr("height",newHeight);
	    }).on("dragend",function(){
	    	thisModel.set('globalHeight',newHeight);
	    	thisModel.set('globalWidth',newWidth);
	    });
			
			var resizeChart = d3.select("#dragResize");
			resizeChart.call(resizedrag);
	}
});

return DistChartView;
});
