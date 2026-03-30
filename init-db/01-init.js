// BrewMap — collection setup
// Run once in mongosh or any MongoDB interface after the container is up.
// Switch to the brewmap database first: use brewmap

const db = connect("mongodb://localhost:27017/brewmap");

db.createCollection("users");
db.createCollection("locations");
db.createCollection("products");
db.createCollection("reviews");
db.createCollection("reports");
db.createCollection("categories");
db.createCollection("payment_options");
db.createCollection("temp_tokens");

print("Collections created.");