import { createI18n } from 'vue-i18n';

const messages = {
    'zh-CN': {
        login: {
            title: '欢迎登录',
            username: '用户名',
            password: '密码',
            inputCode: '验证码',
            submit: '提交',
            error: '用户名或密码错误',
            inputCodeError: '验证码错误',
            usernameRequired: '请输入用户名',
            passwordRequired: '请输入密码',
            inputCodeRequired: '请输入验证码',
            loginSuccess:'登陆成功'
        },
    },
    'en-US': {
        login: {
            title: 'Welcome to Login',
            username: 'Username',
            password: 'Password',
            inputCode: 'Verification Code',
            submit: 'Submit',
            error: 'Invalid username or password',
            inputCodeError: 'Invalid verification code',
            usernameRequired: 'Please enter your username',
            passwordRequired: 'Please enter your password',
            inputCodeRequired: 'Please enter the verification code',
            loginSuccess:'login success'
        },
    },
};

const i18n = createI18n({
    locale: 'zh-CN',
    messages,
});

export default i18n;