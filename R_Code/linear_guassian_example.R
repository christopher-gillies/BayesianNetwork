########
# normal sample
########

r = rnorm(10000)
p_r = dnorm(r)

#average
sum(r * p_r) / sum(p_r)

##############
# X ~ Bernoulli(p = 0.3)
# Y ~ Normal( 5 + 3 * X,1)
##############

n=10000

sample_y_given_x = function(x,n=1,sd=1) {
	rnorm(n,mean=5 + 3 * x,sd=sd);
}


p_y_given_x = function(y,x,sd=1) {
	dnorm(y,mean=5 + 3 * x,sd=sd);
}


######
# Likelihood-weighted particles
######


######
# Given Y = 7
# What is the probability that X?
######

x = rbinom(n,1,0.3);
y = rep(7,n)

w = p_y_given_x( y,x,sd=1)

p_x_1 = sum(w[x==1]) / sum(w)
p_x_0 = sum(w[x==0]) / sum(w)

p_x_1
p_x_0

######
# Given Y = 8
# What is the probability of X?
######

x = rbinom(n,1,0.3);
y = rep(8,n)

w = p_y_given_x( y,x,sd=1)

p_x_1 = sum(w * (x == 1)) / sum(w)
p_x_0 = sum(w * (x == 0)) / sum(w)

p_x_1
p_x_0



##############
# X ~ Normal(0,1)
# Y ~ Normal( 5 + 3 * X,3)
##############


######
# Given Y = 7
# What is the probability that X?
######

x = rnorm(n);
y = rep(7,n)

w = p_y_given_x( y,x)
tot = sum(w);

#probability of x given y
# x = 0.5
# x > 0.4 & x < 0.6
#
p_x_given_y = function(z,width=0.1) {
	sum(w[x > (z - width) & x < (z + width)]) / tot
}

vals_of_x = seq(-3,3,.01)
p = rep(0,length(vals_of_x))
index = 1
for(i in vals_of_x) {
	p[index] = p_x_given_y(i)
	index = index + 1
}

#plot distribution
plot(vals_of_x,p,type="l")


#compute the mode
vals_of_x [which(p == max(p))]

#compute the average
sum(vals_of_x * p) / sum(p)
 
 
##########
# What is the probability that Y = z i.e. z - 0.1 < Y < z + 0.1
#########

 
# simple forward sampling for Bernoulli random variable

x = rbinom(n,1,0.3);
y = sample_y_given_x(x,length(x))

# count the samples that meet the criteria
p_y = function(z,width=0.1) {
	sum(y > z - width & y < z + width) / length(y)
}




vals_of_y = seq(0,12,.01)
p = rep(0,length(vals_of_y))
index = 1
for(i in vals_of_y) {
	p[index] = p_y(i)
	index = index + 1
}

#plot distribution
plot(vals_of_y,p,type="l")



#compute the average
mean(y)
sum(vals_of_y * p) / sum(p)

sum(vals_of_y^2 * p) / sum(p)




###########
# Let us do an example with two random variables
###########



##############
# X1 ~ Bernoulli(p = 0.5)
# X2 ~ Bernoulli(p = 0.5)
# Y ~ Normal( 5 + 3 * X1 + 10*X2,1)
##############

# count the samples that meet the criteria
p_y = function(y,z,width=0.1) {
	sum(y > z - width & y < z + width) / length(y)
}



n=10000

sample_y_given_x = function(x1,x2,n=1,sd=1) {
	rnorm(n,mean=5 + 3 * x1 + 6 * x2,sd=sd);
}


p_y_given_x = function(y,x1,x2,sd=1) {
	dnorm(y,mean=5 + 3 * x1 + 6 * x2,sd=sd);
}


x1 = rbinom(n,1,0.25);
x2 = rbinom(n,1,0.25);

y = sample_y_given_x(x1,x2,n=n)


vals_of_y = seq(0,20,.01)
p = rep(0,length(vals_of_y))
index = 1
for(i in vals_of_y) {
	p[index] = p_y(y,i)
	index = index + 1
}

#plot distribution
plot(vals_of_y,p,type="l")




######
# Likelihood-weighted particles Algorithm 12.2
######


######
# Given Y = 7, X1 = 1
# What is the probability of X2?
######


x2 = rbinom(n,1,0.25);
x1 = rep(1,n)
y = rep(7,n)

w = p_y_given_x( y,x1,x2,sd=1)
w = w * dbinom(x1,1,0.25)

p_x2_1 = sum(w[x2==1]) / sum(w)
p_x2_0 = sum(w[x2==0]) / sum(w)

p_x_1
p_x_0


