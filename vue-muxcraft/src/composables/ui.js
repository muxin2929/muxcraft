import { reactive, ref } from 'vue'

// 全局 toast
const toasts = ref([])
let seq = 0
export function toast(message, type = 'success') {
  const id = ++seq
  toasts.value.push({ id, message, type })
  setTimeout(() => {
    toasts.value = toasts.value.filter(t => t.id !== id)
  }, 2600)
}
export function useToasts() { return toasts }

// 全局 confirm
const confirmState = reactive({
  open: false,
  title: '',
  message: '',
  resolve: null
})
export function confirmDialog({ title = '确认操作', message = '' }) {
  return new Promise(resolve => {
    confirmState.open = true
    confirmState.title = title
    confirmState.message = message
    confirmState.resolve = (v) => {
      confirmState.open = false
      resolve(v)
    }
  })
}
export function useConfirmState() { return confirmState }
