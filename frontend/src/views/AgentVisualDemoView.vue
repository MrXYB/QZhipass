<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { Bell, ChatDotSquare, Clock, Cpu, Headset, Promotion, UserFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import BrandLogo from '../components/BrandLogo.vue'
import {
  AgentHotkeyPanel,
  CreateAgentButton,
  CreateAgentDialog,
  type AgentItem,
  type CreateAgentPayload,
} from '../components/agent'

const STORAGE_KEY = 'qizhitong_agent_vue_demo'
const maxAgents = 10

const agents = ref<AgentItem[]>([])
const dialogVisible = ref(false)
const savingAgent = ref(false)
const hotkeyCollapsed = ref(false)
const modelPanelVisible = ref(false)
const messageText = ref('')
const currentModel = ref('DeepSeek')
const activeNav = ref<'chat' | 'history' | 'agents'>('chat')

const existingNames = computed(() => agents.value.map(agent => agent.agentName))

function loadAgents() {
  try {
    const saved = window.localStorage.getItem(STORAGE_KEY)
    agents.value = saved ? JSON.parse(saved) : []
  } catch {
    agents.value = []
  }
}

function persistAgents() {
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(agents.value))
}

function inferCategory(promptContent: string) {
  const text = promptContent.toLocaleLowerCase()
  const categories = [
    { name: '写邮件', keywords: ['邮件', 'email', '写信', '回复'] },
    { name: 'PPT文案', keywords: ['ppt', '幻灯片', '演示', '文案'] },
    { name: '周报助手', keywords: ['周报', '周总结', '本周'] },
    { name: '营销文案', keywords: ['营销', '推广', '广告', '卖点'] },
    { name: '会议纪要', keywords: ['会议', '纪要', '会议记录'] },
  ]
  return categories.find(category => category.keywords.some(keyword => text.includes(keyword)))?.name ?? '通用'
}

function openCreateDialog() {
  if (agents.value.length >= maxAgents) {
    ElMessage.warning(`Agent 创建数已达上限（${maxAgents} 个），请删除后再创建`)
    return
  }
  dialogVisible.value = true
}

async function createAgent(payload: CreateAgentPayload) {
  if (agents.value.length >= maxAgents) {
    dialogVisible.value = false
    ElMessage.warning(`Agent 创建数已达上限（${maxAgents} 个）`)
    return
  }

  savingAgent.value = true
  await new Promise(resolve => window.setTimeout(resolve, 320))

  agents.value.push({
    agentId: `agent_${Date.now()}`,
    agentName: payload.agentName,
    promptContent: payload.promptContent,
    presetCategory: inferCategory(payload.promptContent),
    createdAt: new Date().toISOString(),
  })
  persistAgents()
  savingAgent.value = false
  dialogVisible.value = false
  ElMessage.success(`Agent「${payload.agentName}」创建成功`)
}

async function deleteAgent(agent: AgentItem) {
  try {
    await ElMessageBox.confirm(`确定删除 Agent「${agent.agentName}」吗？`, '删除 Agent', {
      type: 'warning',
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
    })
    agents.value = agents.value.filter(item => item.agentId !== agent.agentId)
    persistAgents()
    ElMessage.success('Agent 已删除')
  } catch {
    // User cancelled.
  }
}

function invokeAgent() {
  if (!agents.value.length) {
    ElMessage.info('请先创建一个 Agent')
    return
  }
  ElMessage.success(`已唤起 Agent「${agents.value[0].agentName}」`)
}

function sendMessage() {
  if (!messageText.value.trim()) return
  ElMessage.success('演示消息已发送')
  messageText.value = ''
}

function isEmptyTypingTarget(event: KeyboardEvent) {
  const target = event.target
  if (!(target instanceof HTMLInputElement || target instanceof HTMLTextAreaElement)) return true
  return target.value.length === 0
}

function handleGlobalKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') {
    dialogVisible.value = false
    modelPanelVisible.value = false
    return
  }

  if ((event.key === '~' || event.key === '～') && isEmptyTypingTarget(event)) {
    event.preventDefault()
    openCreateDialog()
  }

  if ((event.key === '#' || event.key === '＃') && isEmptyTypingTarget(event)) {
    event.preventDefault()
    modelPanelVisible.value = !modelPanelVisible.value
  }

  if ((event.key === '!' || event.key === '！') && isEmptyTypingTarget(event)) {
    event.preventDefault()
    invokeAgent()
  }
}

