.PHONY: test

test:
	@JAVA_HOME=$$(/usr/libexec/java_home -v1.8) GOOGLE_APPLICATION_CREDENTIALS=$$PWD/gcp_credentials.json lein test
