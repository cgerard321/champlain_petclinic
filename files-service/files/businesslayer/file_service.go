package businesslayer

import (
	"files-service/files/models"
)

type FileService interface {
	GetFile(string) (*models.FileResponseModel, error)
}
