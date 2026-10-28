-- The name and the description of a product in another language than the one of the catalog: the product keeps
-- its own name and description, used when it has no translation in the language of the customer. A product
-- has at most one translation per language
CREATE TABLE product_translation (
    id VARCHAR(50) PRIMARY KEY,
    product_id VARCHAR(50) NOT NULL REFERENCES product(id) ON DELETE CASCADE,
    language VARCHAR(2) NOT NULL CHECK (language IN ('en', 'fr')),
    name TEXT NOT NULL,
    description TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    UNIQUE (product_id, language)
);

-- The products of the catalog, written in English, translated into French (when they still exist)
INSERT INTO product_translation (id, product_id, language, name, description)
SELECT gen_random_uuid()::text, product.id, 'fr', french.name, french.description
FROM product
JOIN (VALUES
    ('1', 'T-shirt en coton bio', 'Confortable et écologique'),
    ('2', 'Veste imperméable', 'Résiste à la pluie et au vent'),
    ('3', 'Jean slim', 'Coupe moderne, stretch'),
    ('4', 'Smartphone 5G', 'Écran OLED, 128 Go'),
    ('5', 'Tablette 10"', 'Idéale pour le multimédia'),
    ('6', 'Ordinateur portable', 'SSD 512 Go, 16 Go de RAM'),
    ('7', 'Montre connectée', 'Suivi d''activité et notifications'),
    ('8', 'Casque Bluetooth', 'Son de haute qualité, 20 h d''autonomie'),
    ('9', 'Sac à dos urbain', 'Compartiment pour ordinateur, design moderne')
) AS french (product_id, name, description) ON french.product_id = product.id;
