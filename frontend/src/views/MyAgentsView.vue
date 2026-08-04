<template>
  <div class="agents-page">

<div class="back-header">
    <el-icon class="back-icon" @click="goBack">
      <ArrowLeft />
    </el-icon>
    <span>账号设置</span>
  </div> 

    <div class="settings-tabs">
      <span class="tab-item">修改密码</span>
      <span class="tab-item">邮箱绑定</span>
      <span class="tab-item active">我的Agent</span>
</div>

    <div class="page-header">
      <div>
        <h2>我的 Agent</h2>
        <p>管理系统 Agent 和个人 Agent</p>
      </div>

      <el-button type="primary" @click="openCreateDialog">
        创建 Agent
      </el-button>
    </div>

    <div class="search-area">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索 Agent 名称"
        clearable
        style="width: 300px"
      />
    </div>

    <section class="agent-section">
      <h3>系统预设 Agent</h3>

      <el-empty
        v-if="filteredSystemAgents.length === 0"
        description="没有找到相关 Agent"
      />

      <div v-else class="agent-grid">
        <el-card
          v-for="agent in filteredSystemAgents"
          :key="agent.id"
          class="agent-card"
          shadow="hover"
        >
          <div class="card-header">
            <div>
              <h4>{{ agent.name }}</h4>
              <el-tag size="small" type="info">系统预设</el-tag>
            </div>
          </div>

          <p class="prompt-preview">
            {{ agent.prompt }}
          </p>

          <div class="card-actions">
           <el-button text circle @click="viewAgent(agent)">
  <el-icon><View /></el-icon>
</el-button>

<el-button text circle @click="copyPrompt(agent.prompt)">
  <el-icon><CopyDocument /></el-icon>
            </el-button>
          </div>
        </el-card>
      </div>
    </section>

    <section class="agent-section">
      <h3>个人创建 Agent</h3>

      <el-empty
        v-if="filteredPersonalAgents.length === 0"
        description="暂无个人 Agent"
      />

      <div v-else class="agent-grid">
        <el-card
          v-for="agent in filteredPersonalAgents"
          :key="agent.id"
          class="agent-card"
          shadow="hover"
        >
          <div class="card-header">
            <div>
              <h4>{{ agent.name }}</h4>
              <el-tag size="small" type="success">个人创建</el-tag>
            </div>

            <div 
  class="icon-actions"
  v-if="agent.source === 'personal'"
>
             <el-button
  text
  circle
  @click="openEditDialog(agent)"
>
  <el-icon><Edit /></el-icon>
</el-button>

<el-button
  text
  circle
  @click="deleteAgent(agent)"
>
  <el-icon><Delete /></el-icon>
</el-button>
            </div>
          </div>

          <p class="prompt-preview">
            {{ agent.prompt }}
          </p>
            
        </el-card>
      </div>
    </section>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogMode === 'create' ? '创建 Agent' : '修改 Agent'"
      width="550px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="90px"
      >
        <el-form-item label="Agent名称" prop="name">
          <el-input
            v-model="formData.name"
            placeholder="请输入 Agent 名称"
            maxlength="30"
            show-word-limit
          />
        </el-form-item>

        <el-form-item label="提示词" prop="prompt">
          <el-input
            v-model="formData.prompt"
            type="textarea"
            :rows="8"
            placeholder="请输入 Agent 提示词"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">
          取消
        </el-button>

        <el-button type="primary" @click="saveAgent">
          {{ dialogMode === 'create' ? '创建' : '保存修改' }}
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="viewDialogVisible"
      title="查看提示词"
      width="550px"
    >
      <h3>{{ viewingAgent?.name }}</h3>

      <div class="prompt-content">
        {{ viewingAgent?.prompt }}
      </div>

      <template #footer>
        <el-button
          type="primary"
          @click="copyPrompt(viewingAgent?.prompt || '')"
        >
          复制提示词
        </el-button>

        <el-button @click="viewDialogVisible = false">
          关闭
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import type { FormInstance, FormRules } from 'element-plus'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Edit,
  Delete,
  View,
  CopyDocument
} from '@element-plus/icons-vue'
interface Agent {
  id: number
  name: string
  prompt: string
  type: 'system' | 'personal'
   source: 'admin' | 'personal'
}

