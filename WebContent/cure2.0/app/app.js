//NOT IS USE. OLD FILE.


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
			dataset : "metabric_with_clinical",
			treestruct : tree,
			comment: Cure.Comment.get("content")
		};
		//POST request to server.		
		$.ajax({
			type : 'POST',
			url : this.url,
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
				if(json_node.children.length>0){
					temp++;
				}
				for ( var i = temp; i < node.get('children').length; i++) {
					Cure.delete_all_children(node.get('children').models[i]);
					node.get('children').models[i].destroy();
					i--;
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
		Cure.updatepositions(Cure.PlayerNodeCollection);
		Cure.render_network(Cure.PlayerNodeCollection.toJSON()[0]);
	},
	parseResponse : function(data) {
		//If empty tree is returned, no tree rendered.
		if (data["treestruct"].name) {
			Cure.PlayerNodeCollection.updateCollection(data["treestruct"], Cure.PlayerNodeCollection.models[0], null);
		} else {
		//If server returns json with tree render and update positions of nodes.
			Cure.updatepositions(Cure.PlayerNodeCollection);
			Cure.render_network(Cure.PlayerNodeCollection.toJSON()[0]);
		}
		
		//Storing Score in a Score Model.
		var scoreArray = data;
		scoreArray.treestruct = null;
		if(scoreArray.novelty == "Infinity"){
			scoreArray.novelty = 0;
		}
		Cure.Score.set("previousAttributes",Cure.Score.toJSON());
		Cure.Score.set(scoreArray);
		
		//Cure.Comment.set("content",data["comment"]); TODO: Include comment in json_tree on server side.
	},
	error : function(data) {
		console.log("Error Receiving Data From Server.");
	}
});

//
// -- Defining our models
//
ClinicalFeature = Backbone.RelationalModel.extend({
	defaults : {
		description : "",
		id : 0,
		long_name : "",
		short_name : "",
		unique_id : 0
	},
	initialize: function(){
		this.bind('change:long_name', this.updateLabel);
		this.updateLabel();
	},
	updateLabel: function(){
		var label = this.get('long_name')
		this.set("label",label);
	}
});

ClinicalFeatureCollection = Backbone.Collection.extend({
	model: ClinicalFeature,
	url: '/cure/MetaServer',
	initialize: function(){
		_.bindAll(this, 'parseResponse');
	},
	fetch: function(){
		var args = {
				command : "get_clinical_features",
				dataset : "metabric_with_clinical"
		};
		$.ajax({
			type : 'POST',
			url : this.url,
			data : JSON.stringify(args),
			dataType : 'json',
			contentType : "application/json; charset=utf-8",
			success : this.parseResponse,
			error : this.error,
			async: true
		});
	},
	parseResponse : function(data) {
		if(data.features.length > 0) {
			this.add(data.features);
		}
	},
	error : function(data) {

	}
});

Score = Backbone.RelationalModel.extend({
	defaults : {
		novelty : 0,
		pct_correct : 0,
		size : 0,// Least Size got form server = 1.
		score : 0,
		scoreDiff : 0,
		sizeDiff : 0,
		pct_correctDiff : 0,
		noveltyDiff : 0,
		previousAttributes: {}
	},
	initialize: function(){
		this.bind('change:size', this.updateScore);
	},
	updateScore: function(){
		if (this.get("size") > 1) {
			var oldScore = this.get('score');
			var score = 750 * (1 / this.get("size")) + 500
					* this.get("novelty") + 1000 * this.get("pct_correct");
			this.set("score", Math.round(score));
			this.set("sizeDiff",parseFloat(this.get('size')-this.get('previousAttributes').size));
			this.set("pct_correctDiff",parseFloat(this.get('pct_correct')-this.get('previousAttributes').pct_correct));
			this.set("noveltyDiff",parseFloat(this.get('novelty')-this.get('previousAttributes').novelty));
			this.set("scoreDiff",parseFloat(Math.round(score)-oldScore));
		} else {
			this.set({
				"score" : 0,
				"size" : 0,
				"pct_correct" : 0,
				"novelty" : 0,
				"scoreDiff": 0,
				"sizeDiff": 0,
				"pct_correctDiff": 0,
				"noveltyDiff": 0,
			});
		}
	}
});

Comment = Backbone.RelationalModel.extend({
	defaults: {
		content: "",
		editView: 0
	}
});

ScoreEntry = Backbone.RelationalModel.extend({
	defaults: {
		comment: "",
		created: 0,
		id: 0,
		ip: "",
		json_tree :{
			novelty : 0,
			pct_correct : 0,
			size : 1,// Least Size got form server = 1.
			score : 0,
			text_tree : '',
			treestruct : {}
		}
	},
	initialize: function(){
		this.bind('change', this.updateScore);
		this.updateScore();
	},
	updateScore: function(){
		var scoreVar = this.get('json_tree');
		if(scoreVar.size>=1) {
			scoreVar.score = Math.round(750 * (1 / scoreVar.size) + 
					500 * scoreVar.novelty + 
					1000 * scoreVar.pct_correct);
		} else {
			scoreVar.score = 0;
		}
		this.set("json_tree", scoreVar);
	}
});

