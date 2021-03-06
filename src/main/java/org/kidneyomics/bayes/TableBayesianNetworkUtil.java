package org.kidneyomics.bayes;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.kidneyomics.bayes.json.JSON_CPD;
import org.kidneyomics.bayes.json.JSON_CPD_Row;
import org.kidneyomics.bayes.json.JSON_Node;
import org.kidneyomics.bayes.json.JSON_TableBayesianNetwork;
import org.kidneyomics.graph.DirectedNode;
import org.kidneyomics.graph.EvaluationMetric;
import org.kidneyomics.graph.MinNeighborsEvaluationMetric;
import org.kidneyomics.graph.UndirectedNode;
import org.kidneyomics.random.DefaultRandomNumberSerivce;
import org.kidneyomics.random.RandomNumberService;


import org.kidneyomics.graph.TopologicalSorter;

public class TableBayesianNetworkUtil {
	
	static RandomNumberService randService = new DefaultRandomNumberSerivce();
	
	/**
	 * 
	 * @param nodes from a table network
	 * @return a graph representation of the network
	 */
	public static List<DirectedNode<TableNode>> createGraphFromTableNetwork(TableBayesianNetwork network) {
		List<TableNode> nodes = network.nodes();
		List<DirectedNode<TableNode>> graphNodes = new LinkedList<DirectedNode<TableNode>>();
		
		
		//Create look up for nodes
		HashMap<TableNode,DirectedNode<TableNode>> nodeMap = new HashMap<TableNode, DirectedNode<TableNode>>();
		
		for(TableNode node : nodes) {
			nodeMap.put(node, DirectedNode.create(node));
		}
		
		
		//add children to each node
		for(TableNode node : nodes) {
			DirectedNode<TableNode> graphNode = nodeMap.get(node);
			for(TableNode child : node.children()) {
				DirectedNode<TableNode> graphChildNode = nodeMap.get(child);
				graphNode.addChild(graphChildNode);
			}
			
			graphNodes.add(graphNode);
		}
		
		
		return graphNodes;
	}
	
	
	public static List<TableNode> topologicalSort(TableBayesianNetwork network) {
		return TopologicalSorter.sort(createGraphFromTableNetwork(network));
	}
	
	public static List<DiscreteVariable> topologicalSortAsDiscreteVariableList(TableBayesianNetwork network) {
		List<TableNode> nodes = TopologicalSorter.sort(createGraphFromTableNetwork(network));
		
		ArrayList<DiscreteVariable> vars = new ArrayList<DiscreteVariable>(nodes.size());
		
		for(TableNode node : nodes) {
			vars.add(node.variable());
		}
		
		return vars;
	}
	
	
	/**
	 * 
	 * @param nodes from Bayesian network
	 * @return a list of nodes describing an undirected graph for the Bayesian network
	 */
	public static List<UndirectedNode<DiscreteVariable>> createUndirectedGraphFromTableNetwork(TableBayesianNetwork network) {
		List<TableNode> nodes = network.nodes();
		List<UndirectedNode<DiscreteVariable>> graphNodes = new LinkedList<UndirectedNode<DiscreteVariable>>();
		
		//create variable --> graphNode
		HashMap<DiscreteVariable,UndirectedNode<DiscreteVariable>> variable2GraphNode = new HashMap<DiscreteVariable, UndirectedNode<DiscreteVariable>>();
		
		for(TableNode tableNode : nodes) {
			UndirectedNode<DiscreteVariable> graphNode = UndirectedNode.create(tableNode.variable());
			variable2GraphNode.put(tableNode.variable(), graphNode);
			
			//store graph node
			graphNodes.add(graphNode);
		}
		
		//add edges
		//two variables are involved in the same factor then add an edge between them
		for(TableNode tableNode : nodes) {
			ArrayList<DiscreteVariable> list = new ArrayList<DiscreteVariable>(tableNode.factor().scope().size());
			list.addAll(tableNode.factor().scope());
			//loop through all pairs of variables in the factor and add edges between them
			for(int i = 0; i < list.size() - 1; i++) {
				UndirectedNode<DiscreteVariable> graphNodeA = variable2GraphNode.get(list.get(i));
				for(int j = i + 1; j < list.size(); j++) {
					UndirectedNode<DiscreteVariable> graphNodeB = variable2GraphNode.get(list.get(j));
					graphNodeA.addNeighbor(graphNodeB);
				}
			}
		}
		
		return graphNodes;
	}
	
	
	/**
	 * 
	 * @param nodes -- undirected graph nodes list
	 * @return -- return a map where the payload points to each node in the graph
	 */
	public static Map<DiscreteVariable,UndirectedNode<DiscreteVariable>> createGraphNodeMapFromList(List<UndirectedNode<DiscreteVariable>> nodes) {
		HashMap<DiscreteVariable,UndirectedNode<DiscreteVariable>> map = new HashMap<DiscreteVariable, UndirectedNode<DiscreteVariable>>();
		
		for(UndirectedNode<DiscreteVariable> node : nodes) {
			map.put(node.payload(), node);
		}
		
		return map;
	}
	
