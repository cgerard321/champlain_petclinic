package presentationlayer_test

import (
	"files-service/files/models"

	"github.com/stretchr/testify/mock"
)

type MockFileService struct {
	mock.Mock
}

func (m *MockFileService) GetFile(id string) (*models.FileResponseModel, error) {
	args := m.Called(id)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.FileResponseModel), args.Error(1)
}

func (m *MockFileService) AddFile(model *models.FileRequestModel) (*models.FileResponseModel, error) {
	args := m.Called(model)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.FileResponseModel), args.Error(1)
}

func (m *MockFileService) UpdateFile(id string, model *models.FileRequestModel) (*models.FileResponseModel, error) {
	args := m.Called(id, model)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.FileResponseModel), args.Error(1)
}

func (m *MockFileService) DeleteFileByFileId(id string) error {
	args := m.Called(id)
	return args.Error(0)
}
