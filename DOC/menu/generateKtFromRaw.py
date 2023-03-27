#!/usr/bin/env python3

# R.id.action_ca -> getContentBySector("ca")

from typing import List

lines: List[str] = list(open("raw.txt"))
for line in lines:
    sector: str = line.strip().strip(",").strip('"').replace("-", "_")
    print(" " * 8 + "R.id.action_" + sector + " -> getContentBySector(\"" + sector + "\")")
