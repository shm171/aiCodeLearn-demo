<template>
  <el-card class="page-card" style="max-width: 720px" v-loading="loading">
    <template #header><span class="card-title">基础配置</span></template>
    <el-form ref="formRef" :model="form" :rules="rules" label-width="110px">
      <el-form-item label="平台名称" prop="platformName">
        <el-input v-model="form.platformName" />
      </el-form-item>
      <el-form-item label="开放注册">
        <el-switch v-model="form.allowRegister" />
        <div class="form-tip">关闭后学生/教师端不再允许自助注册</div>
      </el-form-item>
      <el-form-item label="默认注册角色">
        <el-select v-model="form.defaultRole" style="width: 200px">
          <el-option label="学生" value="STUDENT" />
          <el-option label="教师" value="TEACHER" />
        </el-select>
      </el-form-item>
      <el-form-item label="班级列表">
        <el-select
          v-model="form.classes"
          multiple
          filterable
          allow-create
          default-first-option
          placeholder="输入班级名后回车添加"
          style="width: 100%"
        />
        <div class="form-tip">用于教师端筛选学生，可手动输入新增班级</div>
      </el-form-item>
      <el-form-item>
        <el-button type="success" :loading="saving" @click="onSave">保存配置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { ElMessage } from "element-plus";
import { getConfig, updateConfig } from "@/api/admin";
const loading = ref(false);
const saving = ref(false);
const formRef = ref();
const form = reactive({
  platformName: "",
  allowRegister: true,
  defaultRole: "STUDENT",
  classes: []
});
const rules = {
  platformName: [{ required: true, message: "\u8BF7\u8F93\u5165\u5E73\u53F0\u540D\u79F0", trigger: "blur" }]
};
onMounted(async () => {
  loading.value = true;
  try {
    const c = await getConfig();
    Object.assign(form, c);
  } finally {
    loading.value = false;
  }
});
async function onSave() {
  if (!formRef.value) return;
  const valid = await formRef.value.validate().catch(() => false);
  if (!valid) return;
  saving.value = true;
  try {
    await updateConfig({ ...form });
    ElMessage.success("\u914D\u7F6E\u5DF2\u4FDD\u5B58");
  } finally {
    saving.value = false;
  }
}

</script>

<style scoped lang="scss">
.card-title { font-weight: 600; }
.form-tip { font-size: 12px; color: #909399; margin-top: 4px; }
</style>