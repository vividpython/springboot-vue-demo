<template #default="scope">
  <div style="padding: 10px;">

    <!--功能区域-->
    <div style="margin: 10px;padding: 0px">
      <el-button color="#3B455B" @click="add">新增</el-button>
      <el-button color="#3B455B">导入</el-button>
      <el-button color="#3B455B">导出</el-button>
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
        <el-form-item>
          <el-button color="#E0BF75" style="margin-left: 5px" :icon="Search" @click="load">查询</el-button>
          <el-button color="#E0BF75" style="margin-left: 5px" :icon="RefreshLeft" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <!--内容区域-->
    <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100%;"
        border
        class="table"
        :stripe="false"
        fit
    >
      <el-table-column prop="id" label="ID" sortable/>
      <el-table-column prop="username" label="用户名"/>
      <el-table-column prop="nickName" label="昵称"/>
      <!--<el-table-column  label="角色">-->
      <!--  <template #default="scope">-->
      <!--    <span v-if="scope.row.roleId === 1">管理员</span>-->
      <!--    <span v-if="scope.row.roleId === 2">设计人员</span>-->
      <!--    <span v-if="scope.row.roleId === 3">其他人员</span>-->
      <!--  </template>-->
      <!--</el-table-column>-->
      <el-table-column label="用户角色" prop="role.name" />
      <el-table-column label="部门名称" prop="depart.name" />
      <el-table-column  label="头像">
        <template #default="scope">
          <el-image
              style="width: 50px; height: 50px"
              :src="scope.row.img || '/static/default.png'"
              :preview-src-list="[scope.row.img]"
              preview-teleported="preview-teleported"
          />
        </template>
      </el-table-column>
      <el-table-column fixed="right" label="操作" width="190">
        <template #default="scope">
          <div class="btn-group">
            <div class="col">
              <el-button size="small" @click="handleEdit(scope.row)"
              >编辑
              </el-button
              >
              <el-popconfirm title="你确定要删除用户吗?" @confirm="handleDelete(scope.row.id)">
                <template #reference>
                  <el-button type="danger" size="small">删除</el-button>
                </template>
              </el-popconfirm>
              <el-popconfirm title="重置密码后，账号原密码将会失效，确定重置?" @confirm="handleResetPassword(scope.row.id)">
                <template #reference>
                  <el-button type="warning" size="small">重置</el-button>
                </template>
              </el-popconfirm>
            </div>
          </div>
        </template>
      </el-table-column>
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
    <!--  新增对话框 -->
    <el-dialog v-model="dialogVisible" title="Tips" width="30%">
      <el-form :model="form" label-width="120px">
        <el-form-item label="用户名">
          <el-input v-model="form.username" style="width: 80%"/>
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="form.nickName" style="width: 80%"/>
        </el-form-item>
        <el-form-item label="角色类型" prop="RoleId">
          <el-autocomplete
              v-model="roleInputValue"
              :fetch-suggestions="queryRoleSearchAsync"
              :popper-append-to-body="true"
              :placeholder="'请输入内容'"
              :trigger-on-focus="true"
              :value-key="'label'"
              @select="handleRoleSelect"
          ></el-autocomplete>
        </el-form-item>
        <el-form-item label="部门名称" prop="departId">
          <el-autocomplete
              v-model="departInputValue"
              :fetch-suggestions="queryDepartSearchAsync"
              :popper-append-to-body="true"
              :placeholder="'请输入内容'"
              :trigger-on-focus="true"
              :value-key="'label'"
              @select="handleDepartSelect"
          ></el-autocomplete>
        </el-form-item>
        <el-form-item label="头像">
          <el-upload ref="upload" :action="filesUploadUrl"
                     :headers="headers"
                     :data="importData"
                     :on-success="filesUploadSuccess">
            <el-button type="primary">Click to upload</el-button>
            <template #tip>
              <div class="el-upload__tip">
                jpg/png files with a size less than 500KB.
              </div>
            </template>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="save">
          Confirm
        </el-button>
      </span>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import {RefreshLeft, Search} from '@element-plus/icons-vue';
</script>
<script>

import request from "@/utils/request";
import {ElMessage} from "element-plus";

export default {
  name: 'User',
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
      options: []
    }
  },
  created() {
    console.log(this.headers);
    this.load();
    this.getDepartList();
    this.getRoleList();
  },
  methods: {

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
    // load(){
    //   request.get("/user",{
    //     params:{
    //       index:this.currentPage,
    //       size:this.pageSize,
    //       search:this.formInline.nickName
    //     }
    //   }).then(res => {
    //     console.log(res);
    //     this.tableData = res.data.records;
    //     this.total = res.data.total;
    //   })
    // },

    reset() {
      this.formInline = {};
      this.$refs["resetformInline"].resetFields();
    },
    add() {
      let _this = this;
      _this.dialogVisible = true;
      _this.form = {};
      this.load();//更新表单数据
      this.$nextTick(()=>{
        this.$refs["upload"].clearFiles();
      })
    },
    save() {
      //此处需要先对用户选择的部门信息进行
      //更新
      if (this.form.id) {
        request.put("/user", this.form).then(res => {
          console.log(res);
          if (res.code === '0') {
            ElMessage({
              message: '修改成功',
              type: 'success',
            })
          } else {
            ElMessage({
              message: '修改失败',
              type: 'error',
            })
          }
        });


      } else {
        //新增
        request.post("/user", this.form).then(res => {
          console.log(res);
          if (res.code === '0') {
            ElMessage({
              message: res.msg,
              type: 'success',
            })
          } else {
            ElMessage({
              message: res.msg,
              type: 'error',
            })
          }
        });

      }

      this.dialogVisible = false;//关闭弹窗
      this.load();//更新表单数据
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row));
      this.dialogVisible = true;
      this.$nextTick(()=>{
        this.$refs["upload"].clearFiles();
      })
    },
    handleSizeChange() {
      this.load()
    },
    handleCurrentChange() {
      this.load()
    },
    handleDelete(id) {
      console.log(id)
      request.delete("/user/" + id).then(res => {
        console.log(res);
        if (res.code === '0') {
          ElMessage({
            message: '删除成功',
            type: 'success',
          })
        } else {
          ElMessage({
            message: '删除失败',
            type: 'error',
          })
        }
        this.load();
      });
    },
    handleResetPassword(id) {
      console.log(id)
      request.get("/user/resetPassword/" + id).then(res => {
        console.log(res);
        if (res.code === '0') {
          ElMessage({
            message: '删除成功',
            type: 'success',
          })
        } else {
          ElMessage({
            message: '删除失败',
            type: 'error',
          })
        }
        this.load();
      });
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
