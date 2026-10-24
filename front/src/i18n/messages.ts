export default {
  en: {
    profile: {
      title: "My account",
      notSignedIn: "You are not signed in",
      notSignedInText: "Sign in to see your account information.",
      name: "Name",
      email: "Email",
      shortcuts: "Shortcuts",
      passwordTitle: "Password",
      passwordText: "Use a long password that you do not use on other sites.",
      currentPassword: "Current password",
      newPassword: "New password",
      confirmPassword: "Confirm the new password",
      passwordsDiffer: "The passwords are different",
      passwordUnchanged: "The new password must be different",
      changePassword: "Change password",
      passwordChanged: "Your password has been changed.",
      wrongPassword: "The current password is wrong.",
      passwordError: "The password could not be changed."
    },
    admin: {
      title: "Administration",
      subtitle: "Manage the users and the products of the shop.",
      usersText: "See and delete the user accounts.",
      createUserText: "Add an account that can sign in.",
      createProductText: "Add a product to the catalog.",
      settingsText: "Turn the features of the shop on or off."
    },
    settings: {
      title: "Settings",
      subtitle: "Turn the features of the shop on or off.",
      orderConfirmationEmail: "Order confirmation email",
      orderConfirmationEmailText: "Send an email to the customer, with the summary of the order, when an order is placed.",
      saveError: "The setting could not be saved.",
      loadError: "The settings could not be loaded."
    },
    createProduct: {
      title: "Create a product",
      name: "Product name",
      description: "Description",
      category: "Category",
      price: "Price (€)",
      submit: "Create product",
      success: "The product {name} has been created.",
      error: "The product could not be created.",
      mandatoryField: "Mandatory field",
      invalidPrice: "Enter a positive price"
    },
    about: {
      title: "About Flexstore",
      intro: "Flexstore is a demo online shop. It is an open source project that shows how to build an e-commerce application around use cases: each user journey (creating an account, signing in, adding a product to the cart…) is an explicit, tested use case.",
      projectTitle: "The project",
      projectText: "The back end is written in Kotlin with a hexagonal architecture: a pure domain with no framework, and Spring Boot adapters for the REST API and the database. It is meant to be a learning resource and a base that can be reused for real projects.",
      stackTitle: "Built with",
      stack: {
        backend: "Kotlin and Spring Boot",
        database: "PostgreSQL",
        frontend: "Vue 3 and Vuetify"
      },
      sourceTitle: "Source code",
      sourceText: "The code is available on GitHub.",
      sourceButton: "View on GitHub"
    },
    home: {
      eyebrow: "Welcome to Flexstore",
      heroButton: "Browse products",
      categoriesTitle: "Shop by category",
      categoryLink: "Discover",
      featuredTitle: "Our products",
      seeAll: "See all products",
      title: "Shopping made simple, from click to doorstep",
      intro: "Flexstore was born from a simple idea: buying the things you need every day should be easy, transparent and pleasant. We bring together clothing, electronics and accessories in one place, carefully selected for their quality and their value for money.",
      valuesTitle: "What we stand for",
      values: {
        quality: {
          title: "Selected quality",
          text: "Every product is chosen for its durability and its everyday usefulness. We would rather offer fewer items than products we would not buy ourselves."
        },
        transparency: {
          title: "Clear prices",
          text: "The price you see is the price you pay. No hidden fees at checkout, and clear product descriptions so you know exactly what you are buying."
        },
        care: {
          title: "Responsible choices",
          text: "We favour products designed to last, such as organic cotton clothing, and try to limit unnecessary packaging."
        }
      },
      commitmentsTitle: "Our commitments",
      commitments: {
        delivery: {
          title: "Fast delivery",
          text: "Orders are prepared quickly and you can follow their progress from your account."
        },
        payment: {
          title: "Secure payment",
          text: "Your personal data and passwords are protected and never shared."
        },
        returns: {
          title: "Easy returns",
          text: "Changed your mind? Contact us and we will help you return your item."
        },
        support: {
          title: "Attentive support",
          text: "A question about a product or an order? Our team answers you as quickly as possible."
        }
      },
      ctaTitle: "Ready to explore the shop?",
      ctaText: "Discover our clothing, electronics and accessories.",
      ctaButton: "Browse products"
    },
    table: {
      id: "ID",
      name: "Name",
      email: "Email",
      description: "Description",
      category: "Category",
      price: "Price",
      actions: "Actions",
      quantity: "Quantity",
      orderId: "Order",
      customer: "Customer",
      items: "Items",
      total: "Total",
      status: "Status"
    },
    products: {
      addToCart: "Add to cart",
      count: "No products | 1 product | {n} products",
      sort: "Sort by",
      sortName: "Name",
      sortPriceAsc: "Price: low to high",
      sortPriceDesc: "Price: high to low",
      empty: "No products in this category yet.",
      error: "The products could not be loaded.",
      retry: "Retry",
      added: "{name} added to your cart",
      addError: "The product could not be added to your cart.",
      viewCart: "View cart",
      shop: "Shop",
      quantity: "Quantity",
      inCart: "Not in your cart yet | 1 already in your cart | {n} already in your cart",
      notFound: "Product not found",
      notFoundText: "This product does not exist or is no longer available.",
      backToShop: "Back to the shop",
      similar: "In the same category"
    },
    cart: {
      title: "My cart",
      total: "Total",
      increase: "Increase quantity",
      decrease: "Decrease quantity",
      remove: "Remove from cart",
      unitPrice: "{price} each",
      summary: "Summary",
      itemsCount: "No items | 1 item | {n} items",
      continue: "Continue shopping",
      empty: "Your cart is empty",
      emptyText: "Browse the shop and add the products you like.",
      accountItemsMerged: "The items saved in your account have been added to your cart.",
      placeOrder: "Place order",
      signInToOrder: "You will sign in to confirm your order.",
      orderError: "The order could not be placed. Please try again."
    },
    orders: {
      title: "Orders",
      number: "Order {number}",
      placedOn: "Placed on {date}",
      thanks: "Thank you for your order!",
      confirmation: "Your order {number} was placed on {date}.",
      seeDetail: "See the order details",
      empty: "No orders yet",
      emptyText: "Your orders will appear here once placed from your cart.",
      loadError: "Your orders could not be loaded.",
      viewDetail: "View order {number}",
      progress: "Order progress",
      backToOrders: "My orders",
      product: "Product",
      unitPrice: "Unit price",
      quantity: "Quantity",
      lineTotal: "Total",
      total: "Order total",
      itemsCount: "No items | 1 item | {n} items",
      notFound: "Order not found",
      notFoundText: "This order does not exist or does not belong to your account.",
      orderLoadError: "The order could not be loaded.",
      status: {
        PROCESSING: "Processing",
        SHIPPED: "Shipped",
        DELIVERED: "Delivered"
      }
    },
    loading: {
      inProgress: "Loading…"
    },
    users: {
      loading: "Loading the users…",
      title: "Users",
      delete: "Delete",
      count: "No users | 1 user | {n} users",
      deleted: "The user has been deleted.",
      deleteError: "The user could not be deleted."
    },
    nav: {
      home: "Home",
      shop: "Shop",
      allProducts: "All products",
      clothing: "Clothing",
      electronics: "Electronics",
      accessories: "Accessories",
      cart: "Cart",
      orders: "Orders",
      profile: "Profile",
      account: "My account",
      admin: "Administration",
      users: "Users",
      createUser: "Create a user",
      createProduct: "Create a product",
      settings: "Settings",
      about: "About",
      login: "Sign in",
      logout: "Sign out",
      language: "Language",
      openMenu: "Open menu"
    },
    footer: {
      tagline: "Everyday essentials, delivered fast.",
      rights: "All rights reserved."
    },
    confirmUserDeletion: "Are you sure you want to delete this user?",
    cancel : "Cancel",
    confirm : "Confirm",
    confirmation : "Confirmation",
    createUser: {
      name: "User's Name",
      email: "User's Email",
      password: "Password",
      label: "Create user",
      cancel: "Cancel",
      mandatoryField: "Mandatory field",
      invalidEmail: "Invalid email format",
      passwordTooShort: "Password must be at least 8 characters",
      success: "The user {name} has been created.",
      error: "The user could not be created (the email may already be used).",
      showPassword: "Show password",
      role: "Role",
      roles: {
        USER: { label: "User", text: "Shops and follows their orders." },
        ADMIN: { label: "Administrator", text: "Also manages the users, the products and the settings." }
      }
    },
    login: {
      title: "Login",
      subtitle: "Sign in to access your account and administration.",
      email: "Email",
      password: "Password",
      submit: "Sign In",
      error: "Invalid email or password",
      mandatoryField: "Mandatory field",
      invalidEmail: "Invalid email format"
    }
  },
  fr: {
    profile: {
      title: "Mon compte",
      notSignedIn: "Vous n'êtes pas connecté",
      notSignedInText: "Connectez-vous pour voir les informations de votre compte.",
      name: "Nom",
      email: "Email",
      shortcuts: "Raccourcis",
      passwordTitle: "Mot de passe",
      passwordText: "Choisissez un mot de passe long, que vous n'utilisez pas sur d'autres sites.",
      currentPassword: "Mot de passe actuel",
      newPassword: "Nouveau mot de passe",
      confirmPassword: "Confirmez le nouveau mot de passe",
      passwordsDiffer: "Les mots de passe sont différents",
      passwordUnchanged: "Le nouveau mot de passe doit être différent",
      changePassword: "Changer le mot de passe",
      passwordChanged: "Votre mot de passe a été modifié.",
      wrongPassword: "Le mot de passe actuel est incorrect.",
      passwordError: "Le mot de passe n'a pas pu être modifié."
    },
    admin: {
      title: "Administration",
      subtitle: "Gérez les utilisateurs et les produits de la boutique.",
      usersText: "Consulter et supprimer les comptes utilisateurs.",
      createUserText: "Ajouter un compte qui pourra se connecter.",
      createProductText: "Ajouter un produit au catalogue.",
      settingsText: "Activer ou désactiver les fonctionnalités de la boutique."
    },
    settings: {
      title: "Paramètres",
      subtitle: "Activez ou désactivez les fonctionnalités de la boutique.",
      orderConfirmationEmail: "Email de confirmation de commande",
      orderConfirmationEmailText: "Envoyer au client un email récapitulant sa commande lorsqu'elle est passée.",
      saveError: "Le paramètre n'a pas pu être enregistré.",
      loadError: "Les paramètres n'ont pas pu être chargés."
    },
    createProduct: {
      title: "Créer un produit",
      name: "Nom du produit",
      description: "Description",
      category: "Catégorie",
      price: "Prix (€)",
      submit: "Créer le produit",
      success: "Le produit {name} a été créé.",
      error: "Le produit n'a pas pu être créé.",
      mandatoryField: "Champ obligatoire",
      invalidPrice: "Saisissez un prix positif"
    },
    about: {
      title: "À propos de Flexstore",
      intro: "Flexstore est une boutique en ligne de démonstration. C'est un projet open source qui montre comment construire une application e-commerce autour des cas d'utilisation : chaque parcours utilisateur (créer un compte, se connecter, ajouter un produit au panier…) est un cas d'utilisation explicite et testé.",
      projectTitle: "Le projet",
      projectText: "Le backend est écrit en Kotlin avec une architecture hexagonale : un domaine pur, sans framework, et des adaptateurs Spring Boot pour l'API REST et la base de données. Il se veut à la fois un support d'apprentissage et une base réutilisable pour de vrais projets.",
      stackTitle: "Technologies",
      stack: {
        backend: "Kotlin et Spring Boot",
        database: "PostgreSQL",
        frontend: "Vue 3 et Vuetify"
      },
      sourceTitle: "Code source",
      sourceText: "Le code est disponible sur GitHub.",
      sourceButton: "Voir sur GitHub"
    },
    home: {
      eyebrow: "Bienvenue sur Flexstore",
      heroButton: "Voir les produits",
      categoriesTitle: "Acheter par catégorie",
      categoryLink: "Découvrir",
      featuredTitle: "Nos produits",
      seeAll: "Voir tous les produits",
      title: "Des achats simples, du clic jusqu'à votre porte",
      intro: "Flexstore est né d'une idée simple : acheter ce dont on a besoin au quotidien doit être facile, transparent et agréable. Nous réunissons vêtements, électronique et accessoires au même endroit, sélectionnés avec soin pour leur qualité et leur bon rapport qualité-prix.",
      valuesTitle: "Ce qui nous anime",
      values: {
        quality: {
          title: "Une qualité choisie",
          text: "Chaque produit est retenu pour sa durabilité et son utilité au quotidien. Nous préférons proposer moins d'articles plutôt que des produits que nous n'achèterions pas nous-mêmes."
        },
        transparency: {
          title: "Des prix clairs",
          text: "Le prix affiché est le prix payé. Pas de frais cachés au moment de payer, et des descriptions claires pour savoir exactement ce que vous achetez."
        },
        care: {
          title: "Des choix responsables",
          text: "Nous privilégions les produits conçus pour durer, comme les vêtements en coton bio, et cherchons à limiter les emballages inutiles."
        }
      },
      commitmentsTitle: "Nos engagements",
      commitments: {
        delivery: {
          title: "Livraison rapide",
          text: "Les commandes sont préparées rapidement et vous pouvez suivre leur avancement depuis votre compte."
        },
        payment: {
          title: "Paiement sécurisé",
          text: "Vos données personnelles et vos mots de passe sont protégés et ne sont jamais partagés."
        },
        returns: {
          title: "Retours faciles",
          text: "Vous avez changé d'avis ? Contactez-nous et nous vous aiderons à retourner votre article."
        },
        support: {
          title: "Un service attentif",
          text: "Une question sur un produit ou une commande ? Notre équipe vous répond dans les meilleurs délais."
        }
      },
      ctaTitle: "Prêt à découvrir la boutique ?",
      ctaText: "Parcourez nos vêtements, notre électronique et nos accessoires.",
      ctaButton: "Voir les produits"
    },
    table: {
      id: "ID",
      name: "Nom",
      email: "Email",
      description: "Description",
      category: "Catégorie",
      price: "Prix",
      actions: "Actions",
      quantity: "Quantité",
      orderId: "Commande",
      customer: "Client",
      items: "Articles",
      total: "Total",
      status: "Statut"
    },
    products: {
      addToCart: "Ajouter au panier",
      count: "Aucun produit | 1 produit | {n} produits",
      sort: "Trier par",
      sortName: "Nom",
      sortPriceAsc: "Prix croissant",
      sortPriceDesc: "Prix décroissant",
      empty: "Aucun produit dans cette catégorie pour le moment.",
      error: "Les produits n'ont pas pu être chargés.",
      retry: "Réessayer",
      added: "{name} a été ajouté à votre panier",
      addError: "Le produit n'a pas pu être ajouté à votre panier.",
      viewCart: "Voir le panier",
      shop: "Boutique",
      quantity: "Quantité",
      inCart: "Pas encore dans votre panier | 1 déjà dans votre panier | {n} déjà dans votre panier",
      notFound: "Produit introuvable",
      notFoundText: "Ce produit n'existe pas ou n'est plus disponible.",
      backToShop: "Retour à la boutique",
      similar: "Dans la même catégorie"
    },
    cart: {
      title: "Mon panier",
      total: "Total",
      increase: "Augmenter la quantité",
      decrease: "Diminuer la quantité",
      remove: "Retirer du panier",
      unitPrice: "{price} l'unité",
      summary: "Récapitulatif",
      itemsCount: "Aucun article | 1 article | {n} articles",
      continue: "Continuer mes achats",
      empty: "Votre panier est vide",
      emptyText: "Parcourez la boutique et ajoutez les produits qui vous plaisent.",
      accountItemsMerged: "Les articles enregistrés dans votre compte ont été ajoutés à votre panier.",
      placeOrder: "Passer la commande",
      signInToOrder: "Vous vous connecterez pour valider la commande.",
      orderError: "La commande n'a pas pu être passée. Veuillez réessayer."
    },
    orders: {
      title: "Commandes",
      number: "Commande {number}",
      placedOn: "Passée le {date}",
      thanks: "Merci pour votre commande !",
      confirmation: "Votre commande {number} a été enregistrée le {date}.",
      seeDetail: "Voir le détail de la commande",
      empty: "Aucune commande pour l'instant",
      emptyText: "Vos commandes apparaîtront ici une fois passées depuis votre panier.",
      loadError: "Vos commandes n'ont pas pu être chargées.",
      viewDetail: "Voir la commande {number}",
      progress: "Avancement de la commande",
      backToOrders: "Mes commandes",
      product: "Produit",
      unitPrice: "Prix unitaire",
      quantity: "Quantité",
      lineTotal: "Total",
      total: "Total de la commande",
      itemsCount: "Aucun article | 1 article | {n} articles",
      notFound: "Commande introuvable",
      notFoundText: "Cette commande n'existe pas ou n'appartient pas à votre compte.",
      orderLoadError: "La commande n'a pas pu être chargée.",
      status: {
        PROCESSING: "En préparation",
        SHIPPED: "Expédiée",
        DELIVERED: "Livrée"
      }
    },
    loading: {
      inProgress: "Chargement en cours…"
    },
    users: {
      loading: "Chargement des utilisateurs…",
      title: "Utilisateurs",
      delete: "Supprimer",
      count: "Aucun utilisateur | 1 utilisateur | {n} utilisateurs",
      deleted: "L'utilisateur a été supprimé.",
      deleteError: "L'utilisateur n'a pas pu être supprimé."
    },
    nav: {
      home: "Accueil",
      shop: "Boutique",
      allProducts: "Tous les produits",
      clothing: "Vêtements",
      electronics: "Électronique",
      accessories: "Accessoires",
      cart: "Panier",
      orders: "Commandes",
      profile: "Profil",
      account: "Mon compte",
      admin: "Administration",
      users: "Utilisateurs",
      createUser: "Créer un utilisateur",
      createProduct: "Créer un produit",
      settings: "Paramètres",
      about: "À propos",
      login: "Se connecter",
      logout: "Se déconnecter",
      language: "Langue",
      openMenu: "Ouvrir le menu"
    },
    footer: {
      tagline: "L'essentiel du quotidien, livré rapidement.",
      rights: "Tous droits réservés."
    },
    confirmUserDeletion: "Voulez-vous vraiment supprimer cet utilisateur ?",
    cancel : "Annuler",
    confirm : "Confirmer",
    confirmation : "Confirmation",
    createUser: {
      name: "Nom de l'utilisateur",
      email: "Email de l'utilisateur",
      password: "Mot de passe",
      label: "Créer utilisateur",
      cancel: "Annuler",
      mandatoryField: "Champ obligatoire",
      invalidEmail: "Format d'email invalide",
      passwordTooShort: "Le mot de passe doit contenir au moins 8 caractères",
      success: "L'utilisateur {name} a été créé.",
      error: "L'utilisateur n'a pas pu être créé (l'email est peut-être déjà utilisé).",
      showPassword: "Afficher le mot de passe",
      role: "Rôle",
      roles: {
        USER: { label: "Utilisateur", text: "Achète et suit ses commandes." },
        ADMIN: { label: "Administrateur", text: "Gère aussi les utilisateurs, les produits et les paramètres." }
      }
    },
    login: {
      title: "Connexion",
      subtitle: "Connectez-vous pour accéder à votre compte et à l'administration.",
      email: "Email",
      password: "Mot de passe",
      submit: "Se connecter",
      error: "Email ou mot de passe invalide",
      mandatoryField: "Champ obligatoire",
      invalidEmail: "Format d'email invalide"
    }
  }
};