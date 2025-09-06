import { createApp } from 'vue'
import './style.css'
import { createPinia } from 'pinia'
import { createVuetify } from 'vuetify'
import * as components from 'vuetify/components'
import * as directives from 'vuetify/directives'

import 'vuetify/styles'
import App from './App.vue'

const vuetify = createVuetify({ components, directives })

const pinia = createPinia()
const app = createApp(App)
app.use(pinia)
app.use(vuetify)
app.mount('#app')
