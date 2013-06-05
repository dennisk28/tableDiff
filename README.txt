
1. Build

Using maven to build the whole system, make sure maven is installed to the system.

run: mvn install or build.bat

2. Run a demo

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
