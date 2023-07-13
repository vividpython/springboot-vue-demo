<!--<template>-->
<!--<div style="width: 100%;height: 100vh;background-color: #8A77A5 ;overflow: hidden">-->
<!--  <div style="width: 400px;margin: 150px auto">-->
<!--    <div style="color: #cccccc ;font-size: 30px;text-align: center">欢迎登录</div>-->
<!--    <el-form ref="form" :rules="rules" :model="form" size="default">-->
<!--      <el-form-item prop="username">-->
<!--        <el-input placeholder="用户名" :prefix-icon="UserFilled" v-model="form.username" />-->
<!--      </el-form-item>-->
<!--      <el-form-item prop="password">-->
<!--        <el-input placeholder="密码" :prefix-icon="lock" v-model="form.password" show-password/>-->
<!--      </el-form-item>-->
<!--      <el-form-item>-->
<!--        <div style="display:flex" id="atilposition">-->
<!--          <input-->
<!--              type="text"-->
<!--              ref="inputCode"-->
<!--              v-model="inputCode"-->
<!--              style="width:240px"-->
<!--              placeholder="请输入验证码,点击右边图片可刷新"-->
<!--              clearable-->
<!--          />-->
<!--          <span @click="createCode" id="spancode">-->
<!--                <ValidCode :identifyCode="code"></ValidCode>-->
<!--              </span>-->
<!--        </div>-->
<!--      </el-form-item>-->
<!--      <el-form-item >-->
<!--        <el-button style="width: 100%" type="primary" @click="login">登录</el-button>-->
<!--      </el-form-item>-->
<!--    </el-form>-->
<!--  </div>-->
<!--</div>-->
<!--</template>-->
<template>
    <div style="width: 100%;height: 100vh;
    background-image: linear-gradient(to right, #3B455B, #5d5070);
    overflow: hidden">
      <div style="width: 400px;margin: 150px auto">
        <div style="color: #cccccc ;font-size: 30px;text-align: center">{{ $t('login.title') }}</div>
        <el-form
            ref="form"
            :rules="rules"
            :model="form" size="default"
            v-loading="loading"
            element-loading-text="Loading..."
            :element-loading-spinner="svg"
            element-loading-svg-view-box="-10, -10, 50, 50"
            element-loading-background="rgba(122, 122, 122, 0.8)"
        >
          <el-form-item prop="username">
            <el-input :placeholder="$t('login.username')" :prefix-icon="UserFilled" v-model="form.username" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input :placeholder="$t('login.password')" :prefix-icon="lock" v-model="form.password" show-password/>
          </el-form-item>
          <el-form-item>
            <div style="display:flex" id="atilposition">
              <input
                  type="text"
                  ref="inputCode"
                  v-model="inputCode"
                  style="width:240px"
                  :placeholder="$t('login.inputCode')"
                  clearable
              />
              <span @click="createCode" id="spancode">
                <ValidCode :identifyCode="code"></ValidCode>
              </span>
            </div>
          </el-form-item>
          <el-form-item >
            <el-button style="width: 100%" type="primary" @click="login">{{ $t('login.submit') }}</el-button>
          </el-form-item>
          <!--<el-form-item >-->
          <!--  <el-button  type="primary" @click="switchLanguage">{{ currentLanguage }}/></el-button>-->
          <!--</el-form-item>-->
          <el-form-item>
              <el-button-group>
                <el-button
                    :class="{ active: currentLanguage === '中文' }"
                    @click="switchLanguage('zh-CN')"
                    text
                >
                  中文
                </el-button>
                <el-button
                    :class="{ active: currentLanguage === 'English' }"
                    @click="switchLanguage('en-US')"
                    text
                >
                  English
                </el-button>
              </el-button-group>
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
import { useI18n } from 'vue-i18n';

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
    currentLanguage() {
      return this.$i18n.locale === 'zh-CN' ? '中文' : 'English'
    }
  },
  data(){
    return{
      code:'',
      inputCode:'',
      form:{},
      loading: false,
      svg : `
        <path class="path" d="
          M 30 15
          L 28 17
          M 25.61 25.61
          A 15 15, 0, 0, 1, 15 30
          A 15 15, 0, 1, 1, 27.99 7.5
          L 15 15
        " style="stroke-width: 4px; fill: rgba(0, 0, 0, 0)"/>
      `,
      rules : ({
        username: [
          { required: true, message: this.$t('login.usernameRequired'), trigger: 'blur' },
        ],
        password: [
          { required: true, message: this.$t('login.passwordRequired'), trigger: 'blur'}
        ],
        inputCode: [
          { required: true, message: this.$t('login.inputCodeRequired'), trigger: 'blur'}
        ]
      })
    }
  },
  methods:{
    switchLanguage(locale) {
      this.$i18n.locale = locale
    },
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

      this.loading = true; // 显示Loading遮罩
      let _this = this;
      this.$refs['form'].validate((valid) => {
        if (valid) {
          if(this.inputCode === ''){
            ElMessage({
              message: this.$t('login.inputCodeRequired'),
              type: 'warning',
            })
            return
          }
          if(this.inputCode.toLowerCase() !== this.code.toLowerCase()){
            ElMessage({
              message: this.$t('login.inputCodeError'),
              type: 'error',
            })
            this.inputCode = ''
            this.createCode()
            this.loading = false; // 隐藏Loading遮罩
            return
          }
          //提交操作
          try {
            request.post("/login",this.form).then(res =>{
              if (res.code === '0'){
                ElMessage({
                  message: this.$t('login.loginSuccess'),
                  type: 'success',
                })


                console.log("res.data==" + res.data.token)
                sessionStorage.setItem("token", JSON.stringify( res.data.token))

                _this.$router.push("/")
              }else {
                ElMessage({
                  message: this.$t('login.error'),
                  type: 'error',
                })
                this.inputCode = ''
                this.createCode()
              }
            })
          }catch (error){
            console.error(error)
          }finally {
            this.loading = false; // 隐藏Loading遮罩
          }

        }})

    }
  },
  mounted() {
    this.createCode()
  },
 };
