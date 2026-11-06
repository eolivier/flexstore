-- The privacy policy of the shop, a third legal page beside its legal notice and its terms of sale: written by
-- the administrators, read by everyone, with a version added by each publication
ALTER TABLE legal_page DROP CONSTRAINT legal_page_kind_check;
ALTER TABLE legal_page ADD CONSTRAINT legal_page_kind_check
    CHECK (kind IN ('LEGAL_NOTICE', 'TERMS_OF_SALE', 'PRIVACY_POLICY'));

-- The first version: empty, until the administrators write it
INSERT INTO legal_page (id, kind, version, text, published_at)
VALUES (gen_random_uuid()::text, 'PRIVACY_POLICY', 1, '', now());

-- A standard privacy policy, as a starting set of data (as V38): a new version, only while the current one is
-- still empty. The controller is the current version of the seller; what the shop does not know yet (its host,
-- its data protection contact) is left to complete. The long paragraphs go on over indented lines, joined again
-- into one line when they are inserted
WITH current_seller AS (
    SELECT name, address, siret,
           concat_ws(' - ', 'Téléphone : ' || phone, 'Email : ' || email) AS reach
    FROM seller
    ORDER BY version DESC
    LIMIT 1
)
INSERT INTO legal_page (id, kind, version, text, published_at)
SELECT gen_random_uuid()::text, 'PRIVACY_POLICY', current_page.version + 1,
       regexp_replace($$POLITIQUE DE CONFIDENTIALITÉ

1. Responsable du traitement
Les données personnelles collectées sur le site sont traitées par $$ || s.name || $$, dont le siège social
    est situé $$ || s.address || $$ (SIRET $$ || s.siret || $$)$$
    || coalesce(E'\n' || nullif(s.reach, ''), '') || $$.
Contact pour toute question relative aux données personnelles : [adresse email du contact, à compléter].

2. Données collectées
- Compte client : nom, prénom, adresse email, mot de passe (conservé sous forme chiffrée), type de client
    (particulier ou société) et, pour une société, sa raison sociale, son SIREN et son numéro de TVA.
- Adresses : adresses de livraison et de facturation, nom du destinataire, numéro de téléphone.
- Commandes : produits commandés, montants, mode de livraison, code promo utilisé, conditions générales de
    vente acceptées, factures et avoirs.
- Paiement : le paiement est réalisé par un prestataire sécurisé ; nous ne conservons aucune donnée de carte
    bancaire.
- Navigation : panier, langue et préférences d'affichage (mode clair ou sombre).

3. Finalités et bases légales
- Création et gestion du compte client, traitement, livraison et facturation des commandes, service client :
    exécution du contrat.
- Conservation des pièces comptables et respect des obligations légales : obligation légale.
- Emails liés à la commande (confirmation, suivi) et rappel d'un panier abandonné lorsque cette fonction est
    activée : intérêt légitime de la boutique, ou exécution du contrat pour les emails de commande.
- Mémorisation des préférences d'affichage : consentement, que vous pouvez modifier dans votre compte.

4. Destinataires
Les données ne sont jamais vendues. Elles ne sont transmises qu'aux prestataires nécessaires au service :
    paiement, livraison, envoi des emails et hébergement du site ([nom de l'hébergeur, à compléter]), qui les
    traitent sur nos instructions. Les données ne sont pas transférées hors de l'Union européenne, sauf
    garanties appropriées prévues par le RGPD.

5. Durées de conservation
- Compte client : pendant la durée de la relation commerciale, puis supprimé ou anonymisé à votre demande, ou
    après trois ans sans activité.
- Commandes, factures et avoirs : dix ans, durée imposée par le Code de commerce.
- Panier : tant que le compte existe, ou trente jours pour un panier de visiteur.

6. Vos droits
Conformément au Règlement général sur la protection des données (RGPD) et à la loi Informatique et Libertés,
    vous disposez d'un droit d'accès, de rectification, d'effacement, de limitation, d'opposition et de
    portabilité de vos données, ainsi que du droit de définir des directives sur leur sort après votre décès.
    Pour les exercer, écrivez au contact indiqué à l'article 1 : une réponse vous est apportée dans un délai
    d'un mois. Si vous estimez que vos droits ne sont pas respectés, vous pouvez introduire une réclamation
    auprès de la CNIL (www.cnil.fr).

7. Cookies et stockage local
Le site utilise uniquement le stockage nécessaire à son fonctionnement (connexion au compte, panier, langue),
    et, avec votre accord, vos préférences d'affichage. Aucun traceur publicitaire ni outil de mesure
    d'audience n'est utilisé.

8. Sécurité
Les échanges avec le site sont chiffrés (HTTPS), les mots de passe sont chiffrés et l'accès aux données est
    réservé aux personnes qui en ont besoin. En cas de violation de données susceptible d'engendrer un risque
    pour vos droits, vous en êtes informé dans les conditions prévues par la loi.

9. Modification de la politique
Cette politique peut évoluer : la date de la dernière mise à jour est indiquée en haut de la page.$$,
           E'\n +', ' ', 'g'),
       now()
FROM current_seller s
JOIN LATERAL (
    SELECT version, text FROM legal_page WHERE kind = 'PRIVACY_POLICY' ORDER BY version DESC LIMIT 1
) AS current_page ON true
WHERE current_page.text = '';
