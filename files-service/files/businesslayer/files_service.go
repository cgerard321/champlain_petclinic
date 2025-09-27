package businesslayer

import (
	"files-service/files/models"
)

type FilesService interface {
	GetFile(string) (*models.FileResponseModel, error)
}
