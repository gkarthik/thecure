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
		// This add is for the seed node alone.
		this.on("add", function(model) {
			Cure.updatepositions(this);
			Cure.render_network(this.toJSON()[0]);
		});
		this.on("remove", function() {
			Cure.updatepositions(this);
			Cure.render_network(this.toJSON()[0]);
		});
	},
  url: "/cure/MetaServer",
  sync: function() {
    var tree=[];
    if(this.models[0])
    {
    	tree = this.models[0].toJSON();
    }
    var args = {
      command : "scoretree",
      dataset : "griffith_breast_cancer_1",
      treestruct : tree
    };
		$.ajax({
			type : 'POST',
			url : '/cure/MetaServer',
			data : JSON.stringify(args),
			dataType : 'json',
			contentType : "application/json; charset=utf-8",
			success:      this.parseResponse,
      error:        this.error
		});
  },
  updateCollection: function(json_node,node,parent){
  	if(node!=null && json_node!=null)
  	{
  		for ( var key in json_node) {
				if(key!="children"){
					node.set(key, json_node[key]);
				}
			}
  		if(json_node.children.length>0&&json_node.children.length==node.get('children').length)
  		{
  			for(var temp in json_node.children)
  			{
  				this.updateCollection(json_node.children[temp],node.get('children').models[temp]);
  			}
  		}
  		else if(json_node.children.length > node.get('children').length)
  		{
  			for(var temp in json_node.children)
  			{
  				this.updateCollection(json_node.children[temp],null,node);
  			} 
  		}
  		else if(json_node.children.length < node.get('children').length)
  		{
  			var temp = 0;
  			for(temp in json_node.children)
  			{
  				this.updateCollection(json_node.children[temp],node.get('children').models[temp]);
  			}
  			temp++;
  			for(var i = temp; i<node.get('children').length; i++)
  			{
  				delete_all_children(node.get('children')[temp]);
  				node.get('children')[temp].destroy();
  			}
  		}
  	}
  	else if(node == null)
  	{
  		var newNode = new Node({
  			'name' : "",
  			"options" : {
  			},
  			"created_by": "player"
  		});
  		for ( var key in json_node) {
				if(key!="children"){
					newNode.set(key, json_node[key]);
				}
			}
  		newNode.set("cid", newNode.cid);
  		if(parent!=null)
  		{
  			parent.get('children').add(newNode);
  		}
  		for(var temp in json_node.children)
			{
  			this.updateCollection(json_node.children[temp],null,newNode);
			}
  	}
  	/*
  	Cure.modelcount++;
  	var json = data;
  	var increment = 0;
  	if(data["treestruct"])
  	{
  		json = data["treestruct"];
  	}
  	if(this.models[Cure.modelcount])
  	{
				for ( var key in json) {
					if(key!="children"&&key!="x"&&key!="y"){
						this.models[Cure.modelcount].set(key, json[key]);
					}
				}
			var json_children = {"length":0};
			if(json.children)
			{
				json_children = json.children;
			}
			var collection_children = {"length":0};
			var temp_model;
			temp_model = Cure.modelcount;
			if(this.models[Cure.modelcount].get("children"))
			{
				collection_children = this.models[Cure.modelcount].get("children");
			}
			if(json_children.length == collection_children.length && collection_children.length>0)
			{//Just sync attributes
				for(var temp in json_children)
				{
					this.updateCollection(json_children[temp],this.models[temp_model]);
				}
			}
  		else if(json_children.length > collection_children.length)
  		{//Add extra nodes to tree
  			for(var temp in json_children)
				{
  				this.updateCollection(json_children[temp],this.models[temp_model]);
				}
  		}
  		else if(json_children.length < collection_children.length)
  		{//Delete excess in tree
  			for(var temp in json_children)
				{
  				this.updateCollection(json_children[temp],this.models[temp_model]);
				}
  			temp++;
  			for(var i=temp;i<collection_children.length;i++)
  			{
  				delete_all_children(collection_children[temp]);
  				collection_children[temp].destroy();
  			}
  		}
  	}
  	else
  	{
  		var newNode = new Node({
  			'name' : "",
  			"options" : {
  			},
  			"created_by": "player"
  		});
  		for ( var key in json) {
				if(key!="children"&&key!="x"&&key!="y"){
					newNode.set(key, json[key]);
				}
			}
  		newNode.set("cid", newNode.cid);
  		if(parent!=null)
  		{
  			parent.get('children').add(newNode);
  		}
  		for(var temp in json.children)
			{
  			this.updateCollection(json.children[temp],newNode);
			}
  	}
  	*/
  },
  parseResponse: function(data){
  	Cure.PlayerNodeCollection.updateCollection(data["treestruct"],Cure.PlayerNodeCollection.models[0],null);
  },
  error: function(data){

  }
});

