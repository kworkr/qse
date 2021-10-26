#!/bin/bash
set -e

export DATA_DIR="${PWD}/data"
export NEO4J_HOME=${PWD}/neo4j-server
export NEO4J_IMPORT="${NEO4J_HOME}/import"
mkdir -p -v "${DATA_DIR}"
mkdir -p -v "${NEO4J_IMPORT}"

if [ "$#" -ne 1 ]; then
    echo "Illegal number of parameters."
    exit 1
fi

if [ -d $DATA_DIR ]
then
    echo "Downloading files..."
    rm -v ${DATA_DIR}/*.* || true
    while read -r line; do
        [[ "$line" =~ ^#.*$ ]] && continue
        wget -P ${DATA_DIR}/ $line
        bzip2 -dk ${DATA_DIR}/${line##*/}
        filename=$(basename -- "${DATA_DIR}/${line##*/}")
        filename="${filename%.*}"
        # Remove corrupted chars and lines
        iconv -f utf-8 -t ascii -c "${DATA_DIR}/${filename}" | grep -E '^<(https?|ftp|file)://[-A-Za-z0-9\+&@#/%?=~_|!:,.;]*[A-Za-z0-9\+&@#/%?=~_|]>\W<' |   grep -v 'xn--b1aew' > ${DATA_DIR}/clean-${filename}
        rm -v "${DATA_DIR}/${filename}"
        split -l 5000000 --numeric-suffixes ${DATA_DIR}/clean-${filename} ${NEO4J_IMPORT}/part-${filename}
    done < $1
    chmod -R 777 ${NEO4J_IMPORT}
else
    echo "No destination folder ${DATA_DIR}"
fi