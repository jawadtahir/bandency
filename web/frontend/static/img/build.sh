#!/usr/bin/env bash

watchmedo shell-command --patterns="*.puml" --command='plantuml debs.puml'
