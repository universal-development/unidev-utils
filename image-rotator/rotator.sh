#!/bin/bash
#    Copyright (c) 2015 Denis O <denis@universal-development.com>
 
#    Licensed under the Apache License, Version 2.0 (the "License");
#    you may not use this file except in compliance with the License.
#    You may obtain a copy of the License at
 
#       http://www.apache.org/licenses/LICENSE-2.0
 
#    Unless required by applicable law or agreed to in writing, software
#    distributed under the License is distributed on an "AS IS" BASIS,
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#    See the License for the specific language governing permissions and
#    limitations under the License.
#
# Script which try to rotate/convert to jpg all files from input directory, output is stored in 'output'

counter=1
for file in input/*
do
	angle=`shuf --input-range 1-10 | head -n 1`
	angle=`expr $angle - 6`
	echo "$file % $angle"
	convert -rotate $angle -format jpg "./$file"  output/$counter.jpg
	((counter++))
done


