package com.petclinic.auth.User;

public interface UserService {


    User createUser(UserIDLessDTO user);

    User passwordReset(long id, String passwd)throws Exception;

}
