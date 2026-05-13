
PRAGMA foreign_keys = OFF;

DROP TABLE IF EXISTS shopping_planner_item;
DROP TABLE IF EXISTS shopping_planner;
DROP TABLE IF EXISTS meal_slot;
DROP TABLE IF EXISTS weekly_menu;
DROP TABLE IF EXISTS parameter_planner;
DROP TABLE IF EXISTS recipe_ingredient;
DROP TABLE IF EXISTS ingredient_price;
DROP TABLE IF EXISTS ingredient_nutrition;
DROP TABLE IF EXISTS kitchen_stock;
DROP TABLE IF EXISTS recipe;
DROP TABLE IF EXISTS ingredient;
DROP TABLE IF EXISTS family_member;
DROP TABLE IF EXISTS budget;
DROP TABLE IF EXISTS user_account;

PRAGMA foreign_keys = ON;

-- ------------------------------------------------------------
-- User account
-- ------------------------------------------------------------
CREATE TABLE user_account (
    user_id             INTEGER         NOT NULL PRIMARY KEY AUTOINCREMENT,
    email               VARCHAR(255)    NOT NULL UNIQUE,
    password            VARCHAR(255)    NOT NULL,
    first_name          VARCHAR(100)    NOT NULL,
    last_name           VARCHAR(100),
    active              INTEGER         NOT NULL DEFAULT 1,
    signup_datetime     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    profile_image_name  VARCHAR(255),
    tipe_admin          INTEGER         NOT NULL DEFAULT 0,
    CHECK (active IN (0, 1)),
    CHECK (tipe_admin IN (0, 1))
);

