//usr/bin/env go run "$0" "$@"; exit "$?"

// clean text files
// input=clean_text_file_lines_test.txt output=clean_text_file_lines_output.txt ./clean_text_file_lines.go

package main

import (
	"log"
	"os"
)

func main() {
	inputFile := os.Getenv("input")
	outputFile := os.Getenv("output")
	if inputFile == "" {
		log.Fatal("Missing input variable")
	}
	if outputFile == "" {
		log.Fatal("Missing output variable")
	}

}
