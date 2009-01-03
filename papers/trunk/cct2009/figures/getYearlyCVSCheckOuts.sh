#!/bin/sh
# author: rudi
# creates in the current directory sub-directories from 2003 to 2009, and does a CVS checkout of Karamba! on the 1st of January for each of them

# do 2003 manually, cause it started a bit after 01-01-2003
mkdir 2003
cd 2003
echo cvs co -D 2003-01-10 karamba
cd ..

# now do 2004 to 2009
for ((i=4;i<=9;i+=1)); do
	mkdir 200$i
	cd 200$i
	echo cvs co -D 200$i-01-01 karamba
	cd ..
done
