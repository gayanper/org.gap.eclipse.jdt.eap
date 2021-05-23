#!/bin/bash
function cherry_pick {
    git fetch $1 $2
    if [[ $? -ne 0 ]]; then
        echo "Couldn't fetch gerrit: $1 $2"
        exit 100
    fi       

    git cherry-pick FETCH_HEAD
    if [[ $? -ne 0 ]]; then
        echo "Detected a conflict while applying: $1 $2, resetting repository."
        git reset --hard
        exit 100
    fi       
}

# script start here
cd ./modules

echo "Applying JDT.Core gerrits"
cd ./eclipse.jdt.core

cherry_pick "https://git.eclipse.org/r/jdt/eclipse.jdt.core" "refs/changes/16/178816/18"

cherry_pick "https://git.eclipse.org/r/jdt/eclipse.jdt.core" "refs/changes/43/180143/7"

cherry_pick "https://git.eclipse.org/r/jdt/eclipse.jdt.core" "refs/changes/45/180745/1"

cherry_pick "https://git.eclipse.org/r/jdt/eclipse.jdt.core" "refs/changes/48/176748/6"

cd ../

echo "Applying JDT.Debug gerrits"
cd ./eclipse.jdt.debug

cherry_pick "https://git.eclipse.org/r/jdt/eclipse.jdt.debug" "refs/changes/46/180746/1"

cd ../../
