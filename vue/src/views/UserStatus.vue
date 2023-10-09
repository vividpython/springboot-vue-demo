<template #default="scope">
  <div style="padding: 10px;">

    <!--功能区域-->
    <div style="margin: 10px;padding: 0px">
      <el-button color="#3B455B">刷新</el-button>
    </div>
    <!--搜索区域-->
    <div style="margin: 10px ; padding: 0px">
      <el-form :inline="true" :model="formInline" class="demo-form-inline" ref="resetformInline">
        <el-form-item label="用户名">
          <el-input v-model="formInline.username" placeholder="please input" clearable/>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="formInline.nickName" placeholder="Please input"/>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="formInline.status"
                     placeholder="Please select"
                     collapse-tags
                     collapse-tags-tooltip
                     multiple >
            <el-option label="启用" value="0"/>
            <el-option label="禁用" value="1"/>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button color="#E0BF75" style="margin-left: 5px" :icon="Search" @click="load">查询</el-button>
          <el-button color="#E0BF75" style="margin-left: 5px" :icon="RefreshLeft" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <!--批量操作按钮区域-->
    <div style="margin: 10px ; padding: 0px">
      <el-popconfirm title="你确定要删除吗，删除之后数据将无法恢复?"
                     @confirm="handleBanUsersStatus">
        <template #reference>
          <el-button type="danger" >批量禁用</el-button>
        </template>
      </el-popconfirm>
      <el-popconfirm title="你确定要删除吗，删除之后数据将无法恢复?"
                     @confirm="handleOpenUsersStatus">
        <template #reference>
          <el-button type="primary" >批量启用</el-button>
        </template>
      </el-popconfirm>
    </div>
    <!--内容区域-->
    <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%;"
        border
        class="table"
        :stripe="false"
        @selection-change="handleSelectionChange"
        fit
    >
      <el-table-column type="selection" width="55"/>
      <el-table-column prop="id" label="ID" sortable/>
      <el-table-column prop="username" label="用户名"/>
      <el-table-column prop="nickName" label="昵称"/>
      <el-table-column
          prop="status"
          label="是否启用"
          align="center"
          sortable="custom"
          show-overflow-tooltip
      >
        <template #default="scope">
          <el-switch
              :model-value="scope.row.status"
              :active-value="0"
              :inactive-value="1"
              size="large"
              class="ml-2"
              inline-prompt
              active-text="启用"
              inactive-text="禁用"
              @change="statusChange(scope.row)"
              :before-change="beforeChangeStatus.bind(this, scope.row)"
              style="--el-switch-on-color: #409EFF; --el-switch-off-color: #ff4949"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="用户角色" prop="role.name" />
      <el-table-column label="部门名称" prop="depart.name" />
    </el-table>
    <!--分页条-->
    <div style="margin: 10px ; padding: 0px">
      <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[5, 10, 20, 40]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
      />
    </div>
  </div>
</template>
<script setup>
import {RefreshLeft, Search} from '@element-plus/icons-vue';
</script>
<script>

import request from "@/utils/request";
import {ElMessage} from "element-plus";

