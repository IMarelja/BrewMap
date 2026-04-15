using BrewMapAPI.Models;       
using BrewMapAPI.Service.User;     
using BrewMapAPI.Data;
using Microsoft.Extensions.Options;
using BrewMapAPI.Repository.Drinks;
using BrewMapAPI.Service.Drinks;
using BrewMapAPI.Repository.Locations;
using BrewMapAPI.Service.Location;

var builder = WebApplication.CreateBuilder(args);

// Configure strong-typed settings for MongoDB.
builder.Services.Configure<DatabaseSettings>(
    builder.Configuration.GetSection("DatabaseSettings"));

// Register MongoDB context
builder.Services.AddSingleton<MongoDbContext>();  

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

// Business level architecture
builder.Services.AddScoped<IDrinkService, DrinkService>();
builder.Services.AddScoped<ILocationService, LocationService>();

// Data access level architecture
builder.Services.AddScoped<IDrinkRepo, DrinkRepo>();
builder.Services.AddScoped<ILocationRepo, LocationRepo>();

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
