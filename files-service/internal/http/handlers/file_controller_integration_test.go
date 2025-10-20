package handlers_test

import (
	"bytes"
	"context"
	"database/sql"
	"encoding/json"
	"files-service/internal/client"
	"files-service/internal/domain"
	"files-service/internal/http/handlers"
	"files-service/internal/model"
	"files-service/internal/repository"
	"files-service/internal/service"
	"files-service/internal/util"
	"net/http"
	"net/http/httptest"
	"os"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/minio/minio-go/v7"
	"github.com/minio/minio-go/v7/pkg/credentials"
	"github.com/stretchr/testify/assert"
	container "github.com/testcontainers/testcontainers-go/modules/minio"
	_ "modernc.org/sqlite"
)

var (
	controller   *handlers.FilesController
	minioService domain.MinioClient
	fileRepo     domain.FileInfoRepository
	minioClient  *minio.Client
	db           *sql.DB
	ctx          context.Context
)

func TestMain(m *testing.M) {
	var err error
	db, err = sql.Open("sqlite", "file::memory:?cache=shared")
	if err != nil {
		panic(err)
	}

	fileRepo = repository.NewFileInfoRepo(db)
	util.SetupTable(db)

	ctx = context.Background()

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

	for _, bucket := range domain.Buckets {
		minioClient.MakeBucket(ctx, bucket, minio.MakeBucketOptions{})
	}

	minioService = client.NewMinioClient(minioClient)
	controller = handlers.NewFileController(service.NewFileService(fileRepo, minioService))

	os.Exit(m.Run())
}

func reset() {
	_, err := db.Exec("DELETE FROM files")
	if err != nil {
		panic(err)
	}

	for _, bucket := range domain.Buckets {
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

func addFileToContext(file *domain.FileInfo, data []byte) {
	err := fileRepo.AddFileInfo(file)
	if err != nil {
		panic(err)
	}

	err = minioService.AddFile(file, data)
	if err != nil {
		panic(err)
	}
}

func TestWhenAddNewFile_withValidFileRequestModel_thenReturnFileResponseModel(t *testing.T) {
	t.Cleanup(reset)
	router := gin.Default()
	_ = controller.Routes(router)

	body, err := json.Marshal(VALID_FILE_REQUEST_MODEL)
	if err != nil {
		t.Fatalf("failed to marshal request model: %v", err)
	}

	req, _ := http.NewRequest(http.MethodPost, "/files/", bytes.NewReader(body))
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusCreated, w.Code)
	var got model.FileResponseModel
	err = json.Unmarshal(w.Body.Bytes(), &got)
	assert.NoError(t, err)
	assert.EqualValues(t, VALID_FILE_RESPONSE_MODEL.FileName, got.FileName)
	assert.EqualValues(t, VALID_FILE_RESPONSE_MODEL.FileType, got.FileType)
	assert.EqualValues(t, VALID_FILE_RESPONSE_MODEL.FileData, got.FileData)
}

func TestWhenUpdateFile_withExistingFileId_thenReturnUpdatedFileResponseModel(t *testing.T) {
	t.Cleanup(reset)

	addFileToContext(&VALID_FILE_INFO, VALID_FILE_DATA)
	router := gin.Default()
	_ = controller.Routes(router)

	updatedModel := model.FileRequestModel{
		FileName: "updated petclinic image",
		FileType: "image/jpeg",
		FileData: []byte("new fake file data"),
	}
	body, err := json.Marshal(updatedModel)
	assert.NoError(t, err)

	req, _ := http.NewRequest(http.MethodPut, "/files/"+EXISTING_FILE_ID, bytes.NewReader(body))
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var got model.FileResponseModel
	err = json.Unmarshal(w.Body.Bytes(), &got)
	assert.NoError(t, err)
	assert.Equal(t, updatedModel.FileName, got.FileName)
	assert.Equal(t, updatedModel.FileType, got.FileType)
	assert.Equal(t, updatedModel.FileData, got.FileData)
}

func TestWhenUpdateFile_withNonExistingFileId_thenReturnNotFound(t *testing.T) {
	t.Cleanup(reset)

	router := gin.Default()
	_ = controller.Routes(router)

	updatedModel := model.FileRequestModel{
		FileName: "updated petclinic image",
		FileType: "image/jpeg",
		FileData: []byte("new fake file data"),
	}
	body, err := json.Marshal(updatedModel)
	assert.NoError(t, err)

	req, _ := http.NewRequest(http.MethodPut, "/files/"+NON_EXISTING_ID, bytes.NewReader(body))
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusNotFound, w.Code)
}

func TestWhenGetFile_withExistingFileId_thenReturnFileResponseModel(t *testing.T) {
	t.Cleanup(reset)
	addFileToContext(&VALID_FILE_INFO, VALID_FILE_DATA)
	router := gin.Default()
	_ = controller.Routes(router)

	req, _ := http.NewRequest(http.MethodGet, "/files/"+EXISTING_FILE_ID, nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var got model.FileResponseModel
	err := json.Unmarshal(w.Body.Bytes(), &got)
	assert.NoError(t, err)
	assert.EqualValues(t, VALID_FILE_RESPONSE_MODEL.FileName, got.FileName)
	assert.EqualValues(t, VALID_FILE_RESPONSE_MODEL.FileType, got.FileType)
	assert.EqualValues(t, VALID_FILE_RESPONSE_MODEL.FileData, got.FileData)
}

func TestWhenDeleteFile_withExistingFileId_thenReturnSuccessMessage(t *testing.T) {
	t.Cleanup(reset)

	addFileToContext(&VALID_FILE_INFO, VALID_FILE_DATA)
	router := gin.Default()
	_ = controller.Routes(router)

	req, _ := http.NewRequest(http.MethodDelete, "/files/"+EXISTING_FILE_ID, nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusNoContent, w.Code)
	assert.Empty(t, w.Body.String())
}

func TestWhenDeleteFile_withNonExistingFileId_thenReturnNotFound(t *testing.T) {
	t.Cleanup(reset)

	router := gin.Default()
	_ = controller.Routes(router)

	req, _ := http.NewRequest(http.MethodDelete, "/files/"+NON_EXISTING_ID, nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusNotFound, w.Code)
	var resp string
	err := json.Unmarshal(w.Body.Bytes(), &resp)
	assert.NoError(t, err)

	expected := "fileId: " + NON_EXISTING_ID + " was not found"
	assert.Equal(t, expected, resp)
}
