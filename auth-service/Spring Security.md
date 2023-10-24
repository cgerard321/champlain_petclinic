# Spring Security

# Explanation

[](https://www.notion.so/1a56382ef412401ca324670702a0f3f0?pvs=21)

### WARNING

This section will be about Spring security and how it works. This is based on MY experience, this is in no way the best or 100% accurate. All I can say is that it works. The documentation is extremely lacking online, so this document or me (Dylan Brassard) are in most cases your best bet for any questions. ChatGPT is hit and miss. Of course there is also Christine Gerard or other teachers that can help.

## 1. Basics

First, we add the spring security dependency in our `build.gradle` file :

```
implementation 'org.springframework.boot:spring-boot-starter-security'
```

The dependency is already in the `build.gradle` file, but the version is outdated, use the code above ensure that you are using the latest version.

### What is it ?

"Spring Security is a powerful and highly customizable authentication and access-control framework. It is the de-facto standard for securing Spring-based applications.

Spring Security is a framework that focuses on providing both authentication and authorization to Java applications. Like all Spring projects, the real power of Spring Security is found in how easily it can be extended to meet custom requirements." ([https://spring.io/projects/spring-security](https://spring.io/projects/spring-security))

## My code

Here, I will go through my code and explain what it does.

### @Configuration

This that Beans will be created here that Spring might read.

#Official "Indicates that a class declares one or more @Bean methods and may be processed by the Spring container to generate bean definitions and service requests for those beans at runtime"

### @EnableWebSecurity

This configures spring security with the beans in this class.

#Official "Add this annotation to an @Configuration class to have the Spring Security configuration defined in any `WebSecurityConfigurer` or more likely by exposing a `SecurityFilterChain` bean." 

In other words, you tell spring that this configuration class is for spring security.

## Injections

```java
 UserDetailsService userDetailService;
    JwtTokenFilter jwtTokenFilter;
    CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint;

    public SecurityConfig(UserDetailsService userDetailService, JwtTokenFilter jwtTokenFilter, CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint) {
        this.userDetailService = userDetailService;
        this.jwtTokenFilter = jwtTokenFilter;
        this.customBasicAuthenticationEntryPoint = customBasicAuthenticationEntryPoint;
    }

```

This can vary, but you will most likely have the JWT filter and the `userDetailService` (I will explain later what these are). But depending on what you will need, you simply do constructor injection.

#Note I could have used `@RequiredArgsConstrutor` for cleaner code.

## Authentication Manager Bean

```java
@Bean
    public AuthenticationManager authenticationManager(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailService);
        authProvider.setPasswordEncoder(encoder());
        return new ProviderManager(authProvider);
    }

```

#Official "Each `AuthenticationProvider` performs a specific type of authentication. For example, `[DaoAuthenticationProvider](https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/dao-authentication-provider.html#servlet-authentication-daoauthenticationprovider)` supports username/password-based authentication, while `JwtAuthenticationProvider` supports authenticating a JWT token."

We, in this case, use `DaoAuthenticationProvider`. This bean is for login in a user, for example in my controller I would do or better yet, call my service to do something like this :

```java
//login is the request body of the post to /login
Authentication authenticate = authenticationManager
        .authenticate(
                new UsernamePasswordAuthenticationToken(
                        login.getEmail(), login.getPassword()
                )
        );

UserPrincipalImpl user = (UserPrincipalImpl) authenticate.getPrincipal();

```

This will confirm that the user :
1-exists
2-has the correct credentials

Then after you confirm, you can return the token different ways, here is one way with http cookie:

```java
ResponseCookie token = ResponseCookie.from("Bearer", jwtTokenUtil.generateToken(user))
        .httpOnly(true)
        .secure(true)
        .maxAge(Duration.ofHours(1))
        .sameSite("Lax").build();

response.setHeader(HttpHeaders.SET_COOKIE, token.toString());
return ResponseEntity.ok()
        .body(removePwdMapper.responseModelToResponseModelLessPassword(userService.getUserByEmail(user.getUsername())));
/*

//in another class called JwtTokenUtil

*/
public String generateToken(UserPrincipalImpl userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put(CLAIM_KEY_USERNAME, userDetails.getUsername());
    claims.put(CLAIM_KEY_CREATED, new Date());

    return generateToken(claims);
}

String generateToken(Map<String, Object> claims) {
    Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(60).toInstant());

    return Jwts.builder()
            .setClaims(claims)
            .setExpiration(expirationDate)
            .signWith(SignatureAlgorithm.HS512, SecurityConst.SECRET )
            .compact();
}

```

This code creates a cookie, with `httpOnly` and `secure` set to true, this is IMPORTANT.
Then simply returns it in the response.
The JWT (JSON web token) is generated with the `JWTokenUtil` class, I won't go in details since there is documentation online and this step is mostly done in pet clinic.

## Security Filter Chain Bean

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                    .requestMatchers(HttpMethod.POST,"/api/v1/users/login").permitAll()
                    .requestMatchers(HttpMethod.POST,"/api/v1/users").permitAll()
                    .requestMatchers("/api/v1/users/forgot_password").permitAll()
                    .requestMatchers("/api/v1/users/reset_password").permitAll()
                    .requestMatchers("/api/v1/users/**").authenticated()
                    .anyRequest().denyAll())
            .logout(logout -> logout
                    .logoutUrl("/api/v1/users/logout")
                    .logoutSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> {
                        httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    })
                    .addLogoutHandler((request, response, auth) -> {
                        for (Cookie cookie : request.getCookies()) {
                                String cookieName = cookie.getName();
                                Cookie cookieToDelete = new Cookie(cookieName, null);
                                cookieToDelete.setMaxAge(0);
                                response.addCookie(cookieToDelete);
                        }
                    }))
            .httpBasic()
            .authenticationEntryPoint(customBasicAuthenticationEntryPoint)
            .and()
            .csrf().disable()
            .cors().configurationSource(corsConfigurationSource());
    http.addFilterBefore(
            jwtTokenFilter,
            UsernamePasswordAuthenticationFilter.class
    );

    return http.build();
}

