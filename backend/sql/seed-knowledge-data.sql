USE getoffer;

SET NAMES utf8mb4;

DELETE FROM favorites;
DELETE FROM article_tags;
DELETE FROM articles;

INSERT INTO articles (
  title,
  category,
  type,
  content,
  created_at,
  updated_at,
  view_count,
  favorite_count,
  comment_count,
  author_id
)
SELECT
  '事件循环核心机制梳理',
  'JavaScript/ES6/TypeScript',
  1,
  '# 事件循环核心机制梳理\n\n## 一、为什么要理解事件循环\nJavaScript 在浏览器里通常运行在单线程环境中，同一时刻只能处理一段主线程代码。为了让定时器、网络请求和用户交互不阻塞页面，宿主环境会把异步回调暂存到不同的任务队列里，等主线程空闲后再取回执行。\n\n## 二、宏任务和微任务怎么配合\n一轮事件循环通常先执行一个宏任务，再把这一轮产生的微任务全部清空，然后浏览器才有机会进行渲染。常见宏任务有 script、setTimeout、setInterval，常见微任务有 Promise.then、queueMicrotask、MutationObserver。面试里判断日志顺序，本质上就是先找同步代码，再看微任务，最后看下一轮宏任务。\n\n## 三、常见误区\n最容易混淆的是把 async/await 当成真正的同步逻辑。实际上 await 之后的恢复代码仍然会进入微任务队列。另一个误区是把浏览器和 Node.js 的调度规则完全等同起来，真实场景里它们的细节并不完全一样。\n\n## 四、回答思路\n如果被问到事件循环，最好按“同步代码执行顺序 -> 当前轮微任务 -> 下一轮宏任务 -> 是否发生渲染”这条链路来答。这样即使题目很长，也可以拆成稳定的判断步骤。',
  '2026-04-20 18:00:00',
  '2026-04-20 18:00:00',
  420,
  2,
  0,
  u.id
FROM users u
WHERE u.username = 'frontend_offer';

INSERT INTO articles (
  title,
  category,
  type,
  content,
  created_at,
  updated_at,
  view_count,
  favorite_count,
  comment_count,
  author_id
)
SELECT
  'Promise 链式调用与错误冒泡',
  'JavaScript/ES6/TypeScript',
  1,
  '# Promise 链式调用与错误冒泡\n\n## 一、链式调用的价值\nPromise 解决的是异步流程的可组合问题。通过 then、catch、finally，可以把原本层层嵌套的回调改造成更线性的流程描述，让成功路径和失败路径都更容易维护。\n\n## 二、为什么错误会“冒泡”\n在 then 回调里抛出的异常，或者返回一个 rejected Promise，都会沿着后续链路向下传递，直到遇到 catch 为止。这就是错误冒泡。它的意义在于：你不必在每一层都写一遍错误处理，而是可以把真正兜底的逻辑集中到末尾。\n\n## 三、常见面试问法\n高频问题通常有三个：then 到底返回什么、多个 then 的执行顺序是什么、catch 后面还能不能继续 then。标准回答是：then 总会返回一个新的 Promise；链式回调会按注册顺序依次进入微任务；catch 处理完错误后，只要返回的不是 rejected 状态，后续链路就可以继续执行。\n\n## 四、实战建议\n不要把 Promise 链写成新的回调地狱。每一层只做一件事，数据转换和副作用分开写，错误统一在合适位置收口，这样链路会更清晰。',
  '2026-04-20 18:20:00',
  '2026-04-20 18:20:00',
  360,
  1,
  0,
  u.id
FROM users u
WHERE u.username = 'frontend_offer';

INSERT INTO articles (
  title,
  category,
  type,
  content,
  created_at,
  updated_at,
  view_count,
  favorite_count,
  comment_count,
  author_id
)
SELECT
  '泛型的约束与默认类型',
  'JavaScript/ES6/TypeScript',
  1,
  '# 泛型的约束与默认类型\n\n## 一、泛型解决什么问题\nTypeScript 的泛型本质上是在编写函数、接口和类时延后具体类型的决定。这样既能复用逻辑，又能保留类型信息，不必为了通用性直接退回 any。\n\n## 二、什么叫泛型约束\n当我们希望泛型至少具备某些结构时，就需要使用 extends 添加约束。例如一个获取 length 的函数，泛型参数就必须约束为带有 length 属性的类型。这样做的好处是把“可接受的数据范围”在类型层提前表达出来。\n\n## 三、默认类型有什么用\n泛型默认类型适合用在大部分场景下都有主流取值、但仍想保留扩展能力的地方。它可以减少调用方显式传类型参数的成本，同时避免类型推断失败时退化得太宽泛。\n\n## 四、面试回答技巧\n如果面试官追问泛型，建议从“复用逻辑”“保持类型安全”“通过约束描述边界”“通过默认类型优化调用体验”四个点展开。这样既解释了语法，也解释了设计意图。',
  '2026-04-20 18:40:00',
  '2026-04-20 18:40:00',
  288,
  1,
  0,
  u.id
