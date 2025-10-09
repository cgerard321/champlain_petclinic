package businesslayer_test

import (
	"files-service/files/businesslayer"
	"files-service/files/datalayer"
	"files-service/files/models"
	"testing"

	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/mock"
)

var VALID_FILE_INFO = datalayer.FileInfo{
	FileId:   "3e5a214b-009d-4a25-9313-344676e6157d",
	FileName: "petclinic test image",
	FileType: "image/jpg",
}

var VALID_FILE_RESPONSE_MODEL = models.FileResponseModel{
	FileId:   "3e5a214b-009d-4a25-9313-344676e6157d",
	FileName: "petclinic test image",
	FileType: "image/jpg",
	FileData: []byte("fake image data"),
}

const NON_EXISTING_FILE_ID = "3e5a214b-009d-4a25-9313-344676e6157k"
const EXISTING_FILE_ID = "3e5a214b-009d-4a25-9313-344676e6157d"

func setupFileServiceUnitTest() (*MockFileInfoRepo, *MockMinioServiceClient) {
	return new(MockFileInfoRepo), new(MockMinioServiceClient)
}

func TestWhenGetFileById_withExistingFileId_thenReturnFileResponseModel(t *testing.T) {
	mockRepo, mockClient := setupFileServiceUnitTest()
	mockRepo.On("GetFileInfo", EXISTING_FILE_ID).Return(&VALID_FILE_INFO)
	mockClient.On("GetFile", mock.AnythingOfType("*datalayer.FileInfo")).Return(&VALID_FILE_RESPONSE_MODEL, nil)

	service := businesslayer.NewFileService(mockRepo, mockClient)
	file, err := service.GetFile(EXISTING_FILE_ID)

	assert.Nil(t, err)
	assert.EqualValues(t, VALID_FILE_INFO.FileName, file.FileName)
	assert.EqualValues(t, VALID_FILE_INFO.FileType, file.FileType)
	assert.EqualValues(t, VALID_FILE_INFO.FileId, file.FileId)
	mockRepo.AssertExpectations(t)
	mockClient.AssertExpectations(t)
}

func TestWhenGetFileById_withNonExistingFileId_thenReturnError(t *testing.T) {
	mockRepo, mockClient := setupFileServiceUnitTest()
	mockRepo.On("GetFileInfo", NON_EXISTING_FILE_ID).Return(nil)

	service := businesslayer.NewFileService(mockRepo, mockClient)
	file, err := service.GetFile(NON_EXISTING_FILE_ID)

	assert.NotNil(t, err)
	assert.Nil(t, file)
	mockRepo.AssertExpectations(t)
}

func TestWhenDeleteFileById_withExistingFileId_thenDeleteSuccessfully(t *testing.T) {
	mockRepo, mockClient := setupFileServiceUnitTest()
	mockRepo.On("GetFileInfo", EXISTING_FILE_ID).Return(&VALID_FILE_INFO)
	mockRepo.On("DeleteFileInfo", EXISTING_FILE_ID).Return(nil)
	mockClient.On("DeleteFile", &VALID_FILE_INFO).Return(nil)

	service := businesslayer.NewFileService(mockRepo, mockClient)
	err := service.DeleteFileByFileId(EXISTING_FILE_ID)

	assert.Nil(t, err)
	mockRepo.AssertExpectations(t)
	mockClient.AssertExpectations(t)
}

func TestWhenDeleteFileById_withNonExistingFileId_thenReturnError(t *testing.T) {
	mockRepo, mockClient := setupFileServiceUnitTest()
	mockRepo.On("GetFileInfo", NON_EXISTING_FILE_ID).Return(nil)

	service := businesslayer.NewFileService(mockRepo, mockClient)
	err := service.DeleteFileByFileId(NON_EXISTING_FILE_ID)

	assert.NotNil(t, err)
	assert.EqualError(t, err, "fileId: "+NON_EXISTING_FILE_ID+" was not found")

	mockRepo.AssertExpectations(t)
}
