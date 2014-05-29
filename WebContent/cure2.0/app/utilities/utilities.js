define([
	'jquery',
	'd3'
],
function($, d3){
// -- Pretty Print JSON. Function NOT being used in code but can be used for rendering JSON for testing purposes.
// -- Ref :
// http://stackoverflow.com/questions/4810841/json-pchildjson["children"].length>0retty-print-using-javascript

var CureUtils = {};
CureUtils.prettyPrint = function(json) {
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
CureUtils.updatepositions = function(NodeCollection) {
	var Collection = [];
	if (NodeCollection.toJSON()[0]) {
		Collection = NodeCollection.toJSON()[0];
	}
	var d3nodes = [];
	d3nodes = Cure.cluster.nodes(Collection);
	Cure.cluster.nodes(Collection);
	var depthDiff = 180;
	var maxDepth = 0;
	d3nodes.forEach(function(d) {
			d.y = 0;
			for(i=1;i<=d.depth;i++){
				if(i%2!=0){
					depthDiff = 200;
				} else {
					depthDiff = 100;
				}
				d.y += depthDiff;
				if (d.y > maxDepth) {
					maxDepth = d.y;
				}
			}
	});
	d3.select("#PlayerTreeRegionSVG").attr("height", maxDepth+300);
	//If fit to screen is checked.
	if((window.innerHeight - 100)<(maxDepth+300) && Cure.Zoom.get('fitToScreen')){
		Cure.Zoom.set({
			'scaleLevel': (window.innerHeight - 100)/(maxDepth+300) 
		});
	}
	d3nodes.forEach(function(d) {
		d.x0 = d.x;
		d.y0 = d.y;
	});
	for ( var temp in NodeCollection["models"]) {
		for ( var innerTemp in d3nodes) {
			if (String(d3nodes[innerTemp].options.cid) == String(NodeCollection["models"][temp].get('options').get('cid'))) {
				NodeCollection["models"][temp].set({"x": d3nodes[innerTemp].x, "y": d3nodes[innerTemp].y});
			}
		}
	}
}

//
// -- Function to delete all children of a node
//
CureUtils.delete_all_children = function(seednode) {
	var children = seednode.get('children');
	if (seednode.get('children').length > 0) {
		for (var i=0;i < children.models.length;i++) {
			CureUtils.delete_all_children(children.models[i]);
			children.models[i].destroy();
			i--;
		}
	}
}

//
// -- Render d3 Network
//
CureUtils.render_network = function() {
	var dataset = Cure.PlayerNodeCollection.at(0) ? Cure.PlayerNodeCollection.at(0).toJSON() : undefined;
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
			'options' : {
				"id" : "",
				'cid' : 0,
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
			if (!d.options) {
				d.options = [];
			}
		});
		
		//Drawing Edges
		var node;
		if(Cure.PlayerNodeCollection.length>0){
			node = Cure.PlayerNodeCollection.at(0);
		}
		edgeCount = 0;
		translateLeft = 0;
		allLinks = [];
		if(node){
			CureUtils.drawEdges(node,binY,0);
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
				divLeft = parseFloat(divLeft - parseFloat(binY(allLinks[temp-1].bin_size/2)) - parseFloat(binY(allLinks[temp].bin_size/2)));//When adding second edge, essential to include complete width of previous edge.
				count = allLinks[temp].linkNumber;
			}
			if(allLinks[temp].target.options.kind == "split_value"){
				allLinks[temp].source.y = parseFloat(allLinks[temp].source.y + 107);//For Split Nodes 
			} else if(allLinks[temp].target.options.kind == "leaf_node") {
				allLinks[temp].target.y = parseFloat(allLinks[temp].target.y - 34);//For Leaf Nodes
			}
			allLinks[temp].source.x = parseFloat(divLeft + allLinks[temp].source.x);
			allLinks[temp].target.x = parseFloat(divLeft + allLinks[temp].target.x);
		}
		
		var link = SVG.selectAll(".link").data(allLinks);
		
		var source = {};
		var target = {};
		var nodeOffsets = CureUtils.setOffsets();

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
		});
		
		link.transition().delay(Cure.duration).style("stroke-width", function(d){
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
		
		link.exit().transition().duration(CureUtils.duration).attr("d",function(d){
			return Cure.diagonal({
				source : d.source,
				target : d.source
			});
		}).remove();
	}
}

CureUtils.showAlert = function(message, success){
	if(success){
		$("#alertWrapper").removeClass("alert-danger");
		$("#alertWrapper").addClass("alert-success");
	} else {
		$("#alertWrapper").removeClass("alert-success");
		$("#alertWrapper").addClass("alert-danger");		
	}
	$("#alertMsg").html(message);
	$("#alertWrapper").fadeIn();
	window.setTimeout(function(){
		$("#alertWrapper").hide();
	},4000);
}

