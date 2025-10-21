package mock

import (
	models "files-service/internal/model"

	"github.com/stretchr/testify/mock"
)

type FileService struct {
	mock.Mock
}

func (m *FileService) GetFile(id string) (*models.FileResponseModel, error) {
	args := m.Called(id)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.FileResponseModel), args.Error(1)
}

func (m *FileService) AddFile(model *models.FileRequestModel) (*models.FileResponseModel, error) {
	args := m.Called(model)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.FileResponseModel), args.Error(1)
}

func (m *FileService) UpdateFile(id string, model *models.FileRequestModel) (*models.FileResponseModel, error) {
	args := m.Called(id, model)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.FileResponseModel), args.Error(1)
}
func (m *FileService) DeleteFile(id string) error {
	args := m.Called(id)
	return args.Error(0)
}
