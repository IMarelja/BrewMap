package hr.algebra.mobileapp.models

/**
 * Mirrors `ReadCategory` DTO from the BrewMap API.
 *
 * Returned by `GET api/Category` (list) and `GET api/Category/{tag}` (single).
 * The [tag] field is the primary key used in location's `categoryTag` field.
 */
data class Category(
    val tag: String,
    val name: String
)
