<template>
  <div style="height: 50px;line-height: 50px ; border-bottom: 1px solid #ccc ;display: flex">
    <div style="width: 200px; padding-left: 30px;font-weight: bold;color: #8A77A5" >后台管理</div>
    <div style="flex: 1;"></div>
    <div style="width: 100px;">
      <el-dropdown>
    <span class="el-dropdown-link">
      {{user?.nickName}}
      <el-icon class="el-icon--right">
        <ArrowDown />
      </el-icon>
    </span>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item @click="$router.push('/person')">个人信息</el-dropdown-item>
            <el-dropdown-item @click="$router.push('/login')">退出系统</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>
    </div>
  </div>
</template>

<script>
import ArrowDown from "@element-plus/icons/lib/ArrowDown";
import request from "@/utils/request";
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
    request.get("/getUserInfo",
    ).then(res => {
      this.userId = res.data;
      request.get("/user/" + this.userId
      ).then(res => {
        if (res.code === "0")
        {
          this.user = res.data;
        }
      })
    });
    // let userStr = sessionStorage.getItem("user") || "{}"
    // this.user = JSON.parse(userStr)
  }
}
</script>

<style scoped>

</style>