import {createRouter, createWebHistory} from 'vue-router'
import Layout from "@/layout/Layout.vue";

const routes = [
    {
        path: '/',
        name: 'Layout',
        component: Layout,
        redirect: "/login",
        children: [
            {
                path: '/userStatus',
                name: 'UserStatus',
                component: () => import("@/views/UserStatus.vue")
            },
            {
                path: '/echartDemo',
                name: 'EchartDemo',
                component: () => import("@/views/EchartDemo.vue")
            },
            {
                path: '/depart',
                name: 'Depart',
                component: () => import("@/views/Depart.vue")
            },
            {
                path: '/role',
                name: 'Role',
                component: () => import("@/views/Role.vue")
            },
            {
                path: '/docperms',
                name: 'Docperms',
                component: () => import("@/views/Docperms.vue")
            },
            {
            path: '/user',
            name: 'User',
            component: () => import("@/views/User.vue")
            },
            {
                path: '/news',
                name: 'News',
                component: () => import("@/views/News.vue")
            },
            {
                path: '/drawing',
                name: 'Drawing',
                component: () => import("@/views/Drawing.vue")
            },
            {
                path: '/document',
                name: 'Document',
                component: () => import("@/views/Document.vue")
            },
            {
                path: '/home',
                name: 'home',
                // route level code-splitting
                // this generates a separate chunk (about.[hash].js) for this route
                // which is lazy-loaded when the route is visited.
                component: () => import(/* webpackChunkName: "about" */ '../views/HomeView.vue')
            },
            {
                path: '/person',
                name: 'person',
                // route level code-splitting
                // this generates a separate chunk (about.[hash].js) for this route
                // which is lazy-loaded when the route is visited.
                component: () => import(/* webpackChunkName: "about" */ '../views/Person.vue')
            }
        ]
    },
    {
        path: '/login',
        name: 'Login',
        component: () => import("@/views/Login")
    },
    {
        path: '/register',
        name: 'Register',
        component: () => import("@/views/Register.vue")
    },

]

const router = createRouter({
    history: createWebHistory(process.env.BASE_URL),
    routes
})

export default router