onMounted(() => {
  loadAgents()
  window.addEventListener('keydown', handleGlobalKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleGlobalKeydown)
})
</script>

<template>
  <div class="agent-demo-shell">
    <aside class="demo-sidebar">
      <div class="brand-area">
        <BrandLogo tone="dark" size="sm" />
      </div>

      <nav aria-label="Agent 演示导航">
        <button
          type="button"
          :class="{ active: activeNav === 'chat' }"
          @click="activeNav = 'chat'"
        >
          <el-icon><ChatDotSquare /></el-icon>
          <span>新建对话</span>
        </button>
        <button
          type="button"
          :class="{ active: activeNav === 'history' }"
          @click="activeNav = 'history'"
        >
          <el-icon><Clock /></el-icon>
          <span>历史对话</span>
        </button>
        <button
          type="button"
          :class="{ active: activeNav === 'agents' }"
          @click="activeNav = 'agents'"
        >
          <el-icon><Cpu /></el-icon>
          <span>我的 Agent</span>
          <em v-if="agents.length">{{ agents.length }}</em>
        </button>
      </nav>

      <div class="sidebar-foot">Agent 创建功能 · Vue 组件版</div>
    </aside>

    <section class="demo-main">
      <header class="demo-header">
        <div>
          <span class="header-eyebrow">{{ activeNav === 'agents' ? 'Agent 工作台' : '新建对话' }}</span>
          <strong v-if="activeNav === 'agents'">我的 Agent</strong>
        </div>
        <div class="header-actions">
          <CreateAgentButton @click="openCreateDialog" />
          <button type="button" class="header-icon" aria-label="通知"><el-icon><Bell /></el-icon></button>
          <button type="button" class="header-icon" aria-label="帮助"><el-icon><Headset /></el-icon></button>
          <span class="user-avatar"><el-icon><UserFilled /></el-icon></span>
        </div>
      </header>

      <main v-if="activeNav !== 'agents'" class="chat-canvas">
        <div class="welcome">
          <div class="welcome-icon"><el-icon><ChatDotSquare /></el-icon></div>
          <h1>你好，今天我可以帮你处理什么？</h1>
          <p>按 <kbd>~</kbd> 快速创建 Agent</p>

          <section class="callable-agents">
            <h2>可调用的 Agent</h2>
            <div v-if="agents.length" class="agent-tags">
              <button v-for="agent in agents" :key="agent.agentId" type="button" @click="ElMessage.success(`已选择 ${agent.agentName}`)">
                <el-icon><Cpu /></el-icon>
                <span>{{ agent.agentName }}</span>
                <em>{{ agent.presetCategory }}</em>
              </button>
            </div>
            <p v-else class="empty-agent">暂无 Agent，按 ~ 创建第一个</p>
          </section>
        </div>

        <AgentHotkeyPanel
          v-model:collapsed="hotkeyCollapsed"
          :model-name="currentModel"
          class="hotkey-placement"
        />

        <section class="composer-area">
          <div class="composer-wrap">
            <div v-if="modelPanelVisible" class="model-panel">
              <div class="model-panel-head">
                <strong>选择大模型</strong>
                <button type="button" @click="modelPanelVisible = false">×</button>
              </div>
              <button type="button" class="model-option" @click="modelPanelVisible = false">
                <span class="model-logo">DS</span>
                <span><b>DeepSeek</b><small>当前唯一可用模型</small></span>
                <i>✓</i>
              </button>
              <p>按 <kbd>#</kbd> 打开或关闭 · 目前仅支持 DeepSeek</p>
            </div>

            <div class="composer">
              <button type="button" class="model-trigger" @click="modelPanelVisible = !modelPanelVisible">
                <el-icon><Cpu /></el-icon>
                {{ currentModel }}
                <span>⌃</span>
              </button>
              <textarea
                v-model="messageText"
                rows="1"
                placeholder="输入消息，或按 # 选模型 / ~ 创建 Agent..."
                @keydown.enter.exact.prevent="sendMessage"
              ></textarea>
              <button type="button" class="send-button" aria-label="发送消息" :disabled="!messageText.trim()" @click="sendMessage">
                <el-icon><Promotion /></el-icon>
              </button>
            </div>
            <p class="composer-help">
              按 <kbd>#</kbd> 大模型列表 · <kbd>~</kbd> 创建 Agent ·
              <kbd>!</kbd> 调用 Agent · <kbd>esc</kbd> 关闭
            </p>
          </div>
        </section>
      </main>

      <main v-else class="agents-canvas">
        <section class="agents-heading">
          <div>
            <p>管理你创建的所有 Agent</p>
            <h1>我的 Agent</h1>
          </div>
          <span>{{ agents.length }} / {{ maxAgents }}</span>
        </section>

        <div v-if="agents.length" class="agent-card-grid">
          <article v-for="agent in agents" :key="agent.agentId">
            <div class="agent-card-head">
              <span><el-icon><Cpu /></el-icon></span>
              <div><h2>{{ agent.agentName }}</h2><p>{{ agent.presetCategory }}</p></div>
            </div>
            <p class="agent-prompt">{{ agent.promptContent }}</p>
            <button type="button" @click="deleteAgent(agent)">删除</button>
          </article>
        </div>

        <section v-else class="agent-empty-state">
          <div><el-icon><Cpu /></el-icon></div>
          <h2>还没有创建任何 Agent</h2>
          <p>创建后可以在对话中快速调用预设提示词。</p>
          <CreateAgentButton label="创建第一个 Agent" @click="openCreateDialog" />
        </section>
      </main>
    </section>

    <CreateAgentDialog
      v-model="dialogVisible"
      :existing-names="existingNames"
      :submitting="savingAgent"
      @submit="createAgent"
    />
  </div>
