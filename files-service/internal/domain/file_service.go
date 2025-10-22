package domain

import (
	models2 "files-service/internal/model"
)

type FileService interface {
	GetFile(string) (*models2.FileResponseModel, error)
	AddFile(*models2.FileRequestModel) (*models2.FileResponseModel, error)
	UpdateFile(string, *models2.FileRequestModel) (*models2.FileResponseModel, error)
	DeleteFile(string) error
}
