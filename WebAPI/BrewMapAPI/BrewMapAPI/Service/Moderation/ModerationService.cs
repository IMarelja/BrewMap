using BrewMapAPI.DTO.Moderation;
using BrewMapAPI.Repository.Drinks;
using BrewMapAPI.Repository.Locations;
using BrewMapAPI.Repository.Moderation;
using BrewMapAPI.Repository.Reviews;

namespace BrewMapAPI.Service.Moderation
{
    public class ModerationService : IModerationService
    {
        private readonly IModerationRepo _repo;

        public ModerationService(
            IModerationRepo repo)
        {
            _repo = repo;

        }

        //User management
        public async Task<ModeratedUserInfo?> GetUserInfo(string userId)
        {
            var user = await _repo.GetUserById(userId);

            if (user == null)
                return null;

            return new ModeratedUserInfo
            {
                Id = user.Id,
                Username = user.Username,
                Email = user.Email,
                Role = user.Role,
                IsActive = user.IsActive,
                CreatedAt = user.CreatedAt
            };
        }

        public async Task<ModerationResult> UpdateUser(string userId, UpdateUserModerationRequest request)
        {
            var user = await _repo.GetUserById(userId);

            if (user == null)
                return new ModerationResult { Success = false, Message = $"User with ID {userId} not found." };

            if (request.Suspended == null && request.Role == null)
                return new ModerationResult { Success = false, Message = "No changes requested." };

            if (request.Suspended == true)
            {
                if (!user.IsActive)
                    return new ModerationResult { Success = false, Message = $"User {user.Username} is already suspended." };
                if (user.Role == "admin")
                    return new ModerationResult { Success = false, Message = "Cannot suspend an admin user. Revoke admin privileges first." };

                var result = await _repo.UpdateUserIsActive(userId, false);
                if (result == null)
                    return new ModerationResult { Success = false, Message = "Failed to suspend user." };

                return new ModerationResult { Success = true, Message = $"User {user.Username} has been suspended." };
            }

            if (request.Suspended == false)
            {
                if (user.IsActive)
                    return new ModerationResult { Success = false, Message = $"User {user.Username} is not suspended." };

                var result = await _repo.UpdateUserIsActive(userId, true);
                if (result == null)
                    return new ModerationResult { Success = false, Message = "Failed to unsuspend user." };

                return new ModerationResult { Success = true, Message = $"User {user.Username} has been unsuspended and restored to regular user." };
            }

            if (request.Role == "admin")
            {
                if (user.Role == "admin")
                    return new ModerationResult { Success = false, Message = $"User {user.Username} is already an admin." };
                if (!user.IsActive)
                    return new ModerationResult { Success = false, Message = "Cannot grant admin to a suspended user. Unsuspend them first." };

                var result = await _repo.UpdateUserRole(userId, "admin");
                if (result == null)
                    return new ModerationResult { Success = false, Message = "Failed to grant admin privileges." };

                return new ModerationResult { Success = true, Message = $"User {user.Username} has been granted admin privileges." };
            }

            if (request.Role == "user")
            {
                if (user.Role == "user")
                    return new ModerationResult { Success = false, Message = $"User {user.Username} is already a regular user." };
                if (!user.IsActive)
                    return new ModerationResult { Success = false, Message = $"User {user.Username} is suspended. Unsuspend them first." };

                var result = await _repo.UpdateUserRole(userId, "user");
                if (result == null)
                    return new ModerationResult { Success = false, Message = "Failed to revoke admin privileges." };

                return new ModerationResult { Success = true, Message = $"Admin privileges have been revoked from user {user.Username}." };
            }

            return new ModerationResult { Success = false, Message = "Invalid request. Provide 'suspended' (bool) or 'role' (\"admin\" | \"user\")." };
        }

        // public async Task<ModerationResult> SuspendUser(string userId)
        // {
        //     var user = await _repo.GetUserById(userId);
        //     if (user == null)
        //         return new ModerationResult { Success = false, Message = $"User with ID {userId} not found." };
        //     if (user.Role == "suspended")
        //         return new ModerationResult { Success = false, Message = $"User {user.Username} is already suspended." };
        //     if (user.Role == "admin")
        //         return new ModerationResult { Success = false, Message = "Cannot suspend an admin user. Please revoke admin privileges first." };
        //     var suspended = await _repo.UpdateUserRole(userId, "suspended");
        //     if (suspended == null)
        //         return new ModerationResult { Success = false, Message = "Failed to suspend user." };
        //     return new ModerationResult { Success = true, Message = $"User {user.Username} has been suspended." };
        // }

        // public async Task<ModerationResult> UnsuspendUser(string userId)
        // {
        //     var user = await _repo.GetUserById(userId);
        //     if (user == null)
        //         return new ModerationResult { Success = false, Message = $"User with ID {userId} not found." };
        //     if (user.Role != "suspended")
        //         return new ModerationResult { Success = false, Message = $"User {user.Username} is not suspended." };
        //     var unsuspended = await _repo.UpdateUserRole(userId, "user");
        //     if (unsuspended == null)
        //         return new ModerationResult { Success = false, Message = "Failed to unsuspend user." };
        //     return new ModerationResult { Success = true, Message = $"User {user.Username} has been unsuspended and restored to regular user." };
        // }

        // public async Task<ModerationResult> GrantAdminRole(string userId)
        // {
        //     var user = await _repo.GetUserById(userId);
        //     if (user == null)
        //         return new ModerationResult { Success = false, Message = $"User with ID {userId} not found." };
        //     if (user.Role == "admin")
        //         return new ModerationResult { Success = false, Message = $"User {user.Username} is already an admin." };
        //     if (user.Role == "suspended")
        //         return new ModerationResult { Success = false, Message = "Cannot grant admin privileges to a suspended user. Please unsuspend them first." };
        //     var granted = await _repo.UpdateUserRole(userId, "admin");
        //     if (granted == null)
        //         return new ModerationResult { Success = false, Message = "Failed to grant admin privileges." };
        //     return new ModerationResult { Success = true, Message = $"User {user.Username} has been granted admin privileges." };
        // }

        // public async Task<ModerationResult> RevokeAdminRole(string userId)
        // {
        //     var user = await _repo.GetUserById(userId);
        //     if (user == null)
        //         return new ModerationResult { Success = false, Message = $"User with ID {userId} not found." };
        //     if (user.Role != "admin")
        //         return new ModerationResult { Success = false, Message = $"User {user.Username} is not an admin." };
        //     var revoked = await _repo.UpdateUserRole(userId, "user");
        //     if (revoked == null)
        //         return new ModerationResult { Success = false, Message = "Failed to revoke admin privileges." };
        //     return new ModerationResult { Success = true, Message = $"Admin privileges have been revoked from user {user.Username}." };
        // }
    }
}