</template>

<style scoped>
.agent-demo-shell {
  display: flex;
  width: 100%;
  min-width: 0;
  height: 100vh;
  overflow: hidden;
  color: #17233c;
  background: #f7f9fc;
}

.demo-sidebar {
  display: flex;
  width: 258px;
  flex: 0 0 258px;
  flex-direction: column;
  background: #ffffff;
  border-right: 1px solid #e3e8ef;
}

.brand-area {
  display: flex;
  min-height: 72px;
  align-items: center;
  padding: 0 21px;
  border-bottom: 1px solid #eef1f5;
}

.demo-sidebar nav {
  display: grid;
  gap: 6px;
  padding: 18px 13px;
}

.demo-sidebar nav button {
  position: relative;
  display: flex;
  width: 100%;
  min-height: 46px;
  align-items: center;
  gap: 11px;
  padding: 0 14px;
  color: #526176;
  font-size: 14px;
  text-align: left;
  background: transparent;
  border: 0;
  border-radius: 11px;
  cursor: pointer;
  transition:
    color 0.18s ease,
    background 0.18s ease;
}

.demo-sidebar nav button:hover {
  color: #1d4ed8;
  background: #f5f8ff;
}

.demo-sidebar nav button.active {
  color: #1d4ed8;
  font-weight: 650;
  background: #eaf2ff;
}

.demo-sidebar nav button em {
  margin-left: auto;
  padding: 1px 7px;
  color: #2563eb;
  font-size: 10px;
  font-style: normal;
  background: #dce9ff;
  border-radius: 10px;
}

.sidebar-foot {
  margin-top: auto;
  padding: 17px 20px;
  color: #94a3b8;
  font-size: 11px;
  border-top: 1px solid #eef1f5;
}

.demo-main {
  display: flex;
  min-width: 0;
  flex: 1;
  flex-direction: column;
}

.demo-header {
  display: flex;
  min-height: 72px;
  flex: 0 0 72px;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px 0 30px;
  background: #ffffff;
  border-bottom: 1px solid #e4e9f0;
}

.demo-header > div:first-child {
  display: grid;
  gap: 2px;
}

.header-eyebrow {
  color: #6b778c;
  font-size: 13px;
}

