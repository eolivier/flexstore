import { nextTick, onBeforeUnmount, onMounted, ref, watch, type Ref } from 'vue';
import { visibleFocusAreaOf, type FocusArea } from '@/utils/guidedTourFocus';

const MENU_OPENING_DURATION = 350;

// The area of the page named `focus`, followed while the guided tour is open (null when it is not displayed)
export function useGuidedTourFocus(focus: Ref<string>, isOpen: Ref<boolean>) {
  const focusArea = ref<FocusArea | null>(null);

  function measure() {
    focusArea.value = isOpen.value ? visibleFocusAreaOf(focus.value) : null;
  }

  // Next tick: the page is displayed before the area is measured; measured again once a menu has finished opening
  let remeasure: ReturnType<typeof setTimeout>;
  watch(
    [focus, isOpen],
    () => {
      nextTick(measure);
      clearTimeout(remeasure);
      remeasure = setTimeout(measure, MENU_OPENING_DURATION);
    },
    { immediate: true }
  );
  onMounted(() => window.addEventListener('resize', measure));
  onBeforeUnmount(() => {
    window.removeEventListener('resize', measure);
    clearTimeout(remeasure);
  });

  return { focusArea };
}
