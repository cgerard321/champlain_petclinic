package businesslayer

import (
	"files-service/files/clientlayer"
	"files-service/files/datalayer"
	"files-service/files/models"
	"files-service/files/util/exception"
	"path"
	"strings"

	"github.com/google/uuid"
)

type FilesServiceImpl struct {
	repository         datalayer.FileInfoRepository
	minioServiceClient clientlayer.MinioServiceClient
}

func NewFileService(repository datalayer.FileInfoRepository, minioServiceClient clientlayer.MinioServiceClient) *FilesServiceImpl {
	return &FilesServiceImpl{
		repository:         repository,
		minioServiceClient: minioServiceClient,
	}
}

func (i *FilesServiceImpl) GetFile(id string) (*models.FileResponseModel, error) {
	fileInfo := i.repository.GetFileInfo(id)

	if fileInfo == nil {
		return nil, exception.NewNotFoundException("fileId: " + id + " was not found")
	}

	resp, err := i.minioServiceClient.GetFile(fileInfo)

	if err != nil {
		return nil, err
	}

	resp.FileId = id

	return resp, nil
}

func (i *FilesServiceImpl) AddFile(model *models.FileRequestModel) (*models.FileResponseModel, error) {
	var fileId string
	for {
		fileId = uuid.New().String()

		exists, err := i.repository.ExistsById(fileId)
		if err != nil {
			return nil, err
		}
		if !exists {
			break
		}
	}

	fileName := strings.Replace(strings.TrimSuffix(model.FileName, path.Ext(model.FileName)), "_", " ", -1)
	fileInfo := &datalayer.FileInfo{
		FileId:   fileId,
		FileType: model.FileType,
		FileName: fileName,
	}

	err := i.repository.AddFileInfo(fileInfo)
	if err != nil {
		return nil, err
	}

	err = i.minioServiceClient.AddFile(fileInfo, model.FileData)
	if err != nil {
		err2 := i.repository.DeleteFileInfo(fileInfo.FileId) //this is here to remove the data from the database if adding the file to minio failed
		if err2 != nil {
			return nil, err2
		}
		return nil, err
	}

	response := &models.FileResponseModel{
		FileId:   fileInfo.FileId,
		FileName: fileName,
		FileType: model.FileType,
		FileData: model.FileData,
	}

	return response, nil
}

func (i *FilesServiceImpl) DeleteFileByFileId(id string) error {
	fileInfo := i.repository.GetFileInfo(id)
	if fileInfo == nil {
		return exception.NewNotFoundException("fileId: " + id + " was not found")
	}

	if err := i.minioServiceClient.DeleteFile(fileInfo); err != nil {
		return err
	}

	if err := i.repository.DeleteFileInfo(id); err != nil {
		return err
	}

	return nil
}
