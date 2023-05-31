<template>
<div style="width: 100%;height: 100vh;background-color: #8A77A5 ;overflow: hidden">
  <div style="width: 400px;margin: 150px auto">
    <div style="color: #cccccc ;font-size: 30px;text-align: center">欢迎登录</div>
    <el-form ref="form" :rules="rules" :model="form" size="default">
      <el-form-item prop="username">
        <el-input placeholder="用户名" :prefix-icon="UserFilled" v-model="form.username" />
      </el-form-item>
      <el-form-item prop="password">
        <el-input placeholder="密码" :prefix-icon="lock" v-model="form.password" show-password/>
      </el-form-item>
      <el-form-item>
        <div style="display:flex" id="atilposition">
          <input
              type="text"
              ref="inputCode"
              v-model="inputCode"
              style="width:240px"
              placeholder="请输入验证码,点击右边图片可刷新"
              clearable
          />
          <span @click="createCode" id="spancode">
                <ValidCode :identifyCode="code"></ValidCode>
              </span>
        </div>
      </el-form-item>
      <el-form-item >
        <el-button style="width: 100%" type="primary" @click="login">登录</el-button>
      </el-form-item>
    </el-form>
  </div>
</div>
</template>

<script>
import {UserFilled} from "@element-plus/icons";
import lock from "@element-plus/icons/lib/Lock";
import request from "@/utils/request";
import {ElMessage} from "element-plus";

import ValidCode from "@/components/ValidCode.vue";

export default {
  components:{
    ValidCode
  },
  name: "Login",
  computed: {
    lock() {
      return lock
    },
    UserFilled() {
      return UserFilled
    },
  },
  data(){
    return{
      code:'',
      inputCode:'',

      form:{},
      rules : ({
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
        ]})
    }
  },
  created() {
    sessionStorage.removeItem("user")
  },
  mounted() {

    this.createCode()
  },
  methods:{
    createCode() {
      let text = "";
      let possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
      // 设置验证码长度，
      for( let i=0; i < 4; i++ ){
        text += possible.charAt(Math.floor(Math.random() * possible.length));
      }
      this.code = text
    },

    login(){


      let _this = this;
      this.$refs['form'].validate((valid) => {
        if (valid) {
          if(this.inputCode === ''){
            ElMessage({
              message: '请输入验证码',
              type: 'warning',
            })
            return
          }
          if(this.inputCode.toLowerCase() !== this.code.toLowerCase()){
            ElMessage({
              message: '验证码错误',
              type: 'error',
            })
            this.inputCode = ''
            this.createCode()
            return
          }
          //提交操作

          request.post("/user/login",this.form).then(res =>{
            if (res.code === '0'){
              ElMessage({
                message: '登录成功',
                type: 'success',
              })


              console.log("res.data==" + res.data.token)
              sessionStorage.setItem("token", JSON.stringify( res.data.token))

              _this.$router.push("/")
            }else {
              ElMessage({
                message: '用户名或密码错误',
                type: 'error',
              })
              this.inputCode = ''
              this.createCode()
            }
          })
        }})

    }
  }
}
</script>

<style>
</style>