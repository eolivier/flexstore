export default {
  about: {
    title: 'À propos de Flexstore',
    intro:
      "Flexstore est une boutique en ligne de démonstration. C'est un projet open source qui montre comment construire une application e-commerce autour des cas d'utilisation : chaque parcours utilisateur (créer un compte, se connecter, ajouter un produit au panier…) est un cas d'utilisation explicite et testé.",
    projectTitle: 'Le projet',
    projectText:
      "Le backend est écrit en Kotlin avec une architecture hexagonale : un domaine pur, sans framework, et des adaptateurs Spring Boot pour l'API REST et la base de données. Il se veut à la fois un support d'apprentissage et une base réutilisable pour de vrais projets.",
    featuresTitle: 'Ce que vous pouvez faire',
    features: {
      shopping: {
        title: 'Achats',
        subtitle: 'Ouvert à tous les visiteurs : aucun compte n’est nécessaire pour parcourir la boutique.',
        catalog: {
          title: 'Parcourir le catalogue',
          text: 'Explorez les produits par catégorie et ouvrez une fiche produit avec ses photos et sa description.',
        },
        cart: {
          title: 'Remplir son panier',
          text: 'Ajoutez des produits, modifiez les quantités et gardez votre panier pendant vos achats.',
        },
        checkout: {
          title: 'Passer commande',
          text: 'Choisissez une adresse de livraison, appliquez un code de réduction et validez votre commande.',
        },
        reviews: {
          title: 'Lire et écrire des avis',
          text: 'Découvrez l’avis des autres clients et partagez le vôtre.',
        },
      },
      account: {
        title: 'Mon compte',
        subtitle: 'Disponible une fois connecté.',
        profile: { title: 'Profil', text: 'Modifiez votre nom, votre email et votre mot de passe.' },
        orders: { title: 'Commandes', text: 'Suivez le statut de vos commandes et consultez leur détail.' },
        addresses: {
          title: 'Carnet d’adresses',
          text: 'Enregistrez vos adresses de livraison pour commander plus vite.',
        },
        wishlist: { title: 'Liste d’envies', text: 'Gardez de côté les produits que vous aimez.' },
        loyalty: {
          title: 'Programme de fidélité',
          text: 'La fidélité est une fonctionnalité de Flexstore : gagnez 1 point par euro ou dollar dépensé et consultez votre solde.',
        },
        security: {
          title: 'Sécurité',
          text: 'Vérifiez votre email et protégez votre compte avec l’authentification à deux facteurs.',
        },
        googleSignIn: {
          title: 'Connexion avec Google',
          text: 'Connectez-vous avec votre compte Google, sans créer de mot de passe.',
        },
        twoFactorAuthentication: {
          title: 'Authentification à deux facteurs',
          text: 'Ajoutez une seconde étape de vérification à la connexion pour protéger votre compte.',
        },
      },
      admin: {
        title: 'Administration',
        subtitle: 'Réservée aux administrateurs, depuis le menu d’administration.',
        sales: {
          title: 'Tableau de bord des ventes',
          text: 'Suivez le chiffre d’affaires et les commandes en un coup d’œil.',
        },
        users: {
          title: 'Utilisateurs',
          text: 'Créez et modifiez les comptes, et désactivez l’authentification à deux facteurs d’un utilisateur.',
        },
        products: {
          title: 'Produits',
          text: 'Créez et modifiez les produits, avec leurs traductions et leurs images.',
        },
        orders: {
          title: 'Commandes',
          text: 'Suivez toutes les commandes des clients et mettez à jour leur statut.',
        },
        productPromotions: {
          title: 'Promotions par produit',
          text: 'Mettez un produit en promotion avec un pourcentage de remise entre deux dates : l’ancien prix est affiché barré.',
        },
        discountCodes: {
          title: 'Codes de réduction',
          text: 'Créez des codes promotionnels pour vos clients.',
        },
        seller: {
          title: 'Informations vendeur',
          text: 'Renseignez l’identité de la boutique affichée sur les factures.',
        },
        legalPages: {
          title: 'Pages légales',
          text: 'Modifiez les mentions légales, les conditions de vente et la politique de confidentialité.',
        },
        settings: { title: 'Paramètres', text: 'Ajustez les paramètres de la boutique.' },
        environment: {
          title: 'Environnement',
          text: "Vérifiez la configuration du backend : les variables d'environnement définies avec leur valeur, sauf les secrets, indiqués seulement comme définis ou non.",
        },
        sellerSetup: {
          title: "Assistant d'initialisation de la boutique",
          text: "Tant que la boutique n'est pas initialisée, un assistant pas à pas guide l'administrateur dans la saisie des informations du vendeur pour la préparer.",
        },
      },
      automatic: {
        title: 'Tâches automatiques',
        subtitle: 'Des tâches planifiées qui se déclenchent seules, sans rien à faire.',
        unpaidOrders: {
          title: 'Commandes impayées libérées',
          text: 'Une commande non payée dans le délai de paiement des paramètres est annulée automatiquement, pour que ses unités réservées soient de nouveau en vente.',
        },
        abandonedCart: {
          title: 'Rappel de panier abandonné',
          text: "Un client qui a laissé des articles dans son panier reçoit un email qui les liste et ramène vers le panier, si l'administrateur l'a activé.",
        },
      },
      assistants: {
        title: 'Assistants IA',
        subtitle: 'La boutique peut être consultée par les assistants IA que vous utilisez déjà.',
        mcp: {
          title: 'Serveur MCP des produits',
          text: "Un assistant IA comme Claude Desktop ou Claude Code peut lister les produits et lire l'un d'eux, en français ou en anglais, grâce au Model Context Protocol sur /mcp. Le serveur ne donne que le catalogue et peut être désactivé avec FLEXSTORE_MCP_ENABLED.",
        },
      },
    },
    stackTitle: 'Technologies',
    stack: {
      backend: 'Kotlin et Spring Boot',
      database: 'PostgreSQL',
      frontend: 'Vue 3 et Vuetify',
    },
    sourceTitle: 'Code source',
    sourceText: 'Le code est disponible sur GitHub.',
    sourceButton: 'Voir sur GitHub',
  },
};
