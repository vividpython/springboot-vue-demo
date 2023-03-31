<template #default="scope">
  <div style="padding: 10px;">

    <!--功能区域-->
    <div style="margin: 10px;padding: 0px">
      <el-button color="#E0BF75" @click="add">新增</el-button>
      <el-button color="#958CDD" >导出</el-button>
      <el-button color="#FC9DA9" @click="load">刷新</el-button>
    </div>
    <!--搜索区域-->
    <div style="margin: 10px ; padding: 0px">
      <el-form :inline="true" :model="formInline" class="demo-form-inline" ref="resetformInline">
        <el-form-item label="标题">
          <el-input v-model="formInline.title" placeholder="please input" clearable />
        </el-form-item>
        <el-form-item label="作者">
          <el-input v-model="formInline.author" placeholder="Please input" />
        </el-form-item>
        <el-form-item>
          <el-button color="#E0BF75" style="margin-left: 5px" :icon="Search" @click="load">查询</el-button>
          <el-button color="#E0BF75" style="margin-left: 5px" :icon="RefreshLeft" @click="reset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>
    <!--内容区域-->
    <el-table
        :data="tableData"
        style="width: 100%"
        border
        class="table"
        :stripe="false"
    >
      <el-table-column prop="id" label="ID" sortable/>
      <el-table-column prop="title" label="标题"/>
      <el-table-column prop="author" label="作者"/>
      <el-table-column prop="time" label="时间"/>
      <el-table-column fixed="right" label="操作" width="200">
        <template #default="scope">
          <el-button size="small" @click="details(scope.row)"
          >详情
          </el-button
          >
          <el-button size="small" @click="handleEdit(scope.row)"
          >编辑
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
    <el-dialog v-model="dialogVisible" title="Tips" width="50%">
      <el-form :model="form" label-width="120px">
        <el-form-item label="标题">
          <el-input v-model="form.title" style="width: 50%"/>
        </el-form-item>

        <div id="div1"></div>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">
          确认
        </el-button>
      </span>
      </template>
    </el-dialog>

    <!--  新增对话框 -->
    <el-dialog v-model="vis" title="详情" width="50%">
     <el-card>
       <div v-html="detail.content" style="min-height: 100px"></div>
     </el-card>
    </el-dialog>

  </div>
</template>
<script setup>
import { RefreshLeft, Search } from '@element-plus/icons-vue';

</script>

<script>

import request from "@/utils/request";
import {ElMessage} from "element-plus";
import E from 'wangeditor';

let editor;
export default {
  name: 'Drawing',
  components: {},
  data() {
    return {
      form:{},
      formInline:{},
      search: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      // 新增数据的对话框显示控制
      dialogVisible:false,
      vis:false,
      tableData: [
      ],
      user:{},
      detail:{},
    }
  },
  created() {
    this.load()
  },
  methods: {
    details(row){

      this.detail = row
      this.vis = true
    },

    delay(time) {
      return new Promise(resolve => setTimeout(resolve, time));
    },
    async load(){
      try {

        //延迟执行
        await this.delay(1000);

        request.post(`/news/${this.currentPage}/${this.pageSize}`,JSON.parse(JSON.stringify(this.formInline))
        ).then(res => {
          console.log(res);
          this.tableData = res.data.records;
          this.total = res.data.total;


        })
      }catch (error)
      {
        console.error(error)
      }

    },

    reset(){
      this.formInline = {
      };
      this.$refs["resetformInline" ].resetFields();
    },
    add(){
      let _this = this;
      _this.dialogVisible = true;
      _this.form = {};

      this.$nextTick(()=>{
        editor = new E('#div1')
        let token = '';
        if (sessionStorage.getItem("token")) {
          token = JSON.parse(sessionStorage.getItem("token"));
        }
        editor.config.uploadImgServer = "http://" + window.server.filesUploadUrl + ":9090/files/editorUpload"
        editor.config.uploadImgHeaders = {
          'token':  token // 将token添加到header的Authorization字段中
        };
        editor.config.uploadFileName = "file"
        editor.create()
      })
    },
    save(){
      console.log(editor.txt.html())
      this.form.content = editor.txt.html()


      //更新
      if (this.form.id){
        request.put("/news",this.form).then(res=>{

          if (res.code === '0'){
            ElMessage({
              message: '修改成功',
              type: 'success',
            })
            this.load();//更新表单数据
          }else {
            ElMessage({
              message: res.msg,
              type: 'error',
            //
            })
          }
        });


      }else{
        request.get("/getUserInfo",
        ).then(res => {
          this.userId = res.data;
          request.get("/user/" + this.userId
          ).then(res => {
            if (res.code === "0")
            {
              this.user = res.data;
              this.form.author = this.user.nickName;
              console.log("author==" + this.form.author)
              //新增
              request.post("/news",this.form).then(res=>{
                console.log("author==" + this.form.author)
                if (res.code === '0'){
                  ElMessage({
                    message: '新增成功',
                    type: 'success',
                  })
                  this.load();//更新表单数据
                }else {
                  console.log("this.form" + this.form);
                  ElMessage({
                    message:  res.msg,
                    type: 'error',

                  })
                }
              });
            }
          })
        });


      }

      this.dialogVisible = false;//关闭弹窗
    },


    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row));
      this.dialogVisible = true;
      this.$nextTick(()=>{
        if (!editor) {
          editor = new E('#div1')
          let token = '';
          if (sessionStorage.getItem("token")) {
            token = JSON.parse(sessionStorage.getItem("token"));
          }
          editor.config.uploadImgServer = "http://" + window.server.filesUploadUrl + ":9090/files/editorUpload"
          editor.config.uploadImgHeaders = {
            'token':  token // 将token添加到header的Authorization字段中
          };
          editor.create()
          editor.txt.html(row.content);
        }else{
          editor.txt.html();
          editor.txt.html(row.content);
        }
      })
      //编辑记录的时候 如果重新上传了文件 要删除掉原文件
    },
    handleSizeChange() {
      this.load()
    },
    handleCurrentChange() {
      this.load()
    },
    handleDelete(id){
      console.log(id)
      request.delete("/news/" + id).then(res=>{
        console.log(res);
        if (res.code === '0'){
          ElMessage({
            message: '删除成功',
            type: 'success',
          })
        }else {
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

.radio-group-container {
  display: flex;
}
</style>
