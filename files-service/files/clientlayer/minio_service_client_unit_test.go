package clientlayer_test

import (
	"bytes"
	"context"
	"files-service/files/clientlayer"
	"files-service/files/datalayer"
	"files-service/files/models"
	"os"
	"testing"

	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
	"github.com/stretchr/testify/assert"
	container "github.com/testcontainers/testcontainers-go/modules/minio"
	_ "modernc.org/sqlite"
)

var EXISTING_FILE_INFO = datalayer.FileInfo{
	FileId:   "3e5a214b-009d-4a25-9313-344676e6157d",
	FileName: "test image",
	FileType: "image/jpeg",
}

var NON_EXISTING_FILE_INFO = datalayer.FileInfo{
	FileId:   "3e5a214b-009d-4a25-9313-344676e6157k",
	FileName: "test image",
	FileType: "image/jpeg",
}

var EXISTING_FILE_CONTENT = []byte("fake file content")

var EXISTING_FILE_RESPONSE_MODEL = models.FileResponseModel{
	FileId:   "3e5a214b-009d-4a25-9313-344676e6157d",
	FileName: "test image",
	FileType: "image/jpeg",
	FileData: []byte("fake file content"),
}

var (
	minioClient  *minio.Client
	minioService clientlayer.MinioServiceClient
	ctx          context.Context
)

func TestMain(m *testing.M) {
	ctx = context.Background()

	var err error
	minioContainer, err := container.Run(ctx, "minio/minio:RELEASE.2024-01-16T16-07-38Z", container.WithUsername("user"), container.WithPassword("password"))
	if err != nil {
		panic("failed to start MinIO container: " + err.Error())
	}

	con, err := minioContainer.ConnectionString(ctx)
	if err != nil {
		panic("failed to get connection string: " + err.Error())
	}
	defer minioContainer.Terminate(ctx)

	minioClient, err = minio.New(con, &minio.Options{
		Creds: credentials.NewStaticV4("user", "password", ""),
	})
	if err != nil {
		panic("failed to connect to MinIO: " + err.Error())
	}

	for _, bucket := range datalayer.Buckets {
		minioClient.MakeBucket(ctx, bucket, minio.MakeBucketOptions{})
	}

	minioService = clientlayer.NewMinioServiceClient(minioClient)

	os.Exit(m.Run())
}

func reset() {
	for _, bucket := range datalayer.Buckets {
		objectsCh := make(chan minio.ObjectInfo)
		go func() {
			defer close(objectsCh)
			for obj := range minioClient.ListObjects(ctx, bucket, minio.ListObjectsOptions{Recursive: true}) {
				if obj.Err == nil {
					objectsCh <- obj
				}
			}
		}()

		for rErr := range minioClient.RemoveObjects(ctx, bucket, objectsCh, minio.RemoveObjectsOptions{}) {
			if rErr.Err != nil {
				panic(rErr.Err)
			}
		}
	}
}

func TestWhenGetFile_withExistingURL_thenReturnFile(t *testing.T) {
	_, err := minioClient.PutObject(ctx, EXISTING_FILE_INFO.GetFileBucket(), EXISTING_FILE_INFO.GetFileLink(), bytes.NewReader(EXISTING_FILE_CONTENT), int64(len(EXISTING_FILE_CONTENT)), minio.PutObjectOptions{})
	if err != nil {
		t.Fatalf("failed to add test file: %v", err)
	}

	file, err := minioService.GetFile(&EXISTING_FILE_INFO)

	assert.Nil(t, err)
	assert.EqualValues(t, EXISTING_FILE_RESPONSE_MODEL.FileName, file.FileName)
	assert.EqualValues(t, EXISTING_FILE_RESPONSE_MODEL.FileType, file.FileType)
	assert.EqualValues(t, EXISTING_FILE_RESPONSE_MODEL.FileId, file.FileId)
	assert.EqualValues(t, EXISTING_FILE_RESPONSE_MODEL.FileData, file.FileData)
	t.Cleanup(reset)
}

func TestWhenGetFile_withNonExistingURL_thenReturnError(t *testing.T) {
	file, err := minioService.GetFile(&NON_EXISTING_FILE_INFO)

	assert.Nil(t, file)
	assert.NotNil(t, err)
	t.Cleanup(reset)
}
