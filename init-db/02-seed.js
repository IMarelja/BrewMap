// BrewMap — seed data for development
// Run after 01-init.js
// Switch to the brewmap database first: use brewmap

const db = connect("mongodb://localhost:27017/brewmap");

const now = new Date();

// ─── Users ────────────────────────────────────────────────────────────────────
// Passwords are all "Password1!" — hashed with PBKDF2 for realism
// - PRF: HMAC-SHA256
// - Iterations: 10,000
// - Salt: provided as Base64, then decoded
// - Output length: 128 bits (16 bytes)
// ── How it is hashed (order is important) ─────────────────────────────────────
// (PlaintextPassword + Salt)
// Do NOT use these in any non-development environment

const adminId   = new ObjectId();
const userId1   = new ObjectId(); // ivan
const userId2   = new ObjectId(); // valentina
const userId3   = new ObjectId(); // gonzalo
const userId4   = new ObjectId(); // ruva
const userId5   = new ObjectId(); // zara

db.users.insertMany([
  {
    _id: adminId,
    username: "admin",
    email: "admin@brewmap.dev",
    passwordHash: "etPpHKasXMa2tzTXRPQqbg==",
    passwordSalt: "QeQAgDwNMIq/lNAOazZrqg==",
    role: "admin",
    isActive: true,
    isDeleted: false,
    reportCount: 0,
    createdAt: now,
    lastLoginAt: now,
    deletedAt: null,
  },
  {
    _id: userId1,
    username: "ivan_m",
    email: "ivan@brewmap.dev",
    passwordHash: "/DjBnTndT6GL/6dBxXGSkQ==",
    passwordSalt: "rskvlzO+a4Q99UQTpjMNuA==",
    role: "user",
    isActive: true,
    isDeleted: false,
    reportCount: 0,
    createdAt: now,
    lastLoginAt: now,
    deletedAt: null,
  },
  {
    _id: userId2,
    username: "valentina_r",
    email: "valentina@brewmap.dev",
    passwordHash: "19McnVnb4mh5sJQOydYXew==",
    passwordSalt: "ZklFRvVUQW4XYx7DUv85Qw==",
    role: "user",
    isActive: true,
    isDeleted: false,
    reportCount: 0,
    createdAt: now,
    lastLoginAt: null,
    deletedAt: null,
  },
  {
    _id: userId3,
    username: "gonzalo_g",
    email: "gonzalo@brewmap.dev",
    passwordHash: "YpF1qXc9f4Qu0gDsbI1Yhg==",
    passwordSalt: "h3dE/W0ksOFqqJspWaaJOA==",
    role: "user",
    isActive: true,
    isDeleted: false,
    reportCount: 0,
    createdAt: now,
    lastLoginAt: null,
    deletedAt: null,
  },
  {
    _id: userId4,
    username: "ruva_m",
    email: "ruva@brewmap.dev",
    passwordHash: "liQRVG6OgILRzHSDkjPLAQ==",
    passwordSalt: "cS3T2iJyT6gHrLNDxyghag==",
    role: "user",
    isActive: true,
    isDeleted: false,
    reportCount: 0,
    createdAt: now,
    lastLoginAt: null,
    deletedAt: null,
  },
  {
    _id: userId5,
    username: "zara_c",
    email: "zara@brewmap.dev",
    passwordHash: "o2RBmJ1QRyUjkTDrAX6PlA==",
    passwordSalt: "L1Mp/MlGaZ3Z3tku3Vt38w==",
    role: "user",
    isActive: true,
    isDeleted: false,
    reportCount: 0,
    createdAt: now,
    lastLoginAt: null,
    deletedAt: null,
  },
]);

// ─── Locations (Zagreb, Croatia) ──────────────────────────────────────────────
// Coordinates are real Zagreb spots, offset slightly to nearby streets

const loc1  = new ObjectId();
const loc2  = new ObjectId();
const loc3  = new ObjectId();
const loc4  = new ObjectId();
const loc5  = new ObjectId();
const loc6  = new ObjectId();
const loc7  = new ObjectId();
const loc8  = new ObjectId();
const loc9  = new ObjectId();
const loc10 = new ObjectId();

const hours = (open, close) => ({
  monday:    { open, close, isClosed: false },
  tuesday:   { open, close, isClosed: false },
  wednesday: { open, close, isClosed: false },
  thursday:  { open, close, isClosed: false },
  friday:    { open, close, isClosed: false },
  saturday:  { open, close, isClosed: false },
  sunday:    { open: "09:00", close: "17:00", isClosed: false },
});

