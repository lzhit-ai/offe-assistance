import test from 'node:test'
import assert from 'node:assert/strict'
import { existsSync, readFileSync } from 'node:fs'
import { resolve } from 'node:path'

const navbarSource = readFileSync(resolve(process.cwd(), 'src/components/Navbar.vue'), 'utf8')
const homeSource = readFileSync(resolve(process.cwd(), 'src/views/Home.vue'), 'utf8')
const articleListSource = readFileSync(resolve(process.cwd(), 'src/views/ArticleList.vue'), 'utf8')
const favoritesSource = readFileSync(resolve(process.cwd(), 'src/views/Favorites.vue'), 'utf8')
const aiSource = readFileSync(resolve(process.cwd(), 'src/views/Ai.vue'), 'utf8')
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

test('mobile category drawer reuses metadata-driven categories and hot tags', () => {
  assert.equal(existsSync(mobileCategoryDrawerPath), true)
  const mobileCategoryDrawerSource = readFileSync(mobileCategoryDrawerPath, 'utf8')
  assert.match(mobileCategoryDrawerSource, /metadataApi\.getCategories/)
  assert.match(mobileCategoryDrawerSource, /metadataApi\.getHotTags/)
  assert.match(mobileCategoryDrawerSource, /技术方向/)
  assert.match(mobileCategoryDrawerSource, /常用标签/)
  assert.match(mobileCategoryDrawerSource, /visibleCategories/)
  assert.match(mobileCategoryDrawerSource, /visibleTags/)
  assert.match(mobileCategoryDrawerSource, /selectTag/)
})

test('ai page collapses session history into a drawer at 1024px and below', () => {
  assert.match(aiSource, /historyDrawerVisible/)
  assert.match(aiSource, /history-trigger/)
  assert.match(aiSource, /mobile-ai-history-drawer/)
  assert.match(aiSource, /desktop-ai-sidebar/)
  assert.match(aiSource, /@media \(max-width: 1024px\)/)
  assert.match(aiSource, /direction="ltr"/)
})
