import { createRouter, createWebHistory } from 'vue-router'
import { ElMessage } from 'element-plus'
import { resolveRouteAccess } from './route-access'

const Home = () => import('@/views/Home.vue')
const ArticleList = () => import('@/views/ArticleList.vue')
const ArticleDetail = () => import('@/views/ArticleDetail.vue')
const Upload = () => import('@/views/Upload.vue')
const Favorites = () => import('@/views/Favorites.vue')
const Ai = () => import('@/views/Ai.vue')
const Profile = () => import('@/views/Profile.vue')
const AdminLogin = () => import('@/views/admin/AdminLogin.vue')
const AdminLayout = () => import('@/views/admin/AdminLayout.vue')
const AdminDashboard = () => import('@/views/admin/AdminDashboard.vue')
const AdminUsers = () => import('@/views/admin/AdminUsers.vue')
const AdminArticles = () => import('@/views/admin/AdminArticles.vue')
const AdminAiSessions = () => import('@/views/admin/AdminAiSessions.vue')

const routes = [
  {
    path: '/',
    name: 'home',
    component: Home,
    meta: { title: '首页 - 面经路八股' },
  },
  {
    path: '/articles/:type',
    name: 'articleList',
    component: ArticleList,
    props: true,
    meta: { title: '文章列表' },
  },
  {
    path: '/article/:id',
    name: 'articleDetail',
    component: ArticleDetail,
    props: true,
    meta: { title: '文章详情' },
  },
  {
    path: '/upload',
    name: 'upload',
    component: Upload,
    meta: {
      title: '上传文章',
      requiresAuth: true,
    },
  },
  {
    path: '/favorites',
    name: 'favorites',
    component: Favorites,
    meta: {
      title: '我的收藏',
      requiresAuth: true,
    },
  },
  {
    path: '/ai',
    name: 'ai',
    component: Ai,
    meta: {
      title: 'AI 助手',
      requiresAuth: true,
    },
  },
  {
    path: '/profile',
    name: 'profile',
    component: Profile,
    meta: {
      title: '个人中心',
      requiresAuth: true,
    },
  },
  {
    path: '/admin/login',
    name: 'adminLogin',
    component: AdminLogin,
    meta: {
      title: '后台登录',
    },
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: {
      title: '后台管理',
      requiresAuth: true,
      requiresAdmin: true,
    },
    children: [
      {
        path: '',
        redirect: '/admin/dashboard',
      },
      {
        path: 'dashboard',
        name: 'adminDashboard',
        component: AdminDashboard,
        meta: {
          title: '后台概览',
          requiresAuth: true,
          requiresAdmin: true,
        },
      },
      {
        path: 'users',
        name: 'adminUsers',
        component: AdminUsers,
        meta: {
          title: '用户管理',
          requiresAuth: true,
          requiresAdmin: true,
        },
      },
      {
        path: 'articles',
        name: 'adminArticles',
        component: AdminArticles,
        meta: {
          title: '文章审核',
          requiresAuth: true,
          requiresAdmin: true,
        },
      },
      {
        path: 'ai-sessions',
        name: 'adminAiSessions',
        component: AdminAiSessions,
        meta: {
          title: 'AI 会话',
          requiresAuth: true,
          requiresAdmin: true,
        },
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }

    return { top: 0 }
  },
})

router.beforeEach((to) => {
  const isLoggedIn = Boolean(localStorage.getItem('token'))
  let isAdmin = false

  try {
    const rawUser = localStorage.getItem('user')
    isAdmin = rawUser ? JSON.parse(rawUser)?.role === 'ADMIN' : false
  } catch {
    isAdmin = false
  }

  const redirect = resolveRouteAccess(to, isLoggedIn, isAdmin)

  if (!redirect) {
    return true
  }

  ElMessage.warning(to?.meta?.requiresAdmin ? '无权访问后台' : '请先登录后再访问该页面')
  return redirect
})

export default router
