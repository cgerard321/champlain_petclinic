package datalayer

import (
	"context"
	"database/sql"
	"time"
)

type FileLinkRepo struct {
	db *sql.DB
}

func NewFileLinkRepo(db *sql.DB) *FileLinkRepo {
	return &FileLinkRepo{
		db: db,
	}
}

func (r *FileLinkRepo) GetFileLink(fileId string) string {
	var url string
	err := r.db.QueryRow("SELECT url FROM files WHERE fileId = ?", fileId).Scan(&url)
	if err != nil {
		return ""
	}
	return url
}

func (r *FileLinkRepo) AddFileLink(fileId string, url string) error { //not sure about what these should return
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	_, err := r.db.ExecContext(ctx, "INSERT INTO files (fileId, url) VALUES (?, ?)", fileId, url)
	if err != nil {
		err.Error()
	}
	return nil
}

func (r *FileLinkRepo) UpdateFileLink(fileId string, url string) error { //not sure about what these should return
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	_, err := r.db.ExecContext(ctx, "UPDATE files SET url = ? WHERE fileId = ?", url, fileId)
	if err != nil {
		err.Error()
	}
	return nil
}

func (r *FileLinkRepo) DeleteFileLink(fileId string) error { //not sure about what these should return
	ctx, cancel := context.WithTimeout(context.Background(), 5*time.Second)
	defer cancel()
	_, err := r.db.ExecContext(ctx, "DELETE FROM files WHERE fileId = ?", fileId)
	if err != nil {
		err.Error()
	}
	return nil
}
