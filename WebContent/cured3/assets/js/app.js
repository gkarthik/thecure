//
//-- Defining the App
//
Cure = new Backbone.Marionette.Application();

//
// -- Defining our collections
//
NodeCollection = Backbone.Collection.extend({
	model : Node,
	initialize : function() {
	},
	url : "/cure/MetaServer",
	sync : function() {
		//Function to send request to Server with current tree information.
		var tree = [];
		if (this.models[0]) {
			tree = this.models[0].toJSON();
		}
		var args = {
			command : "scoretree",
			dataset : "griffith_breast_cancer_1",
			treestruct : tree,
			comment: Cure.Comment.get("content")
		};
		//POST request to server.		
		$.ajax({
			type : 'POST',
			url : '/cure/MetaServer',
			data : JSON.stringify(args),
			dataType : 'json',
			contentType : "application/json; charset=utf-8",
			success : this.parseResponse,
			error : this.error
		});
	},
	updateCollection : function(json_node, node, parent) {
		if (node != null && json_node != null) {
			for ( var key in json_node) {
				if (key != "children") {
					node.set(key, json_node[key]);
				}
			}
			if (json_node.children.length > 0
					&& json_node.children.length == node.get('children').length) {
				for ( var temp in json_node.children) {
					this.updateCollection(json_node.children[temp],
							node.get('children').models[temp]);
				}
			} else if (json_node.children.length > node.get('children').length) {
				for ( var temp in json_node.children) {
					this.updateCollection(json_node.children[temp], null, node);
				}
			} else if (json_node.children.length < node.get('children').length) {
				var temp = 0;
				for (temp in json_node.children) {
					this.updateCollection(json_node.children[temp],
							node.get('children').models[temp]);
				}
				temp++;
				for ( var i = temp; i < node.get('children').length; i++) {
					delete_all_children(node.get('children')[temp]);
					node.get('children')[temp].destroy();
				}
			}
		} else if (node == null) {
			var newNode = new Node({
				'name' : "",
				"options" : {},
			});
			for ( var key in json_node) {
				if (key != "children") {
					newNode.set(key, json_node[key]);
				}
			}
			newNode.set("cid", newNode.cid);
			if (parent != null) {
				parent.get('children').add(newNode);
			}
			for ( var temp in json_node.children) {
				this.updateCollection(json_node.children[temp], null, newNode);
			}
		}
		Cure.render_network(Cure.PlayerNodeCollection.toJSON()[0]);
		Cure.updatepositions(Cure.PlayerNodeCollection);
	},
	parseResponse : function(data) {
		//If empty tree is returned, no tree rendered.
		if (data["treestruct"].name) {
			Cure.PlayerNodeCollection.updateCollection(data["treestruct"], Cure.PlayerNodeCollection.models[0], null);
		} else {
		//If server returns json with tree render and update positions of nodes.
			Cure.render_network(Cure.PlayerNodeCollection.toJSON()[0]);
			Cure.updatepositions(Cure.PlayerNodeCollection);
		}
		//Storing Score in a Score Model.
		var scoreArray = data;
		scoreArray.treestruct = null;
		if(scoreArray.novelty == "Infinity"){
			scoreArray.novelty = 0;
		}
		Cure.Score.set(scoreArray);
	},
	error : function(data) {
		console.log("Error Receiving Data From Server.");
	}
});

//
// -- Defining our models
//
Score = Backbone.RelationalModel.extend({
	defaults : {
		novelty : 0,
		pct_correct : 0,
		size : 1,// Least Size got form server = 1.
		score : 0,
	}
});

Comment = Backbone.RelationalModel.extend({
	defaults: {
		content: "",
		editView: 0
	}
});

Node = Backbone.RelationalModel.extend({
	defaults : {
		'name' : '',
		'cid' : 0,
		'options' : {
			"id" : "",
			"kind" : "split_node"
		},
		edit : 0,
		highlight : 0,
		children : [],
		gene_summary : {
			"summaryText" : "",
			"goTerms" : {},
			"generif" : {},
			"name" : ""
		},
		showJSON : 0,
		x: 0,
		y: 0,
		x0 : 0,
		y0: 0
	},
	initialize : function() {
		Cure.PlayerNodeCollection.add(this);
	},
	relations : [ {
		type : Backbone.HasMany,
		key : 'children',
		relatedModel : 'Node',
		reverseRelation : {
			key : 'parentNode',
			includeInJSON : false
		}
	} ]
});

//
// -- Defining our views
//

