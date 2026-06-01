using BrewMapAPI.DTO.User;
using BrewMapAPI.Repository.Auth;
using BrewMapAPI.Repository.Reviews;
using BrewMapAPI.Repository.Users;
using BrewMapAPI.Security;

namespace BrewMapAPI.Service.User
{
    public class UserService : IUserService
    {
        private readonly IUserRepo _userRepo;
        private readonly IAuthRepo _authRepo;
        private readonly IReviewRepo _reviewRepo;

        public UserService(IUserRepo userRepo, IAuthRepo authRepo, IReviewRepo reviewRepo)
        {
            _userRepo = userRepo;
            _authRepo = authRepo;
            _reviewRepo = reviewRepo;
        }

        public async Task<MyUserProfileRead?> GetMyProfile(string userId)
        {
            var user = await _userRepo.GetUserById(userId);
            if (user == null) return null;

            return new MyUserProfileRead
            {
                Id = user.Id,
                Username = user.Username,
                Email = user.Email
            };
        }

        public async Task<StrangerUserProfileRead?> GetUserById(string id)
        {
            var user = await _userRepo.GetUserById(id);
            if (user == null) return null;

            return new StrangerUserProfileRead
            {
                Id = user.Id,
                Username = user.IsDeleted ? "Deleted user" : user.Username
            };
        }

        public async Task<UserResponce> UpdateEmail(string userId, UpdateEmail dto)
        {
            var user = await _userRepo.GetUserById(userId);
            if (user == null)
                return new UserResponce { StatusCode = 404, Message = "User not found." };

            var currentHash = PasswordHashProvider.GetHash(dto.CurrentPassword, user.PasswordSalt);
            if (currentHash != user.PasswordHash)
                return new UserResponce { StatusCode = 401, Message = "Incorrect password." };

            if (user.Email == dto.NewEmail)
                return new UserResponce { StatusCode = 400, Message = "That is already your email." };

            var existing = await _authRepo.GetByEmail(dto.NewEmail);
            if (existing != null)
                return new UserResponce { StatusCode = 409, Message = "Email is already in use." };

            await _userRepo.UpdateEmail(userId, dto.NewEmail);
            return new UserResponce { StatusCode = 200, Success = true, Message = "Email updated." };
        }

        public async Task<UserResponce> UpdatePassword(string userId, UpdatePassword dto)
        {
            var user = await _userRepo.GetUserById(userId);
            if (user == null)
                return new UserResponce { StatusCode = 404, Message = "User not found." };

            var currentHash = PasswordHashProvider.GetHash(dto.CurrentPassword, user.PasswordSalt);
            if (currentHash != user.PasswordHash)
                return new UserResponce { StatusCode = 401, Message = "Incorrect current password." };

            var newSalt = PasswordHashProvider.GetSalt();
            var newHash = PasswordHashProvider.GetHash(dto.NewPassword, newSalt);

            await _userRepo.UpdatePassword(userId, newHash, newSalt);
            return new UserResponce { StatusCode = 200, Success = true, Message = "Password updated." };
        }

        public async Task<UserResponce> DeleteMyAccount(string userId)
        {
            var user = await _userRepo.GetUserById(userId);
            if (user == null)
                return new UserResponce { StatusCode = 404, Message = "User not found." };

            await _userRepo.DeleteUser(userId);
            return new UserResponce { StatusCode = 200, Success = true, Message = "Account deleted." };
        }

        public async Task<UserDataExport?> ExportMyData(string userId)
        {
            var user = await _userRepo.GetUserById(userId);
            if (user == null) return null;

            var reviews = await _reviewRepo.GetByUserId(userId);

            return new UserDataExport
            {
                Id = user.Id,
                Username = user.Username,
                Email = user.Email,
                Role = user.Role,
                CreatedAt = user.CreatedAt,
                LastLoginAt = user.LastLoginAt,
                Reviews = reviews.Select(r => new ExportedReview
                {
                    Id = r.Id,
                    TargetType = r.Target.Type,
                    TargetId = r.Target.TargetId,
                    Rating = r.Rating,
                    Comment = r.Comment,
                    CreatedAt = r.CreatedAt,
                    UpdatedAt = r.UpdatedAt
                }).ToList()
            };
        }
    }
}
