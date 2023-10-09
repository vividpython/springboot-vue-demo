<template>
  <div>
    <el-card style="width: 40%; margin: 10px">
      <el-form ref="form" :model="form" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="部门">
          <el-input v-model="form.depart.name" disabled></el-input>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickName"></el-input>
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password"
                    type="password"
                    show-password:show-password-icon="false"
                    readonly
                    tabindex="-1"
                    >
            <template #suffix>
              <el-icon class="el-input__icon" @click="modifyPwdDialog"><edit /></el-icon>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      <div style="text-align: center">
        <el-button type="primary" @click="update">保存</el-button>
      </div>
    </el-card>

    <!--  修改密码对话框 -->
    <el-dialog v-model="modifyPwdDialogVisible" title="Tips" width="50%" :close-on-click-modal="false" :before-close="handleCloseUpdateDialog">
      <el-form ref="passwordForm" :model="passwordForm" label-width="200px">
        <el-form-item label="旧密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password></el-input>
        </el-form-item>

        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password></el-input>
        </el-form-item>

        <el-form-item label="再次输入新密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="cancel">取消</el-button>
        <el-button type="primary" @click="updatePassword">
          确认
        </el-button>
      </span>
      </template>
    </el-dialog>

  </div>
</template>

<script>
import request from "@/utils/request";
import {ElMessage} from "element-plus";

export default {
  name: "Person",
  data() {
    return {
      //鉴权用的用户信息
      userId:0,
      user:{},
      form: {
        depart: {
          name: ''
        }
      },
      modifyPwdDialogVisible:false,
      passwordForm: {},
    }
  },
  created() {
    request.get("/user/getUserInfo",
    ).then(res => {
      this.userId = res.data;
      request.get("/user/" + this.userId
      ).then(res => {
        if (res.code === "0") {
          this.form = res.data;
        }
      })
    });
  },
  methods: {
    modifyPwdDialog(){
      console.log("点击修改")
      //清除数据
      this.passwordForm = {};
      this.modifyPwdDialogVisible = true;

    },
    update() {
      request.put("/user", this.form).then(res => {
        console.log(res)
        if (res.code === '0') {
          this.$message({
            type: "success",
            message: "更新成功"
          })
          // 修改信息成功之后向后端发送退出登录请求
          request.post("/logout").then((res) => {
            if (res.code === "0") {
              // 清除本地存储的用户认证信息
              sessionStorage.removeItem("token");
              // 跳转到登录页面
              this.$router.push("/login");
            } else {
              ElMessage({
                message: res.msg,
                type: 'error',
              })
            }
          });

        } else {
          this.$message({
            type: "error",
            message: res.msg
          })
        }
      })
    },
    updatePassword(){
      // 检查两次输入的密码是否一致
      if (this.passwordForm.newPassword !== this.passwordForm.confirmPassword) {
        ElMessage({
          message: "两次输入的密码不一致，请重新输入",
          type: 'error',
        })
        //如果两次密码不一致 要重置整个form
        //清除数据
        this.passwordForm = {};
        return;
      }
      // 检查输入的原密码是否正确
      this.form.password = this.passwordForm.oldPassword;
      request.post("/user/confirmPassword",this.form).then(async res => {
        console.log(res)
        if (res.code === '0') {
          //如果原密码验证正确 则执行修改密码
          this.form.password = this.passwordForm.confirmPassword;
          request.put("/user", this.form).then(async res => {
            console.log(res)
            if (res.code === '0') {
              ElMessage({
                message: "修改成功，请重新登录",
                type: 'success',
              })
              // 修改信息成功之后向后端发送退出登录请求
              request.post("/logout").then((res) => {
                if (res.code === "0") {
                  // 清除本地存储的用户认证信息
                  sessionStorage.removeItem("token");
                  // 跳转到登录页面
                  this.$router.push("/login");
                } else {
                  ElMessage({
                    message: res.msg,
                    type: 'error',
                  })
                }
              });

            } else {
              ElMessage({
                message: "修改失败",
                type: 'error',
              })
            }
            // 关闭对话框之前把焦点从输入框中移除
            this.modifyPwdDialogVisible = false;
          })

        } else {
          ElMessage({
            message: "原密码错误",
            type: 'error',
          })
          //清除数据
          this.passwordForm = {};
        }
      })


    },
    cancel() {
      this.handleCloseUpdateDialog();
    },
    async  handleCloseUpdateDialog(){
      console.log("点击关闭");
      //清除数据
      this.passwordForm = {};
      //关闭对话框
      this.modifyPwdDialogVisible = false;

    }

  }
}
</script>

<style>
.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}
.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}
.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  line-height: 120px;
  text-align: center;
}
.avatar {
  width: 178px;
  height: 178px;
  display: block;
}
.el-input__inner[type="password"] {
  color: transparent;
  text-shadow: 0 0 0 #333;
}
</style>