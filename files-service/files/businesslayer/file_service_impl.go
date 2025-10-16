package businesslayer

import (
	"files-service/files/clientlayer"
	"files-service/files/datalayer"
	"files-service/files/models"
	"files-service/files/util/exception"
	"mime"
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

func (i *FilesServiceImpl) saveFile(fileId string, model *models.FileRequestModel) (*models.FileResponseModel, error) {
	fileName := strings.Replace(strings.TrimSuffix(model.FileName, path.Ext(model.FileName)), "_", " ", -1)
	mediaType, _, err := mime.ParseMediaType(model.FileType)

	if err != nil { //This error should never happen here as it is already handled in the Body Validation
		return nil, err
	}

	fileInfo := &datalayer.FileInfo{
		FileId:   fileId,
		FileType: mediaType,
		FileName: fileName,
	}

	if err := i.repository.AddFileInfo(fileInfo); err != nil {
		return nil, err
	}
	if err := i.minioServiceClient.AddFile(fileInfo, model.FileData); err != nil {
		if err2 := i.repository.DeleteFileInfo(fileInfo.FileId); err2 != nil { //this is here to remove the data from the database if adding the file to minio failed
			return nil, err2
		}
		return nil, err
	}

	return &models.FileResponseModel{
		FileId:   fileInfo.FileId,
		FileName: fileInfo.FileName,
		FileType: fileInfo.FileType,
		FileData: model.FileData,
	}, nil
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

	return i.saveFile(fileId, model)
}

func (i *FilesServiceImpl) UpdateFile(id string, model *models.FileRequestModel) (*models.FileResponseModel, error) {
	fileInfo := i.repository.GetFileInfo(id)
	if fileInfo == nil {
		return nil, exception.NewNotFoundException("fileId: " + id + " was not found")
	}
	if err := i.DeleteFile(id); err != nil {
		return nil, err
	}

	return i.saveFile(id, model)
}

func (i *FilesServiceImpl) DeleteFile(id string) error {
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