//
// -- Defining our models
//
Node = Backbone.RelationalModel.extend({
	defaults : {
		'name' : '',
		'cid' : 0,
		'options' : {
      "id": "",
      "kind": "split_node"
		},
		edit : 0,
		highlight : 0,
		created_by:"",
		children: []
	},
	initialize : function() {
		this.bind("add:children", function() {
			if(this.get("created_by")=="player")
			{
				Cure.render_network(Cure.PlayerNodeCollection.toJSON()[0]);
				Cure.updatepositions(Cure.PlayerNodeCollection);
			}
			else if(this.get("created_by")=="barney")
			{
				Cure.render_network(Cure.BarneyNodeCollection.toJSON()[0]);
				Cure.updatepositions(Cure.BarneyNodeCollection);
			}
		});
		this.bind("change", function() {
			if(this.get("created_by")=="player")
			{
				Cure.render_network(Cure.PlayerNodeCollection.toJSON()[0]);
			}
			else if(this.get("created_by")=="barney")
			{
				Cure.render_network(Cure.BarneyNodeCollection.toJSON()[0]);
			}
		});
		if(this.get("created_by")=="player")
		{
			Cure.PlayerNodeCollection.add(this);
		}
		else if(this.get("created_by")=="barney")
		{
			Cure.BarneyNodeCollection.add(this);
		}
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
var node_html = $("#nodeTemplate").html();
var splitvaluenode_html = $("#splitValueTemplate").html();
var splitnode_html = $("#splitNodeTemplate").html();
NodeView = Backbone.Marionette.ItemView.extend({
	// -- View to manipulate each single node
	tagName : 'div',
	className : 'node',
	ui : {
		input : ".edit",
		addgeneinfo: ".addgeneinfo"
	},
	template : function(serialized_model){
		if(serialized_model.options.kind == "split_value")
		{
			return _.template(splitvaluenode_html, {
				name : serialized_model.name,
				options : serialized_model.options,
				cid : serialized_model.cid
			}, {
				variable : 'args' 
			});
		}
		else if(serialized_model.children.length>0 && serialized_model.options.kind == "split_node")
		{
			return _.template(splitnode_html, {
				name : serialized_model.name,
				options : serialized_model.options,
				cid : serialized_model.cid
			}, {
				variable : 'args'
			});			
		}
			return _.template(node_html, {
				name : serialized_model.name,
				options : serialized_model.options,
				cid : serialized_model.cid
			}, {
			variable : 'args'
		});
	},
	events : {
		'click button.addchildren' : 'addChildren',
		'click button.delete' : 'removeChildren',
		'dblclick .name' : 'edit',
		'keypress .edit' : 'updateOnEnter',
		'blur .edit' : 'closeInput'
	},
	initialize : function() {
		_.bindAll(this, 'remove', 'addChildren');
		this.model.bind('change', this.render);
		this.model.bind('add:children', this.render);
		this.model.bind('remove', this.remove);
		this.model.on('change:highlight', function() {
			if (this.model.get('highlight') != 0) {
				this.$el.addClass('highlight');
			} else {
				this.$el.removeClass('highlight');
			}
		}, this);
	},
	onBeforeRender : function() {
		if (this.model.get('x0') != undefined) {
			$(this.el).css({
				'margin-left' : this.model.get('x0') + "px",
				'margin-top' : this.model.get('y0') + "px"
			});
		}
		$(this.el).stop(true, false)
				.animate(
						{
							'margin-left' : (this.model.get('x') - (($(this.el)
									.width()) / 2))
									+ "px",
							'margin-top' : (this.model.get('y') + 10) + "px"
						}, 500);
	},
	updateOnEnter : function(e) {
		if (e.which == 13) {
			this.closeInput();
		}
	},
	closeInput : function() {
		var value = this.ui.input.val().trim();
		if (value) {
			this.model.set('name', value);
		}
		this.$el.removeClass('editing');
	},
	edit : function() {
		this.$el.addClass('editing');
		this.ui.input.focus();
	},
	remove : function() {
		//if (Cure.NodeCollection.length > 1) {//Have to eliminate use of Cure.NodeCollection
		this.$el.remove();
			Cure.delete_all_children(this.model);
			this.model.destroy();
		//}
	},
	removeChildren : function() {
		if (this.model.get('parentNode') != null) {
			Cure.delete_all_children(this.model);
			var prevAttr = this.model.get("previousAttributes");
				for ( var key in prevAttr) {
						this.model.set(key, prevAttr[key]);
				}
				this.model.set("previousAtributes", []);//jdoe@d.com//regular
		}
		else
		{
			Cure.delete_all_children(this.model);
			this.model.destroy();
		}
	},
	addChildren : function() {
		var GeneInfoRegion = new Backbone.Marionette.Region({
		  el: "#"+$(this.ui.addgeneinfo).attr("id")
		});
		Cure.addRegions({MyGeneInfoRegion:GeneInfoRegion});
		var ShowGeneInfoWidget = new AddRootNodeView({'model':this.model});
		Cure.MyGeneInfoRegion.show(ShowGeneInfoWidget);
	}
});

AddRootNodeView = Backbone.Marionette.ItemView.extend({
	initialize : function() {
	},
	ui: {
		'input': '.mygene_query_target'
	},
	template: "#AddRootNode",
	render: function(){
		if(this.model)
		{
			var model = this.model;
		}
		var html_template= $("#AddRootNode").html();
		this.$el.html(html_template);
		this.$el.find('input.mygene_query_target').genequery_autocomplete({
	    select: function(event, ui) {
	    var kind_value="";
	    try{
	    	kind_value= model.get("options").kind;
	    }catch (exception) {}
	    if(kind_value=="leaf_node")
	    {
	    	model.set("previousAttributes",model.toJSON());
	    	model.set("name",ui.item.name);
	    	model.set("options",{id: ui.item.id,"kind": "split_node"});
	    }
	    else
	    {
	    	var newNode = new Node({
					'name' : ui.item.name,
					"options" : {
						id: ui.item.id,
						"kind": "split_node"
					},
					"created_by": "player"
				});
				newNode.set("cid", newNode.cid);
				if(model.get("children"))
				{
					model.get("children").add(newNode);
				}
	    }
			if(Cure.MyGeneInfoRegion)
			{
				Cure.MyGeneInfoRegion.close();
			}
			Cure.PlayerNodeCollection.sync();
		}
	});
	},
});

NodeCollectionView = Backbone.Marionette.CollectionView.extend({
	// -- View to manipulate and display list of all nodes in collection
	itemView : NodeView,
	emptyView : AddRootNodeView,
	initialize : function() {
		this.collection.bind('add', this.onModelAdded);
	},
	onModelAdded : function(addedModel) {
		var newNodeview = new NodeView({
			model : addedModel
		});
		newNodeview.render();
	}
});

var shownode_html = $("#JSONtemplate").html();
var nodeedit_html = $('#Attrtemplate').html();
JSONItemView = Backbone.Marionette.ItemView.extend({
	// -- View to render JSON
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
		'blur .jsonview_data' : 'HideJSON',
		'click .showattr' : 'ShowAttr',
		'dblclick .attredit' : 'editAttr',
		'keypress .edit' : 'onEnter',
		'blur .edit' : 'updateAttr',
		'click .editdone' : 'doneEdit'
	},
	tagName : "tr",
	initialize : function() {
		// this.model.bind('add:children', this.render);
		this.model.bind('change', this.render);
		this.model.on('change:edit', function() {
			if (this.model.get('edit') != 0) {
				this.$el.addClass('editnode');
			} else {
				this.$el.removeClass('editnode');
			}
		}, this);
	},
	template : function(serialized_model) {
		var name = serialized_model.name;
		var options = serialized_model.options;
		if (serialized_model.edit == 0) {
			return _.template(shownode_html, {
				name : name,
				jsondata : Cure.prettyPrint(serialized_model)
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
	editAttr : function(e) {
		var field = $(e.currentTarget);
		field.addClass("editing");
		$(".edit", field).focus();
	},
	ShowJSON : function() {
		this.ui.showjson.addClass("disabled");
		this.ui.jsondata.css({
			'display' : 'block'
		});
		this.ui.jsondata.focus();
	},
	HideJSON : function() {
		this.ui.jsondata.css({
			'display' : 'none'
		});
		this.ui.showjson.removeClass("disabled");
	},
	onEnter : function(e) {
		if (e.which == 13) {
			this.updateAttr($(e.currentTarget));
		}
	},
	updateAttr : function(field) {
		if (field instanceof jQuery.Event) {
			field = $(field.currentTarget);
		}
		if (field.hasClass("modeloption")) {
			var data = {};
			data["options"] = this.model.get('options');
			data["options"][field.attr('id')] = field.val();
			this.model.set(data);
		} else {
			var data = {};
			data[field.attr('id')] = field.val();
			this.model.set(data);
		}
		this.render();
	},
	ShowAttr : function() {
		this.model.set('edit', 1);
	},
	doneEdit : function() {
		this.model.set('edit', 0);
	}
});

JSONCollectionView = Backbone.Marionette.CollectionView.extend({
	// -- View to render JSON
	itemView : JSONItemView,
	collection : NodeCollection,
	initialize : function() {
		this.collection.bind('remove', this.render);
	}
});
//
// -- Utilities / Helpers
//

// -- Pretty Print JSON.
// -- Ref :
// http://stackoverflow.com/questions/4810841/json-pchildjson["children"].length>0retty-print-using-javascript
Cure.prettyPrint = function(json) {
	if (typeof json != 'string') {
		json = JSON.stringify(json, undefined, 2);
	}
	json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g,
			'&gt;');
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
// -- Get JSON from d3 to BackBone
//
Cure.updatepositions = function(NodeCollection) {
	var Collection = [];
	if(NodeCollection.toJSON()[0])
	{
		Collection = NodeCollection.toJSON()[0];
	}
	var d3nodes = [];
	d3nodes = Cure.cluster.nodes(Collection);
	Cure.cluster.nodes(Collection);
	d3nodes.forEach(function(d) {
		d.y = d.depth * 130;
	});
	d3nodes.forEach(function(d) {
		d.x0 = d.x;
		d.y0 = d.y;
	});
	for ( var temp in NodeCollection["models"]) {
		for ( var innerTemp in d3nodes) {
			if (String(d3nodes[innerTemp].cid) == String(NodeCollection["models"][temp]
					.get('cid'))) {
				NodeCollection["models"][temp].set("x",
						d3nodes[innerTemp].x);
				NodeCollection["models"][temp].set("y",
						d3nodes[innerTemp].y);
				NodeCollection["models"][temp].set("x0",
						d3nodes[innerTemp].x0);
				NodeCollection["models"][temp].set("y0",
						d3nodes[innerTemp].y0);
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

Cure.generateJSON = function(parent, childjson, created_by, Collection) {
	//var NodesPresent = Collection.getNodeByName(childjson["name"]);
	//if(NodesPresent.length==0)
	{
		var creator = "";
		if(created_by != null)
		{
			creator = created_by;
		}
		else
		{
			creator = parent.get("created_by");
		}
		var newNode = new Node({
			'name' : childjson["name"],
			"options" : {},
			created_by : creator
		});
		for ( var temp in childjson) {
			if (temp == "name") {
				newNode.set("name", childjson[temp]);
			} 
			else if (temp != "children" && temp != "created_by") {
				var data = newNode.get("options");
				data[temp] = childjson[temp];
				newNode.set("options", data);
			}
		}
		newNode.set("cid", newNode.cid);
		if (parent != null) {
			parent.get('children').add(newNode);
		}
	}
	var flag = null;
	try {
		flag = childjson["children"].length;
	} catch (exception) {

	}
	if (flag != null) {
		for ( var childtemp in childjson["children"]) {
			Cure.generateJSON(newNode, childjson["children"][childtemp], null, Collection);
		}
	}
}

Cure.traverseTree = function(rootNode) {
	rootNode.set("highlight", 1);
	var childnodes = rootNode.get('children').models;
	if (childnodes.length > 0) {
		var min_index = 0;
		var min_value = 100;
		for ( var temp in childnodes) {
			if (childnodes[temp].get('options').bin_size < min_value) {
				min_value = childnodes[temp].get('options').bin_size;
				min_index = temp;
			}
		}
		window.setTimeout(function() {
			Cure.traverseTree(childnodes[min_index]);
		}, 1000);
	} else {
		$("#traverse").html("Traverse Tree");
		$("#traverse").removeClass("disabled");
	}
}

//
// -- Render d3 Network
//
Cure.render_network = function(dataset) {
	var scaleY = d3.scale.linear()
	     .domain([0, 1])
	     .rangeRound([0, 100]);
	
	if(dataset)
	{
		var binY = d3.scale.linear()
		.domain([0, dataset.options.bin_size])
		.rangeRound([0, 50]);
	}
	else
	{
		var binY = d3.scale.linear()
		.domain([0, 100])
		.rangeRound([0, 50]);
		dataset = [{
			'name' : '',
			'cid' : 0,
			'options' : {
	      "id": "",
	      "kind": "split_node"
			},
			edit : 0,
			highlight : 0,
			created_by:"",
			children: []
		}];
	}
	var SVG;
	if(dataset)
	{
		SVG = Cure.PlayerSvg;
		var nodes = Cure.cluster.nodes(dataset), links = Cure.cluster.links(nodes);

		nodes.forEach(function(d) {
			d.y = d.depth * 130;
			if(!d.options)
			{
				d.options =[];
			}
		});

		var node = SVG.selectAll(".MetaDataNode")
    .data(nodes);

		var nodeEnter = node.enter().append("svg:g")
    	.attr("class", "MetaDataNode")
    	.attr("transform", function(d) { return "translate(" + dataset.x + "," + dataset.y + ")"; });
		nodeEnter.append("rect")
			.attr("class","nodeaccuracy")
    	.attr("width", 10)
    	.attr("height", function(d){
    		var height = 0;
    		if(d.options.pct_correct)
    		{
    			height = scaleY(d.options.pct_correct);
    		}
    		else if(d.options.infogain)
    		{
    			height = scaleY(d.options.infogain);
    		}
    		return height;
    	})
    	.attr("x",70)
    	.attr("y", function(d){ 
    		var height = 0;
    		if(d.options.pct_correct)
    		{
    			height = scaleY(d.options.pct_correct);
    		}
    		else if(d.options.infogain)
    		{
    			height = scaleY(d.options.infogain);
    		}
    		return -1 * height; })
    	.style("fill", function(d) { return "lightsteelblue"; });
		
		nodeEnter.append("svg:text")
		.attr("class","nodeaccuracytext")
  	.attr("transform", "translate(60, 0) rotate(-90)")
  	.style("font-size","13")
  	.style("fill", function(d) { return "lightsteelblue"; })
  	.text(function(d){
  		var text = "";
  		if(d.options.pct_correct)
  		{
  			text = "Accuracy: "+d.options.pct_correct;
  		}
  		else if(d.options.infogain)
  		{
  			text = "Info Gain"+d.options.infogain;
  		}
  		return text; }
  	);
		//Bin Size
		nodeEnter.append("rect")
		.attr("class","binsize")
  	.attr("width", 10)
  	.attr("height", function(d){
  		var height = 0;
  		if(d.options.bin_size)
  		{
  			height = binY(d.options.bin_size);
  		}
  		return height;
  	})
  	.attr("x",110)
  	.attr("y", function(d){ 
  		var height = 0;
  		if(d.options.bin_size)
  		{
  			height = binY(d.options.bin_size);
  		}
  		return -1 * height; })
  	.style("fill", function(d) { return "lightsteelblue"; });
	
	nodeEnter.append("svg:text")
	.attr("class","binsizetext")
	.attr("transform", "translate(100, 0) rotate(-90)")
	.style("font-size","13")
	.style("fill", function(d) { return "lightsteelblue"; })
	.text(function(d){
		var text = "";
		if(d.options.bin_size)
		{
			text = "Bin size: "+d.options.bin_size;
		}
		return text; 
		}
	);
		
		var nodeUpdate = node.transition()
		.duration(Cure.duration)
		.attr("transform", function(d) { return "translate(" + d.x + "," + d.y + ")"; });		
		
		nodeUpdate.select(".nodeaccuracy")
  	.attr("width", 10)
  	.attr("height", function(d){
  		var height = 0;
  		if(d.options.pct_correct)
  		{
  			height = scaleY(d.options.pct_correct);
  		}
  		else if(d.options.infogain)
  		{
  			height = scaleY(d.options.infogain);
  		}
  		return height;
  	})
  	.attr("x",70)
  	.attr("y", function(d){ 
  		var height = 0;
  		if(d.options.pct_correct)
  		{
  			height = scaleY(d.options.pct_correct);
  		}
  		else if(d.options.infogain)
  		{
  			height = scaleY(d.options.infogain);
  		}
  		return -1 * height; })
  	.style("fill", function(d) { return "lightsteelblue"; });
	
	nodeUpdate.select(".nodeaccuracytext")
	.attr("transform", "translate(60, 0) rotate(-90)")
	.style("font-size","13")
	.style("fill", function(d) { return "lightsteelblue"; })
	.text(function(d){
		var text = "";
		if(d.options.pct_correct)
		{
			text = "Accuracy: "+d.options.pct_correct;
		}
		else if(d.options.infogain)
		{
			text = "Info Gain"+d.options.infogain;
		}
		return text; }
	);
	//Bin Size
	nodeUpdate.select(".binsize")
	.attr("width", 10)
	.attr("height", function(d){
		var height = 0;
		if(d.options.bin_size)
		{
			height = binY(d.options.bin_size);
		}
		return height;
	})
	.attr("x",110)
	.attr("y", function(d){ 
		var height = 0;
		if(d.options.bin_size)
		{
			height = binY(d.options.bin_size);
		}
		return -1 * height; })
	.style("fill", function(d) { return "lightsteelblue"; });

nodeUpdate.select(".binsizetext")
.attr("transform", "translate(100, 0) rotate(-90)")
.style("font-size","13")
.style("fill", function(d) { return "lightsteelblue"; })
.text(function(d){
	var text = "";
	if(d.options.bin_size)
	{
		text = "Bin size: "+d.options.bin_size;
	}
	return text; 
	}
);
		
	  var nodeExit = node.exit().transition()
    									 .duration(Cure.duration)
    									 .attr("transform", function(d) { return "translate(" + dataset.x + "," + dataset.y + ")"; })
    									 .remove();
		
		var link = SVG.selectAll(".link").data(links);
		link.enter().insert("svg:path", "g").attr("class", "link").attr("d",
				function(d) {
					var o = {
						x : dataset.x0,
						y : dataset.y0
					};
					return Cure.diagonal({
						source : o,
						target : o
					});
				}).style("stroke-width",1);
		link.transition().duration(Cure.duration).attr("d", Cure.diagonal).style("stroke-width",function(d){
			var strokewidth = 1;
			if(d.target.options.kind!="split_value")
			{
				console.log(d.target);
				strokewidth = binY(d.target.options.bin_size);
			}
			else
			{
				if(d.target.children[0])
				{
					strokewidth = binY(d.target.children[0].options.bin_size);
				}
			}
			if(strokewidth<1)
			{
				strokewidth = 1;
			}
			return strokewidth;
		});
		link.exit().transition().duration(Cure.duration).attr("d", function(d) {
			var o = {
				x : dataset.x,
				y : dataset.y
			};
			return Cure.diagonal({
				source : o,
				target : o
			});
		}).remove();
		nodes.forEach(function(d) {
			d.x0 = d.x;
			d.y0 = d.y;
		});
	}
	}

//
// -- App init!
//    
Cure.addInitializer(function(options) {
	_.templateSettings = {
	    interpolate: /\<\@\=(.+?)\@\>/gim,
	    evaluate: /\<\@([\s\S]+?)\@\>/gim,
	    escape: /\<\@\-(.+?)\@\>/gim
	};
	Backbone.emulateHTTP = true;
	$(options.regions.PlayerTreeRegion).html("<div id='"+options.regions.PlayerTreeRegion.replace("#","")+"Tree'></div><svg id='"+options.regions.PlayerTreeRegion.replace("#","")+"SVG'></svg>")
	//$(options.regions.BarneyTreeRegion).html("<div id='"+options.regions.BarneyTreeRegion.replace("#","")+"Tree'></div><svg id='"+options.regions.BarneyTreeRegion.replace("#","")+"SVG'></svg>")
			// Declaring D3 Variables
			Cure.width = options["width"];
			Cure.height = options["height"];	
			Cure.duration = 500;
			Cure.cluster = d3.layout.tree().size([ Cure.width, Cure.height ]);
			Cure.diagonal = d3.svg.diagonal().projection(function(d) {
						return [ d.x, d.y ];
					});
			Cure.PlayerSvg = d3.select(options.regions.PlayerTreeRegion+"SVG").attr("width", Cure.width).attr(
					"height", Cure.height).append("svg:g").attr("transform",
					"translate(0,40)");
			/*
			Cure.BarneySvg = d3.select(options.regions.BarneyTreeRegion+"SVG").attr("width", Cure.width).attr(
					"height", Cure.height).append("svg:g").attr("transform",
					"translate(0,40)");
			*/
			Cure.PlayerNodeCollection = new NodeCollection();
			//Cure.BarneyNodeCollection = new NodeCollection();
			Cure.PlayerNodeCollectionView = new NodeCollectionView({
				collection : Cure.PlayerNodeCollection
			});
			/*
			Cure.BarneyNodeCollectionView = new NodeCollectionView({
				collection : Cure.BarneyNodeCollection
			})
			*/
			
			Cure.JSONCollectionView = new JSONCollectionView({
				collection : Cure.PlayerNodeCollection
			});
			
			// Assign View to Region
			Cure.addRegions({
	  		PlayerTreeRegion : options.regions.PlayerTreeRegion+"Tree",
	  		//BarneyTreeRegion : options.regions.BarneyTreeRegion+"Tree",
	  		JsonRegion : "#json_structure"
	  	});
			Cure.PlayerTreeRegion.show(Cure.PlayerNodeCollectionView);
			//Cure.BarneyTreeRegion.show(Cure.BarneyNodeCollectionView);
			Cure.JsonRegion.show(Cure.JSONCollectionView);

			// Add Nodes from JSON
			//Cure.generateJSON(null, Cure.jsondata["tree"],);			
			Cure.branch = 1;
			/*
			$("#traverse").click(function() {
				Cure.traverseTree(Cure.NodeCollection.models[0]);
				$(this).html("Traversing...");
				$(this).addClass("disabled");
			});
			*/
		});

Cure.start({"height": 600, "width": 800, "regions":{"PlayerTreeRegion":"#PlayerTreeRegion"}});
