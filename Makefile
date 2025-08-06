.PHONY: build

checkstyleMain:
	./app/gradlew -p ./app checkstyleMain

checkstyleTest:
	./app/gradlew -p ./app checkstyleTest

build:
	./app/gradlew -p ./app clean build

test:
	./app/gradlew -p ./app test

report:
	./app/gradlew -p ./app jacocoTestReport

run:
	./app/build/install/app/bin/app

coverage:
	./app/gradlew -p ./app jacocoTestCoverageVerification