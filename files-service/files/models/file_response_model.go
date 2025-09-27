package models

type FileResponseModel struct { //needs to be in its own package
	FileId   string `json:"fileId"`
	FileName string `json:"fileName"`
	FileType string `json:"fileType"`
	FileData []byte `json:"fileData"`
}
