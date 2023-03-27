#!/usr/bin/env python3

        # <item
        #     android:id="@+id/action_CONUS_G17"
        #     android:title="@string/pacus_goes_west"
        #     app:showAsAction="never" />

from typing import List

lines: List[str] = list(open("raw.txt"))
for line in lines:
    sector: str = line.strip().strip(",").strip('"').replace("-", "_")
    fullName: str = sector.replace("-", " ").replace("_", " ").title()
    print(" " * 8 + "<item")
    print(" " * 12 + "android:id=\"@+id/action_" + sector + "\"")
    print(" " * 12 + "android:title=\"" + fullName + "\"")
    print(" " * 12 + "app:showAsAction=\"never\" />")
    print()