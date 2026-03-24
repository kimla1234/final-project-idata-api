# 📚 iData API Documentation

## 📖 Introduction

**iData** is an open-source mockup API with enormous datasets integration for even beginner developers. For a better understanding of our topic, Let us first understand the concept of an API


## ⚙️ Installation

To use the Invoice API, you need to have an active instance of the Inovice service running. Ensure you have the tokens for authentication. You can use tools like Postman to test the API endpoints.

## 🚀 Usage

The Invoice API uses RESTful principles and supports standard HTTP methods such as GET, POST, PUT, PATCH, and DELETE. All endpoints require the base URL ` https://api.idata.fit`, which should be replaced with the actual base URL of your Invoice instance. We will provide you with the Postman Collection and Environment to get started. 

## 📬 Endpoints

### 👤 User

| Endpoint          | Method | URL                                     | Description |
|-------------------|--------|-----------------------------------------|-------------|
| Find User Profile | GET    | `/api/v1/users/me`     `                | Retrieves the authenticated user's profile information. |
| Update User       | PATCH  | `/api/v1/users/me`                    | Updates user information based on the provided token |


### 🔐 Auth

| Endpoint              | Method | URL                                      | Description |
|-----------------------|--------|------------------------------------------|-------------|
| Register              | POST   | `/api/v1/auth/register`                  | Registers a new user. |
| Login                 | POST   | `/api/v1/auth/login`                 | Authenticates a user and returns a JWT token. |
| Refresh Token         | POST   | `/api/v1/auth/refresh-token`            | Refreshes the authentication token using a refresh token. |
| Change Password       | POST   | `/api/v1/setting/password`                   | Changes the user's password. |

### 🖼️ Media

| Endpoint     | Method | URL                                    | Description |
|--------------|--------|----------------------------------------|-------------|
| Upload Image | POST   | `api/v1/media/upload-image`                   | Uploads an image file. |

### Workspace

| Endpoint     | Method | URL                                      | Description |
|--------------|--------|------------------------------------------|-------------|
| Create Workspace      | POST   | `/api/v1/workspaces`          | Create a new project workspace  |
| Get My Workspaces      | GET   | `/api/v1/workspaces/my`          | List all workspaces owned by the user   |
| Get Workspace By ID      | GET   | `/api/v1/workspaces/{id}`          | Retrieve specific workspace details   |
| Update Workspace    | PATCH  | `/api/v1/workspaces/{id}`          | Update workspace name or settings    |
| Delete Workspace	     | DELETE  | `/api/v1/workspaces/{id}`          | Permanently remove a workspace  |
| Invite Member   | POST  | `/api/v1/workspaces/{id}/invite`          | Invite a collaborator via email |
| Join Workspace  | POST  | `/api/v1/workspaces/{id}/join`          | Accept an invitation to join |
| Get Members | GET  | `/api/v1/workspaces/{id}/members`          | List all members in the workspace |
| Update Role  | PATCH  | `/api/v1/workspaces/{id}/members/{mId}/role`          | Change member permissions (Admin/Editor) |
| Remove Member  | DELETE  | `/api/v1/workspaces/{id}/members/{mId}`          | Remove a collaborator from workspace |


### 📂 Folder

| Endpoint              | Method | URL                                          | Description |
|-----------------------|--------|----------------------------------------------|-------------|
|Create Folder | `/api/v1/workspaces/{wId}/folders`                     | Create a new category in a workspace |
| Get All Folders | GET    | `/api/v1/workspaces/{wId}/folders`     | Retrieve all folders for a specific workspace  |
| Update Folder   | PUT | `/api/v1/workspaces/{wId}/folders/{fId}`                   | Rename or modify folder metadata |
| Delete Folder | DELETE| `/api/v1/workspaces/{wId}/folders/{fId}`                       | Remove a folder (and its references)  |


### API Scheme

| Endpoint     | Method | URL                                                | Description |
|--------------|--------|----------------------------------------------------|-------------|
| Create Scheme | POST    | `/api/v1/api-schemes`| Define a new API structure (Auth/Data)  |
| Get by Folder | GET    | `/api/v1/api-schemes/folder/{fId}`| List all schemes inside a specific folder  |
| Get Detail | GET    | `/api/v1/api-schemes/{id}`| Retrieve full configuration of a scheme  |
| Update Scheme  | PUT    | `/api/v1/api-schemes/{id}`| Modify name, fields, or definitions  |
| Delete Scheme  | DELETE    | `/api/v1/api-schemes/{id}`| Permanently remove a scheme |
| Publish/Toggle | PATCH    | `/api/v1/api-schemes/{id}/publish`| Make API public for the Community Feed|
| Community Feed | GET    | `/api/v1/api-schemes/public/feed`| Browse shared APIs (Supports search/paging) |
| Fork API | POST    | `/api/v1/api-schemes/{id}/fork`| Copy a public API into your own folder |

### 📜 Intelligent Generation

| Endpoint     | Method | URL                                                   | Description |
|--------------|--------|-------------------------------------------------------|-------------|
| Preview File  | POST   | `/api/v1/generate-file/preview`                                        | Extract schema fields from a MultipartFile |
| Generate via File | POST| `/api/v1/generate-file/generate-from-file` |Persist a generated schema into a workspace |
| Generate via Prompt | POST| `/api/v1/generate-file/generate-from-prompt` |Create a JSON schema using AI/NLP prompts |

### AI Mock 

| Endpoint            | Method | URL                                                   | Description                                       |
|---------------------|--------|-------------------------------------------------------|---------------------------------------------------|
| Generate Mock Data   | POST   | `/api/v1/ai/mock/{id}`                                             |Generate and save data into a specific scheme                             |




## How test api 

1. Download/fork/clone the repo and Once you're in the correct directory, it's time to install all the necessary dependencies. You can do this by typing the following command:

```
git clone https://github.com/kimla1234/final-project-idata-api
```


2. Check your Run/Debug configurations
   Active profile u can test :
   - dev  : for development  test in localhost
   - test : for production with hosting databse , file uplaod 

3. Run project



## Project workflow test 
1. Register or login
2. Dashboard -> workspace -> generate API 
3. Test API endpoint 

## Test File Upload 
- `{{idata_base_url_production}}/api/v1/media/upload-image`
  => `body` => `form-data` => `key` : file , `type` : file  => upload your image in  `value` 
  ````
   {
    "name": "4895c5a9-1b01-4507-8ee8-f0e6d27a44c9.jpg",
    "contentType": "image/jpeg",
    "uri": "https://api.idata.fit/media/IMAGE/4895c5a9-1b01-4507-8ee8-f0e6d27a44c9.jpg",
    "size": 97814,
    "extension": "jpg"
    }
  ````
- Then you can take the uri and put it wherever you want to upload it. Example create product :
  ```
   {
   "name": "Coca 01",
    "image_url": "https://api.idata.fit/media/IMAGE/4895c5a9-1b01-4507-8ee8-f0e6d27a44c9.jpg",
   "price": 1.25,
   "productTypeId": 1,
   "quantity": 500 
   }
  ```
 

## 🛠️ Troubleshooting

If you encounter issues while using the Invoice API, consider the following steps:
- Ensure your request URLs are correct and the base URL is properly set.
- Check your tokens for validity and expiration.
- Verify the request body and headers match the expected format.
- Refer to the response messages for specific error details.

## 👥 Contributors

- iDATA
- ADITI & Wing Bank 