FROM users u
WHERE u.username = 'frontend_offer';

INSERT INTO articles (
  title,
  category,
  type,
  content,
  created_at,
  updated_at,
  view_count,
  favorite_count,
  comment_count,
  author_id
)
SELECT
  'BFC 的形成条件与应用',
  'HTML&CSS',
  1,
  '# BFC 的形成条件与应用\n\n## 一、BFC 是什么\nBFC 可以理解成页面中的一个独立布局环境，内部元素的布局不会直接影响外部，外部也不会轻易干扰内部。理解它的关键不是背定义，而是知道它能解决哪些真实布局问题。\n\n## 二、常见形成条件\n浮动元素、绝对定位元素、display 为 inline-block/table-cell/flex/grid 的元素、overflow 不为 visible 的块级元素，都会形成新的 BFC。面试里通常不会要求你把所有条件一字不差背全，但至少要能说出几个高频场景。\n\n## 三、BFC 最常见的用法\n第一是清除浮动，让父元素能够包住内部浮动子元素；第二是避免外边距重叠；第三是阻止文字环绕浮动元素时出现意外布局。很多人写 overflow: hidden 时只是“记住能用”，但如果能解释背后的 BFC 原理，回答会更扎实。\n\n## 四、总结\nBFC 的价值在于帮你建立稳定的局部布局边界。记忆方法不要只记条件，要把“清浮动、阻止 margin 合并、隔离布局影响”这三个应用场景一起带上。',
  '2026-04-20 19:00:00',
  '2026-04-20 19:00:00',
  210,
  0,
  0,
  u.id
FROM users u
WHERE u.username = 'frontend_offer';

INSERT INTO articles (
  title,
  category,
  type,
  content,
  created_at,
  updated_at,
  view_count,
  favorite_count,
  comment_count,
  author_id
)
SELECT
  'HTTP 常见状态码与缓存协商',
  '计算机网络',
  1,
  '# HTTP 常见状态码与缓存协商\n\n## 一、状态码要按类别理解\n1xx 表示请求已被接收，2xx 表示成功，3xx 表示重定向，4xx 是客户端问题，5xx 是服务端问题。面试里最常考的是 200、204、301、302、304、400、401、403、404、500。\n\n## 二、304 为什么这么重要\n304 Not Modified 常和缓存协商一起出现。浏览器带着 If-None-Match 或 If-Modified-Since 请求资源时，服务端如果判断资源没有变化，就返回 304，浏览器继续使用本地缓存，从而减少传输成本。\n\n## 三、强缓存和协商缓存\n强缓存看的是 Expires 或 Cache-Control，在有效期内甚至不会向服务端发请求。协商缓存是缓存过期后再去问服务器资源是否变更，对应的核心字段是 ETag/If-None-Match 和 Last-Modified/If-Modified-Since。\n\n## 四、回答思路\n如果面试官把状态码和缓存放在一起问，可以先按状态码分类，再单独解释 304 与缓存协商的关系，最后补一句强缓存优先于协商缓存，这样回答会更完整。',
  '2026-04-20 19:20:00',
  '2026-04-20 19:20:00',
  334,
  1,
  0,
  u.id
FROM users u
WHERE u.username = 'backend_offer';

INSERT INTO articles (
  title,
  category,
  type,
  content,
  created_at,
  updated_at,
  view_count,
  favorite_count,
  comment_count,
  author_id
)
SELECT
  'Vue 响应式原理速记',
  '前端框架',
  1,
  '# Vue 响应式原理速记\n\n## 一、响应式解决的问题\nVue 的响应式系统本质上是在状态变化时，自动找到依赖这些状态的副作用并重新执行。这样开发者只关心状态和视图的映射关系，不需要手动维护大量 DOM 更新逻辑。\n\n## 二、Vue3 的核心链路\nVue3 主要通过 Proxy 对对象做代理，在 get 时收集依赖，在 set 时触发更新。依赖收集可以理解成“谁在用这个数据”，触发更新可以理解成“这个数据变了，通知那些依赖重新运行”。\n\n## 三、computed 和 watch 的差别\ncomputed 更像是带缓存的派生值，适合根据已有状态计算结果；watch 适合监听变化后执行副作用，比如发请求、写日志、同步外部状态。回答这类问题时，不要只说语法，要明确“一个偏结果，一个偏副作用”。\n\n## 四、实战提醒\n不要把所有状态都揉在一个超大对象里，否则依赖追踪和维护边界都会变差。合理拆分状态、理解响应式何时收集和触发，比死记实现细节更重要。',
  '2026-04-20 19:40:00',
  '2026-04-20 19:40:00',
  395,
  2,
  0,
  u.id
