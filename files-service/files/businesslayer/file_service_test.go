package businesslayer

import (
	"errors"
	"files-service/files/datalayer"
	"testing"
)

//import (
//	"errors"
//	"files-service/files/datalayer"
//	"files-service/files/models"
//	"net/http/httptest"
//	"os"
//	"testing"
//)
//
//func TestMain(m *testing.M) {
//	os.Exit(m.Run())
//}
//
//const VALID_FILE_ID = "3e5a214b-009d-4a25-9313-344676e6157d"
//const NOT_FOUND_FILE_ID = "3e5a214b-009d-4a25-9313-344676e6157k"
//
//func TestGetFile_Success(t *testing.T) {
//
//	repo := &mockRepo{
//		fileInfo: &datalayer.FileInfo{},
//	}
//	minio := &mockMinio{
//		resp: &models.FileResponseModel{},
//	}
//
//	service := NewFileLinkService(repo, minio)
//	result, err := service.GetFile(VALID_FILE_ID)
//
//	if err != nil {
//		t.Fatalf("unexpected error: %v", err)
//	}
//	if result.FileId != "123" {
//		t.Errorf("expected FileId 123, got %s", result.FileId)
//	}
//}

func TestGetFile_NotFoundInRepo(t *testing.T) {
	repo := &mockRepo{fileInfo: nil}
	minio := &mockMinio{}
	service := NewFileLinkService(repo, minio)

	_, err := service.GetFile(NOT_FOUND_FILE_ID)
	if err == nil {
		t.Fatal("expected error, got nil")
	}
}

func TestGetFile_MinioError(t *testing.T) {
	repo := &mockRepo{
		fileInfo: &datalayer.FileInfo{FileId: "123", Url: "http://example.com", FileType: "png"},
	}
	minio := &mockMinio{
		err: errors.New("minio error"),
	}

	service := NewFileLinkService(repo, minio)
	_, err := service.GetFile("123")

	if err == nil || err.Error() != "minio error" {
		t.Fatalf("expected minio error, got %v", err)
	}
}
