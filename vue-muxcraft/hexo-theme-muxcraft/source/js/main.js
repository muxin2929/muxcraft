const bar = document.querySelector('.topbar')
window.addEventListener('scroll', () => {
  if (window.scrollY > 24) bar.style.boxShadow = '0 4px 20px rgba(40,75,90,.08)'
  else bar.style.boxShadow = 'none'
})
