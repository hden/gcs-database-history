.PHONY: test cloverage

test:
	@JAVA_HOME=$$(/usr/libexec/java_home -v1.8) lein test

cloverage:
	@JAVA_HOME=$$(/usr/libexec/java_home -v1.8) lein with-profile test cloverage
