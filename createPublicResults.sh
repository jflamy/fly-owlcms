#!/bin/bash -x

# create publicresults, don't deploy. set secrets then deploy
rm -f fly.toml
tmpfile=$(mktemp)
envsubst < publicresults.toml > $tmpfile

export OPTIONS="--yes --ha=false --vm-size shared-cpu-2x"
flyctl deploy $OPTIONS --config $tmpfile
rm $tmpfile

