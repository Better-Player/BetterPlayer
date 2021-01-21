.PHONY: native gradle

all: clean native

GRADLE ?= ./gradlew

cpp/build/libbetterplayer.so:
	cd cpp; \
		$(MAKE) build/libdeepspeech.so

native: cpp/build/libbetterplayer.so
	cp cpp/build/libbetterplayer.so java/src/main/resources/jni/x86_64/libbetterplayer.so
	cp cpp/build/libbetterplayer.so /tmp/

gradle: native
	cd java; \
		$(GRADLE) shadowJar

clean:
	rm -f java/src/main/resources/jni/x86_64/libbetterplayer.so	
	rm -f cpp/build/*