using System.Text;
using BrewMapAPI.Data;
using BrewMapAPI.Models;
using BrewMapAPI.Repository.Auth;
using BrewMapAPI.Repository.Drinks;
using BrewMapAPI.Service.Auth;
using BrewMapAPI.Service.Drinks;
<<<<<<< Ruva
using BrewMapAPI.Service.Pins;
using BrewMapAPI.Repository.Pins;
using BrewMapAPI.Service.Review;
using Swashbuckle.AspNetCore.SwaggerGen;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using BrewMapAPI.Repository.Reviews;
using System.IdentityModel.Tokens.Jwt;
using Microsoft.IdentityModel.Tokens;
using System.Security.Claims;
=======
using BrewMapAPI.Repository.Locations;
using BrewMapAPI.Service.Location;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
>>>>>>> main

using System.Text;
using Microsoft.OpenApi.Models;     
var builder = WebApplication.CreateBuilder(args);

<<<<<<< Ruva
var jwtIssuer = builder.Configuration["JWT:Issuer"];
var jwtAudience = builder.Configuration["JWT:Audience"];
var jwtSecretKey = builder.Configuration["JWT:SecureKey"];

// Configure strong-typed settings for MongoDB
=======
// Configure strong-typed settings for MongoDB.
>>>>>>> main
builder.Services.Configure<DatabaseSettings>(
    builder.Configuration.GetSection("DatabaseSettings"));

// Register MongoDB context
builder.Services.AddSingleton<MongoDbContext>();

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();

<<<<<<< Ruva
builder.Services.AddSwaggerGen(c =>
{
    c.SwaggerDoc("v1", new OpenApiInfo { Title = "BrewMapAPI", Version = "v1" });
    c.AddSecurityDefinition("Bearer", new OpenApiSecurityScheme
    {
        Description = @"JWT Authorization header using the Bearer scheme. 
                        Enter 'Bearer' [space] and then your token in the text input below.
                        Example: 'Bearer 12345abcdef'",
        Name = "Authorization",
        In = ParameterLocation.Header,
        Type = SecuritySchemeType.ApiKey,
        Scheme = "Bearer"
    });
    c.AddSecurityRequirement(new OpenApiSecurityRequirement()
=======
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
>>>>>>> main
    {
        {
            new OpenApiSecurityScheme
            {
                Reference = new OpenApiReference
                {
                    Type = ReferenceType.SecurityScheme,
                    Id = "Bearer"
<<<<<<< Ruva
                },
                Scheme = "oauth2",
                Name = "Bearer",
                In = ParameterLocation.Header,
            },
            new List<string>()
        }
    });
});

=======
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
>>>>>>> main

// Business level architecture
builder.Services.AddScoped<IAuthService, AuthService>();
builder.Services.AddScoped<IDrinkService, DrinkService>();
<<<<<<< Ruva
builder.Services.AddScoped<IReviewService, ReviewService>();
=======
builder.Services.AddScoped<ILocationService, LocationService>();
>>>>>>> main

// Data access level architecture
builder.Services.AddScoped<IAuthRepo, AuthRepo>();
builder.Services.AddScoped<IDrinkRepo, DrinkRepo>();
<<<<<<< Ruva
builder.Services.AddScoped<IReviewRepo, ReviewRepo>();

builder.Services.AddScoped<IPinRepo, PinRepo>();
builder.Services.AddScoped<IPinService, PinService>();

builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuer = true,
            ValidateAudience = true,
            ValidateIssuerSigningKey = true,
            ValidIssuer = jwtIssuer,
            ValidAudience = jwtAudience,
            IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(jwtSecretKey))
        };
    });
=======
builder.Services.AddScoped<ILocationRepo, LocationRepo>();
>>>>>>> main

var app = builder.Build();


if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}
<<<<<<< Ruva

=======
>>>>>>> main
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();
app.Run();
