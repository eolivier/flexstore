export default {
  about: {
    title: 'About Flexstore',
    intro:
      'Flexstore is a demo online shop. It is an open source project that shows how to build an e-commerce application around use cases: each user journey (creating an account, signing in, adding a product to the cart…) is an explicit, tested use case.',
    projectTitle: 'The project',
    projectText:
      'The back end is written in Kotlin with a hexagonal architecture: a pure domain with no framework, and Spring Boot adapters for the REST API and the database. It is meant to be a learning resource and a base that can be reused for real projects.',
    featuresTitle: 'What you can do',
    features: {
      shopping: {
        title: 'Shopping',
        subtitle: 'Open to every visitor: no account is needed to browse.',
        catalog: {
          title: 'Browse the catalog',
          text: 'Explore products by category and open a product page with its photos and description.',
        },
        cart: {
          title: 'Fill your cart',
          text: 'Add products, change quantities and keep your cart while you keep shopping.',
        },
        checkout: {
          title: 'Check out',
          text: 'Choose a delivery address, apply a discount code and place your order.',
        },
        reviews: {
          title: 'Read and write reviews',
          text: 'See what other customers think and share your own opinion.',
        },
      },
      account: {
        title: 'My account',
        subtitle: 'Available once signed in.',
        profile: { title: 'Profile', text: 'Update your name, your email and your password.' },
        orders: { title: 'Orders', text: 'Follow the status of your orders and review their details.' },
        addresses: { title: 'Address book', text: 'Save your delivery addresses to check out faster.' },
        wishlist: { title: 'Wishlist', text: 'Keep the products you love for later.' },
        loyalty: {
          title: 'Loyalty programme',
          text: 'Loyalty is a feature of Flexstore: earn 1 point for each euro or dollar spent and check your balance.',
        },
        security: {
          title: 'Security',
          text: 'Verify your email and protect your account with two-factor authentication.',
        },
        googleSignIn: {
          title: 'Sign in with Google',
          text: 'Sign in with your Google account instead of creating a password.',
        },
        twoFactorAuthentication: {
          title: 'Two-factor authentication',
          text: 'Add a second verification step at sign-in to keep your account safe.',
        },
      },
      admin: {
        title: 'Administration',
        subtitle: 'Reserved to administrators, from the admin menu.',
        sales: { title: 'Sales dashboard', text: 'Follow the revenue and the orders at a glance.' },
        users: {
          title: 'Users',
          text: 'Create and edit accounts, and disable the two-factor authentication of a user.',
        },
        products: {
          title: 'Products',
          text: 'Create and edit products, with their translations and their images.',
        },
        orders: { title: 'Orders', text: 'Track every customer order and update its status.' },
        productPromotions: {
          title: 'Product promotions',
          text: 'Put a product on sale with a percentage off between two dates: the regular price is shown struck through.',
        },
        discountCodes: { title: 'Discount codes', text: 'Create promotional codes for your customers.' },
        seller: { title: 'Seller information', text: 'Set the identity of the shop shown on invoices.' },
        legalPages: {
          title: 'Legal pages',
          text: 'Edit the legal notice, the terms of sale and the privacy policy.',
        },
        settings: { title: 'Settings', text: 'Adjust the settings of the shop.' },
        environment: {
          title: 'Environment',
          text: 'Check the configuration of the back end: the environment variables defined, with their value, except for the secrets, shown only as defined or not.',
        },
        sellerSetup: {
          title: 'Shop setup assistant',
          text: 'Until the shop is set up, a step-by-step assistant guides the administrator through the seller information to get it ready.',
        },
      },
      automatic: {
        title: 'Automatic tasks',
        subtitle: 'Scheduled tasks that run on their own, with nothing to do.',
        unpaidOrders: {
          title: 'Unpaid orders released',
          text: 'An order not paid within the payment delay of the settings is cancelled automatically, so that its reserved units go back on sale.',
        },
        abandonedCart: {
          title: 'Abandoned cart reminder',
          text: 'A customer who left items in the cart receives one email that lists them and leads back to the cart, when the administrator turns it on.',
        },
      },
      assistants: {
        title: 'AI assistants',
        subtitle: 'The shop can be read by the AI assistants you already use.',
        mcp: {
          title: 'MCP server for the products',
          text: 'An AI assistant such as Claude Desktop or Claude Code can list the products and read one of them, in English or in French, through the Model Context Protocol at /mcp. The server only gives the catalog and can be turned off with FLEXSTORE_MCP_ENABLED.',
        },
      },
    },
    stackTitle: 'Built with',
    stack: {
      backend: 'Kotlin and Spring Boot',
      database: 'PostgreSQL',
      frontend: 'Vue 3 and Vuetify',
    },
    sourceTitle: 'Source code',
    sourceText: 'The code is available on GitHub.',
    sourceButton: 'View on GitHub',
  },
};
