package com.petclinic.authservice.domainclientlayer.Mail;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface MailServiceCall {

    @POST("/mail")
    Call<String> sendMail(@Body Mail mail);
}