const emit = defineEmits<{
  back: []
}>()

function goBack() {
  emit('back')
}
const STORAGE_KEY = 'qzhipass-personal-agents'
const activeTab = ref('agent')
const searchKeyword = ref('')
const dialogVisible = ref(false)
const viewDialogVisible = ref(false)
const dialogMode = ref<'create' | 'edit'>('create')
const editingAgentId = ref<number | null>(null)
const viewingAgent = ref<Agent | null>(null)
const formRef = ref<FormInstance>()

const systemAgents = ref<Agent[]>([
  {
    id: 1,
    name: '翻译助手',
    prompt: '你是一名专业翻译助手，请准确翻译用户提供的内容。',
    type: 'system',
     source: 'admin'
  },
  {
    id: 2,
    name: '会议总结助手',
    prompt: '你是一名会议总结助手，请提取会议重点、结论和待办事项。',
    type: 'system',
    source: 'admin'
    
  },
  {
    id: 3,
    name: '写作助手',
    prompt: '你是一名专业写作助手，请帮助用户优化文章结构和表达。',
    type: 'system',
      source: 'admin'
  }
])

const personalAgents = ref<Agent[]>([
  {
    id: 101,
    name: '代码检查助手',
    prompt: '请检查我提供的代码，找出错误并给出修改建议。',
    type: 'personal',
    source: 'personal'
  },
  {
    id: 102,
    name: '学习计划助手',
    prompt: '请根据我的目标和时间，为我制定详细的学习计划。',
    type: 'personal',
    source: 'personal'
  }
])

const formData = reactive({
  name: '',
  prompt: ''
})

const formRules: FormRules = {
  name: [
    {
      required: true,
      message: '请输入 Agent 名称',
      trigger: 'blur'
    }
  ],
  prompt: [
    {
      required: true,
      message: '请输入提示词',
      trigger: 'blur'
    }
  ]
}

const sortAgents = (agents: Agent[]) => {
  return [...agents].sort((a, b) =>
    a.name.localeCompare(b.name, 'zh-CN')
  )
}

const filteredSystemAgents = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  const result = systemAgents.value.filter((agent) => {
    return (
      agent.name.toLowerCase().includes(keyword) ||
      agent.prompt.toLowerCase().includes(keyword)
    )
  })

  return sortAgents(result)
})

const filteredPersonalAgents = computed(() => {
  const keyword = searchKeyword.value.trim().toLowerCase()

  const result = personalAgents.value.filter((agent) => {
    return (
      agent.name.toLowerCase().includes(keyword) ||
      agent.prompt.toLowerCase().includes(keyword)
    )
  })

  return sortAgents(result)
})

const saveToLocalStorage = () => {
  localStorage.setItem(
    STORAGE_KEY,
    JSON.stringify(personalAgents.value)
  )
}

const loadFromLocalStorage = () => {

  // 员工自己创建的 Agent
  const personal = JSON.parse(
    localStorage.getItem(STORAGE_KEY) || '[]'
  )


  // 管理员创建的 Agent
  const adminAgents = JSON.parse(
    localStorage.getItem('admin-agents') || '[]'
  )


  const adminFormat = adminAgents.map((item: any) => ({
    id: item.id,
    name: item.title,
    prompt: item.prompt,
    type: 'personal',
    source: 'admin'
  }))


  personalAgents.value = [
    ...personal,
    ...adminFormat
  ]

}

const resetForm = () => {
  formData.name = ''
  formData.prompt = ''
  editingAgentId.value = null
  formRef.value?.clearValidate()
}

