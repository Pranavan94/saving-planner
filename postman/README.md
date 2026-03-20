# Saving Planner Postman Files

## Files

- `saving-planner.postman_collection.json` — API requests for users and personal finance
- `saving-planner.local.postman_environment.json` — local variables for `http://localhost:8080`

## Import steps

1. Open Postman.
2. Click **Import**.
3. Import both JSON files from this folder.
4. Select the **Saving Planner Local** environment.
5. Start the Spring Boot app.

## Current default auth

The collection uses collection-level **Basic Auth** with environment variables:

- username: `admin`
- password: `password`

If you change the credentials in `SecurityConfig`, update the Postman environment as well.

## Recommended request order

### Users

1. `Create User`
2. `Get All Users`
   - this stores the first returned `id` into `userId`
3. `Get User By Id`
4. `Get All User Info By Id`
5. `Update User`
6. `Delete User`

### Personal Finance

1. `Create Personal Finance Overview`
2. `Get All Personal Finance Overviews`
   - this stores the first returned `id` into `financeId`
3. `Get Personal Finance Overview By Id`

## Notes

- All endpoints require authentication.
- The create endpoints return a success string, not the generated entity ID.
- The `Get All ...` requests include a small test script that saves the first returned ID into the Postman environment.

