# ============================================================
# Dummy Data Generator for Nasi Bergizi Pajak
# Requires:
# pip install faker mysql-connector-python
# ============================================================

from faker import Faker
import random
from datetime import datetime, timedelta
import mysql.connector

fake = Faker("id_ID")
random.seed(42)

# ============================================================
# DATABASE CONFIG
# ============================================================

db = mysql.connector.connect(
    host="localhost",
    user="root",
    password="", #GANTI DENGAN PASSWORD USER
    database="" #GANTI DENGAN NAMA DATABASE LOCAL
)

cursor = db.cursor()

# ============================================================
# CONFIG
# ============================================================

MIN_DATA = 10

MEAL_TIMES = ["breakfast", "lunch", "dinner", "snack"]
UNITS = ["kg", "gram", "pcs", "liter", "ml"]
STORAGE_LOCATIONS = ["Fridge", "Freezer", "Pantry", "Cabinet"]
BUDGET_STATUS = ["active", "inactive"]
MENU_STATUS = ["draft", "completed", "overbudget"]
SHOPPING_STATUS = ["draft", "completed"]
BELI_STATUS = ["belum", "sudah"]
RECIPE_STATUS = ["active", "inactive"]

INGREDIENT_NAMES = [
    "Beras",
    "Telur",
    "Ayam",
    "Bayam",
    "Wortel",
    "Bawang Merah",
    "Bawang Putih",
    "Minyak Goreng",
    "Susu",
    "Tempe",
    "Tahu",
    "Cabai",
    "Garam",
    "Gula",
    "Tomat"
]

RECIPE_NAMES = [
    "Nasi Goreng",
    "Ayam Bakar",
    "Sayur Bayam",
    "Tumis Tempe",
    "Sup Ayam",
    "Omelette",
    "Tahu Goreng",
    "Capcay",
    "Sop Wortel",
    "Tempe Orek"
]

# ============================================================
# HELPER FUNCTIONS
# ============================================================

def rand_date(start_days=-365, end_days=365):
    start = datetime.now() + timedelta(days=start_days)
    end = datetime.now() + timedelta(days=end_days)

    return fake.date_between(start_date=start, end_date=end)

# ============================================================
# 1. USER ACCOUNT
# ============================================================

user_ids = []

for i in range(MIN_DATA):

    email = f"user{i}@example.com"

    sql = """
    INSERT INTO user_account
    (email, password, first_name, last_name,
     active, signup_datetime, profile_image_name, tipe_admin)
    VALUES (%s,%s,%s,%s,%s,%s,%s,%s)
    """

    values = (
        email,
        fake.sha256(),
        fake.first_name(),
        fake.last_name(),
        random.choice([0, 1]),
        fake.date_time_this_decade(),
        f"profile_{i}.jpg",
        1 if i == 0 else 0
    )

    cursor.execute(sql, values)
    user_ids.append(cursor.lastrowid)

# EDGE CASES
cursor.execute("""
INSERT INTO user_account
(email,password,first_name,last_name,active,profile_image_name,tipe_admin)
VALUES
('edgecase@example.com','123','A',NULL,1,NULL,0)
""")

# ============================================================
# 2. FAMILY MEMBER
# ============================================================

member_ids = []

for user_id in user_ids:

    for _ in range(random.randint(1, 4)):

        sql = """
        INSERT INTO family_member
        (user_id,name,birth_date,height,weight,allergy)
        VALUES (%s,%s,%s,%s,%s,%s)
        """

        values = (
            user_id,
            fake.first_name(),
            fake.date_of_birth(minimum_age=1, maximum_age=70),
            round(random.uniform(80, 190), 2),
            round(random.uniform(10, 120), 2),
            random.choice([
                None,
                "Seafood",
                "Peanut",
                "Milk",
                "Egg"
            ])
        )

        cursor.execute(sql, values)
        member_ids.append(cursor.lastrowid)

# EDGE CASE
cursor.execute("""
INSERT INTO family_member
(user_id,name,birth_date,height,weight,allergy)
VALUES (%s,%s,%s,%s,%s,%s)
""", (
    user_ids[0],
    "Baby",
    datetime.now().date(),
    50,
    3.5,
    "Milk"
))

# ============================================================
# 3. BUDGET
# ============================================================

budget_ids = []

