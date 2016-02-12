package org.kidneyomics.bayes;

import java.util.Set;

public interface Factor {
	Set<Variable> scope();
	Factor marinalize(Set<Variable> variables);
	Factor product(Factor factor);
	Factor reduce(Set<VariableValue> variableValues);
}
