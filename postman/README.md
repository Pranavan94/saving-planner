# Saving Planner Postman Notes

## Import order

1. Import `saving-planner.postman_collection.json`
2. Import `saving-planner.local.postman_environment.json`
3. Select the imported environment in Postman
4. Set `loginPassword` in the environment to match your local `.env` `BOOTSTRAP_ADMIN_PASSWORD`
5. Run `Auth > Login (bootstrap admin or registered user)` first

The login request stores these values automatically into the environment:

- `accessToken`
- `tokenType`
- `userId`
- `email`
- `role`
- `expiresIn`

## Auth model

The API uses **JWT Bearer authentication**.

- `POST /api/v1/auth/login` is public
- `POST /api/v1/users/user` is public
- most other endpoints require `Authorization: Bearer <token>`

The included collection already applies Bearer auth to the protected user requests.

## Base URLs

Use the base URL that matches how the backend is running:

- local Gradle / IntelliJ run: `http://localhost:8080`
- Docker Compose app run: `http://localhost:8081`

## Recommended request order

### Bootstrap admin flow

1. `POST /api/v1/auth/login`
2. `GET /api/v1/users`
3. `GET /api/v1/users/{userId}`

### Public registration flow

1. `POST /api/v1/users/user`
2. Update `loginEmail` / `loginPassword` in the Postman environment if you want to log in as that newly registered user
3. `POST /api/v1/auth/login`
4. `GET /api/v1/users/{userId}`

### Users

1. `POST /api/v1/users/user`
2. `GET /api/v1/users`
3. `GET /api/v1/users/{userId}`
4. `GET /api/v1/users/allinfo/{userId}`
5. `PUT /api/v1/users/{userId}`
6. `DELETE /api/v1/users/{userId}`

### Personal Finance

1. `POST /api/v1/finance/overview/create`
2. `GET /api/v1/finance/overview`
3. `GET /api/v1/finance/overview/{financeId}`
4. `PUT /api/v1/finance/overview/{financeId}`
5. `DELETE /api/v1/finance/overview/{financeId}`

## Notes

- `saving-planner.local.postman_environment.json` intentionally leaves `loginPassword` blank so you can fill it from your ignored local `.env` file instead of committing credentials.
- User creation returns a JSON message wrapper.
- Some other write endpoints return plain success strings.
- `CreateUserRequest` accepts `password` and alias `passwordHash`.
- `CreateUserRequest` accepts `telephoneNumber` and alias `phoneNumber`.
- Finance create requests expect `monthlyExpenses.insurances` and `monthlyExpenses.subscriptions` as arrays.
- The included `react-login-example.md` matches the same JWT login flow and local Docker base URL.
