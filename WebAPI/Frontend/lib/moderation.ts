import api from "./api";

export type ModerationUpdate =
  | { suspended: boolean }
  | { role: "admin" | "user" };

export const updateUserModeration = async (
  userId: string,
  data: ModerationUpdate
) => {
  return api.patch(`/api/Moderation/user/${userId}`, data);
};