	/**
	 * Algorithm 9.4 from Probabilistic Graphical Models
	 * An order for performing variable elimination using the supplied metric
	 * @param bayesian network
	 * @param metric to minimize
	 * @return
	 */
	public static List<DiscreteVariable> greedyVariableEliminationOrder(TableBayesianNetwork network, EvaluationMetric<DiscreteVariable> metric) {
		
		
		List<UndirectedNode<DiscreteVariable>> nodes = createUndirectedGraphFromTableNetwork(network);
		LinkedList<DiscreteVariable> order = new LinkedList<DiscreteVariable>();
		HashSet<DiscreteVariable> marked = new HashSet<DiscreteVariable>();
		HashSet<UndirectedNode<DiscreteVariable>> unmarked = new HashSet<UndirectedNode<DiscreteVariable>>();
		unmarked.addAll(nodes);
		HashMap<DiscreteVariable,Double> scores = new HashMap<DiscreteVariable,Double>();
		
		//we need to perform this for each variable
		for(int i = 0; i < nodes.size(); i++) {
			
			//compute score per variable
			//only consider unmarked nodes
			for(UndirectedNode<DiscreteVariable> node : unmarked) {
				scores.put(node.payload(), metric.calculate(node));
			}
			
			//find minimum not marked
			double min = Double.MAX_VALUE;
			UndirectedNode<DiscreteVariable> minNode = null;
			for(UndirectedNode<DiscreteVariable> node : unmarked) {
				double score = scores.get(node.payload());
				if(score < min) {
					min = score;
					minNode = node;
				}
			}
			
			if(minNode == null) {
				throw new IllegalStateException("Algorithm in an illegal state");
			}
			
			//mark node
			marked.add(minNode.payload());
			//remove from unmarked
			unmarked.remove(minNode);
			
			//store variable
			order.add(minNode.payload());
			
			//add edges
			minNode.addEdgesBetweenNeighbors();
			
		}
		
		if(marked.size() != nodes.size()) {
			throw new IllegalStateException("Algorithm in an illegal state");
		}
		
		
		return order;
	}
	
