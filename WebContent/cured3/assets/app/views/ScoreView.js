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
			d3.select("#sizeBarChart").transition().duration(Cure.duration).style('width',Cure.sizeScale(1/this.model.get('size'))+'px');
			d3.select("#accuracyBarChart").transition().duration(Cure.duration).style('width',Cure.accuracyScale(this.model.get('pct_correct'))+'px');
			d3.select("#noveltyBarChart").transition().duration(Cure.duration).style('width',Cure.noveltyScale(this.model.get('novelty'))+'px');
			window.setTimeout(this.hideScoreDiff,8000);
	},
	hideScoreDiff: function(){
		$("#score-panel").removeClass('score-panel-extend');
	},
	updateScore : function() {
		$(this.ui.scoreEL).html(this.model.get("score"));
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
		var ctr = -1;
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
		var points = Cure.ScoreSVG.selectAll(".datapoint").data(json).enter()
				.append("circle").attr(
						"cx",
						function(d) {
							var length = 0;
							if (d.pct_correct) {
								length = Cure.accuracyScale(d.pct_correct);
							} else if (d.size) {
								length = Cure.sizeScale(1 / d.size);
							} else if (d.novelty) {
								length = Cure.noveltyScale(d.novelty);
							}
							ctr++;
							datapoints[ctr].x = (center.x)
									+ (length * Math.cos(ctr * interval_angle));
							return datapoints[ctr].x;
						}).attr(
						"cy",
						function(d) {
							if (ctr == 2) {
								ctr = -1;
							}
							var length = 0;
							if (d.pct_correct) {
								length = Cure.accuracyScale(d.pct_correct);
							} else if (d.size) {
								length = Cure.sizeScale(1 / d.size);
							} else if (d.novelty) {
								length = Cure.noveltyScale(d.novelty);
							}
							ctr++;
							datapoints[ctr].y = (center.y)
									+ (length * Math.sin(ctr * interval_angle));
							return datapoints[ctr].y;
						}).attr("class", "datapoint").attr("fill", function() {
					if (ctr >= 2) {
						ctr = -1;
					}
					ctr++;
					return Cure.colorScale(ctr);
				}).attr("r", 5);
		ctr = -1;
		var pointsUpdate = Cure.ScoreSVG.selectAll(".datapoint").transition()
				.duration(Cure.duration).attr(
						"cx",
						function(d) {
							var length = 0;
							if (d.pct_correct) {
								length = Cure.accuracyScale(d.pct_correct);
							} else if (d.size) {
								length = Cure.sizeScale(1 / d.size);
							} else if (d.novelty) {
								length = Cure.noveltyScale(d.novelty);
							}
							ctr++;
							datapoints[ctr].x = (center.x)
									+ (length * Math.cos(ctr * interval_angle));
							return datapoints[ctr].x;
						}).attr(
						"cy",
						function(d) {
							if (ctr == 2) {
								ctr = -1;
							}
							var length = 0;
							if (d.pct_correct) {
								length = Cure.accuracyScale(d.pct_correct);
							} else if (d.size) {
								length = Cure.sizeScale(1 / d.size);
							} else if (d.novelty) {
								length = Cure.noveltyScale(d.novelty);
							}
							ctr++;
							datapoints[ctr].y = (center.y)
									+ (length * Math.sin(ctr * interval_angle));
							return datapoints[ctr].y;
						}).attr("class", "datapoint").attr("fill", function() {
					if (ctr >= 2) {
						ctr = -1;
					}
					ctr++;
					return Cure.colorScale(ctr);
				}).attr("r", 5);

		Cure.ScoreSVG.selectAll(".dataPolygon").data([ datapoints ]).enter()
				.append("polygon").attr("class", "dataPolygon").attr("points",
						function(d) {
							return d.map(function(d) {
								return [ d.x, d.y ].join(",");
							}).join(" ");
						}).attr("stroke", "rgba(189,189,189,0.25)").attr("stroke-width",
						"0.5px").attr("fill", "rgba(242,223,191,0.5)");

		Cure.ScoreSVG.on("mouseover",
				function() {
					var d = Cure.ScoreSVG.selectAll(".dataPolygon").data()[0];
					var json = [];
					for ( var temp in thisModel.toJSON()) {
						if (temp != "treestruct" && temp != "text_tree") {
							json.push({});
							json[json.length - 1][temp] = thisModel.toJSON()[temp];
						}
					}
					Cure.ScoreSVG.selectAll(".hoverRect").data(d).enter().append("rect")
							.attr("x", function(d) {
								return d.x + 8;
							}).attr("y", function(d) {
								return d.y - 2;
							}).attr("height", "19").attr("width", "42").attr("fill",
									"#FFF").attr("class", "hoverRect");
					ctr = -1;
					Cure.ScoreSVG.selectAll(".hoverText").data(d).enter().append(
							"svg:text").attr("x", function(d) {
						return d.x + 10;
					}).attr("y", function(d) {
						return d.y + 10;
					}).style({
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
					}).attr("class", "hoverText");

					Cure.ScoreSVG.selectAll(".dataPolygon").attr("stroke",
							"rgba(189,189,189,0.5)").attr("stroke-width", "2px");
				}).on(
				"mouseleave",
				function(d) {
					Cure.ScoreSVG.selectAll(".hoverText").remove();
					Cure.ScoreSVG.selectAll(".hoverRect").remove();
					Cure.ScoreSVG.selectAll(".dataPolygon").attr("stroke",
							"rgba(189,189,189,0.25").attr("stroke-width", "0.5px");
				});

		Cure.ScoreSVG.selectAll(".dataPolygon").transition()
				.duration(Cure.duration).attr("points", function(d) {
					return d.map(function(d) {
						return [ d.x, d.y ].join(",");
					}).join(" ");
				}).attr("stroke", "rgba(189,189,189,0.25)").attr("stroke-width", "1px")
				.attr("fill", "rgba(255, 102, 153, 0.25) ");
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
		  format: '',
		  theme: 'train-station'
		});
	}
});
return ScoreView;
});
