<template #default="scope">
  <div style="padding: 10px;">

    <!--功能区域-->
    <div style="margin: 10px;padding: 0px">
      <el-button color="#E0BF75" @click="add" v-if="user.role !== 3">新增</el-button>
      <el-button color="#958CDD" @click="exportExcel">导出</el-button>
      <el-button color="#FC9DA9" @click="load">刷新</el-button>
    </div>
    <!--搜索区域-->
    <div style="margin: 10px ; padding: 0px">
      <el-form :inline="true" :model="formInline" class="demo-form-inline" ref="resetformInline">
        <el-form-item label="料号">
          <el-input v-model="formInline.materialNo" placeholder="please input" clearable/>
        </el-form-item>
        <el-form-item label="项目编号">
          <el-input v-model="formInline.itemNo" placeholder="Please input"/>
        </el-form-item>
        <el-form-item label="文件类型">
          <el-select v-model="formInline.documentType" placeholder="Please select">
            <el-option label="材料清单" value="1"/>
            <el-option label="装配工艺图" value="2"/>
            <el-option label="电气接线图" value="3"/>
            <el-option label="变更通知单" value="4"/>
            <el-option label="技术交底单" value="5"/>
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
      <el-button type="primary" @click="handleDownLoadDrawings">批量下载</el-button>
      <el-popconfirm title="Are you sure to delete this?"
                     @confirm="handleDeleteDrawings">
        <template #reference>
          <el-button type="danger" v-if="user.role !== 3">批量删除</el-button>
        </template>
      </el-popconfirm>
    </div>
    <!--内容区域-->
    <el-table
        v-loading="loading"
        :data="tableData"
        style="width: 100% "
        border
        class="table"
        :stripe="false"
        @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="55"/>
      <el-table-column prop="id" label="ID" sortable />
      <el-table-column prop="itemNo" label="项目编号" width="100"/>
      <el-table-column prop="materialNo" label="料号" width="100"/>
      <el-table-column prop="documentName" label="文件名称" show-overflow-tooltip/>
      <el-table-column prop="documentType" label="文件类型" show-overflow-tooltip>
        <template #default="scope">
          {{ getDocumentType(scope.row.documentType) }}
        </template>
      </el-table-column>
      <el-table-column prop="sequenceNo" label="文件序号" show-overflow-tooltip/>
      <el-table-column prop="documentVersion" label="文件版本" show-overflow-tooltip/>
      <el-table-column prop="documentPath" label="文件路径" :min-width="200" show-overflow-tooltip :ellipsis="true"
                       style="white-space: nowrap; word-break: break-all;"/>
      <el-table-column prop="createTime" label="创建时间" sortable show-overflow-tooltip/>
      <el-table-column prop="updateTime" label="更新时间" sortable  show-overflow-tooltip/>
      <el-table-column fixed="right" label="操作" width="220">
        <template #default="scope">
          <div class="btn-group">
            <div class="col">
              <el-button size="small" @click="previewFile(scope.row)"
              >预览
              </el-button
              >
              <el-button size="small" v-if="user.role !== 3" @click="updateVersion(scope.row)"
              >更新
              </el-button
              >
              <el-popconfirm title="Are you sure to delete this?"
                             @confirm="handleDelete(scope.row.id,scope.row.documentPath)">
                <template #reference>
                  <el-button type="danger" size="small" v-if="user.role !== 3">删除</el-button>
                </template>
              </el-popconfirm>
            </div>
            <div class="col">
              <el-button size="small" @click="downloadFile(scope.row)">下载文件</el-button>
              <el-button size="small" @click="showHistory(scope.row)">历史版本</el-button>
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
    <el-dialog v-model="dialogVisible" title="Tips" width="50%" :before-close="handleCloseDialog">
      <el-form :model="form" label-width="120px">
        <el-form-item label="项目编号">
          <el-input v-model="form.itemNo" style="width: 80%">
            <template #suffix>
              <el-icon class="el-input__icon" @click="handleIconClick"><search /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="料号">
          <el-input v-model="form.materialNo" style="width: 80%"/>
        </el-form-item>
        <el-form-item label="图纸类型">
          <div class="radio-group-container">
            <el-radio-group v-model="form.documentType">
              <el-radio :label="1">材料清单</el-radio>
              <el-radio :label="2">装配工艺图</el-radio>
              <el-radio :label="3">电气接线图</el-radio>
              <el-radio :label="4">变更通知单</el-radio>
              <el-radio :label="5">技术交底单</el-radio>
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

    <!--  更新文件对话框 -->
    <el-dialog v-model="updatedDialogVisible" title="Tips" width="50%" :before-close="handleCloseUpdateDialog">
      <el-form :model="form" label-width="120px">
        <el-form-item label="选择文件">
          <el-upload ref="updateDocument"
                     :auto-upload="false"
                     :http-request="updateFile">
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
            <el-button type="primary" @click="handleBeforeUpdate">点击上传</el-button>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button @click="cancelUpate">取消</el-button>
        <el-button type="primary" @click="saveDocument">
          确认
        </el-button>
      </span>
      </template>
    </el-dialog>

    <!--查看历史版本对话框-->
    <el-dialog v-model="historyDialogVisible" title="历史版本" width="80%" :close-on-click-modal="false"
               :before-close="handleDialogHisClose">
      <el-table
          v-loading="loading"
          :data="historyTableData"
          style="width: 100%"
          border
          class="table"
          :stripe="false"
      >
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="id" label="ID" show-overflow-tooltip/>
        <el-table-column prop="itemNo" label="项目编号" show-overflow-tooltip/>
        <el-table-column prop="materialNo" label="料号" show-overflow-tooltip/>
        <el-table-column prop="documentName" label="文件名称" show-overflow-tooltip/>
        <el-table-column prop="documentType" label="文件类型" show-overflow-tooltip>
          <template #default="scope">
            {{ getDocumentType(scope.row.documentType) }}
          </template>
        </el-table-column>
        <el-table-column prop="sequenceNo" label="文件序号" show-overflow-tooltip/>
        <el-table-column prop="documentVersion" label="文件版本" show-overflow-tooltip/>
        <el-table-column prop="documentPath" label="文件路径" show-overflow-tooltip/>
        <el-table-column prop="createTime" label="创建时间" show-overflow-tooltip/>
        <el-table-column prop="updateTime" label="更新时间" show-overflow-tooltip/>
        <el-table-column fixed="right" label="操作" width="220">
          <template #default="scope">
            <div class="btn-group">
              <div class="col">
                <el-button size="small" @click="previewFile(scope.row)"
                >预览
                </el-button
                >
                <el-button size="small" @click="downloadFile(scope.row)">下载文件</el-button>
              </div>
              <div class="col">
                <el-popconfirm title="Are you sure to delete this?"
                               @confirm="handleDelete(scope.row.id,scope.row.documentPath)">
                  <template #reference>
                    <el-button type="danger" size="small" v-if="user.role !== 3">删除</el-button>
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
            v-model:current-page="currentHisPage"
            v-model:page-size="pageHisSize"
            :page-sizes="[5, 10, 20, 40]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalHis"
            @size-change="handleHisSizeChange"
            @current-change="handleHisCurrentChange"
        />
      </div>
    </el-dialog>

    <!--查看ItemMaster对话框-->
    <el-dialog v-model="itemCodeSelectDialogVisable" title="料品档案" width="80%" :close-on-click-modal="false"
               :before-close="handleDialogItemClose">
      <div style="margin: 10px ; padding: 0px">
        <el-form :inline="true" :model="ItemFormInline" class="demo-form-inline" ref="resetformInline">
          <el-form-item label="料号">
            <el-input v-model="ItemFormInline.code" placeholder="please input" clearable/>
          </el-form-item>
          <el-form-item label="料品名称">
            <el-input v-model="ItemFormInline.name" placeholder="please input" clearable/>
          </el-form-item>
          <el-form-item label="规格(项目号)">
            <el-input v-model="ItemFormInline.specs" placeholder="please input" clearable/>
          </el-form-item>
          <el-form-item>
            <el-button color="#E0BF75" style="margin-left: 5px" :icon="Search" @click="itemMasterLoad">查询</el-button>
            <el-button color="#E0BF75" style="margin-left: 5px" :icon="RefreshLeft" @click="reset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
      <el-table
          v-loading="loading"
          :data="itemMasterTableData"
          style="width: 100%"
          border
          class="table"
          :stripe="false"
          @row-dblclick="handleRowDoubleClick"
      >
        <el-table-column prop="org" label="组织ID" sortable show-overflow-tooltip/>
        <el-table-column prop="id" label="料品ID" show-overflow-tooltip/>
        <el-table-column prop="code" label="料品编码" show-overflow-tooltip/>
        <el-table-column prop="specs" label="规格(项目号)" show-overflow-tooltip/>
        <el-table-column prop="name" label="料品名称" show-overflow-tooltip/>
        <el-table-column prop="effective" label="有效性" show-overflow-tooltip>
          <template #default="scope">
            {{ getIsEffective(scope.row.effective) }}
          </template>
        </el-table-column>
        <el-table-column prop="mainItemCategoryId" label="料品分类ID" show-overflow-tooltip/>
        <el-table-column prop="state" label="料品状态">
          <template #default="scope">
            {{ getItemState(scope.row.state) }}
          </template>
        </el-table-column>

        <el-table-column prop="itemFormAttribute" label="料品形态属性" show-overflow-tooltip/>

      </el-table>
      <!--分页条-->
      <div style="margin: 10px ; padding: 0px">
        <el-pagination
            v-model:current-page="currentItemPage"
            v-model:page-size="pageItemSize"
            :page-sizes="[5, 10, 20, 40]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalItem"
            @size-change="handleItemSizeChange"
            @current-change="handleItemCurrentChange"
        />
      </div>
    </el-dialog>

  </div>
