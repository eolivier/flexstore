// The color shared by both themes: the orange of the brand
const brandColors = {
  secondary: '#E4572E',
  accent: '#E4572E',
};

export const flexstoreLight = {
  dark: false,
  colors: {
    ...brandColors,
    background: '#F7F6F3',
    surface: '#FFFFFF',
    'surface-variant': '#EEECE7',
    'on-surface-variant': '#3A3F4B',
    primary: '#1F2A44',
    footer: '#1F2A44',
    // The call to action of the home page
    banner: '#1F2A44',
    info: '#2F6FDE',
    success: '#2E8B57',
    warning: '#D98E04',
    error: '#C62828',
    // Backgrounds of the product visuals, by category
    'tint-clothing': '#F6E3DA',
    'tint-electronics': '#DFE6F3',
    'tint-accessories': '#E3EDE3',
    'tint-other': '#EEECE7',
  },
  variables: {
    'border-color': '#1F2A44',
    'border-opacity': 0.1,
    'high-emphasis-opacity': 0.92,
    'medium-emphasis-opacity': 0.64,
  },
};

// The navy of the brand becomes the background; the primary color lightens to stay readable on it
export const flexstoreDark = {
  dark: true,
  colors: {
    ...brandColors,
    background: '#12151C',
    surface: '#1A1E27',
    'surface-variant': '#262B36',
    'on-surface-variant': '#C9CDD6',
    primary: '#AFC0EA',
    footer: '#0C0E13',
    banner: '#28324D',
    info: '#6FA0F0',
    success: '#4FB37E',
    warning: '#E8A93A',
    error: '#EF6B6B',
    'tint-clothing': '#3A2A24',
    'tint-electronics': '#252E3F',
    'tint-accessories': '#26332A',
    'tint-other': '#2A2E37',
  },
  variables: {
    'border-color': '#FFFFFF',
    'border-opacity': 0.12,
    'high-emphasis-opacity': 0.92,
    'medium-emphasis-opacity': 0.68,
  },
};

export const themes = { flexstoreLight, flexstoreDark };