CommentView = Backbone.Marionette.ItemView.extend({
	tagName: 'div',
	className: 'commentBox',
	ui: {
		commentContent: ".commentContent"
	},
	template : "#commentTemplate",
	events: {
		"click .enter-comment": 'changeView',
		"click .save-comment": 'saveComment'
	},
	initialize : function(){
		this.model.bind('change', this.render);
	},
	changeView: function(){
		this.model.set("editView",1);
	},
	saveComment: function(){
		this.model.set("content",$(this.ui.commentContent).val());
		this.model.set("editView",0);
		Cure.PlayerNodeCollection.sync();
	}
});

//HTML Templates for different types of nodes.
var node_html = $("#nodeTemplate").html();
var splitvaluenode_html = $("#splitValueTemplate").html();
var splitnode_html = $("#splitNodeTemplate").html();

// -- View to manipulate each single node
NodeView = Backbone.Marionette.ItemView.extend({
	tagName : 'div',
	className : 'node',
	ui : {
		input : ".edit",
		addgeneinfo : ".addgeneinfo",
		name : ".name"
	},
	template : function(serialized_model) {
		if (serialized_model.options.kind == "split_value") {
			return _.template(splitvaluenode_html, {
				name : serialized_model.name,
				highlight : serialized_model.highlight,
				options : serialized_model.options,
				cid : serialized_model.cid
			}, {
				variable : 'args'
			});
		} else if (serialized_model.children.length > 0
				&& serialized_model.options.kind == "split_node") {
			return _.template(splitnode_html, {
				name : serialized_model.name,
				highlight : serialized_model.highlight,
				options : serialized_model.options,
				cid : serialized_model.cid
			}, {
				variable : 'args'
			});
		}
		return _.template(node_html, {
			name : serialized_model.name,
			highlight : serialized_model.highlight,
			options : serialized_model.options,
			cid : serialized_model.cid
		}, {
			variable : 'args'
		});
	},
	events : {
		'click button.addchildren' : 'addChildren',
		'click button.delete' : 'removeChildren',
		'click .name' : 'showSummary',
		'blur .name': 'setHighlight'
	},
	initialize : function() {
		_.bindAll(this, 'remove', 'addChildren', 'showSummary');
		this.model.bind('change', this.render);
		this.model.bind('add:children', this.render);
		this.model.bind('remove', this.remove);
	},
	setHighlight: function(){
		this.model.set('highlight',0);
	},
	showSummary : function() {
		//showJSON is used to render the Gene Info POP Up.
		if (this.model.get("options").kind == "split_node") {
			this.model.set("showJSON", 1);
		}
	},
	onBeforeRender : function() {
		//Render the positions of each node as obtained from d3.
		var width = $(this.el).width();
		
		if(width<=100 && this.model.get('options').kind!="split_value"){
			width = 100;;
		}
		
		$(this.el).stop(false, false).animate(
				{
					'margin-left' : (this.model.get('x') - ((width) / 2))
							+ "px",
					'margin-top' : (this.model.get('y') + 70) + "px"
				});
		$(this.el).attr("class", "node");
		$(this.el).addClass(this.model.get("options").kind);
	}, 
	remove : function() {
		//Remove and destroy current node.
		this.$el.remove();
		Cure.delete_all_children(this.model);
		this.model.destroy();
	},
	removeChildren : function() {
		//Remove all children from current node.
		if (this.model.get('parentNode') != null) {
			Cure.delete_all_children(this.model);
			var prevAttr = this.model.get("previousAttributes");
			for ( var key in prevAttr) {
				this.model.set(key, prevAttr[key]);
			}
			this.model.set("previousAtributes", []);
		} else {
			Cure.delete_all_children(this.model);
			this.model.destroy();
		}
		Cure.PlayerNodeCollection.sync();
	},
	addChildren : function() {
		//Adding new children to the node. This function shows the AddRootNodeView to show mygeneinfo autocomplete.
		var GeneInfoRegion = new Backbone.Marionette.Region({
			el : "#" + $(this.ui.addgeneinfo).attr("id")
		});
		Cure.addRegions({
			MyGeneInfoRegion : GeneInfoRegion
		});
		var ShowGeneInfoWidget = new AddRootNodeView({
			'model' : this.model
		});
		Cure.MyGeneInfoRegion.show(ShowGeneInfoWidget);
	}
});

