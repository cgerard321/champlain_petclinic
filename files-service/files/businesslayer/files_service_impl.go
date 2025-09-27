package businesslayer

import (
	"files-service/files/clientlayer"
	"files-service/files/datalayer"
	"files-service/files/models"
	"files-service/files/util/exception"
)

type FilesServiceImpl struct {
	repository         *datalayer.FileLinkRepo
	minioServiceClient *clientlayer.MinioServiceClient
}

func NewFileLinkService(repository *datalayer.FileLinkRepo, minioServiceClient *clientlayer.MinioServiceClient) *FilesServiceImpl {
	return &FilesServiceImpl{
		repository:         repository,
		minioServiceClient: minioServiceClient,
	}
}

func (i *FilesServiceImpl) GetFile(id string) (*models.FileResponseModel, error) {
	fileInfo := i.repository.GetFileInfo(id)

	if fileInfo == nil {
		return nil, exception.NewNotFoundException("fileId: " + id + " was not inside the database")
	}

	resp, err := i.minioServiceClient.GetFile(fileInfo)

	if err != nil {
		return nil, err
	}

	resp.FileId = id

	return resp, nil
}
