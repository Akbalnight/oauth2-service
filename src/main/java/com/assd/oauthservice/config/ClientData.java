package com.assd.oauthservice.config;

/**
 * Const.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Константы с данными сервисов
 */
public class ClientData
{
    public static final String CLIENT_ID = "ASKUTE-service";
//    public static final String CLIENT_SECRET = "$2a$10$gQxHepiCTQ2OkFG2yldYdOFpXlCPBKKPOMv5HB9O0evnjB0iW8EjO"; // ASKUTE-password в Bcrypt
    public static final String CLIENT_SECRET = "ASKUTE-password"; // ASKUTE-password в Bcrypt

}


/**
 *
 * http://localhost:8809/oauth/authorize?response_type=code&client_id=ASKUTE-service&redirect_uri=http://public-client/&scope=read
 *
 * http://public-client/?error=invalid_request&error_description=Code%20challenge%20required.
 *
 *
 * http://localhost:8809/oauth/authorize?response_type=code&client_id=ASKUTE-service&client_secret=ASKUTE-password&redirect_uri=http://public-client/&scope=read&code_challenge=4cc9b165-1230-4607-873b-3a78afcf60c5
 *
 * http://localhost:8809/oauth/authorize?response_type=code&client_id=ASKUTE-service&redirect_uri=http://public-client/&scope=read&code_challenge=4cc9b165-1230-4607-873b-3a78afcf60c5
 *
 * http://localhost:8809/oauth/authorize?response_type=code&client_id=ASKUTE-service&redirect_uri=http://public-client/&scope=read&code_challenge=YmRmMTkyODk4YjJhYmM4MWQyOGNlZWYxMWJmODExMTYyMWZjY2ZhMGNjMGJjZTZlMjAwMGZlMzdmODc0MjcwZQ==&code_challenge_method=s256
 *
 * curl localhost:8809/oauth/token -d client_id=ASKUTE-service -d grant_type=authorization_code -d redirect_uri=http://public-client/ -d code=PEVHvn -d code_verifier=4cc9b165-1230-4607-873b-3a78afcf60c5
 *
 *
 *
 *
 *
 * http://localhost:8809/oauth/authorize?response_type=code&client_id=ASKUTE-service&redirect_uri=http://localhost:3002/authorization_code&scope=read&code_challenge=4cc9b165-1230-4607-873b-3a78afcf60c5
 * */
