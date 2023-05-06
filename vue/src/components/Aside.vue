<template>
<div>
    <el-menu
        active-text-color="#E0C55C"
        background-color="#88A799"
        class="el-menu-vertical-demo"
        :default-active="path"
        router
        text-color="#AF2A2B"
    style="min-height: calc(100vh - 50px)">
      <el-sub-menu index="1">
        <template #title>
          <el-icon><location /></el-icon>
          <span  class="fontClass">系统管理</span>
        </template>
          <el-menu-item index="/user" v-if="user.role === 1">用户管理</el-menu-item>
          <el-menu-item index="/document">项目文件</el-menu-item>
          <el-menu-item index="/drawing">生产图纸</el-menu-item>
          <el-menu-item index="/news">新闻管理</el-menu-item>
      </el-sub-menu>
      <el-menu-item index="2">
        <el-icon><Menu /></el-icon>
        <span class="fontClass">供应链管理</span>
      </el-menu-item>
      <el-menu-item index="3" disabled>
        <el-icon><document /></el-icon>
        <span class="fontClass">数据分析系统</span>
      </el-menu-item>
      <el-menu-item index="4">
        <el-icon><setting /></el-icon>
        <span class="fontClass">全员质量管理培训</span>
      </el-menu-item>
    </el-menu>
</div>
</template>

<script>

import request from "@/utils/request";

export default {
  name: "Aside",
  data(){
    return{
      userId:0,
      user:{},
      path:this.$route.path
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

      },

}
</script>

<style scoped>

.fontClass{
  font-size:14px;
  font-weight: bolder;
  font-family: Microsoft Yahei;
}

</style>