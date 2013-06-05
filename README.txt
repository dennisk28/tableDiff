1. Introduction

TableDiff is a tool to compare two large tabular format text files. Some of the key features:

   a. Use external merge sort algorithm to sort unsorted large files before comparison.
   b. User can specify multiple columns to generate a key for each row.
   c. User can specify columns to compare between two files.
   d. Support absolute and percentage comparison for two double values. The threshhold of the comparison can be configured.
   e. User can implement own key generator and comparison algorithm.
   f. User can use filter class to filter out certain rows before comparison.
   g. Memory print can be as small as 50m no matter how large the files are.
   h. Speed is very fast.
   i. Support escape character to represent delimiter characters. for example, if ; is delimiter, \; will be treated as character ";".

2. Build

Use maven to build the system.

run: mvn install or build.bat

3. Run a demo

Inside test directory you can find:

genSamp.bat: A program to generate two identical files with 1 million records based on random numbers
runDemo.bat: Take the generated files, run sort and then compare.
usage.bat: Display command line parameters

Steps to run the demo:

a. Use genSamp.bat to generate two files t.txt and t1.txt each with 1 million random records
b. Modify one of the files, moving some records, deleting some records, make changes to some of the values
c. Use runDemo.bat to sort the two files and then compare them. A result.txt is generated to show the difference between two compared files.

Any question, please send email to denniskds@yahoo.com.

Cheers!
