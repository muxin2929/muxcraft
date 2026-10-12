// v-tilt：卡片跟随鼠标轻微 3D 倾斜，离开回弹。仅装饰。
export const tilt = {
  mounted(el) {
    el.style.transformStyle = 'preserve-3d'
    el.style.transition = 'transform .25s ease'
    function onMove(e) {
      const r = el.getBoundingClientRect()
      const x = (e.clientX - r.left) / r.width - 0.5
      const y = (e.clientY - r.top) / r.height - 0.5
      el.style.transform = `perspective(700px) rotateX(${(-y * 5).toFixed(2)}deg) rotateY(${(x * 5).toFixed(2)}deg) translateY(-4px)`
    }
    function onLeave() {
      el.style.transform = ''
    }
    el.addEventListener('mousemove', onMove)
    el.addEventListener('mouseleave', onLeave)
    el._tiltCleanup = () => {
      el.removeEventListener('mousemove', onMove)
      el.removeEventListener('mouseleave', onLeave)
    }
  },
  unmounted(el) { el._tiltCleanup?.() }
}
