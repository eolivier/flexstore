import { createApp } from 'vue'
import './style.css'
import { createPinia } from 'pinia'
import { createVuetify } from 'vuetify'
import 'vuetify/styles'
import { aliases, mdi } from 'vuetify/iconsets/mdi'
import '@mdi/font/css/materialdesignicons.css'
import App from './App.vue'

const pinia = createPinia()
//const vuetify = createVuetify()
const vuetify = createVuetify({
    icons: {
        defaultSet: 'mdi',
        aliases,
        sets: { mdi }
    }
})
const app = createApp(App)
app.use(pinia)
app.use(vuetify)
app.mount('#app')
