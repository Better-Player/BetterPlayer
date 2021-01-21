#.PHONY: create-jni-binding-headers

JAVAC=/usr/bin/javac
JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64


LIBS=			libavutil						\
				libswresample					\
				samplerate

SRC_FILES=		src/BetterPlayerSpeech.cpp

INCLUDE=		-I.								\
				-I.headers						\
				-I$(JAVA_HOME)/include 			\
				-I$(JAVA_HOME)/include/linux 	\
				-I/usr/include

CXXFLAGS := $(shell pkg-config --cflags $(LIBS))
LDLIBS := $(shell pkg-config --libs $(LIBS))

build:
	mkdir build

create-jni-binding-headers:
	$(JAVAC) -h headers/ ../java/src/main/java/nl/thedutchmc/betterplayer/natives/*.java
	rm ../java/src/main/java/nl/thedutchmc/betterplayer/natives/*.class

build/libdeepspeech.o: build create-jni-binding-headers
	g++ -std=c++11 -fPIC $(INCLUDE) $(CXXFLAGS) $(LDLIBS) -o build/betterplayer.o -c src/BetterPlayerSpeech.cpp

build/libdeepspeech.so: build/libdeepspeech.o
	g++ -std=c++11 -shared -fPIC -o build/libbetterplayer.so build/betterplayer.o -lc
