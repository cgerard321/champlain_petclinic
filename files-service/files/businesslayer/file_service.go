package businesslayer

import (
	"files-service/files/models"
)

type FileService interface {
	GetFile(string) (*models.FileResponseModel, error)
	AddFile(*models.FileRequestModel) (*models.FileResponseModel, error)
	UpdateFile(string, *models.FileRequestModel) (*models.FileResponseModel, error)
	DeleteFileByFileId(string) error
}
