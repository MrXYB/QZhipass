<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { Close } from '@element-plus/icons-vue'
import type { CreateAgentPayload } from './types'

const props = withDefaults(
  defineProps<{
    modelValue: boolean
    existingNames?: string[]
    submitting?: boolean
    maxNameLength?: number
  }>(),
  {
    existingNames: () => [],
    submitting: false,
    maxNameLength: 20,
  },
)

const emit = defineEmits<{
  'update:modelValue': [value: boolean]
  submit: [payload: CreateAgentPayload]
}>()

const form = reactive<CreateAgentPayload>({
  agentName: '',
  promptContent: '',
})
const attempted = ref(false)
const nameInput = ref<HTMLInputElement>()

const trimmedName = computed(() => form.agentName.trim())
const trimmedPrompt = computed(() => form.promptContent.trim())

const nameError = computed(() => {
  if (!trimmedName.value) return attempted.value ? '请输入 Agent 名称' : ''
  if (trimmedName.value.length > props.maxNameLength) {
    return `名称长度不得超过 ${props.maxNameLength} 个字符`
  }
  if (!/^[\u4e00-\u9fa5a-zA-Z0-9_-]+$/.test(trimmedName.value)) {
    return '仅支持中英文、数字、下划线和短横线（-）'
  }
  if (props.existingNames.some(name => name.trim().toLocaleLowerCase() === trimmedName.value.toLocaleLowerCase())) {
    return '该 Agent 名称已存在，请重新输入'
  }
  return ''
})

const promptError = computed(() => {
  if (!trimmedPrompt.value && attempted.value) return '请输入预设提示词'
  return ''
})

const canSubmit = computed(() => {
  return Boolean(trimmedName.value && trimmedPrompt.value && !nameError.value && !props.submitting)
})

function resetForm() {
  form.agentName = ''
  form.promptContent = ''
  attempted.value = false
}

function closeDialog() {
  if (props.submitting) return
  emit('update:modelValue', false)
}

function submitForm() {
  attempted.value = true
  if (!canSubmit.value) return
  emit('submit', {
    agentName: trimmedName.value,
    promptContent: trimmedPrompt.value,
  })
}

function handleGlobalKeydown(event: KeyboardEvent) {
  if (props.modelValue && event.key === 'Escape') {
    event.preventDefault()
    closeDialog()
  }
}

watch(
  () => props.modelValue,
  async visible => {
    if (!visible) return
    resetForm()
    await nextTick()
    nameInput.value?.focus()
  },
)

onMounted(() => {
  window.addEventListener('keydown', handleGlobalKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleGlobalKeydown)
})
</script>

<template>
  <Teleport to="body">
    <Transition name="agent-dialog">
      <div
        v-if="modelValue"
        class="agent-dialog-backdrop"
        data-testid="create-agent-dialog"
        @mousedown.self="closeDialog"
      >
        <section
          class="agent-dialog"
          role="dialog"
          aria-modal="true"
          aria-labelledby="create-agent-dialog-title"
        >
          <header class="dialog-header">
            <h2 id="create-agent-dialog-title">创建新的 Agent</h2>
            <button
              type="button"
              class="close-button"
              aria-label="关闭创建 Agent 弹窗"
              @click="closeDialog"
            >
              <el-icon><Close /></el-icon>
            </button>
          </header>

          <form class="dialog-form" @submit.prevent="submitForm">
            <div class="form-field">
              <label for="agent-name-input">
                Agent 名称 <em>*</em>
              </label>
              <input
                id="agent-name-input"
                ref="nameInput"
                v-model="form.agentName"
                type="text"
                :maxlength="maxNameLength"
                placeholder="请输入 Agent 名称，限 20 字以内"
                autocomplete="off"
                :aria-invalid="Boolean(nameError)"
                aria-describedby="agent-name-help agent-name-error"
              />
              <p v-if="nameError" id="agent-name-error" class="field-error">{{ nameError }}</p>
              <p id="agent-name-help" class="field-help">
                仅支持中英文、数字、下划线和短横线（-）
                <span>{{ form.agentName.length }}/{{ maxNameLength }}</span>
              </p>
            </div>

            <div class="form-field">
              <label for="agent-prompt-input">
                预设提示词 <em>*</em>
              </label>
              <textarea
                id="agent-prompt-input"
                v-model="form.promptContent"
                rows="6"
                placeholder="请输入预设提示词内容..."
                :aria-invalid="Boolean(promptError)"
                aria-describedby="agent-prompt-error"
              ></textarea>
              <p v-if="promptError" id="agent-prompt-error" class="field-error">{{ promptError }}</p>
            </div>
          </form>

          <footer class="dialog-footer">
            <button type="button" class="cancel-button" :disabled="submitting" @click="closeDialog">
              取消
            </button>
            <button
              type="button"
              class="save-button"
              :disabled="!canSubmit"
              data-testid="save-agent-button"
              @click="submitForm"
            >
              {{ submitting ? '保存中...' : '保存' }}
            </button>
          </footer>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.agent-dialog-backdrop {
  position: fixed;
  z-index: 3000;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(30, 41, 59, 0.48);
  backdrop-filter: blur(4px);
}

