package exception

type InvalidRequestModelException struct {
	message string
}

func NewInvalidRequestModelException(message string) *InvalidRequestModelException {
	return &InvalidRequestModelException{message: message}
}

func NewInvalidRequestModelValueException(field string, value string) *InvalidRequestModelException {
	return &InvalidRequestModelException{message: "Invalid value : " + value + " for field: " + field}
}

func NewInvalidRequestModelFieldException(field string) *InvalidRequestModelException {
	return &InvalidRequestModelException{message: "field : " + field + " is mandatory"}
}

func (e *InvalidRequestModelException) Error() string {
	return e.message
}
