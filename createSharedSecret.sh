#!/bin/bash -x
export FLY_API_TOKEN=$(flyctl tokens create deploy)

# set secrets then deploy
rm fly.toml
export OPTIONS="--yes --region=$REGION -i owlcms/publicresults:$VERSION --ha=false --vm-size shared-cpu-2x"
fly secrets set OWLCMS_UPDATEKEY="$SECRET" --app $FLY_APP_PUBLICRESULTS
flyctl deploy $OPTIONS

# it is acceptable to only have publicresults
fly secrets set OWLCMS_UPDATEKEY="$SECRET" --app $FLY_APP_OWLCMS
flyctl deploy $OPTIONS