const hoursClosedSunday = (open, close) => ({
  monday:    { open, close, isClosed: false },
  tuesday:   { open, close, isClosed: false },
  wednesday: { open, close, isClosed: false },
  thursday:  { open, close, isClosed: false },
  friday:    { open, close, isClosed: false },
  saturday:  { open, close, isClosed: false },
  sunday:    { isClosed: true },
});

db.locations.insertMany([
  {
    _id: loc1,
    name: "Cogito Coffee",
    description: "Specialty coffee roaster and café in the heart of Zagreb.",
    address: { street: "Varšavska 11", city: "Zagreb", country: "Croatia", postalCode: "10000" },
    location: { type: "Point", coordinates: [15.9734, 45.8131] },
    categoryTag: "coffeeshop",
    paymentOptionTags: ["cash", "card"],
    openingHours: hours("07:30", "21:00"),
    contact: { website: "https://cogitocoffee.com" },
    reportCount: 0,
    isActive: true,
    addedByUserId: userId1,
    createdAt: now,
    updatedAt: now,
    
    edits: [],
    aggregatedRating: { average: 0, count: 0 },
  },
  {
    _id: loc2,
    name: "Eli's Caffe",
    description: "Popular local café known for its relaxed vibe and great espresso.",
    address: { street: "Ilica 63", city: "Zagreb", country: "Croatia", postalCode: "10000" },
    location: { type: "Point", coordinates: [15.9651, 45.8132] },
    categoryTag: "cafe",
    paymentOptionTags: ["cash", "card", "mobile"],
    openingHours: hours("08:00", "22:00"),
    contact: { website: null },
    reportCount: 0,
    isActive: true,
    addedByUserId: userId2,
    createdAt: now,
    updatedAt: now,
    
    edits: [],
    aggregatedRating: { average: 0, count: 0 },
  },
  {
    _id: loc3,
    name: "Stari Fijaker 900",
    description: "Old-school Zagreb café with traditional pastries and strong coffee.",
    address: { street: "Mesnička 6", city: "Zagreb", country: "Croatia", postalCode: "10000" },
    location: { type: "Point", coordinates: [15.9692, 45.8142] },
    categoryTag: "cafe",
    paymentOptionTags: ["cash"],
    openingHours: hours("07:00", "20:00"),
    contact: { website: null },
    reportCount: 0,
    isActive: true,
    addedByUserId: userId1,
    createdAt: now,
    updatedAt: now,
    
    edits: [],
    aggregatedRating: { average: 0, count: 0 },
  },
  {
    _id: loc4,
    name: "Quahwa",
    description: "Third-wave specialty café with single-origin pour-overs.",
    address: { street: "Preradovićeva 11", city: "Zagreb", country: "Croatia", postalCode: "10000" },
    location: { type: "Point", coordinates: [15.9712, 45.8127] },
    categoryTag: "coffeeshop",
    paymentOptionTags: ["cash", "card"],
    openingHours: hours("08:00", "21:00"),
    contact: { website: null },
    reportCount: 0,
    isActive: true,
    addedByUserId: userId3,
    createdAt: now,
    updatedAt: now,
    
    edits: [],
    aggregatedRating: { average: 0, count: 0 },
  },
  {
    _id: loc5,
    name: "Torrefazione",
    description: "In-house roastery with an ever-changing menu of seasonal beans.",
    address: { street: "Gajeva 14", city: "Zagreb", country: "Croatia", postalCode: "10000" },
    location: { type: "Point", coordinates: [15.9756, 45.8124] },
    categoryTag: "roastery",
    paymentOptionTags: ["cash", "card", "mobile"],
    openingHours: hoursClosedSunday("08:00", "20:00"),
    contact: { website: "https://torrefazione.hr" },
    reportCount: 0,
    isActive: true,
    addedByUserId: userId4,
    createdAt: now,
    updatedAt: now,
    
    edits: [],
    aggregatedRating: { average: 0, count: 0 },
  },
  {
    _id: loc6,
    name: "Vanilla Zagreb",
    description: "Bright bakery café famous for its croissants and filter coffee.",
    address: { street: "Petrinjska 4", city: "Zagreb", country: "Croatia", postalCode: "10000" },
    location: { type: "Point", coordinates: [15.9779, 45.8115] },
    categoryTag: "bakery",
    paymentOptionTags: ["cash", "card"],
    openingHours: hours("07:00", "20:00"),
    contact: { website: null },
    reportCount: 0,
    isActive: true,
    addedByUserId: userId5,
    createdAt: now,
    updatedAt: now,
    
    edits: [],
    aggregatedRating: { average: 0, count: 0 },
  },
  {
    _id: loc7,
    name: "Kava Tava",
    description: "Cozy neighbourhood spot with homemade cakes and loose-leaf teas.",
    address: { street: "Jurišićeva 9", city: "Zagreb", country: "Croatia", postalCode: "10000" },
    location: { type: "Point", coordinates: [15.9801, 45.8133] },
    categoryTag: "cafe",
    paymentOptionTags: ["cash"],
    openingHours: hours("09:00", "21:00"),
    contact: { website: null },
    reportCount: 1,
    isActive: true,
    addedByUserId: userId2,
    createdAt: now,
    updatedAt: now,
    
    edits: [],
    aggregatedRating: { average: 0, count: 0 },
  },
  {
    _id: loc8,
    name: "Express Bar Donji Grad",
    description: "Standing-room espresso bar, classic Zagreb style.",
    address: { street: "Bogovićeva 7", city: "Zagreb", country: "Croatia", postalCode: "10000" },
    location: { type: "Point", coordinates: [15.9745, 45.8118] },
    categoryTag: "coffeeshop",
    paymentOptionTags: ["cash", "mobile"],
    openingHours: hoursClosedSunday("06:30", "18:00"),
    contact: { website: null },
    reportCount: 0,
    isActive: true,
    addedByUserId: userId3,
    createdAt: now,
    updatedAt: now,
    
    edits: [],
    aggregatedRating: { average: 0, count: 0 },
  },
  {
    _id: loc9,
    name: "Bread Club",
    description: "Artisan bakery with sourdough, pastries, and excellent flat whites.",
    address: { street: "Tratinska 22", city: "Zagreb", country: "Croatia", postalCode: "10000" },
    location: { type: "Point", coordinates: [15.9589, 45.8155] },
    categoryTag: "bakery",
    paymentOptionTags: ["cash", "card"],
    openingHours: hoursClosedSunday("07:00", "19:00"),
    contact: { website: "https://breadclub.hr" },
    reportCount: 0,
    isActive: true,
    addedByUserId: userId1,
    createdAt: now,
    updatedAt: now,
    
    edits: [],
    aggregatedRating: { average: 0, count: 0 },
  },
  {
    _id: loc10,
    name: "Soft Spot",
    description: "Laptop-friendly café with good WiFi, all-day breakfast, and batch brew.",
    address: { street: "Vlaška 49", city: "Zagreb", country: "Croatia", postalCode: "10000" },
    location: { type: "Point", coordinates: [15.9851, 45.8143] },
    categoryTag: "cafe",
    paymentOptionTags: ["cash", "card", "mobile"],
    openingHours: hours("08:00", "22:00"),
    contact: { website: null },
    reportCount: 0,
    isActive: true,
    addedByUserId: userId4,
    createdAt: now,
    updatedAt: now,
    
    edits: [],
    aggregatedRating: { average: 0, count: 0 },
  },
]);

