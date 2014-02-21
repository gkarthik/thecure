require.config({
	baseUrl: "/cure/cured3/assets/",
  paths : {
    underscore : 'lib/underscore',
    backbone : 'lib/backbone',
    backboneRelational : 'lib/backbone-relational',
    marionette : 'lib/marionette.backbone.min',

    //jQuery
    jquery : 'http://ajax.googleapis.com/ajax/libs/jquery/2.1.0/jquery.min',
    jqueryui : 'http://ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min',

    //d3
    d3 : 'lib/d3.v3',

    //mygeneautocomplete
    myGeneAutocomplete : 'lib/mygene_autocomplete_jqueryui',
    text : 'lib/text'
  },
  shim : {
    jquery : {
      exports : '$'
    },
    underscore : {
      exports : '_'
    },
    backbone : {
      deps : ['jquery', 'underscore'],
      exports : 'Backbone'
    },
    backboneRelational : {
      deps : ['backbone'],
      exports : 'BackboneRelational'
    },
    marionette : {
      deps : ['jquery', 'underscore', 'backbone', 'backboneRelational'],
      exports : 'Marionette'
    },
    jqueryui : {
      deps : ['jquery'],
      exports : 'jQueryUI'
    },
    d3 : {
      exports : 'd3'
    },
    myGeneAutocomplete : {
      deps : ['jquery','jqueryui'],
      exports : 'myGeneAutocomplete'
    }
  }
});

//Starting the app
require(["app/core"],
function() {
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
});
