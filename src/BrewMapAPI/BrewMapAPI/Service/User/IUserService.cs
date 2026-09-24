using BrewMapAPI.DTO.User;

namespace BrewMapAPI.Service.User
{
    public interface IUserService
    {
        Task<MyUserProfileRead?> GetMyProfile(string userId);
        Task<StrangerUserProfileRead?> GetUserById(string id);
        Task<UserResponce> UpdateEmail(string userId, UpdateEmail dto);
        Task<UserResponce> UpdatePassword(string userId, UpdatePassword dto);
        Task<UserResponce> DeleteMyAccount(string userId);
        Task<UserDataExport?> ExportMyData(string userId);
    }
}
