export default {
  legalPages: {
    'legal-notice': { title: 'Legal notice' },
    'terms-of-sale': { title: 'Terms of sale' },
    'privacy-policy': { title: 'Privacy policy' },
    publishedOn: 'Updated on {date}',
    notWritten: 'This page has not been written yet.',
    loadError: 'The page could not be loaded.',
    adminTitle: 'Legal pages',
    adminSubtitle:
      'The legal notice, the terms of sale and the privacy policy of the shop, read by everyone from the footer.',
    textHint: 'Plain text: each line break is kept',
    required: 'Required',
    tooLong: '50,000 characters at most',
    publish: 'Publish',
    see: 'See the page',
    saved: 'Published: the visitors now read this version.',
    invalid: 'The shop refused the page: its text is empty or too long.',
    saveError: 'The page could not be published.',
    versionsNote: 'Each publication is kept as a new version: the versions before are not lost.',
  },
  shopPageContent: {
    title: 'Shop page content',
    subtitle:
      'What the shop tells the search engines and the visitor statistics. Each change is shown at once to the visitors.',
    META_KEY_WORDS: {
      label: 'Key words',
      hint: 'Key words of the shop, separated by commas',
    },
    META_SHOP_DESCRIPTION: {
      label: 'Description of the shop',
      hint: 'The text that the search engines show under the title of the shop',
    },
    GOOGLE_ANALYTICS_ID: {
      label: 'Google Analytics id',
      hint: 'For example G-ABC123DEF4. Visits are only measured for the visitors who accepted the cookies. Empty: no measure',
    },
    tooLong: '1,000 characters at most',
    badIdentifier: 'Letters, digits and hyphens only',
    saved: 'Saved: the visitors now read this content.',
    invalid: 'The shop refused the content: it is too long, or it is not an identifier.',
    saveError: 'The content could not be saved.',
    loadError: 'The content of the shop pages could not be loaded.',
  },
};
