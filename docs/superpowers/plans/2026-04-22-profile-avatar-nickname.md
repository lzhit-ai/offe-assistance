# Profile Avatar And Nickname Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add editable user nicknames and local avatar uploads in the profile center while keeping `username` as the immutable login identifier.

**Architecture:** Extend the backend `User` model with a unique `nickname`, add one JSON profile update endpoint plus one multipart avatar upload endpoint, and expose uploaded avatars through a static resource mapping. On the frontend, add profile helpers and dedicated API calls, then update the navbar and profile page to prefer `nickname` over `username` and refresh the local user store after successful edits.

**Tech Stack:** Spring Boot 4, Spring Security, Spring Data JPA, MockMvc, Vue 3, Pinia, Element Plus, Node test runner

---

## File Map

### Backend files

- Modify: `backend/src/main/java/com/example/getoffer/entity/User.java`
- Modify: `backend/src/main/java/com/example/getoffer/repository/UserRepository.java`
- Modify: `backend/src/main/java/com/example/getoffer/dto/auth/UserProfileResponse.java`
- Create: `backend/src/main/java/com/example/getoffer/dto/user/UpdateProfileRequest.java`
- Create: `backend/src/main/java/com/example/getoffer/dto/user/AvatarUploadResponse.java`
- Create: `backend/src/main/java/com/example/getoffer/service/user/UserProfileService.java`
- Modify: `backend/src/main/java/com/example/getoffer/service/auth/AuthService.java`
- Modify: `backend/src/main/java/com/example/getoffer/controller/UserController.java`
- Create: `backend/src/main/java/com/example/getoffer/config/StaticResourceConfig.java`
- Modify: `backend/src/main/resources/application.properties`
- Modify: `backend/src/main/java/com/example/getoffer/common/GlobalExceptionHandler.java`
- Modify: `backend/src/test/java/com/example/getoffer/controller/UserControllerTest.java`
- Create: `backend/src/test/java/com/example/getoffer/service/user/UserProfileServiceTest.java`

### Frontend files

- Modify: `getoffer/src/api/transformers.js`
- Modify: `getoffer/src/api/frontend.js`
- Create: `getoffer/src/utils/user-profile.js`
- Modify: `getoffer/src/components/Navbar.vue`
- Modify: `getoffer/src/views/Profile.vue`
- Create: `getoffer/tests/user-profile.test.js`

### Repo metadata

- Modify: `.gitignore`

## Task 1: Add backend nickname model and profile update flow

**Files:**
- Modify: `backend/src/main/java/com/example/getoffer/entity/User.java`
- Modify: `backend/src/main/java/com/example/getoffer/repository/UserRepository.java`
- Modify: `backend/src/main/java/com/example/getoffer/dto/auth/UserProfileResponse.java`
- Create: `backend/src/main/java/com/example/getoffer/dto/user/UpdateProfileRequest.java`
- Create: `backend/src/main/java/com/example/getoffer/service/user/UserProfileService.java`
- Modify: `backend/src/main/java/com/example/getoffer/service/auth/AuthService.java`
- Modify: `backend/src/main/java/com/example/getoffer/controller/UserController.java`
- Modify: `backend/src/test/java/com/example/getoffer/controller/UserControllerTest.java`
- Create: `backend/src/test/java/com/example/getoffer/service/user/UserProfileServiceTest.java`

- [ ] **Step 1: Write the failing service tests for nickname updates**

```java
@Test
void updateProfileStoresTrimmedNickname() {
    User user = new User();
    user.setId(1L);
    user.setUsername("login_name");
    user.setNickname("old_name");

    when(userRepository.findByUsername("login_name")).thenReturn(Optional.of(user));
    when(userRepository.existsByNicknameAndIdNot("new_name", 1L)).thenReturn(false);
    when(userRepository.save(user)).thenAnswer(invocation -> invocation.getArgument(0));

    UserProfileResponse response = service.updateProfile("login_name", new UpdateProfileRequest(" new_name "));

    assertThat(user.getNickname()).isEqualTo("new_name");
    assertThat(response.getNickname()).isEqualTo("new_name");
}

@Test
void updateProfileRejectsDuplicateNickname() {
    User user = new User();
    user.setId(1L);
    user.setUsername("login_name");

    when(userRepository.findByUsername("login_name")).thenReturn(Optional.of(user));
    when(userRepository.existsByNicknameAndIdNot("taken_name", 1L)).thenReturn(true);

    assertThatThrownBy(() -> service.updateProfile("login_name", new UpdateProfileRequest("taken_name")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("nickname");
}

@Test
void updateProfileRejectsNicknameLongerThanTenCharacters() {
    User user = new User();
    user.setId(1L);
    user.setUsername("login_name");

    when(userRepository.findByUsername("login_name")).thenReturn(Optional.of(user));

    assertThatThrownBy(() -> service.updateProfile("login_name", new UpdateProfileRequest("12345678901")))
            .isInstanceOf(IllegalArgumentException.class);
}
```

