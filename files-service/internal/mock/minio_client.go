package mock

import (
	"files-service/internal/domain"
	"files-service/internal/model"

	"github.com/stretchr/testify/mock"
)

type MinioClient struct {
	mock.Mock
}

func (m *MinioClient) GetFile(fileInfo *domain.FileInfo) (*model.FileResponseModel, error) {
	args := m.Called(fileInfo)
	if args.Get(0) == nil {
		return nil, args.Error(1)
	}
	return args.Get(0).(*model.FileResponseModel), args.Error(1)
}

func (m *MinioClient) AddFile(fileInfo *domain.FileInfo, data []byte) error {
	args := m.Called(fileInfo, data)
	return args.Error(0)
}

func (m *MinioClient) DeleteFile(fileInfo *domain.FileInfo) error {
	args := m.Called(fileInfo)
	return args.Error(0)
}