for user_id in user_ids:

    start_date = rand_date(-30, 0)
    end_date = start_date + timedelta(days=30)

    sql = """
    INSERT INTO budget
    (user_id,name,amount,period_start,period_end,status)
    VALUES (%s,%s,%s,%s,%s,%s)
    """

    values = (
        user_id,
        f"Monthly Budget {user_id}",
        round(random.uniform(100000, 5000000), 2),
        start_date,
        end_date,
        random.choice(BUDGET_STATUS)
    )

    cursor.execute(sql, values)
    budget_ids.append(cursor.lastrowid)

# EDGE CASE: ZERO BUDGET
cursor.execute("""
INSERT INTO budget
(user_id,name,amount,period_start,period_end,status)
VALUES (%s,%s,%s,%s,%s,%s)
""", (
    user_ids[0],
    "Zero Budget",
    0,
    datetime.now().date(),
    datetime.now().date() + timedelta(days=7),
    "active"
))

# ============================================================
# 4. PARAMETER PLANNER
# ============================================================

parameter_ids = []

for i in range(MIN_DATA):

    start_date = rand_date(-15, 0)
    end_date = start_date + timedelta(days=7)

    sql = """
    INSERT INTO parameter_planner
    (user_id,budget_id,shopping_period_start,
     shopping_period_end,meals_per_day,snack_per_day)
    VALUES (%s,%s,%s,%s,%s,%s)
    """

    values = (
        user_ids[i],
        budget_ids[i],
        start_date,
        end_date,
        random.randint(1, 5),
        random.randint(0, 3)
    )

    cursor.execute(sql, values)
    parameter_ids.append(cursor.lastrowid)

# ============================================================
# 5. INGREDIENT
# ============================================================

ingredient_ids = []

for ingredient in INGREDIENT_NAMES:

    sql = """
    INSERT INTO ingredient
    (name, unit)
    VALUES (%s,%s)
    """

    values = (
        ingredient,
        random.choice(UNITS)
    )

    cursor.execute(sql, values)
    ingredient_ids.append(cursor.lastrowid)

# ============================================================
# 6. INGREDIENT NUTRITION
# ============================================================

for ingredient_id in ingredient_ids:

    sql = """
    INSERT INTO ingredient_nutrition
    (ingredient_id, calories, protein,
     carbohydrate, fat, fibre, unit)
    VALUES (%s,%s,%s,%s,%s,%s,%s)
    """

    values = (
        ingredient_id,
        round(random.uniform(0, 500), 2),
        round(random.uniform(0, 50), 2),
        round(random.uniform(0, 100), 2),
        round(random.uniform(0, 40), 2),
        round(random.uniform(0, 30), 2),
        "100g"
    )

    cursor.execute(sql, values)

# EDGE CASE: ZERO NUTRITION
cursor.execute("""
INSERT INTO ingredient (name, unit)
VALUES (%s,%s)
""", ("Air Kosong", "ml"))

edge_ingredient_id = cursor.lastrowid

cursor.execute("""
INSERT INTO ingredient_nutrition
(ingredient_id, calories, protein,
 carbohydrate, fat, fibre, unit)
VALUES (%s,%s,%s,%s,%s,%s,%s)
""", (
    edge_ingredient_id,
    0,0,0,0,0,
    "100g"
))

# ============================================================
# 7. INGREDIENT PRICE
# ============================================================

for ingredient_id in ingredient_ids:

    for _ in range(3):

        sql = """
        INSERT INTO ingredient_price
        (ingredient_id,price,effective_date)
        VALUES (%s,%s,%s)
        """

        values = (
            ingredient_id,
            round(random.uniform(1000, 100000), 2),
            rand_date(-365, 0)
        )

        cursor.execute(sql, values)

# ============================================================
# 8. KITCHEN STOCK
# ============================================================

for user_id in user_ids:

    sampled = random.sample(ingredient_ids, 5)

    for ingredient_id in sampled:

        sql = """
        INSERT INTO kitchen_stock
        (user_id,ingredient_id,quantity,
         unit,storage_location,expiry_date)
        VALUES (%s,%s,%s,%s,%s,%s)
        """

        values = (
            user_id,
            ingredient_id,
            round(random.uniform(0, 20), 2),
            random.choice(UNITS),
            random.choice(STORAGE_LOCATIONS),
            rand_date(-10, 30)
        )

        cursor.execute(sql, values)

# EDGE CASE: EXPIRED STOCK
cursor.execute("""
INSERT INTO kitchen_stock
(user_id,ingredient_id,quantity,unit,storage_location,expiry_date)
VALUES (%s,%s,%s,%s,%s,%s)
""", (
    user_ids[0],
    ingredient_ids[0],
    1,
    "kg",
    "Fridge",
    datetime.now().date() - timedelta(days=5)
))

