<template>
  <div style=

           "background-image: linear-gradient(to right, #3B455B, #5d5070);
           height: 50px;
           line-height: 50px ;
           border-bottom: 1px solid #ccc ;
           box-shadow: 0 0 10px 5px rgba(138, 119, 165, 0.5);
           display: flex">
    <div style="width: 200px; padding-left: 30px;font-weight: bold;color: white" >生产文件管理系统</div>
    <div style="flex: 1;"></div>
    <div style="width: 100px; display: flex; align-items: center; justify-content: center;">
      <el-dropdown>
        <el-image
            class="el-dropdown-link"
            style="width: 40px; height: 40px"
            :src="user?.img || '/static/default.png'"
            :preview-src-list="[user?.img]"
            preview-teleported="preview-teleported"
        >
        </el-image>
    <!--<span class="el-dropdown-link" style="font-weight: bold; color: white">-->
    <!--  {{user?.nickName}}-->
    <!--  <el-icon class="el-icon&#45;&#45;right" style="font-weight: bold; color: white">-->
    <!--    <ArrowDown />-->
    <!--  </el-icon>-->
    <!--</span>-->
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="$router.push('/person')">个人信息</el-dropdown-item>
            <el-dropdown-item @click="handleLogout">退出系统</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
import ArrowDown from "@element-plus/icons/lib/ArrowDown";
import request from "@/utils/request";
import {ElMessage} from "element-plus";
export default {
  name: "Header",
  components:{
    ArrowDown
  },
  data(){
    return{
      user:{}
    }
  },
  created() {
    const token = sessionStorage.getItem("token");
    if (token) {
      // 如果本地存储中存在 token，说明用户已登录，从后端获取用户信息
      request.get("/user/getUserInfo").then((res) => {
        this.userId = res.data;
        request.get(`/user/${this.userId}`).then((res) => {
          if (res.code === "0") {
            this.user = res.data;
          }
        });
      });
    } else {
      // 如果本地存储中不存在 token，说明用户未登录，跳转到登录页面
      this.$router.push("/login");
    }
  },
  methods: {
    handleLogout() {
      // 向后端发送退出登录请求
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
    },
  },

    // let userStr = sessionStorage.getItem("user") || "{}"
    // this.user = JSON.parse(userStr)
}
</script>

<style scoped>

</style>