- [ ] **Step 2: Run the backend service test to verify it fails**

Run: `./mvnw.cmd -Dtest=UserProfileServiceTest test`

Expected: FAIL with missing symbols such as `nickname`, `UpdateProfileRequest`, or `updateProfile`.

- [ ] **Step 3: Implement the minimal nickname model changes**

```java
// User.java
@Column(length = 50, unique = true)
private String nickname;

public String getNickname() {
    return nickname;
}

public void setNickname(String nickname) {
    this.nickname = nickname;
}

// UserRepository.java
boolean existsByNicknameAndIdNot(String nickname, Long id);

// UserProfileResponse.java
private String nickname;

public String getNickname() {
    return nickname;
}

public void setNickname(String nickname) {
    this.nickname = nickname;
}
```

```java
// UpdateProfileRequest.java
public class UpdateProfileRequest {

    private String nickname;

    public UpdateProfileRequest() {
    }

    public UpdateProfileRequest(String nickname) {
        this.nickname = nickname;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
```

```java
// AuthService.java
public UserProfileResponse toUserProfile(User user) {
    UserProfileResponse response = new UserProfileResponse();
    response.setId(user.getId());
    response.setUsername(user.getUsername());
    response.setNickname(user.getNickname());
    response.setPhone(user.getPhone());
    response.setEmail(user.getEmail());
    response.setAvatar(user.getAvatar());
    response.setRegisterTime(user.getRegisterTime());
    response.setStats(new UserStatsResponse(
            articleRepository.countByAuthorId(user.getId()),
            favoriteRepository.countByUserId(user.getId()),
            0
    ));
    return response;
}
```

```java
// UserProfileService.java
public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("user not found"));

    String nickname = normalizeNickname(request.getNickname());
    validateNickname(nickname, user.getId());
    user.setNickname(nickname);

    return authService.toUserProfile(userRepository.save(user));
}
```

- [ ] **Step 4: Add the controller endpoint and controller tests**

```java
// UserController.java
@PatchMapping("/profile")
public ApiResponse<UserProfileResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    return ApiResponse.success(userProfileService.updateProfile(username, request));
}
```

```java
// UserControllerTest.java
@Test
void updateProfileReturnsUpdatedNickname() throws Exception {
    String token = TestAuthHelper.registerAndGetToken(mockMvc, "writer3", "13800138011", "123456");

    mockMvc.perform(patch("/api/v1/users/me/profile")
                    .header("Authorization", "Bearer " + token)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                              "nickname": "new_name"
                            }
                            """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.username").value("writer3"))
            .andExpect(jsonPath("$.data.nickname").value("new_name"));
}
```

- [ ] **Step 5: Run the focused backend tests to verify they pass**

Run: `./mvnw.cmd -Dtest=UserProfileServiceTest,UserControllerTest test`

Expected: PASS with nickname update coverage green.

- [ ] **Step 6: Commit the backend nickname slice**

```bash
git add backend/src/main/java/com/example/getoffer/entity/User.java backend/src/main/java/com/example/getoffer/repository/UserRepository.java backend/src/main/java/com/example/getoffer/dto/auth/UserProfileResponse.java backend/src/main/java/com/example/getoffer/dto/user/UpdateProfileRequest.java backend/src/main/java/com/example/getoffer/service/user/UserProfileService.java backend/src/main/java/com/example/getoffer/service/auth/AuthService.java backend/src/main/java/com/example/getoffer/controller/UserController.java backend/src/test/java/com/example/getoffer/controller/UserControllerTest.java backend/src/test/java/com/example/getoffer/service/user/UserProfileServiceTest.java
git commit -m "feat: add nickname profile update"
```

## Task 2: Add backend avatar upload and static file serving

