#########
# Three random variables
#  C = class ~ Bernoulli(p=0.3);
#  X1 = continuous variables ~ Normal(4 + 5C,1)
#  X2 = binomial variable ~ p(X2|C=0) ~ Bernoulli(p=0.3) and p(X2|C=1) ~ Bernoulli(p=0.6)
#########

#forward sampling
ns = 200;
p_c = 0.3;

p_x2 = function(c) { ifelse(c==0,0.3,0.6) }
r_x2 = function(c) {
	sapply(c,FUN=function(x) {
		rbinom(1,1,p_x2(x))
	})
}

c = rbinom(ns,1,p_c);
x1 = rnorm(ns,4 + 5*c);
x2 = r_x2(c);


# skip sd for this calculation just assume it is one

compute_ml_x1 = function(expected_x1, expected_c, expected_c_2, expected_x1_c) {
	
	#beta_0 + beta_1 * E[C] = E[X1]
	#beta_0 * E[C] + beta1 * E[C * C] = E[X1 * C]
	a = matrix( c(1, expected_c,
		    expected_c, expected_c_2), ncol=2, byrow=TRUE);
	b = c(expected_x1, expected_x1_c);
	solve(a,b)
}

compute_ml_bernoulli = function(count0,count1) {
	c(count0,count1) / (count0 + count1)
}

compute_ml_x2 = function(c_0_count_0, c_0_count_1, c_1_count_0, c_1_count_1) {
	matrix( c(compute_ml_bernoulli(c_0_count_0,c_0_count_1), compute_ml_bernoulli(c_1_count_0,c_1_count_1)), ncol=2, byrow=T)
}

#ex compute_ml_x1

compute_ml_x1(mean(x1), mean(c), mean(c * c), mean(x1 * c))

#ex compute_ml_bernoulli (c)
compute_ml_bernoulli(sum(c==0),sum(c==1))

#ex compute_ml_x2

compute_ml_x2( sum( x2[c==0] == 0  ), sum( x2[c==0] == 1  ), sum( x2[c==1] == 0  ), sum( x2[c==1] == 1  ))