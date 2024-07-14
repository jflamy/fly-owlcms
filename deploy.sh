#!/bin/bash -
fly deploy . --app owlcms-cl --config owlcms-cl.toml --ha=false --image-label $1