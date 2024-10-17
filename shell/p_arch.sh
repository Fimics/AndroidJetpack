#!/bin/bash

echo "Current directory is: $(pwd)"
LIBS_DIR="./libAiui/libs"


for ARCH in arm64-v8a
do
    find "${LIBS_DIR}/${ARCH}" -name "*.so" -print0 | while IFS= read -r -d '' file
    do
        echo "Processing $file"
        file "$file"
    done
done
