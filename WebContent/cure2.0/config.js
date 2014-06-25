require.config({
      baseUrl : base_url+"cure2%2E0/",
      waitSeconds: 40,
      paths : {
        underscore : 'lib/underscore',
        backbone : 'lib/backbone',
        backboneRelational : 'lib/backbone-relational',
        backboneDeepModel: 'lib/deep-model.min',
        marionette : 'lib/marionette.backbone.min',
        csb: "http://yako.io/jsapi/csb",

        // jQuery
        jquery : 'http://ajax.googleapis.com/ajax/libs/jquery/2.1.0/jquery.min',
        jqueryui : 'http://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min',

        // d3
        d3 : 'lib/d3.v3',

        // mygeneautocomplete
        myGeneAutocomplete : 'lib/mygene_autocomplete_jqueryui',
        text : 'lib/text',
        odometer: 'lib/odometer',
        
        //Bootstrap Tour
        bootstrapTour: 'lib/bootstrap-tour-standalone.min',
        
        //Bootstrap Switch
        bootstrapSwitch : 'lib/bootstrap-switch'
      },
      shim : {
        jquery : {
	        exports : '$'
        },
        underscore : {
	        exports : '_'
        },
        backbone : {
          deps : [ 'jquery', 'underscore' ],
          exports : 'Backbone'
        },
        backboneRelational : {
          deps : [ 'backbone' ],
          exports : 'BackboneRelational'
        },
        backboneDeepModel: {
        	deps : [ 'backbone' ],
          exports : 'BackboneDeepModel'
        },
        marionette : {
          deps : [ 'jquery', 'underscore', 'backbone', 'backboneRelational' ],
          exports : 'Marionette'
        },
        jqueryui : {
          deps : [ 'jquery' ],
          exports : 'jQueryUI'
        },
        d3 : {
	        exports : 'd3'
        },
        csb : {
	        exports : 'csb'
        },
        myGeneAutocomplete : {
          deps : [ 'jquery', 'jqueryui' ],
          exports : 'myGeneAutocomplete'
        },
        text : {
          deps : [ 'jquery', 'underscore' ],
          exports : 'text'
        },
        odometer : {
        	deps : [ 'jquery'],
	        exports : 'Odometer'
        },
        bootstrapTour: {
        	deps: ['jquery'],
        	exports: 'bootstrapTour'
        },
        bootstrapSwitch: {
        	deps: ['jquery'],
        	exports: 'bootstrapSwitch'
        }
      }
    });

// Starting the app
require([ "csb", "app/core" ], function(csb, Cure) {
	Cure.start({
	  "height" : 300,
	  "width" : window.innerWidth - 365,
	  "Scorewidth" : 270,
	  "Scoreheight" : 270,
	  "regions" : {
	    "PlayerTreeRegion" : "#PlayerTreeRegion",
	    "JSONSummaryRegion" : "#jsonSummary",
	    "SideBarRegion": "#cure-panel-wrapper",
	    "ZoomControlsRegion": "#zoom-controls",
	    "LoginRegion": "#LoginRegion",
	    "ScoreBoardRegion" : "#scoreboard_innerwrapper",
	    "GenePoolRegion": "#GenePoolRegion",
	    "FeatureBuilderRegion": "#FeatureBuilderRegion"
	  },
	  posNodeName : "y",
	  negNodeName : "n",
	  startTour: false,
	  scoreWeights: {
	  	pct_correct: 1000,
	  	novelty: 500,
	  	size: 750
	  }
	});
	if(_csb){
	  if(csb.inSession()){
	  	console.log("In Session!");
	  	csb.getUserInfo(function(err, res) {
	  		 if(!err) {
	  		  console.log("@collection info test", res);
	  		 }
	  	});
	  	csb.getUserInfo(function(err, res) {
	  		 if(!err && res.user_token!="Guest") {
	  			 var args = {
		  					command : "user_ref_login",		  					
		  					token: 	res.userToken
		  				};
		  				//POST request to server.		
		  				$.ajax({
		  					type : 'POST',
		  					url : '/cure/SocialServer',
		  					data : JSON.stringify(args),
		  					dataType : 'json',
		  					contentType : "application/json; charset=utf-8",
		  					success : function(data){
		  						if(data.success==true){
		  							Cure.Player.set("username",data.player_name);
			  						Cure.Player.set("id",data.player_id);	
		  						} else {
		  							Cure.utils
		  					    .showAlert("<strong>Error!</strong><br>"+data.message, 0);
		  						}
		  					},
		  					error : function(error){
		  						console.log(error);
		  					}
		  				});
	  		 }
	  	});
	  }
	}
});
