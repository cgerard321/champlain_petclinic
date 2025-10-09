package businesslayer_test

import (
	"files-service/files/datalayer"
	"files-service/files/models"

	"github.com/stretchr/testify/mock"
)

type MockMinioServiceClient struct {
	mock.Mock
}

func (m *MockMinioServiceClient) GetFile(fileInfo *datalayer.FileInfo) (*models.FileResponseModel, error) {
	args := m.Called(fileInfo)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*models.FileResponseModel), args.Error(1)
}

func (m *MockMinioServiceClient) AddFile(fileInfo *datalayer.FileInfo, data []byte) error {
	args := m.Called(fileInfo, data)
	return args.Error(0)
}

func (m *MockMinioServiceClient) DeleteFile(fileInfo *datalayer.FileInfo) error {
	args := m.Called(fileInfo)
	return args.Error(0)
}
