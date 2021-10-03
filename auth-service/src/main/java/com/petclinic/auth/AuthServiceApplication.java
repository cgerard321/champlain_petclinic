/**
 * Created by IntelliJ IDEA.
 *
 * User: @Fube
 * Date: 21/09/21
 * Ticket: feat(AUTH-CPC-95)
 *
 * User: @MaxGrabs
 * Date: 26/09/21
 * Ticket: feat(AUTH-CPC-13)
 *
 */
package com.petclinic.auth;

import com.petclinic.auth.User.User;
import com.petclinic.auth.User.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.xml.ws.soap.Addressing;

@SpringBootApplication
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

}