//View to reflect current score and radar chart.
ScoreView = Backbone.Marionette.ItemView.extend({
	initialize : function() {
		_.bindAll(this, 'updateScore');
		this.model.bind("change:pct_correct", this.updateScore);
		this.model.bind("change:size", this.updateScore);
		this.model.bind("change:novelty", this.updateScore);
	},
	ui : {
		'svg' : "#ScoreSVG",
		'scoreEL' : "#score"
	},
	template : "#ScoreTemplate",
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
			"x" : parseInt(Cure.width / 8 - 20),
			"y" : Cure.height / 6
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
	updateScore : function() {
		// 1000*pct_correct + 750*1/size_of_tree + 500*feature_novelty
		if (this.model.get("size") != 1) {
			var score = 750 * (1 / this.model.get("size")) + 500
					* this.model.get("novelty") + 1000 * this.model.get("pct_correct");
			this.model.set("score", Math.round(score));
		} else {
			this.model.set({
				"score" : 0,
				"size" : 1 / 0,
				"pct_correct" : 0,
				"novelty" : 0
			});
		}
		$(this.ui.scoreEL).html(this.model.get("score"));
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
			"x" : parseInt(Cure.width / 8 - 20),
			"y" : Cure.height / 6
		};
		var ctr = -1;
		var accuracyScale = d3.scale.linear().domain([ 0, 100 ]).rangeRound(
				[ 0, 100 ]);
		var noveltyScale = d3.scale.linear().domain([ 0, 1 ])
				.rangeRound([ 0, 100 ]);
		var sizeScale = d3.scale.linear().domain([ 0, 1 ]).rangeRound([ 0, 100 ]);
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
								length = accuracyScale(d.pct_correct);
							} else if (d.size) {
								length = sizeScale(1 / d.size);
							} else if (d.novelty) {
								length = noveltyScale(d.novelty);
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
								length = accuracyScale(d.pct_correct);
							} else if (d.size) {
								length = sizeScale(1 / d.size);
							} else if (d.novelty) {
								length = noveltyScale(d.novelty);
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
								length = accuracyScale(d.pct_correct);
							} else if (d.size) {
								length = sizeScale(1 / d.size);
							} else if (d.novelty) {
								length = noveltyScale(d.novelty);
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
								length = accuracyScale(d.pct_correct);
							} else if (d.size) {
								length = sizeScale(1 / d.size);
							} else if (d.novelty) {
								length = noveltyScale(d.novelty);
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

		Cure.ScoreSVG.on(
				"mouseover",
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
							}).attr("height", "17").attr("width", "40").attr("fill",
									"rgb(255,213,52)").attr("class", "hoverRect");
					ctr = -1;
					Cure.ScoreSVG.selectAll(".hoverText").data(d).enter().append(
							"svg:text").attr("x", function(d) {
						return d.x + 10;
					}).attr("y", function(d) {
						return d.y + 10;
					}).style({
						"font-size" : "10px",
						"background" : "#FFF"
					}).attr("stroke", function() {
						if (ctr >= 2) {
							ctr = -1;
						}
						ctr++;
						return Cure.colorScale(ctr);
					}).text(function() {
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
				.attr("fill", "rgba(229,32,30,0.25) ");
	},
	onRender : function() {
		Cure.ScoreSVG = d3.selectAll(this.ui.svg).attr("width", Cure.width / 3)
				.attr("height", Cure.height / 3);
		this.drawAxis();
		this.updateScore();
	}
});

