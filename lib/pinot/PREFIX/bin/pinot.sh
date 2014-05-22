#!/bin/bash

declare -a javaFiles
declare finalJavaFilesString
declare finalJarFilesString
declare -a jarFiles
let javaCounter=0
let jarCounter=0
declare projectName=$1
declare rtJar=$2
declare outputFile=$3
declare rtPath
declare currentJarFile

process_dir() {
    local -a subdirs=()
    # Scan the directory, processing files and collecting subdirs
    for file in "$1"/*; do
        if [[ -f "$file" ]]; then
		if [ ${file##*.} == "java" ] || [ ${file##*.} == "JAVA" ] || [ ${file##*.} == "Java" ]; then
			((javaCounter++))
			javaFiles[$javaCounter]=$file		
			# add the above files to an array..
		elif [ ${file##*.} == "jar" ] || [ ${file##*.} == "JAR" ] || [ ${file##*.} == "Jar" ]; then
			((jarCounter++))
			jarFiles[$jarCounter]=$file
		fi
        elif [[ -d "$file" ]]; then
            subdirs+=("$file")
        fi
    done

    # Now go through the subdirs
    for d in "${subdirs[@]}"; do
        process_dir "$d"
    done
}

clear
#Scan for java & jar files in Project [Path=$1]
process_dir "$1"

#creating a String with all .java files path
echo "..total java files found: " ${#javaFiles[@]}
echo "..creating the java Files String."
for i in "${javaFiles[@]}"
	do
	finalJavaFilesString="$finalJavaFilesString $i"
	done
echo "...Java Files String completed."

#creating the CLASSPATH
echo "..total jar files found: " ${#jarFiles[@]}
for i in "${jarFiles[@]}"
	do
	finalJarFilesString="$finalJarFilesString:$i"
	done
echo "...CLASSPATH String completed."

#find the necessary rt.jar library
#rtPath=$(find ~ -name rt.jar)
#creating the final CLASSPATH
#export CLASSPATH=$rtPath$finalJarFilesString:$CLASSPATH
#export CLASSPATH=../../lib/rt.jar$finalJarFilesString:$CLASSPATH
export CLASSPATH=$2$finalJarFilesString:$CLASSPATH
echo "classpath: "$CLASSPATH 
echo "..CLASSPATH exported."

#Executing pinot and writing results to a given file as a command line parameter{$2}
#cd ~/Applications/pinot/PREFIX/bin/ 
cd lib/pinot/PREFIX/bin/
./pinot $finalJavaFilesString > $3 2>&1
echo "..results saved at file $3"
