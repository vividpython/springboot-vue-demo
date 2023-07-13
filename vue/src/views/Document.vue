<template #default="scope">
  <div style="padding: 10px;">

    <!--功能区域-->
    <div style="margin: 10px;padding: 0px">
      <el-button color="#3B455B" style="color: white" @click="add" v-if="user.role.roleKey === 'superuser'">单选新增</el-button>
      <el-button color="#3B455B" style="color: white" @click="add1" v-if="user.role.roleKey === 'superuser' || user.role.roleKey === 'department_admin' || user.role.roleKey === 'designer'">多选新增</el-button>
      <el-button color="#3B455B" style="color: white" @click="updateM" v-if="user.role.roleKey === 'superuser' || user.role.roleKey === 'department_admin' || user.role.roleKey === 'designer'">设计变更</el-button>
      <el-button color="#3B455B" style="color: white" @click="exportExcel">导出</el-button>
      <el-button color="#3B455B" style="color: white" @click="load">刷新</el-button>
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
        <el-form-item label="创建人">
          <el-autocomplete
              v-model="userInputValue"
              :fetch-suggestions="queryUserSearchAsync"
              :popper-append-to-body="true"
              :placeholder="'请输入用户名'"
              :trigger-on-focus="true"
              :value-key="'label'"
              @select="handleUserSelect"
          ></el-autocomplete>
        </el-form-item>
        <el-form-item label="文件类型">
          <el-select v-model="formInline.documentType"
                     placeholder="Please select"
                     collapse-tags
                     collapse-tags-tooltip
                     multiple >
            <el-option label="材料清单" value="1"/>
            <el-option label="装配工艺图" value="2"/>
            <el-option label="电气接线图" value="3"/>
            <el-option label="变更通知单" value="4"/>
            <el-option label="技术交底单" value="5"/>
          </el-select>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-date-picker
              v-model="daterange"
              type="datetimerange"
              :shortcuts="shortcuts"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
          >
          </el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" style="margin-left: 5px" :icon="Search" @click="load">查询</el-button>
          <el-button type="warning" style="margin-left: 5px" :icon="RefreshLeft" @click="reset">重置</el-button>


        </el-form-item>
      </el-form>
    </div>
    <!--批量操作按钮区域-->
    <div style="margin: 10px ; padding: 0px">
      <el-button type="primary" @click="handleDownLoadDocuments">批量下载</el-button>
      <el-button  color="#626aef"
                  @click="handleVerifyDocuments"
                 v-if="user.role.roleKey === 'superuser' || user.role.roleKey === 'department_admin'"
      >批量校核</el-button>
      <el-popconfirm title="你确定要删除吗，删除之后数据将无法恢复?"
                     @confirm="handleDeleteDocuments"
                     v-if="user.role.roleKey === 'superuser'">
        <template #reference>
          <el-button type="danger" >批量删除</el-button>
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
      <el-table-column prop="user.nickName" label="创建人" show-overflow-tooltip/>
      <el-table-column prop="substitution.nickName" label="变更人" show-overflow-tooltip/>
      <el-table-column prop="documentPath" label="文件路径" :min-width="200" show-overflow-tooltip :ellipsis="true"
                       style="white-space: nowrap; word-break: break-all;"/>

      <el-table-column
          prop="deleted"
          label="是否启用"
          align="center"
          sortable
          show-overflow-tooltip
      >
        <template #default="scope">
          <el-switch
              v-model="scope.row.deleted"
              :active-value="0"
              :inactive-value="1"
              size="large"
              class="ml-2"
              inline-prompt
              active-text="启用"
              inactive-text="作废"
              :before-change="beforeChangeStatus.bind(this, scope.row)"
              style="--el-switch-on-color: #409EFF; --el-switch-off-color: #ff4949"
              v-if="user.role.roleKey === 'regular_user'"
              disabled
          ></el-switch>
          <el-switch
              v-else
              v-model="scope.row.deleted"
              :active-value="0"
              :inactive-value="1"
              size="large"
              class="ml-2"
              inline-prompt
              active-text="启用"
              inactive-text="作废"
              :before-change="beforeChangeStatus.bind(this, scope.row)"
              style="--el-switch-on-color: #409EFF; --el-switch-off-color: #ff4949"
          ></el-switch>
        </template>
      </el-table-column>
      <el-table-column label="审批状态">
        <template #default="scope">
          <el-button :type="scope.row.approvalStatus === 1 ? 'success' : 'danger'"
                     :disabled="true"
                     class="custom-button">
            {{ scope.row.approvalStatus === 1 ? '已校核' : '未校核' }}
          </el-button>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" sortable show-overflow-tooltip/>
      <el-table-column prop="updateTime" label="更新时间" sortable  show-overflow-tooltip/>
      <el-table-column fixed="right" label="操作" width="220">
        <template #default="scope">
          <div class="btn-group">
            <div class="col">
              <el-button size="small" color="#67C23A" style="color: white" @click="previewFile(scope.row)"
              >预览
              </el-button
              >
              <!--//单个文件更新功能现只对管理员开放-->
              <el-button size="small" color="#3BA6C4"  v-if="user.role.roleKey === 'superuser'" @click="updateVersion(scope.row)"
              >更新
              </el-button
              >
              <el-popconfirm title="你确定要删除吗，删除之后数据将无法恢复?"
                             @confirm="handleDelete(scope.row.id,scope.row.documentPath,scope.row.createTime)"
                             v-if="user.role.roleKey === 'superuser' || user.role.roleKey === 'department_admin' || user.role.roleKey === 'designer'">
                <template #reference>
                  <el-button color="#f56c6c" style="color: white" size="small" >删除</el-button>
                </template>
              </el-popconfirm>
            </div>
            <div class="col">
              <el-button size="small" color="#409EFF" style="color: white" @click="downloadFile(scope.row)">下载</el-button>
              <el-button size="small" color="#909399" style="color: white" @click="showHistory(scope.row)">历史</el-button>
              <el-popconfirm title="对文件校核后将自动通过，确定校核?"
                             @confirm="handVerify(scope.row)"
                             v-if="user.role.roleKey === 'superuser' || user.role.roleKey === 'department_admin'">
                <template #reference>
                  <el-button color="#3773EA" style="color: white" size="small" >校核</el-button>
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
                     :disabled="isDisabledUpload"
          >
            <template #trigger>
              <el-button type="primary">选择文件</el-button>
            </template>
            <template #tip>
              <div class="el-upload__tip">
               文件大小限制在50MB以内
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
    <!--  新增对话框多选 -->
    <el-dialog v-model="dialogVisibleM" title="Tips" width="50%" :before-close="handleCloseMDialog">
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
          <el-form-item label="选择文件">
            <el-upload ref="upload"
                       class="upload-demo"
                       style="display: inline"
                       :auto-upload="false"
                       :on-change="handleChange"
                       :file-list="fileList"
                       :disabled="isDisabledUploadM"
                       :multiple="true"
                       action="#">
              <template #trigger>
                <el-button type="primary">选择文件</el-button>
              </template>
              <template #tip>
                <div class="el-upload__tip">
                 文件大小限制在50MB以内
                </div>
              </template>
            </el-upload>
          </el-form-item>
          <el-form-item>
            <el-button  type="primary" @click="uploadFileM">点击上传</el-button>
          </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button type="primary" @click="closeM">
          关闭
        </el-button>
      </span>
      </template>
    </el-dialog>

    <!--  设计变更压缩包单选 -->
    <el-dialog v-model="updatedDialogVisibleM" title="设计变更" width="50%" :before-close="handleCloseUpdateMDialog">
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
        <el-form-item label="选择文件">
          <el-upload ref="upload"
                     class="upload-demo"
                     style="display: inline"
                     :auto-upload="false"
                     :on-change="handleChange"
                     :file-list="fileList"
                     :disabled="isDisabledUploadM"
                     action="#">
            <template #trigger>
              <el-button type="primary">选择文件</el-button>
            </template>
            <template #tip>
              <div class="el-upload__tip">
               文件大小限制在50MB以内
              </div>
            </template>

          </el-upload>
        </el-form-item>

        <el-form-item>
          <el-button  type="primary" @click="updateFileM">点击上传</el-button>
        </el-form-item>
      </el-form>
      <template #footer>
      <span class="dialog-footer">
        <el-button type="primary" @click="closeupdatedDialog">
          关闭
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
               文件大小限制在50MB以内
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
        <el-table-column prop="user.username" label="创建人" show-overflow-tooltip/>
        <el-table-column prop="substitution.username" label="变更人" show-overflow-tooltip/>
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
                <el-popconfirm title="你确定要删除吗，删除之后数据将无法恢复?"
                               @confirm="handleDelete(scope.row.id,scope.row.documentPath)"
                               v-if="user.role.roleKey === 'superuser' || user.role.roleKey === 'department_admin' || user.role.roleKey === 'designer'">
                  <template #reference>
                    <el-button type="danger" size="small" >删除</el-button>
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

