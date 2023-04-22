#!/usr/bin/env bash
VERSION="1.7.0"
PLUGIN="TouchTypistsCompletionCaddy"
PLUGIN_DIST=".zip"
HOME_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
OLD_PLUGIN=
IDE_LIST=(
#    191
#    192
#    193
#    201
#    202
    203
    211
    212
    213
    221
    222
    223
    231
    232
    233
)

# copy the versioned file to un-versioned one.
cp "./build/distributions/${PLUGIN}-${VERSION}${PLUGIN_DIST}" "./${PLUGIN}${PLUGIN_DIST}"
cp "./build/distributions/${PLUGIN}-${VERSION}${PLUGIN_DIST}" "./dist"

echo updating "/Volumes/Pegasus/Data" for latest "${PLUGIN}${PLUGIN_DIST}"
cp "${PLUGIN}${PLUGIN_DIST}" "/Volumes/Pegasus/Data"

../update-plugin.sh "${HOME_DIR}" "${PLUGIN}" "${PLUGIN_JAR}" "${OLD_PLUGIN}" "${IDE_LIST[@]}"
