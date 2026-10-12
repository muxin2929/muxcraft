<script setup>
import { useI18n } from '../i18n/index'
defineProps({ role: { type: String, default: '' } })
const { t } = useI18n()
</script>

<template>
  <span v-if="role === 'ADMIN'" class="admin-badge">{{ t('admin.badge') }}</span>
</template>

<style scoped>
.admin-badge {
  display: inline-block;
  margin-left: 6px;
  padding: 1px 7px;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: .06em;
  color: #fff;
  background: linear-gradient(135deg, #9dddd7, #71b9df);
  border-radius: 6px;
  vertical-align: middle;
}
</style>