const openCreateDialog = () => {
  dialogMode.value = 'create'
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (agent: Agent) => {
  dialogMode.value = 'edit'
  editingAgentId.value = agent.id

  formData.name = agent.name
  formData.prompt = agent.prompt

  dialogVisible.value = true
}

const saveAgent = async () => {
  if (!formRef.value) {
    return
  }

  const valid = await formRef.value.validate().catch(() => false)

  if (!valid) {
    return
  }

  if (dialogMode.value === 'create') {
    personalAgents.value.push({
      id: Date.now(),
      name: formData.name.trim(),
      prompt: formData.prompt.trim(),
      type: 'personal',
       source: 'personal'
    })

    ElMessage.success('Agent 创建成功')
  } else {
    const targetAgent = personalAgents.value.find(
      (agent) => agent.id === editingAgentId.value
    )

    if (!targetAgent) {
      ElMessage.error('没有找到需要修改的 Agent')
      return
    }

    targetAgent.name = formData.name.trim()
    targetAgent.prompt = formData.prompt.trim()

    ElMessage.success('Agent 修改成功')
  }

  saveToLocalStorage()
  dialogVisible.value = false
  resetForm()
}

const deleteAgent = async (agent: Agent) => {
  try {
    await ElMessageBox.confirm(
      `确定删除“${agent.name}”吗？`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    personalAgents.value = personalAgents.value.filter(
      (item) => item.id !== agent.id
    )

    saveToLocalStorage()
    ElMessage.success('Agent 删除成功')
  } catch {
    // 用户点击取消时不进行处理
  }
}

const viewAgent = (agent: Agent) => {
  viewingAgent.value = agent
  viewDialogVisible.value = true
}

const copyPrompt = async (prompt: string) => {
  try {
    await navigator.clipboard.writeText(prompt)
    ElMessage.success('提示词已复制')
  } catch {
    ElMessage.error('复制失败，请手动复制')
  }
}

onMounted(() => {
  loadFromLocalStorage()
})
</script>

<style scoped>
.agents-page {
  min-height: 100vh;
  padding: 28px;
  background: #f5f7fa;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-header h2 {
  margin: 0 0 8px;
  font-size: 26px;
  color: #303133;
}

.page-header p {
  margin: 0;
  color: #909399;
}

.search-area {
  padding: 20px;
  margin-bottom: 24px;
  background: #ffffff;
  border-radius: 8px;
}

.agent-section {
  margin-bottom: 32px;
}

.agent-section h3 {
  margin-bottom: 16px;
  color: #303133;
}

.agent-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(310px, 1fr));
  gap: 18px;
}

.agent-card {
  min-height: 190px;
}

.card-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.card-header h4 {
  margin: 0 0 10px;
  font-size: 18px;
  color: #303133;
}

.icon-actions {
  display: flex;
  flex-shrink: 0;
}

.prompt-preview {
  display: -webkit-box;
  min-height: 48px;
  margin: 20px 0;
  overflow: hidden;
  color: #606266;
  line-height: 24px;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.card-actions {
  display: flex;
  justify-content: flex-end;
}

.prompt-content {
  padding: 16px;
  margin-top: 16px;
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.8;
  background: #f5f7fa;
  border-radius: 6px;
}
.settings-tabs {
  display: flex;
  gap: 36px;
  padding: 0 24px;
  margin-bottom: 24px;
  border-bottom: 1px solid #ebeef5;
   background: #fff;
}

.tab-item {
  padding: 16px 0;
  color: #606266;
  font-size: 15px;
  cursor: default;
}

.tab-item.active {
  color: #409eff;
  border-bottom: 2px solid #409eff;
  font-weight: 600;
}

.back-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;
  font-size: 26px;
  font-weight: 700;
}

.back-icon {
  cursor: pointer;
  font-size: 22px;
  transition: color 0.2s;
}

.back-icon:hover {
  color: #409eff;
}
</style>