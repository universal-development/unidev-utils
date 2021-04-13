//usr/bin/env go run "$0" "$@"; exit "$?"

// clean text files
// input=clean_text_file_lines_test.txt output=clean_text_file_lines_output.txt ./clean_text_file_lines.go

package main

import (
	"bufio"
	"fmt"
	"log"
	"os"
	"strings"
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

	f, err := os.Open(inputFile)

	if err != nil {
		log.Fatal(err)
	}

	defer f.Close()

	log.Print("Loading file lines")
	totalLines := 0
	blankLines := 0

	uniqueLines := make(map[string]bool)

	scanner := bufio.NewScanner(f)
	for scanner.Scan() {
		totalLines++
		line := scanner.Text()
		line = strings.ReplaceAll(line, " ", "")
		if len(line) == 0 {
			blankLines++
			continue
		}
		uniqueLines[line] = true
	}

	log.Printf("Total lines : %+v", totalLines)
	log.Printf("Blank lines : %+v", blankLines)
	log.Printf("Saving results in %+v", outputFile)

	output, err := os.Create(outputFile)
	if err != nil {
		log.Fatal(err)
	}
	defer output.Close()

	writer := bufio.NewWriter(output)
	for k := range uniqueLines {
		writer.WriteString(fmt.Sprintf("%s\n", k))
	}
	flush_err := writer.Flush()
	if flush_err != nil {
		log.Fatal(flush_err)
	}
}
