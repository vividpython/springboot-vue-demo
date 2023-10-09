
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
          <!--<el-form-item>-->
          <!--  <div style="display:flex" id="atilposition">-->
          <!--    <input-->
          <!--        type="text"-->
          <!--        ref="inputCode"-->
          <!--        v-model="inputCode"-->
          <!--        style="width:240px"-->
          <!--        :placeholder="$t('login.inputCode')"-->
          <!--        clearable-->
          <!--    />-->
          <!--    <span @click="createCode" id="spancode">-->
          <!--      <ValidCode :identifyCode="code"></ValidCode>-->
          <!--    </span>-->
          <!--  </div>-->
          <!--</el-form-item>-->
          <el-form-item>
              <!-- 记住我 -->
              <el-checkbox v-model="checked">记住我</el-checkbox>
          </el-form-item>
          <el-form-item >
            <Vcode :show="isShow" @success="onSuccess" @close="onClose" @fail="onfail" />
            <el-button style="width: 100%" type="primary" @click="submitForm">{{ $t('login.submit') }}</el-button>
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
import { ref } from "vue";
import Vcode from "vue3-puzzle-vcode";
import ValidCode from "@/components/ValidCode.vue";

const Base64 = require("js-base64").Base64

export default {

  components:{
    // ValidCode,
    Vcode
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
      isShow:false,
      code:'',
      // inputCode:'',
      form:{
        username: "",
        password: "",
      },

      checked: false,

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
      rules : {
        username: [
          { required: true, message: this.$t('login.usernameRequired'), trigger: 'blur' },
        ],
        password: [
          { required: true, message: this.$t('login.passwordRequired'), trigger: 'blur'}
        ]
        // inputCode: [
        //   { required: true, message: this.$t('login.inputCodeRequired'), trigger: 'blur'}
        // ]
      }
    }
  },
  mounted() {
    this.getCookie();
  },
  methods:{
    switchLanguage(locale) {
      this.$i18n.locale = locale
    },
    // createCode() {
    //   let text = "";
    //   let possible = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    //   // 设置验证码长度，
    //   for( let i=0; i < 4; i++ ){
    //     text += possible.charAt(Math.floor(Math.random() * possible.length));
    //   }
    //   this.code = text
    // },
    submitForm() {
      // 检查账号和密码是否为空
      if (!this.form.username || !this.form.password) {
        ElMessage({
          message: this.$t('login.inputNullError'),
          type: 'error',
        })
        return;
      }
      this.isShow=true
    },
    //滑块验证成功
    onSuccess(msg) {
      console.log("验证成功")
      this.isShow=false
      this.loading = true; // 显示Loading遮罩
      let _this = this;

      this.$refs['form'].validate((valid) => {
        if (valid) {

          /* ------ 账号密码的存储 ------ */

          if (this.checked) {
            let password = Base64.encode(this.form.password); // base64加密
            this.setCookie(this.form.username, password, 7);
          } else {
            this.setCookie("", "", -1); // 修改2值都为空，天数为负1天就好了
          }

          //可以在此加上axios或其他代码
          // this.login();
          try {
            request.post("/login",this.form).then(res =>{
              if (res.code === '0'){
                ElMessage({
                  message: this.$t('login.loginSuccess'),
                  type: 'success',
                })


                // console.log("res.data==" + res.data.token)
                sessionStorage.setItem("token", JSON.stringify( res.data.token))

                _this.$router.push("/home")
              }else if(res.code === '201') {
                ElMessage({
                  message: this.$t('login.error'),
                  type: 'error',
                })
                // this.inputCode = ''
                // this.createCode()
              }else if(res.code === '403'){
                ElMessage({
                  message: this.$t('login.banStatusError'),
                  type: 'error',
                })
              }else{
                ElMessage({
                  message: res.msg,
                  type: 'error',
                })
              }
            })
          }catch (error){
            console.error(error)
          }finally {
            this.loading = false; // 隐藏Loading遮罩
          }
        }
      })

    },
    // 设置cookie
    setCookie(username, password, days) {
      let date = new Date(); // 获取时间
      date.setTime(date.getTime() + 24 * 60 * 60 * 1000 * days); // 保存的天数
      // 字符串拼接cookie
      window.document.cookie =
          "username" + "=" + username + ";path=/;expires=" + date.toGMTString();
      window.document.cookie =
          "password" + "=" + password + ";path=/;expires=" + date.toGMTString();
    },
// 读取cookie 将用户名和密码回显到input框中
    getCookie() {
      if (document.cookie.length > 0) {
        let arr = document.cookie.split("; "); //分割成一个个独立的“key=value”的形式
        for (let i = 0; i < arr.length; i++) {
          let arr2 = arr[i].split("="); // 再次切割，arr2[0]为key值，arr2[1]为对应的value
          if (arr2[0] === "username") {
            this.form.username = arr2[1];
          } else if (arr2[0] === "password") {
            this.form.password = Base64.decode(arr2[1]);// base64解密
            this.checked = true;
          }
        }
      }
    },

    // 关闭滑块验证后提示用户取消验证
    onClose() {
      this.isShow=false
      this.$message({
        message: '取消验证！',
        type: 'warning'
      });
    },
    onfail(){
      console.log("验证失败")
    },

  },
  // mounted() {
  //   this.createCode()
  // },
 };
</script>

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