#!/bin/bash -x
export FLY_API_TOKEN=$(flyctl tokens create deploy)

# create publicresults, don't deploy. set secrets then deploy
rm fly.toml
export OPTIONS="--yes --region=$REGION -i owlcms/publicresults:$VERSION --ha=false --vm-size shared-cpu-2x"
echo "N" | flyctl launch --no-deploy --name $FLY_APP $OPTIONS
fly secrets set OWLCMS_UPDATEKEY="$FLY_API_TOKEN" --app $FLY_APP
flyctl deploy $OPTIONS