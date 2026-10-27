import { computed } from 'vue';
import { useTheme } from 'vuetify';
import { oppositeColorMode, rememberColorMode, themeNames } from '@/utils/colorMode';

// The color mode of the site, switched by the customer and remembered for their next visits
export function useColorMode() {
  const theme = useTheme();
  const colorMode = computed(() => (theme.global.current.value.dark ? 'dark' : 'light'));

  function switchColorMode() {
    const nextColorMode = oppositeColorMode(colorMode.value);
    theme.change(themeNames[nextColorMode]);
    rememberColorMode(nextColorMode);
  }

  return { colorMode, switchColorMode };
}