.demo-header strong {
  color: #23314a;
  font-size: 15px;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-icon {
  display: grid;
  width: 36px;
  height: 36px;
  place-items: center;
  color: #8390a4;
  background: transparent;
  border: 0;
  border-radius: 9px;
  cursor: pointer;
}

.header-icon:hover {
  color: #2563eb;
  background: #f1f5fb;
}

.user-avatar {
  display: grid;
  width: 38px;
  height: 38px;
  margin-left: 3px;
  place-items: center;
  color: #8290a6;
  background: #e5eaf1;
  border-radius: 50%;
}

.chat-canvas,
.agents-canvas {
  position: relative;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

.welcome {
  position: absolute;
  top: 45%;
  left: 50%;
  width: min(650px, calc(100% - 60px));
  transform: translate(-50%, -50%);
}

.welcome-icon {
  display: grid;
  width: 66px;
  height: 66px;
  margin: 0 auto 24px;
  place-items: center;
  color: #2563eb;
  font-size: 30px;
  background: #deebff;
  border-radius: 17px;
}

.welcome h1 {
  margin: 0;
  color: #17233c;
  font-size: clamp(24px, 3vw, 30px);
  font-weight: 760;
  text-align: center;
  letter-spacing: -0.02em;
}

.welcome > p {
  margin: 10px 0 38px;
  color: #718096;
  font-size: 14px;
  text-align: center;
}

kbd {
  display: inline-grid;
  min-width: 22px;
  min-height: 20px;
  place-items: center;
  padding: 0 5px;
  color: #526176;
  font-family: inherit;
  font-size: 10px;
  background: #f4f6f9;
  border: 1px solid #dce2ea;
  border-radius: 5px;
}

.callable-agents h2 {
  margin: 0 0 12px;
  color: #94a3b8;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.empty-agent {
  margin: 0;
  color: #93a2ba;
  font-size: 14px;
  font-style: italic;
}

.agent-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.agent-tags button {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 10px;
  color: #255fc6;
  font-size: 12px;
  background: #eaf2ff;
  border: 1px solid #d8e7ff;
  border-radius: 20px;
  cursor: pointer;
}

.agent-tags button em {
  padding: 1px 5px;
  color: #6582ad;
  font-size: 9px;
  font-style: normal;
  background: #dce9fc;
  border-radius: 8px;
}

.hotkey-placement {
  position: absolute;
  right: 24px;
  bottom: 116px;
  z-index: 20;
}

.composer-area {
  position: absolute;
  right: 0;
  bottom: 0;
  left: 0;
  padding: 50px 22px 20px;
  background: linear-gradient(to top, #f7f9fc 64%, rgba(247, 249, 252, 0));
}

.composer-wrap {
  position: relative;
  width: min(760px, calc(100% - 40px));
  margin: 0 auto;
}

.composer {
  display: flex;
  min-height: 66px;
  align-items: center;
  gap: 12px;
  padding: 11px 12px;
  background: #ffffff;
  border: 1px solid #dbe2eb;
  border-radius: 18px;
  box-shadow: 0 3px 10px rgba(15, 23, 42, 0.05);
}

.model-trigger {
  display: inline-flex;
  height: 36px;
  align-items: center;
  gap: 6px;
  padding: 0 10px;
  color: #526176;
  font-size: 12px;
  background: #f1f4f8;
  border: 0;
  border-radius: 9px;
  cursor: pointer;
}

.model-trigger span {
  color: #9aa5b4;
}

.composer textarea {
  min-width: 0;
  height: 30px;
  flex: 1;
  padding: 5px 2px;
  color: #334155;
  font: inherit;
  font-size: 13px;
  line-height: 20px;
  background: transparent;
  border: 0;
  outline: 0;
  resize: none;
}

.composer textarea::placeholder {
  color: #9aa6b7;
}

.send-button {
  display: grid;
  width: 42px;
  height: 42px;
  flex: 0 0 42px;
  place-items: center;
  color: #ffffff;
  font-size: 18px;
  background: #2563eb;
  border: 0;
  border-radius: 12px;
  cursor: pointer;
}

.send-button:disabled {
  background: #b8cdf7;
  cursor: not-allowed;
}

.composer-help {
  margin: 9px 0 0;
  color: #95a1b2;
  font-size: 10px;
  text-align: center;
}

.model-panel {
  position: absolute;
  z-index: 25;
  bottom: calc(100% + 9px);
  left: 0;
  width: 288px;
  overflow: hidden;
  background: #ffffff;
  border: 1px solid #dfe5ed;
  border-radius: 15px;
  box-shadow: 0 14px 34px rgba(15, 23, 42, 0.13);
}

.model-panel-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 14px;
  border-bottom: 1px solid #eef1f5;
}

.model-panel-head strong {
  font-size: 13px;
}

.model-panel-head button {
  color: #94a3b8;
  font-size: 18px;
  background: transparent;
  border: 0;
  cursor: pointer;
}

.model-option {
  display: flex;
  width: calc(100% - 14px);
  align-items: center;
  gap: 10px;
  margin: 7px;
  padding: 9px;
  text-align: left;
  background: #edf4ff;
  border: 0;
  border-radius: 10px;
  cursor: pointer;
}

.model-logo {
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  color: #ffffff;
  font-size: 10px;
  font-weight: 700;
  background: #172033;
  border-radius: 8px;
}

.model-option > span:nth-child(2) {
  display: grid;
  flex: 1;
  gap: 2px;
}

.model-option b {
  color: #26344b;
  font-size: 12px;
}

.model-option small {
  color: #8d98a9;
  font-size: 9px;
}

.model-option i {
  color: #2563eb;
  font-style: normal;
}

.model-panel > p {
  margin: 0;
  padding: 8px 13px;
  color: #95a1b1;
  font-size: 9px;
  background: #f8fafc;
  border-top: 1px solid #eef1f5;
}

.agents-canvas {
  padding: 34px;
  overflow: auto;
}

.agents-heading {
  display: flex;
  max-width: 960px;
  align-items: flex-end;
  justify-content: space-between;
  margin: 0 auto 24px;
}

.agents-heading p {
  margin: 0 0 4px;
  color: #8995a7;
  font-size: 12px;
}

.agents-heading h1 {
  margin: 0;
  font-size: 25px;
}

.agents-heading > span {
  color: #758196;
  font-size: 12px;
}

.agent-card-grid {
  display: grid;
  max-width: 960px;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
  margin: 0 auto;
}

.agent-card-grid article {
  padding: 18px;
  background: #ffffff;
  border: 1px solid #e1e6ed;
  border-radius: 15px;
  box-shadow: 0 3px 10px rgba(15, 23, 42, 0.03);
}

.agent-card-head {
  display: flex;
  align-items: center;
  gap: 10px;
}

.agent-card-head > span,
.agent-empty-state > div {
  display: grid;
  width: 38px;
  height: 38px;
  place-items: center;
  color: #2563eb;
  background: #e5efff;
  border-radius: 10px;
}

.agent-card-head h2,
.agent-card-head p {
  margin: 0;
}

.agent-card-head h2 {
  font-size: 14px;
}

.agent-card-head p {
  margin-top: 2px;
  color: #8995a7;
  font-size: 10px;
}

.agent-prompt {
  display: -webkit-box;
  min-height: 40px;
  margin: 14px 0;
  overflow: hidden;
  color: #68758a;
  font-size: 12px;
  line-height: 1.65;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.agent-card-grid article > button {
  float: right;
  padding: 5px 9px;
  color: #e04e4b;
  font-size: 10px;
  background: #fff1f0;
  border: 0;
  border-radius: 6px;
  cursor: pointer;
}

.agent-empty-state {
  display: grid;
  min-height: 460px;
  place-items: center;
  align-content: center;
  text-align: center;
}

.agent-empty-state > div {
  width: 58px;
  height: 58px;
  margin-bottom: 15px;
  font-size: 24px;
}

.agent-empty-state h2 {
  margin: 0;
  font-size: 18px;
}

.agent-empty-state p {
  margin: 7px 0 20px;
  color: #8490a3;
  font-size: 12px;
}

@media (max-width: 960px) {
  .demo-sidebar {
    width: 80px;
    flex-basis: 80px;
  }

  .brand-area {
    justify-content: center;
    padding: 0;
  }

  .brand-area :deep(.brand-name),
  .demo-sidebar nav button span,
  .demo-sidebar nav button em,
  .sidebar-foot {
    display: none;
  }

  .demo-sidebar nav button {
    justify-content: center;
    padding: 0;
  }

  .hotkey-placement {
    right: 12px;
  }
}

@media (max-width: 720px) {
  .demo-sidebar {
    display: none;
  }

  .demo-header {
    padding: 0 14px;
  }

  .header-icon {
    display: none;
  }

  .welcome {
    top: 40%;
    width: calc(100% - 36px);
  }

  .callable-agents {
    padding: 0 10px;
  }

  .hotkey-placement {
    right: 8px;
    bottom: 124px;
  }

  .composer-area {
    padding-right: 8px;
    padding-left: 8px;
  }

  .composer-wrap {
    width: 100%;
  }

  .composer-help {
    white-space: nowrap;
    transform: scale(0.9);
  }

  .agent-card-grid {
    grid-template-columns: 1fr;
  }
}
</style>
