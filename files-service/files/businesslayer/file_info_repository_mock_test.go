package businesslayer_test

import (
	"files-service/files/datalayer"

	"github.com/stretchr/testify/mock"
)

type MockFileInfoRepo struct {
	mock.Mock
}

func (m *MockFileInfoRepo) GetFileInfo(fileId string) *datalayer.FileInfo {
	args := m.Called(fileId)
	if args.Get(0) == nil {
		return nil
	}
	return args.Get(0).(*datalayer.FileInfo)
}

func (m *MockFileInfoRepo) AddFileInfo(fileInfo *datalayer.FileInfo) error {
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
