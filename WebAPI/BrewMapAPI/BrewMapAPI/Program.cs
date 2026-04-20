using System.Text;
using BrewMapAPI.Data;
using BrewMapAPI.Models;
using BrewMapAPI.Repository.Auth;
using BrewMapAPI.Repository.Drinks;
using BrewMapAPI.Service.Auth;
using BrewMapAPI.Service.Drinks;
using BrewMapAPI.Repository.Locations;
using BrewMapAPI.Service.Location;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;

var builder = WebApplication.CreateBuilder(args);

// Configure strong-typed settings for MongoDB.
builder.Services.Configure<DatabaseSettings>(
    builder.Configuration.GetSection("DatabaseSettings"));

// Register MongoDB context
builder.Services.AddSingleton<MongoDbContext>();

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();

// Swagger + JWT implementation
builder.Services.AddSwaggerGen(options =>
{
    options.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        Name = "Authorization",
        Type = SecuritySchemeType.Http,
        Scheme = "Bearer",
        BearerFormat = "JWT",
        In = ParameterLocation.Header
    });
    options.AddSecurityRequirement(new OpenApiSecurityRequirement
    {
        {
            new OpenApiSecurityScheme
            {
                Reference = new OpenApiReference
                {
                    Type = ReferenceType.SecurityScheme,
                    Id = "Bearer"
                }
            },
            Array.Empty<string>()
        }
    });
});

// JWT Authentication
var secureKey = builder.Configuration["JWT:SecureKey"];
builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secureKey!)),
            ValidateIssuer = false,
            ValidateAudience = false
        };
    });

// Business level architecture
builder.Services.AddScoped<IAuthService, AuthService>();
builder.Services.AddScoped<IDrinkService, DrinkService>();
builder.Services.AddScoped<ILocationService, LocationService>();

// Data access level architecture
builder.Services.AddScoped<IAuthRepo, AuthRepo>();
builder.Services.AddScoped<IDrinkRepo, DrinkRepo>();
builder.Services.AddScoped<ILocationRepo, LocationRepo>();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();
app.Run();
