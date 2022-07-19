#!/usr/bin/env python3
#
# find files that don't have contiguous import statements
#

from glob import glob
from typing import List

files: List[str] = glob("app/src/main/java/joshuatee/wx/*/*.kt") + glob("app/src/main/java/joshuatee/wx/*.kt")
for file in files:
    lines: List[str] = list(open(file))
    firstImportFound: bool = False
    for index, line in enumerate(lines):
        if "joshua.tee@gmail.com" in line:
            print(line.strip(), file.split("/")[-1])
        if line.startswith("import") and lines[index - 1] == "\n" and firstImportFound:
            print(file.split("/")[-1])
        if line.startswith("import"):
            firstImportFound = True
