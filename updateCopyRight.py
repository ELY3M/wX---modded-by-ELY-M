#!/usr/bin/env python3
#
# update copyright
#
from glob import glob
from typing import List
import argparse

#
# Main
#
parser = argparse.ArgumentParser()
parser.add_argument("-c", "--commit", help="commit changes", action="store_true")
args = parser.parse_args()

files: List[str] = glob("app/src/main/java/joshuatee/wx/*.kt") + glob("app/src/main/java/joshuatee/wx/*/*.kt")

oldCopyright: str = "Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020  joshua.tee@gmail.com"
newCopyright: str = "Copyright 2013, 2014, 2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022  joshua.tee@gmail.com"

for f in files:
    data: str = open(f).read()
    data = data.replace(oldCopyright, newCopyright)
    fh = open(f, "w")
    fh.write(data)
    fh.close()
