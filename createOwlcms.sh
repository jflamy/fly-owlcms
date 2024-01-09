#!/bin/bash -x
export FLY_APP_root=$FLY_APP

# create owlcms don't deploy because no database yet.
rm -f fly.toml
flyctl apps create --name $FLY_APP --org personal

# create database and connect to owlcms. 
export FLY_APP=${FLY_APP_root}-db
flyctl postgres create --name $FLY_APP --initial-cluster-size 1 --vm-size shared-cpu-1x --volume-size 1 --region=$REGION --org personal

set +x
echo
echo "***** The next step can take a couple minutes.  Please be patient *****"
set -x
flyctl postgres attach $FLY_APP --app $FLY_APP_root --yes

# deploy owlcms
export FLY_APP=$FLY_APP_root
tmpfile=$(mktemp)
envsubst < owlcms.toml > $tmpfile
flyctl deploy --ha=false --config $tmpfile
rm -f $tmpfile
