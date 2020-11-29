# Archiver
Simple Zip archiver. Compresses and extracts files and folders using ZIP compression algorithm.

## Prerequisites

- JRE 8 or later
- Shell

## Usage

1. Clone or download this repository
2. Run `./mvnw package` to compile and package archiver
2. Make archiver executable `chmod +x ./archiver`

## Archive

To create zip archive add space-delimited list of files and folders and redirect output to a zip file.

Example: 

`$./archiver ./file1 ./file2 ./dir1 > archive.zip`

## Extract

To extract files from zip file, pipe zip file output to archiver. Files and folders will be extracted into the current directory.

Example:

`$cat archive.zip | ./archiver`