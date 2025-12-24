import Axios from 'axios';
var root = process.env.API_ROOT;
const axios = Axios.create();

axios.interceptors.request.use((config) => {
    config.url = root + config.url;
    return config;
});

export default axios;