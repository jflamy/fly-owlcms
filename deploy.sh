#!/bin/bash -
fly deploy . --app owlcms-cloud --config owlcms-cloud.toml --ha=false --image-label $1