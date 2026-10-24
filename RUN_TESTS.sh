#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
sh ./gradlew testDebugUnitTest assembleDebug
