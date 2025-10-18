package mock

import (
	"files-service/internal/domain"

	"github.com/stretchr/testify/mock"
)

type MockFileInfoRepo struct {
	mock.Mock
}

func (m *MockFileInfoRepo) GetFileInfo(fileId string) *domain.FileInfo {
	args := m.Called(fileId)
	if args.Get(0) == nil {
		return nil
	}
	return args.Get(0).(*domain.FileInfo)
}

func (m *MockFileInfoRepo) AddFileInfo(fileInfo *domain.FileInfo) error {
	args := m.Called(fileInfo)
	return args.Error(0)
}

func (m *MockFileInfoRepo) DeleteFileInfo(fileId string) error {
	args := m.Called(fileId)
	return args.Error(0)
}

func (m *MockFileInfoRepo) ExistsById(fileId string) (bool, error) {
	//TODO implement me
	panic("implement me")
}