// ─── Products ─────────────────────────────────────────────────────────────────

const products = [];

// helper to push a product and return its ObjectId
function prod(locationId, name, description, addedBy) {
  const id = new ObjectId();
  products.push({
    _id: id,
    name,
    description,
    availableAtLocationId: locationId,
    isVisible: true,
    reportCount: 0,
    createdByUserId: addedBy,
    createdAt: now,
    updatedAt: now,
    aggregatedRating: { average: 0, count: 0 },
  });
  return id;
}

// Cogito
const cogEspresso  = prod(loc1, "Espresso",       "Single-origin espresso shot",               userId1);
const cogFlat      = prod(loc1, "Flat White",      "Double ristretto with steamed milk",         userId1);
const cogFilter    = prod(loc1, "Filter Coffee",   "Seasonal batch brew, changes weekly",         userId1);

// Eli's
const eliCap       = prod(loc2, "Cappuccino",      "Classic Italian-style cappuccino",            userId2);
const eliCold      = prod(loc2, "Cold Brew",       "12-hour cold-steeped concentrate",            userId2);

// Stari Fijaker
const sfTurkish    = prod(loc3, "Turkish Coffee",  "Traditional džezva-brewed coffee",            userId1);
const sfStrudel    = prod(loc3, "Apple Strudel",   "House-made, served warm",                     userId1);

