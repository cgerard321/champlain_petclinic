package clientlayer

import (
	"files-service/files/datalayer"
	"files-service/files/models"
)

type MinioServiceClient interface {
	GetFile(fileInfo *datalayer.FileInfo) (*models.FileResponseModel, error)
	AddFile(fileInfo *datalayer.FileInfo, data []byte) error
	DeleteFile(fileInfo *datalayer.FileInfo) error
}
