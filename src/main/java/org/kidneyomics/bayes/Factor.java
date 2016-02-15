package org.kidneyomics.bayes;

import java.util.Collection;
import java.util.Set;

public interface Factor<T1 extends Variable, T2 extends VariableValue<T1,? extends Value>> {
	Collection<T1> scope();
	Factor<T1,T2> marginalize(Set<T1> variables);
	Factor<T1,T2> product(Factor<T1,T2> factor);
	Factor<T1,T2> reduce(Set<T2> variableValues);
}
