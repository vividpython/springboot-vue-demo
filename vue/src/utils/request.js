import axios from 'axios'
import router from "@/router";
import {ElMessage, ElMessageBox} from "element-plus";


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


// 是否显示重新登录
export let isRelogin = { show: false };
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
    }, error => {
        console.log('err' + error)
        let { message } = error;
        if (message === "Network Error") {
            message = "后端接口连接异常";
            ElMessage({
                message: message,
                type: 'warning',
                duration: 5 * 1000
            });
        } else if (message.includes("timeout")) {
            message = "系统接口请求超时";
            ElMessage({
                message: message,
                type: 'warning',
                duration: 5 * 1000
            });
        }else if(message.includes("401")){
            console.log('401：' + error)
            if (!isRelogin.show) {
                console.log("尚未显示登录页面")
                isRelogin.show = true;
                ElMessageBox.confirm('登录状态已过期，您可以继续留在该页面，或者重新登录', '系统提示', { confirmButtonText: '重新登录', cancelButtonText: '取消', type: 'warning' }).then(() => {
                    isRelogin.show = false;
                    sessionStorage.removeItem("token")
                    router.push('/login');
                }).catch(() => {
                    isRelogin.show = false;
                });
            }
        }
        else if (message.includes("Request failed with status code")) {
            message = "系统接口" + message.substr(message.length - 3) + "异常";
            ElMessage({
                message: message,
                type: 'warning',
                duration: 5 * 1000
            });
        }

        return Promise.reject(error)

    }
)


export default request

