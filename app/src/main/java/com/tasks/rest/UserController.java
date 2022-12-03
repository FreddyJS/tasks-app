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
        UserDetails userDetails = userService.loadUserByUsername(credentials.getUsername());
        String token = tokenProvider.generateToken(userDetails);

        return ResponseEntity.ok(new TokenResponse(token));
    }    
    
    @ApiOperation(value = "Get all users", authorizations = {@Authorization(value = "Bearer")})
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successfully retrieved the list of users", 
                     responseContainer="List", response = User.class)
    })
    @RequestMapping(value = "/users", method = RequestMethod.GET)
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Iterable<User>> doGetUsers() {
        return ResponseEntity.ok(userService.findByUserRole());
    }    

}
