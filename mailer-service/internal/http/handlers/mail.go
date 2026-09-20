package handlers

import (
	"net/http"

	"github.com/gin-gonic/gin"

	mailsvc "mailer-service/internal/mailer"
	pkg "mailer-service/pkg/mailer"
)

type MailHandler struct {
	s mailsvc.Service
}

func NewMailHandler(s mailsvc.Service) *MailHandler {
	return &MailHandler{s: s}
}

func (h *MailHandler) Register(r *gin.Engine) {
	r.Group("/mail")
}

// Post godoc
// @Summary Queue an email
// @Description Validates the request body and queues an email for asynchronous delivery.
// @Tags mail
// @Accept json
// @Produce json
// @Param mail body mailer.Mail true "Email payload"
// @Success 200 {string} string "Message queued"
// @Failure 400 {string} string "mail not found or invalid request payload"
// @Failure 500 {string} string "failed to queue message"
// @Router /mail [post]
func (h *MailHandler) Post(c *gin.Context) {
	v, ok := c.Get("mail")

	if !ok || v == nil {
		c.JSON(http.StatusBadRequest, "mail not found")
		return
	}

	m := v.(*pkg.Mail)

	if err := h.s.Send(m); err != nil {
		c.JSON(http.StatusInternalServerError, "failed to queue message")
		return
	}

	c.IndentedJSON(http.StatusOK, "Message queued")
}
