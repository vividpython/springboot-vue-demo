<template>
  <div id="test_app" style="display: flex; flex-direction: row;background-color: rgb(34, 21, 34); ">
    <!--echarts的容器-->
    <div id="LastWeekCreate" style="width: 33%; height: 300px; background-color: rgb(34, 39, 51);margin-left: 20px; margin-right: 20px;margin-top: 20px;"></div>
    <div id="TypeDistribution" style="width: 33%; height: 300px; background-color: rgb(34, 39, 51); margin-left: 20px;margin-top: 20px;"></div>
    <div id="LastWeekPublish" style="width: 33%; height: 300px; background-color: rgb(34, 39, 51); margin-left: 20px;margin-top: 20px;"></div>
  </div>


</template>

<script>

import * as echarts from 'echarts'
import request from "@/utils/request";
import {ElMessage} from "element-plus";
const typeMappings = {
  "1": "材料清单",
  "2": "定屏图纸",
  "3": "配线图",
  "4": "技术交底单",
  "5": "变更交底单"
};
export default {
  name: 'EchartDemo',
  components: {},
  data() {
    this.charts = null
    return {

      xAxisLastWeekCreateData:[],
      opinionLastWeekCreateDataWLW: [],//数据
      opinionLastWeekCreateDataXNY: [],//数据

      xAxisLastWeekPublishData:[],
      opinionLastWeekPublishDataWLW: [],//数据
      opinionLastWeekPublishDataXNY: [],//数据

      opinionDataTypeDistribution:[]
    }
  },
  methods: {
    fetchLastWeekCreateData(){
      let _this = this
      request.get(`/document/getOneWeekInfoList/` + 8).then(res => {
        console.log(res);
        if (res.code === '0') {
          // 将获取到的数据赋值给 opinionLastWeekCreateDataWLW
          _this.opinionLastWeekCreateDataWLW = res.data.map(item => item.value);
          // 将日期格式化为 "8月8日" 的形式
          _this.xAxisLastWeekCreateData = res.data.map(item => {
            const date = new Date(item.date);
            const month = date.getMonth() + 1; // 获取月份，需要加上 1，因为月份是从 0 开始的
            const day = date.getDate(); // 获取日期
            return `${month}月${day}日`;
          });
          request.get(`/document/getOneWeekInfoList/` + 9).then(res => {
            if (res.code === '0') {
              // 将获取到的数据赋值给 opinionLastWeekCreateDataWLW
              _this.opinionLastWeekCreateDataXNY = res.data.map(item => item.value);
              _this.drawLastWeekCreate('LastWeekCreate')
            }})
        }else {
          console.log("err" + res.msg)
        }
      })
    },

    fetchLastWeekPublishData(){
      let _this = this
      request.get(`/document/getLastWeekPublishList/` + 8).then(res => {
        console.log(res);
        if (res.code === '0') {
          // 将获取到的数据赋值给 opinionLastWeekCreateDataWLW
          _this.opinionLastWeekPublishDataWLW = res.data.map(item => item.value);
          // 将日期格式化为 "8月8日" 的形式
          _this.xAxisLastWeekPublishData = res.data.map(item => {
            const date = new Date(item.date);
            const month = date.getMonth() + 1; // 获取月份，需要加上 1，因为月份是从 0 开始的
            const day = date.getDate(); // 获取日期
            return `${month}月${day}日`;
          });
          request.get(`/document/getLastWeekPublishList/` + 9).then(res => {
            if (res.code === '0') {
              // 将获取到的数据赋值给 opinionLastWeekCreateDataWLW
              _this.opinionLastWeekPublishDataXNY = res.data.map(item => item.value);
              _this.drawLastWeekPublish('LastWeekPublish')
            }})
        }else {
          console.log("err" + res.msg)
        }
      })
    },

    fetchTypeDistributionData(){
      let _this = this
      request.get(`/document/getTypeDistribution`).then(res => {
        console.log(res);
        if (res.code === '0') {
          console.log("echartdata:" + res.data)
          // 将获取到的数据赋值给 opinionLastWeekCreateDataWLW
          _this.opinionDataTypeDistribution = res.data.map(item => ({
            name: typeMappings[item.name], // 使用映射表转换名称
            value: item.value
          }));
          _this.drawTypeDistribution('TypeDistribution')
        }else {
          console.log("err" + res.msg)
        }
      })
    },


    drawTypeDistribution(id) {
      let _this = this
      _this.charts = echarts.init(document.getElementById(id))
      _this.charts.setOption({
        title:{
          left:'center',
          top:'5%',
          text:"文件类型分布图",//标题文本，支持使用 \n 换行。
          textStyle: {
            color: '#FFFFFF' // 标题文字颜色为红色
          }
        },
        tooltip: {
          trigger: 'item',
          textStyle: {
            color: '#888888' // 标题文字颜色为红色
          }
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          textStyle: {
            color: '#FFFFFF' // 标题文字颜色为红色
          }
        },
        toolbox:{
          show : true,                                 //是否显示工具栏组件
          orient:"horizontal",                        //工具栏 icon 的布局朝向'horizontal' 'vertical'
          itemSize:15,                                 //工具栏 icon 的大小
          itemGap:10,                                  //工具栏 icon 每项之间的间隔
          showTitle:true,                             //是否在鼠标 hover 的时候显示每个工具 icon 的标题
          feature : {
            mark : {                                 // '辅助线开关'
              show: true
            },
            dataView : {                            //数据视图工具，可以展现当前图表所用的数据，编辑后可以动态更新
              show: true,                         //是否显示该工具。
              title:"数据视图",
              readOnly: false,                    //是否不可编辑（只读）
              lang: ['数据视图', '关闭', '刷新'],  //数据视图上有三个话术，默认是['数据视图', '关闭', '刷新']
              backgroundColor:"#fff",             //数据视图浮层背景色。
              textareaColor:"#fff",               //数据视图浮层文本输入区背景色
              textareaBorderColor:"#333",         //数据视图浮层文本输入区边框颜色
              textColor:"#000",                    //文本颜色。
              buttonColor:"#c23531",              //按钮颜色。
              buttonTextColor:"#fff",             //按钮文本颜色。
            },
            restore : {                             //配置项还原。
              show: true,                         //是否显示该工具。
              title:"还原",
            },
            saveAsImage : {                         //保存为图片。
              show: true,                         //是否显示该工具。
              type:"png",                         //保存的图片格式。支持 'png' 和 'jpeg'。
              name:"pic1",                        //保存的文件名称，默认使用 title.text 作为名称
              backgroundColor:"#ffffff",        //保存的图片背景色，默认使用 backgroundColor，如果backgroundColor不存在的话会取白色
              title:"保存为图片",
              pixelRatio:1                        //保存图片的分辨率比例，默认跟容器相同大小，如果需要保存更高分辨率的，可以设置为大于 1 的值，例如 2
            }
          },
        },

        series: [
          {
            name: 'Access From',
            type: 'pie',
            radius: '50%',
            data: _this.opinionDataTypeDistribution,
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            }
          }
        ]
      })
    },


    drawLastWeekCreate(id) {
      let _this = this
      _this.charts = echarts.init(document.getElementById(id))
      _this.charts.setOption({
        title:{
          left:'center',
          top:'5%',
          text:"文件创建数量",//标题文本，支持使用 \n 换行。
          textStyle: {
            color: '#FFFFFF' // 标题文字颜色为红色
          }
        },
        tooltip: {
          trigger: 'axis',
          textStyle: {
            color: '#888888' // 提示框文字颜色为绿色
          }
        },
        legend: {
          align:'right',//文字在前图标在后
          left:'3%',
          top:'15%',
          data: ['物联网事业部','新能源事业部'],
          textStyle: {
            color: '#FFFFFF' // 图例文字颜色为蓝色
          }
        },
        grid: {
          top:'30%',
          left: '5%',
          right: '5%',
          bottom: '5%',
          containLabel: true
        },

        // toolbox: {
        //   feature: {
        //     saveAsImage: {} //保存为图片
        //   }
        // },
        toolbox:{
          show : true,                                 //是否显示工具栏组件
          orient:"horizontal",                        //工具栏 icon 的布局朝向'horizontal' 'vertical'
          itemSize:15,                                 //工具栏 icon 的大小
          itemGap:10,                                  //工具栏 icon 每项之间的间隔
          showTitle:true,                             //是否在鼠标 hover 的时候显示每个工具 icon 的标题
          feature : {
            mark : {                                 // '辅助线开关'
              show: true
            },
            dataView : {                            //数据视图工具，可以展现当前图表所用的数据，编辑后可以动态更新
              show: true,                         //是否显示该工具。
              title:"数据视图",
              readOnly: false,                    //是否不可编辑（只读）
              lang: ['数据视图', '关闭', '刷新'],  //数据视图上有三个话术，默认是['数据视图', '关闭', '刷新']
              backgroundColor:"#fff",             //数据视图浮层背景色。
              textareaColor:"#fff",               //数据视图浮层文本输入区背景色
              textareaBorderColor:"#333",         //数据视图浮层文本输入区边框颜色
              textColor:"#000",                    //文本颜色。
              buttonColor:"#c23531",              //按钮颜色。
              buttonTextColor:"#fff",             //按钮文本颜色。
            },
            magicType: {                            //动态类型切换
              show: true,
              title:"切换",                       //各个类型的标题文本，可以分别配置。
              type: ['line', 'bar'],              //启用的动态类型，包括'line'（切换为折线图）, 'bar'（切换为柱状图）, 'stack'（切换为堆叠模式）, 'tiled'（切换为平铺模式）
            },
            restore : {                             //配置项还原。
              show: true,                         //是否显示该工具。
              title:"还原",
            },
            saveAsImage : {                         //保存为图片。
              show: true,                         //是否显示该工具。
              type:"png",                         //保存的图片格式。支持 'png' 和 'jpeg'。
              name:"pic1",                        //保存的文件名称，默认使用 title.text 作为名称
              backgroundColor:"#ffffff",        //保存的图片背景色，默认使用 backgroundColor，如果backgroundColor不存在的话会取白色
              title:"保存为图片",
              pixelRatio:1                        //保存图片的分辨率比例，默认跟容器相同大小，如果需要保存更高分辨率的，可以设置为大于 1 的值，例如 2
            },
            dataZoom :{                             //数据区域缩放。目前只支持直角坐标系的缩放
              show: true,                         //是否显示该工具。
              title:"缩放",                       //缩放和还原的标题文本
              xAxisIndex:0,                       //指定哪些 xAxis 被控制。如果缺省则控制所有的x轴。如果设置为 false 则不控制任何x轴。如果设置成 3 则控制 axisIndex 为 3 的x轴。如果设置为 [0, 3] 则控制 axisIndex 为 0 和 3 的x轴
              yAxisIndex:false,                   //指定哪些 yAxis 被控制。如果缺省则控制所有的y轴。如果设置为 false 则不控制任何y轴。如果设置成 3 则控制 axisIndex 为 3 的y轴。如果设置为 [0, 3] 则控制 axisIndex 为 0 和 3 的y轴
            },
          },
        },
        xAxis: {
          type: 'category',
          boundaryGap:'true',
          axisTick:{
            alignWithLabel:true //保证刻度线和标签对齐
          },
          data: _this.xAxisLastWeekCreateData //x坐标的名称

        },
        yAxis: {
          type: 'value',
          boundaryGap: 'true',
          splitNumber:4, //纵坐标数
          interval:250 //强制设置坐标轴分割间隔
        },

        series: [
          {
            name: '物联网事业部',
            type: 'line', //折线图line;柱形图bar;饼图pie
            areaStyle: {
              //显示区域颜色---渐变效果
              color:{
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0, color: 'rgb(255,200,213)' // 0% 处的颜色
                }, {
                  offset: 1, color: '#ffffff' // 100% 处的颜色
                }],
                global: false // 缺省为 false
              }
            },
            itemStyle: {
              color: 'rgb(255,96,64)', //改变折线点的颜色
              lineStyle: {
                color: 'rgb(255,96,64)' //改变折线颜色
              }

            },
            data: _this.opinionLastWeekCreateDataWLW
          },
          {
            name: '新能源事业部',
            type: 'line',
            areaStyle: {
              //显示区域颜色---渐变效果
              color:{
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0, color: 'rgb(64,158,255)' // 0% 处的颜色
                }, {
                  offset: 1, color: '#ffffff' // 100% 处的颜色
                }],
                global: false // 缺省为 false
              }
            },
            itemStyle: {
              color: 'rgb(41,50,225)', //改变折线点的颜色
              lineStyle: {
                color: 'rgb(41,50,225)' //改变折线颜色
              }

            },
            data: _this.opinionLastWeekCreateDataXNY,
          }
        ]
      })
    },


    drawLastWeekPublish(id) {
      let _this = this
      _this.charts = echarts.init(document.getElementById(id))
      _this.charts.setOption({
        title:{
          left:'center',
          top:'5%',
          text:"文件发布数量",//标题文本，支持使用 \n 换行。
          textStyle: {
            color: '#FFFFFF' // 标题文字颜色为红色
          }
        },
        tooltip: {
          trigger: 'axis',
          textStyle: {
            color: '#888888' // 提示框文字颜色为绿色
          }
        },
        legend: {
          align:'right',//文字在前图标在后
          left:'3%',
          top:'15%',
          data: ['物联网','新能源'],
          textStyle: {
            color: '#FFFFFF' // 图例文字颜色为蓝色
          }
        },
        grid: {
          top:'30%',
          left: '5%',
          right: '5%',
          bottom: '5%',
          containLabel: true
        },

        // toolbox: {
        //   feature: {
        //     saveAsImage: {} //保存为图片
        //   }
        // },
        toolbox:{
          show : true,                                 //是否显示工具栏组件
          orient:"horizontal",                        //工具栏 icon 的布局朝向'horizontal' 'vertical'
          itemSize:15,                                 //工具栏 icon 的大小
          itemGap:10,                                  //工具栏 icon 每项之间的间隔
          showTitle:true,                             //是否在鼠标 hover 的时候显示每个工具 icon 的标题
          feature : {
            mark : {                                 // '辅助线开关'
              show: true
            },
            dataView : {                            //数据视图工具，可以展现当前图表所用的数据，编辑后可以动态更新
              show: true,                         //是否显示该工具。
              title:"数据视图",
              readOnly: false,                    //是否不可编辑（只读）
              lang: ['数据视图', '关闭', '刷新'],  //数据视图上有三个话术，默认是['数据视图', '关闭', '刷新']
              backgroundColor:"#fff",             //数据视图浮层背景色。
              textareaColor:"#fff",               //数据视图浮层文本输入区背景色
              textareaBorderColor:"#333",         //数据视图浮层文本输入区边框颜色
              textColor:"#000",                    //文本颜色。
              buttonColor:"#c23531",              //按钮颜色。
              buttonTextColor:"#fff",             //按钮文本颜色。
            },
            magicType: {                            //动态类型切换
              show: true,
              title:"切换",                       //各个类型的标题文本，可以分别配置。
              type: ['line', 'bar'],              //启用的动态类型，包括'line'（切换为折线图）, 'bar'（切换为柱状图）, 'stack'（切换为堆叠模式）, 'tiled'（切换为平铺模式）
            },
            restore : {                             //配置项还原。
              show: true,                         //是否显示该工具。
              title:"还原",
            },
            saveAsImage : {                         //保存为图片。
              show: true,                         //是否显示该工具。
              type:"png",                         //保存的图片格式。支持 'png' 和 'jpeg'。
              name:"pic1",                        //保存的文件名称，默认使用 title.text 作为名称
              backgroundColor:"#ffffff",        //保存的图片背景色，默认使用 backgroundColor，如果backgroundColor不存在的话会取白色
              title:"保存为图片",
              pixelRatio:1                        //保存图片的分辨率比例，默认跟容器相同大小，如果需要保存更高分辨率的，可以设置为大于 1 的值，例如 2
            },
            dataZoom :{                             //数据区域缩放。目前只支持直角坐标系的缩放
              show: true,                         //是否显示该工具。
              title:"缩放",                       //缩放和还原的标题文本
              xAxisIndex:0,                       //指定哪些 xAxis 被控制。如果缺省则控制所有的x轴。如果设置为 false 则不控制任何x轴。如果设置成 3 则控制 axisIndex 为 3 的x轴。如果设置为 [0, 3] 则控制 axisIndex 为 0 和 3 的x轴
              yAxisIndex:false,                   //指定哪些 yAxis 被控制。如果缺省则控制所有的y轴。如果设置为 false 则不控制任何y轴。如果设置成 3 则控制 axisIndex 为 3 的y轴。如果设置为 [0, 3] 则控制 axisIndex 为 0 和 3 的y轴
            },
          },
        },
        xAxis: {
          type: 'category',
          boundaryGap:'true',
          axisTick:{
            alignWithLabel:true //保证刻度线和标签对齐
          },
          data: _this.xAxisLastWeekPublishData //x坐标的名称

        },
        yAxis: {
          type: 'value',
          boundaryGap: 'true',
          splitNumber:4, //纵坐标数
          interval:250 //强制设置坐标轴分割间隔
        },

        series: [
          {
            name: '物联网',
            type: 'bar', //折线图line;柱形图bar;饼图pie
            areaStyle: {
              //显示区域颜色---渐变效果
              color:{
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0, color: 'rgb(255,200,213)' // 0% 处的颜色
                }, {
                  offset: 1, color: '#ffffff' // 100% 处的颜色
                }],
                global: false // 缺省为 false
              }
            },
            itemStyle: {
              color: 'rgb(255,96,64)', //改变折线点的颜色
              lineStyle: {
                color: 'rgb(255,96,64)' //改变折线颜色
              }

            },
            data: _this.opinionLastWeekPublishDataWLW
          },
          {
            name: '新能源',
            type: 'bar',
            areaStyle: {
              //显示区域颜色---渐变效果
              color:{
                type: 'linear',
                x: 0,
                y: 0,
                x2: 0,
                y2: 1,
                colorStops: [{
                  offset: 0, color: 'rgb(64,158,255)' // 0% 处的颜色
                }, {
                  offset: 1, color: '#ffffff' // 100% 处的颜色
                }],
                global: false // 缺省为 false
              }
            },
            itemStyle: {
              color: 'rgb(41,50,225)', //改变折线点的颜色
              lineStyle: {
                color: 'rgb(41,50,225)' //改变折线颜色
              }

            },
            data: _this.opinionLastWeekPublishDataXNY,
          }
        ]
      })
    }
  },
  //调用
  mounted() {

    let _this = this
    this.$nextTick(function() {
      _this.fetchLastWeekCreateData()
      _this.fetchTypeDistributionData()
      _this.fetchLastWeekPublishData()
    })
  }
}
</script>

<style scoped>

</style>