	/**
	 * 
	 * @param network -- bayesian network to perform operation on
	 * @param queryVar -- variable that we want to query
	 * @param evidence -- the evidence that we have
	 * @return
	 */
	public static TableProbabilityDistribution conditionalProbVarElim(TableBayesianNetwork network, DiscreteVariable queryVar, DiscreteVariableValue... evidence) {
		
		Set<TableFactor> factors = network.factors();
		
		for(DiscreteVariableValue piece : evidence) {
			TableNode node = network.getNode(piece.variable());
			if(node == null) {
				throw new IllegalArgumentException(piece.variable() + " not found in the networ!");
			}
			//get factor
			TableFactor factor = node.factor();
			//remove this factor from set of factors
			factors.remove(factor);
			//remove non-evidence rows from factor
			factor = (TableFactor) factor.reduce(piece);
			//add factor back to the set
			factors.add(factor);
		}
		
		List<DiscreteVariable> eliminationOrder = greedyVariableEliminationOrder(network, new MinNeighborsEvaluationMetric<DiscreteVariable>());
		
		//remove query variable
		eliminationOrder.remove(queryVar);
		
		
		return sumProductVariableElimination(factors,eliminationOrder, null);
	}
	
	/***
	 * 
	 * @param network -- bayesian network to perform the analysis on
	 * @param eliminationOrder -- order of variables to eliminate
	 * @return
	 */
	public static TableProbabilityDistribution sumProductVariableElimination(TableBayesianNetwork network, List<DiscreteVariable> eliminationOrder) {
		return sumProductVariableElimination(network.factors(), eliminationOrder, null);
	}
	
	/**
	 * Algorithm 9.1
	 * @param factors -- factors from a bayesian network
	 * @param eliminationOrder -- order of variables to eliminate
	 * @param tree - CliqueTree and optional parameter if you want a clique tree result
	 * @return
	 */
	public static TableProbabilityDistribution sumProductVariableElimination(Set<TableFactor> factors, List<DiscreteVariable> eliminationOrder, CliqueTree tree) {
		
		for(DiscreteVariable var : eliminationOrder) {
			//sum product eliminate
			factors = sumProductEliminateVar(factors,var, tree);
		}
		
		//compute result product
		Iterator<TableFactor> iter = factors.iterator();
		TableFactor current = iter.next();
		while(iter.hasNext()) {
			current = (TableFactor) current.product(iter.next());
		}
		
		return TableProbabilityDistribution.create(current);
	}
	
	/**
	 * 
	 * @param factors
	 * @param varToEliminate
	 * @return factors with varToEliminate marginalized out
	 */
	public static Set<TableFactor> sumProductEliminateVar(Set<TableFactor> factors, DiscreteVariable varToEliminate, CliqueTree tree) {
		
		//all factors that contain variable to be eliminated in their scope
		Set<TableFactor> factorsPrime = new HashSet<TableFactor>();
		for(TableFactor factor : factors) {
			if(factor.scope().contains(varToEliminate)) {
				factorsPrime.add(factor);
			}
		}
		
		//Factors not in the scope of varToEliminate
		Set<TableFactor> factorDoublePrime = new HashSet<TableFactor>();
		for(TableFactor factor : factors) {
			if(!factorsPrime.contains(factor)) {
				factorDoublePrime.add(factor);
			}
		}
		
		//compute product
		Iterator<TableFactor> iter = factorsPrime.iterator();
		TableFactor tauBeforeMarginalize = iter.next();
		while(iter.hasNext()) {
			tauBeforeMarginalize = (TableFactor) tauBeforeMarginalize.product(iter.next());
		}
		
		
		//compute sum
		TableFactor tauAfterMarginalize = (TableFactor) tauBeforeMarginalize.marinalize(varToEliminate);
		
		//update cliquetree
		if(tree != null) {
			//create node with scope tau before marginalize and sa
			tree.addNode(tauBeforeMarginalize, tauAfterMarginalize, factorsPrime);
		}
		
		//add tou to factorDoublePrime
		factorDoublePrime.add(tauAfterMarginalize);
		
		return factorDoublePrime;
		
	}
	
	
	public static <T> String setToString(Set<T> set) {
		StringBuilder sb = new StringBuilder();
		Iterator<T> iter = set.iterator();
		while(iter.hasNext()) {
			sb.append(iter.next().toString());
			if(iter.hasNext()) {
				sb.append(", ");
			}
		}
		return sb.toString();
	}
	
