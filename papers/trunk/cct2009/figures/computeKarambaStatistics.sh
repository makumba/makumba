#!/bin/sh
# author: rudi
# script to compute statistics from karamba checkouts.
# assumes one directory named 200x for each year (use getYearlyCVSCheckouts.sh to create)
#  and one directory cvs-complete as the output dir of cvsstat.

# find all files of type java, jsp, MDDs and script, excluding some that are not relevant for ITD

findTotals="find -type f -regex '.*\.\(java\|jsp\|mdd\|idd\|js\)' | grep -v ./karamba/public_html/committees/ | grep -v 'karamba/public_html/WEB-INF/classes/com/ecyrd/jspwiki/' | grep -v './karamba/public_html/layout/scripts/'"

# header
echo -e "Year\t#Files\tLOC\tMDDs\tJSP\tBL\t(=Java-\tWiki)\tCommits";

for i in 200?;  do 
	cd $i;
	year=$(($i-1))
	mddCount=`find . -iname "*.?dd" | wc -l`;
	jspCount=`find . -name "*.jsp" | grep -v ./karamba/public_html/committees/ | wc -l`;
	javaCount=`find . -iname "*.java" | grep -v ./karamba/public_html/committees/ | wc -l`;
	jspWikiCount=`find -iname "*.java" | grep "karamba/public_html/WEB-INF/classes/com/ecyrd/jspwiki/" | wc -l`
	blCount=$(($javaCount-$jspWikiCount))
	totalCount=`eval $findTotals | wc -l`

	# lines of code
	totalLineCount=`eval $findTotals | xargs cat | wc -l`
	commits="N/A"

	# get cvs commit stats for this year
	if [ "$year" != "2002" ]; then # only available from Jan 2003 on..
		commits=0;
		# this is a bit sucky to have two loops.. but well..
		for ((j=1;j<=9;j+=1)); do
			#ls -l "../cvs-complete/$year-0$j.html";
			month=`grep -A 1 "Number of Commits" "../cvs-complete/$year-0$j.html" | grep -v "Number of" | grep -v "\-\-" | sed 's/<dd>//' | sed 's/<\/dd>//' | sed 's/ //g'`; 
			commits=$(($commits+$month));
		done
		for ((j=10;j<=12;j+=1)); do
			month=`grep -A 1 "Number of Commits" "../cvs-complete/$year-$j.html" | grep -v "Number of" | grep -v "\-\-" | sed 's/<dd>//' | sed 's/<\/dd>//' | sed 's/ //g'`; 
			commits=$(($commits+$month));
		done
	fi
	
	# print stats, construct latex table
	echo -e "$year\t$totalCount\t$totalLineCount\t$mddCount\t$jspCount\t$blCount\t$javaCount\t$jspWikiCount\t$commits";
        latex="$latex\t\t$year \t& $mddCount\t& $jspCount\t& $blCount\t& $totalCount\t\t& $totalLineCount \t& $commits\t \\\\\\\\ \n"
	latex="$latex\t\t\hline\n"
	cd ..;
done

# finish latex table
echo -e "\n\nAs Latex table\n\n"

echo -e "\t\tYear\t& MDDs\t& JSP\t& BL\t& \# files\t& LOC\t\t& Commits\t \\\\\\\\";
echo -e "\t\t\hline"
echo -e "\t\t\hline"
echo -e "$latex"

