using BrewMapAPI.Models;       
using BrewMapAPI.Service.User;     
using BrewMapAPI.Data;
using Microsoft.Extensions.Options;
using BrewMapAPI.Repository.Drinks;
using BrewMapAPI.Service.Drinks;
using BrewMapAPI.Service.Flags;
using BrewMapAPI.Repository.Flags;


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
builder.Services.AddScoped<IDrinkService, DrinkService>();
builder.Services.AddScoped<IFlagService, FlagService>();


// Data access level architecture
builder.Services.AddScoped<IDrinkRepo, DrinkRepo>();
builder.Services.AddScoped<IFlagRepo, FlagRepo>();

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