export default {
  name: 'UserStatus',
  components: {},
  data() {
    return {
      form: {
        departId: '', // 初始值为空字符串
        roleId: '' // 初始值为空字符串
      },
      loading: false,
      formInline: {},
      departList:[],
      roleList:[],
      search: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      // 新增数据的对话框显示控制
      dialogVisible: false,
      importData:{imporType:1},
      headers:{
        token: JSON.parse(sessionStorage.getItem('token'))
      },

      tableData: [],
      filesUploadUrl:"http://" + window.server.filesUploadUrl + ":9090/files/uploadUserIcons",
      departInputValue: '',
      roleInputValue: '',
      options: [],
      ids: [],
      selectedFiles: [],
      status:-1,

    }
  },
  created() {
    console.log(this.headers);
    this.load();
    this.getDepartList();
    this.getRoleList();
  },
  methods: {
    handleOpenUsersStatus(){
      this.status = 0;
      this.handleUsersStatus();
    },
    handleBanUsersStatus(){
      this.status = 1;
      this.handleUsersStatus();
    },
    handleUsersStatus() {
      if (!this.ids.length) {
        ElMessage({
          message: '请选择数据',
          type: 'warning',
        })
        return
      }
      request.post(`/user/handleUsersStatus/${this.status}`, this.ids).then(res => {
            console.log(res);
            if (res.code === '0') {
              ElMessage({
                message: res.msg,
                type: 'success',
              });
            }else if(res.code === '101'){
              ElMessage({
                message: res.msg,
                type: 'warning',
              })
            } else {
              ElMessage({
                message: res.msg,
                type: 'info',
                //
              })
            }
            this.load();
          }
      )
    },



    //多选栏选择变化
    handleSelectionChange(val) {
      this.ids = val.map(v => v.id)
      this.selectedFiles = val
      console.log("this.selectedFiles:" + this.selectedFiles)
    },
    statusChange(row){
      console.log("row" + row.status);
      this.form = JSON.parse(JSON.stringify(row));
      console.log("this.form.status" + this.form.status);
      try {
        request.post("/user/updateUserstatus",this.form
        ).then(res => {
          console.log(res);
          if (res.code === '0') {
            if (this.form.status === 1){
              //表示用户想要作废文件
              ElMessage({
                message: '用户已禁用',
                type: 'warning',
              })
            }else{
              //表示用户想要启用文件
              ElMessage({
                message: '用户已启用',
                type: 'success',
              })
            }
          }else{

            ElMessage({
              message: res.msg,
              type: 'error',

            })
          }


        })
      } catch (error) {
        console.error(error)
      }
    },
    beforeChangeStatus(row) {
      this.form = JSON.parse(JSON.stringify(row));
      return new Promise((resolve, reject) => {
        try {
              if(row.status == 0){
                row.status = 1
              }else{
                row.status = 0
              }
              resolve(true)
            }catch (error) {
                reject(false)
           }
      })
    },
    handleDepartSelect(item){
      console.log("item.value"+item.value)  // 获取选中项的 value 值
      this.form.departId = item.value  // 将选中项的 value 赋值给其它变量
    },

    handleRoleSelect(item){
      console.log("item.value"+item.value)  // 获取选中项的 value 值
      this.form.roleId = item.value  // 将选中项的 value 赋值给其它变量
    },

    async queryDepartSearchAsync(queryString, cb) {
      try {
        console.log("this.departInputValue:" + this.departInputValue)
        request.post("/depart/findListByName",JSON.stringify(this.departInputValue)
        ).then(res => {
          console.log(res);
          // 将查询结果作为选项返回给组件
          this.options = res.data.map(item => ({ value: item.id, label: item.name }))
          console.log("this.options:" + JSON.stringify(this.options) )
          cb(this.options)
        })

      } catch (error) {
        console.error(error)
        cb([])
      }
    },
    async queryRoleSearchAsync(queryString, cb) {
      try {
        console.log("this.roleInputValue:" + this.roleInputValue)
        request.post("/role/findListByName",JSON.stringify(this.roleInputValue)
        ).then(res => {
          console.log(res);
          // 将查询结果作为选项返回给组件
          this.options = res.data.map(item => ({ value: item.id, label: item.name }))
          console.log("this.options:" + JSON.stringify(this.options) )
          cb(this.options)
        })

      } catch (error) {
        console.error(error)
        cb([])
      }
    },
    filesUploadSuccess(res){
      this.form.img = res.data;
      console.log("this.form.img:" + this.form.img)
    },
    delay(time) {
      return new Promise(resolve => setTimeout(resolve, time));
    },
    async getDepartList() {
      try {

        //延迟执行
        await this.delay(1000);
        //此处设置先按10个查
        request.post(`/depart/1/10`
        ).then(res => {
          console.log(res);
          this.departList = res.data.records;
        })
      }catch (error){
        console.error(error)
      }

    },
    async getRoleList() {
      try {

        //延迟执行
        await this.delay(1000);
        //此处设置先按10个查
        request.post(`/role/1/10`
        ).then(res => {
          console.log(res);
          this.roleList = res.data.records;
        })
      }catch (error){
        console.error(error)
      }

    },
    async load() {
      try {
        this.loading = true; // 显示Loading遮罩
        //延迟执行
        await this.delay(1000);
        console.log("this.formInline:"+this.formInline);
        // const url = "/user/" + this.currentPage + "/" + this.pageSize;
        request.post(`/user/${this.currentPage}/${this.pageSize}`, JSON.parse(JSON.stringify(this.formInline))
        ).then(res => {
          console.log(res);
          this.tableData = res.data.records;
          this.total = res.data.total;
        })
      }catch (error){
        console.error(error)
      }finally {
        this.loading = false; // 隐藏Loading遮罩
      }

    },

    reset() {
      this.formInline = {};
      this.$refs["resetformInline"].resetFields();
    },
    handleSizeChange() {
      this.load()
    },
    handleCurrentChange() {
      this.load()
    },
  }
}
</script>
<style>
.table tbody tr:nth-child(odd) {
  background-color: #F5F5F5;
}

.table tbody tr:nth-child(even) {
  background-color: #8A77A5;
}

.el-table_3_column_12 .cell {
  background-color: #F5F5F5;
}

.el-table_3_column_12 .cell.highlight {
  background-color: #8A77A5;
}
</style>
