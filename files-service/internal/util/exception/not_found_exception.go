package exception

type NotFoundException struct {
	message string
}

func NewNotFoundException(message string) *NotFoundException {
	return &NotFoundException{message: message}
}

func (e *NotFoundException) Error() string {
	return e.message
}