	/**
	 * 
	 * @param network -- table Bayesian network
	 * @param sortedList -- topologically sorted nodes from the Bayesian network
	 * @return a DiscreteInstance from this bayesian network
	 */
	public static DiscreteInstance forwardSample(TableBayesianNetwork network, List<TableNode> sortedList) {
		
		DiscreteInstance instance = DiscreteInstance.create();
		for(TableNode node : sortedList) {
			
			List<Row> rows = null;
			if(node.parents().size() == 0) {
				//no parents so include all rows
				rows = node.cpd().getFactor().rows();
				
			} else {
				//get list of parents values
				HashSet<DiscreteVariableValue> currentValues = new HashSet<DiscreteVariableValue>();
				for(TableNode parent : node.parents()) {
					DiscreteVariableValue parVal = instance.get(parent.variable());
					if(parVal == null) {
						throw new IllegalArgumentException("Graph not topologically sorted!");
					}
					currentValues.add(parVal);
				}
				
				//get rows corresponding to these parental values
				rows = node.cpd().getFactor().getRowsByValues(false, currentValues);
				
			}
			
			Map<DiscreteVariableValue,Double> probsPerVarValue = new HashMap<DiscreteVariableValue, Double>();
			//store map for every variable of value of nodes variable and its correponding probability
			for(Row row : rows) {
				DiscreteVariableValue varValue = row.getVariableValue(node.variable());
				if(probsPerVarValue.containsKey(varValue)) {
					throw new IllegalStateException("not all parents selected");
				}
				probsPerVarValue.put(varValue, row.getValue());
			}
			
			if(probsPerVarValue.size() != node.variable().values().size())  {
				throw new IllegalStateException("not all values for node are included");
			}
			
			DiscreteVariableValue randVarVal = randService.emit(probsPerVarValue);
			
			instance.put(node.variable(), randVarVal);
		}
		
		
		return instance;
	}
	
	public static DiscreteInstance forwardSample(TableBayesianNetwork network) {
		
		List<TableNode> sortedList = topologicalSort(network);
		
		return forwardSample(network,sortedList);
	}
	
	/**
	 * 
	 * @param network -- table bayesian network
	 * @param number -- number of samples to generate
	 * @return a list of generated samples from the network
	 * 
	 */
	public static List<DiscreteInstance> forwardSampling(TableBayesianNetwork network, int number) {
		
		List<TableNode> sortedList = topologicalSort(network);
		List<DiscreteInstance> list = new ArrayList<DiscreteInstance>(number);
		
		
		for(int i = 0; i < number; i++) {
			list.add(forwardSample(network,sortedList));
		}
		
		return list;
	}
	
	
	/**
	 * Learn network weights from complete data
	 * @param network
	 * @param instances
	 */
	public static void computeMaximumLikelihoodEstimation(TableBayesianNetwork network, List<DiscreteInstance> instances) {
		for(TableNode node : network.nodes()) {
			Map<Row,Double> stats = node.cpd().computeSufficientStatisticsCompleteData(instances);
			node.cpd().maximumLikelihoodEstimation(stats);
		}
	}
	
	/**
	 * 
	 * @param network -- the network to use
	 * @param eliminationOrder -- the variable elimination order
	 * @param instances  -- instances of data
	 * @param logScale -- return in log scale
	 * @return the likelihood of the data
	 */
	public static double likelihood(TableBayesianNetwork network, List<DiscreteVariable> eliminationOrder, List<DiscreteInstance> instances, boolean logScale) {
		
		List<CliqueTree> trees = new LinkedList<CliqueTree>();
		
		for(DiscreteInstance instance : instances) {
			CliqueTree tree = CliqueTree.createFromEvidenceAndOrder(network, instance.evidence(), eliminationOrder);
			trees.add(tree);
		}
		
		return likelihood(trees, logScale);
		

	}
	
