<template>
  <div class="home-container">
    <h1 class="home-title">生产文件管理系统</h1>
    <div class="home-card-container">
      <div class="home-card">
        <i class="home-card-icon el-icon-folder-add"></i>
        <h2 class="home-card-title">上传生产文件</h2>
        <p class="home-card-desc">上传新的生产文件，并在系统中创建相应的记录。</p>
      </div>
      <div class="home-card">
        <i class="home-card-icon el-icon-document-checked"></i>
        <h2 class="home-card-title">查看生产记录</h2>
        <p class="home-card-desc">查看已上传的生产文件记录，并进行搜索、筛选等操作。</p>
      </div>
      <div class="home-card">
        <i class="home-card-icon el-icon-search"></i>
        <h2 class="home-card-title">搜索生产记录</h2>
        <p class="home-card-desc">使用关键词、日期等条件来搜索符合要求的生产记录。</p>
      </div>
      <div class="home-card">
        <i class="home-card-icon el-icon-setting"></i>
        <h2 class="home-card-title">系统设置</h2>
        <p class="home-card-desc">对生产文件管理系统的系统配置和参数进行修改和维护。</p>
      </div>
      <div class="home-card">
        <i class="home-card-icon el-icon-setting"></i>
        <el-button color="#3B455B" style="color: white" @click="computerCount" >计算人数</el-button>
        <p class="home-card-desc">对生产文件管理系统的系统配置和参数进行修改和维护。</p>
      </div>
    </div>
  </div>
</template>
<script>
import request from "@/utils/request";
import {ElMessage} from "element-plus";


export default {
  name: 'Home',
  components: {},
  data() {
    return {
      count:-1
    }
  },

  methods: {
    computerCount(){
      request.get("/getOnlineCount").then(res => {
            console.log(res);
            if (res.code === '0') {
              ElMessage({
                message: '接口获取成功',
                type: 'success',
                //
              })
              console.log("count:" + res.data);
            } else {
              ElMessage({
                message: '接口获取失败',
                type: 'error',
                //
              })
            }
          }
      )
    }
  }
}
</script>
<style scoped>
.home-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
}

.home-title {
  text-align: center;
  margin-bottom: 40px;
}

.home-card-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  grid-gap: 20px;
  max-width: 1200px;
}

.home-card {
  background-color: #f2f2f2;
  padding: 40px 20px;
  text-align: center;
  border-radius: 4px;
  box-shadow: 0 2px 4px rgba(0,0,0,0.2);
}

.home-card-icon {
  font-size: 60px;
  margin-bottom: 20px;
  color: #409EFF;
}

.home-card-title {
  font-size: 24px;
  margin-bottom: 10px;
}

.home-card-desc {
  font-size: 16px;
}
</style>