import moment from 'moment'
let upload = ref();
let updateDocument = ref();

export default {
  name: 'Document',
  components: {},
  data() {
    return {
      previewUrl: '',
      form: {
      },
      fileList: [],
      historyForm: {},
      loading: false,
      previewForm: {},
      //搜索栏表单
      daterange: '',
      shortcuts: [
        {
          text: '最近一周',
          value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
            return [start, end]
          },
        },
        {
          text: '最近一个月',
          value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
            return [start, end]
          },
        },
        {
          text: '最近三个月',
          value: () => {
            const end = new Date()
            const start = new Date()
            start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
            return [start, end]
          },
        },
      ],
      formInline: {},
      //搜索栏搜索用户输入框内容
      userInputValue:'',
      //ItemMaster搜索表单栏
      ItemFormInline: {},
      search: '',

      // //文件存储的真实路径 生产环境时应视需求修改
      // file_location:'/files/',

      //鉴权用的用户信息
      userId:0,
      user:{
        role:{},
      },



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
      dialogVisibleM: false,
      //更新文件版本对话框显示控制
      updatedDialogVisible: false,
      updatedDialogVisibleM: false,
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

      errInsertUpdateFileList:[],

      itemMasterTableData: [],


      ids: [],
      selectedFiles: [],
      // filesUploadUrl:"http://" + window.server.filesUploadUrl + ":9090/files/uploadDrawingFiles"
    }
  },
  created() {

    request.get("/user/getUserInfo",
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
    },
    isDisabledUploadM() {
      const { itemNo } = this.form || {};
      return !itemNo?.trim();
    }

  },
  methods: {
    handleUserSelect(item){
      console.log("item.value"+item.value)  // 获取选中项的 value 值
      this.formInline.userId = item.value  // 将选中项的 value 赋值给其它变量
    },
    async queryUserSearchAsync(queryString, cb) {
      try {
        console.log("this.userInputValue:" + this.userInputValue)
        request.post("/user/findListByName",JSON.stringify(this.userInputValue)
        ).then(res => {
          console.log(res);
          // 将查询结果作为选项返回给组件
          this.options = res.data.map(item => ({ value: item.id, label: item.nickName }))
          console.log("this.options:" + JSON.stringify(this.options) )
          cb(this.options)
        })

      } catch (error) {
        console.error(error)
        cb([])
      }
    },

    async handleDownLoadDocuments() {
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
        console.log(file.documentPath.replace(window.server.filesUploadUrl, ''))
        const url_raw = window.server.filesUploadUrl + file.documentPath.replace(window.server.filesUploadUrl, '')
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
    handleDeleteDocuments() {
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
                message: res.msg,
                type: 'success',
              })
              let filePaths = res.data;
              console.log("filePaths:" + filePaths)
              this.del_files(filePaths).then(() => {  // 等待删除操作完成再执行load()方法
                this.load(); // 更新表单数据
              });
            }else if(res.code === '101'){
              ElMessage({
                message: res.msg,
                type: 'warning',
              })
              let filePaths = res.data;
              console.log("filePaths:" + filePaths)
              this.del_files(filePaths).then(() => {  // 等待删除操作完成再执行load()方法
                this.load(); // 更新表单数据
              })
            } else {
              ElMessage({
                message: res.msg,
                type: 'info',
                //
              })
            }
          }
      )
    },
    handleVerifyDocuments() {
      if (!this.ids.length) {
        ElMessage({
          message: '请选择数据',
          type: 'warning',
        })
        return
      }
      request.post("/document/verifyBatch", this.ids).then(res => {
            console.log(res);
            if (res.code === '0') {
              ElMessage({
                message: res.msg,
                type: '全部校核成功',
              })
              this.load();
            }else {
              ElMessage({
                message: res.msg,
                type: 'warning',
                //
              })
              this.load()
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
          this.$nextTick(() => {
            // 隐藏Loading遮罩
            this.loading = false;
          });

        })
      } catch (error) {
        console.error(error)
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
      const url_raw = window.server.filesUploadUrl + row.documentPath.replace(window.server.filesUploadUrl,"");
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
      try {
        request.post("/document/updateconfirm",this.form
        ).then(res => {
          console.log(res);
          if (res.code === '0') {
            ElMessage({
              message: '更新成功',
              type: 'success',
            })
            this.oldFilePath = this.form.documentPath;
            this.updatedDialogVisible = true;
            this.$nextTick(() => {
              this.$refs["updateDocument"].clearFiles();
            })
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
    handleCloseMDialog(){
      this.closeM();
    },
    handleCloseUpdateMDialog(){
      this.closeupdatedDialog();
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

    //通过onchanne触发方法获得文件列表
    handleChange(file, fileList) {
      this.fileList = fileList;
      console.log(fileList)
    },
    obsoleteChange(row){
      console.log("row" + row.deleted);
      this.form = JSON.parse(JSON.stringify(row));
      console.log("this.form.deleted" + this.form.deleted);

      try {
        request.post("/document/updatestatus",this.form
        ).then(res => {
          console.log(res);
          if (res.code === '0') {
            if (this.form.deleted === 1){
              //表示用户想要作废文件
              ElMessage({
                message: '文件已作废',
                type: 'success',
              })
            }else{
              //表示用户想要启用文件
              ElMessage({
                message: '文件已启用',
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
          request.post("/document/updateconfirm",this.form
          ).then(res => {

            if (res.code === '0') {
              //验证身份成功
              resolve(true)
            }else{

              ElMessage({
                message: res.msg,
                type: 'error',

              })
              reject(false)
            }


          })
        }catch (error) {
          reject(false)
        }
      })
    },
    async uploadFileM() {

      const deleteIndexList = [];

      // 定义一个数组，用于保存所有重复导入失败的文件名
      const failedFiles = [];
      // 定义一个数组，用于保存所有文件名错误的文件名
      const ErrNameFiles = [];
      // 定义一个数组，用于保存所有文件名错误的文件名
      const successFiles = [];

      // 创建包含五种类型的列表
      const typeList = ["清单", "工艺", "图纸", "变更单", "交底单"];


      for (let i = 0; i < this.fileList.length; i++) {
        let item = this.fileList[i];
        console.log(item.raw)
        console.log(this.fileList[0])

        // 获取当前文件名
        const fileName = item.raw.name;
        console.log("文件名称：" + fileName);
        // 判断文件名中是否包含字符串，如果包含，根据映射关系设置 this.form.documentType 的值
        if (fileName.indexOf('清单') !== -1 || fileName.indexOf('材料清单') !== -1) {
          console.log("matches.length");
          this.form.documentType = 1;
        } else if (fileName.indexOf('工艺') !== -1 || fileName.indexOf('装配工艺图') !== -1) {
          this.form.documentType = 2;
        } else if (fileName.indexOf('图纸') !== -1 || fileName.indexOf('电气接线图') !== -1) {
          this.form.documentType = 3;
        } else if (fileName.indexOf('变更单') !== -1 || fileName.indexOf('变更通知单') !== -1) {
          this.form.documentType = 4;
        } else if (fileName.indexOf('交底单') !== -1 || fileName.indexOf('技术交底单') !== -1) {
          this.form.documentType = 5;
        }else{
          console.log("名称错误");
          //如果上传的文件命名不规范则 直接删除并提示错误即可
          // 如果上传失败，将当前文件下标保存到 deleteIndexList 中
          ErrNameFiles.push(fileName);
          deleteIndexList.push(i);
          // 文件名不符合要求，直接跳过
          continue;
        }


        // 使用正则表达式匹配文件名中的数字
        const regex = /_(\d+)\./;
        const matches = fileName.match(regex);
        if (matches && matches.length > 1) {
          // 匹配成功，获取到数字，并根据数字设置 this.form.documentType 和 this.form.sequenceNo 的值
          this.form.sequenceNo = parseInt(matches[1]);
        }else{
          console.log("序号错误");
          //如果上传的文件命名不规范则 直接删除并提示错误即可
          // 如果上传失败，将当前文件下标保存到 deleteIndexList 中
          ErrNameFiles.push(fileName);
          deleteIndexList.push(i);
          // 文件名不符合要求，直接跳过
          continue;
        }

        //重复导入文件验证 因为该方法已经不再对序号进行管理 所以如果发现某项目某料号下的某种类型的某个序号的图纸再次被上传 应该对其进行拒绝



        console.log("this.form:" +  JSON.stringify(this.form))
        //先默认form中的版本号为A01 用insert
        this.form.documentVersion = "A01";
        let res = await request.post("/document/confirm", this.form);
        console.log("重复验证通过");
        if (res.code === '0') {
            //开始上传文件和生成记录

          let fileData = new FormData()
          let string = JSON.stringify(this.form)
          fileData.append('document', string)
          fileData.append('file', item.raw)
          console.log("document:" + string)
          try {
            const response = await axios.request({
              method: 'post',
              url: 'http://' + window.server.filesUploadUrl + ':9090/files/uploadDocumentFiles',
              headers: {
                'Content-Type': 'multipart/form-data',
                'token': JSON.parse(sessionStorage.getItem('token'))
              },
              data: fileData
            });
            let res = response.data;
            if (res.code === '0') {
              // 设置文件状态为已上传，并在文件名后面加上对号
              item.status = 'success';

              //上传成功之后把成功的文件加到列表中
              successFiles.push(fileName);
              // 如果是新增的文件，则默认其版本为A01
              this.form.documentVersion = "A01";
              this.form.documentPath = res.data;
              this.oldFilePath = this.form.documentPath;

              //开始执行记录插入及改文件名
              // Convert documentType to an integer before submitting the form
              this.form.documentType = parseInt(this.form.documentType);
              console.log("this.form.documentType:" + this.form.documentType)
              //新增的时候创建人的id就是当前用户的id
              this.form.userId =  this.userId;



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
                console.log("this.form:" +  JSON.stringify(this.form))
                let res = await request.post("/document/insert", this.form);
                console.log("进入新增");
                if (res.code === '0') {
                } else {
                  console.log("this.form" + this.form);
                  //   如果新增失败 需要发起删除掉上传到文件服务器的文件
                  console.log(this.oldFilePath)
                  this.del_file(this.form.documentPath)
                }
              } else {
                ElMessage({
                  message: `缺少以下字段：${missingFields.join(', ')}`,
                  type: 'warning',
                })
              }

            }else {
              item.status = 'error';
            }
          } catch (error) {
            console.log(error);
          }


        } else {
          // 如果上传失败，将当前文件下标保存到 deleteIndexList 中
          failedFiles.push(fileName);
          deleteIndexList.push(i);
        }

      }



      // 按照逆序进行删除重复导入的文件
      for (let j = deleteIndexList.length - 1; j >= 0; j--) {
        const deleteIndex = deleteIndexList[j];
        this.fileList.splice(deleteIndex, 1);
      }


      // 循环处理完所有文件之后，判断 failedFiles 是否为空
      if (failedFiles.length > 0) {
        // 如果有失败的文件，拼接成提示消息并弹窗
        const errorMsg = `${failedFiles.join(', ')} 文件重复导入，上传失败`;
        ElMessage({
          message: errorMsg,
          type: 'error',
        });
      }
      // 循环处理完所有文件之后，判断 failedFiles 是否为空
      if (ErrNameFiles.length > 0) {
        // 如果有失败的文件，拼接成提示消息并弹窗
        const errorMsg = `${ErrNameFiles.join(', ')} 文件名称不规范，上传失败`;
        ElMessage({
          message: errorMsg,
          type: 'error',
        });
      }

      // 循环处理完所有文件之后，判断 successFiles 是否为空
      if (successFiles.length > 0) {
        // 如果有失败的文件，拼接成提示消息并弹窗
        const successMsg = `${successFiles.join(', ')} 文件上传成功`;
        ElMessage({
          message: successMsg,
          type: 'success',
        });
      }

    },
    async updateFileM(){
      //开始上传文件和生成记录

      let fileData = new FormData()
      let string = JSON.stringify(this.form)
      fileData.append('document', string)
      fileData.append('file', this.fileList[0].raw)
      console.log("document:" + string)

      // 创建包含五种类型的列表
      const typeList = ["清单", "工艺", "图纸", "变更单", "交底单"];
      //此处应该加一层验证 验证一下当前用户的部门和创建该项目的用户是不是同一个部门的
      let res = await request.post("/document/departConfirm", this.form);
      if (res.code === '0') {
        try{
          try{
            const response = await axios.request({
              method: 'post',
              url: 'http://' + window.server.filesUploadUrl + ':9090/files/updateDocumentFilesM',
              headers: {
                'Content-Type': 'multipart/form-data',
                'token': JSON.parse(sessionStorage.getItem('token'))
              },
              data: fileData
            })
            //获取上传成功的文件的路径的列表
            let updateFilesName = response.data;
            //直接返回的数据是object类型的数据 需要转换为数据类型的数据
            let dataArray = updateFilesName["data"];
            console.log(dataArray.length);
            //根据返回的文件路径去更新数据
            for (let i = 0; i < dataArray.length; i++){

              //首先把返回的文件存储路径赋值给当前要传回后端的实体
              this.form.documentPath = dataArray[i];


              // 判断文件名中是否包含字符串，如果包含，根据映射关系设置 this.form.documentType 的值
              if (dataArray[i].indexOf('BOM') !== -1) {
                console.log("matches.length");
                this.form.documentType = 1;
              } else if (dataArray[i].indexOf('APD') !== -1) {
                console.log("TYPE:APD");
                this.form.documentType = 2;
              } else if (dataArray[i].indexOf('EWD') !== -1) {
                this.form.documentType = 3;
              } else if (dataArray[i].indexOf('CN') !== -1) {
                this.form.documentType = 4;
                console.log("TYPE:CN");
              } else if (dataArray[i].indexOf('TDF') !== -1) {
                this.form.documentType = 5;
              }else{
                console.log("名称错误");
                // 文件名不符合要求，直接跳过
                continue;
              }

              // 使用正则表达式匹配文件名中的数字
              const regex = /_(\d+)\./;
              const matches = dataArray[i].match(regex);
              if (matches && matches.length > 1) {
                // 匹配成功，获取到数字，并根据数字设置 this.form.documentType 和 this.form.sequenceNo 的值
                console.log("序号获取成功");
                this.form.sequenceNo = parseInt(matches[1]);
              }else{
                console.log("序号错误");
                // 文件名不符合要求，直接跳过
                continue;
              }
              // Convert documentType to an integer before submitting the form
              this.form.documentType = parseInt(this.form.documentType);

              //更新文件版本
              const fieldNames = {
                itemNo: '产品编号',
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
                //这里需要对文件进行甄别 如果文件是变更通知单 则不是更新版本而是插入记录 如果是变更通知单的附件 也就是其他类型的文件则是更新版本
                if (this.form.documentType === 4){
                  //变更通知单不存在更新的情况 所以这里要直接设置变更通知单的版本为A01
                  this.form.documentVersion = "A01";
                  console.log("开始更新");
                  try{
                    const res = await request.post("/document/insert", this.form);

                    if (res.code === '0') {
                      ElMessage({
                        message: typeList[this.form.documentType - 1] + '变更单保存成功',
                        type: 'success',
                      })
                    } else {

                      //如果变更单保存失败要去找到这个文件把它重新删除
                      //"localhost:80/files/WLMMXS2023030101/41601-3987/BOM/WLMMXS2023030101_41601-3987_BOM_1.docx"
                      this.errInsertUpdateFileList.push(dataArray[i]);
                      console.log(dataArray[i]);
                      console.log("变更通知单插入失败")
                      console.log("this.errInsertUpdateFileList" + this.errInsertUpdateFileList);
                      ElMessage({
                        message: typeList[this.form.documentType - 1] + '保存失败',
                        type: 'error',
                      })
                    }
                  }catch (error) {
                    console.log(error)
                  }

                }else{
                  try{

                    const res = await request.post("/document/updateM", this.form);
                    if (res.code === '0') {
                      ElMessage({
                        message: typeList[this.form.documentType - 1] + '文件更新成功',
                        type: 'success',
                      })
                    } else {
                      //如果变更单保存失败要去找到这个文件把它重新删除
                      console.log("变更附件插入失败")
                      console.log(dataArray[i]);
                      //"localhost:80/files/WLMMXS2023030101/41601-3987/BOM/WLMMXS2023030101_41601-3987_BOM_1.docx"
                      this.errInsertUpdateFileList.push(dataArray[i])

                      ElMessage({
                        message: typeList[this.form.documentType - 1] + '保存失败',
                        type: 'error',
                      })
                    }
                  }catch (error){
                    console.log(error)
                  }

                }


              } else {
                ElMessage({
                  message: `缺少以下字段：${missingFields.join(', ')}`,
                  type: 'warning',
                })
              }

            }


            console.log("this.errInsertUpdateFileList" + this.errInsertUpdateFileList);


            // 循环处理完所有文件之后，判断 failedFiles 是否为空
            if (this.errInsertUpdateFileList.length > 0) {




              this.del_files(this.errInsertUpdateFileList)
              // 如果有失败的文件，拼接成提示消息并弹窗
              // const errorMsg = `${this.errInsertUpdateFileList.join(', ')} 文件导入失败`;
              // //此处
              // ElMessage({
              //   message: errorMsg,
              //   type: 'error',
              //   duration: 5000, // 设置为5秒
              // });
            }
            //删除完之后把这个列表清空
            this.errInsertUpdateFileList = [];
          }catch (error){
            ElMessage({
              message: error.response.data.msg,
              type: 'error',
            })
          }
        }catch (error){
          console.error(error)
        }
      } else {
        ElMessage({
          message: res.msg,
          type: 'success',
        })
      }

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
      console.log("this.daterange" + this.daterange);
      const [start, end] = this.daterange
      if (start && end) {
        this.formInline.createTimeStart = moment(start).format('YYYY-MM-DD HH:mm:ss')
        this.formInline.createTimeEnd = moment(end).format('YYYY-MM-DD HH:mm:ss')
      }
      console.log('this.formInline',JSON.stringify(this.formInline))
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
        this.reset();//查询结束后 清空查询条件
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
    add1() {
      console.log("新增1")
      this.dialogVisibleM = true;
      this.form = {};

      this.$nextTick(() => {
        this.$refs["upload"].clearFiles();
      })
    },
    //设计变更
    updateM(){
      console.log("设计变更")
      this.updatedDialogVisibleM = true;
      this.form = {};
      this.fileList =[];
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
    del_files(filePaths) {
      return request.post("/files/delDocumentFiles", filePaths).then(res => {
            console.log(res);
            if (res.code === '0') {
              ElMessage({
                message: 'nginx文件同步已同步',
                type: 'info',
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
          //此处不设置料号 因为有的记录条目不填料号
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
      //新增的时候创建人的id就是当前用户的id
      this.form.userId =  this.userId;
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
    closeM(){
      this.dialogVisibleM = false;//关闭弹窗
      this.fileList =[];
      this.load();
    },
    closeupdatedDialog(){
      this.updatedDialogVisibleM = false;//关闭弹窗
      this.fileList =[];
      this.load();
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
          this.previewUrl = "http://" + window.server.filesUploadUrl
              + res.data.documentPath.replace(window.server.filesUploadUrl,"");;
          console.log("this.previewUrl" + this.previewUrl);

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

    handleDelete(id, documentPath,createTime) {
      console.log(id)
      console.log("documentPath=" + documentPath)
      console.log("createTime=" + createTime)
      request.delete(`/document/${id}/${createTime}`).then(res => {
        console.log(res);
        if (res.code === '0') {
          ElMessage({
            message: '删除成功',
            type: 'success',
          })
          this.del_file(documentPath)
        } else {
          ElMessage({
            message: res.msg,
            type: 'error',
          })
        }
        this.load();
      });
    },

    handVerify(row) {
      this.form = JSON.parse(JSON.stringify(row));
      this.form.approvalStatus = 1;
      console.log("edit.row:", this.form)
      // this.oldFilePath =  this.form.drawingPath;
      request.post(`/document/verifyPass`,this.form).then(res => {
        console.log(res);
        if (res.code === '0') {
          ElMessage({
            message: '审批成功',
            type: 'success',
          })
        } else {
          ElMessage({
            message: res.msg,
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

.custom-button {
  background-color: rgba(0, 0, 0, 0.8); /* 设置按钮背景颜色为深一些的黑色 */
  color: #fff; /* 设置文字颜色为白色 */
}
</style>
