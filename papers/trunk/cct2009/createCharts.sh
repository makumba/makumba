#!/bin/bash

# add external jars to classpath
for i in ../java-tools/lib/*.jar; do
  CP=$CP:$i
done

java -cp $CP:../java-tools/bin ChartGenerator figures/

