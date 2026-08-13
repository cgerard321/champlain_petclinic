package handlers_test

import (
	"files-service/internal/domain"
	models2 "files-service/internal/model"
)

const EXISTING_FILE_ID = "3e5a214b-009d-4a25-9313-344676e6157d"
const NON_EXISTING_ID = "3e5a214b-009d-4a25-9313-344676e6157f"
const INVALID_FILE_ID = "3e5a214b-009d-4a25-9313-344676e615"

var VALID_FILE_DATA = []byte("fake image data")

var VALID_FILE_REQUEST_MODEL = models2.FileRequestModel{
	FileName: "test image",
	FileType: "image/jpeg",
	FileData: []byte("fake image data"),
}

var VALID_FILE_RESPONSE_MODEL = models2.FileResponseModel{
	FileId:   "3e5a214b-009d-4a25-9313-344676e6157d",
	FileName: "test image",
	FileType: "image/jpeg",
	FileData: []byte("fake image data"),
}

var VALID_FILE_INFO = domain.FileInfo{
	FileId:   EXISTING_FILE_ID,
	FileName: "test image",
	FileType: "image/jpeg",
}