```

### Authorize Http Requests

```java
authorizeHttpRequests(authorizeRequests -> authorizeRequests
                    .requestMatchers(HttpMethod.POST,"/api/v1/users/login").permitAll()
                    .requestMatchers(HttpMethod.POST,"/api/v1/users").permitAll()
                    .requestMatchers("/api/v1/users/forgot_password").permitAll()
                    .requestMatchers("/api/v1/users/reset_password").permitAll()
                    .requestMatchers("/api/v1/users/**").authenticated()
                    .anyRequest().denyAll())

```

#Official "Allows restricting access based upon the `HttpServletRequest` using `RequestMatcher` implementations (i.e. via URL patterns)."

In other words, this will look at the request and if the URI matched from the one specifies it follows the procedure you tell it to.

For example :

```java
 .requestMatchers(HttpMethod.POST,"/api/v1/users/login").permitAll()

```

This looks if the method is a post and the URI is `/api/v1/users/login`, if it is it allows everyone to access it. This means users without tokens can access it.

```java
.requestMatchers("/api/v1/users/forgot_password").permitAll()

```

This just looks for if the URI matches it.

A more complex example :

```java
.requestMatchers("/api/v1/users/**").authenticated()

```

This matches the URI like before BUT `/**` means that it also matches whatever comes after.

#StackOverflow
Example :

```java
@Override
    protected void configure(HttpSecurity http) throws Exception {
    // ...
    .antMatchers(HttpMethod.GET, "/**").permitAll
    .antMatchers(HttpMethod.POST, "/*").permitAll
    // ...
 }

```

"In this configuration any "**Get**" request will be permitted, for example:

- /book
- /book/20
- /book/20/author

So, all this urls match text with pattern "/**".

Permitted urls for "**Post**":

- /book
- /magazine

Urls above match with "/*""

### Logout

```java
.logout(logout -> logout
                    logout -> logout
                    .logoutUrl("/api/v1/users/logout")
                    .logoutSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> {
                        httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
                    })
                    .addLogoutHandler((request, response, auth) -> {
                        for (Cookie cookie : request.getCookies()) {
                                String cookieName = cookie.getName();
                                Cookie cookieToDelete = new Cookie(cookieName, null);
                                cookieToDelete.setMaxAge(0);
                                response.addCookie(cookieToDelete);
                        }
                    }))

```

Lets break it down :

### Logout URL

```java
.logoutUrl("/api/v1/users/logout")

```

This sets what URL to call to logout.

### Logout Success Handler

```java
.logoutSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> {                        httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
    })

```

This handles what happens when the logout is accomplished successfully, in this case it simply returns an empty response body with a Http status of 204.

### Add logout Handler

```java
.addLogoutHandler((request, response, auth) -> {
                        for (Cookie cookie : request.getCookies()) {
                                String cookieName = cookie.getName();
                                Cookie cookieToDelete = new Cookie(cookieName, null);
                                cookieToDelete.setMaxAge(0);
                                response.addCookie(cookieToDelete);
                        }
                    }))

```

This is where you do the logic to logout. In this case, since I was using cookies, I am deleting the cookie (technically expiring). This deletes ALL cookies, so the token and also the JSESSIONID which is important.

#Note After writing this, I think I found a better way :

```java
CookieClearingLogoutHandler cookies = new CookieClearingLogoutHandler("our-custom-cookie");
http
    .logout((logout) -> logout.addLogoutHandler(cookies))

```

Or even better (I think ?) :

```java
HeaderWriterLogoutHandler clearSiteData =
		new HeaderWriterLogoutHandler(new ClearSiteDataHeaderWriter());
http
    .logout((logout) -> logout.addLogoutHandler(clearSiteData))

```

#Note I think we should also create a `CustomLogoutHandler` implementing `LogoutHandler` to make the code cleaner and follow regulations.

### Http basic

```java
.httpBasic()

```

#Official "Configures HTTP Basic authentication."

### Authentication Entry Point

```java
.authenticationEntryPoint(customBasicAuthenticationEntryPoint)

```

#Official "Used by `ExceptionTranslationFilter` to commence an authentication scheme."
#Official "The `AuthenticationEntryPoint` to be populated on `BasicAuthenticationFilter` in the event that authentication fails. The default to use `BasicAuthenticationEntryPoint` with the realm "Realm""

In other words, when an exception is thrown, it goes there.

For example :

```java

@Component
public class CustomBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        PrintWriter writer = response.getWriter();
        writer.println("HTTP Status 401 - " + authException.getMessage());
    }

    @Override
    public void afterPropertiesSet() {
        setRealmName("YOUR REALM");
        super.afterPropertiesSet();
    }

```

### And

#Official "Return the `SecurityBuilder` when done using the `SecurityConfigurer`. This is useful for method chaining."

### CSRF disabled

```java
.csrf().disable()

```

This disables CSRF

### CSRF

#Google"Definition. Cross-Site Request Forgery (CSRF) is **an attack that forces authenticated users to submit a request to a Web application against which they are currently authenticated**."

### CORS

```java
.cors().configurationSource(corsConfigurationSource());

```

Your declare where it should look for the CORS configuration.

### CORS Definition

#Google "Cross-origin resource sharing (CORS) is **a mechanism for integrating applications**. CORS defines a way for client web applications that are loaded in one domain to interact with resources in a different domain."

### Add Filter Before

```java
  http.addFilterBefore(
            jwtTokenFilter,
            UsernamePasswordAuthenticationFilter.class
    );

```

It takes two parameters, the first one is the custom filter the token will pass through. The second one is the filter that the custom one will be placed before.

In this case we want two parameters, a username and a password.

### Http Build

And that's it ! We build our config and return it so it can be used.

## CORS Configuration

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    final CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("<http://localhost:3000>");
    config.addAllowedMethod("GET");
    config.addAllowedMethod("PUT");
    config.addAllowedMethod("POST");
    config.addAllowedMethod("DELETE");
    config.addAllowedHeader("*");
    source.registerCorsConfiguration("/**", config);
    return source;
}

```

This configures CORS, I highly recommend you read up on what CORS is and why it matters.
All you need to know is that this line :

```java
    config.addAllowedOrigin("<http://localhost:3000>");

```

This is the URL of the frontend, since we want to receive requests from it.
Otherwise your requests will work with postman but not through a browser with AXIOS for example.

## User Details Service

```java
@Bean
public UserDetailsService userDetailsServiceBean() throws Exception {
    UserDetails user = User.withUsername("user")
            .password(encoder().encode("userPass"))
            .roles("USER")
            .build();
    return new InMemoryUserDetailsManager(user);
    }

```

This adds a basic user.
#Note I think this should just be removed, more research is needed. Maybe we need it when we add roles since we might have things hidden behind role requirements.

## Password Encoder

```java
   @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

```

This sets what password encoder is used.

### Config file

---

Below is the complete config file including everything motioned above, if there are any parts that you don't understand go back up and read whatever method you have trouble with.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig  {

    UserDetailsService userDetailService;
    JwtTokenFilter jwtTokenFilter;
    CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint;

    public SecurityConfig(UserDetailsService userDetailService, JwtTokenFilter jwtTokenFilter, CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint) {
        this.userDetailService = userDetailService;
        this.jwtTokenFilter = jwtTokenFilter;
        this.customBasicAuthenticationEntryPoint = customBasicAuthenticationEntryPoint;
    }

    @Bean
    public AuthenticationManager authenticationManager(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailService);
        authProvider.setPasswordEncoder(encoder());
        return new ProviderManager(authProvider);
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(HttpMethod.POST,"/api/v1/users/login").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/v1/users").permitAll()
                        .requestMatchers("/api/v1/users/forgot_password").permitAll()
                        .requestMatchers("/api/v1/users/reset_password").permitAll()
                        .requestMatchers("/api/v1/users/**").authenticated()
                        .anyRequest().denyAll())
                .logout(logout -> logout
                        .logoutUrl("/api/v1/users/logout")
                        .logoutSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> {
                            httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        })
                        .addLogoutHandler((request, response, auth) -> {
                            for (Cookie cookie : request.getCookies()) {
                                    String cookieName = cookie.getName();
                                    Cookie cookieToDelete = new Cookie(cookieName, null);
                                    cookieToDelete.setMaxAge(0);
                                    response.addCookie(cookieToDelete);
                            }
                        }))
                .httpBasic()
                .authenticationEntryPoint(customBasicAuthenticationEntryPoint)
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());
        http.addFilterBefore(
                jwtTokenFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

//    @Bean
//    public WebSecurityCustomizer webSecurityCustomizer() {
//        return (web) -> web.ignoring()
//                .requestMatchers(HttpMethod.POST,"/api/v1/users/login")
//                .requestMatchers(HttpMethod.POST,"/api/v1/users");
//    }

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    final CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("<http://localhost:3000>");
    config.addAllowedMethod("GET");
    config.addAllowedMethod("PUT");
    config.addAllowedMethod("POST");
    config.addAllowedMethod("DELETE");
    config.addAllowedHeader("*");
    source.registerCorsConfiguration("/**", config);
    return source;
}
    @Bean
    public UserDetailsService userDetailsServiceBean() throws Exception {

        UserDetails user = User.withUsername("user")
                .password(encoder().encode("userPass"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);}
    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}

```

# More Information

There is so much left, I will add it here when I see something that I think requires attention.

## User Details Service Implementation

#ChatGPT**UserDetailsService Implementation**: The `UserDetailsService` is an interface provided by Spring Security, a popular framework for securing Java applications. It defines a contract for loading user details by a username (in this case, an email address). The code you've provided is implementing this interface, indicating that it will be used for loading user details during authentication.

```java
@Service
public class UserDetailsImpl implements UserDetailsService {

    private final UserServiceClient userServiceClient;

    public UserDetailsImpl(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @Override
    public UserPrincipalImpl loadUserByUsername(String email) {
        UserResponseModel user = userServiceClient.getUserByEmail(email);
        if (user == null) {
            throw new UsernameNotFoundException(email);
        }
        return new UserPrincipalImpl(user);
    }
}

```

## How to change password

Below are all the steps required to change a user's password. We need to optimise the code, it's a bit of a mess.

### In the controller

```java
//CAN BE DONE IN FRONT END
@GetMapping("/forgot_password")
public String showForgotPasswordForm() {
    return "forgot_password_form";
}

@PostMapping("/forgot_password")
public String processForgotPassword(@RequestBody UserResetPwdRequestModel userResetPwdRequestModel, Model model) {
    //PUT IN A SERVICE
    model = userService.processForgotPassword(userResetPwdRequestModel, model);
    return "forgot_password_form";

}

@GetMapping("/reset_password")
public String showResetPasswordForm(@RequestParam Map<String, String> querryParams, Model model) {
    //Hash token
   model = userService.showResetPasswordForm(querryParams, model);

    return "reset_password_form";

}

@PostMapping("/reset_password")
public String processResetPassword(@RequestBody UserResetPwdWithTokenRequestModel resetRequest, Model model) {
   model = userService.processResetPassword(resetRequest, model);

    return "message";
}

```

### In the service

```java
@Override
public Model processForgotPassword(UserResetPwdRequestModel userResetPwdRequestModel, Model model) {
    String email = userResetPwdRequestModel.getEmail();
    String token = UUID.randomUUID().toString();
    log.info("Generated token: " + token);
    try {
        getUserByEmail(email);
    }
    catch(RuntimeException e){
        model.addAttribute("message", "This Email is not registered to any account !");
        return model;
    }

    try {

        updateResetPasswordToken( token, email);
        log.info("Line 155");

        String resetPasswordLink =  userResetPwdRequestModel.getUrl()+ "/api/v1/users/reset_password?token=" + token;
        sendEmail(email, resetPasswordLink);
        model.addAttribute("message", "We have sent a reset password link to your email. Please check.");
    } catch (Exception ex) {
        model.addAttribute("error", ex.getMessage());
    }
return model;
}

@Override
public void updateResetPasswordToken(String token, String email) {
    User user = userRepository.findByEmail(email);
    if (user != null) {
        if(tokenRepository.findResetPasswordTokenByUserIdentifier_UserId(user.getUserIdentifier().getUserId()) != null){
            tokenRepository.delete(tokenRepository.findResetPasswordTokenByUserIdentifier_UserId(user.getUserIdentifier().getUserId()));
        }
        //Hash the tokens
        ResetPasswordToken resetPasswordToken = new ResetPasswordToken(user.getUserIdentifier().getUserId(),BCrypt.hashpw(token,salt));
        tokenRepository.save(resetPasswordToken);
    } else {
        throw new IllegalArgumentException("Could not find any customer with the email " + email);
    }
}

@Override
public UserResponseModel getByResetPasswordToken(String token) {
    log.info("Token: " + token);
    log.info("Line 187");
    String hashedToken = BCrypt.hashpw(token, salt);
    log.info("Hashed token: " + hashedToken);
    ResetPasswordToken resetPasswordToken = tokenRepository.findResetPasswordTokenByToken(hashedToken);
    final Calendar cal = Calendar.getInstance();

    if(resetPasswordToken.getExpiryDate().after(cal.getTime()))
        return userResponseMapper.entityToResponseModel(userRepository.findUserByUserIdentifier_UserId(resetPasswordToken.getUserIdentifier().getUserId()));
    else        throw new IllegalArgumentException("Token is expired (in getByResetPasswordToken()");    }

@Override
public void updatePassword(String newPassword, String token) {

        final Calendar cal = Calendar.getInstance();
        log.info("line 201");
        ResetPasswordToken resetPasswordToken = tokenRepository.findResetPasswordTokenByToken(BCrypt.hashpw(token, salt));
        if(resetPasswordToken.getExpiryDate().before(cal.getTime())){
            throw new IllegalArgumentException("Token expired");
        }

        String encodedPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt(10));
        User user = userRepository.findUserByUserIdentifier_UserId(resetPasswordToken.getUserIdentifier().getUserId());
        user.setPassword(encodedPassword);

        userRepository.save(user);
        tokenRepository.delete(resetPasswordToken);

}

@Override
public Model showResetPasswordForm(Map<String, String> querryParams, Model model) {
    String token = querryParams.get("token");

    UserResponseModel userResponseModel = getByResetPasswordToken(token);
    model.addAttribute("token", token);

    if (userResponseModel == null) {
        model.addAttribute("message", "Invalid Token");
    }
    else{
        model.addAttribute("message", "You have successfully changed your password.");
    }
    return model;
}

@Override
public Model processResetPassword(UserResetPwdWithTokenRequestModel resetRequest, Model model) {
    String token = resetRequest.getToken();
    String password = resetRequest.getPassword();

    //Hash token
    UserResponseModel userResponseModel = getByResetPasswordToken(token);

    model.addAttribute("title", "Reset your password");

    if (userResponseModel == null) {
        model.addAttribute("message", "Invalid Token");
    } else {
        updatePassword(password, token);

        model.addAttribute("message", "You have successfully changed your password.");
    }
    return model;
}

public void sendEmail(String recipientEmail, String link) throws MessagingException, UnsupportedEncodingException, InterruptedException {
    Message message = new MimeMessage(session);
    message.setFrom(new InternetAddress(username));
    message.setRecipients(
            Message.RecipientType.TO,
            InternetAddress.parse(recipientEmail) //grif2004@hotmail.com
    );
    message.setSubject("Change Password");

    message.setText("Test : " + link);

    Transport.send(message);
}

```

### The HTML pages

### forgot_password_form.html

```html
<html xmlns:th="<http://www.w3.org/1999/xhtml>">
<head>
    <link rel="stylesheet" href="<https://cdn.jsdelivr.net/npm/bootstrap@4.3.1/dist/css/bootstrap.min.css>" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">

    <title>Reset Password</title>
</head>
<div>
    <h2 class="text-center">Forgot Password</h2>
</div>

<div class="text-center" th:if="${error != null}">
    <p class="text-danger">[[${error}]]</p>
</div>
<div class="text-center" th:if="${message != null}">
    <p class="text-warning">[[${message}]]</p>
</div>
<form th:action="@{/api/v1/users/forgot_password}" method="post" style="max-width: 420px; margin: 0 auto;">
    <div class="border border-secondary rounded p-3">
        <div>            <p>We will be sending a reset password link to your email.</p>
        </div>        <div>            <p>                <input type="email" name="email" class="form-control" placeholder="Enter your e-mail" required autofocus/>
            </p>            <p class="text-center">
                <input type="submit" value="Send" class="btn btn-primary" />
            </p>        </div>    </div></form>
</html>

```

### message.html

```html
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="<https://cdn.jsdelivr.net/npm/bootstrap@4.3.1/dist/css/bootstrap.min.css>" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">

    <title>Menard Comics</title>
</head>
<body>
<h1 class="text-center">
    [[${message}]]
</h1>
</body>
</html>

```

### reset_password_form.html

```html
<!DOCTYPE html>
<html lang="en" xmlns="<http://www.w3.org/1999/html>">
<head>
  <meta charset="UTF-8">
  <link rel="stylesheet" href="<https://cdn.jsdelivr.net/npm/bootstrap@4.3.1/dist/css/bootstrap.min.css>" integrity="sha384-ggOyR0iXCbMQv3Xipma34MD+dH/1fQ784/j6cY/iJTQUOhcWr7x9JvoRxT2MZw1T" crossorigin="anonymous">

  <title>Reset Your Password</title>
</head>
<body>
<div>
  <h2 class="text-center">Reset Your Password</h2>
</div>

<form th:action="@{/api/v1/users/reset_password}" method="post" style="max-width: 350px; margin: 0 auto;">
  <input type="hidden" name="token" th:value="${token}" />
  <div class="border border-secondary rounded p-3">
    <div>      <p>        <input type="password" name="password" id="password" class="form-control"
               placeholder="Enter your new password" required autofocus />
      </p>      <p>        <input type="password" id="confirmedPassword" class="form-control" placeholder="Confirm your new password"
               required oninput="checkPasswordMatch();" />
        <span id="passwordError" style="color: red;"></span>
      </p>      <p class="text-center">
        <input type="submit" value="Change Password" class="btn btn-primary" />
      </p>      <input type="checkbox" class="mr-1" onclick="showPwd()">Show Fields</input>

    </div>  </div></form>
</body>
<script>
  function checkPasswordMatch() {
    var password = document.getElementById("password");
    var confirmedPassword = document.getElementById("confirmedPassword");
    var passwordError = document.getElementById("passwordError");

    if (password.value !== confirmedPassword.value) {
      passwordError.textContent = "Passwords do not match!";
      confirmedPassword.setCustomValidity("Passwords do not match!");
    } else {
      passwordError.textContent = "";
      confirmedPassword.setCustomValidity("");
    }
  }
  function showPwd() {
    var password = document.getElementById("password");
    var confirmedPassword = document.getElementById("confirmedPassword");

    if (password.type === "password") {
      password.type = "text";
      confirmedPassword.type = "text";
    } else {
      password.type = "password";
      confirmedPassword.type = "password";
    }
  }
</script>
</html>

```

# [Example](https://github.com/DylanBrass/mc_website_backend) of security IN API-Gateway

- *We need to make it work in a different microservice

#Note ***Below are the steps I believe might work to accomplish this goal

Create a filter in `api-gateway` that makes a request to the `auth-service`. Make an endpoint for validation in `auth-service`.

## Filter in API-Gateway

```java
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // Extract the JWT token from the request (e.g., from headers or cookies)
        String jwtToken = extractToken(request);

        // Validate the JWT token using AuthValidationService
        ResponseEntity<String> validationResponse = authValidationService.validateToken(jwtToken);

        if (validationResponse.getStatusCode() == HttpStatus.OK) {
            // Token is valid, proceed with the request
            filterChain.doFilter(request, response);
        } else {
            // Token is invalid, deny access
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		//maybe return this instead ?

                resolver.resolveException(request, response, null, new InvalidBearerTokenException("Token is invalid"));
                return;
            }
    }

```

- *The `authValidationService` makes a call to the `auth-service` microservice. This is the code I think might work to do it :

## Make call to auth-service

```java
@Service
public class AuthValidationService {
    private final WebClient.Builder webClientBuilder;

    @Autowired
    public AuthValidationService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public ResponseEntity<String> validateToken(String jwtToken) {
        // Make a POST request to the auth-service for token validation
        ResponseEntity<String> responseEntity = webClientBuilder.build()
            .post()
            .uri("<http://auth-service-hostname>:port/validate-token")
            .bodyValue(jwtToken)
            .retrieve()
            .toEntity(String.class)
            .block();

        return responseEntity;
    }
}

```

### The filter in auth-service

- *There is already one, but I think it can be better

This code is called when a request is done to the auth-service, this verifies the token.

This is done outside of servlet, meaning that errors throw here are really weird when returned. For example I had the problem of 401 error being thrown all the time I fixed it with this :

```java
@Component
public class CustomBasicAuthenticationEntryPoint extends BasicAuthenticationEntryPoint {
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        PrintWriter writer = response.getWriter();
        writer.println("HTTP Status 401 - " + authException.getMessage());
    }

    @Override
    public void afterPropertiesSet() {
        setRealmName("YOUR REALM");
        super.afterPropertiesSet();
    }
}

```

This is a custom entry point, I set it up like this in the security config file :

```java
.authenticationEntryPoint(customBasicAuthenticationEntryPoint)

```

and initialize like this with constructor injection:

```java
CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint;

```

```java
@Override
protected void doFilterInternal(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain chain)
        throws ServletException, IOException {

    response.setHeader("Access-Control-Allow-Origin", "<http://localhost:3000>");
        log.warn("Entered Filter");
        // Get authorization header and validate
        final Cookie[] cookies = request.getCookies();

            if(cookies == null)
            {
                chain.doFilter(request, response);
            return;        }
        Cookie sessionCookie = null;
        for( Cookie cookie : cookies ) {
            if( ( "Bearer" ).equals( cookie.getName() ) ) {
                sessionCookie = cookie;
                break;            }
        }

    if(sessionCookie == null)
    {
        chain.doFilter(request, response);
        return;    }

    final String token = sessionCookie.getValue();

    try {

        if (!jwtTokenUtil.validateToken(token)) {
            resolver.resolveException(request, response, null, new InvalidBearerTokenException("Token is expired"));
            return;        }

        UserResponseModel userResponseModel = userServiceClient
                .getUserByEmail(jwtTokenUtil.getUsernameFromToken(token));

        UserDetails userDetails = new UserPrincipalImpl(userResponseModel);

        UsernamePasswordAuthenticationToken
                authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null,
                userDetails.getAuthorities()
        );

        authentication.setDetails(
                new WebAuthenticationDetailsSource().buildDetails(request)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(request, response);
    }
    catch (Exception ex){
        resolver.resolveException(request, response, null, ex);

    }
}

```

This is in the api-gateway filter. The filter for the auth-service will be like the one in mc-backend or the one in pet clinic. Don't forget to configure spring security in the api-gateway :

## Configure spring security in Api-Gateway

```java
@Configuration
public class ApiGatewayConfig {
    private final AuthValidationService authValidationService;

    @Autowired
    public ApiGatewayConfig(AuthValidationService authValidationService) {
        this.authValidationService = authValidationService;
    }

    @Bean
    public FilterRegistrationBean<AuthFilter> authFilterRegistrationBean() {
        FilterRegistrationBean<AuthFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new AuthFilter(authValidationService));
        registrationBean.addUrlPatterns("/api/*"); // Specify the URL patterns to apply the filter
        return registrationBean;
    }
}

```

Something along these lines or this from My summer project

```java
@Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                    .requestMatchers(HttpMethod.POST,"/api/v1/users/login").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/v1/users").permitAll()
                        .requestMatchers("/api/v1/users/forgot_password").permitAll()
                        .requestMatchers("/api/v1/users/reset_password").permitAll()
                        .requestMatchers("/api/v1/users/**").authenticated()
                        .anyRequest().denyAll())
                .logout(logout -> logout
                        .logoutUrl("/api/v1/users/logout")
                        .logoutSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> {
                            httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        })
                        .addLogoutHandler((request, response, auth) -> {
                            for (Cookie cookie : request.getCookies()) {
                                    String cookieName = cookie.getName();
                                    Cookie cookieToDelete = new Cookie(cookieName, null);
                                    cookieToDelete.setMaxAge(0);
                                    response.addCookie(cookieToDelete);
                            }
                        }))
                .httpBasic()
                .authenticationEntryPoint(customBasicAuthenticationEntryPoint)
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());
        http.addFilterBefore(
                jwtTokenFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

```

To configure what is and what isn't to be verified (like creating a user).

We might need to do logout :

### In config, for logout handler:

```java
.logout(logout -> logout
        .logoutUrl("/api/v1/users/logout")
        .logoutSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> {
            httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
        })
        .addLogoutHandler((request, response, auth) -> {
        //this removes the cookie we set below
            for (Cookie cookie : request.getCookies()) {
                    String cookieName = cookie.getName();
                    Cookie cookieToDelete = new Cookie(cookieName, null);
                    cookieToDelete.setMaxAge(0);
                    response.addCookie(cookieToDelete);
            }
        }))

```

Don't forget to add http cookie, talk to it with teacher:

```java
//this is in the controller
ResponseCookie token = ResponseCookie.from("Bearer", jwtTokenUtil.generateToken(user))
        .httpOnly(true)
        .secure(true)
        .maxAge(Duration.ofHours(1))
        .sameSite("Lax").build();

response.setHeader(HttpHeaders.SET_COOKIE, token.toString());

```

## My full security config :

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig  {

    UserDetailsService userDetailService;
    JwtTokenFilter jwtTokenFilter;
    CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint;

    public SecurityConfig(UserDetailsService userDetailService, JwtTokenFilter jwtTokenFilter, CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint) {
        this.userDetailService = userDetailService;
        this.jwtTokenFilter = jwtTokenFilter;
        this.customBasicAuthenticationEntryPoint = customBasicAuthenticationEntryPoint;
    }

    @Bean
    public AuthenticationManager authenticationManager(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailService);
        authProvider.setPasswordEncoder(encoder());
        return new ProviderManager(authProvider);
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(HttpMethod.POST,"/api/v1/users/login").permitAll()
                        .requestMatchers(HttpMethod.POST,"/api/v1/users").permitAll()
                        .requestMatchers("/api/v1/users/forgot_password").permitAll()
                        .requestMatchers("/api/v1/users/reset_password").permitAll()
                        .requestMatchers("/api/v1/users/**").authenticated()
                        .anyRequest().denyAll())
                .logout(logout -> logout
                        .logoutUrl("/api/v1/users/logout")
                        .logoutSuccessHandler((httpServletRequest, httpServletResponse, authentication) -> {
                            httpServletResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
                        })
                        .addLogoutHandler((request, response, auth) -> {
                            for (Cookie cookie : request.getCookies()) {
                                    String cookieName = cookie.getName();
                                    Cookie cookieToDelete = new Cookie(cookieName, null);
                                    cookieToDelete.setMaxAge(0);
                                    response.addCookie(cookieToDelete);
                            }
                        }))
                .httpBasic()
                .authenticationEntryPoint(customBasicAuthenticationEntryPoint)
                .and()
                .csrf().disable()
                .cors().configurationSource(corsConfigurationSource());
        http.addFilterBefore(
                jwtTokenFilter,
                UsernamePasswordAuthenticationFilter.class
        );

        return http.build();
    }

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    final CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("<http://localhost:3000>");
    config.addAllowedMethod("GET");
    config.addAllowedMethod("PUT");
    config.addAllowedMethod("POST");
    config.addAllowedMethod("DELETE");
    config.addAllowedHeader("*");
    source.registerCorsConfiguration("/**", config);
    return source;
}
    @Bean
    public UserDetailsService userDetailsServiceBean() throws Exception {

        UserDetails user = User.withUsername("user")
                .password(encoder().encode("userPass"))
                .roles("USER")
                .build();
        return new InMemoryUserDetailsManager(user);}
    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}

```

# What to fix in the Pet-Clinic project

### CORS is disabled, this is bad practice

```java
http
        .cors()
        .and()
        .csrf().disable()

```

Same thing for csrf, maybe find a way to enable it.

For CORS, my code has it enabled with the corsConfigurationSource code :

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    final CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(true);
    config.addAllowedOrigin("<http://localhost:3000>");
    config.addAllowedMethod("GET");
    config.addAllowedMethod("PUT");
    config.addAllowedMethod("POST");
    config.addAllowedMethod("DELETE");
    config.addAllowedHeader("*");
    source.registerCorsConfiguration("/**", config);
    return source;
}

```

### Outdated version

```java
@Configuration
@RequiredArgsConstructor
public class SpringSecurityConfig extends WebSecurityConfigurerAdapter {
//config is here
}

```

This is the old way of doing, now we create beans like so :

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig  {

    UserDetailsService userDetailService;
    JwtTokenFilter jwtTokenFilter;
    CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint;

    public SecurityConfig(UserDetailsService userDetailService, JwtTokenFilter jwtTokenFilter, CustomBasicAuthenticationEntryPoint customBasicAuthenticationEntryPoint) {
        this.userDetailService = userDetailService;
        this.jwtTokenFilter = jwtTokenFilter;
        this.customBasicAuthenticationEntryPoint = customBasicAuthenticationEntryPoint;
    }

    @Bean
    public AuthenticationManager authenticationManager(){
	    //Set up authenticator
    }
    @Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
	{
		//code for spring security config is here
    }

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
	    //cors config code here
	}
    @Bean
    public UserDetailsService userDetailsServiceBean() throws Exception {
		  //create fake user code goes here
    }
    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }
}

```

To see what the code commented out refer to my config file.

This requires an update to spring boot 3.*
So this :

```
plugins {
   id 'org.springframework.boot' version '2.3.2.RELEASE'
   id 'io.spring.dependency-management' version '1.0.9.RELEASE'
   id 'java'
   id "io.freefair.lombok" version "6.0.0-m2"
   id 'jacoco'
}

```

To this :

```
plugins {
   id 'java'
   id 'org.springframework.boot' version '3.0.2'
   id 'io.spring.dependency-management' version '1.1.0'
   id 'io.freefair.lombok' version '6.6.1'
   id 'jacoco'
}

```

### Bad handling of security token

This is very vulnerable :

```java
final String header = request.getHeader(AUTHORIZATION);
if (isEmpty(header) || !header.startsWith("Bearer ")) {
    filterChain.doFilter(request, response);
    return;
    }

```

An http cookie is much better.

### Bad logout

The logout is done though the front end only, the backend should handle it as well and remove the cookies as shown with the logout handler.

```jsx
function purgeUser() {
        localStorage.removeItem("token")
        localStorage.removeItem("username")
        localStorage.removeItem("email")
        }

```

### Returning token

```java
return UserTokenPair.builder()
        .token(jwtService.encrypt(principal))
        .user(principal)
        .build();

```

```java
@PostMapping("/login")
public ResponseEntity<UserPasswordLessDTO> login(@RequestBody UserIDLessRoleLessDTO user) throws IncorrectPasswordException {
    final UserTokenPair login = userService.login(user);
    return ok()
            .header(AUTHORIZATION, login.getToken())
            .body(userMapper.modelToPasswordLessDTO(login.getUser()));
}

```

Need to ask teacher but I think everything can be done in the controller for cleaner code and to return a cookie (so safer) :

```java
@PostMapping("/login")
public ResponseEntity<UserResponseModelPasswordLess> loginUser(@RequestBody UserLoginRequestModel login, HttpServletResponse response){
    try {
        Authentication authenticate = authenticationManager
                .authenticate(
                        new UsernamePasswordAuthenticationToken(
                                login.getEmail(), login.getPassword()
                        )
                );

        UserPrincipalImpl user = (UserPrincipalImpl) authenticate.getPrincipal();

        ResponseCookie token = ResponseCookie.from("Bearer", jwtTokenUtil.generateToken(user))
                .httpOnly(true)
                .secure(true)
                .maxAge(Duration.ofHours(1))
                .sameSite("Lax").build();

        response.setHeader(HttpHeaders.SET_COOKIE, token.toString());
        return ResponseEntity.ok()
                .body(removePwdMapper.responseModelToResponseModelLessPassword(userService.getUserByEmail(user.getUsername())));
    } catch (BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
}

```

Otherwise just make the service return a cookie and a `UserPrincipal` to have the logic in the service (simple fix I hope).

### Overhaul of the roles

The roles exist and work, but a lot is left to be desired.
Such as admin being default :

```java
Optional<Role> role = roleRepo.findById(1L);

```

### Password Reset

We can do it through email :

```java
@Override
public User passwordReset(long userId, @Valid String newPassword) {

    log.info("id={}", userId);
    User user = userRepo.findById(userId).orElseThrow(() -> new NotFoundException("No user for id:" + userId));
    user.setPassword(newPassword);
    return userRepo.save(user);

}

```

There is an example in the example project.

```java
@GetMapping("/forgot_password")
public String showForgotPasswordForm() {
    return userService.userForgotEmail();
}

@PostMapping("/forgot_password")
public String processForgotPassword(HttpServletRequest request) {
    return userService.sendEmailForForgottenEmail(request);
}

@GetMapping("/reset_password")
public String showResetPasswordForm(@Param(value = "token") String token) throws IllegalAccessException {
    if(token == null)
        throw new IllegalAccessException("An error as occured");

    return userService.resetPasswordPage(token);

}

@PostMapping("/reset_password")
public String processResetPassword(HttpServletRequest resetRequest) {
    return userService.resetPassword(resetRequest);
}

```

This returns pages and sends emails to reset passwords.

I will explain how to do it in the extended explanations at the end.

### Move Secret

We need to move this (from `applications.yml`) to the `applications.properties`

```
jwt:
  expiration: 900000 # 15 minutes
  secret: thisismyverycoolsecretthatisatleast256byteslong # Must at least be 256 bytes long

```

### Create a default admin

```java
// add users in List
		List<UserDetails> users = new ArrayList<UserDetails>();

		users.add(User.withDefaultPasswordEncoder()
				.username("websparrow")
				.password("web123").roles("USER").build());

		return new InMemoryUserDetailsManager(users);
```

### Error Handling:

Improve the Error Handling for more specific cases.
Current one are too general and dont have meaningfull messages.

### Transform to HTTPS instead of HTTP

Do the security headers need to be implemented (Http) & (Https). Might be possible ?

## Multi-Factor Authentication

Verify email does not work well

## Adding a security Documentation page

Would be similar to how asking for help on websites work when you cant get into you account and will provide the solution in a step by step manner (For those who are not that great at codding but understand the web app)

## Content Filtering

Similar to how we limit user accesses.
Not too sure if it is included but we filter the content that is beeing shown
(Don't show certain things that are not supposed to be seen example special plans for new users to old users)

## Backup

Create a way to help the users restore their data if complications happen during the Authentication.
Example : They lose access to their Email/Phone numbers