#!/bin/bash -x
export FLY_APP_root=owlcms-next
export VERSION=stable
export REGION=yyz

export FLY_ACCESS_TOKEN=
#export FLY_API_TOKEN=$(flyctl tokens create deploy)

fly apps delete $FLY_APP_root --yes
fly apps delete ${FLY_APP_root}-db --yes
fly apps delete ${FLY_APP_root}-results --yes

# create owlcms don't deploy because no database yet.
export FLY_APP=$FLY_APP_root
rm fly.toml
export OPTIONS="--yes --region=$REGION -i owlcms/owlcms:$VERSION --ha=false --vm-size shared-cpu-2x"
echo "N" | flyctl launch --no-deploy --name $FLY_APP --org personal $OPTIONS

# create database and connect to owlcms. 
export FLY_APP=${FLY_APP_root}-db
flyctl postgres create --name $FLY_APP --initial-cluster-size 1 --vm-size shared-cpu-1x --volume-size 1 --region=$REGION --org personal
flyctl postgres attach $FLY_APP --app $FLY_APP_root --yes

# launch owlcms
export FLY_APP=$FLY_APP_root
tmpfile=$(mktemp)
envsubst < owlcms.toml > $tmpfile
cat $tmpfile
flyctl deploy $OPTIONS --config $tmpfile
rm $tmpfile

# create publicresults, don't deploy. set secrets then deploy
export FLY_APP="$FLY_APP_root-results"
rm fly.toml
export OPTIONS="--yes --region=$REGION -i owlcms/publicresults:$VERSION --ha=false --vm-size shared-cpu-2x"
echo "N" | flyctl launch --no-deploy --name $FLY_APP --org personal $OPTIONS
tmpfile=$(mktemp)
envsubst < publicresults.toml > $tmpfile
flyctl deploy $OPTIONS --config $tmpfile
rm $tmpfile