var geneinfosummary = $("#GeneInfoSummary").html();
AddRootNodeView = Backbone.Marionette.ItemView.extend({
	initialize : function() {
	},
	ui : {
		'input' : '.mygene_query_target'
	},
	template : "#AddRootNode",
	render : function() {
		if (this.model) {
			var model = this.model;
		}
		var html_template = $("#AddRootNode").html();
		this.$el.html(html_template);
		this.$el.find('input.mygene_query_target').genequery_autocomplete({
			minLength: 1,
			focus: function( event, ui ) {
				focueElement = $(event.currentTarget);//Adding PopUp to .ui-auocomplete
				if($("#SpeechBubble")){
					$("#SpeechBubble").remove();
				}
				focueElement.append("<div id='SpeechBubble'></div>")
				$.getJSON("http://mygene.info/v2/gene/"+ui.item.id,function(data){
					var summary = {
							summaryText: data.summary,
							goTerms: data.go,
							generif: data.generif,
							name: data.name
					};
					var html = _.template(geneinfosummary, {
						symbol : data.symbol,
						summary : summary
					}, {
						variable : 'args'
					});
					var offset = $(".ui-autocomplete").offset();
					var uiwidth = $(".ui-autocomplete").width();
					var width = 0.9 * (offset.left);
					var left = 0;
					if(window.innerWidth - (offset.left+uiwidth) > offset.left ){
						left = offset.left+uiwidth+10;
						width = 0.9 * (window.innerWidth - (offset.left+uiwidth));
					}
					$("#SpeechBubble").css({
						"top": "10%",
						"left": left,
						"height": "50%",
						"width": width,
						"display": "block"
					});
					$("#SpeechBubble").html(html);
					$("#SpeechBubble .summary_header").css({
						"width": (0.9*width)
					});
					$("#SpeechBubble .summary_content").css({
						"margin-top": $("#SpeechBubble .summary_header").height()+10
					});
				});
			},
			search: function( event, ui ) {
				$("#SpeechBubble").remove();
			},
			select : function(event, ui) {
				$("#SpeechBubble").remove();
				var kind_value = "";
				try {
					kind_value = model.get("options").kind;
				} catch (exception) {
				}
				if (kind_value == "leaf_node") {
					model.set("previousAttributes", model.toJSON());
					model.set("name", ui.item.symbol);
					model.set("options", {
						id : ui.item.id,
						"kind" : "split_node",
						"full_name" : ui.item.name
					});
				} else {
					var newNode = new Node({
						'name' : ui.item.symbol,
						"options" : {
							id : ui.item.id,
							"kind" : "split_node",
							"full_name" : ui.item.name
						}
					});
					newNode.set("cid", newNode.cid);
					if (model.get("children")) {
						model.get("children").add(newNode);
					}
				}
				if (Cure.MyGeneInfoRegion) {
					Cure.MyGeneInfoRegion.close();
				}
				Cure.PlayerNodeCollection.sync();
			}
		});
		
		$("body").delegate(".close","click",function(){
			if($(this).parent().attr("class")=="alert")
			{
				$(this).parent().hide();
			}
			else{
				$(this).parent().parent().parent().hide();
			}
		})
		
		$(document).mouseup(function(e) {
			var container = $("#mygene_addnode");
			var geneList = $(".ui-autocomplete");

			if (!container.is(e.target)	&& container.has(e.target).length === 0 && !geneList.is(e.target)	&& geneList.has(e.target).length === 0) 
			{
				$("input.mygene_query_target").val("");
				if (Cure.MyGeneInfoRegion) {
					Cure.MyGeneInfoRegion.close();
				}
			}
		});
	}
});

NodeCollectionView = Backbone.Marionette.CollectionView.extend({
	// -- View to manipulate and display list of all nodes in collection
	itemView : NodeView,
	emptyView : AddRootNodeView,
	initialize : function() {

	}
});

//HTML Templates for rendering Gene SUmary Pop Up and Node Information. 
var shownode_html = $("#JSONtemplate").html();
var nodeedit_html = $('#Attrtemplate').html();

// -- View to render Gene SUmmary List
JSONItemView = Backbone.Marionette.ItemView.extend({
	model : Node,
	ui : {
		jsondata : ".jsonview_data",
		showjson : ".showjson",
		attreditwrapper : ".attreditwrapper",
		attredit : ".attredit",
		input : ".edit",
		key : ".attrkey",
	},
	events : {
		'click .showjson' : 'ShowJSON',
		'click button.close' : 'HideJSON',
		'click .showattr' : 'ShowAttr',
		'click .editdone' : 'doneEdit',
	},
	tagName : "tr",
	initialize : function() {
		_.bindAll(this, 'getSummary', 'ShowJSON', 'HideJSON');
		this.model.bind('change', this.render);
		this.model.on('change:edit', function() {
			if (this.model.get('edit') != 0) {
				this.$el.addClass('editnode');
			} else {
				this.$el.removeClass('editnode');
			}
		}, this);
		this.model.on('change:showJSON', function() {
			if (this.model.get('showJSON') != 0) {
				this.ShowJSON();
			}
		}, this);
	},
	onRender : function() {
		if (this.model.get('showJSON') != 0) {
			this.ShowJSON();
		}
	},
	getSummary : function() {
		var thisView = this,
			summary = this.model.get("gene_summary").summaryText || "";
		if (summary.length == 0) {
			$.getJSON("http://mygene.info/v2/gene/" + thisView.model.get("options").id,
					function(data) {
						var summary = {
							"summaryText" : data.summary,
							"goTerms" : data.go,
							"generif" : data.generif,
							"name" : data.name
						};
						thisView.model.set("gene_summary", summary);
						thisView.model.set("showJSON", 1);
					});
		}
	},
	template : function(serialized_model) {
		var name = serialized_model.name;
		var options = serialized_model.options;
		if (serialized_model.edit == 0) {
			return _.template(shownode_html, {
				name : name,
				summary : serialized_model.gene_summary,
				kind : serialized_model.options.kind
			}, {
				variable : 'args'
			});
		} else {
			return _.template(nodeedit_html, {
				name : name,
				options : options
			}, {
				variable : 'args'
			});
		}
	},
	ShowJSON : function() {
		this.getSummary();
		this.$el.find(this.ui.showjson).addClass("disabled");
		this.$el.find(this.ui.jsondata).css({
			'display' : 'block'
		});
	},
	HideJSON : function() {
		$(this.ui.jsondata).css({
			'display' : 'none'
		});
		$(this.ui.showjson).removeClass("disabled");
		this.model.set("showJSON", 0);
	},
	ShowAttr : function() {
		this.model.set('edit', 1);
	},
	doneEdit : function() {
		this.model.set('edit', 0);
	}
});