FROM users u
WHERE u.username = 'frontend_offer';

INSERT INTO articles (
  title,
  category,
  type,
  content,
  created_at,
  updated_at,
  view_count,
  favorite_count,
  comment_count,
  author_id
)
SELECT
  '链表题目的常见解法模板',
  '数据结构与算法',
  1,
  '# 链表题目的常见解法模板\n\n## 一、为什么链表题总是反复出现\n链表的价值在于它非常适合考察指针移动、边界处理和过程推演能力。虽然业务里不一定天天手写链表，但它能有效暴露候选人在细节控制上的稳定性。\n\n## 二、几个高频模板\n第一类是双指针模板，比如找中点、判断环、找倒数第 k 个节点；第二类是 dummy 节点模板，适合删除节点、合并链表、局部反转；第三类是原地反转模板，要明确 prev、curr、next 三个指针的推进顺序。\n\n## 三、最容易错的地方\n链表题最怕边界漏判，比如空链表、单节点、头节点被删除、反转区间包含首尾等情况。写代码前先画图或口头过一遍指针变化，能大幅减少低级错误。\n\n## 四、面试建议\n如果现场写链表题，不要急着一口气写完。先给出思路和模板，再落代码，边写边说明当前指针分别代表什么，这样即使最后有小 bug，面试官也能看出你的结构化思维。',
  '2026-04-20 20:00:00',
  '2026-04-20 20:00:00',
  240,
  1,
  0,
  u.id
FROM users u
WHERE u.username = 'demo_admin';

INSERT INTO articles (
  title,
  category,
  type,
  content,
  created_at,
  updated_at,
  view_count,
  favorite_count,
  comment_count,
  author_id
)
SELECT
  '前端性能优化排查思路',
  '前端纵向领域',
  1,
  '# 前端性能优化排查思路\n\n## 一、先分清性能问题出现在哪里\n前端性能不能只靠“背优化点”来回答，更重要的是先定位瓶颈在加载、渲染、脚本执行还是网络阶段。不同阶段的问题，优化手段差异很大。\n\n## 二、加载阶段怎么想\n加载性能通常关注资源体积、请求数量和缓存命中率。常见手段包括代码分割、图片压缩、按需加载、合理使用缓存策略、减少首屏不必要资源。这里的核心不是堆术语，而是说明为什么这些手段能减少首屏等待时间。\n\n## 三、运行阶段怎么排查\n如果页面已经加载完成但仍然卡顿，要看长任务、频繁重排重绘、过多无效渲染和内存泄漏。对于 Vue 或 React 应用，很多性能问题并不是框架本身慢，而是组件边界、状态设计和副作用管理不合理。\n\n## 四、面试表达方式\n推荐按“现象 -> 指标 -> 定位工具 -> 优化措施 -> 回归验证”来讲。这样能体现你不是只会背 Lighthouse 分数，而是真正知道如何在项目里定位和验证性能问题。',
  '2026-04-20 20:20:00',
  '2026-04-20 20:20:00',
  372,
  1,
  0,
  u.id
FROM users u
WHERE u.username = 'backend_offer';

INSERT INTO article_tags (article_id, name)
SELECT a.id, 'JavaScript' FROM articles a WHERE a.title = '事件循环核心机制梳理';
INSERT INTO article_tags (article_id, name)
SELECT a.id, '事件循环' FROM articles a WHERE a.title = '事件循环核心机制梳理';
INSERT INTO article_tags (article_id, name)
SELECT a.id, '微任务' FROM articles a WHERE a.title = '事件循环核心机制梳理';

INSERT INTO article_tags (article_id, name)
SELECT a.id, 'Promise' FROM articles a WHERE a.title = 'Promise 链式调用与错误冒泡';
INSERT INTO article_tags (article_id, name)
SELECT a.id, 'ES6' FROM articles a WHERE a.title = 'Promise 链式调用与错误冒泡';
INSERT INTO article_tags (article_id, name)
SELECT a.id, '错误处理' FROM articles a WHERE a.title = 'Promise 链式调用与错误冒泡';

