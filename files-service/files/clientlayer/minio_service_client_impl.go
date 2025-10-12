package clientlayer

import (
	"bytes"
	"context"
	"files-service/files/datalayer"
	"files-service/files/models"
	"io"

	"github.com/minio/minio-go/v7"
)

type MinioServiceClientImpl struct {
	client *minio.Client
}

func NewMinioServiceClient(client *minio.Client) *MinioServiceClientImpl {
	return &MinioServiceClientImpl{
		client: client,
	}
}

func (msc *MinioServiceClientImpl) GetFile(fileInfo *datalayer.FileInfo) (*models.FileResponseModel, error) {
	ctx := context.Background()
	object, err := msc.client.GetObject(ctx, fileInfo.GetFileBucket(), fileInfo.GetFileLink(), minio.GetObjectOptions{})
	if err != nil {
		return nil, err
	}
	defer object.Close()

	data, err := io.ReadAll(object)
	if err != nil {
		return nil, err
	}

	file := models.FileResponseModel{
		FileId:   fileInfo.FileId,
		FileName: fileInfo.FileName,
		FileType: fileInfo.FileType,
		FileData: data,
	}

	return &file, nil
}

func (msc *MinioServiceClientImpl) AddFile(fileInfo *datalayer.FileInfo, data []byte) error {
	ctx := context.Background()
	obj := bytes.NewReader(data)

	_, err := msc.client.PutObject(ctx, fileInfo.GetFileBucket(), fileInfo.GetFileLink(), obj, obj.Size(), minio.PutObjectOptions{ContentType: fileInfo.FileType})
	return err
}

func (msc *MinioServiceClientImpl) DeleteFile(fileInfo *datalayer.FileInfo) error {
	ctx := context.Background()
	return msc.client.RemoveObject(ctx, fileInfo.GetFileBucket(), fileInfo.GetFileLink(), minio.RemoveObjectOptions{})
}
