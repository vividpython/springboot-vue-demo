
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import store from './store'
import '@/assets/css/global.css'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ELIcons from '@element-plus/icons-vue';
import i18n from './i18n/index.js';

import 'core-js/stable';
import 'regenerator-runtime/runtime';


// import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

const app = createApp(App);
for (let iconName in ELIcons) {app.component(iconName, ELIcons[iconName]) };
// app.use(store).use(router).use(ElementPlus, {
//     locale: zhCn,size:"small"
// }).mount('#app')
app.use(store).use(router).use(ElementPlus, {size:"small"
}).use(i18n).mount('#app')


