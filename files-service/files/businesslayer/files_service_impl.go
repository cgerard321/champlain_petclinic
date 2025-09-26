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

func (i *FilesServiceImpl) GetFile(bucket string, id string) (*models.FileResponseModel, error) {
	url := i.repository.GetFileLink(id)

	if url == "" {
		return nil, exception.NewNotFoundException("url not found for fileId: " + id)
	}

	resp, err := i.minioServiceClient.GetFile(bucket, url)

	if err != nil {
		return nil, err
	}

	resp.FileId = id

	return resp, nil
}

// needs to care about folders
func (i *FilesServiceImpl) AddFile(bucket string, dto *models.FileResponseModel) (string, error) { //request model not response
	//TODO implement me
	panic("implement me")
}

func (i *FilesServiceImpl) DeleteFile(bucket string, id string) error {
	//TODO implement me
	panic("implement me")
}

func (i *FilesServiceImpl) UpdateFile(bucket string, id string, dto *models.FileResponseModel) error { //request model not response
	//TODO implement me
	panic("implement me")
}