//Score Entry Collection
ScoreBoard = Backbone.Collection.extend({
	model: ScoreEntry,
	initialize : function(){
		_.bindAll(this, 'parseResponse');
	},
	upperLimit: 10,
	lowerLimit: 0,
	url : '/cure/MetaServer',
	fetch: function(){
		var args = {
				command : "get_trees_with_range",
				lowerLimit : this.lowerLimit,
				upperLimit : this.upperLimit,
		};
		this.lowerLimit+=10;
		this.upperLimit+=10;
		$.ajax({
			type : 'POST',
			url : this.url,
			data : JSON.stringify(args),
			dataType : 'json',
			contentType : "application/json; charset=utf-8",
			success : this.parseResponse,
			error : this.error,
			async: true
		});
	},
	parseResponse : function(data) {
		//If empty tree is returned, no tree rendered.
		if(data.n_trees > 0) {
			this.add(data.trees);
		}
		Cure.ScoreBoardRequestSent = false;
	},
	error : function(data) {

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
			includeInJSON: false
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
	className : 'node dragHtmlGroup',
	ui : {
		input : ".edit",
		addgeneinfo : ".addgeneinfo",
		name : ".name",
		chart : ".chart"
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
			cid : serialized_model.cid,
			posNodeName : Cure.posNodeName,
			negNodeName : Cure.negNodeName
		}, {
			variable : 'args'
		});
	},
	events : {
		'click button.addchildren' : 'addChildren',
		'click button.delete' : 'removeChildren',
		'click .name' : 'showSummary',
		'blur .name': 'setHighlight',
		'click .chart': 'showNodeDetails'
	},
	initialize : function() {
		if(this.model.get('options').kind != "split_value") {
				this.model.set('accLimit',0);																																																																																																																																																																												
		}
		if(Cure.PlayerNodeCollection.models.length > 0) {
			Cure.binScale = d3.scale.linear().domain([ 0, Cure.PlayerNodeCollection.models[0].get('options').bin_size ]).range([ 0, 100 ]);
		} else {
			Cure.binScale = d3.scale.linear().domain([ 0, 239 ]).range([ 0, 100 ]);
		}
		_.bindAll(this, 'remove', 'addChildren', 'showSummary', 'setaccLimit');
		this.model.bind('change', this.render);
		this.model.bind('add:children', this.setaccLimit);
		this.model.bind('remove', this.remove);
	},
	setaccLimit : function(children){
		if(children.get('options').kind=="leaf_node") {
			var accLimit = 0;
			if(children.get('name')==Cure.negNodeName) {
				accLimit += Cure.binScale(children.get('options').bin_size)*(1-children.get('options').pct_correct);
			} else if(children.get('name') == Cure.posNodeName) {
				accLimit += Cure.binScale(children.get('options').bin_size)*(children.get('options').pct_correct);
			}
			accLimit += this.model.get('parentNode').get('accLimit');
			this.model.get('parentNode').set('accLimit',accLimit);
		}
		this.render;
	},
	showNodeDetails: function(){
		var datasetBinSize = Cure.PlayerNodeCollection.models[0].get('options').bin_size;
		var content = "";
		//% cases
		if(this.model.get('options').bin_size && this.model.get('options').kind =="leaf_node"){
			content+="<p class='binsizeNodeDetail'><span class='percentDetails'>"+Math.round((this.model.get('options').bin_size/datasetBinSize)*10000)/100+"%</span><span class='textDetail'>of cases from the dataset fall here.</span></p>";
		} else if(this.model.get('options').bin_size && this.model.get('options').kind =="split_node") {
			content+="<p class='binsizeNodeDetail'><span class='percentDetails'>"+Math.round((this.model.get('options').bin_size/datasetBinSize)*10000)/100+"%</span><span class='textDetail'>of cases from the dataset pass through this node.</span></p>";
		}		
		//Accuracy
		if(this.model.get('options').pct_correct){
			content+="<p class='accuracyNodeDetail'><span class='percentDetails'>"+Math.round(this.model.get('options').pct_correct*10000)/100+"%</span><span class='textDetail'>is the percentage accuracy at this node.</span></p>";
		}
		Cure.showDetailsOfNode(content, this.$el.offset().top, this.$el.offset().left);
	},
	setHighlight: function(){
		this.model.set('highlight',0);
	},
	showSummary : function() {
		//showJSON is used to render the Gene Info POP Up.
		if (this.model.get("options").kind == "split_node" || this.model.get("options").kind == "split_value") {
			this.model.set("showJSON", 1);
		}
	},
	onBeforeRender : function() {
		//Render the positions of each node as obtained from d3.
		var numNodes = Cure.getNumNodesatDepth(Cure.PlayerNodeCollection.models[0], Cure.getDepth(this.model));
		
		if(numNodes * 100 >= Cure.width){//TODO: find way to get width of node dynamically.
			$(this.el).addClass('shrink_'+this.model.get('options').kind);
			$(this.el).css({
				width: (Cure.width - 10*numNodes) /numNodes,//TODO: account for border-width and padding programmatically.	
				height: 'auto',
				'font-size': '0.5vw',
				'min-width': '0px'
			});
		} else {
			if($(this.el).hasClass('shrink_'+this.model.get('options').kind)){
				$(this.el).removeClass('shrink_'+this.model.get('options').kind);
			}
			if(this.model.get('options').kind=='leaf_node'||this.model.get('options').kind=='split_node'){
				try{
					$(this.el).css({
						'width': this.model.get('parentNode').get('parentNode').get('viewCSS').width+"px",
						'min-width': '0px'
					});
				} catch(e){
						$(this.el).css({
							'min-width': "100px"
						});
					}
				} else {
					if(this.model.get('parentNode')!=null){
						$(this.el).css({
							'width': parseFloat(this.model.get('parentNode').get('viewCSS').width)+"px",
							'min-width': '0px'
						});
					}
				}
			}
		var width = $(this.el).outerWidth();
		var nodeTop = (this.model.get('y')+71);
		var styleObject = {
			"left": (this.model.get('x') - ((width) / 2)) +"px",
			"top": nodeTop +"px",
			"background": "#FFF",
			"border-color": "#000"
		};
		if(this.model.get('name')==Cure.negNodeName) {
			styleObject.background = "rgba(255,0,0,0.2)";
			styleObject.borderColor = "red";
		} else if(this.model.get('name')==Cure.posNodeName) {
			styleObject.background = "rgba(0,0,255,0.2)";
			styleObject.borderColor = "blue";
		}
		$(this.el).attr('class','node dragHtmlGroup');//To refresh class everytime node is rendered.
		$(this.el).css(styleObject);
		$(this.el).addClass(this.model.get("options").kind);
		this.model.set("viewCSS",{'width':this.$el.width()});
	}, 
	onRender: function(){
		var id = this.$el.find(".chart").attr('id');
		var accLimit = 0;
		//Setting up accLimit for leaf_node
		if(this.model.get('options').kind=="leaf_node") {
			accLimit = Cure.binScale(this.model.get('options').bin_size)*(this.model.get('options').pct_correct);
			this.model.set('accLimit',accLimit);
		}
		if(id!=undefined){
			id = "#"+id;
			var radius = 4;
			var width = this.model.get('viewCSS').width-10;
			radius = (width - 4)/20;
			var limit = Cure.binScale(this.model.get('options').bin_size);
			Cure.drawChart(d3.select(id), limit, this.model.get('accLimit'), radius, this.model.get('options').kind, this.model.get('name'));
			var classToChoose = [{"className":""},{"color":""}];
			if(this.model.get('name') == Cure.negNodeName){
				classToChoose["className"]= " .posCircle";
				classToChoose["color"]= "red";
			} else{
				classToChoose["className"]= " .posCircle";
				classToChoose["color"]= "blue";
			}
			d3.selectAll(id+classToChoose["className"]).style("fill",classToChoose["color"]);
		}		
		/*
		else if($(this.el).hasClass("shrink_leaf_node") && id!= undefined){
			var bin_size = this.model.get('options').bin_size;
			var accLimit = this.model.get('accLimit'); 
			console.log(accLimit/100)
			var hue = 0;
			if(this.model.get('name')==Cure.posNodeName){//Deviate starting from blue. H = 240 degrees
				hue = 1.2/360 - ((100-accLimit) * 1.2/360);
			} else if(this.model.get('name')==Cure.negNodeName) {//Deviate starting from red. H = 360 degrees
				hue = 1.2/360 - ((accLimit) * 1.2/360);	
			}
			var rgb = Cure.hslToRgb(hue,1,0.5);
			id = "#"+id;
			d3.select(id).style('background',function(){
				return 'rgb('+rgb[0]+','+rgb[1]+','+rgb[2]+')';
			});
		}*/
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
				this.model.set("previousAttributes", []);
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

var scoreDetailsTemplate = $("#scoreDetailsTemplate").html();
//View to reflect current score and radar chart.
ScoreView = Backbone.Marionette.ItemView.extend({
	initialize : function() {
		_.bindAll(this, 'updateScore');
		this.model.bind("change:scoreDiff", this.updateScore);
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
	template : "#ScoreTemplate",
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
		$(this.ui.scoreDetails).html(_.template(scoreDetailsTemplate,this.model.toJSON(),{variable: 'args'}));
			$(this.ui.scoreDetails).show();
			var args = this.model.toJSON();
			var time = 1000/Math.abs(args.scoreDiff);
				var currentVal = args.score + (-1 * args.scoreDiff);
				var endVal = args.score;
				var increment = args.scoreDiff / Math.abs(args.scoreDiff) * 10;
				if(args.scoreDiff != args.score){
					var counter = window.setInterval(function() {
						if (currentVal > endVal) {
							$("#score").html(endVal);
							window.clearInterval(counter);
						} else {
							currentVal = currentVal + increment;
							$("#score").html(currentVal);
						}
					}, time);
				}
			d3.select("#sizeBarChart").transition().duration(Cure.duration).style('width',Cure.sizeScale(1/this.model.get('size'))+'px');
			d3.select("#accuracyBarChart").transition().duration(Cure.duration).style('width',Cure.accuracyScale(this.model.get('pct_correct'))+'px');
			d3.select("#noveltyBarChart").transition().duration(Cure.duration).style('width',Cure.noveltyScale(this.model.get('novelty'))+'px');
			window.setTimeout(function(){$("#score-panel").removeClass('score-panel-extend');},8000);
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
	}
});

var geneinfosummary = $("#GeneInfoSummary").html();
var cfsummary = $("#ClinicalFeatureSummary").html();
AddRootNodeView = Backbone.Marionette.ItemView.extend({
	initialize : function() {
	},
	ui : {
		'input' : '.mygene_query_target',
		'cfWrapper': '#mygenecf_wrapper'
	},
	events:{
		'click .showCf': 'showCf',
		'click .hideCf': 'hideCf'
	},
	showCf: function(){
		$("#mygeneinfo_wrapper").hide();
		if (this.model) {
			var model = this.model;
		}
		
		//Clinical Features Autocomplete
		var availableTags = Cure.ClinicalFeatureCollection.toJSON();
		
		this.$el.find('#cf_query').autocomplete({
			source : availableTags,
			minLength: 0,
			open: function(event){
				var scrollTop = $(event.target).offset().top-400;
				$("html, body").animate({scrollTop:scrollTop}, '500');
			},
			close: function(){
				$(this).val("");
			},
			minLength: 0,
			focus: function( event, ui ) {
				focueElement = $(event.currentTarget);//Adding PopUp to .ui-auocomplete
				if($("#SpeechBubble")){
					$("#SpeechBubble").remove();
				}
				focueElement.append("<div id='SpeechBubble'></div>")
					var html = _.template(cfsummary, {
						long_name : ui.item.long_name,
						description : ui.item.description
					}, {
						variable : 'args'
					});
					var dropdown = $("#cf_query").data('ui-autocomplete').bindings[1];
					var offset = $(dropdown).offset();
					var uiwidth = $(dropdown).width();
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
			},
			search: function( event, ui ) {
				$("#SpeechBubble").remove();
			},
			select : function(event, ui) {
				if(ui.item.long_name != undefined){//To ensure "no gene name has been selected" is not accepted.
					$("#SpeechBubble").remove();
					var kind_value = "";
					try {
						kind_value = model.get("options").kind;
					} catch (exception) {
					}
					if (kind_value == "leaf_node") {
						model.set("previousAttributes", model.toJSON());
						model.set("name", ui.item.short_name.replace(/_/g," "));
						model.set('accLimit', 0, {silent:true});
						if(Cure.isJSON(ui.item.description)){
							model.set("options", {
								id : ui.item.unique_id,
								"unique_id" : ui.item.unique_id,
								"kind" : "split_node",
								"full_name" : ui.item.long_name,
								"description" : ui.item.description
							});
						} else {
							model.set("options", {
								id : ui.item.unique_id,
								"unique_id" : ui.item.unique_id,
								"kind" : "split_node",
								"full_name" : ui.item.long_name,
								"description" : ui.item.description
							});
						}
					} else {
						var newNode = new Node({
							'name' : ui.item.short_name.replace(/_/g," "),
							"options" : {
								id : ui.item.unique_id,
								"unique_id" : ui.item.unique_id,
								"kind" : "split_node",
								"full_name" : ui.item.long_name,
								"description" : ui.item.description
							}
						});
						newNode.set("cid", newNode.cid);
					}
					if (Cure.MyGeneInfoRegion) {
						Cure.MyGeneInfoRegion.close();
					}
					Cure.PlayerNodeCollection.sync();
				}
			},
		}).data("ui-autocomplete")._renderItem = function (ul, item) {
		    return $("<li></li>")
	        .data("item.autocomplete", item)
	        .append("<a>" + item.label + "</a>")
	        .appendTo(ul);
	    };
	    
	    this.$el.find('#cf_query').focus(function(){
	    	$(this).autocomplete("search", "");
	    });	
		$("#mygenecf_wrapper").show();
	},
	hideCf: function(){
		$("#mygenecf_wrapper").hide();
		$("#mygeneinfo_wrapper").show();
	},
	template : "#AddRootNode",
	render : function() {
		if (this.model) {
			var model = this.model;
		}
		var html_template = $("#AddRootNode").html();
		this.$el.html(html_template);
		
		this.$el.find('#gene_query').genequery_autocomplete({
			open: function(event){
				var scrollTop = $(event.target).offset().top-400;
				$("html, body").animate({scrollTop:scrollTop}, '500');
			},
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
					var dropdown = $("#gene_query").data('my-genequery_autocomplete').bindings[0];
					var offset = $(dropdown).offset();
					var uiwidth = $(dropdown).width();
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
				if(ui.item.name != undefined){//To ensure "no gene name has been selected" is not accepted.
					$("#SpeechBubble").remove();
					var kind_value = "";
					try {
						kind_value = model.get("options").kind;
					} catch (exception) {
					}
					if (kind_value == "leaf_node") {
						model.set("previousAttributes", model.toJSON());
						model.set("name", ui.item.symbol);
						model.set('accLimit', 0, {silent:true});
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
					}
					if (Cure.MyGeneInfoRegion) {
						Cure.MyGeneInfoRegion.close();
					}
					Cure.PlayerNodeCollection.sync();
				}
			}
		});		
	}
});

var emptyLayout = Backbone.Marionette.Layout.extend({
    template: "#Empty-Layout-Template",
    regions: {
      AddRootNode: "#AddRootNodeWrapper"
    },
    onBeforeRender: function(){
    	//Cure.ToggleHelp(false);
    },
    onRender: function(){
    	if(!Cure.helpText){
        	Cure.helpText = $("#HelpText").html();	
        	Cure.ToggleHelp(true);
    	}
    	var newAddRootNodeView = new AddRootNodeView(); 
    	this.AddRootNode.show(newAddRootNodeView);
    },
    onBeforeClose: function(){
    }
});

NodeCollectionView = Backbone.Marionette.CollectionView.extend({
	// -- View to manipulate and display list of all nodes in collection
	itemView : NodeView,
	emptyView : emptyLayout,
	initialize : function() {

	}
});

//HTML Templates for rendering Gene SUmary Pop Up and Node Information. 
var shownode_html = $("#JSONtemplate").html();
var showsplitvaluenode_html = $("#JSONSplitValuetemplate").html();
var showsplitnodecf_html = $("#JSONSplitNodeCftemplate").html();
// -- View to render Gene SUmmary List
JSONItemView = Backbone.Marionette.ItemView.extend({
	model : Node,
	ui : {
		jsondata : ".jsonview_data",
		showjson : ".showjson"
	},
	events : {
		'click .showjson' : 'ShowJSON',
		'click button.close' : 'HideJSON'
	},
	tagName : "tr",
	initialize : function() {
		_.bindAll(this, 'getSummary', 'ShowJSON', 'HideJSON');
		this.model.bind('change', this.render);
		this.model.on('change:showJSON', function() {
			if (this.model.get('showJSON') != 0) {
				this.ShowJSON();
			}
		}, this);
		var thisView = this;
		$(document).mouseup(function(e){
			var classToclose = $(".jsonview_data");
			if (!classToclose.is(e.target)	&& classToclose.has(e.target).length == 0) 
			{
				thisView.HideJSON();
			}
		});
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
		if(serialized_model.options.kind == "split_node" && serialized_model.options.id.indexOf("metabric") == -1) {
			return _.template(shownode_html, {
				name : name,
				summary : serialized_model.gene_summary,
				kind : serialized_model.options.kind
			}, {
				variable : 'args'
			});
		} else if (serialized_model.options.kind == "split_node" && serialized_model.options.id.indexOf("metabric") != -1){
			return _.template(showsplitnodecf_html, {
				name : name,
				summary : serialized_model.gene_summary,
				kind : serialized_model.options.kind
			}, {
				variable : 'args'
			});
		} else {
			return _.template(showsplitvaluenode_html, {
				name : name,
				summary : serialized_model.gene_summary,
				kind : serialized_model.options.kind
			}, {
				variable : 'args'
			});
		}
	},
	ShowJSON : function() {
		if(this.model.get('options').kind == "split_node"){
			if(this.model.get('options').id.indexOf("metabric") == -1){
				this.getSummary();
			} else {
				var summary = {};
				summary.name = this.model.get('name');
				summary.summaryText = this.model.get('options').description;
				this.model.set("gene_summary",summary);
			}
		} else {
			if(Cure.isJSON(this.model.get('parentNode').get('options').description)){
				var json_string = JSON.parse(this.model.get('parentNode').get('options').description);
				var summary = {};
				summary.name = this.model.get('name');
				summary.summaryText = json_string[this.model.get('name')];
				this.model.set("gene_summary",summary);
			}
		}
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
	}
});

//Collection View to render gene summary list.
JSONCollectionView = Backbone.Marionette.CollectionView.extend({
	itemView : JSONItemView,
	collection : NodeCollection,
	initialize : function() {
		this.collection.bind('add', this.render);
		this.collection.bind('remove', this.render);
	}
});

ScoreEntryView = Backbone.Marionette.ItemView.extend({
	model : ScoreEntry,
	tagName: 'tr',
	ui : {

	},
	initialize : function() {
		_.bindAll(this, 'loadNewTree');
		this.model.bind('change', this.render);
		this.$el.click(this.loadNewTree);
	},
	loadNewTree: function(){
		Cure.showLoading();
		var json_struct = JSON.stringify(this.model.get('json_tree'));//JSON.stringify to not pass model reference.
		Cure.PlayerNodeCollection.parseResponse(JSON.parse(json_struct));
		Cure.hideLoading();
	},
	template: "#ScoreBoardTemplate"
});

ScoreBoardView = Backbone.Marionette.CollectionView.extend({
	itemView : ScoreEntryView,
	tagName: 'table',
	collection : ScoreBoard,
	className : "ScoreBoardInnerWrapper table",
	initialize : function() {
		this.collection.bind('add', this.render);
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
	var depthDiff = 180;
	d3nodes.forEach(function(d) {
			d.y = 0;
			for(i=1;i<=d.depth;i++){
				if(i%2!=0){
					depthDiff = 200;
				} else {
					depthDiff = 100;
				}
				d.y += depthDiff;
			}
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
		for (var i=0;i < children.models.length;i++) {
			Cure.delete_all_children(children.models[i]);
			children.models[i].destroy();
			i--;
		}
	}
}

//
// -- Render d3 Network
//
Cure.render_network = function(dataset) {

	if (dataset) {
		var binY = d3.scale.linear().domain([ 0, dataset.options.bin_size ])
				.range([ 0, 30 ]);
		var binsizeY = d3.scale.linear().domain([ 0, dataset.options.bin_size ])
				.range([ 0, 100 ]);
	} else {
		var binY = d3.scale.linear().domain([ 0, 100 ]).range([ 0, 30 ]);
		var binsizeY = d3.scale.linear().domain([ 0, 100 ])
			.range([ 0, 100 ]);
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
		var depthDiff = 180;
		nodes.forEach(function(d) {
			d.y = 0;
			for(i=1;i<=d.depth;i++){
				if(i%2!=0){
					depthDiff = 200;
				} else {
					depthDiff = 100;
				}
				d.y += depthDiff;
			}
			if (d.y > maxDepth) {
				maxDepth = d.y;
			}
			if (!d.options) {
				d.options = [];
			}
		});
		d3.select("#PlayerTreeRegionSVG").attr("height", maxDepth + 300);
		
		//Drawing Edges
		var node = Cure.PlayerNodeCollection.models[0];
		edgeCount = 0;
		translateLeft = 0;
		allLinks = [];
		if(node){
			Cure.drawEdges(node,binY,0);
		}
		
		try{
			var count =	allLinks[0].linkNumber;
			var divLeft = binY(dataset.options.bin_size/2) - parseInt(binY(allLinks[0].bin_size/2)); //Edge is rendered from middle.
		} catch(e){
			var count = 0;
			var divLeft = binY(dataset.options.bin_size/2);
		}
				
		allLinks.sort(function(a,b){return parseInt(a.linkNumber-b.linkNumber);});
		
		for(var temp in allLinks){
			if(allLinks[temp].linkNumber != count) {
				divLeft = divLeft - parseInt(binY(allLinks[temp-1].bin_size/2)) - parseInt(binY(allLinks[temp].bin_size/2));//When adding second edge, essentaial to include complete width of previous edge.
				count = allLinks[temp].linkNumber;
			}
			if(allLinks[temp].target.options.kind == "split_value"){
				allLinks[temp].source.y += 107;//For Split Nodes 
			} else if(allLinks[temp].target.options.kind == "leaf_node") {
				allLinks[temp].target.y -= 34;//For Leaf Nodes
			}
			allLinks[temp].source.x += divLeft;
			allLinks[temp].target.x += divLeft;
		}
		
		var link = SVG.selectAll(".link").data(allLinks);
		
		var source = {};
		var target = {};
		var nodeOffsets = Cure.setOffsets();
		
		link.enter().append("path").attr("class", "link").style("stroke-width", "1").style("stroke",function(d){
			if(d.name==Cure.negNodeName){
				return "red";
			} else {
				return "blue";
			}
		}).attr("d",function(d){
			return Cure.diagonal({
				source : d.source,
				target : d.source
			});
		});
	
		link.transition().duration(Cure.duration).attr("d",function(d){
			return Cure.diagonal({
				source : d.source,
				target : d.target
			});
		}).transition().delay(Cure.duration).duration(Cure.duration).style("stroke-width", function(d){
			var edgeWidth = binY(d.bin_size);
			if(edgeWidth<1){
				edgeWidth = 1;
			} 
			return edgeWidth;
		}).style("stroke",function(d){
			if(d.name==Cure.negNodeName){
				return "red";
			} else {
				return "blue";
			}
		}).attr("transform","translate(-5,0)");
		
		link.exit().transition().duration(Cure.duration).attr("d",function(d){
			return Cure.diagonal({
				source : d.source,
				target : d.source
			});
		}).remove();
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
	$("#NodeDetailsWrapper").css({
		"top": top-100,
		"left": left+150,
		"display": "block"
	});
	$("#NodeDetailsContent").html('<span><button type="button" class="close">×</button></span>'+content);
}

Cure.ToggleHelp = function(check){
	if(!check){
    	$("#HelpText").css({"position":"relative","cursor": "auto","background": "#FFF","color": "#000","width":"50%","height":"300px","overflow":"auto"});
    	window.setTimeout(function(){
        	$("#HelpText").html(Cure.helpText);
		},500);
	} else {
		$("#HelpText").css({"position":"absolute","overflow":"hidden","background": "#F69","color": "#FFF","cursor": "pointer","width":"45px","height":"30px"});
		$("#HelpText").html("");
		window.setTimeout(function(){
			$("#HelpText").html("Help");
		},500);
	}
}

Cure.isInt = function(n) {
   return n % 1 === 0;
}

Cure.isJSON = function(str) {
    try {
        JSON.parse(str);
    } catch (e) {
        return false;
    }
    return true;
}

Cure.showLoading = function(){
	$("#loading-wrapper").show();
}

Cure.hideLoading = function(){
	$("#loading-wrapper").hide();
}

//Function to get number of nodes at a particular depth level
Cure.getNumNodesatDepth = function(root,givenDepth){
	var num = 0;
	if(Cure.getDepth(root)==givenDepth){
		num++;
	} else if(Cure.getDepth(root)<givenDepth){
		if(root.get('children').models.length>0){
			for(var temp in root.get('children').models){
				num+=Cure.getNumNodesatDepth(root.get('children').models[temp],givenDepth);
			}
		}
	}
	return num;
}

//Function to convert from HSL to RGB
//Reference -> http://stackoverflow.com/questions/2353211/hsl-to-rgb-color-conversion
Cure.hslToRgb = function hslToRgb(h, s, l){
    var r, g, b;

    if(s == 0){
        r = g = b = l; // achromatic
    }else{
        function hue2rgb(p, q, t){
            if(t < 0) t += 1;
            if(t > 1) t -= 1;
            if(t < 1/6) return p + (q - p) * 6 * t;
            if(t < 1/2) return q;
            if(t < 2/3) return p + (q - p) * (2/3 - t) * 6;
            return p;
        }

        var q = l < 0.5 ? l * (1 + s) : l + s - l * s;
        var p = 2 * l - q;
        r = hue2rgb(p, q, h + 1/3);
        g = hue2rgb(p, q, h);
        b = hue2rgb(p, q, h - 1/3);
    }

    return [Math.floor(r * 255), Math.floor(g * 255), Math.floor(b * 255)];
}


//Function to get depth of a node
Cure.getDepth = function(node){
	var  givenDepth= 0;
	while(node!=null){
		node = node.get('parentNode');
		givenDepth++;
	}
	return givenDepth;
}

Cure.drawChart = function(parentElement, limit, accLimit,radius, nodeKind, nodeName){
	var chartWrapper = parentElement.attr("width",function(){
		return (radius*20)+8;
	}).attr("height",function(){
		return (radius*20)+8;
	}).append("svg:g").attr("class","chartWrapper").attr("transform","translate(3,3)");
	chartWrapper.append("rect").attr("class","circleContainer "+nodeName).attr("height",function(){
		if(nodeKind!="split_value"){
			return (radius*20)+2;
		}
		return 0;
	}).attr("width",function(d){
		if(nodeKind!="split_value"){
			return (radius*20)+2;
		}
		return 0;
	}).attr("fill",function(){
		return "none";
	}).attr("transform","translate(-2,2)").attr("stroke",function(){
		if(nodeName == Cure.negNodeName){
			return "rgba(255, 0, 0, 1)";
		} else if(nodeName == Cure.posNodeName) {
			return "rgba(0, 0, 255, 1)";
		}
		return "#000";
	}).attr("stroke-width","1");
	
	for(i=0;i<(limit);i++){
		if(Cure.isInt(accLimit) || i<=(accLimit-1) || i > (accLimit)){
			chartWrapper.append("rect").attr("class",function(){
				if(i<accLimit)
					return "posCircle";
				return "negCircle";
			}).attr("height",(radius*2)-2).attr("width",(radius*2)-2).style("fill",function(){
				if(nodeName==Cure.negNodeName){
					return "blue";//Opposite Color
				}
				return "red";//Opposite Color
			}).attr("transform","translate("+(radius*2)*(i%10)+","+((radius*19)-(radius*2)*parseInt(i/10))+")");
		} else if(!Cure.isInt(accLimit) && i== parseInt((accLimit)/1)){//Final square to be printed
			chartWrapper.append("rect").attr("class",function(){
				return "posCircle";
			}).attr("height",(radius*2)-2).attr("width",function(){
				return ((radius*2)-2) * ((accLimit) % 1);
			}).style("fill",function(){
				if(nodeName==Cure.negNodeName){
					return "blue";//Opposite Color
				}
				return "red";//Opposite Color
			}).attr("transform","translate("+(radius*2)*(i%10)+","+((radius*19)-(radius*2)*parseInt(i/10))+")");
			
			chartWrapper.append("rect").attr("class",function(){
				return "negCircle";
			}).attr("height",(radius*2)-2).attr("width",function(){
				return  ((radius*2)-2) * (1- (accLimit % 1));
			}).style("fill",function(){
				if(nodeName==Cure.negNodeName){
					return "blue";//Opposite Color
				}
				return "red";//Opposite Color
			}).attr("transform","translate("+parseInt((radius*2)*(i%10) + ((radius*2)-2) * (accLimit % 1)) + ","+((radius*19)-(radius*2)*parseInt(i/10))+")");
		}
	}
}

//Variables for drawEdges.
var allLinks = [];
var leafNodeCount = 0;
Cure.drawEdges = function(node,binY,count){
	if(node.get('children').models.length == 0){
		leafNodeCount++;
		var links = [];
		var tempNode = node;
		var source =[];
		var target = [];
		while(tempNode.get('parentNode')!=null){
			source = tempNode.get('parentNode').toJSON();
			target = tempNode.toJSON();
			source.y = parseFloat(105 + source.y);
			target.y = parseFloat(105 + target.y);
			links.push({"source":source,"target":target,"bin_size":node.get('options').bin_size,"name":node.get('name'),"linkNumber":leafNodeCount,"divLeft":0});
			tempNode = tempNode.get('parentNode');
		}
		allLinks.push.apply(allLinks, links);
	}
	var i = 0;
	for(i = node.get('children').models.length;i>0;i--){
		Cure.drawEdges(node.get('children').models[i-1],binY,count);
	}
}

Cure.setOffsets = function(){
	var nodeOffsets = [];
	$('.node').each(function(){
		nodeOffsets.push($(this).offset());
	});
	return nodeOffsets;
}

var temp = 0;
Cure.shiftNodes = function(translateX,translateY,nodeOffsets){
	temp = 0;
	$('.node').each(function(){
		$(this).css({"transform":"translate("+parseFloat(translateX)+"px,"+parseFloat(translateY)+"px)"});
		temp++;
	});
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
	
	var args = {
			command : "get_trees_all",
/*			dataset : "metabric_with_clinical",
			treestruct : tree,
			player_id : cure_user_id,
			comment: Cure.Comment.get("content")*/
	};
	$(options.regions.PlayerTreeRegion).html(
			"<div id='" + options.regions.PlayerTreeRegion.replace("#", "")
					+ "Tree'></div><svg id='"
					+ options.regions.PlayerTreeRegion.replace("#", "") + "SVG'></svg>");
	Cure.width = options["width"];
	Cure.height = options["height"];
	Cure.posNodeName = options["posNodeName"];
	Cure.negNodeName = options["negNodeName"];
	
	//Scales
	Cure.accuracyScale = d3.scale.linear().domain([ 0, 100 ]).range([ 0, 100 ]);
	Cure.noveltyScale = d3.scale.linear().domain([ 0, 1 ]).range([ 0, 100 ]);
	Cure.sizeScale = d3.scale.linear().domain([ 0, 1 ]).range([ 0, 100 ]);
	
	function zoomed() 
	{
		if(Cure.PlayerNodeCollection.models.length > 0){
			 var t = d3.event.translate,
		      s = d3.event.scale,
		      height = Cure.height,
		      width = Cure.width;
			 if(Cure.PlayerSvg.attr('width')!=null && Cure.PlayerSvg.attr('height')!=null){
				  width = Cure.PlayerSvg.attr('width')*(8/9),
				  height = Cure.PlayerSvg.attr('height');
			 }
			 t[0] = Math.min(width/2 * (s), Math.max(width/2 * (-1 * s), t[0]));
		  	t[1] = Math.min(height/2 * (s), Math.max(height/2 * (-1 * s), t[1]));
		  	zoom.translate(t);
		  	Cure.PlayerSvg.attr("transform", "translate(" + t + ")scale(" + s + ")");
			var splitTranslate = String(t).match(/-?[0-9\.]+/g); 
			$("#PlayerTreeRegionTree").css({"transform": "translate(" + splitTranslate[0] + "px,"+splitTranslate[1]+"px)scale(" + s + ")"});
		}
	}
	
	var zoom = d3.behavior.zoom()
    .scaleExtent([-10, 10])
    .on("zoom", zoomed);
	
	$(options.regions.PlayerTreeRegion + "Tree").css({"width":Cure.width});
	Cure.PlayerSvg = d3.select(options.regions.PlayerTreeRegion + "SVG").attr(
			"width", Cure.width).attr("height", Cure.height).call(zoom).append("svg:g")
			.attr("transform", "translate(0,0)").attr("class","dragSvgGroup");
			
	//Event Initializers
					
	$("#HelpText").on("click",function(){
		Cure.ToggleHelp(false);
	});
	
	$("body").delegate("#closeHelp","click",function(){
		Cure.ToggleHelp(true);
	});
	
	$("body").delegate(".close","click",function(){
		if($(this).parent().attr("class")=="alert")
		{
			$(this).parent().hide();
		}
		else{
			$(this).parent().parent().parent().hide();
		}
	});
	
	$(document).mouseup(function(e) {
		var container = $(".addnode_wrapper");
		var geneList = $(".ui-autocomplete");

		if (!container.is(e.target)	&& container.has(e.target).length == 0 && !geneList.is(e.target)	&& geneList.has(e.target).length == 0) 
		{
			$("input.mygene_query_target").val("");
			if (Cure.MyGeneInfoRegion) {
				Cure.MyGeneInfoRegion.close();
			}
		}
		
		var classToclose = $('.blurCloseElement');
		if (!classToclose.is(e.target)	&& classToclose.has(e.target).length == 0) 
		{
			classToclose.hide();
		}
	});
	
	Cure.ScoreBoardRequestSent = false;
	$("#scoreboard_wrapper").scroll(function () { 
		   if ($("#scoreboard_wrapper").scrollTop() >= $("#scoreboard_wrapper .ScoreBoardInnerWrapper").height() - $("#scoreboard_wrapper").height()) {
		      if(!Cure.ScoreBoardRequestSent){
		    	  Cure.ScoreBoard.fetch();
		    	  Cure.ScoreBoardRequestSent = true;
		      } 
		   }
		});
	
	$(".togglePanel").on("click",function(){
		$(".panel-body").slideUp();
		var panelBody = $(this).parent().parent().find(".panel-body");
		panelBody.slideToggle();
	});
	
	$("#save_tree").on("click",function(){
		var tree;
		if(Cure.PlayerNodeCollection.models[0])
		{
			tree = Cure.PlayerNodeCollection.models[0].toJSON();
			var args = {
					command : "savetree",
					dataset : "metabric_with_clinical",
					treestruct : tree,
					player_id : cure_user_id,
					comment: Cure.Comment.get("content")
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
	
	options.regions.PlayerTreeRegion+="Tree";
	Cure.addRegions(options.regions);
	Cure.colorScale = d3.scale.category10();
	Cure.edgeColor = d3.scale.category20();
	Cure.Scorewidth = options["Scorewidth"];
	Cure.Scoreheight = options["Scoreheight"];
	Cure.duration = 500;
	var width = 0;
	Cure.cluster = d3.layout.tree().size([ Cure.width*0.8, "auto" ]).separation(function(a, b) {
		try{
			if(a.children.length>2){
				return a.children.length;
			}
		} catch(e){
			
		}
		return (a.parent==b.parent) ?1 :2;
	});
	Cure.diagonal = d3.svg.diagonal().projection(function(d) {
		return [ d.x, d.y ];
	});
	Cure.ClinicalFeatureCollection = new ClinicalFeatureCollection();
	Cure.ClinicalFeatureCollection.fetch();
	Cure.ScoreBoard = new ScoreBoard();
	//Sync Score Board
	Cure.ScoreBoard.fetch();
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
	Cure.ScoreBoardView = new ScoreBoardView({
		collection: Cure.ScoreBoard
	});
	
	Cure.PlayerTreeRegion.show(Cure.PlayerNodeCollectionView);
	Cure.ScoreRegion.show(Cure.ScoreView);
	Cure.ScoreBoardRegion.show(Cure.ScoreBoardView);
	Cure.JSONSummaryRegion.show(Cure.JSONCollectionView);
	Cure.CommentRegion.show(Cure.CommentView);
	Cure.relCoord = $('#PlayerTreeRegionSVG').offset();
});

//App Start
Cure.start({
	"height" : 300,
	"width" : window.innerWidth*0.9,
	"Scorewidth" : 268,
	"Scoreheight" : 200,
	"regions" : {
		"PlayerTreeRegion" : "#PlayerTreeRegion",
		"ScoreRegion" : "#ScoreRegion",
		"CommentRegion" : "#CommentRegion",
		"ScoreBoardRegion" : "#scoreboard_wrapper",
		"JSONSummaryRegion" : "#jsonSummary"
	},
	posNodeName: "y",
	negNodeName: "n"
});