</template>
<script setup>
import {RefreshLeft, Search} from '@element-plus/icons-vue';

</script>

<script>

import request from "@/utils/request";
import {ElMessage} from "element-plus";
import * as XLSX from 'xlsx';
import * as FileSaver from 'file-saver';
import {Base64} from "js-base64";
import axios from "axios";
import {ref} from "vue";
import JSZip from 'jszip'
let upload = ref();
let updateDocument = ref();

export default {
  name: 'Document',
  components: {},
  data() {
    return {
      previewUrl: '',
      form: {},
      historyForm: {},
      loading: false,
      previewForm: {},
      //搜索栏表单
      formInline: {},
      //ItemMaster搜索表单栏
      ItemFormInline: {},
      search: '',

      // //文件存储的真实路径 生产环境时应视需求修改
      // file_location:'/files/',

      //鉴权用的用户信息
      userId:0,
      user:{},



      currentPage: 1,
      pageSize: 10,

      currentHisPage: 1,
      pageHisSize: 10,

      currentItemPage: 1,
      pageItemSize: 10,

      //总条数
      total: 0,

      totalHis: 0,

      totalItem: 0,
      // 新增数据的对话框显示控制
      dialogVisible: false,
      //更新文件版本对话框显示控制
      updatedDialogVisible: false,
      //修改文件信息对话框显示控制
      editDialogVisible: false,
      //查看历史版本对话框显示控制
      historyDialogVisible: false,

      //控制新增信息时候得料号选择表格弹出
      itemCodeSelectDialogVisable:false,
      fileData: '', // 表单数据+文件
      EditData: '',//编辑更新数据 新数据行+编辑行
      // importData:form,
      // headers:{
      //   token: JSON.parse(sessionStorage.getItem('token'))
      // },
      oldFilePath: '',
      tableData: [],

      historyTableData: [],


      itemMasterTableData: [],


      ids: [],
      selectedFiles: [],
      // filesUploadUrl:"http://" + window.server.filesUploadUrl + ":9090/files/uploadDrawingFiles"
    }
  },
  created() {

    request.get("/getUserInfo",
    ).then(res => {
      this.userId = res.data;
      request.get("/user/" + this.userId
      ).then(res => {
        if (res.code === "0") {
          this.user = res.data;
        }
      })
    });
    this.load()
  },
  computed: {
    isDisabledUpload() {
      const {itemNo, documentType} = this.form || {}
      return !itemNo?.trim() || !documentType
    }
  },
  methods: {
    async handleDownLoadDrawings() {
      if (!this.ids.length) {
        ElMessage({
          message: '请选择数据',
          type: 'warning',
        })
        return
      }
      // for (const file of this.selectedFiles) {
      //   const url_raw = window.server.filesUploadUrl + ":" + window.server.filesUploadPort + file.documentPath.replace(window.server.filesUploadUrl,"");
      //   console.log(url_raw)
      //   const url = "http://" + url_raw;
      //   const link = document.createElement('a');
      //   link.href = url;
      //   link.target = "_blank";
      //   link.download = url.split('/').pop(); // 获取文件名
      //   link.click();
      //   await new Promise(resolve => setTimeout(resolve, 1000)); // 等待1s
      // }
      const zip = new JSZip()
      for (const file of this.selectedFiles) {
        const url_raw = window.server.filesUploadUrl + ':' + window.server.filesUploadPort + file.documentPath.replace(window.server.filesUploadUrl, '')
        console.log(url_raw)
        const url = 'http://' + url_raw
        const response = await axios.get(url, { responseType: 'blob' })
        const fileName = url.split('/').pop()
        zip.file(fileName, response.data)
      }

      const zipBlob = await zip.generateAsync({ type: 'blob' })
      const url = window.URL.createObjectURL(zipBlob)
      const link = document.createElement('a')
      link.href = url
      link.target = '_blank'
      link.download = 'drawings.zip' // 下载的文件名为 drawings.zip
      link.click()
    },
    //批量删除方法
    handleDeleteDrawings() {
      if (!this.ids.length) {
        ElMessage({
          message: '请选择数据',
          type: 'warning',
        })
        return
      }
      request.post("/document/deleteBatch", this.ids).then(res => {
            console.log(res);
            if (res.code === '0') {
              ElMessage({
                message: '批量删除成功',
                type: 'success',
              })
              this.del_files(this.ids).then(() => {  // 等待删除操作完成再执行load()方法
                this.load(); // 更新表单数据
              });
            } else {
              ElMessage({
                message: '批量删除失败',
                type: 'error',
                //
              })
            }
          }
      )
    },

    //多选栏选择变化
    handleSelectionChange(val) {
      this.ids = val.map(v => v.id)
      this.selectedFiles = val
      console.log("this.selectedFiles:" + this.selectedFiles)
    },

    handleIconClick() {
      console.log('Search icon clicked!');
      this.itemMasterLoad();
      this.itemCodeSelectDialogVisable = true;

    },

    //双击两行的事件
    handleRowDoubleClick(row) {
      // 处理表格行双击事件
      console.log('双击了表格行：', row);
      this.form.itemNo = row['specs'];
      this.form.materialNo = row['code'];
      this.handleDialogItemClose();

    },
    async itemMasterLoad() {
      try {

        this.loading = true; // 显示Loading遮罩

        //延迟执行
        await this.delay(1000);

        request.post(`/itemMaster/${this.currentItemPage}/${this.pageItemSize}`,JSON.parse(JSON.stringify(this.ItemFormInline))
        ).then(res => {
          console.log(res);
          this.itemMasterTableData = res.data.records;
          this.totalItem = res.data.total;


        })
      } catch (error) {
        console.error(error)
      } finally {
        this.loading = false; // 隐藏Loading遮罩
      }
    },
    async historyLoad() {
      try {
        console.log("loadingState:"+this.loading)
        this.loading = true; // 显示Loading遮罩

        //延迟执行
        await this.delay(1000);

        request.post(`/document/historyList/${this.currentHisPage}/${this.pageHisSize}`, this.historyForm
        ).then(res => {
          console.log(res);
          this.historyTableData = res.data.records;
          this.totalHis = res.data.total;


        })
      } catch (error) {
        console.error(error)
      } finally {
        this.loading = false; // 隐藏Loading遮罩
      }
    },
    //展示历史版本数据对话框点击事件
    showHistory(row) {
      this.historyForm = JSON.parse(JSON.stringify(row));
      this.historyLoad();
      this.historyDialogVisible = true;
    },
    //文件下载
    downloadFile(row) {
      const url_raw = window.server.filesUploadUrl + ":" + window.server.filesUploadPort + row.documentPath.replace(window.server.filesUploadUrl,"");
      console.log(url_raw)
      const url = "http://" + url_raw;
      const link = document.createElement('a');
      link.href = url;
      link.download = url.split('/').pop(); // 获取文件名
      link.click();
    },
    //更新文件版本按钮
    updateVersion(row) {
      this.form = JSON.parse(JSON.stringify(row));
      this.oldFilePath = this.form.documentPath;
      this.updatedDialogVisible = true;
      this.$nextTick(() => {
        this.$refs["updateDocument"].clearFiles();
      })
    },

    handleDialogHisClose() {
      this.historyTableData = [];
      this.historyDialogVisible = false;
    },
    handleDialogItemClose(){
      this.itemMasterTableData = [];
      this.itemCodeSelectDialogVisable = false;
    },
    handleCloseUpdateDialog() {
      // 调用 cancel 方法
      this.cancelUpate();
    },
    //关闭窗口的时候和cancel的时候一样 都要删除那些没有保存的文件
    handleCloseEditDialog() {
      // 调用 cancel 方法
      this.cancel();
    },
    handleCloseDialog() {
      // 调用 cancel 方法
      this.cancel();
    },
    updateFile(params) {
      this.fileData = new FormData()
      let string = JSON.stringify(this.form)
      this.fileData.append('document', string)
      this.fileData.append("file", params.file) // append增加数据
      axios.request({
        method: 'post',
        url: 'http://' + window.server.filesUploadUrl + ':9090/files/updateDocumentFiles',
        headers: {
          'Content-Type': 'multipart/form-data',
          'token': JSON.parse(sessionStorage.getItem('token'))
        },
        data: this.fileData
      }).then(response => {
        let res = response.data;
        if (res.code === '0') {
          ElMessage({
            message: '更新成功',
            type: 'success',
          })

          // 从返回结果中获取filePath和newVersion
          this.form.documentVersion = res.data.newVersion;
          this.form.documentPath = res.data.filePath;
        }
      }).catch(() => {
      })
    },
    handleBeforeUpdate() {
      updateDocument.value.submit()
    },
    uploadFile(params) {
      this.fileData = new FormData()
      let string = JSON.stringify(this.form)
      this.fileData.append('document', string)
      this.fileData.append("file", params.file) // append增加数据
      axios.request({
        method: 'post',
        url: 'http://' + window.server.filesUploadUrl + ':9090/files/uploadDocumentFiles',
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
          //如果是新增的文件 则默认其版本为A01
          this.form.documentVersion = "A01";
          this.form.documentPath = res.data;
          this.oldFilePath = this.form.documentPath;
        }
      }).catch(() => {
      })
    },

    handleBeforeUpload() {
      upload.value.submit()
    },

    exportExcel() {
      // 创建字段名映射表格
      const mapping = {
        id: '序号',
        itemNo: '项目编号',
        materialNo: '材料编号',
        documentName: '文档名称',
        documentType: '文档类型',
        sequenceNo: '文档序号',
        documentVersion: '文档版本',
        documentPath: '文档路径',
        createTime: '创建时间',
        updateTime: '更新时间'
      };
      // 将表格数据中的字段名替换为中文名
      const data = this.tableData.map(item => {
        const newItem = {};
        for (const key in item) {
          if (Object.prototype.hasOwnProperty.call(item, key)) {
            newItem[mapping[key] || key] = item[key];
          }
        }
        return newItem;
      });
      // 转换为Excel文件格式
      const ws = XLSX.utils.json_to_sheet(data);

      ws['!cols'] = [
        { wpx: 40},  // 第一列宽度为 100px
        { wpx: 100},  // 第二列宽度为 200px
        { wpx: 80 },  // 第三列宽度为 200px
        { wpx: 200 },
        { wpx: 60 },
        { wpx: 60},
        { wpx: 60},
        { wpx: 200 },
        { wpx: 120 },
        { wpx: 80 },
      ];
      const wb = XLSX.utils.book_new();
      XLSX.utils.book_append_sheet(wb, ws, 'Sheet1');
      // 保存文件
      const fileName = '生产文件列表.xlsx';
      const blob = new Blob([XLSX.write(wb, {bookType: 'xlsx', type: 'array'})], {type: 'application/octet-stream'});
      FileSaver.saveAs(blob, fileName);
    },
    getDocumentType(type) {
      switch (type) {
        case 1:
          return '材料清单'
        case 2:
          return '装配工艺图'
        case 3:
          return '电气接线图 '
        case 4:
          return '变更通知单'
        case 5:
          return '技术交底单'
        default:
          return ''
      }
    },
    getIsEffective(type) {
      switch (type) {
        case 0:
          return '失效'
        case 1:
          return '有效'
        default:
          return ''
      }
    },
    getItemState(type) {
      switch (type) {
        case 0:
          return '待核准'
        case 1:
          return '核准中'
        case 2:
          return '已核准'
        default:
          return ''
      }
    },
    filesUploadSuccess(res) {
      this.form.documentPath = res.data;
    },
    delay(time) {
      return new Promise(resolve => setTimeout(resolve, time));
    },
    async load() {
      try {

        this.loading = true; // 显示Loading遮罩
        //延迟执行
        await this.delay(1000);

        request.post(`/document/${this.currentPage}/${this.pageSize}`, JSON.parse(JSON.stringify(this.formInline))
        ).then(res => {
          console.log(res);
          this.tableData = res.data.records;
          this.total = res.data.total;


        })
      } catch (error) {
        console.error(error)
      } finally {
        this.loading = false; // 隐藏Loading遮罩
      }

    },

    reset() {
      this.ItemFormInline = {};
      this.$refs["resetformInline"].resetFields();
    },
    add() {

      this.dialogVisible = true;
      this.form = {};

      this.$nextTick(() => {
        this.$refs["upload"].clearFiles();
      })
    },
    //删除单个文件
    del_file(path) {
      console.log("path" + path)
      request.post("/files/delDocumentFile", {delDocumentPath: path}).then(res => {
            console.log(res);
            if (res.code === '0') {
              ElMessage({
                message: 'nginx文件同步成功',
                type: 'success',
              })
            } else {
              ElMessage({
                message: 'nginx文件同步失败',
                type: 'error',
                //
              })
            }
          }
      )
    },
    //批量删除文件
    del_files(ids) {
      return request.post("/files/delDocumentFiles", this.ids).then(res => {
            console.log(res);
            if (res.code === '0') {
              ElMessage({
                message: 'nginx文件同步成功',
                type: 'success',
              })
            } else {
              ElMessage({
                message: 'nginx文件同步失败',
                type: 'error',
                //
              })
            }
          }
      )
    },


    saveDocument() {
      // Convert documentType to an integer before submitting the form
      this.form.documentType = parseInt(this.form.documentType);
      //更新文件版本
      if (this.form.id) {

        const fieldNames = {
          itemNo: '产品编号',
          documentType: '图纸类型',
          materialNo: '料号',
          documentPath: '图纸文件',
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
          request.post("/document", this.form).then(res => {

            if (res.code === '0') {
              ElMessage({
                message: '更新成功',
                type: 'success',
              })
              this.load();//更新表单数据
            } else {
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
      }
      this.updatedDialogVisible = false;//关闭弹窗
    },
    save() {
      // Convert documentType to an integer before submitting the form
      this.form.documentType = parseInt(this.form.documentType);
      //新增
      const fieldNames = {
        itemNo: '产品编号',
        documentType: '图纸类型',
        documentPath: '图纸文件',
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
        request.post("/document", this.form).then(res => {
          console.log("进入新增");
          if (res.code === '0') {
            ElMessage({
              message: '新增成功',
              type: 'success',
            })
            this.load();//更新表单数据
          } else {
            console.log("this.form" + this.form);
            ElMessage({
              message: res.msg,
              type: 'error',

            })
            //   如果新增失败 需要发起删除掉上传到文件服务器的文件
            console.log(this.oldFilePath)
            this.del_file(this.form.documentPath)
          }
        });
      } else {
        ElMessage({
          message: `缺少以下字段：${missingFields.join(', ')}`,
          type: 'warning',
        })
      }
      this.dialogVisible = false;//关闭弹窗


    },

    cancelUpate() {
      //只要用户在取消之前进行了文件上传的工作 并且点击了取消 都要删除掉本次上传的文件
      if (this.form.documentPath !== this.oldFilePath) {
        console.log("用户上传过文件了")
        console.log("this.form.documentPath:" + this.form.documentPath)
        // this.del_file(this.form.documentPath.substring(this.form.documentPath.indexOf("/files/") + "/files/".length))
        // this.del_file(this.form.documentPath.substring(this.form.documentPath.indexOf(this.file_location) + this.file_location.length))
        this.del_file(this.form.documentPath)
      }
      //如果用户没有进行了文件上传的工作
      //关闭对话框
      this.updatedDialogVisible = false;
    },
    //如果用户在新增或者修改窗口点击了取消
    cancel() {
      //新增的时候 如果点击了取消 而且又上传过文件 则需要把这个文件删除掉
      if (this.oldFilePath !== "") {
        //如果存储的文件发生了变化 并且用户点击了确定删除掉源文件
        console.log("this.oldFilePath:" + this.oldFilePath)
        this.del_file(this.oldFilePath)
      }

      //关闭对话框
      this.dialogVisible = false;
    },
    previewFile(row) {
      this.form = JSON.parse(JSON.stringify(row));

      request.get("/document/getId/" + this.form.id).then(res => {
        console.log(res);
        if (res.code === '0') {

          //查询成功之后 开始进行调用预览
          this.previewUrl = "http://" + window.server.filesUploadUrl + ":"
              + window.server.filesUploadPort
              + res.data.documentPath.replace(window.server.filesUploadUrl,"");;
          console.log(this.previewUrl);

          window.open('http://' + window.server.filesUploadUrl  + ':'
              + window.server.kkfileviewPort +'/onlinePreview?url='
              + encodeURIComponent(Base64.encode(this.previewUrl)));
        } else {
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
    handleHisSizeChange() {
      this.historyLoad()
    },
    handleHisCurrentChange() {
      this.historyLoad()
    },

    handleItemSizeChange() {
      this.itemMasterLoad()
    },
    handleItemCurrentChange() {
      this.itemMasterLoad()
    },

    handleDelete(id, documentPath) {
      console.log(id)
      request.delete("/document/" + id).then(res => {
        console.log(res);
        if (res.code === '0') {
          ElMessage({
            message: '删除成功',
            type: 'success',
          })
          this.del_file(documentPath)
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
.table tbody tr:nth-child(odd) {
  background-color: #F5F5F5;
}

.table tbody tr:nth-child(even) {
  background-color: #8A77A5;
}

.table .el-table__row {
  height: 60px; /* 设置行高为60px，可以根据实际情况进行调整 */
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