// -- View to render empty Gene SUmmar List for genes not of type "split_node"
EmptyJSONItemView = Backbone.Marionette.ItemView.extend({
	model : Node,
	template : "#EmptyTemplate"
});

//Collection View to render gene summary list.
JSONCollectionView = Backbone.Marionette.CollectionView.extend({
	getItemView : function(model) {
		if (model.get("options").kind == "split_node") {
			return JSONItemView;
		} else {
			return EmptyJSONItemView;
		}

	},
	collection : NodeCollection,
	initialize : function() {
		this.collection.bind('add', this.render);
		this.collection.bind('remove', this.render);
	}
});

//
// -- Utilities / Helpers
//

// -- Pretty Print JSON. Function NOT being used in code but can be used for rendering JSON for testing purposes.
// -- Ref :
// http://stackoverflow.com/questions/4810841/json-pchildjson["children"].length>0retty-print-using-javascript

Cure.prettyPrint = function(json) {
	if (typeof json != 'string') {
		json = JSON.stringify(json, undefined, 2);
	}
	json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;')
			.replace(/>/g, '&gt;');
	return json
			.replace(
					/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g,
					function(match) {
						var cls = 'number';
						if (/^"/.test(match)) {
							if (/:$/.test(match)) {
								cls = 'key';
							} else {
								cls = 'string';
							}
						} else if (/true|false/.test(match)) {
							cls = 'boolean';
						} else if (/null/.test(match)) {
							cls = 'null';
						}
						return match;
					});
}

//
// -- Function to get positions from d3 and update the attributes of the NodeCollection.
//
Cure.updatepositions = function(NodeCollection) {
	var Collection = [];
	if (NodeCollection.toJSON()[0]) {
		Collection = NodeCollection.toJSON()[0];
	}
	var d3nodes = [];
	d3nodes = Cure.cluster.nodes(Collection);
	Cure.cluster.nodes(Collection);
	d3nodes.forEach(function(d) {
		d.y = d.depth * 80;
	});
	d3nodes.forEach(function(d) {
		d.x0 = d.x;
		d.y0 = d.y;
	});
	for ( var temp in NodeCollection["models"]) {
		for ( var innerTemp in d3nodes) {
			if (String(d3nodes[innerTemp].cid) == String(NodeCollection["models"][temp]
					.get('cid'))) {
				NodeCollection["models"][temp].set("x", d3nodes[innerTemp].x);
				NodeCollection["models"][temp].set("y", d3nodes[innerTemp].y);
				NodeCollection["models"][temp].set("x0", d3nodes[innerTemp].x0);
				NodeCollection["models"][temp].set("y0", d3nodes[innerTemp].y0);
			}
		}
	}
}

//
// -- Function to delete all children of a node
//
Cure.delete_all_children = function(seednode) {
	var children = seednode.get('children');
	if (seednode.get('children').length > 0) {
		for ( var temp in children.models) {
			Cure.delete_all_children(children.models[temp]);
			children.models[temp].destroy();
		}
	}
}

