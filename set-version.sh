#!/bin/bash
version=$1

if [ -z "$version" ]; then
    echo "Usage:"
    echo "      set-version 1.3.4-SNAPSHOT"
    echo
    exit 1
fi

mvn -Pversion-update,build-individual-bundles org.eclipse.tycho:tycho-versions-plugin:2.5.0:set-version -DnewVersion=$version