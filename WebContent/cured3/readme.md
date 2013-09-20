#The Cure - Gsoc '13
#Description

A new interface developed for a master level of the Cure. It involves developing a tree starting from the root node. 

An auto complete feature exists to help in choosing the relevant gene. The auto complete is supported by the REST services at <http://mygene.info>. 

#Functions
 - Hit + to add nodes and x to delete them.
 - A list of genes in the tree is displayed on the left with further information regarding split values. 
 - Clicking on the gene node displays gene summary.
 - Hovering or focussing on the gene list in the autocomplete also throws up a Gene Summary bubble.

#Score Chart
The score is determined by three factors.

 - Size of tree
 - Novelty
 - Accuracy

The three values are visualized in a radar chart on the sidebar. 
Score is calucalted by the following formula,
Score = 1000 * accuracy + 750* (1/size_of_tree) + 500 * feature_novelty.
Further details regarding the current tree are shown on hover of the score chart.

#Libraries Used
- Backbone <http://backbonejs.org/>
- Marionette <http://marionettejs.com/>
- Backbone Relational <http://backbonerelational.org/>
- d3 <http://d3js.org/>
- jQuery <http://http://jquery.com/>


#Screenshots

Auto Complete from <http://mygene.info/> 

![Alt text](http://gkarthik.net/ss/Skywalker130920215152.png "Auto Complete") 

On Selecting the first node

![Alt text](http://gkarthik.net/ss/Skywalker130920215247.png "First Node Rendered") 

Score Chart

![Alt text](http://gkarthik.net/ss/Skywalker130920215338.png "Score Chart") 

Gene Summary

![Alt text](http://gkarthik.net/ss/Skywalker130920215440.png "Gene Summary") 



