package datalayer

import "strings"

type FileInfo struct {
	FileId   string
	FileName string
	FileType string
}

func (f *FileInfo) GetFileBucket() string {
	var bucket string
	parts := strings.Split(f.FileType, "/")
	if parts[0] != "application" {
		bucket = parts[0]
	} else {
		bucket = parts[1]
	}
	return Buckets[bucket]
}

func (f *FileInfo) GetFileLink() string {
	return f.FileId + "." + strings.Split(f.FileType, "/")[1]
}