**Files:**
- Create: `backend/src/main/java/com/example/getoffer/dto/user/AvatarUploadResponse.java`
- Modify: `backend/src/main/java/com/example/getoffer/service/user/UserProfileService.java`
- Modify: `backend/src/main/java/com/example/getoffer/controller/UserController.java`
- Create: `backend/src/main/java/com/example/getoffer/config/StaticResourceConfig.java`
- Modify: `backend/src/main/resources/application.properties`
- Modify: `backend/src/main/java/com/example/getoffer/common/GlobalExceptionHandler.java`
- Modify: `.gitignore`
- Modify: `backend/src/test/java/com/example/getoffer/controller/UserControllerTest.java`

- [ ] **Step 1: Write the failing avatar upload controller tests**

```java
@Test
void uploadAvatarStoresImageAndReturnsAvatarPath() throws Exception {
    String token = TestAuthHelper.registerAndGetToken(mockMvc, "avatar_user", "13800138012", "123456");
    MockMultipartFile file = new MockMultipartFile(
            "file",
            "avatar.png",
            MediaType.IMAGE_PNG_VALUE,
            "fake-image-content".getBytes(StandardCharsets.UTF_8)
    );

    mockMvc.perform(multipart("/api/v1/users/me/avatar")
                    .file(file)
                    .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.avatar").value(startsWith("/uploads/avatars/")));
}

@Test
void uploadAvatarRejectsNonImageFile() throws Exception {
    String token = TestAuthHelper.registerAndGetToken(mockMvc, "avatar_user_2", "13800138013", "123456");
    MockMultipartFile file = new MockMultipartFile(
            "file",
            "avatar.txt",
            MediaType.TEXT_PLAIN_VALUE,
            "plain-text".getBytes(StandardCharsets.UTF_8)
    );

    mockMvc.perform(multipart("/api/v1/users/me/avatar")
                    .file(file)
                    .header("Authorization", "Bearer " + token))
            .andExpect(status().isBadRequest());
}
```

- [ ] **Step 2: Run the focused avatar tests to verify they fail**

Run: `./mvnw.cmd -Dtest=UserControllerTest test`

Expected: FAIL because `/api/v1/users/me/avatar` does not exist yet or returns the wrong payload.

- [ ] **Step 3: Implement the minimal avatar upload DTO and service**

```java
// AvatarUploadResponse.java
public class AvatarUploadResponse {

    private String avatar;

    public AvatarUploadResponse(String avatar) {
        this.avatar = avatar;
    }

    public String getAvatar() {
        return avatar;
    }
}
```

```java
// UserProfileService.java
public AvatarUploadResponse uploadAvatar(String username, MultipartFile file) {
    validateAvatarFile(file);
    User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("user not found"));

    Path avatarDir = Paths.get(uploadDir).resolve("avatars");
    Files.createDirectories(avatarDir);

    String filename = buildAvatarFilename(user.getId(), file.getOriginalFilename());
    Path target = avatarDir.resolve(filename);
    file.transferTo(target);

    String avatarPath = "/uploads/avatars/" + filename;
    user.setAvatar(avatarPath);
    userRepository.save(user);
    return new AvatarUploadResponse(avatarPath);
}
```

- [ ] **Step 4: Wire controller, resource mapping, and upload config**

```java
// UserController.java
@PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
public ApiResponse<AvatarUploadResponse> uploadAvatar(@RequestParam("file") MultipartFile file) {
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    return ApiResponse.success(userProfileService.uploadAvatar(username, file));
}
```

```java
// StaticResourceConfig.java
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(Paths.get(uploadDir).toUri().toString());
    }
}
```

```properties
# application.properties
app.upload-dir=${APP_UPLOAD_DIR:uploads}
spring.servlet.multipart.max-file-size=2MB
spring.servlet.multipart.max-request-size=2MB
```

```gitignore
backend/uploads/
uploads/
```

- [ ] **Step 5: Re-run the avatar tests and the whole backend suite**

Run: `./mvnw.cmd test`

Expected: PASS with avatar upload behavior green and no controller regressions.

- [ ] **Step 6: Commit the avatar upload slice**

```bash
git add backend/src/main/java/com/example/getoffer/dto/user/AvatarUploadResponse.java backend/src/main/java/com/example/getoffer/service/user/UserProfileService.java backend/src/main/java/com/example/getoffer/controller/UserController.java backend/src/main/java/com/example/getoffer/config/StaticResourceConfig.java backend/src/main/resources/application.properties backend/src/main/java/com/example/getoffer/common/GlobalExceptionHandler.java backend/src/test/java/com/example/getoffer/controller/UserControllerTest.java .gitignore
git commit -m "feat: add avatar upload endpoint"
```

## Task 3: Add frontend profile helpers, API calls, and mapping tests

