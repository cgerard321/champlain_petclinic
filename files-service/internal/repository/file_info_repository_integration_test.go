package repository_test

import (
	"database/sql"
	"files-service/internal/domain"
	"files-service/internal/repository"
	"files-service/internal/util"
	"os"
	"testing"

	"github.com/stretchr/testify/assert"
	_ "modernc.org/sqlite"
)

const EXISTING_FILE_ID = "3e5a214b-009d-4a25-9313-344676e6157d"
const NON_EXISTING_FILE_ID = "3e5a214b-009d-4a25-9313-344676e6157k"

var VALID_FILE_INFO = domain.FileInfo{
	FileId:   EXISTING_FILE_ID,
	FileName: "petclinic test image",
	FileType: "image/png",
}

var UPDATED_FILE_INFO = domain.FileInfo{
	FileId:   EXISTING_FILE_ID,
	FileName: "updated petclinic test image",
	FileType: "image/jpeg",
}

var (
	fileRepo domain.FileInfoRepository
	db       *sql.DB
)

func TestMain(m *testing.M) {
	var err error
	db, err = sql.Open("sqlite", "file::memory:?cache=shared")
	if err != nil {
		panic(err)
	}

	fileRepo = repository.NewFileInfoRepo(db)
	util.SetupTable(db)

	os.Exit(m.Run())
}

func reset() {
	db.Exec("DELETE FROM files")
}

func TestWhenGetFileInfo_withExistingFileId_thenReturnFileInfo(t *testing.T) {
	t.Cleanup(reset)
	err := fileRepo.AddFileInfo(&VALID_FILE_INFO)
	assert.Nil(t, err)

	file := fileRepo.GetFileInfo(EXISTING_FILE_ID)

	assert.NotNil(t, file)
	assert.Equal(t, file.FileId, VALID_FILE_INFO.FileId)
	assert.Equal(t, file.FileName, VALID_FILE_INFO.FileName)
	assert.Equal(t, file.FileType, VALID_FILE_INFO.FileType)
}

func TestWhenGetFileInfo_withNonExistingFileId_thenReturnNilFileInfo(t *testing.T) {
	t.Cleanup(reset)
	err := fileRepo.AddFileInfo(&VALID_FILE_INFO)
	assert.Nil(t, err)

	file := fileRepo.GetFileInfo(NON_EXISTING_FILE_ID)

	assert.Nil(t, file)
}

func TestWhenUpdateFileInfo_withExistingFileId_thenUpdateSuccessfully(t *testing.T) {
	t.Cleanup(reset)
	err := fileRepo.AddFileInfo(&VALID_FILE_INFO)
	assert.Nil(t, err)

	err = fileRepo.DeleteFileInfo(EXISTING_FILE_ID)
	assert.Nil(t, err)
	err = fileRepo.AddFileInfo(&UPDATED_FILE_INFO)
	assert.Nil(t, err)

	updated := fileRepo.GetFileInfo(EXISTING_FILE_ID)
	assert.NotNil(t, updated)
	assert.Equal(t, UPDATED_FILE_INFO.FileName, updated.FileName)
	assert.Equal(t, UPDATED_FILE_INFO.FileType, updated.FileType)
}

func TestWhenUpdateFileInfo_withNonExistingFileId_thenReturnNoEffect(t *testing.T) {
	t.Cleanup(reset)

	err := fileRepo.DeleteFileInfo(NON_EXISTING_FILE_ID)
	assert.Nil(t, err)

	newInfo := domain.FileInfo{
		FileId:   NON_EXISTING_FILE_ID,
		FileName: "non-existing update",
		FileType: "image/jpeg",
	}

	err = fileRepo.AddFileInfo(&newInfo)
	assert.Nil(t, err)

	file := fileRepo.GetFileInfo(NON_EXISTING_FILE_ID)
	assert.NotNil(t, file)
	assert.Equal(t, "non-existing update", file.FileName)
}

func TestWhenDeleteFileInfo_withExistingFileId_thenDeleteSuccessfully(t *testing.T) {
	t.Cleanup(reset)
	err := fileRepo.AddFileInfo(&VALID_FILE_INFO)
	assert.Nil(t, err)

	err = fileRepo.DeleteFileInfo(EXISTING_FILE_ID)
	assert.Nil(t, err)

	file := fileRepo.GetFileInfo(EXISTING_FILE_ID)
	assert.Nil(t, file)
}

func TestWhenDeleteFileInfo_withNonExistingFileId_thenNoError(t *testing.T) {
	t.Cleanup(reset)
	err := fileRepo.DeleteFileInfo(NON_EXISTING_FILE_ID)
	assert.Nil(t, err)
}