CureUtils.showDetailsOfNode = function(content, top, left){
	$("#NodeDetailsWrapper").css({
		"top": top-100,
		"left": left+150,
		"display": "block"
	});
	$("#NodeDetailsContent").html('<span><button type="button" class="close">×</button></span>'+content);
}

CureUtils.ToggleHelp = function(check, helptext){
	if(!check){
		$("#HelpText").removeClass("HelpButton");
		$("#HelpText").addClass("HelpWindow");
		$("#HelpText").css({
			width: Cure.width+"px"
		});
    	window.setTimeout(function(){
        	$("#HelpText").html(helptext);
		},500);
	} else {
		$("#HelpText").removeClass("HelpWindow");
		$("#HelpText").addClass("HelpButton");
		$("#HelpText").css({
			width: "45px"
		});
		$("#HelpText").html("");
		window.setTimeout(function(){
			$("#HelpText").html("Help");
		},500);
	}
}

CureUtils.isInt = function(n) {
   return n % 1 === 0;
}

CureUtils.isJSON = function(str) {
    try {
        JSON.parse(str);
    } catch (e) {
        return false;
    }
    return true;
}

CureUtils.showLoading = function(countString){
	if(countString!=null){
		$("#loadingCount").html(countString);
	}
	$("#loading-wrapper").show();
}

CureUtils.hideLoading = function(){
	$("#loadingCount").html("");
	$("#loading-wrapper").hide();
}

//Function to get number of nodes at a particular depth level
CureUtils.getNumNodesatDepth = function(root,givenDepth){
	var num = 0;
	if(CureUtils.getDepth(root)==givenDepth){
		num++;
	} else if(CureUtils.getDepth(root)<givenDepth){
		if(root.get('children').models.length>0){
			for(var temp in root.get('children').models){
				num+=CureUtils.getNumNodesatDepth(root.get('children').models[temp],givenDepth);
			}
		}
	}
	return num;
}

//Function to convert from HSL to RGB
//Reference -> http://stackoverflow.com/questions/2353211/hsl-to-rgb-color-conversion
CureUtils.hslToRgb = function hslToRgb(h, s, l){
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
CureUtils.getDepth = function(node){
	var  givenDepth= 0;
	while(node!=null){
		node = node.get('parentNode');
		givenDepth++;
	}
	return givenDepth;
}

CureUtils.drawChart = function(parentElement, limit, accLimit,radius, nodeKind, nodeName){
	parentElement.selectAll(".chartWrapper").remove();
	var chartWrapper = parentElement.attr("width",function(){
		return (radius*20)+8;
	}).attr("height",function(){
		return (radius*20)+8;
	}).append("svg:g").attr("class","chartWrapper").attr("transform","translate(3,3)");
	chartWrapper.append("rect").attr("class","chartContainer").attr("height",function(){
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
		if(CureUtils.isInt(accLimit) || i<=(accLimit-1) || i > (accLimit)){
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
		} else if(!CureUtils.isInt(accLimit) && i== parseInt((accLimit)/1)){//Final square to be printed
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
CureUtils.drawEdges = function(node,binY,count){
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
			links.push({"source":source,"target":target,"bin_size":node.get('options').get('bin_size'),"name":node.get('name'),"linkNumber":leafNodeCount,"divLeft":0});
			tempNode = tempNode.get('parentNode');
		}
		allLinks.push.apply(allLinks, links);
	}
	var i = 0;
	for(i = node.get('children').models.length;i>0;i--){
		CureUtils.drawEdges(node.get('children').models[i-1],binY,count);
	}
}

CureUtils.setOffsets = function(){
	var nodeOffsets = [];
	$('.node').each(function(){
		nodeOffsets.push($(this).offset());
	});
	return nodeOffsets;
}

var temp = 0;
CureUtils.shiftNodes = function(translateX,translateY,nodeOffsets){
	temp = 0;
	$('.node').each(function(){
		$(this).css({"transform":"translate("+parseFloat(translateX)+"px,"+parseFloat(translateY)+"px)"});
		temp++;
	});
}

CureUtils.highlightNodes = function(seednode, ListofIds){
	var children = seednode.get('children');
	if(jQuery.inArray( seednode.get('options').get('cid'), ListofIds )!= -1){
		seednode.set('highlight',1);
	} else {
		seednode.set('highlight',0);
	}
	if (seednode.get('children').length > 0) {
		for (var i=0;i < children.models.length;i++) {
			CureUtils.highlightNodes(children.models[i], ListofIds);
		}
	}
}

CureUtils.getNumNodesinJSON = function(node){
	var num = 0;
	if(node!=null){
		num++;
		if(node.children){
			for(var temp in node.children){
				num+=CureUtils.getNumNodesinJSON(node.children[temp]);
			}
		}
	}
	return num;
}

return CureUtils;
});