**Files:**
- Modify: `getoffer/src/api/transformers.js`
- Modify: `getoffer/src/api/frontend.js`
- Create: `getoffer/src/utils/user-profile.js`
- Create: `getoffer/tests/user-profile.test.js`

- [ ] **Step 1: Write the failing frontend helper tests**

```javascript
import test from 'node:test'
import assert from 'node:assert/strict'

import {
  getDisplayName,
  getAvatarFallback,
  normalizeNicknameInput,
  validateNickname,
  isAcceptedAvatarFile,
} from '../src/utils/user-profile.js'

test('getDisplayName prefers nickname over username', () => {
  assert.equal(getDisplayName({ username: 'login_name', nickname: 'display_name' }), 'display_name')
  assert.equal(getDisplayName({ username: 'login_name', nickname: '' }), 'login_name')
})

test('getAvatarFallback uses the first visible character', () => {
  assert.equal(getAvatarFallback({ username: 'login_name', nickname: 'display_name' }), 'D')
  assert.equal(getAvatarFallback({ username: 'login_name', nickname: '' }), 'L')
})

test('validateNickname rejects values longer than ten characters', () => {
  assert.equal(validateNickname('1234567890').valid, true)
  assert.equal(validateNickname('12345678901').valid, false)
})

test('isAcceptedAvatarFile only accepts image files under the size limit', () => {
  assert.equal(isAcceptedAvatarFile({ type: 'image/png', size: 1024 }).valid, true)
  assert.equal(isAcceptedAvatarFile({ type: 'text/plain', size: 1024 }).valid, false)
})
```

- [ ] **Step 2: Run the frontend helper tests to verify they fail**

Run: `node --test getoffer/tests/user-profile.test.js`

Expected: FAIL because `user-profile.js` does not exist yet.

- [ ] **Step 3: Implement the minimal helper module and API mapping**

```javascript
// user-profile.js
export const getDisplayName = (user = {}) => user.nickname?.trim() || user.username || 'User'

export const getAvatarFallback = (user = {}) => {
  const displayName = getDisplayName(user)
  return displayName.charAt(0).toUpperCase() || 'U'
}

export const normalizeNicknameInput = (value = '') => value.trim()

export const validateNickname = (value = '') => {
  const nickname = normalizeNicknameInput(value)
  if (!nickname) {
    return { valid: false, message: 'Nickname is required' }
  }
  if (nickname.length > 10) {
    return { valid: false, message: 'Nickname must be 10 characters or fewer' }
  }
  return { valid: true, message: '' }
}

export const isAcceptedAvatarFile = (file, maxSize = 2 * 1024 * 1024) => {
  if (!file?.type?.startsWith('image/')) {
    return { valid: false, message: 'Please upload an image file' }
  }
  if (file.size > maxSize) {
    return { valid: false, message: 'Avatar must be 2MB or smaller' }
  }
  return { valid: true, message: '' }
}
```

```javascript
// transformers.js
export const mapUserProfile = (profile = {}) => ({
  ...profile,
  nickname: profile.nickname || '',
  avatar: profile.avatar || '',
  registerTime: formatDate(profile.registerTime),
  stats: {
    articleCount: profile.stats?.articleCount || 0,
    favoriteCount: profile.stats?.favoriteCount || 0,
    likeCount: profile.stats?.likeCount || 0,
  },
})
```

```javascript
// frontend.js
export const userApi = {
  async updateProfile(data) {
    const payload = await request({
      url: '/api/v1/users/me/profile',
      method: 'patch',
      data,
    })
    return { data: mapUserProfile(payload) }
  },

  async uploadAvatar(file) {
    const formData = new FormData()
    formData.append('file', file)
    const payload = await request({
      url: '/api/v1/users/me/avatar',
      method: 'post',
      data: formData,
    })
    return { data: payload }
  },
}
```

- [ ] **Step 4: Run the helper tests and one existing frontend test**

Run: `node --test getoffer/tests/user-profile.test.js getoffer/tests/auth-session.test.js`

Expected: PASS with the new helper behavior and no auth session regression.

- [ ] **Step 5: Commit the frontend helper slice**

```bash
git add getoffer/src/api/transformers.js getoffer/src/api/frontend.js getoffer/src/utils/user-profile.js getoffer/tests/user-profile.test.js
git commit -m "feat: add frontend profile helpers"
```

## Task 4: Update the profile page and navbar UI with TDD-backed helpers

