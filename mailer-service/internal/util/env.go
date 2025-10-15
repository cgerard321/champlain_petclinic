package util

import (
	"log"
	"os"
)

func Getenv(k string) string {
	v := os.Getenv(k)

	if v == "" {
		log.Fatalf("missing required env %q", k)
	}

	return v
}
