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

const routes = [
  {
    path: '/',
    name: 'home',
    component: Home,
    meta: { title: '首页 - 面经·八股' },
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
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }

    return { top: 0 }
  },
})

router.beforeEach((to) => {
  const redirect = resolveRouteAccess(to, Boolean(localStorage.getItem('token')))

  if (!redirect) {
    return true
  }

  ElMessage.warning('请先登录后再访问该页面')
  return redirect
})

export default router
