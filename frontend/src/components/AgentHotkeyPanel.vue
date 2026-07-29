<script setup lang="ts">
import { ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import type { AgentHotkeyItem } from './types'

const props = withDefaults(
  defineProps<{
    collapsed?: boolean
    modelName?: string
    shortcuts?: AgentHotkeyItem[]
  }>(),
  {
    collapsed: false,
    modelName: 'DeepSeek',
    shortcuts: () => [
      { label: '大模型列表', key: '#' },
      { label: '创建 Agent', key: '~' },
      { label: '调用 Agent', key: '!' },
      { label: '关闭弹层', key: 'esc' },
    ],
  },
)

const emit = defineEmits<{
  'update:collapsed': [value: boolean]
}>()

function togglePanel() {
  emit('update:collapsed', !props.collapsed)
}
</script>

<template>
  <aside
    class="agent-hotkey-panel"
    :class="{ collapsed }"
    data-testid="agent-hotkey-panel"
    aria-label="Agent 快捷键说明"
  >
    <div class="panel-content">
      <h3>快捷键说明</h3>
      <dl>
        <div v-for="item in shortcuts" :key="item.key">
          <dt>{{ item.label }}</dt>
          <dd><kbd>{{ item.key }}</kbd></dd>
        </div>
      </dl>
      <p>当前大模型仅支持 {{ modelName }}</p>
    </div>

    <button
      type="button"
      class="collapse-handle"
      :aria-label="collapsed ? '展开快捷键说明' : '收起快捷键说明'"
      data-testid="agent-hotkey-toggle"
      @click="togglePanel"
    >
      <el-icon>
        <ArrowLeft v-if="collapsed" />
        <ArrowRight v-else />
      </el-icon>
    </button>
  </aside>
</template>

<style scoped>
.agent-hotkey-panel {
  display: flex;
  width: 272px;
  overflow: hidden;
  color: #334155;
  background: #ffffff;
  border: 1px solid #dfe5ee;
  border-radius: 18px;
  box-shadow: 0 16px 36px rgba(15, 23, 42, 0.13);
  transition: transform 0.28s cubic-bezier(0.4, 0, 0.2, 1);
}

.agent-hotkey-panel.collapsed {
  transform: translateX(calc(100% - 38px));
}

.panel-content {
  width: 233px;
  flex: 0 0 233px;
  padding: 18px 18px 14px;
}

.panel-content h3 {
  margin: 0 0 13px;
  color: #172033;
  font-size: 15px;
  font-weight: 750;
}

.panel-content dl {
  display: grid;
  gap: 9px;
  margin: 0;
}

.panel-content dl > div {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.panel-content dt {
  color: #475569;
  font-size: 12px;
}

.panel-content dd {
  margin: 0;
}

kbd {
  display: inline-grid;
  min-width: 26px;
  min-height: 24px;
  place-items: center;
  padding: 1px 6px;
  color: #526277;
  font-family: inherit;
  font-size: 11px;
  line-height: 1;
  background: #f5f7fa;
  border: 1px solid #dce2e9;
  border-radius: 5px;
  box-shadow: 0 1px 0 #e6ebf1;
}

.panel-content p {
  margin: 14px 0 0;
  padding-top: 11px;
  color: #94a3b8;
  font-size: 11px;
  border-top: 1px solid #eef1f5;
}

.collapse-handle {
  display: grid;
  width: 38px;
  flex: 0 0 38px;
  place-items: center;
  color: #94a3b8;
  background: #f8fafc;
  border: 0;
  border-left: 1px solid #e2e8f0;
  cursor: pointer;
  transition:
    color 0.18s ease,
    background 0.18s ease;
}

.collapse-handle:hover {
  color: #475569;
  background: #f1f5f9;
}

.collapse-handle:focus-visible {
  outline: 3px solid rgba(59, 130, 246, 0.22);
  outline-offset: -3px;
}

@media (max-width: 720px) {
  .agent-hotkey-panel {
    width: 250px;
  }

  .panel-content {
    width: 211px;
    flex-basis: 211px;
  }
}
</style>
