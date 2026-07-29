<script setup lang="ts">
import { Plus } from '@element-plus/icons-vue'

withDefaults(
  defineProps<{
    label?: string
    disabled?: boolean
    compact?: boolean
  }>(),
  {
    label: '创建 Agent',
    disabled: false,
    compact: false,
  },
)

defineEmits<{
  click: []
}>()
</script>

<template>
  <button
    type="button"
    class="create-agent-button"
    :class="{ compact }"
    :disabled="disabled"
    data-testid="create-agent-button"
    @click="$emit('click')"
  >
    <el-icon aria-hidden="true"><Plus /></el-icon>
    <span>{{ label }}</span>
  </button>
</template>

<style scoped>
.create-agent-button {
  display: inline-flex;
  min-height: 38px;
  align-items: center;
  justify-content: center;
  gap: 7px;
  padding: 0 17px;
  color: #ffffff;
  font-size: 14px;
  font-weight: 650;
  white-space: nowrap;
  background: #2563eb;
  border: 1px solid #2563eb;
  border-radius: 9px;
  box-shadow: 0 5px 14px rgba(37, 99, 235, 0.18);
  cursor: pointer;
  transition:
    background 0.18s ease,
    border-color 0.18s ease,
    transform 0.18s ease,
    box-shadow 0.18s ease;
}

.create-agent-button:hover:not(:disabled) {
  background: #1d4ed8;
  border-color: #1d4ed8;
  box-shadow: 0 7px 18px rgba(37, 99, 235, 0.24);
  transform: translateY(-1px);
}

.create-agent-button:active:not(:disabled) {
  transform: translateY(0);
}

.create-agent-button:focus-visible {
  outline: 3px solid rgba(59, 130, 246, 0.24);
  outline-offset: 2px;
}

.create-agent-button:disabled {
  color: #94a3b8;
  background: #e2e8f0;
  border-color: #e2e8f0;
  box-shadow: none;
  cursor: not-allowed;
}

.create-agent-button.compact {
  min-height: 34px;
  padding: 0 13px;
  font-size: 13px;
}

.create-agent-button :deep(.el-icon) {
  font-size: 14px;
}
</style>
