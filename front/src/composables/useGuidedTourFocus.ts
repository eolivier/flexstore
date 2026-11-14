import { nextTick, onBeforeUnmount, onMounted, ref, watch, type Ref } from 'vue';
import { visibleFocusAreaOf, type FocusArea } from '@/utils/guidedTourFocus';

// The area of the page named `focus`, followed while the guided tour is open (null when it is not displayed)
export function useGuidedTourFocus(focus: Ref<string>, isOpen: Ref<boolean>) {
  const focusArea = ref<FocusArea | null>(null);

  function measure() {
    focusArea.value = isOpen.value ? visibleFocusAreaOf(focus.value) : null;
  }

  // Next tick: the page is displayed before the area is measured
  watch([focus, isOpen], () => nextTick(measure), { immediate: true });
  onMounted(() => window.addEventListener('resize', measure));
  onBeforeUnmount(() => window.removeEventListener('resize', measure));

  return { focusArea };
}
