<template>
<div style="width: 100%;height: 100vh;background-color: #8A77A5 ;overflow: hidden">
  <div style="width: 400px;margin: 150px auto">
    <div style="color: #cccccc ;font-size: 30px;text-align: center">欢迎注册</div>
    <el-form ref="form"  :model="form" size="default" :rules="rules">
      <el-form-item prop="username">
        <el-input placeholder="用户名" clearable="" :prefix-icon="UserFilled" v-model="form.username" />
      </el-form-item>
      <el-form-item prop="password">
        <el-input placeholder="密码"  clearable="" :prefix-icon="lock" v-model="form.password" show-password />
      </el-form-item>
      <el-form-item prop="confirmPassword">
        <el-input placeholder="再次输入密码"  clearable="" :prefix-icon="lock" v-model="form.confirmPassword" show-password/>
      </el-form-item>
      <el-form-item >
        <el-button style="width: 100%" type="primary" @click="register">注册</el-button>
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

export default {
  name: "Register",
  computed: {
    lock() {
      return lock
    },
    UserFilled() {
      return UserFilled
    },
  },
  data(){
    /*
    *@description:重复密码校验
    *@date: 2022-07-14 15:25:12
    */
    let checkPasswdRe = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== this.form.password) {
        callback(new Error('两次输入密码不一致!'))
      } else {
        callback()
      }
    }
    //密码校验
    let newValValidate = (rule, value, callback) => {
      if (/^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[._~!@#$^&*])[A-Za-z0-9._~!@#$^&*]{8,16}$/g.test(value)) {
        callback()
      } else {
        callback(new Error('请输入包含英文字母大小写、数字和特殊符号的 8-16 位组合'))
      }
    }

    return{
      form:{},
      rules : ({
        username: [
          { required: true, message: '请输入用户名', trigger: 'blur' },
        ],
        password: [
          { required: true, message: '请输入密码', trigger: 'blur' },
          {
            min: 8,
            max: 16,
            message: '长度应在 8 到 16 个字符',
            trigger: 'blur'
          },
          { validator: newValValidate, trigger: 'blur' }
        ],
        confirmPassword: [
          {
            required: true,
            validator: checkPasswdRe,
            trigger: 'blur'
          }
        ],
      })
    }
  },
  methods:{
    register(){
      if (this.form.password !== this.form.confirmPassword){
        ElMessage({
          message: '两次密码输入不一致',
          type: 'error',
        })
        return
      }

      let _this = this;
      request.post("/user/register",this.form).then(res =>{
        if (res.code === '0'){
          ElMessage({
            message: '注册成功',
            type: 'success',
          })
          _this.$router.push("/login")
        }else {
          ElMessage({
            message: '注册失败',
            type: 'error',
          })
        }
      })
    }
  }
}
</script>

<style scoped>

</style>