CREATE TABLE IF NOT EXISTS food_category (
    id          BIGINT       PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(255),                       
    CONSTRAINT uq_food_category_name UNIQUE (name)  
);

CREATE TABLE IF NOT EXISTS fruit_product (
    id                BIGINT        PRIMARY KEY,
    name              VARCHAR(100)  NOT NULL,
    weight_grams      INT           NOT NULL,
    calories_per_100g NUMERIC(6, 2) NOT NULL,
    protein_per_100g  NUMERIC(5, 2) NOT NULL,
    meal_type         VARCHAR(20)   NOT NULL,
    sugar_grams       NUMERIC(5, 2) NOT NULL,
    fresh             BOOLEAN       NOT NULL DEFAULT TRUE,
    category_id       BIGINT,                
    CONSTRAINT ck_fruit_weight   CHECK (weight_grams BETWEEN 1 AND 5000),
    CONSTRAINT ck_fruit_calories CHECK (calories_per_100g BETWEEN 0 AND 900),
    CONSTRAINT ck_fruit_protein  CHECK (protein_per_100g BETWEEN 0 AND 100),
    CONSTRAINT ck_fruit_sugar    CHECK (sugar_grams >= 0),
    CONSTRAINT ck_fruit_meal     CHECK (meal_type IN ('сніданок', 'обід', 'вечеря', 'перекус')),
    CONSTRAINT fk_fruit_category FOREIGN KEY (category_id)
        REFERENCES food_category (id) ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_fruit_category ON fruit_product (category_id);
CREATE INDEX IF NOT EXISTS idx_fruit_name     ON fruit_product (name);