// Quahwa
const qPourOver    = prod(loc4, "Pour Over",       "Single-origin V60, rotating origins",         userId3);
const qLatte       = prod(loc4, "Oat Latte",       "Espresso with house-made oat milk",           userId3);

// Torrefazione
const torEthiopia  = prod(loc5, "Ethiopia Yirgacheffe", "Light roast, floral and citrus notes",  userId4);
const torBrazil    = prod(loc5, "Brazil Natural",  "Medium roast, chocolate and caramel",         userId4);
const torEspresso  = prod(loc5, "Espresso Blend",  "House blend, balanced and sweet",             userId4);

// Vanilla
const vanCroissant = prod(loc6, "Butter Croissant","Freshly baked each morning",                  userId5);
const vanFlat      = prod(loc6, "Flat White",      "Double shot with velvety microfoam",          userId5);

// Kava Tava
const ktCherry     = prod(loc7, "Cherry Cake",     "House-made, seasonal fruit filling",          userId2);
const ktLatte      = prod(loc7, "Latte",           "Smooth espresso with steamed milk",           userId2);

// Express Bar
const exEspresso   = prod(loc8, "Espresso",        "Classic standing-bar espresso",               userId3);
const exMacchiato  = prod(loc8, "Macchiato",       "Espresso with a dash of foam",                userId3);

// Bread Club
const bcSourdough  = prod(loc9, "Sourdough Loaf",  "Long-fermented, baked fresh daily",           userId1);
const bcFlatWhite  = prod(loc9, "Flat White",      "Double ristretto with silky milk",            userId1);

// Soft Spot
const ssAvo        = prod(loc10, "Avocado Toast",  "Sourdough, avo, chilli flakes, poached egg",  userId4);
const ssBatch      = prod(loc10, "Batch Brew",     "Large-format filter, free refills",            userId4);

db.products.insertMany(products);

// ─── Reviews ─────────────────────────────────────────────────────────────────
// Each location gets 2 reviews from different users

