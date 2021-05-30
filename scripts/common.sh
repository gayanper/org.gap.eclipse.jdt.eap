#!/bin/bash
function read_repos {
    read_repos_val=()
    local file_path=$1
    while IFS='=' read -r key value
    do
        read_repos_val+=("$key:$value")
    done < "$file_path"
}

function read_refs {
    read_refs_val=()
    local file_path=$1
    while IFS= read -r ref
    do
        read_refs_val+=("$ref")
    done < "$file_path"
}

function read_key {
    echo ${1%%:*}
}

function read_value {
    echo ${1#*:}
}