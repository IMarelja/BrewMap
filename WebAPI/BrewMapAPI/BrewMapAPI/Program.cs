using BrewMapAPI.Models;       
using BrewMapAPI.Service.User;     
using BrewMapAPI.Data;
using Microsoft.Extensions.Options;

var builder = WebApplication.CreateBuilder(args);

// Configure strong-typed settings for MongoDB
builder.Services.Configure<DatabaseSettings>(
    builder.Configuration.GetSection("DatabaseSettings"));

// Register MongoDB service(s)
builder.Services.AddScoped<MongoDbContext>();  
builder.Services.AddScoped<UserService>(); 

// Add services to the container.
builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

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