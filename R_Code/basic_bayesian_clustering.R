#########
# Three random variables
#  C = class ~ Bernoulli(p=0.3);
#  X1 = continuous variables ~ Normal(4 + 5C,1)
#  X2 = binomial variable ~ p(X2|C=0) ~ Bernoulli(p=0.3) and p(X2|C=1) ~ Bernoulli(p=0.6)
#########

#forward sampling
ns = 200;
nsim = 1000;
p_c = 0.3;

p_x2 = function(c,p_c_0=0.3,p_c_1=0.6) { ifelse(c==0,p_c_0,p_c_1) }
r_x2 = function(c,p_c_0=0.3,p_c_1=0.6) {
	sapply(c,FUN=function(x) {
		rbinom(1,1,p_x2(x,p_c_0,p_c_1))
	})
}


p_x1_c = function(x,c,beta0=4,beta1=5) {
	dnorm(x,beta0  + beta1 *c,1)
}

r_x1_c = function(ns,c,beta0=4,beta1=5) {
	rnorm(ns,beta0 + beta1*c);
}

p_x2_c = function(x,c) {
	mat = matrix(c(x,c),byrow=F,ncol=2);
	#matrix of columns of x,c
	apply(mat,MARGIN=1,FUN=function(z) {
			ifelse(z[1] == 1, p_x2(z[2]), 1 - p_x2(z[2]))			
	})
}

c = rbinom(ns,1,p_c);
x1 = r_x1_c(ns)
x2 = r_x2(c);

prob_c_given_x1_x2 = function(x1_s,x2_s) {
	
	x2 = rep(x2_s,nsim)
	x1 = rep(x1_s,nsim)
	c = rbinom(nsim,1,p_c)
	
	w = p_x1_c(x1,c) * p_x2_c(x2,c);
	
	p_c_1 = sum(w[c==1]) / sum(w)
	p_c_0 = sum(w[c==0]) / sum(w)
	
	return(list(p_c_0 = p_c_0, p_c_1=p_c_1))
}

#just compute from mass and densitity function
#just like naive bayes classifier
prob_c_given_x1_x2_simple = function(x1_s,x2_s) {
	p_c_1_joint = p_x1_c(x1_s,1)*p_x2_c(x2_s,1);
	p_c_0_joint = p_x1_c(x1_s,0)*p_x2_c(x2_s,0);
	
	tot = p_c_1_joint + p_c_0_joint;
	p_c_1 = p_c_1_joint / tot
	p_c_0 = p_c_0_joint / tot
	
	return(list(p_c_0 = p_c_0, p_c_1=p_c_1))
}
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


#test inference
mat = matrix( c(c,rep(0,3*ns)),ncol=4);
for(i in 1:ns) {
	mat[i,2] = prob_c_given_x1_x2( x1[i], x2[i] )$p_c_1
	mat[i,3] = prob_c_given_x1_x2_simple( x1[i], x2[i] )$p_c_1
}
#mark correct and incorrect samples
mat[,4] = mat[,2] > 0.5

mat

#acc
sum(mat[,1] == mat[,4]) / dim(mat)[1]

#show errors
mat[mat[,4] != mat[,1],]


####
# em algorithm
####

#all the x1s are observered
expected_x1 = mean(x1);
expected_c = 0;
expected_c_2 = 0;
expected_x1_c = 0;
c_0_count_0 = 0;
c_0_count_1 = 0;
c_1_count_0 = 0;
c_1_count_1 = 0;

beta0 = 3;
beta1 = 3;
for(i in 1:ns) {
	
	prob = prob_c_given_x1_x2_simple(x1[i],x2[i]);
	p_c_0 = prob$p_c_0;
	p_c_1 = prob$p_c_1;
	
	#compute ess for x1
	#no need to use c_
	expected_c = expected_c + p_c_1 * 1;
	#no difference for binary random variables
	expected_c_2 = expected_c + p_c_1 * 1^2;
	
	expected_x1_c = expected_x1_c + p_c_1 * (x1[i] * 1);
	
	#compute ess for x2
	if(x2[i] == 0) {
		c_0_count_0 = c_0_count_0 + p_c_0;
		c_1_count_0 = c_1_count_0 + p_c_1;
	} else {
		c_0_count_0 = c_0_count_0 + p_c_0;
		c_1_count_0 = c_1_count_0 + p_c_1;
	}
	
}




