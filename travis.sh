#!/bin/bash
set -euo pipefail

#
# Reference: https://github.com/SonarSource/sonarqube/blob/master/travis.sh
#

SONAR_HOST_URL=https://sonarcloud.io/

case "$TARGET" in

BUILD)

  # Minimal Maven settings
  export MAVEN_OPTS="-Xmx1G -Xms128m"
  MAVEN_ARGS="-T 1C -Dmaven.test.redirectTestOutputToFile=false -Dsurefire.useFile=false -B -e -V -Dtests.es.logger.level=WARN"

  if [ "$TRAVIS_BRANCH" == "master" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
    echo 'Build and analyze master'

    # Fetch all commit history so that SonarQube has exact blame information
    # for issue auto-assignment
    # This command can fail with "fatal: --unshallow on a complete repository does not make sense"
    # if there are not enough commits in the Git repository (even if Travis executed git clone --depth 50).
    # For this reason errors are ignored with "|| true"
    git fetch --unshallow || true

    mvn org.jacoco:jacoco-maven-plugin:prepare-agent package $MAVEN_ARGS 
        
    mvn sonar:sonar \
          -Dsonar.host.url=$SONAR_HOST_URL \
          -Dsonar.login=$SONAR_TOKEN_PITEST 
          

  elif [[ "$TRAVIS_BRANCH" == "branch-"* ]] && [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
    echo 'Build release branch'

    mvn org.jacoco:jacoco-maven-plugin:prepare-agent package $MAVEN_ARGS 

    mvn sonar:sonar \
        -Dsonar.host.url=$SONAR_HOST_URL \
        -Dsonar.login=$SONAR_TOKEN_PITEST \
        -Dsonar.branch.name=$TRAVIS_BRANCH

  elif [ "$TRAVIS_PULL_REQUEST" != "false" ] && [ -n "${GITHUB_TOKEN:-}" ]; then
    echo 'Build and analyze internal pull request'

    mvn org.jacoco:jacoco-maven-plugin:prepare-agent package \
        $MAVEN_ARGS \
        -Dsource.skip=true 

    # analysis to decorate GitHub pull request
    # (need support of standard analysis mode in GH plugin)
    mvn sonar:sonar \
        -Dsonar.host.url=$SONAR_HOST_URL \
        -Dsonar.login=$SONAR_TOKEN_PITEST \
        -Dsonar.analysis.mode=preview \
        -Dsonar.github.pullRequest=$TRAVIS_PULL_REQUEST \
        -Dsonar.github.repository=$TRAVIS_REPO_SLUG \
        -Dsonar.github.oauth=$GITHUB_TOKEN_PITEST

    if [ "$TRAVIS_BRANCH" == "master" ]; then
      # analysis of short-living branch based on another short-living branch
      # is currently not supported
      mvn sonar:sonar \
          -Dsonar.host.url=$SONAR_HOST_URL \
          -Dsonar.login=$SONAR_TOKEN_PITEST \
          -Dsonar.branch.name=$TRAVIS_PULL_REQUEST_BRANCH \
          -Dsonar.branch.target=$TRAVIS_BRANCH
    fi
  else
    echo 'Build feature branch or external pull request'

    mvn package $MAVEN_ARGS -Dsource.skip=true
  fi


*)
  echo "Unexpected TARGET value: $TARGET"
  exit 1
  ;;

esac
