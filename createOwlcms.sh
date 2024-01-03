#!/bin/bash -x
export FLY_API_TOKEN=$(flyctl tokens create deploy)
export FLY_APP_root=$FLY_APP

# create owlcms and set secrets.  don't deploy because no database yet.
rm fly.toml
export OPTIONS="--yes --region=$REGION -i owlcms/owlcms:$VERSION --ha=false --vm-size shared-cpu-2x"
echo "N" | flyctl launch --no-deploy --name $FLY_APP $OPTIONS --org personal
fly secrets set OWLCMS_UPDATEKEY="$FLY_API_TOKEN" --app $FLY_APP

# create database and connect to owlcms. 
export FLY_APP=${FLY_APP_root}-db
flyctl postgres create --name $FLY_APP --initial-cluster-size 1 --vm-size shared-cpu-1x --volume-size 1 --region=$REGION --org personal
flyctl postgres attach $FLY_APP --app $FLY_APP_root --yes

# launch owlcms
export FLY_APP=$FLY_APP_root
flyctl deploy $OPTIONS