	/**
	 * 
	 * @param network -- the network to use
	 * @param instances  -- instances of data
	 * @param logScale -- return in log scale
	 * @return the likelihood of the data
	 */
	public static double likelihood(TableBayesianNetwork network, List<DiscreteInstance> instances, boolean logScale) {
		List<DiscreteVariable> eliminationOrder = greedyVariableEliminationOrder(network, new MinNeighborsEvaluationMetric<DiscreteVariable>());
		return likelihood(network, eliminationOrder, instances, logScale);
	}
	
	/**
	 * Compute the expected likelihood of the data
	 * @param trees -- clique trees
	 * @param logScale -- return in log scale
	 * @return
	 */
	public static double likelihood(Collection<CliqueTree> trees, boolean logScale) {
		double res = 0;
		
		for(CliqueTree tree : trees) {
			if(!tree.isCalibrated()) {
				tree.calibrateCliqueTree();
			}
			res += tree.probabilityOfEvidenceMarginalizedOverMissingValues(true);
		}
		
		if(logScale) {
			return res;
		} else {
			return Math.exp(res);
		}
	}
	
	public static void randomizeNetworkWeights(TableBayesianNetwork network) {
		for(TableNode node : network.nodes()) {
			//for each node randomize the weights
			TableConditionalProbabilityDistribution cpd = node.cpd();
			for(Row row : cpd.getFactor().rows()) {
				row.setValue(randService.getNextRandomNumber());
			}
			//renormalize
			cpd.normalize();
		}
	}
	
	
	/**
	 * Learn network weights from missing data
	 * @param network -- table Bayesian network to maximize likelihood for
	 * @param instances -- training instances
	 */
	public static void expectationMaximizationAlgorithm(TableBayesianNetwork network, List<DiscreteInstance> instances) {
		expectationMaximizationAlgorithm(network,instances,100,0.5, UniformPriorSufficientStatistics.create());
	}
	
	/**
	 * Learn network weights from missing data
	 * @param network -- table Bayesian network to maximize likelihood for
	 * @param instances -- training instances
	 * @param prior -- the prior knowledge to add to the sufficient statistics
	 */
	public static void expectationMaximizationAlgorithm(TableBayesianNetwork network, List<DiscreteInstance> instances,PriorSufficientStatisitcs prior) {
		expectationMaximizationAlgorithm(network,instances,100,0.5, prior);
	}
	
	/**
	 * Learn network weights from missing data
	 * @param network -- table Bayesian network to maximize likelihood for
	 * @param instances -- training instances
	 * @param maxIter -- maximum number of iteratations
	 * @param minConvergence -- minimum convergence criteria
	 * @param prior -- the prior knowledge to add to the sufficient statistics
	 */
	public static void expectationMaximizationAlgorithm(TableBayesianNetwork network, List<DiscreteInstance> instances, int maxIter, double minConvergence, PriorSufficientStatisitcs prior) {
		
		
		double oldLikelihood = Double.NEGATIVE_INFINITY;
		double currentLikelihood = Double.NEGATIVE_INFINITY;
		
		List<DiscreteVariable> eliminationOrder = greedyVariableEliminationOrder(network, new MinNeighborsEvaluationMetric<DiscreteVariable>());
		
		for(int i = 0; i < maxIter; i++) {
			
			List<CliqueTree> trees = new LinkedList<CliqueTree>();
			for(DiscreteInstance instance : instances) {
				CliqueTree tree = CliqueTree.createFromEvidenceAndOrder(network, instance.evidence(), eliminationOrder);
				tree.calibrateCliqueTree();
				trees.add(tree);
			}
			oldLikelihood = currentLikelihood;
			currentLikelihood = likelihood(trees,true);
			
			System.err.println("Iteration: " + i);
			System.err.println("Old likelihood: " + oldLikelihood);
			System.err.println("Current log likelihood: " + currentLikelihood);
			
			//check for error state
			if(oldLikelihood - currentLikelihood > 0.001) {
				throw new IllegalStateException("Error new log likelihood > than old");
			}
			
			//check for convergence
			if(i >= 2 && Math.abs(oldLikelihood - currentLikelihood) < minConvergence) {
				break;
			}
			
			//compute sufficient statistics
			for(TableNode node : network.nodes()) {
				Map<Row,Double> stats = node.cpd().computeSufficientStatisticsMissingData(trees);
				
				/*
				 * add prior bias to results
				 */
				prior.addPrior(node, stats);
				
				node.cpd().maximumLikelihoodEstimation(stats);
			}
			
			
			
		}
	}
	
