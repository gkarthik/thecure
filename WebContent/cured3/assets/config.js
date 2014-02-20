require.config({
  paths : {
    underscore : './js/underscore.js',
    backbone : './js/backbone.js',
    backboneRelational : './js/backbone-relational.js',
    marionette : 'path/to/marionette',

    //jQuery
    jquery : '//ajax.googleapis.com/ajax/libs/jquery/2.1.0/jquery.min.js',
    jqueryui : '//ajax.googleapis.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min.js',

    //d3
    d3 : '/js/d3.v3.js',

    //mygeneautocomplete
    myGeneAutocomplete : './js/mygene_autocomplete_jqueryui.js'
  },
  shim : {
    jquery : {
      exports : 'jQuery'
    },
    underscore : {
      exports : '_'
    },
    backbone : {
      deps : ['jquery', 'underscore'],
      exports : 'Backbone'
    },
    backboneRelational : {
      deps : ['jquery', 'underscore', 'backbone'],
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
})

//Starting the app
requirejs(["app/core"],
function(Cure) {
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
