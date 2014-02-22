define([
  //Libraries
	'jquery',
	'marionette',
	'd3',
	//Templates
	'text!app/templates/Score.html',
	'text!app/templates/ScoreChangeSummary.html',
	//Plugins
	'odometer'
    ], function($, Marionette, d3, scoreTemplate, scoreChangeTemplate, Odometer) {
ScoreView = Backbone.Marionette.ItemView.extend({
	initialize : function() {
		_.bindAll(this, 'updateScore');
		this.model.bind("change:scoreDiff", this.updateScore);
		var thisView = this;
		$(document).mouseup(function(e){
			var classToclose = $('.score-panel-extend');
	    if(!classToclose.is(e.target) && classToclose.has(e.target).length == 0) {
	    	thisView.hideScoreDiff();
	    }
		});
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
		$("#ScoreSVG").slideDown();
		$(".showSVG").html('<i class="glyphicon glyphicon-resize-small"></i>Hide Chart');
		$(".showSVG").addClass("closeSVG");
		$(".showSVG").removeClass("showSVG");
	},
	closeSVG: function(){
		$("#ScoreSVG").slideUp();
		$(".closeSVG").html('<i class="glyphicon glyphicon-resize-full"></i>Show Chart');
		$(".closeSVG").addClass("showSVG");
		$(".closeSVG").removeClass("closeSVG");
	},
	drawAxis : function() {
		var json = [];
		var thisModel = this.model;
		for ( var temp in this.model.toJSON()) {
			if (temp == "pct_correct" || temp == "novelty" || temp == "size") {
				json.push({});
				json[json.length - 1][temp] = this.model.toJSON()[temp];
			}
		}
		var interval_angle = 2 * Math.PI / json.length;
		var center = {
			"x" : parseInt((Cure.Scorewidth / 2) - 50),
			"y" : Cure.Scoreheight / 2
		};
		var ctr = -1;
		var maxlimit = [ {
			'x' : 0,
			'y' : 0
		}, {
			'x' : 0,
			'y' : 0
		}, {
			'x' : 0,
			'y' : 0
		} ];
		var maxHalflimit = [ {
			'x' : 0,
			'y' : 0
		}, {
			'x' : 0,
			'y' : 0
		}, {
			'x' : 0,
			'y' : 0
		} ];
		var axes = Cure.ScoreSVG.selectAll(".axis").data(json).enter().append(
				"svg:line").attr("x1", center.x).attr("y1", center.y).attr(
				"x2",
				function(d) {
					var length = 100;
					ctr++;
					maxlimit[ctr].x = (center.x)
							+ (length * Math.cos(ctr * interval_angle));
					maxHalflimit[ctr].x = (center.x)
							+ (length / 2 * Math.cos(ctr * interval_angle));
					return maxlimit[ctr].x;
				}).attr(
				"y2",
				function(d) {
					if (ctr >= 2) {
						ctr = -1;
					}
					var length = 100;
					ctr++;
					maxlimit[ctr].y = (center.y)
							+ (length * Math.sin(ctr * interval_angle));
					maxHalflimit[ctr].y = (center.y)
							+ (length / 2 * Math.sin(ctr * interval_angle));
					return maxlimit[ctr].y;
				}).attr("class", "axis").style("stroke", function() {
			if (ctr >= 2) {
				ctr = -1;
			}
			ctr++;
			return Cure.colorScale(ctr);
		}).style("stroke-width", "1px");
		ctr = -1;
		var axesUpdate = Cure.ScoreSVG.selectAll(".axis").transition().duration(
				Cure.duration).attr("x1", center.x).attr("y1", center.y).attr(
				"x2",
				function(d) {
					var length = 100;
					ctr++;
					maxlimit[ctr].x = (center.x)
							+ (length * Math.cos(ctr * interval_angle));
					maxHalflimit[ctr].x = (center.x)
							+ (length / 2 * Math.cos(ctr * interval_angle));
					return maxlimit[ctr].x;
				}).attr(
				"y2",
				function(d) {
					if (ctr >= 2) {
						ctr = -1;
					}
					var length = 100;
					ctr++;
					maxlimit[ctr].y = (center.y)
							+ (length * Math.sin(ctr * interval_angle));
					maxHalflimit[ctr].y = (center.y)
							+ (length / 2 * Math.sin(ctr * interval_angle));
					return maxlimit[ctr].y;
				}).attr("class", "axis").attr("class", "axis").style("stroke",
				function() {
					if (ctr >= 2) {
						ctr = -1;
					}
					ctr++;
					return Cure.colorScale(ctr);
				}).style("stroke-width", "1px");
		ctr = -1;
		var axesText = Cure.ScoreSVG.selectAll(".axisText").data(json).enter()
				.append("svg:text").attr("x", function(d) {
					var length = 100;
					ctr++;
					return (center.x) + (length * Math.cos(ctr * interval_angle)) + 10;
				}).attr("y", function(d) {
					if (ctr >= 2) {
						ctr = -1;
					}
					var length = 100;
					ctr++;
					return (center.y) + (length * Math.sin(ctr * interval_angle)) + 5;
				}).attr("class", "axisText").style("stroke", function() {
					if (ctr >= 2) {
						ctr = -1;
					}
					ctr++;
					return Cure.colorScale(ctr);
				}).style("stroke-width", "0.5px").text(function(d) {
					var text = "";
					if (d.pct_correct != null) {
						text = "(100) Accuracy";
					} else if (d.size != null) {
						text = "(1) Size";
					} else if (d.novelty != null) {
						text = "(1) Novelty";
					}
					return text;
				});
		Cure.ScoreSVG.selectAll(".maxPolygon").data([ maxlimit ]).enter().append(
				"polygon").attr("points", function(d) {
			return d.map(function(d) {
				return [ d.x, d.y ].join(",");
			}).join(" ");
		}).attr("stroke", "rgba(3,3,3,0.15)").attr("stroke-width", "1px").attr(
				"fill", "none").attr("class", "maxPolygon");
		Cure.ScoreSVG.selectAll(".maxHalfPolygon").data([ maxHalflimit ]).enter()
				.append("polygon").attr("points", function(d) {
					return d.map(function(d) {
						return [ d.x, d.y ].join(",");
					}).join(" ");
				}).attr("stroke", "rgba(3,3,3,0.15)").attr("stroke-width", "1px").attr(
						"fill", "none").attr("class", "maxHalfPolygon");
	},
	showScoreDiff: function(){
		if($("#score-panel .panel-body").css('display')=="none"){
			$("#score-panel .togglePanel").trigger('click');
		}
		$("#score-panel").addClass('score-panel-extend');
		$(this.ui.scoreDetails).html(scoreChangeTemplate(this.model.toJSON()));
			$(this.ui.scoreDetails).show();
	},
	hideScoreDiff: function(){
		$("#score-panel").removeClass('score-panel-extend');
	},
	updateScore : function() {
		var thisView = this;
		if(this.model.get('score') != 0){
			this.showScoreDiff();
		}
		var json = [];
		var thisModel = this.model;
		for ( var temp in this.model.toJSON()) {
			if (temp == "pct_correct" || temp == "novelty" || temp == "size") {
				json.push({});
				json[json.length - 1][temp] = this.model.toJSON()[temp];
			}
		}
		var interval_angle = 2 * Math.PI / json.length;
		var center = {
				"x" : parseInt((Cure.Scorewidth / 2) - 50),
				"y" : Cure.Scoreheight / 2
		};
		var ctr = -1;//Universal counter to loop through datapoints
		var datapoints = [ {
			"x" : 0,
			"y" : 0
		}, {
			"x" : 0,
			"y" : 0
		}, {
			"x" : 0,
			"y" : 0
		} ];
		
		var pointEnter = Cure.ScoreSVG.selectAll(".data-point-group").data(json).enter().append("svg:g").attr("class","data-point-group")
		.attr("transform",function(d){
			ctr++;
			var length = 0;
			if (d.pct_correct) {
				length = Cure.accuracyScale(d.pct_correct);
			} else if (d.size) {
				length = Cure.sizeScale(1 / d.size);
			} else if (d.novelty) {
				length = Cure.noveltyScale(d.novelty);
			}
			datapoints[ctr].x = (center.x)
					+ (length * Math.cos(ctr * interval_angle));
			var translateX = datapoints[ctr].x;
			var length = 0;
			if (d.pct_correct) {
				length = Cure.accuracyScale(d.pct_correct);
			} else if (d.size) {
				length = Cure.sizeScale(1 / d.size);
			} else if (d.novelty) {
				length = Cure.noveltyScale(d.novelty);
			}
			datapoints[ctr].y = (center.y)
					+ (length * Math.sin(ctr * interval_angle));
			var translateY = datapoints[ctr].y;
			return "translate("+translateX+","+translateY+")";
		});
		
		pointEnter.append("circle").attr("class", "datapoint").attr("fill", function() {
					if (ctr >= 2) {
						ctr = -1;
					}
					ctr++;
					return Cure.colorScale(ctr);
				}).attr("r", 5);
		
		pointEnter.append("rect")
		.attr("height", "19").attr("width", "42").attr("fill",
		"rgba(255,255,255,0.5)").attr("class", "hoverRect").attr("transform",function(d){
			var transformString = "";
			if (d.pct_correct == 0) {
				transformString = "translate(10,10)";
				console.log(transformString);	
			} else if (d.size == 0) {
				transformString = "translate(10,0)";
			} else if (d.novelty == 0) {
				transformString = "translate(5,15)";
			}
			return transformString;
		});

		pointEnter.append(
		"svg:text").style({
			"font-size" : "12px"
		}).attr("stroke","rgb(255, 102, 153)").text(function() {
			if (ctr >= 2) {
				ctr = -1;
			}
			ctr++;
			var text = 0;
			if (json[ctr].pct_correct) {
				text = json[ctr].pct_correct;
			} else if (json[ctr].size) {
				text = json[ctr].size;
			} else if (json[ctr].novelty) {
				text = json[ctr].novelty;
			}
			return text.toFixed(3);
		}).attr("class", "hoverText").attr("transform",function(d){
			var transformString = "";
			if (d.pct_correct == 0) {
				transformString = "translate(10,10)";
				console.log(transformString);	
			} else if (d.size == 0) {
				transformString = "translate(10,0)";
			} else if (d.novelty == 0) {
				transformString = "translate(5,15)";
			}
			return transformString;
		});
		
		ctr = -1;
		var pointsUpdate = Cure.ScoreSVG.selectAll(".data-point-group").transition()
					.duration(Cure.duration).delay(function(d, i) { return Cure.duration*3*i; }).attr("transform",function(d, i){
					ctr++;
					var length = 0;
					if (d.pct_correct) {
						window.setTimeout(function(){
							$(".accuracy-row").show();
						}, i * Cure.duration*3);
						length = Cure.accuracyScale(d.pct_correct);
					} else if (d.size) {
						window.setTimeout(function(){
							$(".size-row").show();
						}, i * Cure.duration*3);
						length = Cure.sizeScale(1 / d.size);
					} else if (d.novelty) {
						window.setTimeout(function(){
							$(".novelty-row").show();
						}, i * Cure.duration*3);
						length = Cure.noveltyScale(d.novelty);
					}
					datapoints[ctr].x = (center.x)
							+ (length * Math.cos(ctr * interval_angle));
					var translateX = datapoints[ctr].x;
					var length = 0;
					if (d.pct_correct) {
						length = Cure.accuracyScale(d.pct_correct);
					} else if (d.size) {
						length = Cure.sizeScale(1 / d.size);
					} else if (d.novelty) {
						length = Cure.noveltyScale(d.novelty);
					}
					datapoints[ctr].y = (center.y)
							+ (length * Math.sin(ctr * interval_angle));
					var translateY = datapoints[ctr].y;
					
					return "translate("+translateX+","+translateY+")";
				});
				
		Cure.ScoreSVG.selectAll(".hoverText").transition().duration(Cure.duration).text(function() {
			if (ctr >= 2) {
				ctr = -1;
			}
			ctr++;
			var text = 0;
			if (json[ctr].pct_correct) {
				text = json[ctr].pct_correct;
			} else if (json[ctr].size) {
				text = json[ctr].size;
			} else if (json[ctr].novelty) {
				text = json[ctr].novelty;
			}
			return Math.round(parseFloat(text)*100)/100;
		});
				
				
				//Data Polygon Enter
				Cure.ScoreSVG.selectAll(".dataPolygon").data([ datapoints ]).enter()
				.append("polygon").attr("class", "dataPolygon").attr("points",
						function(d) {
							return d.map(function(d) {
								return [ d.x, d.y ].join(",");
							}).join(" ");
						}).attr("stroke", "rgba(189,189,189,0.25)").attr("stroke-width",
						"0.5px").attr("fill", "rgba(242,223,191,0.5)");
				//Data Polygon Transition
				Cure.ScoreSVG.selectAll(".dataPolygon").transition()
				.duration(Cure.duration).delay(function(d, i) { return Cure.duration*3*i; }).attr("points", function(d, i) {
					return d.map(function(d) {
						return [ d.x, d.y ].join(",");
					}).join(" ");
				}).attr("stroke", "rgba(189,189,189,0.25)").attr("stroke-width", "1px")
				.attr("fill", "rgba(255, 102, 153, 0.25) ");
				
		window.setTimeout(function(){
			$(thisView.ui.scoreEL).html(thisView.model.get("score"));
			window,setTimeout(function(){
				thisView.hideScoreDiff();
			}, 8000);
		}, Cure.duration * 7);
	},
	onRender : function() {
		Cure.ScoreSVG = d3.selectAll(this.ui.svg).attr("width", Cure.Scorewidth)
				.attr("height", Cure.Scoreheight);
		this.drawAxis();
		this.updateScore();
	},
	onShow : function(){
		var el = document.getElementById("score");
		od = new Odometer({
		  el: el,
		  value: 0,
		  duration: '2000',
		  format: '',
		  theme: 'train-station'
		});
	}
});
return ScoreView;
});
