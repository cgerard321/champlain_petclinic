package businesslayer

import (
	"files-service/files/models"
)

type FilesService interface {
	GetFile(string, string) (*models.FileResponseModel, error)
	AddFile(string, *models.FileResponseModel) (string, error)
	DeleteFile(string, string) error
	UpdateFile(string, string, *models.FileResponseModel) error
}
