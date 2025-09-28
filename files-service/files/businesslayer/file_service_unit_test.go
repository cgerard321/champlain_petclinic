package businesslayer

//import (
//	"context"
//	"database/sql"
//	"files-service/files/clientlayer"
//	"files-service/files/datalayer"
//	"files-service/files/util"
//	"time"
//
//	"github.com/minio/minio-go/v7"
//	"github.com/minio/minio-go/v7/pkg/credentials"
//	_ "modernc.org/sqlite"
//
//	"os"
//	"testing"
//
//	"github.com/testcontainers/testcontainers-go"
//	"github.com/testcontainers/testcontainers-go/wait"
//)
//
//func TestMain(m *testing.M) {
//	os.Exit(m.Run())
//}
//
//const VALID_FILE_ID = "3e5a214b-009d-4a25-9313-344676e6157d"
//const VALID_FILE_NAME = "petclinic-base-image.jpg"
//const NOT_FOUND_FILE_ID = "3e5a214b-009d-4a25-9313-344676e6157k"
//
//var VALID_FILE_CONTENT = []byte("fake file content")
//
//func setUpServiceTests(t *testing.T) *FilesServiceImpl {
//	db, err := sql.Open("sqlite", "file::memory:?cache=shared")
//	if err != nil {
//		t.Fatal(err)
//	}
//	util.SetupDatabase(db)
//
//	//need a way to mock the minio api response
//
//	if err != nil {
//		t.Fatal(err)
//	}
//
//	repo := datalayer.NewFileLinkRepo(db)
//	m := clientlayer.NewMinioServiceClient(minioClient)
//
//	return NewFileService(repo, m)
//}
//
//func TestGetFile_Success(t *testing.T) {
//	service := setUpServiceTests(t)
//
//	result, err := service.GetFile(VALID_FILE_ID)
//
//	if err != nil {
//		t.Fatal(err)
//	}
//
//	if result == nil {
//		t.Fatal("result is nil")
//	}
//}
//
//func TestGetFile_NotFoundInRepo(t *testing.T) {
//	service := setUpServiceTests(t)
//
//	_, err := service.GetFile(NOT_FOUND_FILE_ID)
//	if err == nil {
//		t.Fatal("expected error, got nil")
//	}
//}
