package mailer

type Mail struct {
	To            string `json:"EmailSendTo" validate:"required,email" example:"customer@example.com"`
	Subject       string `json:"EmailTitle" validate:"required" example:"Appointment confirmation"`
	TemplateName  string `json:"TemplateName" example:"appointment-confirmation"`
	Header        string `json:"Header" example:"Your appointment is booked"`
	Body          string `json:"Body" example:"<p>Your appointment has been confirmed for tomorrow at 2:00 PM.</p>"`
	Footer        string `json:"Footer" example:"Thank you for choosing PetClinic."`
	Correspondent string `json:"CorrespondantName" example:"Dr. Taylor"`
	SenderName    string `json:"SenderName" example:"PetClinic"`
}
