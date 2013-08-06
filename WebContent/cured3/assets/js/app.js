    //
    //-- Defining the App
    //
    Cure = new Backbone.Marionette.Application();
    
    //
    //-- Defining our collections
    //
    NodeCollection = Backbone.Collection.extend({
        model : Node,
        initialize: function() {
          //This add is for the seed node alone.      
          this.on("add",function(){
            Cure.updatepositions();
            Cure.render_network(this.toJSON()[0]);
          });
          this.on("remove",function() {
            Cure.updatepositions();
            Cure.render_network(this.toJSON()[0]);
          });
        }
    });

    //
    //-- Defining our models
    //
    Node = Backbone.RelationalModel.extend({
      defaults: {
        'name' : '',
        'cid':0,
        'options' : {
        	
        },
        edit: 0,
        highlight: 0 
      },
      url: "./",
      initialize: function() { 
        this.bind("add:children", function() {
          Cure.updatepositions();
          Cure.render_network(Cure.NodeCollection.toJSON()[0]);          
        });
        this.bind("change", function() {
          Cure.render_network(Cure.NodeCollection.toJSON()[0]);
        });          
        Cure.NodeCollection.add(this);        
      },
      relations: [{
        type: Backbone.HasMany,
        key: 'children',
        relatedModel: 'Node',
        reverseRelation: {
          key: 'parentNode',
          includeInJSON: false
        }
      }]
    });

    //
    //-- Defining our views
    //
    NodeView = Backbone.Marionette.ItemView.extend({
      //-- View to manipulate each single node
      tagName: 'div',
      className: 'node',
      ui: {
        input: ".edit"
      },
      template: "#nodeTemplate",
      events: {
        'click button.addchildren'  : 'addChildren',
        'click button.delete'       : 'remove',
        'dblclick .name': 'edit',
        'keypress .edit' : 'updateOnEnter',
        'blur .edit' : 'close'
      },
      initialize: function(){
        _.bindAll(this, 'remove', 'addChildren');
        this.model.bind('change', this.render);
        this.model.bind('remove', this.remove);        
        this.model.on('change:highlight', function () {
          if (this.model.get('highlight') != 0) 
          {
            this.$el.addClass('highlight');
          }
          else
          {
            this.$el.removeClass('highlight');
          }
      }, this);
      },
      onBeforeRender: function(){
        if(this.model.get('x0')!=undefined)
        {
          $(this.el).css({'margin-left': this.model.get('x0')+"px", 
                          'margin-top': this.model.get('y0')+"px"});          
        }    
        $(this.el).stop(true,false).animate({'margin-left': (this.model.get('x')-(($(this.el).width())/2))+"px", 
                                             'margin-top': (this.model.get('y')+10)+"px"},500);        
      },
      updateOnEnter: function(e){
        if(e.which == 13){
          this.close();
        }
      },
      close: function(){
        var value = this.ui.input.val().trim();
        if(value) {
          this.model.set('name', value);
        }
        this.$el.removeClass('editing');
      },
      edit: function(){
        this.$el.addClass('editing');
        this.ui.input.focus();
      },
      remove: function(){
        if(Cure.NodeCollection.length > 1) {
          $(this.el).remove();
          Cure.delete_all_children(this.model); 
          this.model.destroy();
        }
      },
      addChildren: function(){
        var name = 0;
        if(this.model.parentNode==null) {
          name = Cure.branch;
          Cure.branch++;
        } else {
          name = this.model.get('name')+"."+this.model.get('children').length;
        }
        var newNode = new Node({'name' : name, "options": {}});
        newNode.set("cid",newNode.cid);
        newNode.parentNode = this.model;
        this.model.get('children').add(newNode);
      }
    });

    NodeCollectionView = Backbone.Marionette.CollectionView.extend({
      //-- View to manipulate and display list of all nodes in collection
      itemView: NodeView,
      initialize: function() {
        this.collection.bind('add', this.onModelAdded);
      },
      onModelAdded: function(addedModel) {
        var newNodeview = new NodeView({ model: addedModel });
        newNodeview.render();
      }
    });
    
    var shownode_html = $("#JSONtemplate").html();
    var nodeedit_html = $('#Attrtemplate').html();
    JSONItemView = Backbone.Marionette.ItemView.extend({
      //-- View to render JSON
      model: Node,
      ui:{
        jsondata: ".jsonview_data",
        showjson: ".showjson",
        attreditwrapper: ".attreditwrapper",
        attredit: ".attredit",
        input: ".edit",
        key: ".attrkey",
      },
      events:{
        'click .showjson': 'ShowJSON',
        'blur .jsonview_data' : 'HideJSON',
        'click .showattr': 'ShowAttr',
        'dblclick .attredit': 'editAttr',
        'keypress .edit' : 'onEnter',
        'blur .edit' : 'updateAttr',
        'click .editdone': 'doneEdit'
      },
      tagName: "tr",
      initialize: function() {        
        //this.model.bind('add:children', this.render);
        this.model.bind('change', this.render);
        this.model.on('change:edit', function () {
          if (this.model.get('edit') != 0) 
          {
            this.$el.addClass('editnode');
          }
          else
          {
            this.$el.removeClass('editnode');
          }
      }, this);
      },
      template : function(serialized_model) {
        var name = serialized_model.name;
        var options = serialized_model.options;
        if(serialized_model.edit == 0)
        {
          return _.template(shownode_html, {name : name,jsondata : Cure.prettyPrint(serialized_model)}, {variable: 'args'});  
        }
        else
        {
          return _.template(nodeedit_html, {name : name,options : options}, {variable: 'args'});
        }
      },
      editAttr: function(e){
        var field = $(e.currentTarget);
        field.addClass("editing");
        $(".edit", field).focus();
      },
      ShowJSON: function(){
        this.ui.showjson.addClass("disabled");
        this.ui.jsondata.css({'display':'block'});
        this.ui.jsondata.focus();
      },
      HideJSON: function(){
        this.ui.jsondata.css({'display':'none'});
        this.ui.showjson.removeClass("disabled");
      },
      onEnter: function(e){
        if(e.which == 13){
          this.updateAttr($(e.currentTarget));
        }
      },
      updateAttr: function(field){
        if(field instanceof jQuery.Event)
        {
          field = $(field.currentTarget);
        }        
        if(field.hasClass("modeloption"))
        {
          var data = {}; 
          data["options"] = this.model.get('options');            
          data["options"][field.attr('id')] = field.val();
          this.model.set(data); 
        }
        else
        {
          var data = {};
          data[field.attr('id')] = field.val();
          this.model.set(data);
        }
        this.render();
      },
      ShowAttr: function(){
        this.model.set('edit',1);
      },
      doneEdit: function(){
        this.model.set('edit',0);
      }
    });

    JSONCollectionView = Backbone.Marionette.CollectionView.extend({
      //-- View to render JSON
      itemView: JSONItemView, 
      collection: NodeCollection,
      initialize: function() {
        this.collection.bind('remove', this.render);
      }
    });
    //
    //-- Utilities / Helpers
    //

    //-- Pretty Print JSON.
    //-- Ref : http://stackoverflow.com/questions/4810841/json-pchildjson["children"].length>0retty-print-using-javascript
    Cure.prettyPrint = function (json) {
      if (typeof json != 'string') {
           json = JSON.stringify(json, undefined, 2);
      }
      json = json.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
      return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function (match) {
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
    //-- Get JSON from d3 to BackBone
    //
    Cure.updatepositions = function ()
    {
      var d3nodes = Cure.cluster.nodes(Cure.NodeCollection.toJSON()[0]);
      d3nodes.forEach(function(d) { d.y = d.depth * 130 ;});
      d3nodes.forEach(function(d) {
        d.x0 = d.x;
        d.y0 = d.y;
      });
      for(var temp in Cure.NodeCollection["models"])
      {
        for(var innerTemp in d3nodes)
        {
          if(String(d3nodes[innerTemp].cid)==String(Cure.NodeCollection["models"][temp].get('cid')))
          {
            Cure.NodeCollection["models"][temp].set("x",d3nodes[innerTemp].x);
            Cure.NodeCollection["models"][temp].set("y",d3nodes[innerTemp].y);
            Cure.NodeCollection["models"][temp].set("x0",d3nodes[innerTemp].x0);
            Cure.NodeCollection["models"][temp].set("y0",d3nodes[innerTemp].y0);   
          }
        }
      }  
    }
    
    //
    //-- Function to delete all children of a node
    //
    Cure.delete_all_children = function (seednode)
    {
      var children = seednode.get('children');
      if(seednode.get('children').length>0)
      {
        for(var temp in children.models)
        {
          Cure.delete_all_children(children.models[temp]);
          children.models[temp].destroy();
        }
      }
    }
    
    Cure.generateJSON = function(parent,childjson)
    {
    	var newNode = new Node();
    	for(var temp in childjson)
    	{
    		if(temp == "name")
    		{
    			newNode.set("name",childjson[temp]);
    		}
    		else if(temp!="children")
    		{
    			var data = newNode.get("options");
    			data[temp] = childjson[temp];
    			newNode.set("options",data);
    		}
    	}
    	newNode.set("cid",newNode.cid);
    	if(parent!=null)
    	{
    		parent.get('children').add(newNode);
    		newNode.parentNode = parent;
    	}
    	var flag = null;
    	try
    	{
    		flag = childjson["children"].length;
    	}
    	catch(exception)
    	{
    		console.log("No Children.");
    	}
  		if(flag != null)
  		{
  			for(var childtemp in childjson["children"])
  			{
  				Cure.generateJSON(newNode,childjson["children"][childtemp]);
  			}
  		} 
    	//console.log(Cure.NodeCollection.toJSON());
    }
    
    Cure.traverseTree = function(rootNode)
    {
      rootNode.set("highlight",1);
      var childnodes = rootNode.get('children').models;
      if(childnodes.length > 0)
      {
        var min_index = 0;
        var min_value = 100;
        for(var temp in childnodes)
        {
          if(childnodes[temp].get('options').bin_size < min_value)
          {
            min_value = childnodes[temp].get('options').bin_size;
            min_index = temp;
          }
        }
        window.setTimeout(function(){
          Cure.traverseTree(childnodes[min_index]);          
        },1000);
      }
      else
      {
        $("#traverse").html("Traverse Tree");
        $("#traverse").removeClass("disabled");
      }
    }
    
    //
    //-- Render d3 Network
    //
    Cure.render_network = function(dataset)
    {
      var nodes = Cure.cluster.nodes(dataset),
          links = Cure.cluster.links(nodes);
      nodes.forEach(function(d) { d.y = d.depth * 130; });
      var link = Cure.svg.selectAll(".link")
        .data(links);
      link.enter()
        .insert("svg:path", "g")
        .attr("class", "link")
        .attr("d", function(d) {
          var o = {x: dataset.x0, y: dataset.y0};
          return Cure.diagonal({source: o, target: o});
        });
      link.transition()
        .duration(Cure.duration)
        .attr("d", Cure.diagonal);
      link.exit().transition()
        .duration(Cure.duration)
        .attr("d", function(d) {
          var o = {x: dataset.x, y: dataset.y};
          return Cure.diagonal({source: o, target: o});
        })
      .remove();
      nodes.forEach(function(d) {
        d.x0 = d.x;
        d.y0 = d.y;
      });
    }

    //
    //-- App init!
    //    
    Cure.addInitializer(function(options){
    	//Test JSON
    	Cure.jsondata = {"evaluation" : {"modelrep":"J48 pruned tree\n------------------\n\naquatic = FALSE\n|   tail = TRUE: mammal (49.0/19.0)\n|   tail = FALSE: not mammal (16.0/5.0)\naquatic = TRUE: not mammal (36.0/6.0)\n\nNumber of Leaves  : \t3\n\nSize of the tree : \t5\n","accuracy":70}, "max_depth":"5","num_leaves":"3","tree_size":"5","tree":{"split_point":"nominal","name":"aquatic","id":"aquatic","attribute_name":"aquatic","kind":"split_node","gain_ratio":0.10834896756263769,"infogain":0.10181386403186005,"total_below_left":65.0,"total_below_right":36.0,"bin_size":101.0,"errors_from_left":30.0,"errors_from_right":6.0,"total_errors_here":36.0,"pct_correct_here":64.35643564356435,"children":[{"name":"FALSE","threshold":"FALSE","bin_size":65.0,"kind":"split_value","children":[{"split_point":"nominal","name":"tail","id":"tail","attribute_name":"tail","kind":"split_node","gain_ratio":0.06080720189176067,"infogain":0.048957398877010186,"total_below_left":49.0,"total_below_right":16.0,"bin_size":65.0,"errors_from_left":19.0,"errors_from_right":5.0,"total_errors_here":24.0,"pct_correct_here":63.07692307692308,"children":[{"name":"TRUE","threshold":"TRUE","bin_size":49.0,"kind":"split_value","children":[{"kind":"leaf_node","name":"mammal","bin_size":49.0,"errors":19.0}]},{"name":"FALSE","threshold":"FALSE","bin_size":16.0,"kind":"split_value","children":[{"kind":"leaf_node","name":"not mammal","bin_size":16.0,"errors":5.0}]}]}]},{"name":"TRUE","threshold":"TRUE","bin_size":36.0,"kind":"split_value","children":[{"kind":"leaf_node","name":"not mammal","bin_size":36.0,"errors":6.0}]}]}};
      //Declaring D3 Variables
      Cure.width = $("#svgwrapper").width(),
      Cure.height = $("#svgwrapper").height(),
      Cure.duration = 500,
      Cure.cluster = d3.layout.tree()
                    .size([Cure.width, Cure.height]),
      Cure.diagonal = d3.svg.diagonal()
                    .projection(function(d) { return [d.x, d.y]; });
      Cure.svg = d3.select("svg").attr("width", Cure.width)
                .attr("height", Cure.height)
                .append("svg:g")
                .attr("transform", "translate(0,40)");
      Cure.NodeCollection = new NodeCollection();
      Cure.NodeCollectionView = new NodeCollectionView({ collection: Cure.NodeCollection }),
      Cure.JSONCollectionView = new JSONCollectionView({ collection: Cure.NodeCollection });
      
      //Assign View to Region
      Cure.addRegions({
        TreeRegion: "#svgwrapper",
        JsonRegion: "#json_structure"
      });
      Cure.TreeRegion.show(Cure.NodeCollectionView);
      Cure.JsonRegion.show(Cure.JSONCollectionView);

      //Add Nodes from JSON
      Cure.generateJSON(null,Cure.jsondata["tree"]);
      Cure.branch = 1;
      $("#traverse").click(function(){
        Cure.traverseTree(Cure.NodeCollection.models[0]);
        $(this).html("Traversing...");
        $(this).addClass("disabled");
      });
    });
    
    Cure.start();