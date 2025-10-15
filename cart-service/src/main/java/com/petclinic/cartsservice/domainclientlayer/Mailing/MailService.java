package com.petclinic.cartsservice.domainclientlayer.Mailing;

import com.petclinic.cartsservice.domainclientlayer.Mailing.Mail;

public interface MailService {

    String sendMail(Mail mail);
}