.agent-dialog {
  width: min(560px, 100%);
  overflow: hidden;
  color: #25324a;
  background: #ffffff;
  border: 1px solid rgba(255, 255, 255, 0.55);
  border-radius: 18px;
  box-shadow: 0 28px 70px rgba(15, 23, 42, 0.28);
}

.dialog-header {
  display: flex;
  min-height: 78px;
  align-items: center;
  justify-content: space-between;
  padding: 0 30px;
  border-bottom: 1px solid #eef1f5;
}

.dialog-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 720;
  letter-spacing: 0.01em;
}

.close-button {
  display: grid;
  width: 34px;
  height: 34px;
  place-items: center;
  color: #94a3b8;
  background: transparent;
  border: 0;
  border-radius: 8px;
  cursor: pointer;
  transition:
    color 0.18s ease,
    background 0.18s ease;
}

.close-button:hover {
  color: #475569;
  background: #f1f5f9;
}

.close-button:focus-visible,
.cancel-button:focus-visible,
.save-button:focus-visible {
  outline: 3px solid rgba(59, 130, 246, 0.22);
  outline-offset: 2px;
}

.dialog-form {
  display: grid;
  gap: 25px;
  padding: 30px;
}

.form-field {
  display: grid;
  gap: 8px;
}

.form-field label {
  color: #334155;
  font-size: 14px;
  font-weight: 650;
}

.form-field label em {
  color: #ef4444;
  font-style: normal;
}

.form-field input,
.form-field textarea {
  width: 100%;
  color: #334155;
  font: inherit;
  font-size: 14px;
  background: #ffffff;
  border: 1px solid #dbe2eb;
  border-radius: 13px;
  outline: 0;
  transition:
    border-color 0.18s ease,
    box-shadow 0.18s ease;
}

.form-field input {
  height: 52px;
  padding: 0 16px;
}

.form-field textarea {
  min-height: 150px;
  padding: 14px 16px;
  line-height: 1.65;
  resize: vertical;
}

.form-field input::placeholder,
.form-field textarea::placeholder {
  color: #a3adbd;
}

.form-field input:focus,
.form-field textarea:focus {
  border-color: #3b82f6;
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.13);
}

.form-field input[aria-invalid='true'],
.form-field textarea[aria-invalid='true'] {
  border-color: #f87171;
}

.field-help,
.field-error {
  margin: -2px 0 0;
  font-size: 11px;
  line-height: 1.5;
}

.field-help {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  color: #94a3b8;
}

.field-error {
  color: #ef4444;
}

.dialog-footer {
  display: flex;
  min-height: 84px;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  padding: 0 30px;
  background: #f8fafc;
  border-top: 1px solid #eef1f5;
}

.cancel-button,
.save-button {
  min-width: 86px;
  min-height: 42px;
  padding: 0 20px;
  font-size: 14px;
  font-weight: 600;
  border: 0;
  border-radius: 12px;
  cursor: pointer;
  transition:
    background 0.18s ease,
    color 0.18s ease;
}

.cancel-button {
  color: #475569;
  background: transparent;
}

.cancel-button:hover:not(:disabled) {
  background: #e9eef5;
}

.save-button {
  color: #ffffff;
  background: #2563eb;
}

.save-button:hover:not(:disabled) {
  background: #1d4ed8;
}

.save-button:disabled {
  color: #94a3b8;
  background: #e2e8f0;
  cursor: not-allowed;
}

.agent-dialog-enter-active,
.agent-dialog-leave-active {
  transition: opacity 0.2s ease;
}

.agent-dialog-enter-active .agent-dialog,
.agent-dialog-leave-active .agent-dialog {
  transition:
    transform 0.22s ease,
    opacity 0.22s ease;
}

.agent-dialog-enter-from,
.agent-dialog-leave-to {
  opacity: 0;
}

.agent-dialog-enter-from .agent-dialog,
.agent-dialog-leave-to .agent-dialog {
  opacity: 0;
  transform: translateY(12px) scale(0.98);
}

@media (max-width: 600px) {
  .agent-dialog-backdrop {
    align-items: end;
    padding: 12px;
  }

  .agent-dialog {
    border-radius: 18px 18px 12px 12px;
  }

  .dialog-header,
  .dialog-form,
  .dialog-footer {
    padding-right: 20px;
    padding-left: 20px;
  }

  .dialog-form {
    padding-top: 24px;
    padding-bottom: 24px;
  }
}
</style>
