-- A standard legal notice and standard terms of sale, as a starting set of data: a new version of each page,
-- only while its current version is still empty (a text the administrators wrote is never replaced). The seller
-- is the current version of the seller; what the shop does not know yet (its host, its mediator) is left to
-- complete. The long paragraphs go on over indented lines, joined again into one line when they are inserted
WITH current_seller AS (
    SELECT name, address, siret, vat_number,
           concat_ws(' - ', 'Téléphone : ' || phone, 'Email : ' || email) AS reach
    FROM seller
    ORDER BY version DESC
    LIMIT 1
),
standard_page AS (
    SELECT 'LEGAL_NOTICE' AS kind,
           regexp_replace($$MENTIONS LÉGALES

1. Éditeur du site
Le site est édité par $$ || s.name || $$.
Siège social : $$ || s.address || $$
SIRET : $$ || s.siret || $$
TVA intracommunautaire : $$ || s.vat_number || coalesce(E'\n' || nullif(s.reach, ''), '') || $$
Directeur de la publication : le représentant légal de $$ || s.name || $$.

2. Hébergement
Le site est hébergé par : [nom, adresse et téléphone de l'hébergeur, à compléter].

3. Propriété intellectuelle
Les textes, images, logos et marques présents sur le site sont la propriété de $$ || s.name || $$ ou de
    ses partenaires. Toute reproduction, représentation ou adaptation, totale ou partielle, sans autorisation
    écrite préalable est interdite.

4. Données personnelles
Les données personnelles collectées (nom, adresse email, adresses de livraison et de facturation,
    commandes) sont nécessaires à la création du compte client, au traitement des commandes, à leur
    facturation et au service client. Elles sont conservées pendant la durée de la relation commerciale, puis
    pendant les durées imposées par la loi (notamment dix ans pour les pièces comptables). Elles ne sont
    transmises qu'aux prestataires nécessaires à l'exécution des commandes (paiement, livraison).
Conformément au Règlement général sur la protection des données (RGPD) et à la loi Informatique et
    Libertés, le client dispose d'un droit d'accès, de rectification, d'effacement, de limitation,
    d'opposition et de portabilité de ses données, qu'il exerce en écrivant à $$ || s.name || $$, à l'adresse
    du siège social. Il peut introduire une réclamation auprès de la CNIL (www.cnil.fr).

5. Cookies et stockage local
Le site utilise uniquement le stockage nécessaire à son fonctionnement (connexion au compte, panier,
    langue). Aucun traceur publicitaire n'est déposé.

6. Droit applicable
Les présentes mentions légales sont régies par le droit français.$$, E'\n +', ' ', 'g') AS text
    FROM current_seller s
    UNION ALL
    SELECT 'TERMS_OF_SALE',
           regexp_replace($$CONDITIONS GÉNÉRALES DE VENTE

Article 1 - Objet et champ d'application
Les présentes conditions générales de vente (CGV) régissent les ventes de produits conclues sur le site
    entre $$ || s.name || $$ (le « Vendeur »), dont le siège social est situé $$ || s.address || $$ (SIRET $$
    || s.siret || $$), et toute personne passant commande (le « Client »). Le Client les accepte en cochant
    la case prévue à cet effet avant de commander. La version acceptée est conservée avec la commande.

Article 2 - Produits
Les produits proposés sont ceux décrits sur le site, dans la limite des stocks disponibles. Les
    photographies et descriptions sont aussi fidèles que possible mais n'engagent pas le Vendeur sur des
    différences mineures.

Article 3 - Prix
Les prix sont indiqués en euros, toutes taxes comprises (TVA française applicable au jour de la
    commande). Les frais de livraison sont indiqués avant la validation de la commande ; la livraison
    standard est offerte à partir du montant d'achat affiché sur le site. Le prix facturé est celui affiché
    lors de la validation de la commande.

Article 4 - Commande
Le Client sélectionne les produits dans son panier, indique ses adresses de livraison et de facturation,
    choisit le mode de livraison, saisit le cas échéant un code promo, puis accepte les présentes CGV. La
    commande est confirmée par email une fois payée. Le Vendeur se réserve le droit de refuser une commande
    en cas de litige avec le Client ou de stock insuffisant.

Article 5 - Paiement
Le paiement s'effectue en ligne par carte bancaire, au moyen d'un prestataire de paiement sécurisé ; le
    Vendeur n'a jamais connaissance des données bancaires du Client. Les produits sont réservés pendant le
    délai de paiement indiqué lors de la commande : une commande qui n'est pas payée dans ce délai est
    annulée.

Article 6 - Livraison
Les produits sont livrés à l'adresse de livraison indiquée par le Client, selon le mode choisi (standard
    ou express). Les délais indiqués sur le site sont donnés à titre indicatif. En cas de retard de plus de
    trente jours, le Client peut résoudre la vente dans les conditions des articles L.216-2 et suivants du
    Code de la consommation. Le Client vérifie l'état des produits à la réception et signale toute anomalie
    au transporteur et au Vendeur.

Article 7 - Droit de rétractation
Conformément aux articles L.221-18 et suivants du Code de la consommation, le Client consommateur dispose
    d'un délai de quatorze jours à compter de la réception des produits pour se rétracter, sans avoir à se
    justifier. Il en informe le Vendeur par une déclaration dénuée d'ambiguïté, puis retourne les produits, à
    ses frais, dans les quatorze jours suivant cette information. Le Vendeur rembourse la totalité des sommes
    versées, frais de livraison standard compris, dans les quatorze jours suivant la rétractation ; il peut
    différer le remboursement jusqu'à la réception des produits ou la preuve de leur expédition.

Article 8 - Annulation par le Vendeur
Tant qu'une commande payée n'est pas expédiée, le Vendeur peut l'annuler, notamment en cas
    d'indisponibilité d'un produit ; le Client est alors intégralement remboursé par le moyen de paiement
    utilisé et informé par email.

Article 9 - Garanties légales
Les produits bénéficient de la garantie légale de conformité (articles L.217-3 et suivants du Code de la
    consommation), pendant deux ans à compter de la délivrance du bien, et de la garantie des vices cachés
    (articles 1641 et suivants du Code civil). Le Client met en œuvre ces garanties en contactant le Vendeur.

Article 10 - Facturation
La facture de chaque commande payée est disponible dans l'espace client. Lorsqu'une commande est
    remboursée, un avoir annulant sa facture est émis.

Article 11 - Données personnelles
Les données personnelles du Client sont traitées comme indiqué dans les mentions légales du site.

Article 12 - Réclamations et médiation
Toute réclamation est adressée au Vendeur, à l'adresse de son siège social$$ || coalesce(' (' ||
    nullif(s.reach, '') || ')', '') || $$. À défaut de solution amiable, le Client consommateur peut recourir
    gratuitement au médiateur de la consommation : [nom et coordonnées du médiateur, à compléter].

Article 13 - Droit applicable
Les présentes CGV sont soumises au droit français. Tout litige relève des juridictions françaises
    compétentes, sous réserve des dispositions protectrices du consommateur.$$, E'\n +', ' ', 'g')
    FROM current_seller s
)
INSERT INTO legal_page (id, kind, version, text, published_at)
SELECT gen_random_uuid()::text, page.kind, current_page.version + 1, page.text, now()
FROM standard_page page
JOIN LATERAL (
    SELECT version, text FROM legal_page WHERE kind = page.kind ORDER BY version DESC LIMIT 1
) AS current_page ON true
WHERE current_page.text = '';
