import test from 'node:test'
import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const navbarSource = readFileSync(resolve(process.cwd(), 'src/components/Navbar.vue'), 'utf8')
const homeSource = readFileSync(resolve(process.cwd(), 'src/views/Home.vue'), 'utf8')
const articleListSource = readFileSync(resolve(process.cwd(), 'src/views/ArticleList.vue'), 'utf8')
const favoritesSource = readFileSync(resolve(process.cwd(), 'src/views/Favorites.vue'), 'utf8')
const mobileCategoryDrawerPath = resolve(process.cwd(), 'src/components/MobileCategoryDrawer.vue')

test('tablet breakpoint hides the right hot panel before collapsing the navbar', () => {
  assert.match(homeSource, /:lg="5"/)
  assert.match(homeSource, /:md="5"/)
  assert.match(homeSource, /hot-panel-col/)
  assert.match(homeSource, /content-col/)
  assert.match(homeSource, /@media \(min-width: 768px\) and \(max-width: 1024px\)/)
  assert.match(homeSource, /flex:\s*0 0 75%/)
  assert.match(homeSource, /@media \(max-width: 1024px\)/)

  assert.match(articleListSource, /hot-panel-col/)
  assert.match(articleListSource, /content-col/)
  assert.match(articleListSource, /@media \(min-width: 768px\) and \(max-width: 1024px\)/)
  assert.match(articleListSource, /flex:\s*0 0 75%/)
  assert.match(articleListSource, /@media \(max-width: 1024px\)/)

  assert.match(favoritesSource, /hot-panel-col/)
  assert.match(favoritesSource, /content-col/)
  assert.match(favoritesSource, /@media \(min-width: 768px\) and \(max-width: 1024px\)/)
  assert.match(favoritesSource, /flex:\s*0 0 75%/)
  assert.match(favoritesSource, /@media \(max-width: 1024px\)/)
})

test('mobile navbar uses a drawer menu instead of the full nav items', () => {
  assert.match(navbarSource, /mobile-menu-trigger/)
  assert.match(navbarSource, /el-drawer/)
  assert.match(navbarSource, /mobile-nav/)
  assert.match(navbarSource, /@media \(max-width: 767px\)/)
})

test('mobile category drawer keeps only the important categories', () => {
  assert.equal(existsSync(mobileCategoryDrawerPath), true)
  const mobileCategoryDrawerSource = readFileSync(mobileCategoryDrawerPath, 'utf8')
  assert.match(mobileCategoryDrawerSource, /前端/)
  assert.match(mobileCategoryDrawerSource, /后端/)
  assert.match(mobileCategoryDrawerSource, /算法/)
  assert.match(mobileCategoryDrawerSource, /Java/)
  assert.match(mobileCategoryDrawerSource, /Vue/)
  assert.match(mobileCategoryDrawerSource, /Spring/)
  assert.match(mobileCategoryDrawerSource, /MySQL/)
  assert.doesNotMatch(mobileCategoryDrawerSource, /面经精选/)
})
