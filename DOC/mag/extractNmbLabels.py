#!/usr/bin/env python3
#
# given MAG model html extract codes and labels for use in UtilityModelNcepInterface.kt
#
import re
from typing import Optional

lines: list[str] = list(open("nmb-labels-codes.txt"))

for line in lines:
   if "params_link bluehover" in line:
      matchObj: Optional[re.Match[str]] = re.match(r".*?id='(.*?)'.*?title='(.*?)'.*", line, re.M|re.I) 
      if matchObj:
         # print(f"{matchObj[1]} \"{matchObj[2]}\",")
         # print(f"\"{matchObj[2]}\",")
         print(f"\"{matchObj[1]}\",")
