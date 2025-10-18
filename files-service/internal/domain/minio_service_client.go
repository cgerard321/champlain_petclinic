package domain

import (
	"files-service/internal/model"
)

type MinioServiceClient interface {
	GetFile(fileInfo *FileInfo) (*model.FileResponseModel, error)
	AddFile(fileInfo *FileInfo, data []byte) error
	DeleteFile(fileInfo *FileInfo) error
}
