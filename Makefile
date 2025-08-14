.PHONY: build

checkstyleMain:
	./gradlew checkstyleMain

checkstyleTest:
	./gradlew checkstyleTest

build:
	./gradlew clean build

test:
	./gradlew test

report:
	./gradlew jacocoTestReport

run:
	./build/install/java-project-99/bin/java-project-99

coverage:
	./gradlew jacocoTestCoverageVerification