-- ------------------------------------------------------------
-- Family member
-- ------------------------------------------------------------
CREATE TABLE family_member (
    member_id    INTEGER         NOT NULL PRIMARY KEY AUTOINCREMENT,
    user_id      INTEGER         NOT NULL,
    name         VARCHAR(100)    NOT NULL,
    relationship VARCHAR(50),
    birth_date   DATE,
    height       DOUBLE,
    weight       DOUBLE,
    allergy      TEXT,
    FOREIGN KEY (user_id) REFERENCES user_account(user_id)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- Budget
-- ------------------------------------------------------------
CREATE TABLE budget (
    budget_id     INTEGER         NOT NULL PRIMARY KEY AUTOINCREMENT,
    user_id       INTEGER         NOT NULL,
    name          VARCHAR(100)    NOT NULL,
    amount        DECIMAL(15,2)   NOT NULL,
    period_start  DATE            NOT NULL,
    period_end    DATE            NOT NULL,
    status        VARCHAR(50)     NOT NULL DEFAULT 'active',
    FOREIGN KEY (user_id) REFERENCES user_account(user_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CHECK (amount >= 0),
    CHECK (period_end >= period_start)
);

-- ------------------------------------------------------------
-- Parameter planner
-- ------------------------------------------------------------
CREATE TABLE parameter_planner (
    parameter_id           INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
    user_id                INTEGER NOT NULL,
    budget_id              INTEGER NOT NULL,
    shopping_period_start  DATE    NOT NULL,
    shopping_period_end    DATE    NOT NULL,
    meals_per_day          INTEGER NOT NULL,
    snack_per_day          INTEGER NOT NULL DEFAULT 0,
    FOREIGN KEY (user_id)   REFERENCES user_account(user_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (budget_id) REFERENCES budget(budget_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CHECK (meals_per_day > 0),
    CHECK (snack_per_day >= 0),
    CHECK (shopping_period_end >= shopping_period_start)
);

-- ------------------------------------------------------------
-- Weekly menu
-- ------------------------------------------------------------
CREATE TABLE weekly_menu (
    menu_id           INTEGER         NOT NULL PRIMARY KEY AUTOINCREMENT,
    user_id           INTEGER         NOT NULL,
    parameter_id      INTEGER         NOT NULL,
    week_start_date   DATE            NOT NULL,
    week_end_date     DATE            NOT NULL,
    total_estimation  DECIMAL(15,2)   NOT NULL DEFAULT 0,
    status_budget     VARCHAR(50)     NOT NULL DEFAULT 'draft',
    created_datetime  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id)       REFERENCES user_account(user_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (parameter_id)  REFERENCES parameter_planner(parameter_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CHECK (total_estimation >= 0),
    CHECK (week_end_date >= week_start_date)
);

-- ------------------------------------------------------------
-- Ingredient
-- ------------------------------------------------------------
CREATE TABLE ingredient (
    ingredient_id  INTEGER         NOT NULL PRIMARY KEY AUTOINCREMENT,
    name           VARCHAR(100)    NOT NULL UNIQUE,
    unit           VARCHAR(50)     NOT NULL
);

-- ------------------------------------------------------------
-- Ingredient nutrition
-- ------------------------------------------------------------
CREATE TABLE ingredient_nutrition (
    nutrition_id   INTEGER         NOT NULL PRIMARY KEY AUTOINCREMENT,
    ingredient_id  INTEGER         NOT NULL UNIQUE,
    calories       DECIMAL(10,2)   NOT NULL DEFAULT 0,
    protein        DECIMAL(10,2)   NOT NULL DEFAULT 0,
    carbohydrate   DECIMAL(10,2)   NOT NULL DEFAULT 0,
    fat            DECIMAL(10,2)   NOT NULL DEFAULT 0,
    fibre          DECIMAL(10,2)   NOT NULL DEFAULT 0,
    unit           VARCHAR(50)     NOT NULL,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CHECK (calories >= 0),
    CHECK (protein >= 0),
    CHECK (carbohydrate >= 0),
    CHECK (fat >= 0),
    CHECK (fibre >= 0)
);

-- ------------------------------------------------------------
-- Ingredient price history
-- ------------------------------------------------------------
CREATE TABLE ingredient_price (
    price_id        INTEGER         NOT NULL PRIMARY KEY AUTOINCREMENT,
    ingredient_id   INTEGER         NOT NULL,
    price           DECIMAL(15,2)   NOT NULL,
    effective_date  DATE            NOT NULL,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CHECK (price >= 0)
);

-- ------------------------------------------------------------
-- Kitchen stock
-- ------------------------------------------------------------
CREATE TABLE kitchen_stock (
    stock_id          INTEGER         NOT NULL PRIMARY KEY AUTOINCREMENT,
    user_id           INTEGER         NOT NULL,
    ingredient_id     INTEGER         NOT NULL,
    quantity          DOUBLE          NOT NULL DEFAULT 0,
    unit              VARCHAR(50)     NOT NULL,
    storage_location  VARCHAR(100),
    expiry_date       DATE,
    FOREIGN KEY (user_id)       REFERENCES user_account(user_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CHECK (quantity >= 0)
);

-- ------------------------------------------------------------
-- Recipe
-- ------------------------------------------------------------
CREATE TABLE recipe (
    recipe_id     INTEGER         NOT NULL PRIMARY KEY AUTOINCREMENT,
    name          VARCHAR(200)    NOT NULL UNIQUE,
    description   TEXT,
    serving_size  INTEGER         NOT NULL DEFAULT 1,
    status        VARCHAR(50)     NOT NULL DEFAULT 'active',
    CHECK (serving_size > 0)
);

-- ------------------------------------------------------------
-- Recipe ingredient
-- ------------------------------------------------------------
CREATE TABLE recipe_ingredient (
    recipe_ingredient_id  INTEGER         NOT NULL PRIMARY KEY AUTOINCREMENT,
    recipe_id             INTEGER         NOT NULL,
    ingredient_id         INTEGER         NOT NULL,
    amount                DOUBLE          NOT NULL,
    unit                  VARCHAR(50)     NOT NULL,
    UNIQUE (recipe_id, ingredient_id),
    FOREIGN KEY (recipe_id)     REFERENCES recipe(recipe_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CHECK (amount > 0)
);

-- ------------------------------------------------------------
-- Meal slot
-- ------------------------------------------------------------
CREATE TABLE meal_slot (
    slot_id        INTEGER         NOT NULL PRIMARY KEY AUTOINCREMENT,
    menu_id        INTEGER         NOT NULL,
    recipe_id      INTEGER         NULL,
    meal_date      DATE            NOT NULL,
    meal_time      VARCHAR(50)     NOT NULL,
    is_eating_out  INTEGER         NOT NULL DEFAULT 0,
    outside_cost   DECIMAL(15,2)   NOT NULL DEFAULT 0,
    FOREIGN KEY (menu_id)   REFERENCES weekly_menu(menu_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id),
    CHECK (is_eating_out IN (0, 1)),
    CHECK (outside_cost >= 0)
);

-- ------------------------------------------------------------
-- Shopping planner
-- ------------------------------------------------------------
CREATE TABLE shopping_planner (
    planner_id        INTEGER         NOT NULL PRIMARY KEY AUTOINCREMENT,
    menu_id           INTEGER         NOT NULL,
    created_datetime  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_estimation  DECIMAL(15,2)   NOT NULL DEFAULT 0,
    total_actual      DECIMAL(15,2)   NOT NULL DEFAULT 0,
    status            VARCHAR(50)     NOT NULL DEFAULT 'draft',
    FOREIGN KEY (menu_id) REFERENCES weekly_menu(menu_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CHECK (total_estimation >= 0),
    CHECK (total_actual >= 0)
);

-- ------------------------------------------------------------
-- Shopping planner item
-- ------------------------------------------------------------
CREATE TABLE shopping_planner_item (
    item_id          INTEGER         NOT NULL PRIMARY KEY AUTOINCREMENT,
    planner_id       INTEGER         NOT NULL,
    ingredient_id    INTEGER         NOT NULL,
    required_qty     DOUBLE          NOT NULL,
    unit             VARCHAR(50)     NOT NULL,
    estimated_price  DECIMAL(15,2)   NOT NULL DEFAULT 0,
    actual_price     DECIMAL(15,2)   NOT NULL DEFAULT 0,
    status_beli      VARCHAR(50)     NOT NULL DEFAULT 'belum',
    UNIQUE (planner_id, ingredient_id),
    FOREIGN KEY (planner_id)    REFERENCES shopping_planner(planner_id)
        ON UPDATE CASCADE ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CHECK (required_qty > 0),
    CHECK (estimated_price >= 0),
    CHECK (actual_price >= 0)
);

-- ------------------------------------------------------------
-- Indexes
-- ------------------------------------------------------------
CREATE INDEX idx_family_member_user_id           ON family_member(user_id);
CREATE INDEX idx_budget_user_id                  ON budget(user_id);
CREATE INDEX idx_parameter_user_id               ON parameter_planner(user_id);
CREATE INDEX idx_parameter_budget_id             ON parameter_planner(budget_id);
CREATE INDEX idx_weekly_menu_user_id             ON weekly_menu(user_id);
CREATE INDEX idx_weekly_menu_parameter_id        ON weekly_menu(parameter_id);
CREATE INDEX idx_kitchen_stock_user_id           ON kitchen_stock(user_id);
CREATE INDEX idx_kitchen_stock_ingredient_id     ON kitchen_stock(ingredient_id);
CREATE INDEX idx_ingredient_price_ingredient_id  ON ingredient_price(ingredient_id);
CREATE INDEX idx_recipe_ingredient_recipe_id     ON recipe_ingredient(recipe_id);
CREATE INDEX idx_recipe_ingredient_ingredient_id ON recipe_ingredient(ingredient_id);
CREATE INDEX idx_meal_slot_menu_id               ON meal_slot(menu_id);
CREATE INDEX idx_meal_slot_recipe_id             ON meal_slot(recipe_id);
CREATE INDEX idx_shopping_planner_menu_id        ON shopping_planner(menu_id);
CREATE INDEX idx_shopping_item_planner_id        ON shopping_planner_item(planner_id);
CREATE INDEX idx_shopping_item_ingredient_id     ON shopping_planner_item(ingredient_id);
