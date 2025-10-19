package repository

import (
	"database/sql"
	"files-service/internal/domain"
)

type FileInfoRepository struct {
	db *sql.DB
}

func NewFileInfoRepo(db *sql.DB) *FileInfoRepository {
	return &FileInfoRepository{
		db: db,
	}
}

func (r *FileInfoRepository) GetFileInfo(fileId string) *domain.FileInfo {
	var fileInfo domain.FileInfo
	err := r.db.QueryRow("SELECT fileId, fileName, fileType FROM files WHERE fileId = ?", fileId).Scan(&fileInfo.FileId, &fileInfo.FileName, &fileInfo.FileType)
	if err != nil {
		return nil
	}
	return &fileInfo
}

func (r *FileInfoRepository) AddFileInfo(fileInfo *domain.FileInfo) error {
	_, err := r.db.Exec("insert into files (fileId, fileName, fileType) values (?, ?, ?)", fileInfo.FileId, fileInfo.FileName, fileInfo.FileType)
	return err
}

func (r *FileInfoRepository) DeleteFileInfo(fileId string) error {
	_, err := r.db.Exec("delete from files WHERE fileId = ?", fileId)
	return err
}

func (r *FileInfoRepository) ExistsById(fileId string) (bool, error) {
	var count int
	err := r.db.QueryRow("SELECT COUNT(1) FROM files WHERE fileId = ?", fileId).Scan(&count)
	if err != nil {
		return false, err
	}
	return count > 0, nil
}
