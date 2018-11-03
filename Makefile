.PHONY: test

test:
	@JAVA_HOME=$$(/usr/libexec/java_home -v1.8) lein test
