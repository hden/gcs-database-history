.PHONY: test coverage clean

test:
	@JAVA_HOME=$$(/usr/libexec/java_home -v1.8) lein test

coverage:
	@JAVA_HOME=$$(/usr/libexec/java_home -v1.8) lein with-profile test cloverage

clean:
	@JAVA_HOME=$$(/usr/libexec/java_home -v1.8) lein clean
