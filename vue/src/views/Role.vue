<template #default="scope">
  <div style="padding: 10px;">

    <!--功能区域-->
    <div style="margin: 10px;padding: 0px">
      <el-button color="#E0BF75" @click="add">新增</el-button>
      <el-button color="#958CDD">导入</el-button>
      <el-button color="#FFDDAB">导出</el-button>
      <el-button color="#FC9DA9">刷新</el-button>
    </div>
    <!--搜索区域-->
    <div style="margin: 10px ; padding: 0px">
      <el-form :inline="true" :model="formInline" class="demo-form-inline" ref="resetformInline">
        <el-form-item label="角色名称">
          <el-input v-model="formInline.name" placeholder="please input" clearable/>
        </el-form-item>
        <el-form-item>
          <el-button color="#E0BF75" style="margin-left: 5px" :icon="Search" @click="load">查询</el-button>
          <el-button color="#E0BF75" style="margin-left: 5px" :icon="RefreshLeft" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!--  权限修改对话框 -->
    <el-dialog v-model="permsdialogVisible" title="Tips" width="30%">
      <el-tree
          :data="allPermissions"
          show-checkbox
          default-expand-all
          node-key="id"
          ref="tree"
          highlight-current
          :props="defaultProps">
      </el-tree>
      <div slot="footer" class="dialog-footer">
        <el-button @click="permsdialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="setRolePermissions()">确 定</el-button>
      </div>
    </el-dialog>

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
      <el-table-column prop="name" label="角色名称"/>
      <el-table-column prop="roleKey" label="角色权限字"/>
      <el-table-column prop="createTime" label="创建时间"/>
      <el-table-column prop="updateTime" label="更新时间"/>
      <el-table-column fixed="right" label="操作" width="170">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)"
          >编辑
          </el-button
          >
          <el-button size="small" @click="handlePerms(scope.row)"
          >修改权限
          </el-button
          >
          <el-popconfirm title="Are you sure to delete this?" @confirm="handleDelete(scope.row.id)">
            <template #reference>
              <el-button type="danger" size="small">删除</el-button>
            </template>
          </el-popconfirm>
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
        <el-form-item label="角色名称">
          <el-input v-model="form.name" style="width: 80%"/>
        </el-form-item>
        <el-form-item label="角色权限关键字">
          <el-input v-model="form.roleKey" style="width: 80%"/>
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
  name: 'Role',
  components: {},
  data() {
    return {
      form: {},
      loading: false,
      formInline: {},
      search: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      // 新增数据的对话框显示控制
      dialogVisible: false,
      //权限配置对话框显示控制
      permsdialogVisible:false,


      allPermissions: [],
      rolePermissions:[],
      defaultProps: {
        children: 'subPermissions',
        label: 'name'
      },


      importData:{imporType:1},
      headers:{
        token: JSON.parse(sessionStorage.getItem('token'))
      },

      tableData: [],
      RoleId:-1,
    }
  },
  created() {
    console.log(this.headers)
    this.load()
  },

  methods: {
    filesUploadSuccess(res){
      this.form.img = res.data;
    },
    delay(time) {
      return new Promise(resolve => setTimeout(resolve, time));
    },
    async load() {
      try {
        this.loading = true; // 显示Loading遮罩
        //延迟执行
        await this.delay(1000);
        console.log("this.formInline:"+this.formInline);
        request.post(`/role/${this.currentPage}/${this.pageSize}`, JSON.parse(JSON.stringify(this.formInline))
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
      //更新
      if (this.form.id) {
        request.put("/role", this.form).then(res => {
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
        request.post("/role", this.form).then(res => {
          console.log(res);
          if (res.code === '0') {
            ElMessage({
              message: '新增成功',
              type: 'success',
            })
          } else {
            ElMessage({
              message: '新增失败',
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
    },
    //点击修改权限
    handlePerms(row){
      this.permsdialogVisible = true;
      this.RoleId = row.id
      try {
        //获取全部权限
        request.post(`/docperms/allPermissions`
        ).then(res => {
          console.log(res);
          if (res.code === '0') {
            this.allPermissions=res.data;
            console.log("this.allPermissions:"+this.allPermissions);
            //查询该角色拥有的权限
            try {
              request.get(`/docperms/getPermissionsByRoleId/${row.id}`).then(res => {
                if (res.code === '0') {
                  this.rolePermissions=res.data;
                  //设置勾选效果
                  this.$refs.tree.setCheckedNodes(this.rolePermissions);
                }
              })
            }catch (error){
              console.error(error)
            }
          }
        })
      }catch (error){
        console.error(error)
      }
    },

    //设置角色权限
    setRolePermissions(){
      console.log(this.$refs.tree.getCheckedKeys());
      //将数组转换成字符串
      let keys = this.$refs.tree.getCheckedKeys();
      let keyStr=""
      for (let i=0;i<keys.length;i++){
        keyStr+=keys[i]+",";
      }
      keyStr.substr(0,keyStr.length-1);
      console.log("keyStr:" + keyStr)
      request.post(`/docperms/setDocPermsByRoleId/${this.RoleId}`,{ keyStr: keyStr })
          .then(res=>{
            if (res.code === '0'){
              console.log("权限设置成功")
            }
          })
      this.permsdialogVisible=false;
    },
    handleSizeChange() {
      this.load()
    },
    handleCurrentChange() {
      this.load()
    },
    handleDelete(id) {
      console.log(id)
      request.delete("/role/" + id).then(res => {
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
    }
  }
}
</script>
<style>

.dialog-footer {
  margin-top: 20px;
}
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