const reviewDocs = [
  // Cogito
  { _id: new ObjectId(), userId: userId2, target: { type: "location", id: loc1 }, rating: 5, comment: "Best specialty coffee in Zagreb, hands down.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },
  { _id: new ObjectId(), userId: userId3, target: { type: "location", id: loc1 }, rating: 4, comment: "Great beans and skilled baristas. Gets busy on weekends.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },
  { _id: new ObjectId(), userId: userId5, target: { type: "location", id: loc1 }, rating: 5, comment: "The filter coffee menu is incredible.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },

  // Eli's
  { _id: new ObjectId(), userId: userId1, target: { type: "location", id: loc2 }, rating: 4, comment: "Solid neighbourhood café. Love the outdoor seating.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },
  { _id: new ObjectId(), userId: userId4, target: { type: "location", id: loc2 }, rating: 4, comment: "Good espresso and friendly staff.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },

  // Stari Fijaker
  { _id: new ObjectId(), userId: userId3, target: { type: "location", id: loc3 }, rating: 4, comment: "A slice of old Zagreb. The strudel is unmissable.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },
  { _id: new ObjectId(), userId: userId5, target: { type: "location", id: loc3 }, rating: 4, comment: "Traditional atmosphere. Turkish coffee was perfect.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },

  // Quahwa
  { _id: new ObjectId(), userId: userId1, target: { type: "location", id: loc4 }, rating: 5, comment: "Exceptional pour overs. The barista really knows their stuff.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },
  { _id: new ObjectId(), userId: userId2, target: { type: "location", id: loc4 }, rating: 4, comment: "Love the rotating single-origin selection.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },

  // Torrefazione
  { _id: new ObjectId(), userId: userId3, target: { type: "location", id: loc5 }, rating: 5, comment: "You can smell the roasting from the street. Incredible beans.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },
  { _id: new ObjectId(), userId: userId5, target: { type: "location", id: loc5 }, rating: 5, comment: "The Ethiopia Yirgacheffe is unlike anything I have tried.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },

  // Vanilla
  { _id: new ObjectId(), userId: userId1, target: { type: "location", id: loc6 }, rating: 4, comment: "Croissants are worth the morning detour.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },
  { _id: new ObjectId(), userId: userId4, target: { type: "location", id: loc6 }, rating: 4, comment: "Bright and clean space. Good flat white.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },

  // Kava Tava
  { _id: new ObjectId(), userId: userId2, target: { type: "location", id: loc7 }, rating: 3, comment: "Nice cakes but coffee was a bit weak.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },
  { _id: new ObjectId(), userId: userId3, target: { type: "location", id: loc7 }, rating: 4, comment: "Quiet and cosy. Good for an afternoon break.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },

  // Express Bar
  { _id: new ObjectId(), userId: userId4, target: { type: "location", id: loc8 }, rating: 4, comment: "Classic Zagreb espresso bar experience.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },
  { _id: new ObjectId(), userId: userId5, target: { type: "location", id: loc8 }, rating: 4, comment: "Fast, cheap, and good. Great before work.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },

  // Bread Club
  { _id: new ObjectId(), userId: userId2, target: { type: "location", id: loc9 }, rating: 5, comment: "Best sourdough in Zagreb. The flat white is excellent too.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },
  { _id: new ObjectId(), userId: userId3, target: { type: "location", id: loc9 }, rating: 4, comment: "Always a queue but always worth it.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },

  // Soft Spot
  { _id: new ObjectId(), userId: userId1, target: { type: "location", id: loc10 }, rating: 4, comment: "My go-to for remote work days. Fast WiFi and good coffee.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },
  { _id: new ObjectId(), userId: userId5, target: { type: "location", id: loc10 }, rating: 5, comment: "Avocado toast is the best in the city.", isVisible: true, reportCount: 0, createdAt: now, updatedAt: now },
];

function refreshTargetAggregatedRating(targetType, targetId) {
  const normalizedType = targetType === "drink" ? "product" : targetType;
  const [aggregation] = db.reviews.aggregate([
    { $match: { "target.type": normalizedType, "target.id": targetId, isVisible: true } },
    {
      $group: {
        _id: null,
        average: { $avg: "$rating" },
        count: { $sum: 1 },
      },
    },
  ]).toArray();

  const aggregatedRating = aggregation
    ? { average: Number(aggregation.average.toFixed(2)), count: aggregation.count }
    : { average: 0, count: 0 };

  if (normalizedType === "location") {
    db.locations.updateOne(
      { _id: targetId },
      { $set: { aggregatedRating } }
    );
    return;
  }

  if (normalizedType === "product") {
    db.products.updateOne(
      { _id: targetId },
      { $set: { aggregatedRating } }
    );
  }
}

function insertReviewAndRefresh(reviewDoc) {
  db.reviews.insertOne(reviewDoc);
  refreshTargetAggregatedRating(reviewDoc.target.type, reviewDoc.target.id);
}

for (const reviewDoc of reviewDocs) {
  insertReviewAndRefresh(reviewDoc);
}

// Categories
db.categories.insertMany([
  { _tag: "coffeeshop", name: "Coffee Shop", isActive: true, createdAt: now, updatedAt: now },
  { _tag: "cafe",       name: "Café",        isActive: true, createdAt: now, updatedAt: now },
  { _tag: "roastery",   name: "Roastery",    isActive: true, createdAt: now, updatedAt: now },
  { _tag: "bakery",     name: "Bakery",      isActive: true, createdAt: now, updatedAt: now },
]);

// Payment options
db.payment_options.insertMany([
  { _tag: "cash",   name: "Cash",         isActive: true, createdAt: now, updatedAt: now },
  { _tag: "card",   name: "Card",         isActive: true, createdAt: now, updatedAt: now },
  { _tag: "mobile", name: "Mobile Pay",   isActive: true, createdAt: now, updatedAt: now },
]);

// Reports — one against a location, one against a review (use real IDs from your seed)
// Replace the ObjectId strings below with actual _id values from your seeded data
db.reports.insertMany([
  {
    reportedByUserId: userId1,
    target: { type: "location", id: loc2 },
    reason: "spam",
    description: "This listing appears to be a duplicate of another nearby entry.",
    status: "pending",
    resolvedByAdminId: null,
    resolvedAt: null,
    resolutionNote: null,
    createdAt: now,
    updatedAt: now,
  },
  {
    reportedByUserId: userId4,
    target: { type: "review", id: userId4 },
    reason: "inappropriate",
    description: "Review contains offensive language.",
    status: "resolved",
    resolvedByAdminId: adminId,
    resolvedAt: now,
    resolutionNote: "Nothing will happen",
    createdAt: now,
    updatedAt: now,
  },
]);