INSERT INTO article_tags (article_id, name)
SELECT a.id, 'TypeScript' FROM articles a WHERE a.title = '泛型的约束与默认类型';
INSERT INTO article_tags (article_id, name)
SELECT a.id, '泛型' FROM articles a WHERE a.title = '泛型的约束与默认类型';
INSERT INTO article_tags (article_id, name)
SELECT a.id, '类型系统' FROM articles a WHERE a.title = '泛型的约束与默认类型';

INSERT INTO article_tags (article_id, name)
SELECT a.id, 'CSS' FROM articles a WHERE a.title = 'BFC 的形成条件与应用';
INSERT INTO article_tags (article_id, name)
SELECT a.id, 'BFC' FROM articles a WHERE a.title = 'BFC 的形成条件与应用';
INSERT INTO article_tags (article_id, name)
SELECT a.id, '布局' FROM articles a WHERE a.title = 'BFC 的形成条件与应用';

INSERT INTO article_tags (article_id, name)
SELECT a.id, 'HTTP' FROM articles a WHERE a.title = 'HTTP 常见状态码与缓存协商';
INSERT INTO article_tags (article_id, name)
SELECT a.id, '缓存' FROM articles a WHERE a.title = 'HTTP 常见状态码与缓存协商';
INSERT INTO article_tags (article_id, name)
SELECT a.id, '状态码' FROM articles a WHERE a.title = 'HTTP 常见状态码与缓存协商';

INSERT INTO article_tags (article_id, name)
SELECT a.id, 'Vue' FROM articles a WHERE a.title = 'Vue 响应式原理速记';
INSERT INTO article_tags (article_id, name)
SELECT a.id, '响应式' FROM articles a WHERE a.title = 'Vue 响应式原理速记';
INSERT INTO article_tags (article_id, name)
SELECT a.id, '前端框架' FROM articles a WHERE a.title = 'Vue 响应式原理速记';

INSERT INTO article_tags (article_id, name)
SELECT a.id, '链表' FROM articles a WHERE a.title = '链表题目的常见解法模板';
INSERT INTO article_tags (article_id, name)
SELECT a.id, '双指针' FROM articles a WHERE a.title = '链表题目的常见解法模板';
INSERT INTO article_tags (article_id, name)
SELECT a.id, '算法' FROM articles a WHERE a.title = '链表题目的常见解法模板';

INSERT INTO article_tags (article_id, name)
SELECT a.id, '性能优化' FROM articles a WHERE a.title = '前端性能优化排查思路';
INSERT INTO article_tags (article_id, name)
SELECT a.id, '前端工程化' FROM articles a WHERE a.title = '前端性能优化排查思路';
INSERT INTO article_tags (article_id, name)
SELECT a.id, '排查思路' FROM articles a WHERE a.title = '前端性能优化排查思路';

INSERT INTO favorites (user_id, article_id, created_at)
SELECT u.id, a.id, '2026-04-20 20:40:00'
FROM users u
JOIN articles a ON a.title = '事件循环核心机制梳理'
WHERE u.username = 'demo_admin';

INSERT INTO favorites (user_id, article_id, created_at)
SELECT u.id, a.id, '2026-04-20 20:41:00'
FROM users u
JOIN articles a ON a.title = 'Vue 响应式原理速记'
WHERE u.username = 'demo_admin';

INSERT INTO favorites (user_id, article_id, created_at)
SELECT u.id, a.id, '2026-04-20 20:42:00'
FROM users u
JOIN articles a ON a.title = 'HTTP 常见状态码与缓存协商'
WHERE u.username = 'frontend_offer';

INSERT INTO favorites (user_id, article_id, created_at)
SELECT u.id, a.id, '2026-04-20 20:43:00'
FROM users u
JOIN articles a ON a.title = '前端性能优化排查思路'
WHERE u.username = 'frontend_offer';

INSERT INTO favorites (user_id, article_id, created_at)
SELECT u.id, a.id, '2026-04-20 20:44:00'
FROM users u
JOIN articles a ON a.title = 'Promise 链式调用与错误冒泡'
WHERE u.username = 'backend_offer';

INSERT INTO favorites (user_id, article_id, created_at)
SELECT u.id, a.id, '2026-04-20 20:45:00'
FROM users u
JOIN articles a ON a.title = '链表题目的常见解法模板'
WHERE u.username = 'backend_offer';
