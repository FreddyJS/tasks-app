package com.tasks.rest;

import com.tasks.business.UsersService;
import com.tasks.business.entities.User;
import com.tasks.config.JwtTokenProvider;
import com.tasks.rest.json.Credentials;
import com.tasks.rest.json.TokenResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Api(value = "Authentication", tags = { "Authentication" })
public class UserController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private UsersService userService;
    
    @ApiOperation(value = "Login to get a JWT Token")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully authenticated", response = TokenResponse.class),
    })
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public ResponseEntity<TokenResponse> doLogin(@RequestBody Credentials credentials) 
            throws AuthenticationException {

        final Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                credentials.getUsername(),
                credentials.getPassword()
            )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        String token = null;
        if("user1".equals(credentials.getUsername())) {
            token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMSIsImF1dGhvcml0aWVzIjpbIlJPTEVfVVNFUiJdLCJpYXQiOjE1NDIyOTM3MTQsImV4cCI6MTU0NDkyMzQ2MH0.DXizE1O9gcCYd0kEy7oxfGO5L9X1lNaJAXTO_yj-E_F4EYUygD3G8wPqd0gUsSeWtGNZghuLR9AOodzYUDfanw";
        } else if("admin1".equals(credentials.getUsername())) {
            token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbjEiLCJhdXRob3JpdGllcyI6WyJST0xFX0FETUlOIiwiUk9MRV9VU0VSIl0sImlhdCI6MTU0MjI5Mzg1MiwiZXhwIjoxNTQ0OTIzNTk4fQ.YJ2XSzu7Sqt7L_YO6MNeq_YyYfRiXDJT4S4r0nR8KBmSdXuABXMPMu0DB3JKnIOwu7BZnPYrGGXzZQXmZQriYA";
        }

        return ResponseEntity.ok(new TokenResponse(token));
    }    
    
    @ApiOperation(value = "Get all users", authorizations = {@Authorization(value = "Bearer")})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved the list of users", 
                     responseContainer="List", response = User.class)
    })
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    public ResponseEntity<Iterable<User>> doGetUsers() {
        return ResponseEntity.ok(userService.findByUserRole());
    }    

}