//
// -- Render d3 Network
//
Cure.render_network = function(dataset) {
	var scaleY = d3.scale.linear().domain([ 0, 1 ]).rangeRound([ 0, 100 ]);
	var infoY = d3.scale.log().domain([ 0.00001, 1 ]).rangeRound([ 0, 100 ]);

	if (dataset) {
		var binY = d3.scale.linear().domain([ 0, dataset.options.bin_size ])
				.rangeRound([ 0, 30 ]);
		var binsizeY = d3.scale.linear().domain([ 0, dataset.options.bin_size ])
				.rangeRound([ 0, 100 ]);
	} else {
		var binY = d3.scale.linear().domain([ 0, 100 ]).rangeRound([ 0, 30 ]);
		var binsizeY = d3.scale.linear().domain([ 0, 100 ])
			.rangeRound([ 0, 100 ]);
		dataset = [ {
			'name' : '',
			'cid' : 0,
			'options' : {
				"id" : "",
				"kind" : "split_node"
			},
			edit : 0,
			highlight : 0,
			children : []
		} ];
	}
	var SVG;
	if (dataset) {
		SVG = Cure.PlayerSvg;
		var nodes = Cure.cluster.nodes(dataset), links = Cure.cluster.links(nodes);
		var maxDepth = 0;
		nodes.forEach(function(d) {
			d.y = d.depth * 80;
			if (d.y > maxDepth) {
				maxDepth = d.y;
			}
			if (!d.options) {
				d.options = [];
			}
		});
		d3.select("#PlayerTreeRegionSVG").attr("height", maxDepth + 300);
		var marginTop = maxDepth-160;
		if(marginTop<0){
			marginTop = 0;
		}
		$("#footer").css("margin-top",marginTop);
		
		//Drawing Edges
		var node = Cure.PlayerNodeCollection.models[0];
		Cure.PlayerSvg.selectAll(".link").remove();
		edgeCount = 0;
		translateLeft = 0;
		if(node){
			Cure.drawEdges(node,binY,0);
		}

		var node = SVG.selectAll(".MetaDataNode").data(nodes);

		var nodeEnter = node.enter().append("svg:g").attr("class", "MetaDataNode")
				.attr(
				"transform", function(d) {
					return "translate(" + d.x + "," + d.y + ")";
				}).on("click",function(d){
					var content = "<h4>Node Details</h4><ul>";
					if(d.options.pct_correct){
						content+="<li><span class='text-info'><b>Accuracy</b>[<a>?</a>]</a></span>: "+d.options.pct_correct+"</li>";
					} else if(d.options.infogain){
						content+="<li><span class='text-info'><b>Info Gain</b>[<a>?</a>]</a></span>: "+d.options.infogain+"</li>";
					}
					if(d.options.bin_size){
						content+="<li><span class='text-info'><b>Bin Size</b>[<a>?</a>]</a></span>: "+d.options.bin_size+"</li>";
						content+="<li><span>"+Math.round((d.options.bin_size/dataset.options.bin_size)*100)+"% of cases from dataset fall here.</span></li>"
					}
					Cure.showDetailsOfNode(content, d.y, d.x+70);
				}).on("mouseenter",function(){
					d3.select(this).classed("selected",true);
					d3.selectAll(".selected .nodehinttext").style("display","block");
				}).on("mouseleave",function(){
					d3.selectAll(".selected .nodehinttext").style("display","none");
					d3.select(this).classed("selected",false);
				});
		
		var arc = d3.svg.arc()
	    			.innerRadius(0)
	    			.outerRadius(30)
	    			.startAngle(0 * (Math.PI/180))
	    			.endAngle(2 * (Math.PI/180));
		
		nodeEnter.append(arc);
		
		nodeEnter.append("rect").attr("class", "scaleIndicator").attr("transform","translate(-50,-41)").attr("width",function(d){
			if(d.options.kind!="split_value"){
				return "100";
			}
		}).attr("height", 10).style("fill", function(d) {
			return "white";
		}).attr("stroke","#000").attr("stroke-width","1");
		
		nodeEnter.append("rect").attr("class", "nodeaccuracy").attr("transform","translate(-50,-40)").attr("width", 0).attr("height", 8).style("fill", function(d) {
			if (d.options.pct_correct) {
				return "orange";
			} else if (d.options.infogain) {
				return "green";
			}
		});
		
		//Accuracy Text
		
		nodeEnter.append("svg:text").attr("class", "nodeaccuracytext nodehinttext").attr(
				"transform", "translate(-50, -49)").style("font-size", "10")
				.style("fill", function(d) {
					return "black";
				}).style("display","none").text(function(d) {
					var text = "";
					if (d.options.pct_correct) {
						text = "Accuracy";
					} else if (d.options.infogain) {
						text = "Info Gain";
					}
					return text;
				});
		
		// Bin Size
		nodeEnter.append("rect").attr("class", "scaleIndicator binScaleIndicator").attr("transform",function(d){
			if(d.options.kind == "leaf_node"){
				return "translate(-50,16)";
			} else{
				return "translate(-50,2)";
			}
		}).attr("width",function(d){
			if(d.options.kind!="split_value"){
				return "100";
			}
		}).attr("height", 10).style("fill", function(d) {
			return "white";
		}).attr("stroke","#000").attr("stroke-width","1");
		
		nodeEnter.append("rect").attr("class", "binsize").attr("transform",function(d){
			if(d.options.kind == "leaf_node"){
				return "translate(-50,17)";
			} else{
				return "translate(-50,3)";
			}
		}).attr("width", 0).attr(
				"height", 8).style("fill", function(d) {
			return "blue";
		});

		//Bin Size Text 
		nodeEnter.append("svg:text").attr("class", "binsizetext nodehinttext").attr("transform",function(d){
				if(d.options.kind == "leaf_node"){
					return "translate(-50,36)";
				} else{
					return "translate(-50, 22)";
				}
			}).style("font-size", "10").style("fill",
				function(d) {
					return "black";
				}).style("display","none").text(function(d) {
			var text = "";
			if (d.options.bin_size) {
				text = "Bin size";
			}
			return text;
		});

		
		var nodeUpdate = node.transition().duration(Cure.duration).attr(
				"transform", function(d) {
					return "translate(" + d.x + "," + d.y + ")";
				});
		
		//Update Accuracy Nodes
		
		nodeUpdate.select(".nodeaccuracy").transition().duration(Cure.duration).attr("width", function(d) {
			var height = 0;
			if (d.options.pct_correct) {
				height = scaleY(d.options.pct_correct);
			} else if (d.options.infogain) {
				height = infoY(d.options.infogain);
			}
			return height;
		}).style("fill", function(d) {
			if (d.options.pct_correct) {
				return "orange";
			} else if (d.options.infogain) {
				return "green";
			}
		});
		
		
		//Update Accuracy Text
		nodeUpdate.select(".nodeaccuracytext").text(function(d) {
			var text = "";
			if (d.options.pct_correct) {
				text = "Accuracy";
			} else if (d.options.infogain) {
				text = "Info Gain";
			}
			return text;
		});
		
		
		// Bin Size
		nodeUpdate.select(".binScaleIndicator").attr("transform",function(d){
			if(d.options.kind == "leaf_node"){
				return "translate(-50,16)";
			} else{
				return "translate(-50,2)";
			}
		});
		
		nodeUpdate.select(".binsize").attr("transform",function(d){
			if(d.options.kind == "leaf_node"){
				return "translate(-50,17)";
			} else{
				return "translate(-50,3)";
			}
		}).transition().duration(Cure.duration).attr("width", function(d) {
			var height = 0;
			if (d.options.bin_size) {
				height = binsizeY(d.options.bin_size);
			}
			return height;
		});
		
		//Update Bin Size Text
		nodeUpdate.select(".binsizetext").attr("transform",function(d){
			if(d.options.kind == "leaf_node"){
				return "translate(-50,36)";
			} else{
				return "translate(-50, 22)";
			}
		});

		var nodeExit = node.exit().transition().duration(Cure.duration).attr(
				"transform", function(d) {
					return "translate(" + dataset.x + "," + dataset.y + ")";
				}).remove();
		
		nodes.forEach(function(d) {
			d.x0 = d.x;
			d.y0 = d.y;
		});
	}
}

