package handlers_test

import (
	"bytes"
	"encoding/json"
	"files-service/internal/http/handlers"
	"files-service/internal/mock"
	"files-service/internal/model"
	"files-service/internal/util/exception"
	"net/http"
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"
	"github.com/stretchr/testify/assert"
)

func setUpControllerUnitTests() (*handlers.FilesController, *mock.FileService) {
	fs := new(mock.FileService)
	return handlers.NewFileController(fs), fs
}

func TestWhenGetFileById_withExistingFileId_thenReturnFileResponseModel(t *testing.T) {
	gin.SetMode(gin.TestMode)

	controller, mockService := setUpControllerUnitTests()

	router := gin.Default()
	_ = controller.Routes(router)

	mockService.On("GetFile", EXISTING_FILE_ID).Return(&VALID_FILE_RESPONSE_MODEL, nil)

	req, _ := http.NewRequest(http.MethodGet, "/files/"+EXISTING_FILE_ID, nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var got model.FileResponseModel
	err := json.Unmarshal(w.Body.Bytes(), &got)
	assert.NoError(t, err)
	assert.EqualValues(t, VALID_FILE_RESPONSE_MODEL.FileId, got.FileId)
	assert.EqualValues(t, VALID_FILE_RESPONSE_MODEL.FileName, got.FileName)
	assert.EqualValues(t, VALID_FILE_RESPONSE_MODEL.FileType, got.FileType)
	assert.EqualValues(t, VALID_FILE_RESPONSE_MODEL.FileData, got.FileData)
	mockService.AssertExpectations(t)
}

func TestWhenGetFileById_withNonExistingFileId_thenReturnFileResponseModel(t *testing.T) {
	gin.SetMode(gin.TestMode)

	controller, mockService := setUpControllerUnitTests()

	router := gin.Default()
	_ = controller.Routes(router)

	mockService.On("GetFile", NON_EXISTING_ID).Return(nil, exception.NewNotFoundException("fileId: "+NON_EXISTING_ID+" was not found"))

	req, _ := http.NewRequest(http.MethodGet, "/files/"+NON_EXISTING_ID, nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusNotFound, w.Code)
	var resp string
	err := json.Unmarshal(w.Body.Bytes(), &resp)
	assert.NoError(t, err)
	assert.Equal(t, "fileId: "+NON_EXISTING_ID+" was not found", resp)

	mockService.AssertExpectations(t)
}

func TestWhenGetFileById_withInvalidFileId_thenReturnInvalidFileIdException(t *testing.T) {
	gin.SetMode(gin.TestMode)
	controller, _ := setUpControllerUnitTests()
	router := gin.Default()
	_ = controller.Routes(router)

	req, _ := http.NewRequest(http.MethodGet, "/files/"+INVALID_FILE_ID, nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusBadRequest, w.Code)
	var resp string
	err := json.Unmarshal(w.Body.Bytes(), &resp)
	assert.NoError(t, err)
	assert.Equal(t, "Invalid fileId: "+INVALID_FILE_ID, resp)
}

func TestWhenUpdateFile_withExistingFileId_thenReturnUpdatedFileResponseModel_Unit(t *testing.T) {
	gin.SetMode(gin.TestMode)
	controller, mockService := setUpControllerUnitTests()
	router := gin.Default()
	_ = controller.Routes(router)

	updateModel := model.FileRequestModel{
		FileName: "updated petclinic image",
		FileType: "image/png",
		FileData: []byte("new fake data"),
	}

	expectedResponse := model.FileResponseModel{
		FileId:   EXISTING_FILE_ID,
		FileName: "updated petclinic image",
		FileType: "image/png",
		FileData: []byte("new fake data"),
	}

	mockService.On("UpdateFile", EXISTING_FILE_ID, &updateModel).Return(&expectedResponse, nil)

	body, _ := json.Marshal(updateModel)
	req, _ := http.NewRequest(http.MethodPut, "/files/"+EXISTING_FILE_ID, bytes.NewReader(body))
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusOK, w.Code)
	var got model.FileResponseModel
	err := json.Unmarshal(w.Body.Bytes(), &got)
	assert.NoError(t, err)
	assert.Equal(t, expectedResponse.FileName, got.FileName)
	assert.Equal(t, expectedResponse.FileType, got.FileType)
	assert.Equal(t, expectedResponse.FileData, got.FileData)
	mockService.AssertExpectations(t)
}

func TestWhenUpdateFile_withNonExistingFileId_thenReturnNotFound_Unit(t *testing.T) {
	gin.SetMode(gin.TestMode)
	controller, mockService := setUpControllerUnitTests()
	router := gin.Default()
	_ = controller.Routes(router)

	updateModel := model.FileRequestModel{
		FileName: "updated petclinic image",
		FileType: "image/png",
		FileData: []byte("new fake data"),
	}

	mockService.On("UpdateFile", NON_EXISTING_ID, &updateModel).Return(nil, exception.NewNotFoundException("fileId: "+NON_EXISTING_ID+" was not found"))

	body, _ := json.Marshal(updateModel)
	req, _ := http.NewRequest(http.MethodPut, "/files/"+NON_EXISTING_ID, bytes.NewReader(body))
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusNotFound, w.Code)
	var resp string
	err := json.Unmarshal(w.Body.Bytes(), &resp)
	assert.NoError(t, err)
	assert.Equal(t, "fileId: "+NON_EXISTING_ID+" was not found", resp)
	mockService.AssertExpectations(t)
}

func TestWhenDeleteFile_withExistingFileId_thenReturnSuccess(t *testing.T) {
	gin.SetMode(gin.TestMode)
	controller, mockService := setUpControllerUnitTests()
	router := gin.Default()
	_ = controller.Routes(router)

	mockService.On("DeleteFile", EXISTING_FILE_ID).Return(nil)

	req, _ := http.NewRequest(http.MethodDelete, "/files/"+EXISTING_FILE_ID, nil)
	w := httptest.NewRecorder()
	router.ServeHTTP(w, req)

	assert.Equal(t, http.StatusNoContent, w.Code)
	assert.Empty(t, w.Body.String())
	mockService.AssertExpectations(t)
}
