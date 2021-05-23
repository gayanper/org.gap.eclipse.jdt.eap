#!/bin/bash

# preparation
work_dir=$0/../
cd $work_dir
mkdir ./modules/
cd ./modules

# cloning repositories
echo "cloning modules"
git clone https://git.eclipse.org/r/jdt/eclipse.jdt.core --depth=1
git clone https://git.eclipse.org/r/jdt/eclipse.jdt.debug --depth=1

cd ../

