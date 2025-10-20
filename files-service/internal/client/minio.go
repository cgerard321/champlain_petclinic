package client

import (
	"bytes"
	"context"
	"files-service/internal/domain"
	"files-service/internal/model"
	"io"

	"github.com/minio/minio-go/v7"
)

type MinioClient struct {
	client *minio.Client
}

func NewMinioClient(client *minio.Client) *MinioClient {
	return &MinioClient{
		client: client,
	}
}

func (msc *MinioClient) GetFile(fileInfo *domain.FileInfo) (*model.FileResponseModel, error) {
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

	file := model.FileResponseModel{
		FileId:   fileInfo.FileId,
		FileName: fileInfo.FileName,
		FileType: fileInfo.FileType,
		FileData: data,
	}

	return &file, nil
}

func (msc *MinioClient) AddFile(fileInfo *domain.FileInfo, data []byte) error {
	ctx := context.Background()
	obj := bytes.NewReader(data)

	_, err := msc.client.PutObject(ctx, fileInfo.GetFileBucket(), fileInfo.GetFileLink(), obj, obj.Size(), minio.PutObjectOptions{ContentType: fileInfo.FileType})
	return err
}

func (msc *MinioClient) DeleteFile(fileInfo *domain.FileInfo) error {
	ctx := context.Background()
	return msc.client.RemoveObject(ctx, fileInfo.GetFileBucket(), fileInfo.GetFileLink(), minio.RemoveObjectOptions{})
}
