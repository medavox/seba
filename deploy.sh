#!/bin/bash -x

branch=$(git branch --show-current)
git stash
if [[ $branch == "main" ]] ; then
  ./gradlew browserProductionWebpack
else
  git checkout main
fi

branch=$(git branch --show-current)
if [[ $branch == "main" ]] ; then
  ./gradlew browserProductionWebpack
else
  echo "couldn't checkout main branch. exiting deploy.sh."
  exit 1
fi

git checkout site
branch=$(git branch --show-current)
if [[ $branch != "site" ]] ; then
  echo "couldn't checkout site branch. exiting deploy.sh."
  exit 1
fi

cp -f build/distributions/* .
git add index.html style.css seba.js seba.js.map
git commit -m "deploy site using script"
git push hub
git checkout main
git stash pop