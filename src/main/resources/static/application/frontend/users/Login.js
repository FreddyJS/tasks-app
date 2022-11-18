var Login = (props) => {
    const handleClick = () => {        
        var onSuccess = data => {
            if(data.token) {
                const jwtToken = jwt.parseJwtToken(data.token);
                props.dispatch({
                    type: 'login',
                    user: jwtToken.sub,
                    authorities: jwtToken.authorities,
                    token: data.token
                });
                alerts.success('Welcome ' + escapeHtml(jwtToken.sub) + '!');
                jwt.storeJwtToken(data.token);
                props.history.push('/');
            } else {
                alerts.error('Invalid token!');
            }
        };
        
        userService.login({
            token: props.token,
            body: {
		username: $('#username').val(),
		password: $('#password').val()
            }
        }, onSuccess);
    };
    const handleSSO = () => {
    };
    return (
        <div className="row w-100">
           <div className="col">
              <div className="card mx-auto bg-light center-block ui-helper-margin-top">
                 <div className="card-body">

                   <div className="row">
                       <div className="col-12 form-title text-center">
                          <i className="fas fa-user-circle fa-4x"></i>
                       </div>
                   </div>

                   <br/>

                   <form className="form-horizontal toggle-disabled">
                       <div className="form-group row">
                           <label htmlFor="username" className="d-none d-sm-block col-sm-3 text-right input-label-middle">
                               Username
                           </label>
                           <div className="input-group col-12 col-sm-6">
                             <div className="input-group-prepend">
                                <span className="input-group-text"><i className="fas fa-user"></i></span>
                             </div>
                             <input type="text" className="form-control" id="username" name="username"
                                    data-validation-error-msg-container="#input-username-error"
                                    data-validation="length" data-validation-length="min4"
                                    data-validation-error-msg="Invalid username"/>
                           </div>
                       </div>
                       <div className="form-group row d-none">
                           <div className="d-none d-sm-block col-sm-3"></div>
                           <div className="col-12 col-sm-6 text-left" id="input-username-error"></div>
                       </div>
                       <div className="form-group row">
                           <label htmlFor="password" className="d-none d-sm-block col-sm-3 text-right input-label-middle">
                               Password
                           </label>
                          <div className="input-group col-12 col-sm-6">
                             <div className="input-group-prepend">
                                <span className="input-group-text"><i className="fas fa-lock"></i></span>
                             </div>
                             <input type="password" className="form-control"
                                    id="password" name="password"
                                    data-validation-error-msg-container="#input-password-error"
                                    data-validation="length" data-validation-length="min4"
                                    data-validation-error-msg="Invalid password"/>
                          </div>
                       </div>
                       <div className="form-group row d-none">
                          <div className="d-none d-sm-block col-sm-3"></div>
                          <div className="col-12 col-sm-9 text-left" id="input-password-error"></div>
                       </div>

                       <div className="form-row text-center">
                           <div className="col-12">
                               <button type="button" className="btn btn-primary" onClick={handleClick}>
                                   Login
                               </button>
                          </div>
                       </div>
                    </form>
                 </div>
              </div>
              
              <div className="form-group row ui-helper-margin-top">
                  <label className="col text-center input-label-middle">
                     OR
                  </label>
              </div>

              <div className="form-row text-center">
                  <div className="col-12">
                    <button type="button" className="btn btn-primary" onClick={handleSSO}>
                       <i className="fas fa-user-shield"></i>&nbsp;Single Sign On with OAuth2
                    </button>
                 </div>
              </div>
              
           </div>
        </div>
    );
};

Login = ReactRedux.connect()(Login);
