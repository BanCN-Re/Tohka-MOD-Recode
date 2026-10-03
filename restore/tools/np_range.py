#!/usr/bin/env python3
"""Dump a line range of a string table (UTF-8 safe), optional filter."""
import sys

def main():
    path = sys.argv[1]
    lo = int(sys.argv[2]); hi = int(sys.argv[3])
    pat = sys.argv[4] if len(sys.argv) > 4 else None
    with open(path, "r", encoding="utf-8", errors="replace") as f:
        for i, line in enumerate(f, 1):
            if i < lo:
                continue
            if i > hi:
                break
            line = line.rstrip("\n").rstrip("\r")
            if pat and pat not in line:
                continue
            print("%6d| %s" % (i, line))

main()
