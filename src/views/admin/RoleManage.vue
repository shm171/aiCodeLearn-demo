<template>
  <el-card class="page-card">
    <el-table :data="roles" v-loading="loading" border stripe>
      <el-table-column label="角色" width="120" align="center">
        <template #default="{ row }">
          <el-tag :type="roleTag(row.role)" size="large">{{ row.name }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="说明" min-width="220" />
      <el-table-column label="用户数" width="100" align="center">
        <template #default="{ row }">{{ row.userCount }}</template>
      </el-table-column>
      <el-table-column label="权限" min-width="280">
        <template #default="{ row }">
          <el-tag
            v-for="p in row.permissions"
            :key="p"
            size="small"
            effect="plain"
            class="perm-tag"
          >
            {{ p }}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { getRoles } from "@/api/admin";
const loading = ref(false);
const roles = ref([]);
onMounted(async () => {
  loading.value = true;
  try {
    roles.value = await getRoles();
  } finally {
    loading.value = false;
  }
});
function roleTag(r) {
  const map = {
    STUDENT: "primary",
    TEACHER: "success",
    ADMIN: "danger"
  };
  return map[r] || "info";
}

</script>

<style scoped lang="scss">
.perm-tag {
  margin: 2px 4px 2px 0;
}
</style>