</script>
<!--export default {-->
<!--  components:{-->
<!--    ValidCode-->
<!--  },-->
<!--  name: "Login",-->
<!--  computed: {-->
<!--    lock() {-->
<!--      return lock-->
<!--    },-->
<!--    UserFilled() {-->
<!--      return UserFilled-->
<!--    },-->
<!--  },-->
<!--  data(){-->
<!--    return{-->
<!--      code:'',-->
<!--      inputCode:'',-->

<!--      form:{},-->
<!--      rules : ({-->
<!--        username: [-->
<!--          { required: true, message: '请输入用户名', trigger: 'blur' },-->
<!--        ],-->
<!--        password: [-->
<!--          { required: true, message: '请输入密码', trigger: 'blur' },-->
<!--        ]})-->
<!--    }-->
<!--  },-->
<!--  created() {-->
<!--    sessionStorage.removeItem("user")-->
<!--  },-->
<!--  mounted() {-->

<!--    this.createCode()-->
<!--  },-->
<!--  methods:{-->
<!--    createCode() {-->
<!--      let text = "";-->
<!--      let possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";-->
<!--      // 设置验证码长度，-->
<!--      for( let i=0; i < 4; i++ ){-->
<!--        text += possible.charAt(Math.floor(Math.random() * possible.length));-->
<!--      }-->
<!--      this.code = text-->
<!--    },-->

<!--    login(){-->


<!--      let _this = this;-->
<!--      this.$refs['form'].validate((valid) => {-->
<!--        if (valid) {-->
<!--          if(this.inputCode === ''){-->
<!--            ElMessage({-->
<!--              message: '请输入验证码',-->
<!--              type: 'warning',-->
<!--            })-->
<!--            return-->
<!--          }-->
<!--          if(this.inputCode.toLowerCase() !== this.code.toLowerCase()){-->
<!--            ElMessage({-->
<!--              message: '验证码错误',-->
<!--              type: 'error',-->
<!--            })-->
<!--            this.inputCode = ''-->
<!--            this.createCode()-->
<!--            return-->
<!--          }-->
<!--          //提交操作-->

<!--          request.post("/user/login",this.form).then(res =>{-->
<!--            if (res.code === '0'){-->
<!--              ElMessage({-->
<!--                message: '登录成功',-->
<!--                type: 'success',-->
<!--              })-->


<!--              console.log("res.data==" + res.data.token)-->
<!--              sessionStorage.setItem("token", JSON.stringify( res.data.token))-->

<!--              _this.$router.push("/")-->
<!--            }else {-->
<!--              ElMessage({-->
<!--                message: '用户名或密码错误',-->
<!--                type: 'error',-->
<!--              })-->
<!--              this.inputCode = ''-->
<!--              this.createCode()-->
<!--            }-->
<!--          })-->
<!--        }})-->

<!--    }-->
<!--  }-->
<!--}-->
<!--</script>-->

<style>
.active {
  box-shadow: inset 0 2px 0 #409EFF;
  outline: none;
  background-color: transparent;
}
.example-showcase .el-loading-mask {
  z-index: 9;
}
</style>