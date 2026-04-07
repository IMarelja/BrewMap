using BrewMapAPI.Models;       
using BrewMapAPI.Service.User;     
using BrewMapAPI.Data;
using BrewMapAPI.Repository.Auth;
using Microsoft.Extensions.Options;
using BrewMapAPI.Repository.Drinks;
using BrewMapAPI.Service.Auth;
using BrewMapAPI.Service.Drinks;

var builder = WebApplication.CreateBuilder(args);

// Configure strong-typed settings for MongoDB
builder.Services.Configure<DatabaseSettings>(
    builder.Configuration.GetSection("DatabaseSettings"));

// Register MongoDB context
builder.Services.AddSingleton<MongoDbContext>();  

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Business level architecture
builder.Services.AddScoped<IAuthService, AuthService>();
builder.Services.AddScoped<IDrinkService, DrinkService>();

// Data access level architecture
builder.Services.AddScoped<IAuthRepo, AuthRepo>();
builder.Services.AddScoped<IDrinkRepo, DrinkRepo>();

var app = builder.Build();

// Configure the HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}
app.UseAuthorization();
app.MapControllers();
app.Run();