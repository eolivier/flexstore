import { onBeforeUnmount, onMounted, ref } from 'vue';

// The current moment, updated every second while the component is shown
export function useNow() {
  const now = ref(new Date());
  let timer: ReturnType<typeof setInterval> | undefined;

  onMounted(() => {
    timer = setInterval(() => (now.value = new Date()), 1000);
  });
  onBeforeUnmount(() => clearInterval(timer));

  return { now };
}
