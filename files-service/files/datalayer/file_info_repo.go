package datalayer

import (
	"database/sql"
)

type FileLinkRepo struct {
	db *sql.DB
}

func NewFileLinkRepo(db *sql.DB) *FileLinkRepo {
	return &FileLinkRepo{
		db: db,
	}
}

func (r *FileLinkRepo) GetFileInfo(fileId string) *FileInfo {
	var fileInfo FileInfo
	err := r.db.QueryRow("SELECT fileId, url, fileType FROM files WHERE fileId = ?", fileId).Scan(&fileInfo.FileId, &fileInfo.Url, &fileInfo.FileType)
	if err != nil {
		return nil
	}
	return &fileInfo
}