Cure.showAlert = function(message){
	$("#alertMsg").html(message);
	$("#alertWrapper").fadeIn();
	window.setTimeout(function(){
		$("#alertWrapper").hide();
	},2000);
}

Cure.showDetailsOfNode = function(content, top, left){
	top = top + 40;
	left = left + (window.innerWidth-1170)/2;
	$("#NodeDetailsWrapper").css({
		"top": top,
		"left": left,
		"display": "block"
	});
	$("#NodeDetailsContent").html('<span><button type="button" class="close">×</button></span>'+content);
}
var translateLeft=0;
var edgeCount=0;
Cure.drawEdges = function(node,binY,count){
	if(node.get('children').models.length == 0){
		translateLeft += binY(node.get('options').bin_size);
		edgeCount++;
		var links = [];
		var tempNode = node;
		var bin_size = binY(node.get('options').bin_size);
		while(tempNode.get('parentNode')!=null){
			links.push({"source":tempNode.get('parentNode').toJSON(),"target":tempNode.toJSON()});
			tempNode = tempNode.get('parentNode');
		}
		for(var temp in links){
			Cure.PlayerSvg.append("path").attr("class", "link").attr("d",function(){
				if(links[temp].source.options.kind!="split_value"){
					links[temp].source.y+=14;
				}
				if(links[temp].target.options.kind!="split_value"){
					links[temp].target.y=links[temp].target.y-42;
				}
				return Cure.diagonal({
					source : links[temp].source,
					target : links[temp].target
				});
		}).style("stroke-width", function(){
			var edgeWidth = binY(node.get('options').bin_size);
			if(edgeWidth<1){
				edgeWidth = 1;
			} 
			return edgeWidth;
		}).style("stroke",/*Cure.edgeColor(edgeCount)*/"lightgrey");
		}
	}
	var i = 0;
	for(i = node.get('children').models.length;i>0;i--){
		Cure.drawEdges(node.get('children').models[i-1],binY,count);
	}
}

