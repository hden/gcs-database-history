.PHONY: test

test:
	@JAVA_HOME=$$(/usr/libexec/java_home -v1.8) GOOGLE_APPLICATION_CREDENTIALS=$$PWD/tests-220909-a6a3dd723175.json lein test
