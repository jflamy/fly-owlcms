#!/bin/bash -x

rm -f fly.toml
tmpfile=$(mktemp)
envsubst < publicresults.toml > $tmpfile

# this deploys publicresults without a requiring a prior creation.
export OPTIONS="--yes --ha=false --vm-size shared-cpu-2x"
flyctl deploy $OPTIONS --config $tmpfile
rm $tmpfile

