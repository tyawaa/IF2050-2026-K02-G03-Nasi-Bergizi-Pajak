
SET FOREIGN_KEY_CHECKS = 0;

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

SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------------------------
-- User account
-- ------------------------------------------------------------
CREATE TABLE user_account (
    user_id             INT             NOT NULL AUTO_INCREMENT,
    email               VARCHAR(255)    NOT NULL,
    password            VARCHAR(255)    NOT NULL,
    first_name          VARCHAR(100)    NOT NULL,
    last_name           VARCHAR(100),
    active              TINYINT(1)      NOT NULL DEFAULT 1,
    signup_datetime     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    profile_image_name  VARCHAR(255),
    tipe_admin          TINYINT(1)      NOT NULL DEFAULT 0,
    PRIMARY KEY (user_id),
    UNIQUE KEY uq_user_email (email),
    CONSTRAINT chk_user_active      CHECK (active IN (0, 1)),
    CONSTRAINT chk_user_tipe_admin  CHECK (tipe_admin IN (0, 1))
);

-- ------------------------------------------------------------
-- Family member
-- ------------------------------------------------------------
CREATE TABLE family_member (
    member_id   INT             NOT NULL AUTO_INCREMENT,
    user_id     INT             NOT NULL,
    name        VARCHAR(100)    NOT NULL,
    birth_date  DATE,
    height      DOUBLE,
    weight      DOUBLE,
    allergy     TEXT,
    PRIMARY KEY (member_id),
    CONSTRAINT fk_fm_user FOREIGN KEY (user_id)
        REFERENCES user_account(user_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

-- ------------------------------------------------------------
-- Budget
-- ------------------------------------------------------------
CREATE TABLE budget (
    budget_id     INT             NOT NULL AUTO_INCREMENT,
    user_id       INT             NOT NULL,
    name          VARCHAR(100)    NOT NULL,
    amount        DECIMAL(15,2)   NOT NULL,
    period_start  DATE            NOT NULL,
    period_end    DATE            NOT NULL,
    status        VARCHAR(50)     NOT NULL DEFAULT 'active',
    PRIMARY KEY (budget_id),
    CONSTRAINT fk_budget_user FOREIGN KEY (user_id)
        REFERENCES user_account(user_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT chk_budget_amount       CHECK (amount >= 0),
    CONSTRAINT chk_budget_period       CHECK (period_end >= period_start)
);

-- ------------------------------------------------------------
-- Parameter planner
-- ------------------------------------------------------------
CREATE TABLE parameter_planner (
    parameter_id           INT     NOT NULL AUTO_INCREMENT,
    user_id                INT     NOT NULL,
    budget_id              INT     NOT NULL,
    shopping_period_start  DATE    NOT NULL,
    shopping_period_end    DATE    NOT NULL,
    meals_per_day          INT     NOT NULL,
    snack_per_day          INT     NOT NULL DEFAULT 0,
    PRIMARY KEY (parameter_id),
    CONSTRAINT fk_pp_user   FOREIGN KEY (user_id)
        REFERENCES user_account(user_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_pp_budget FOREIGN KEY (budget_id)
        REFERENCES budget(budget_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT chk_pp_meals_per_day    CHECK (meals_per_day > 0),
    CONSTRAINT chk_pp_snack_per_day    CHECK (snack_per_day >= 0),
    CONSTRAINT chk_pp_period           CHECK (shopping_period_end >= shopping_period_start)
);

-- ------------------------------------------------------------
-- Weekly menu
-- ------------------------------------------------------------
CREATE TABLE weekly_menu (
    menu_id           INT             NOT NULL AUTO_INCREMENT,
    user_id           INT             NOT NULL,
    parameter_id      INT             NOT NULL,
    week_start_date   DATE            NOT NULL,
    week_end_date     DATE            NOT NULL,
    total_estimation  DECIMAL(15,2)   NOT NULL DEFAULT 0,
    status_budget     VARCHAR(50)     NOT NULL DEFAULT 'draft',
    created_datetime  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (menu_id),
    CONSTRAINT fk_wm_user      FOREIGN KEY (user_id)
        REFERENCES user_account(user_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_wm_parameter FOREIGN KEY (parameter_id)
        REFERENCES parameter_planner(parameter_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT chk_wm_total_estimation CHECK (total_estimation >= 0),
    CONSTRAINT chk_wm_week_dates       CHECK (week_end_date >= week_start_date)
);

-- ------------------------------------------------------------
-- Ingredient
-- ------------------------------------------------------------
CREATE TABLE ingredient (
    ingredient_id  INT             NOT NULL AUTO_INCREMENT,
    name           VARCHAR(100)    NOT NULL,
    unit           VARCHAR(50)     NOT NULL,
    PRIMARY KEY (ingredient_id),
    UNIQUE KEY uq_ingredient_name (name)
);

-- ------------------------------------------------------------
-- Ingredient nutrition
-- ------------------------------------------------------------
CREATE TABLE ingredient_nutrition (
    nutrition_id   INT             NOT NULL AUTO_INCREMENT,
    ingredient_id  INT             NOT NULL,
    calories       DECIMAL(10,2)   NOT NULL DEFAULT 0,
    protein        DECIMAL(10,2)   NOT NULL DEFAULT 0,
    carbohydrate   DECIMAL(10,2)   NOT NULL DEFAULT 0,
    fat            DECIMAL(10,2)   NOT NULL DEFAULT 0,
    fibre          DECIMAL(10,2)   NOT NULL DEFAULT 0,
    unit           VARCHAR(50)     NOT NULL,
    PRIMARY KEY (nutrition_id),
    UNIQUE KEY uq_nutrition_ingredient (ingredient_id),
    CONSTRAINT fk_in_ingredient FOREIGN KEY (ingredient_id)
        REFERENCES ingredient(ingredient_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT chk_in_calories     CHECK (calories >= 0),
    CONSTRAINT chk_in_protein      CHECK (protein >= 0),
    CONSTRAINT chk_in_carbohydrate CHECK (carbohydrate >= 0),
    CONSTRAINT chk_in_fat          CHECK (fat >= 0),
    CONSTRAINT chk_in_fibre        CHECK (fibre >= 0)
);

-- ------------------------------------------------------------
-- Ingredient price history
-- ------------------------------------------------------------
CREATE TABLE ingredient_price (
    price_id        INT             NOT NULL AUTO_INCREMENT,
    ingredient_id   INT             NOT NULL,
    price           DECIMAL(15,2)   NOT NULL,
    effective_date  DATE            NOT NULL,
    PRIMARY KEY (price_id),
    CONSTRAINT fk_ip_ingredient FOREIGN KEY (ingredient_id)
        REFERENCES ingredient(ingredient_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT chk_ip_price CHECK (price >= 0)
);

-- ------------------------------------------------------------
-- Kitchen stock
-- ------------------------------------------------------------
CREATE TABLE kitchen_stock (
    stock_id          INT             NOT NULL AUTO_INCREMENT,
    user_id           INT             NOT NULL,
    ingredient_id     INT             NOT NULL,
    quantity          DOUBLE          NOT NULL DEFAULT 0,
    unit              VARCHAR(50)     NOT NULL,
    storage_location  VARCHAR(100),
    expiry_date       DATE,
    PRIMARY KEY (stock_id),
    CONSTRAINT fk_ks_user       FOREIGN KEY (user_id)
        REFERENCES user_account(user_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_ks_ingredient FOREIGN KEY (ingredient_id)
        REFERENCES ingredient(ingredient_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT chk_ks_quantity CHECK (quantity >= 0)
);

-- ------------------------------------------------------------
-- Recipe
-- ------------------------------------------------------------
CREATE TABLE recipe (
    recipe_id     INT             NOT NULL AUTO_INCREMENT,
    name          VARCHAR(200)    NOT NULL,
    description   TEXT,
    serving_size  INT             NOT NULL DEFAULT 1,
    status        VARCHAR(50)     NOT NULL DEFAULT 'active',
    PRIMARY KEY (recipe_id),
    UNIQUE KEY uq_recipe_name (name),
    CONSTRAINT chk_recipe_serving_size CHECK (serving_size > 0)
);

-- ------------------------------------------------------------
-- Recipe ingredient
-- ------------------------------------------------------------
CREATE TABLE recipe_ingredient (
    recipe_ingredient_id  INT             NOT NULL AUTO_INCREMENT,
    recipe_id             INT             NOT NULL,
    ingredient_id         INT             NOT NULL,
    amount                DOUBLE          NOT NULL,
    unit                  VARCHAR(50)     NOT NULL,
    PRIMARY KEY (recipe_ingredient_id),
    UNIQUE KEY uq_ri_recipe_ingredient (recipe_id, ingredient_id),
    CONSTRAINT fk_ri_recipe     FOREIGN KEY (recipe_id)
        REFERENCES recipe(recipe_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_ri_ingredient FOREIGN KEY (ingredient_id)
        REFERENCES ingredient(ingredient_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_ri_amount CHECK (amount > 0)
);

-- ------------------------------------------------------------
-- Meal slot
-- ------------------------------------------------------------
CREATE TABLE meal_slot (
    slot_id        INT             NOT NULL AUTO_INCREMENT,
    menu_id        INT             NOT NULL,
    recipe_id      INT             NOT NULL,
    meal_date      DATE            NOT NULL,
    meal_time      VARCHAR(50)     NOT NULL,
    is_eating_out  TINYINT(1)      NOT NULL DEFAULT 0,
    outside_cost   DECIMAL(15,2)   NOT NULL DEFAULT 0,
    PRIMARY KEY (slot_id),
    CONSTRAINT fk_ms_menu   FOREIGN KEY (menu_id)
        REFERENCES weekly_menu(menu_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_ms_recipe FOREIGN KEY (recipe_id)
        REFERENCES recipe(recipe_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_ms_is_eating_out CHECK (is_eating_out IN (0, 1)),
    CONSTRAINT chk_ms_outside_cost  CHECK (outside_cost >= 0)
);

-- ------------------------------------------------------------
-- Shopping planner
-- ------------------------------------------------------------
CREATE TABLE shopping_planner (
    planner_id        INT             NOT NULL AUTO_INCREMENT,
    menu_id           INT             NOT NULL,
    created_datetime  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_estimation  DECIMAL(15,2)   NOT NULL DEFAULT 0,
    total_actual      DECIMAL(15,2)   NOT NULL DEFAULT 0,
    status            VARCHAR(50)     NOT NULL DEFAULT 'draft',
    PRIMARY KEY (planner_id),
    CONSTRAINT fk_sp_menu FOREIGN KEY (menu_id)
        REFERENCES weekly_menu(menu_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT chk_sp_total_estimation CHECK (total_estimation >= 0),
    CONSTRAINT chk_sp_total_actual     CHECK (total_actual >= 0)
);

-- ------------------------------------------------------------
-- Shopping planner item
-- ------------------------------------------------------------
CREATE TABLE shopping_planner_item (
    item_id          INT             NOT NULL AUTO_INCREMENT,
    planner_id       INT             NOT NULL,
    ingredient_id    INT             NOT NULL,
    required_qty     DOUBLE          NOT NULL,
    unit             VARCHAR(50)     NOT NULL,
    estimated_price  DECIMAL(15,2)   NOT NULL DEFAULT 0,
    actual_price     DECIMAL(15,2)   NOT NULL DEFAULT 0,
    status_beli      VARCHAR(50)     NOT NULL DEFAULT 'belum',
    PRIMARY KEY (item_id),
    UNIQUE KEY uq_spi_planner_ingredient (planner_id, ingredient_id),
    CONSTRAINT fk_spi_planner    FOREIGN KEY (planner_id)
        REFERENCES shopping_planner(planner_id)
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT fk_spi_ingredient FOREIGN KEY (ingredient_id)
        REFERENCES ingredient(ingredient_id)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,
    CONSTRAINT chk_spi_required_qty     CHECK (required_qty > 0),
    CONSTRAINT chk_spi_estimated_price  CHECK (estimated_price >= 0),
    CONSTRAINT chk_spi_actual_price     CHECK (actual_price >= 0)
);

-- ------------------------------------------------------------
-- Indexes
-- ------------------------------------------------------------
CREATE INDEX idx_family_member_user_id          ON family_member(user_id);
CREATE INDEX idx_budget_user_id                 ON budget(user_id);
CREATE INDEX idx_parameter_user_id              ON parameter_planner(user_id);
CREATE INDEX idx_parameter_budget_id            ON parameter_planner(budget_id);
CREATE INDEX idx_weekly_menu_user_id            ON weekly_menu(user_id);
CREATE INDEX idx_weekly_menu_parameter_id       ON weekly_menu(parameter_id);
CREATE INDEX idx_kitchen_stock_user_id          ON kitchen_stock(user_id);
CREATE INDEX idx_kitchen_stock_ingredient_id    ON kitchen_stock(ingredient_id);
CREATE INDEX idx_ingredient_price_ingredient_id ON ingredient_price(ingredient_id);
CREATE INDEX idx_recipe_ingredient_recipe_id    ON recipe_ingredient(recipe_id);
CREATE INDEX idx_recipe_ingredient_ingredient_id ON recipe_ingredient(ingredient_id);
CREATE INDEX idx_meal_slot_menu_id              ON meal_slot(menu_id);
CREATE INDEX idx_meal_slot_recipe_id            ON meal_slot(recipe_id);
CREATE INDEX idx_shopping_planner_menu_id       ON shopping_planner(menu_id);
CREATE INDEX idx_shopping_item_planner_id       ON shopping_planner_item(planner_id);
CREATE INDEX idx_shopping_item_ingredient_id    ON shopping_planner_item(ingredient_id);