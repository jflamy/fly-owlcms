#!/bin/bash -x
export FLY_API_TOKEN=$(flyctl tokens create deploy)

# create publicresults, don't deploy. set secrets then deploy
rm fly.toml

tmpfile=$(mktemp)
envsubst < publicresults.toml > $tmpfile

export OPTIONS="--yes --ha=false --vm-size shared-cpu-2x"
flyctl deploy $OPTIONS --config $tmpfile
rm $tmpfile

