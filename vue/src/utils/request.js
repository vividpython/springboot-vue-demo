import axios from 'axios'
import router from "@/router";
import {ElMessage} from "element-plus";

const cancelToken = axios.CancelToken;
const request = axios.create({
    baseURL: '/api',  // 注意！！ 这里是全局统一加上了 '/api' 前缀，也就是说所有接口都会加上'/api'前缀在，页面里面写接口的时候就不要加 '/api'了，否则会出现2个'/api'，类似 '/api/api/user'这样的报错，切记！！！
    timeout: 5000,
    cancelToken: cancelToken.token, // 添加cancelToken属性
})

// request 拦截器
// 可以自请求发送前对请求做一些处理
// 比如统一加token，对请求参数统一加密
request.interceptors.request.use(config => {
    config.headers['Content-Type'] = 'application/json;charset=utf-8';

    let token = '';
    if (sessionStorage.getItem("token")) {
        token = JSON.parse(sessionStorage.getItem("token"));
    }
    if (!token){
        router.push("/login");
    }
    // 将token添加到请求头headers中
    config.headers['token'] = token;
    return config
}, error => {
    return Promise.reject(error)
});
let is401Handled = false; // 添加标记，表示是否已经处理过 401 错误
// response 拦截器
// 可以在接口响应后统一处理结果
request.interceptors.response.use(
    response => {
        //
        let res = response.data;
        // 如果是返回的文件
        if (response.config.responseType === 'blob') {
            return res
        }
        // 兼容服务端返回的字符串数据
        if (typeof res === 'string') {
            res = res ? JSON.parse(res) : res
        }
        return res;
    },
    error => {
        if (error.response && error.response.status === 401) {
            if (is401Handled)
            {
                return Promise.reject(error); // 如果已经处理过 401 了，就直接拒绝这个错误
            }
            is401Handled = true; // 将标记设置为 true，表示已经处理过 401 错误
            const res = error.response.data;
            console.log("reject.js进来了")
            if (res.code === '401') {
                ElMessage({
                    message: res.msg,
                    type: 'warning',
                });
                sessionStorage.removeItem("token")
                router.push('/login');
            }
        }
        console.log('err' + error.response) // for debug
        return Promise.reject(error);
    }
)


export default request

