package mock

import (
	models2 "files-service/internal/model"

	"github.com/stretchr/testify/mock"
)

type MockFileService struct {
	mock.Mock
}

func (m *MockFileService) GetFile(id string) (*models2.FileResponseModel, error) {
	args := m.Called(id)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models2.FileResponseModel), args.Error(1)
}

func (m *MockFileService) AddFile(model *models2.FileRequestModel) (*models2.FileResponseModel, error) {
	args := m.Called(model)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models2.FileResponseModel), args.Error(1)
}

func (m *MockFileService) UpdateFile(id string, model *models2.FileRequestModel) (*models2.FileResponseModel, error) {
	args := m.Called(id, model)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models2.FileResponseModel), args.Error(1)
}
func (m *MockFileService) DeleteFile(id string) error {
	args := m.Called(id)
	return args.Error(0)
}
