<template #default="scope">
  <div style="padding: 10px;">

    <!--功能区域-->
    <div style="margin: 10px;padding: 0px">
      <el-button color="#E0BF75" @click="add">新增</el-button>
      <el-button color="#958CDD"  @click="exportExcel">导出</el-button>
      <el-button color="#FC9DA9" @click="load">刷新</el-button>
    </div>
    <!--搜索区域-->
    <div style="margin: 10px ; padding: 0px">
      <el-form :inline="true" :model="formInline" class="demo-form-inline" ref="resetformInline">
        <el-form-item label="料号">
          <el-input v-model="formInline.productNo" placeholder="please input" clearable />
        </el-form-item>
        <el-form-item label="图纸名称">
          <el-input v-model="formInline.drawingName" placeholder="Please input" />
        </el-form-item>
        <el-form-item label="图纸类型">
          <el-select v-model="formInline.drawingType" placeholder="Please select">
            <el-option label="材料清单" value="1" />
            <el-option label="定屏图纸" value="2" />
            <el-option label="配线图" value="3" />
            <el-option label="技术交底单" value="4" />
          </el-select>
        </el-form-item>

        <el-form-item label="存储路径">
          <el-input v-model="formInline.drawingPath" placeholder="Please input" />
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
      <el-table-column prop="productNo" label="产品料号"/>
      <el-table-column prop="itemNo" label="项目编号"/>
      <el-table-column prop="drawingName" label="图纸名称"/>
      <el-table-column prop="drawingType" label="图纸类型">
        <template #default="scope">
          {{ getDrawingType(scope.row.drawingType) }}
        </template>
      </el-table-column>
      <el-table-column prop="drawingPath" label="存储路径"/>
      <el-table-column prop="createTime" label="创建时间" />
      <el-table-column prop="updateTime" label="更新时间"/>
      <el-table-column fixed="right" label="操作" width="220">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)"
          >编辑
          </el-button
          >
          <el-button size="small" @click="previewFile(scope.row)"
          >预览
          </el-button
          >
          <el-popconfirm title="Are you sure to delete this?" @confirm="handleDelete(scope.row.id,scope.row.drawingPath)">
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
        <el-form-item label="产品料号">
          <el-input v-model="form.productNo" style="width: 80%"/>
        </el-form-item>
        <el-form-item label="项目编号">
          <el-input v-model="form.itemNo" style="width: 80%"/>
        </el-form-item>
        <el-form-item label="图纸名称">
          <el-input v-model="form.drawingName" style="width: 80%"/>
        </el-form-item>
        <el-form-item label="图纸类型" >
          <div class="radio-group-container">
            <el-radio-group v-model="form.drawingType">
              <el-radio :label="1">材料清单</el-radio>
              <el-radio :label="2">定屏图纸</el-radio>
              <el-radio :label="3">配线图</el-radio>
              <el-radio :label="4">技术交底单</el-radio>
            </el-radio-group>
          </div>
        </el-form-item>
        <el-form-item label="选择文件">
          <el-upload ref="upload"
                     :auto-upload="false"
                     :http-request="uploadFile"
                     :disabled="isDisabledUpload">
              <template #trigger>
                <el-button type="primary">选择文件</el-button>
              </template>
            <template #tip>
              <div class="el-upload__tip">
                Any files with a size less than 10MB.
              </div>
            </template>

          </el-upload>
        </el-form-item>
        <el-form-item label="上传文件">
        <div>
          <el-button type="primary" @click="handleBeforeUpload">点击上传</el-button>
        </div>
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="cancel">取消</el-button>
        <el-button type="primary" @click="save">
          确认
        </el-button>
      </span>
      </template>
    </el-dialog>
  </div>
</template>
<script setup>
import { RefreshLeft, Search } from '@element-plus/icons-vue';

</script>

<script>