	private static DiscreteValue getValueByVariable(DiscreteVariable parent, Row row) {
		for(DiscreteVariableValue varVal : row.variableValueSet()) {
			if(varVal.variable().equals(parent)) {
				return varVal.value();
			}
		}
		
		return null;
	}
	
	
	private static Row getFirstRowByVariableValue(DiscreteVariableValue variableValue, List<Row> rows) {
		for(Row row : rows) {
			if(row.hasDiscreteVariableValue(variableValue)) {
				return row;
			}
		}
		return null;
	}
	
	public static JSON_TableBayesianNetwork toJSON(TableBayesianNetwork network) {
		JSON_TableBayesianNetwork jsonNetwork = new JSON_TableBayesianNetwork();
		jsonNetwork.name = network.name();
		
		for(TableNode tableNode : network.nodes()) {
			JSON_Node jsonNode = new JSON_Node();
			jsonNode.name = tableNode.name();
			
			//add parents
			List<TableNode> parents = new ArrayList<TableNode>(tableNode.parents().size());
			parents.addAll(tableNode.parents());
			for(TableNode parNode : parents) {
				jsonNode.parents.add(parNode.name());
			}
			
			//create cpd
			jsonNode.cpd = new JSON_CPD();
			
			//add columns
			DiscreteVariable variable = tableNode.cpd().getUnconditionedVariable();
			List<DiscreteValue> sortedValues = variable.valuesSorted();
			for(DiscreteValue value : sortedValues) {
				jsonNode.cpd.columns.add(value.getName());
			}
			
			//add rows to json_cpd
			HashMap<String,List<Row>> rowBucketByParentCombo = tableNode.cpd().createBucketsOfRows();
			List<String> rowOrder = new ArrayList<String>(rowBucketByParentCombo.keySet().size());
			rowOrder.addAll(rowBucketByParentCombo.keySet());
			Collections.sort(rowOrder);
			for(String rowId : rowOrder) {
				List<Row> tableRows = rowBucketByParentCombo.get(rowId);
				JSON_CPD_Row jsonRow = new JSON_CPD_Row();
				
				//add labels

				//labels same order as parents
				//since the rows are organized for a particular combination of parental values, each row will have the same parent values
				//the unconditioned variable will have different values for each row organized by the rowId (combination of parent values)
				for(TableNode parent : parents) {
					DiscreteValue val = getValueByVariable(parent.variable(),tableRows.get(0));
					if(val == null) {
						throw new IllegalStateException(parent.variable() + " not found in " + tableRows.get(0) + " for node " + tableNode);
					}
					jsonRow.labels.add(val.getName());
			
				}
				
				//add values in same order as the columns
				for(DiscreteValue var : sortedValues) {
					DiscreteVariableValue varVal = variable.getVariableValueByName(var.getName());
					Row row = getFirstRowByVariableValue(varVal,tableRows);
					if(row == null) {
						throw new IllegalStateException("No row found for "+ varVal + " in bucket " + rowId);
					}
					
					jsonRow.values.add(row.getValue());
				}
				
				// add json row to cpd
				jsonNode.cpd.rows.add(jsonRow);
			}
			
			//add jsonNode to json network
			
			jsonNetwork.nodes.add(jsonNode);
		}
		return jsonNetwork;
	}
}
