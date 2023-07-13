"use strict";(self["webpackChunkspringboot_vue_demo"]=self["webpackChunkspringboot_vue_demo"]||[]).push([[728],{5728:function(e,l,o){o.r(l),o.d(l,{default:function(){return w}});var t=o(6252),a=o(2262),s=o(5781),i=o(4471),r=o(1348);const n={style:{padding:"10px"}},d={style:{margin:"10px",padding:"0px"}},m={style:{margin:"10px",padding:"0px"}},p={slot:"footer",class:"dialog-footer"},c={style:{margin:"10px",padding:"0px"}},u={class:"dialog-footer"},h={name:"Role",components:{},data(){return{form:{},loading:!1,formInline:{},search:"",currentPage:1,pageSize:10,total:0,dialogVisible:!1,permsdialogVisible:!1,allPermissions:[],rolePermissions:[],defaultProps:{children:"subPermissions",label:"name"},importData:{imporType:1},headers:{token:JSON.parse(sessionStorage.getItem("token"))},tableData:[],RoleId:-1}},created(){console.log(this.headers),this.load()},methods:{filesUploadSuccess(e){this.form.img=e.data},delay(e){return new Promise((l=>setTimeout(l,e)))},async load(){try{this.loading=!0,await this.delay(1e3),console.log("this.formInline:"+this.formInline),i.Z.post(`/role/${this.currentPage}/${this.pageSize}`,JSON.parse(JSON.stringify(this.formInline))).then((e=>{console.log(e),this.tableData=e.data.records,this.total=e.data.total}))}catch(e){console.error(e)}finally{this.loading=!1}},reset(){this.formInline={},this.$refs["resetformInline"].resetFields()},add(){let e=this;e.dialogVisible=!0,e.form={},this.load(),this.$nextTick((()=>{this.$refs["upload"].clearFiles()}))},save(){this.form.id?i.Z.put("/role",this.form).then((e=>{console.log(e),"0"===e.code?(0,r.z8)({message:"修改成功",type:"success"}):(0,r.z8)({message:"修改失败",type:"error"})})):i.Z.post("/role",this.form).then((e=>{console.log(e),"0"===e.code?(0,r.z8)({message:"新增成功",type:"success"}):(0,r.z8)({message:"新增失败",type:"error"})})),this.dialogVisible=!1,this.load()},handleEdit(e){this.form=JSON.parse(JSON.stringify(e)),this.dialogVisible=!0},handlePerms(e){this.permsdialogVisible=!0,this.RoleId=e.id;try{i.Z.post("/docperms/allPermissions").then((l=>{if(console.log(l),"0"===l.code){this.allPermissions=l.data,console.log("this.allPermissions:"+this.allPermissions);try{i.Z.get(`/docperms/getPermissionsByRoleId/${e.id}`).then((e=>{"0"===e.code&&(this.rolePermissions=e.data,this.$refs.tree.setCheckedNodes(this.rolePermissions))}))}catch(o){console.error(o)}}}))}catch(l){console.error(l)}},setRolePermissions(){console.log(this.$refs.tree.getCheckedKeys());let e=this.$refs.tree.getCheckedKeys(),l="";for(let o=0;o<e.length;o++)l+=e[o]+",";l.substr(0,l.length-1),console.log("keyStr:"+l),i.Z.post(`/docperms/setDocPermsByRoleId/${this.RoleId}`,{keyStr:l}).then((e=>{"0"===e.code&&console.log("权限设置成功")})),this.permsdialogVisible=!1},handleSizeChange(){this.load()},handleCurrentChange(){this.load()},handleDelete(e){console.log(e),i.Z["delete"]("/role/"+e).then((e=>{console.log(e),"0"===e.code?(0,r.z8)({message:"删除成功",type:"success"}):(0,r.z8)({message:"删除失败",type:"error"}),this.load()}))}}};var g=Object.assign(h,{setup(e){return(e,l)=>{const o=(0,t.up)("el-button"),i=(0,t.up)("el-input"),r=(0,t.up)("el-form-item"),h=(0,t.up)("el-form"),g=(0,t.up)("el-tree"),f=(0,t.up)("el-dialog"),w=(0,t.up)("el-table-column"),y=(0,t.up)("el-popconfirm"),b=(0,t.up)("el-table"),k=(0,t.up)("el-pagination"),C=(0,t.Q2)("loading");return(0,t.wg)(),(0,t.iD)("div",n,[(0,t._)("div",d,[(0,t.Wm)(o,{color:"#E0BF75",onClick:e.add},{default:(0,t.w5)((()=>[(0,t.Uk)("新增")])),_:1},8,["onClick"]),(0,t.Wm)(o,{color:"#958CDD"},{default:(0,t.w5)((()=>[(0,t.Uk)("导入")])),_:1}),(0,t.Wm)(o,{color:"#FFDDAB"},{default:(0,t.w5)((()=>[(0,t.Uk)("导出")])),_:1}),(0,t.Wm)(o,{color:"#FC9DA9"},{default:(0,t.w5)((()=>[(0,t.Uk)("刷新")])),_:1})]),(0,t._)("div",m,[(0,t.Wm)(h,{inline:!0,model:e.formInline,class:"demo-form-inline",ref:"resetformInline"},{default:(0,t.w5)((()=>[(0,t.Wm)(r,{label:"角色名称"},{default:(0,t.w5)((()=>[(0,t.Wm)(i,{modelValue:e.formInline.name,"onUpdate:modelValue":l[0]||(l[0]=l=>e.formInline.name=l),placeholder:"please input",clearable:""},null,8,["modelValue"])])),_:1}),(0,t.Wm)(r,null,{default:(0,t.w5)((()=>[(0,t.Wm)(o,{color:"#E0BF75",style:{"margin-left":"5px"},icon:(0,a.SU)(s.Search),onClick:e.load},{default:(0,t.w5)((()=>[(0,t.Uk)("查询")])),_:1},8,["icon","onClick"]),(0,t.Wm)(o,{color:"#E0BF75",style:{"margin-left":"5px"},icon:(0,a.SU)(s.RefreshLeft),onClick:e.reset},{default:(0,t.w5)((()=>[(0,t.Uk)("重置")])),_:1},8,["icon","onClick"])])),_:1})])),_:1},8,["model"])]),(0,t.Wm)(f,{modelValue:e.permsdialogVisible,"onUpdate:modelValue":l[3]||(l[3]=l=>e.permsdialogVisible=l),title:"Tips",width:"30%"},{default:(0,t.w5)((()=>[(0,t.Wm)(g,{data:e.allPermissions,"show-checkbox":"","default-expand-all":"","node-key":"id",ref:"tree","highlight-current":"",props:e.defaultProps},null,8,["data","props"]),(0,t._)("div",p,[(0,t.Wm)(o,{onClick:l[1]||(l[1]=l=>e.permsdialogVisible=!1)},{default:(0,t.w5)((()=>[(0,t.Uk)("取 消")])),_:1}),(0,t.Wm)(o,{type:"primary",onClick:l[2]||(l[2]=l=>e.setRolePermissions())},{default:(0,t.w5)((()=>[(0,t.Uk)("确 定")])),_:1})])])),_:1},8,["modelValue"]),(0,t.wy)(((0,t.wg)(),(0,t.j4)(b,{data:e.tableData,style:{width:"100%"},border:"",class:"table",stripe:!1,fit:""},{default:(0,t.w5)((()=>[(0,t.Wm)(w,{prop:"id",label:"ID",sortable:""}),(0,t.Wm)(w,{prop:"name",label:"角色名称"}),(0,t.Wm)(w,{prop:"roleKey",label:"角色权限字"}),(0,t.Wm)(w,{prop:"createTime",label:"创建时间"}),(0,t.Wm)(w,{prop:"updateTime",label:"更新时间"}),(0,t.Wm)(w,{fixed:"right",label:"操作",width:"170"},{default:(0,t.w5)((l=>[(0,t.Wm)(o,{size:"small",onClick:o=>e.handleEdit(l.row)},{default:(0,t.w5)((()=>[(0,t.Uk)("编辑 ")])),_:2},1032,["onClick"]),(0,t.Wm)(o,{size:"small",onClick:o=>e.handlePerms(l.row)},{default:(0,t.w5)((()=>[(0,t.Uk)("修改权限 ")])),_:2},1032,["onClick"]),(0,t.Wm)(y,{title:"Are you sure to delete this?",onConfirm:o=>e.handleDelete(l.row.id)},{reference:(0,t.w5)((()=>[(0,t.Wm)(o,{type:"danger",size:"small"},{default:(0,t.w5)((()=>[(0,t.Uk)("删除")])),_:1})])),_:2},1032,["onConfirm"])])),_:1})])),_:1},8,["data"])),[[C,e.loading]]),(0,t._)("div",c,[(0,t.Wm)(k,{"current-page":e.currentPage,"onUpdate:currentPage":l[4]||(l[4]=l=>e.currentPage=l),"page-size":e.pageSize,"onUpdate:pageSize":l[5]||(l[5]=l=>e.pageSize=l),"page-sizes":[5,10,20,40],layout:"total, sizes, prev, pager, next, jumper",total:e.total,onSizeChange:e.handleSizeChange,onCurrentChange:e.handleCurrentChange},null,8,["current-page","page-size","total","onSizeChange","onCurrentChange"])]),(0,t.Wm)(f,{modelValue:e.dialogVisible,"onUpdate:modelValue":l[9]||(l[9]=l=>e.dialogVisible=l),title:"Tips",width:"30%"},{footer:(0,t.w5)((()=>[(0,t._)("span",u,[(0,t.Wm)(o,{onClick:l[8]||(l[8]=l=>e.dialogVisible=!1)},{default:(0,t.w5)((()=>[(0,t.Uk)("Cancel")])),_:1}),(0,t.Wm)(o,{type:"primary",onClick:e.save},{default:(0,t.w5)((()=>[(0,t.Uk)(" Confirm ")])),_:1},8,["onClick"])])])),default:(0,t.w5)((()=>[(0,t.Wm)(h,{model:e.form,"label-width":"120px"},{default:(0,t.w5)((()=>[(0,t.Wm)(r,{label:"角色名称"},{default:(0,t.w5)((()=>[(0,t.Wm)(i,{modelValue:e.form.name,"onUpdate:modelValue":l[6]||(l[6]=l=>e.form.name=l),style:{width:"80%"}},null,8,["modelValue"])])),_:1}),(0,t.Wm)(r,{label:"角色权限关键字"},{default:(0,t.w5)((()=>[(0,t.Wm)(i,{modelValue:e.form.roleKey,"onUpdate:modelValue":l[7]||(l[7]=l=>e.form.roleKey=l),style:{width:"80%"}},null,8,["modelValue"])])),_:1})])),_:1},8,["model"])])),_:1},8,["modelValue"])])}}});const f=g;var w=f}}]);
//# sourceMappingURL=728.0bac8ae9.js.map