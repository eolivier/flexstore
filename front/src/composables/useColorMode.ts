import { computed, watch } from 'vue';
import { useTheme } from 'vuetify';
import { useColorModeStore } from '@/stores/colorMode.store';
import { type ColorMode, oppositeColorMode, themeNames } from '@/utils/colorMode';

// The color mode of the site, switched by the customer and remembered for their next visits: in their browser,
// or in their account once signed in (where it applies as soon as they sign in)
export function useColorMode() {
  const theme = useTheme();
  const colorModeStore = useColorModeStore();
  const colorMode = computed(() => (theme.global.current.value.dark ? 'dark' : 'light'));

  function apply(newColorMode: ColorMode) {
    theme.change(themeNames[newColorMode]);
  }

  function switchColorMode() {
    const nextColorMode = oppositeColorMode(colorMode.value);
    apply(nextColorMode);
    colorModeStore.choose(nextColorMode);
  }

  watch(
    () => colorModeStore.accountColorMode,
    (accountColorMode) => {
      if (accountColorMode !== 'none') apply(accountColorMode);
    },
    { immediate: true }
  );

  return { colorMode, switchColorMode };
}
