package models

type FileRequestModel struct {
	FileName string `json:"fileName"`
	FileType string `json:"fileType"`
	FileData []byte `json:"fileData"`
}
