PRAGMA foreign_keys = ON;

-- Drop tables in dependency order
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

-- User account
CREATE TABLE user_account (
    user_id             INTEGER PRIMARY KEY AUTOINCREMENT,
    email               TEXT NOT NULL UNIQUE,
    password            TEXT NOT NULL,
    first_name          TEXT NOT NULL,
    last_name           TEXT,
    active              INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1)),
    signup_datetime     TEXT NOT NULL DEFAULT (datetime('now')),
    profile_image_name  TEXT
);

-- Family member
CREATE TABLE family_member (
    member_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id     INTEGER NOT NULL,
    name        TEXT NOT NULL,
    birth_date  TEXT,
    height      REAL,
    weight      REAL,
    allergy     TEXT,
    FOREIGN KEY (user_id) REFERENCES user_account(user_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- Budget
CREATE TABLE budget (
    budget_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id       INTEGER NOT NULL,
    name          TEXT NOT NULL,
    amount        NUMERIC NOT NULL CHECK (amount >= 0),
    period_start  TEXT NOT NULL,
    period_end    TEXT NOT NULL,
    status        TEXT NOT NULL DEFAULT 'active',
    FOREIGN KEY (user_id) REFERENCES user_account(user_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CHECK (period_end >= period_start)
);

-- Parameter planner
CREATE TABLE parameter_planner (
    parameter_id           INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id                INTEGER NOT NULL,
    budget_id              INTEGER NOT NULL,
    shopping_period_start  TEXT NOT NULL,
    shopping_period_end    TEXT NOT NULL,
    meals_per_day          INTEGER NOT NULL CHECK (meals_per_day > 0),
    snack_per_day          INTEGER NOT NULL DEFAULT 0 CHECK (snack_per_day >= 0),
    FOREIGN KEY (user_id) REFERENCES user_account(user_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (budget_id) REFERENCES budget(budget_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CHECK (shopping_period_end >= shopping_period_start)
);

-- Weekly menu
CREATE TABLE weekly_menu (
    menu_id           INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id           INTEGER NOT NULL,
    parameter_id      INTEGER NOT NULL,
    week_start_date   TEXT NOT NULL,
    week_end_date     TEXT NOT NULL,
    total_estimation  NUMERIC NOT NULL DEFAULT 0 CHECK (total_estimation >= 0),
    status_budget     TEXT NOT NULL DEFAULT 'draft',
    created_datetime  TEXT NOT NULL DEFAULT (datetime('now')),
    FOREIGN KEY (user_id) REFERENCES user_account(user_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (parameter_id) REFERENCES parameter_planner(parameter_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CHECK (week_end_date >= week_start_date)
);

-- Ingredient
CREATE TABLE ingredient (
    ingredient_id  INTEGER PRIMARY KEY AUTOINCREMENT,
    name           TEXT NOT NULL UNIQUE,
    unit           TEXT NOT NULL
);

-- Ingredient nutrition
CREATE TABLE ingredient_nutrition (
    nutrition_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    ingredient_id  INTEGER NOT NULL UNIQUE,
    calories       NUMERIC NOT NULL DEFAULT 0 CHECK (calories >= 0),
    protein        NUMERIC NOT NULL DEFAULT 0 CHECK (protein >= 0),
    carbohydrate   NUMERIC NOT NULL DEFAULT 0 CHECK (carbohydrate >= 0),
    fat            NUMERIC NOT NULL DEFAULT 0 CHECK (fat >= 0),
    fibre          NUMERIC NOT NULL DEFAULT 0 CHECK (fibre >= 0),
    unit           TEXT NOT NULL,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- Ingredient price history
CREATE TABLE ingredient_price (
    price_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    ingredient_id   INTEGER NOT NULL,
    price           NUMERIC NOT NULL CHECK (price >= 0),
    effective_date  TEXT NOT NULL,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- Kitchen stock
CREATE TABLE kitchen_stock (
    stock_id          INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id           INTEGER NOT NULL,
    ingredient_id     INTEGER NOT NULL,
    quantity          REAL NOT NULL DEFAULT 0 CHECK (quantity >= 0),
    unit              TEXT NOT NULL,
    storage_location  TEXT,
    expiry_date       TEXT,
    FOREIGN KEY (user_id) REFERENCES user_account(user_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- Recipe
CREATE TABLE recipe (
    recipe_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT NOT NULL UNIQUE,
    description   TEXT,
    serving_size  INTEGER NOT NULL DEFAULT 1 CHECK (serving_size > 0),
    status        TEXT NOT NULL DEFAULT 'active'
);

-- Recipe ingredient
CREATE TABLE recipe_ingredient (
    recipe_ingredient_id  INTEGER PRIMARY KEY AUTOINCREMENT,
    recipe_id             INTEGER NOT NULL,
    ingredient_id         INTEGER NOT NULL,
    amount                REAL NOT NULL CHECK (amount > 0),
    unit                  TEXT NOT NULL,
    FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    UNIQUE (recipe_id, ingredient_id)
);

-- Meal slot
CREATE TABLE meal_slot (
    slot_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    menu_id        INTEGER NOT NULL,
    recipe_id      INTEGER NOT NULL,
    meal_date      TEXT NOT NULL,
    meal_time      TEXT NOT NULL,
    is_eating_out  INTEGER NOT NULL DEFAULT 0 CHECK (is_eating_out IN (0, 1)),
    outside_cost   NUMERIC NOT NULL DEFAULT 0 CHECK (outside_cost >= 0),
    FOREIGN KEY (menu_id) REFERENCES weekly_menu(menu_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (recipe_id) REFERENCES recipe(recipe_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT
);

-- Shopping planner
CREATE TABLE shopping_planner (
    planner_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    menu_id           INTEGER NOT NULL,
    created_datetime  TEXT NOT NULL DEFAULT (datetime('now')),
    total_estimation  NUMERIC NOT NULL DEFAULT 0 CHECK (total_estimation >= 0),
    total_actual      NUMERIC NOT NULL DEFAULT 0 CHECK (total_actual >= 0),
    status            TEXT NOT NULL DEFAULT 'draft',
    FOREIGN KEY (menu_id) REFERENCES weekly_menu(menu_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- Shopping planner item
CREATE TABLE shopping_planner_item (
    item_id          INTEGER PRIMARY KEY AUTOINCREMENT,
    planner_id       INTEGER NOT NULL,
    ingredient_id    INTEGER NOT NULL,
    required_qty     REAL NOT NULL CHECK (required_qty > 0),
    unit             TEXT NOT NULL,
    estimated_price  NUMERIC NOT NULL DEFAULT 0 CHECK (estimated_price >= 0),
    actual_price     NUMERIC NOT NULL DEFAULT 0 CHECK (actual_price >= 0),
    status_beli      TEXT NOT NULL DEFAULT 'belum',
    FOREIGN KEY (planner_id) REFERENCES shopping_planner(planner_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredient(ingredient_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    UNIQUE (planner_id, ingredient_id)
);

CREATE INDEX idx_family_member_user_id ON family_member(user_id);
CREATE INDEX idx_budget_user_id ON budget(user_id);
CREATE INDEX idx_parameter_user_id ON parameter_planner(user_id);
CREATE INDEX idx_parameter_budget_id ON parameter_planner(budget_id);
CREATE INDEX idx_weekly_menu_user_id ON weekly_menu(user_id);
CREATE INDEX idx_weekly_menu_parameter_id ON weekly_menu(parameter_id);
CREATE INDEX idx_kitchen_stock_user_id ON kitchen_stock(user_id);
CREATE INDEX idx_kitchen_stock_ingredient_id ON kitchen_stock(ingredient_id);
CREATE INDEX idx_ingredient_price_ingredient_id ON ingredient_price(ingredient_id);
CREATE INDEX idx_recipe_ingredient_recipe_id ON recipe_ingredient(recipe_id);
CREATE INDEX idx_recipe_ingredient_ingredient_id ON recipe_ingredient(ingredient_id);
CREATE INDEX idx_meal_slot_menu_id ON meal_slot(menu_id);
CREATE INDEX idx_meal_slot_recipe_id ON meal_slot(recipe_id);
CREATE INDEX idx_shopping_planner_menu_id ON shopping_planner(menu_id);
CREATE INDEX idx_shopping_item_planner_id ON shopping_planner_item(planner_id);
CREATE INDEX idx_shopping_item_ingredient_id ON shopping_planner_item(ingredient_id);
