using System.Text;
using BrewMapAPI.Data;
using BrewMapAPI.Email;
using BrewMapAPI.Models;
using BrewMapAPI.Repository.Auth;
using BrewMapAPI.Repository.Drinks;
using BrewMapAPI.Service.Auth;
using BrewMapAPI.Service.Drinks;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi.Models;
using BrewMapAPI.Service.Review;
using BrewMapAPI.Repository.Reviews;
using BrewMapAPI.Service.Pins;
using BrewMapAPI.Repository.Pins;
using BrewMapAPI.Repository.Locations;
using BrewMapAPI.Service.Location;
using BrewMapAPI.Repository.Categories;
using BrewMapAPI.Repository.PaymentOptions;
using BrewMapAPI.Service.Flags;
using BrewMapAPI.Repository.Flags;
using BrewMapAPI.Service.Moderation;
using BrewMapAPI.Repository.Moderation;
using BrewMapAPI.Service.User;
using BrewMapAPI.Repository.Users;
using BrewMapAPI.Service.Category;
using BrewMapAPI.Service.PaymentOption;

var builder = WebApplication.CreateBuilder(args);

// Configure strong-typed settings for MongoDB
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

builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowFrontend", policy =>
    {
        policy.WithOrigins(
            "http://localhost:3000",
            "http://localhost:5239",
            "http://localhost:7000"
        )
        .AllowAnyHeader()
        .AllowAnyMethod()
        .AllowCredentials();
    });
});

// JWT Authentication
var secureKey = builder.Configuration["Jwt:SecureKey"];
var issuer = builder.Configuration["Jwt:Issuer"];
var audience = builder.Configuration["Jwt:Audience"];

builder.Services.AddAuthentication(JwtBearerDefaults.AuthenticationScheme)
    .AddJwtBearer(options =>
    {
        options.TokenValidationParameters = new TokenValidationParameters
        {
            ValidateIssuerSigningKey = true,
            IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secureKey!)),
            
            ValidateIssuer = true,
            ValidIssuer = issuer,

            ValidateAudience = true,
            ValidAudience = audience
        };
    });


var emailConfig = builder.Configuration
    .GetSection("EmailConfiguration")
    .Get<EmailConfiguration>();
builder.Services.AddSingleton(emailConfig);
builder.Services.AddScoped<IEmailSender, EmailSender>();

// Business level architecture
builder.Services.AddScoped<IAuthService, AuthService>();
builder.Services.AddScoped<IDrinkService, DrinkService>();
builder.Services.AddScoped<IReviewService, ReviewService>();
builder.Services.AddScoped<ILocationService, LocationService>();
builder.Services.AddScoped<IPinService, PinService>();
builder.Services.AddScoped<IUserService, UserService>();
builder.Services.AddScoped<IFlagService, FlagService>();
builder.Services.AddScoped<IModerationService, ModerationService>();
builder.Services.AddScoped<ICategoryService, CategoryService>();
builder.Services.AddScoped<IPaymentOptionService, PaymentOptionService>();

// Data access level architecture
builder.Services.AddScoped<IAuthRepo, AuthRepo>();
builder.Services.AddScoped<IDrinkRepo, DrinkRepo>();
builder.Services.AddScoped<IReviewRepo, ReviewRepo>();
builder.Services.AddScoped<ILocationRepo, LocationRepo>();
builder.Services.AddScoped<IPinRepo, PinRepo>();
builder.Services.AddScoped<IUserRepo, UserRepo>();
builder.Services.AddScoped<ICategoryRepo, CategoryRepo>();
builder.Services.AddScoped<IPaymentOptionRepo, PaymentOptionRepo>();
builder.Services.AddScoped<IUserRepo, UserRepo>();
builder.Services.AddScoped<IFlagRepo, FlagRepo>();
builder.Services.AddScoped<IModerationRepo, ModerationRepo>();
builder.Services.AddScoped<ICategoryRepo, CategoryRepo>();
builder.Services.AddScoped<IPaymentOptionRepo, PaymentOptionRepo>();

var app = builder.Build();


if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}
app.UseCors("AllowFrontend");
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();
app.Run();