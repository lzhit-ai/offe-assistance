<template>
  <div class="admin-layout">
    <AdminSidebar />
    <main class="admin-layout__main">
      <header class="admin-layout__header">
        <div>
          <p class="admin-layout__eyebrow">Admin Console</p>
          <h1>内容与用户管理后台</h1>
        </div>
      </header>
      <RouterView />
    </main>
  </div>
</template>

<script setup lang="ts">
import AdminSidebar from '@/components/admin/AdminSidebar.vue'
</script>

<style scoped>
.admin-layout {
  min-height: 100vh;
  display: flex;
  background: #f8fafc;
}

.admin-layout__main {
  flex: 1;
  padding: 28px;
}

.admin-layout__header {
  margin-bottom: 24px;
}

.admin-layout__eyebrow {
  margin: 0 0 8px;
  color: #0f766e;
  text-transform: uppercase;
  letter-spacing: 0.16em;
  font-size: 12px;
}

.admin-layout__header h1 {
  margin: 0;
  font-size: 28px;
  color: #0f172a;
}
</style>
