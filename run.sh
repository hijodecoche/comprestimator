#! /bin/sh

if [ ! -e skip_list.txt ]; then
  echo /proc >> skip_list.txt
  echo /dev >> skip_list.txt
  echo /sys >> skip_list.txt
  echo /snap >> skip_list.txt
  echo /run >> skip_list.txt
fi

java -Xmx6g -jar comprestimator.jar
