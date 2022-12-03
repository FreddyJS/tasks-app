const OAUTH_SERVER = 'http://localhost:7777/oauth-server/';
const CLIENT_ID = 'tasks_app'

var userService = {
    findUsers : (data, onSuccess, onError) => {
        doFetch('api/users', data, onSuccess, onError);
    },

    login : (data, onSuccess, onError) => {
        doFetch('api/login', $.extend(data, {method: 'post'}), onSuccess, onError);
    },

    oauthLogin : () => {
        var url = OAUTH_SERVER + 'oauth/authorize?';
        url += 'response_type=token';
        url += '&client_id=' + CLIENT_ID;
        url += '&redirect_uri=http://localhost:8888/tasks-service/dashboard/loginOAuth'

        window.location = url;
    }
    
};
