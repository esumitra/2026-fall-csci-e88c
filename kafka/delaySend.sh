#!/bin/bash
infile=${1?specify input file}
broker=${2?specify broker, with port}
topic=${3?specify topic}
idx=${4?specify key index}
delay=${5?specify delay}
while read -r line; do
  key=`echo "$line" | awk -v idx=$idx -F, '{print $idx}'`
  echo "$key=$line"
  echo "$line" | kcat -b $broker -t $topic -k $key -P
  sleep $delay  
done < $infile
