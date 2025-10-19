package mock

import (
	"files-service/internal/domain"

	"github.com/stretchr/testify/mock"
)

type FileInfoRepository struct {
	mock.Mock
}

func (m *FileInfoRepository) GetFileInfo(fileId string) *domain.FileInfo {
	args := m.Called(fileId)
	if args.Get(0) == nil {
		return nil
	}
	return args.Get(0).(*domain.FileInfo)
}

func (m *FileInfoRepository) AddFileInfo(fileInfo *domain.FileInfo) error {
	args := m.Called(fileInfo)
	return args.Error(0)
}

func (m *FileInfoRepository) DeleteFileInfo(fileId string) error {
	args := m.Called(fileId)
	return args.Error(0)
}

func (m *FileInfoRepository) ExistsById(fileId string) (bool, error) {
	//TODO implement me
	panic("implement me")
}
