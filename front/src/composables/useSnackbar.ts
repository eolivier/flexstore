import { ref } from 'vue';

export interface SnackbarAction {
  text: string;
  to: string;
}

export interface SnackbarOptions {
  action?: SnackbarAction;
  icon?: string;
  // Stays open until the user closes it, for a message that asks for an action
  persistent?: boolean;
}

export interface SnackbarMessage {
  id: number;
  text: string;
  color: 'primary' | 'warning' | 'error';
  timeout: number;
  action?: SnackbarAction;
  icon?: string;
}

const SNACKBAR_TIMEOUT = 3000;
const PERSISTENT_TIMEOUT = -1;

// One message at a time for the whole application: a new one replaces the previous one. It is displayed by the
// AppSnackbar mounted in App.vue.
const message = ref<SnackbarMessage | null>(null);
const open = ref(false);
let lastId = 0;

function show(color: SnackbarMessage['color'], text: string, options: SnackbarOptions) {
  const timeout = options.persistent ? PERSISTENT_TIMEOUT : SNACKBAR_TIMEOUT;
  message.value = { id: ++lastId, text, color, timeout, action: options.action, icon: options.icon };
  open.value = true;
}

export function useSnackbar() {
  const success = (text: string, options: SnackbarOptions = {}) => show('primary', text, options);
  const warning = (text: string, options: SnackbarOptions = {}) => show('warning', text, options);
  const error = (text: string, options: SnackbarOptions = {}) => show('error', text, options);
  const close = () => {
    open.value = false;
  };

  return { message, open, success, warning, error, close };
}