**Files:**
- Modify: `getoffer/src/components/Navbar.vue`
- Modify: `getoffer/src/views/Profile.vue`
- Modify: `getoffer/src/utils/user-profile.js`
- Modify: `getoffer/tests/user-profile.test.js`

- [ ] **Step 1: Extend the frontend tests with a failing empty-state avatar fallback case**

```javascript
test('getAvatarFallback uses U when no nickname or username is present', () => {
  assert.equal(getAvatarFallback({}), 'U')
})
```

- [ ] **Step 2: Run the frontend tests again to verify they fail**

Run: `node --test getoffer/tests/user-profile.test.js`

Expected: FAIL because `getAvatarFallback({})` does not return the empty-state fallback yet.

- [ ] **Step 3: Implement the minimal helper fix plus navbar and profile page changes**

```javascript
// user-profile.js
export const getAvatarFallback = (user = {}) => {
  const displayName = getDisplayName(user)
  return displayName?.charAt(0).toUpperCase() || 'U'
}
```

```vue
<!-- Navbar.vue -->
<el-dropdown-item disabled>
  {{ displayName }}
</el-dropdown-item>
```

```js
const userAvatar = computed(() => userStore.user?.avatar || '')
const displayName = computed(() => getDisplayName(userStore.user))
const avatarFallback = computed(() => getAvatarFallback(userStore.user))
```

```vue
<!-- Profile.vue -->
<el-upload
  :show-file-list="false"
  :auto-upload="false"
  :before-upload="handleAvatarBeforeUpload"
>
  <el-button :loading="avatarUploading">Change Avatar</el-button>
</el-upload>

<el-form-item label="Login Username">
  <el-input :model-value="userInfo.username" disabled />
</el-form-item>
<el-form-item label="Nickname" :error="nicknameError">
  <el-input v-model="nicknameForm.nickname" maxlength="10" show-word-limit />
</el-form-item>
<el-button type="primary" :loading="nicknameSaving" @click="saveNickname">
  Save Nickname
</el-button>
```

```js
const userAvatar = computed(() => userInfo.value.avatar || '')
const displayName = computed(() => getDisplayName(userInfo.value))
const avatarFallback = computed(() => getAvatarFallback(userInfo.value))

const saveNickname = async () => {
  const validation = validateNickname(nicknameForm.value.nickname)
  if (!validation.valid) {
    nicknameError.value = validation.message
    return
  }

  const response = await userApi.updateProfile({
    nickname: normalizeNicknameInput(nicknameForm.value.nickname),
  })

  applyProfile(response.data)
}
```

- [ ] **Step 4: Verify the frontend helper suite stays green after wiring the UI**

Run: `node --test getoffer/tests/user-profile.test.js getoffer/tests/auth-session.test.js getoffer/tests/navbar-visibility.test.js`

Expected: PASS with updated display name logic and no helper regressions.

- [ ] **Step 5: Run the frontend build as the UI verification gate**

Run: `npm.cmd run build`

Workdir: `e:\项目文件\八股code\getoffer`

Expected: PASS with Vite build output and no type-check errors.

- [ ] **Step 6: Commit the profile UI slice**

```bash
git add getoffer/src/components/Navbar.vue getoffer/src/views/Profile.vue getoffer/src/utils/user-profile.js getoffer/tests/user-profile.test.js
git commit -m "feat: add editable profile nickname and avatar ui"
```

## Task 5: Final verification and manual sanity checks

**Files:**
- No new files expected

- [ ] **Step 1: Run the full backend verification**

Run: `./mvnw.cmd test`

Workdir: `e:\项目文件\八股code\backend`

Expected: PASS with all backend tests green.

- [ ] **Step 2: Run the full frontend verification**

Run: `node --test tests/*.test.js`

Workdir: `e:\项目文件\八股code\getoffer`

Expected: PASS with all frontend tests green.

- [ ] **Step 3: Run the production frontend build**

Run: `npm.cmd run build`

Workdir: `e:\项目文件\八股code\getoffer`

Expected: PASS with generated production bundle.

- [ ] **Step 4: Do a manual smoke test**

```text
1. Register a new user and open /profile
2. Confirm the header name falls back from nickname to username
3. Set a unique nickname and confirm the profile header and navbar update immediately
4. Upload a png or jpg avatar and confirm the new image shows without refresh
5. Refresh the page and confirm avatar and nickname persist
6. Try a duplicate nickname and a non-image file, and confirm clear error messages
```

- [ ] **Step 5: Commit the verified final state**

```bash
git add .
git commit -m "feat: support nickname editing and avatar uploads"
```
