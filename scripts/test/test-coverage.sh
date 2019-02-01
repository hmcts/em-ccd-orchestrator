#!/usr/bin/env bash

./gradlew clean check jacocoTestReport --info

xdg-open build/reports/jacoco/test/html/index.html
open build/reports/jacoco/test/html/index.html
start "" build/reports/jacoco/test/html/index.html