import request from "@/utils/request";
import {ElMessage} from "element-plus";
import * as XLSX from 'xlsx';
import * as FileSaver from 'file-saver';
import { Base64 } from "js-base64";
import axios from "axios";
import {ref} from "vue";

let upload = ref();
export default {
  name: 'Drawing',
  components: {},
  data() {
    return {
      previewUrl:'',
      form:{},
      previewForm:{},
      formInline:{},
      search: '',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      // 新增数据的对话框显示控制
      dialogVisible:false,
      fileData:'', // 表单数据+文件
      // importData:form,
      // headers:{
      //   token: JSON.parse(sessionStorage.getItem('token'))
      // },
      oldFilePath:'',
      tableData: [
      ],
      // filesUploadUrl:"http://" + window.server.filesUploadUrl + ":9090/files/uploadDrawingFiles"
    }
  },
  created() {
    this.load()
  },
  computed: {
    isDisabledUpload() {
      const { productNo, drawingName, drawingType } = this.form || {}
      return !productNo?.trim() || !drawingName?.trim() || !drawingType
    }
  },
  methods: {

    uploadFile(params){
      this.fileData = new FormData()
      let string = JSON.stringify(this.form)
      this.fileData.append('drawing', string )
      this.fileData.append("file",params.file) // append增加数据
      axios.request({
        method: 'post',
        url: 'http://' + window.server.filesUploadUrl + ':9090/files/uploadDrawingFiles',
        headers: {
          'Content-Type': 'multipart/form-data',
          'token': JSON.parse(sessionStorage.getItem('token'))
        },
        data: this.fileData
      }).then(response => {
        let res = response.data;
            if (res.code === '0') {
              ElMessage({
                message: '上传成功',
                type: 'success',
              })
              this.form.drawingPath = res.data;
            }
          }).catch(() => {
      })
    },
    handleBeforeUpload(){
      upload.value.submit()
    },

    exportExcel() {
      // 获取当前查询结果的数据
      const data = this.tableData;
      // 转换为Excel文件格式
      const ws = XLSX.utils.json_to_sheet(data);
      const wb = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(wb, ws, 'Sheet1');
      // 保存文件
      const fileName = '图纸数据.xlsx';
      const blob = new Blob([XLSX.write(wb, { bookType: 'xlsx', type: 'array' })], { type: 'application/octet-stream' });
      FileSaver.saveAs(blob, fileName);
    },
    getDrawingType (type) {
      switch (type) {
        case 1:
          return '材料清单'
        case 2:
          return '定屏图纸'
        case 3:
          return '配线图'
        case 4:
          return '技术交底单'
        default:
          return ''
      }
    },

    filesUploadSuccess(res){
     this.form.drawingPath = res.data;
    },
    delay(time) {
      return new Promise(resolve => setTimeout(resolve, time));
    },
    async load(){
      try {

        //延迟执行
        await this.delay(1000);

        // const url = "/drawing/" + this.currentPage + "/" + this.pageSize;
        request.post(`/drawing/${this.currentPage}/${this.pageSize}`,JSON.parse(JSON.stringify(this.formInline))
        ).then(res => {
          console.log(res);
          this.tableData = res.data.records.map(record => {
            const filePathArray = record.drawingPath.split('/');
            const fileName = filePathArray[filePathArray.length - 1].replace(/^[^_]+_/, '');
            const result = filePathArray.slice(3, filePathArray.length - 1).concat(fileName).join('/');
            return { ...record, drawingPath: result };
          });
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
        this.$refs["upload"].clearFiles();
      })
    },
    del_file(path){
      console.log("path" + path)
      request.post("/files/delDrawingFile",{delDrawingPath: path}).then(res=>{
            console.log(res);
            if (res.code === '0'){
              ElMessage({
                message: 'nginx文件同步成功',
                type: 'success',
              })
            }else {
              ElMessage({
                message: 'nginx文件同步失败',
                type: 'error',
                //
              })}
      }
            )
    },
    save(){
      // Convert drawingType to an integer before submitting the form
      this.form.drawingType = parseInt(this.form.drawingType);
      //更新
      if (this.form.id){
        if (this.form.drawingPath !== this.oldFilePath){
          //如果存储的文件发生了变化 并且用户点击了确定删除掉源文件
          this.del_file(this.oldFilePath)
        }

        const fieldNames = {
          productNo: '产品编号',
          drawingType: '图纸类型',
          drawingName: '图纸名称',
          drawingPath: '图纸文件',
        };
        const requiredFields = Object.keys(fieldNames);
        let isFormValid = true;
        let missingFields = [];
        for (let i = 0; i < requiredFields.length; i++) {
          const field = requiredFields[i];
          if (!this.form[field]) {
            isFormValid = false;
            missingFields.push(fieldNames[field]);
          }
        }
        if (isFormValid) {
          request.put("/drawing",this.form).then(res=>{

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
        } else {
          ElMessage({
            message: `缺少以下字段：${missingFields.join(', ')}`,
            type: 'warning',
          })
        }


      }else{
        const fieldNames = {
          productNo: '产品编号',
          drawingType: '图纸类型',
          drawingName: '图纸名称',
          drawingPath: '图纸文件',
        };
        const requiredFields = Object.keys(fieldNames);
        let isFormValid = true;
        let missingFields = [];
        for (let i = 0; i < requiredFields.length; i++) {
          const field = requiredFields[i];
          if (!this.form[field]) {
            isFormValid = false;
            missingFields.push(fieldNames[field]);
          }
        }
        if (isFormValid) {
          //新增
          request.post("/drawing",this.form).then(res=>{
            console.log("进入新增");
            console.log("once time" + this.form.drawingPath);
            console.log("res:" + res);
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
              console.log("twice time" + this.form.drawingPath);
              //   如果新增失败 需要发起删除掉上传到文件服务器的文件
              this.del_file(this.form.drawingPath)
            }
          });
        } else {
          ElMessage({
            message: `缺少以下字段：${missingFields.join(', ')}`,
            type: 'warning',
          })
        }

      }

      this.dialogVisible = false;//关闭弹窗
    },
    //如果用户在新增或者修改窗口点击了取消
    cancel(){
      //   this.form.drawingType = parseInt(this.form.drawingType);

      //只要用户在取消之前进行了文件上传的工作 并且点击了取消 都要删除掉本次上传的文件
      if (this.form.drawingPath !== this.oldFilePath){
        this.del_file(this.form.drawingPath)
      }
      //关闭对话框
      this.dialogVisible = false;
    },
    handleEdit(row) {
      this.form = JSON.parse(JSON.stringify(row));
      this.oldFilePath =  this.form.drawingPath;
      this.dialogVisible = true;
      this.$nextTick(()=>{
        this.$refs["upload"].clearFiles();
      })
      //编辑记录的时候 如果重新上传了文件 要删除掉原文件
    },
    previewFile(row){
      this.form = JSON.parse(JSON.stringify(row));

      request.get("/drawing/" + this.form.id).then(res=>{
        console.log(res);
        if (res.code === '0'){

          //查询成功之后 开始进行调用预览
          this.previewUrl = "http://"  + res.data.drawingPath;
          console.log(this.previewUrl);
          window.open('http://127.0.0.1:8012/onlinePreview?url='+encodeURIComponent(Base64.encode(this.previewUrl)));
        }else {
          ElMessage({
            message: '预览失败',
            type: 'error',
          })
        }
      });

    },
    handleSizeChange() {
      this.load()
    },
    handleCurrentChange() {
      this.load()
    },
    handleDelete(id,drawingPath){
      console.log(id)
      request.delete("/drawing/" + id).then(res=>{
        console.log(res);
        if (res.code === '0'){
          ElMessage({
            message: '删除成功',
            type: 'success',
          })
          //文件记录删除之后 同样要删除掉源文件
          this.del_file(drawingPath)
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
