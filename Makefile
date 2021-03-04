.PHONY: clear compile run

all: compile clear run

clear:
	clear

compile:
	./gradlew proprietaryJar

run:
	java -jar releases/BetterPlayer-1.0-PROPRIETARY.jar
