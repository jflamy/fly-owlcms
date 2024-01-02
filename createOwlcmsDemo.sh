#!/bin/bash -x
export FLY_APP_root=owlcms
export VERSION=stable
export REGION=yyz

export FLY_ACCESS_TOKEN=fo1_cTlqK6Rp20TZa3kH-WqGr7XTkyLJdeAi-ZAl96lec8g
export FLY_API_TOKEN=$(flyctl tokens create deploy)

# create owlcms and set secrets.  don't deploy because no database yet.
export FLY_APP=$FLY_APP_root
rm fly.toml
export OPTIONS="--yes --region=$REGION -i owlcms/owlcms:$VERSION --ha=false --vm-size shared-cpu-2x"
echo "N" | flyctl launch --no-deploy --name $FLY_APP $OPTIONS
fly secrets set OWLCMS_UPDATEKEY="$FLY_API_TOKEN" --app $FLY_APP

# create database and connect to owlcms. 
export FLY_APP=${FLY_APP_root}-db
flyctl postgres create --name $FLY_APP --initial-cluster-size 1 --vm-size shared-cpu-1x --volume-size 1 --region=$REGION
flyctl postgres attach $FLY_APP --app $FLY_APP_root --yes

# launch owlcms
export FLY_APP=$FLY_APP_root
flyctl deploy --yes $OPTIONS \
       --env OWLCMS_ENABLEEMBEDDEDMQTT=false --env OWLCMS_INITIALDATA=LARGEGROUP_DEMO --env OWLCMS_PUBLICDEMO=7200 --env OWLCMS_RESETMODE=true 

# create publicresults, don't deploy. set secrets then deploy
export FLY_APP="$FLY_APP_root-results"
rm fly.toml
export OPTIONS="--yes --region=$REGION -i owlcms/publicresults:$VERSION --ha=false --vm-size shared-cpu-2x"
echo "N" | flyctl launch --no-deploy --name $FLY_APP $OPTIONS
fly secrets set OWLCMS_UPDATEKEY="$FLY_API_TOKEN" --app $FLY_APP
flyctl deploy $OPTIONS