//
// -- App init!
//    
Cure.addInitializer(function(options) {
	//JSP Uses <% %> to render elements and this clashes with default underscore templates.
	_.templateSettings = {
		interpolate : /\<\@\=(.+?)\@\>/gim,
		evaluate : /\<\@([\s\S]+?)\@\>/gim,
		escape : /\<\@\-(.+?)\@\>/gim
	};
	Backbone.emulateHTTP = true;
	$(options.regions.PlayerTreeRegion).html(
			"<div id='" + options.regions.PlayerTreeRegion.replace("#", "")
					+ "Tree'></div><svg id='"
					+ options.regions.PlayerTreeRegion.replace("#", "") + "SVG'></svg>")
	$("#save_tree").on("click",function(){
		var tree;
		if(Cure.PlayerNodeCollection.models[0])
		{
			tree = Cure.PlayerNodeCollection.models[0].toJSON();
			var args = {
					command : "savetree",
					dataset : "griffith_breast_cancer_1",
					treestruct : tree,
					player_id : cure_user_id,
					comment: Cure.COmment.get("content")
			};
			$.ajax({
				type : 'POST',
				url : '/cure/MetaServer',
				data : JSON.stringify(args),
				dataType : 'json',
				contentType : "application/json; charset=utf-8",
				success : Cure.showAlert("saved"),
				error : Cure.showAlert("Error Occured. Please try again in a while.")
			});
		}
		else
		{
			tree = [];
			Cure.showAlert("Empty Tree!<br>Please build a tree by using the auto complete box.");
		}
	});
	Cure.addRegions({
		PlayerTreeRegion : options.regions.PlayerTreeRegion + "Tree",
		ScoreRegion : options.regions.ScoreRegion,
		CommentRegion : options.regions.CommentRegion,
		JsonRegion : "#json_structure"
	});
	Cure.colorScale = d3.scale.category10();
	Cure.edgeColor = d3.scale.category20();
	Cure.width = options["width"];
	Cure.height = options["height"];
	Cure.duration = 500;
	Cure.cluster = d3.layout.tree().size([ Cure.width, "auto" ]);
	Cure.diagonal = d3.svg.diagonal().projection(function(d) {
		return [ d.x, d.y ];
	});
	Cure.PlayerSvg = d3.select(options.regions.PlayerTreeRegion + "SVG").attr(
			"width", Cure.width).append("svg:g")
			.attr("transform", "translate(0,100)");
	Cure.PlayerNodeCollection = new NodeCollection();
	Cure.Comment = new Comment();
	Cure.Score = new Score();
	Cure.ScoreView = new ScoreView({
		"model" : Cure.Score
	});
	Cure.CommentView = new CommentView({model:Cure.Comment});
	Cure.PlayerNodeCollectionView = new NodeCollectionView({
		collection : Cure.PlayerNodeCollection
	});

	Cure.JSONCollectionView = new JSONCollectionView({
		collection : Cure.PlayerNodeCollection
	});
	Cure.PlayerTreeRegion.show(Cure.PlayerNodeCollectionView);
	Cure.ScoreRegion.show(Cure.ScoreView);
	Cure.JsonRegion.show(Cure.JSONCollectionView);
	Cure.CommentRegion.show(Cure.CommentView);
});

//App Start
Cure.start({
	"height" : 600,
	"width" : 850,
	"regions" : {
		"PlayerTreeRegion" : "#PlayerTreeRegion",
		"ScoreRegion" : "#ScoreRegion",
		"CommentRegion" : "#CommentRegion"
	}
});
