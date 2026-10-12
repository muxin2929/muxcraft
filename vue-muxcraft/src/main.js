import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { reveal } from './directives/reveal.js'
import { tilt } from './directives/tilt.js'

import './assets/main.css'

createApp(App)
  .use(router)
  .directive('reveal', reveal)
  .directive('tilt', tilt)
  .mount('#app')
