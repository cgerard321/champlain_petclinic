package clientlayer

import (
	"context"
	"files-service/files/models"
	"io"
	"path"
	"strings"

	"github.com/minio/minio-go/v7"
)

type MinioServiceClient struct {
	client *minio.Client
}

func NewMinioServiceClient(client *minio.Client) *MinioServiceClient {
	return &MinioServiceClient{
		client: client,
	}
}

func (msc *MinioServiceClient) GetFile(bucket string, FileUrl string) (*models.FileResponseModel, error) {
	ctx := context.Background()
	object, err := msc.client.GetObject(ctx, bucket, FileUrl, minio.GetObjectOptions{}) //maybe add content type to object options
	if err != nil {
		return nil, err
	}
	defer object.Close()

	info, err := object.Stat()
	if err != nil {
		return nil, err
	}

	data, err := io.ReadAll(object)
	if err != nil {
		return nil, err
	}

	//get file name from url
	fileNameWithExt := path.Base(FileUrl)
	fileName := strings.Replace(strings.TrimSuffix(fileNameWithExt, path.Ext(fileNameWithExt)), "_", " ", -1) //removes extension from name and replaces "_" with " "

	file := models.FileResponseModel{
		FileId:   "",
		FileName: fileName,
		FileType: info.ContentType,
		FileData: data,
	}

	return &file, nil
}
