build:
	@echo "Compiling (with dependencies) ..."
	@mvn clean compile assembly:single

run:
	@java -cp target/chatty-*-jar-with-dependencies.jar com.chrisrx.chatty.App