# ============================================================
# 9. RECIPE
# ============================================================

recipe_ids = []

for recipe in RECIPE_NAMES:

    sql = """
    INSERT INTO recipe
    (name,description,serving_size,status)
    VALUES (%s,%s,%s,%s)
    """

    values = (
        recipe,
        fake.paragraph(),
        random.randint(1, 8),
        random.choice(RECIPE_STATUS)
    )

    cursor.execute(sql, values)
    recipe_ids.append(cursor.lastrowid)

# EDGE CASE
cursor.execute("""
INSERT INTO recipe
(name,description,serving_size,status)
VALUES (%s,%s,%s,%s)
""", (
    "Extreme Portion Meal",
    "Huge serving meal",
    100,
    "active"
))

# ============================================================
# 10. RECIPE INGREDIENT
# ============================================================

for recipe_id in recipe_ids:

    sampled = random.sample(ingredient_ids, 4)

    for ingredient_id in sampled:

        sql = """
        INSERT INTO recipe_ingredient
        (recipe_id,ingredient_id,amount,unit)
        VALUES (%s,%s,%s,%s)
        """

        values = (
            recipe_id,
            ingredient_id,
            round(random.uniform(0.1, 5), 2),
            random.choice(UNITS)
        )

        cursor.execute(sql, values)

# ============================================================
# 11. WEEKLY MENU
# ============================================================

menu_ids = []

for i in range(MIN_DATA):

    start_date = rand_date(-7, 7)
    end_date = start_date + timedelta(days=6)

    sql = """
    INSERT INTO weekly_menu
    (user_id,parameter_id,week_start_date,
     week_end_date,total_estimation,status_budget)
    VALUES (%s,%s,%s,%s,%s,%s)
    """

    values = (
        user_ids[i],
        parameter_ids[i],
        start_date,
        end_date,
        round(random.uniform(50000, 1000000), 2),
        random.choice(MENU_STATUS)
    )

    cursor.execute(sql, values)
    menu_ids.append(cursor.lastrowid)

# ============================================================
# 12. MEAL SLOT
# ============================================================

for menu_id in menu_ids:

    for _ in range(7):

        sql = """
        INSERT INTO meal_slot
        (menu_id,recipe_id,meal_date,
         meal_time,is_eating_out,outside_cost)
        VALUES (%s,%s,%s,%s,%s,%s)
        """

        eating_out = random.choice([0, 1])

        values = (
            menu_id,
            random.choice(recipe_ids),
            rand_date(-7, 7),
            random.choice(MEAL_TIMES),
            eating_out,
            round(random.uniform(0, 150000), 2)
            if eating_out else 0
        )

        cursor.execute(sql, values)

# ============================================================
# 13. SHOPPING PLANNER
# ============================================================

planner_ids = []

for menu_id in menu_ids:

    sql = """
    INSERT INTO shopping_planner
    (menu_id,created_datetime,total_estimation,
     total_actual,status)
    VALUES (%s,%s,%s,%s,%s)
    """

    values = (
        menu_id,
        fake.date_time_this_year(),
        round(random.uniform(10000, 1000000), 2),
        round(random.uniform(10000, 1000000), 2),
        random.choice(SHOPPING_STATUS)
    )

    cursor.execute(sql, values)
    planner_ids.append(cursor.lastrowid)

# ============================================================
# 14. SHOPPING PLANNER ITEM
# ============================================================

for planner_id in planner_ids:

    sampled = random.sample(ingredient_ids, 5)

    for ingredient_id in sampled:

        estimated = round(random.uniform(1000, 50000), 2)

        sql = """
        INSERT INTO shopping_planner_item
        (planner_id,ingredient_id,required_qty,
         unit,estimated_price,actual_price,status_beli)
        VALUES (%s,%s,%s,%s,%s,%s,%s)
        """

        values = (
            planner_id,
            ingredient_id,
            round(random.uniform(0.1, 10), 2),
            random.choice(UNITS),
            estimated,
            estimated + random.uniform(-5000, 10000),
            random.choice(BELI_STATUS)
        )

        cursor.execute(sql, values)

# ============================================================
# COMMIT
# ============================================================

db.commit()

print("===================================")
print("Dummy data inserted successfully!")
print("===================================")

cursor.close()
db.close()