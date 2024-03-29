#!/bin/bash -x

# it is acceptable to only have publicresults
rm -f fly.toml
fly secrets set OWLCMS_UPDATEKEY="$SECRET" --app $FLY_APP_OWLCMS
fly secrets set OWLCMS_REMOTE="https://$FLY_APP_PUBLICRESULTS.fly.dev"
tmpfile=$(mktemp)
envsubst < publicresults.toml > $tmpfile
flyctl deploy $OPTIONS --config $tmpfile
rm $tmpfile

# set secrets then deploy
export OPTIONS="--yes --region=$REGION -i owlcms/publicresults:$VERSION --ha=false --vm-size shared-cpu-2x"
fly secrets set OWLCMS_UPDATEKEY="$SECRET" --app $FLY_APP_PUBLICRESULTS
tmpfile=$(mktemp)
envsubst < owlcms.toml > $tmpfile
flyctl deploy $OPTIONS --config $tmpfile
rm $tmpfile

