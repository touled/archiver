# Archiver
Simple Zip archiver. Compresses and extracts files and folders using ZIP compression algorithm.

## Prerequisites

- JRE11
- Maven

## Usage

1. Clone or download repo
2. run `mvn package`
2. make archiver executable `chmod +x ./archiver`

## Archive

To create zip archive add space-delimited list of files and folders and redirect output to a zip file.

Example: 

`./archiver ./file1 ./file2 ./dir1 > archive.zip`

## Extract

To extract files from zip file, pipe zip file output to the archiver.

Example:

`$cat archive.zip > ./archiver`