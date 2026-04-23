<template>
  <aside class="admin-sidebar">
    <div class="admin-sidebar__brand">Offer Admin</div>
    <nav class="admin-sidebar__nav">
      <RouterLink to="/admin/dashboard">数据概览</RouterLink>
      <RouterLink to="/admin/users">用户管理</RouterLink>
      <RouterLink to="/admin/articles">文章审核</RouterLink>
      <RouterLink to="/admin/ai-sessions">AI 会话</RouterLink>
    </nav>
  </aside>
</template>

<style scoped>
.admin-sidebar {
  width: 220px;
  min-height: 100vh;
  padding: 28px 20px;
  background: linear-gradient(180deg, #0f172a 0%, #1e293b 100%);
  color: #e2e8f0;
}

.admin-sidebar__brand {
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.04em;
}

.admin-sidebar__nav {
  margin-top: 28px;
  display: grid;
  gap: 10px;
}

.admin-sidebar__nav a {
  padding: 10px 12px;
  border-radius: 12px;
  color: inherit;
  text-decoration: none;
  background: rgba(148, 163, 184, 0.12);
}

.admin-sidebar__nav a.router-link-active {
  background: rgba(56, 189, 248, 0.22);
}
</style>
