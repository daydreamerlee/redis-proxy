repopath = $(shell pwd)

export $(repopath)

build: ## Build the container
	echo $(repopath)
test: 
	docker run -it --rm --name my-maven-project -v "$(repopath)":/usr/src/mymaven -w /usr/src/mymaven maven:3.3-jdk-8 mvn clean test

clean:
	docker run -it --rm --name my-maven-project -v "$(repopath)":/usr/src/mymaven -w /usr/src/mymaven maven:3.3-jdk-8 mvn clean

compile:
	docker run -it --rm --name my-maven-project -v "$(repopath)":/usr/src/mymaven -w /usr/src/mymaven maven:3.3-jdk-8 mvn clean compile

package:
	docker run -it --rm --name my-maven-project -v "$(repopath)":/usr/src/mymaven -w /usr/src/mymaven maven:3.3-jdk-8 mvn clean package
