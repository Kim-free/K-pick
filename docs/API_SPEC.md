# Kpick API 명세서 초안

## 공통

### Enum

| 이름 | 값 |
| --- | --- |
| CommunityPostType | `THREAD`, `USER_VOTE` |
| MissionState | `ONGOING`, `WAITING_RESULT`, `COMPLETED`, `TEMPORARY` |
| MissionSuggestionStatus | `SUBMITTED`, `ACCEPTED`, `REJECTED` |
| Genre | `DATINGSHOW`, `SURVIVAL`, `COOKING` |
| RankingType | `SEASON`, `COMMUNITY`, `TOTAL` |
| PointScope | `SEASON`, `TOTAL` |
| LoginType | `LOCAL`, `APPLE`, `GOOGLE`, `KAKAO` |
| ImageUploadPurpose | `THREAD`, `PROFILE`, `PROGRAM`, `INQUIRY` |
| AppUserRole | `USER`, `ADMIN` |

### Date Format

모든 날짜 요청/응답 값은 문자열 `"yyyy-MM-dd"` 형식으로 통일한다. 서버 내부에서 `LocalDateTime`으로 관리되는 값도 API JSON에서는 시간 없이 날짜만 주고받는다.

### Error Response

```json
{
  "message": "에러 메시지"
}
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| message | String | ✅ | 에러 메시지 |

### Authorization

`/api/auth/**`, Swagger 문서, 브라우저 preflight 요청을 제외한 API는 로그인 후 받은 JWT를 요청 헤더에 전달한다.

```http
Authorization: Bearer {accessToken}
```

토큰이 없거나 서명이 올바르지 않거나 만료되면 `401 Unauthorized`를 반환한다.

서버는 검증된 JWT에서 `appUserId`, `profileId`를 추출한다. `/me` 경로와 본인 관점이 필요한 API는 클라이언트가 보낸 식별값을 받지 않고 JWT의 `profileId`를 사용한다. 다른 사용자의 랭킹 프로필 조회처럼 공개 조회가 필요한 API만 URL의 `{profileId}`를 유지한다.

### Admin Authorization

`/api/admin/**` 경로는 JWT 인증에 더해 `ADMIN` 권한이 필요하다. 일반 유저 토큰으로 접근하면 `403 Forbidden`을 반환한다.

관리자 권한은 로그인 시 이메일 기준으로 부여한다. 배포 환경변수 `ADMIN_EMAILS`에 쉼표로 구분한 관리자 이메일을 등록한다.

```env
ADMIN_EMAILS=admin@example.com,owner@example.com
```

해당 이메일로 OAuth 로그인하면 `AuthResponse.appUserRole`이 `ADMIN`으로 내려가며, JWT payload에도 `role=ADMIN`이 포함된다.

---

# Image Upload (이미지 업로드)

▶ 이미지 업로드용 Presigned URL 발급 : `POST /api/images/presigned-url` -완-

## 이미지 업로드용 Presigned URL 발급

`POST /api/images/presigned-url`

앱 서버가 이미지 파일을 직접 받지 않고 S3 호환 오브젝트 스토리지에 바로 업로드한다.

1. 프론트가 이 API에서 presigned URL과 최종 `imageUrl`을 발급받는다.
2. 프론트가 `uploadUrl`에 동일한 `Content-Type` 헤더를 넣어 이미지 바이너리를 `PUT`한다.
3. 업로드 완료 후 `imageUrl`을 스레드, 프로필, 프로그램 또는 문의 생성·수정 API에 전달한다.

### Request

```json
{
  "uploadPurpose": "THREAD",
  "fileName": "photo.jpg",
  "contentType": "image/jpeg",
  "fileSize": 245760
}
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| uploadPurpose | ImageUploadPurpose | ✅ | `THREAD`, `PROFILE`, `PROGRAM`, `INQUIRY` |
| fileName | String | ✅ | 사용자가 선택한 원본 파일명 |
| contentType | String | ✅ | `image/jpeg`, `image/png`, `image/webp` |
| fileSize | Long | ✅ | 파일 크기. 최대 10MB |

### Response

```json
{
  "objectKey": "threads/550e8400-e29b-41d4-a716-446655440000.jpg",
  "uploadUrl": "https://bucket.s3.ap-northeast-2.amazonaws.com/threads/...?X-Amz-Signature=...",
  "imageUrl": "https://cdn.example.com/threads/550e8400-e29b-41d4-a716-446655440000.jpg",
  "method": "PUT",
  "contentType": "image/jpeg",
  "expiresInSeconds": 300
}
```

배포 환경에는 `S3_BUCKET`, `S3_REGION`, `S3_ACCESS_KEY`, `S3_SECRET_KEY`를 설정한다. CDN을 사용하면 `S3_PUBLIC_BASE_URL`도 설정한다. 로컬 기본 설정에는 앱 실행을 위한 dummy key가 들어있으므로, 실제 S3 업로드를 테스트할 때는 반드시 실제 access key 환경변수를 넣어야 한다.

Access Key가 연결된 IAM User에는 최소한 업로드 대상 버킷의 `s3:PutObject` 권한을 부여한다.

```json
{
  "Effect": "Allow",
  "Action": ["s3:PutObject"],
  "Resource": "arn:aws:s3:::kpick-images/*"
}
```

로컬에서 실제 presigned URL을 시험할 때만 AWS CLI 프로필 또는 SSO 로그인을 사용한다. S3 버킷 CORS에는 앱의 origin에서 `PUT`, `GET`을 허용해야 한다.

---

# Auth (인증)

▶ Apple 로그인 : `POST /api/auth/apple` -완-
▶ Google 로그인 : `POST /api/auth/google` -완-
▶ Kakao 로그인 : `POST /api/auth/kakao` -완-
▶ 회원 탈퇴 : `DELETE /api/app-users/me` -완-

## Apple 로그인

`POST /api/auth/apple`

프론트는 Apple 로그인 후 받은 인가 코드만 서버로 전달한다. 서버는 Apple token endpoint에 인가 코드를 전달해 `id_token`을 발급받고 Apple JWKS 공개키로 서명, issuer, audience, 만료를 검증한다. 이미 가입된 Apple 계정이면 로그인하고, 처음 보는 Apple 계정이면 `AppUser`와 빈 `Profile`을 생성한 뒤 Kpick JWT를 발급한다.

### Request

```json
{
  "authorizationCode": "apple.authorization.code"
}
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| authorizationCode | String | ✅ | Apple에서 받은 일회용 인가 코드 |

### Response

```json
{
  "appUserId": 1,
  "profileId": 1,
  "accessToken": "jwt.access.token",
  "tokenType": "Bearer",
  "newUser": true,
  "signUpStatus": "NULL",
  "appUserRole": "USER"
}
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| appUserId | Long | ✅ | 앱 유저 식별값 |
| profileId | Long | ✅ | 프로필 식별값 |
| accessToken | String | ✅ | API 인증에 사용할 JWT |
| tokenType | String | ✅ | Bearer |
| newUser | boolean | ✅ | 신규 생성 여부 |
| signUpStatus | SignUpStatus | ✅ | 온보딩 진행 상태 |
| appUserRole | AppUserRole | ✅ | `USER`, `ADMIN` |

## Google 로그인

`POST /api/auth/google`

프론트는 Google 로그인 후 받은 인가 코드만 서버로 전달한다. 서버는 Google token endpoint에서 access token을 발급받고 Google userinfo API를 호출해 사용자 정보를 확인한 뒤 로그인 또는 회원가입 처리한다.

### Request

```json
{
  "authorizationCode": "google.authorization.code"
}
```

### Response

`AuthResponse`

## Kakao 로그인

`POST /api/auth/kakao`

프론트는 Kakao 로그인 후 받은 인가 코드만 서버로 전달한다. 서버는 Kakao token endpoint에서 access token을 발급받고 Kakao 사용자 정보 조회 API를 호출해 사용자 정보를 확인한 뒤 로그인 또는 회원가입 처리한다.

### Request

```json
{
  "authorizationCode": "kakao.authorization.code"
}
```

### Response

`AuthResponse`

## 회원 탈퇴

`DELETE /api/app-users/me`

JWT의 `appUserId`를 기준으로 현재 로그인한 AppUser를 탈퇴 처리한다. 기존 게시글, 미션 참여, 랭킹 기록과의 연결을 보존하기 위해 물리 삭제하지 않고 탈퇴 상태로 변경한다. 탈퇴 후 기존 JWT로 일반 API를 호출하면 `401 Unauthorized`를 반환한다.

### Request

없음

### Response

`204 No Content`

### OAuth 서버 환경변수

| 환경변수 | 필수 | 설명 |
| --- | --- | --- |
| APPLE_CLIENT_ID | ✅ | Apple App ID 또는 Services ID |
| APPLE_TEAM_ID | ✅ | Apple Developer Team ID |
| APPLE_KEY_ID | ✅ | Apple Sign in with Apple Key ID |
| APPLE_PRIVATE_KEY_PATH | ✅ | Apple `.p8` private key 경로. 외부 파일은 `file:/path/to/AuthKey.p8` 형식 |
| APPLE_REDIRECT_URI |  | Apple 인가 요청에 redirect URI를 포함한 경우 동일한 값 |
| GOOGLE_CLIENT_ID | ✅ | Google OAuth client ID |
| GOOGLE_CLIENT_SECRET |  | 사용하는 Google OAuth client 유형에 따라 필요 |
| GOOGLE_REDIRECT_URI |  | Google Console에 등록하고 인가 요청에 사용한 redirect URI |
| KAKAO_REST_API_KEY | ✅ | Kakao REST API Key |
| KAKAO_CLIENT_SECRET |  | Kakao Client Secret 기능이 켜져 있으면 필요 |
| KAKAO_REDIRECT_URI | ✅ | Kakao Developers에 등록하고 인가 요청에 사용한 redirect URI |

### 공통 Response Body 기준

아래 DTO들은 여러 API에서 그대로 재사용된다. 각 API의 `Response` 예시는 실제 반환 필드명을 기준으로 작성했다.

#### PointTierResponse

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| step | int | ✅ | 큰 단계 번호 |
| tierName | String | ✅ | 단계 이름 |
| subLevel | Integer |  | 세부 레벨. 전설토리는 없음 |
| displayName | String | ✅ | 화면 표시용 단계명 |
| minPoint | long | ✅ | 단계 시작 포인트 |
| maxPoint | Long |  | 단계 종료 포인트. 최상위 단계는 없음 |
| nextTierName | String |  | 다음 단계 이름 |
| pointsToNextTier | Long |  | 다음 단계까지 남은 포인트 |

#### CommunityPostResponse

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| postType | CommunityPostType | ✅ | 게시글 타입 |
| postId | Long | ✅ | 타입별 게시글 식별값 |
| profileId | Long | ✅ | 작성자 프로필 식별값 |
| nickname | String |  | 작성자 닉네임 |
| profileImageUrl | String |  | 작성자 프로필 이미지 URL |
| programId | Long | ✅ | 프로그램 식별값 |
| missionId | Long |  | 공유된 미션 식별값. 스레드가 미션에서 공유된 경우만 존재 |
| title | String | ✅ | 제목 |
| description | String |  | 내용 |
| dueDateTime | String |  | 유저투표 마감 날짜. `yyyy-MM-dd` |
| viewCount | long | ✅ | 조회수 |
| likeCount | long | ✅ | 좋아요 수 |
| commentCount | long | ✅ | 댓글 수 |
| voteCount | long | ✅ | 투표 수 |
| isVoted | Boolean |  | JWT로 인증된 조회자가 유저투표에 참여했는지 여부 |
| selectedUserVoteOptionId | Long |  | 조회자가 선택한 유저투표 선택지 식별값 |
| userVoteOptions | List<UserVoteOptionSummaryResponse> | ✅ | 유저투표 목록 카드에 노출할 선택지 요약. 스레드는 빈 배열 |
| sharedMission | SharedMissionSummaryResponse |  | 미션에서 공유된 스레드인 경우 노출할 미션 카드 요약 |
| imageUrls | List<String> | ✅ | 스레드 첨부 이미지 URL. 유저투표는 빈 배열 |
| createdAt | String | ✅ | 생성 날짜. `yyyy-MM-dd` |

#### UserVoteOptionSummaryResponse

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| userVoteOptionId | Long | ✅ | 유저투표 선택지 식별값 |
| content | String | ✅ | 선택지 내용 |
| displayOrder | Integer | ✅ | 선택지 노출 순서 |
| isSelected | Boolean | ✅ | 조회자가 선택한 선택지인지 여부 |

#### SharedMissionSummaryResponse

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| missionId | Long | ✅ | 공유된 미션 식별값 |
| programId | Long | ✅ | 프로그램 식별값 |
| programName | String | ✅ | 프로그램명 |
| episode | String |  | 회차 |
| missionName | String | ✅ | 미션명 |
| dueDateTime | String | ✅ | 마감 날짜. `yyyy-MM-dd` |
| attenderCount | int | ✅ | 참여자 수 |

#### CommunityCommentResponse

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| communityCommentId | Long | ✅ | 댓글 식별값 |
| postType | CommunityPostType | ✅ | 댓글이 달린 게시글 타입 |
| postId | Long | ✅ | 댓글이 달린 게시글 식별값 |
| profileId | Long | ✅ | 작성자 프로필 식별값 |
| nickname | String |  | 댓글 작성자 닉네임 |
| profileImageUrl | String |  | 댓글 작성자 프로필 이미지 URL |
| content | String | ✅ | 댓글 내용 |
| likeCount | long | ✅ | 댓글 좋아요 수 |
| createdAt | String | ✅ | 생성 날짜. `yyyy-MM-dd` |

#### CommunityLikeToggleResponse

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| isLiked | Boolean | ✅ | 요청 후 좋아요 상태 |
| likeCount | long | ✅ | 요청 후 좋아요 수 |

#### RankingEntryResponse

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| rank | int | ✅ | 순위 |
| profileId | Long | ✅ | 프로필 식별값 |
| nickname | String | ✅ | 닉네임 |
| profileImageUrl | String |  | 프로필 이미지 URL |
| score | long | ✅ | 랭킹 점수 |
| pointTier | PointTierResponse |  | 포인트 티어. 커뮤니티 랭킹에서는 null |
| threadCount | Long |  | 커뮤니티 랭킹에서 작성한 글 수 |
| commentCount | Long |  | 커뮤니티 랭킹에서 작성한 댓글 수 |

---

# Profile (프로필)

▶ 마이페이지 조회 : `GET /api/profiles/me/my-page` -완-
▶ 온보딩 프로필 설정 : `PATCH /api/profiles/me/onboarding` -완-
▶ 프로필 이미지 변경 : `PATCH /api/profiles/me/image` -완-
▶ 닉네임 중복 확인 : `GET /api/profiles/nickname/check?nickname=나의닉네임` -완-
▶ 닉네임 변경 : `PATCH /api/profiles/me/nickname` -완-
▶ 내 픽 기록 조회 : `GET /api/profiles/me/mission-history?resultFilter=ALL` -완-
▶ 내 커뮤니티 활동 조회 : `GET /api/profiles/me/community-activities?activityType=POST` -완-
▶ 스페셜 뱃지 조회 : `GET /api/profiles/me/badges` -완-
▶ 스페셜 뱃지 임시 생성 : `POST /api/profiles/me/badges` -완-
▶ 관심 프로그램 조회 : `GET /api/profiles/me/program-interests` -완-
▶ 관심 프로그램 키워드 검색 : `GET /api/profiles/me/program-interests/search?keyword=런닝` -완-
▶ 관심 프로그램 수정 : `PUT /api/profiles/me/program-interests` -완-

## 마이페이지 조회

`GET /api/profiles/me/my-page`

### Response

```json
{
  "profileId": 1,
  "nickname": "나의닉네임",
  "profileImageUrl": "https://cdn.example.com/profiles/profile-image.jpg",
  "pointTier": {
    "step": 3,
    "tierName": "꼬마토리",
    "subLevel": 2,
    "displayName": "꼬마토리 2",
    "minPoint": 300,
    "maxPoint": 699,
    "nextTierName": "사춘기토리",
    "pointsToNextTier": 180
  },
  "seasonRank": 9,
  "seasonPoint": 285,
  "totalRank": 12,
  "totalPoint": 520,
  "communityRank": 7,
  "communityPoint": 90,
  "coin": 12,
  "growthMessage": "사춘기토리까지 180pt 남음",
  "inviteCode": "ABCDE-FG12"
}
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| profileId | Long | ✅ | 프로필 식별값 |
| nickname | String | ✅ | 닉네임 |
| profileImageUrl | String |  | 프로필 이미지 URL |
| pointTier | PointTierResponse | ✅ | 누적 포인트 기준 티어 |
| seasonRank | int | ✅ | 시즌 순위 |
| seasonPoint | long | ✅ | 이번 시즌 포인트 |
| totalRank | int | ✅ | 전체 누적 순위 |
| totalPoint | long | ✅ | 전체 누적 포인트 |
| communityRank | int | ✅ | 커뮤니티 순위 |
| communityPoint | long | ✅ | 커뮤니티 활동 점수 |
| coin | long | ✅ | 보유 Pick |
| growthMessage | String | ✅ | 다음 성장 단계 안내 |
| inviteCode | String | ✅ | 초대 코드 |

## 온보딩 프로필 설정

`PATCH /api/profiles/me/onboarding`

온보딩의 프로필 기본 정보 입력 화면에서 닉네임, 프로필 이미지, 성별, 생년월일, 가입 경로, 친구 초대코드를 한 번에 저장한다. 친구 초대코드는 선택값이며, 유효한 코드를 처음 입력하면 가입자와 초대한 사용자에게 각각 `100 Pick`을 지급한다.

### Request

```json
{
  "nickname": "나의닉네임",
  "profileImageUrl": "https://cdn.example.com/profiles/profile-image.jpg",
  "gender": "FEMALE",
  "birthDate": "2000-01-01",
  "joinPath": "INSTAGRAM",
  "friendInviteCode": "ABCDE-FG12"
}
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| nickname | String | ✅ | 닉네임. 2~12자 |
| profileImageUrl | String |  | 프로필 이미지 URL |
| gender | ProfileGender | ✅ | `FEMALE`, `MALE`, `OTHER` |
| birthDate | String | ✅ | 생년월일. `yyyy-MM-dd` |
| joinPath | String |  | 가입 경로 |
| friendInviteCode | String |  | 친구 초대코드 |

### Response

```json
{
  "profileId": 1,
  "nickname": "나의닉네임",
  "profileImageUrl": "https://cdn.example.com/profiles/profile-image.jpg",
  "gender": "FEMALE",
  "birthDate": "2000-01-01",
  "joinPath": "INSTAGRAM",
  "invitedByProfileId": 2,
  "inviteRewardPick": 100,
  "signUpStatus": "NICKNAMEDONE"
}
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| profileId | Long | ✅ | 프로필 식별값 |
| nickname | String | ✅ | 닉네임 |
| profileImageUrl | String |  | 프로필 이미지 URL |
| gender | ProfileGender | ✅ | 저장된 성별 |
| birthDate | String | ✅ | 생년월일. `yyyy-MM-dd` |
| joinPath | String |  | 가입 경로 |
| invitedByProfileId | Long |  | 초대코드 주인의 프로필 식별값 |
| inviteRewardPick | int | ✅ | 이번 요청에서 지급된 가입 보너스 Pick |
| signUpStatus | SignUpStatus | ✅ | 온보딩 진행 상태 |

## 닉네임 중복 확인

`GET /api/profiles/nickname/check?nickname=나의닉네임`

### Response

```json
{
  "nickname": "나의닉네임",
  "available": true
}
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| nickname | String | ✅ | 확인한 닉네임 |
| available | boolean | ✅ | 사용 가능 여부 |

## 닉네임 변경

`PATCH /api/profiles/me/nickname`

### Request

```json
{
  "nickname": "나의닉네임"
}
```

### Response

```json
{
  "profileId": 1,
  "nickname": "나의닉네임"
}
```

## 프로필 이미지 변경

`PATCH /api/profiles/me/image`

먼저 `uploadPurpose=PROFILE`로 presigned URL을 발급받아 이미지를 업로드한 뒤 최종 URL을 전달한다.

### Request

```json
{
  "profileImageUrl": "https://cdn.example.com/profiles/profile-image.jpg"
}
```

### Response

```json
{
  "profileId": 1,
  "profileImageUrl": "https://cdn.example.com/profiles/profile-image.jpg"
}
```

## 내 픽 기록 조회

`GET /api/profiles/me/mission-history`
`GET /api/profiles/me/mission-history?resultFilter=CORRECT`

| Query | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| resultFilter | String |  | ALL, CORRECT, WRONG, PENDING |

### Response

```json
{
  "profileId": 1,
  "resultFilter": "ALL",
  "totalCount": 24,
  "missions": [
    {
      "missionId": 10,
      "programId": 1,
      "programName": "환승연애4",
      "programThumbnailImageUrl": "https://cdn.example.com/programs/transit-love-4.png",
      "episode": "5화",
      "missionName": "마지막 커플은 누구?",
      "selectedMissionOptionId": 1,
      "selectedOptionContent": "수지 & 현준",
      "correctMissionOptionId": 1,
      "correctOptionContent": "수지 & 현준",
      "missionResultStatus": "CORRECT",
      "coinFee": 2,
      "earnedPoint": 42,
      "dueDateTime": "2026-05-30T22:00:00"
    }
  ]
}
```

## 내 커뮤니티 활동 조회

`GET /api/profiles/me/community-activities`
`GET /api/profiles/me/community-activities?activityType=COMMENT`

| Query | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| activityType | String |  | POST, COMMENT, ALL |

### Response

```json
{
  "profileId": 1,
  "activityType": "POST",
  "totalCount": 5,
  "activities": [
    {
      "activityTargetType": "POST",
      "postType": "THREAD",
      "postId": 20,
      "communityCommentId": null,
      "title": "수지현준 진짜 될 것 같아요!",
      "content": "5화 분위기 보면 확실한 것 같은데...",
      "likeCount": 12,
      "commentCount": 4,
      "createdAt": "2026-05-30T16:00:00"
    }
  ]
}
```

## 스페셜 뱃지 조회

`GET /api/profiles/me/badges`

### Response

```json
{
  "profileId": 1,
  "acquiredCount": 3,
  "acquiredBadges": [
    {
      "badgeCode": "FIRE_TORI",
      "badgeName": "불꽃토리",
      "description": "3연속 정답",
      "emoji": "🔥",
      "acquired": true
    }
  ],
  "lockedBadges": [
    {
      "badgeCode": "ALL_PICK_TORI",
      "badgeName": "올픽토리",
      "description": "전 미션 참여",
      "emoji": "✅",
      "acquired": false
    }
  ]
}
```

## 스페셜 뱃지 임시 생성

`POST /api/profiles/me/badges`

정식 뱃지 지급 로직이 생기기 전까지 관리자/테스트 용도로 특정 프로필에 뱃지를 부여한다.

### Request

```json
{
  "badgeCode": "FIRST_PICK_TORI",
  "badgeName": "첫픽토리",
  "description": "미션 첫 픽",
  "emoji": "⚡"
}
```

### Response

```json
{
  "badgeCode": "FIRST_PICK_TORI",
  "badgeName": "첫픽토리",
  "description": "미션 첫 픽",
  "emoji": "⚡",
  "acquired": true
}
```

## 관심 프로그램 조회

`GET /api/profiles/me/program-interests`

### Response

```json
{
  "profileId": 1,
  "programs": [
    {
      "programId": 1,
      "programName": "환승연애4",
      "broadcaster": "TVING",
      "genre": "DATINGSHOW",
      "season": "시즌4",
      "isOnAir": true
    }
  ]
}
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| profileId | Long | ✅ | 프로필 식별값 |
| programs | List<InterestProgramResponse> | ✅ | 관심 프로그램 목록 |
| programs[].programId | Long | ✅ | 프로그램 식별값 |
| programs[].programName | String | ✅ | 프로그램명 |
| programs[].broadcaster | String | ✅ | 방송사 |
| programs[].genre | Genre | ✅ | 장르 |
| programs[].season | String | ✅ | 시즌 |
| programs[].isOnAir | Boolean | ✅ | 방영중 여부 |

## 관심 프로그램 키워드 검색

`GET /api/profiles/me/program-interests/search?keyword=런닝`

관심 프로그램 수정 화면에서 검색어 입력 시 프로그램 후보를 조회한다. 이미 관심 프로그램으로 선택된 항목은 `isInterested=true`로 내려준다.

| Query | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| keyword | String |  | 프로그램명 검색어. 비어 있으면 빈 목록 반환 |

### Response

```json
{
  "profileId": 1,
  "keyword": "런닝",
  "programs": [
    {
      "programId": 3,
      "programName": "런닝맨",
      "broadcaster": "SBS",
      "genre": "ENTERTAINMENT",
      "season": "시즌1",
      "isOnAir": true,
      "isInterested": false
    }
  ]
}
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| profileId | Long | ✅ | 프로필 식별값 |
| keyword | String | ✅ | 검색어 |
| programs | List<ProgramInterestSearchItemResponse> | ✅ | 검색된 프로그램 목록 |
| programs[].programId | Long | ✅ | 프로그램 식별값 |
| programs[].programName | String | ✅ | 프로그램명 |
| programs[].broadcaster | String | ✅ | 방송사 |
| programs[].genre | Genre | ✅ | 장르 |
| programs[].season | String |  | 시즌 |
| programs[].isOnAir | Boolean | ✅ | 방영중 여부 |
| programs[].isInterested | Boolean | ✅ | 이미 관심 프로그램으로 선택되어 있는지 |

## 관심 프로그램 수정

`PUT /api/profiles/me/program-interests`

### Request

```json
{
  "programIds": [1, 4, 7]
}
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| programIds | List<Long> | ✅ | 최종 선택한 관심 프로그램 id 목록 |

### Response

```json
{
  "profileId": 1,
  "programs": [
    {
      "programId": 1,
      "programName": "환승연애4",
      "broadcaster": "TVING",
      "genre": "DATINGSHOW",
      "season": "시즌4",
      "isOnAir": true
    }
  ]
}
```

---

# Program (프로그램)

▶ 등록된 프로그램 목록 조회 : `GET /api/programs` -완-
▶ 프로그램 선택 목록 조회 : `GET /api/programs/selections?keyword=환승연애` -완-
▶ 프로그램 회차 목록 조회 : `GET /api/programs/{programId}/episodes` -완-
▶ 프로그램 생성 : `POST /api/admin/programs` -완-
▶ 프로그램 조회 : `GET /api/admin/programs` -완-
▶ 프로그램 상세 조회 : `GET /api/admin/programs/{programId}/details` -완-
▶ 프로그램 수정 : `PATCH /api/admin/programs/{programId}` -완-
▶ 프로그램 삭제 : `DELETE /api/admin/programs/{programId}` -완-

## 등록된 프로그램 목록 조회

`GET /api/programs`

미션 건의하기, 커뮤니티 글 작성 화면에 처음 진입했을 때 왼쪽 프로그램 목록을 구성할 때 사용한다.

### Response

```json
[
  {
    "programId": 1,
    "programName": "환승연애4",
    "genre": "DATINGSHOW"
  }
]
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| programId | Long | ✅ | 프로그램 식별값 |
| programName | String | ✅ | 프로그램명 |
| genre | Genre | ✅ | 장르 |

## 프로그램 선택 검색

`GET /api/programs/selections`
`GET /api/programs/selections?keyword=환승연애`

미션 건의하기, 커뮤니티 글 작성 화면에서 프로그램 검색 결과와 회차 드롭다운을 함께 구성할 때 사용한다.

| Query | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| keyword | String |  | 프로그램명 검색어 |

### Response

```json
[
  {
    "programId": 1,
    "programName": "환승연애4",
    "genre": "DATINGSHOW",
    "thumbnailImageUrl": "https://cdn.example.com/programs/transit-love-4.png",
    "episodes": ["5화", "4화", "3화", "2화", "1화"]
  }
]
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| programId | Long | ✅ | 프로그램 식별값 |
| programName | String | ✅ | 프로그램명 |
| genre | Genre | ✅ | 장르 |
| thumbnailImageUrl | String |  | 프로그램 아이콘 이미지 URL |
| episodes | List<String> | ✅ | 최신 회차부터 내려주는 회차 목록 |

## 프로그램 회차 목록 조회

`GET /api/programs/{programId}/episodes`

프로그램 목록에서 특정 프로그램을 눌렀을 때 오른쪽 회차 목록을 조회한다.

### Response

```json
{
  "programId": 1,
  "programName": "환승연애4",
  "episodes": ["5화", "4화", "3화", "2화", "1화"]
}
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| programId | Long | ✅ | 프로그램 식별값 |
| programName | String | ✅ | 프로그램명 |
| episodes | List<String> | ✅ | 최신 회차부터 내려주는 회차 목록 |

## 프로그램 생성

`POST /api/admin/programs`

### Request

```json
{
  "programName": "환승연애4",
  "broadcaster": "TVING",
  "genre": "DATINGSHOW",
  "season": "시즌4",
  "episodeCount": 5,
  "broadcastStartDate": "2025-02-15",
  "broadcastEndDate": null,
  "thumbnailImageUrl": "https://cdn.kpick.com/programs/transit-love-4.png",
  "isExposed": true,
  "description": "X와 재회할지 환승할지"
}
```

### Response

```json
{
  "programId": 1,
  "programName": "환승연애4",
  "broadcaster": "TVING",
  "genre": "DATINGSHOW",
  "season": "시즌4",
  "episodeCount": 5,
  "broadcastStartDate": "2025-02-15",
  "broadcastEndDate": null,
  "registeredDate": "2026-05-31",
  "thumbnailImageUrl": "https://cdn.kpick.com/programs/transit-love-4.png",
  "isOnAir": true,
  "isExposed": true,
  "description": "X와 재회할지 환승할지",
  "missionCount": 0
}
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| programId | Long | ✅ | 프로그램 식별값 |
| programName | String | ✅ | 프로그램명 |
| broadcaster | String | ✅ | 방송사 |
| genre | Genre | ✅ | 장르 |
| season | String |  | 시즌 |
| episodeCount | int | ✅ | 현재까지 등록된 회차 수 |
| broadcastStartDate | String |  | 방영 시작일. `yyyy-MM-dd` |
| broadcastEndDate | String |  | 방영 종료일. `yyyy-MM-dd` |
| registeredDate | String | ✅ | 프로그램 등록일. 서버가 등록 시점에 자동 기록. `yyyy-MM-dd` |
| thumbnailImageUrl | String |  | 썸네일 이미지 URL |
| isOnAir | Boolean | ✅ | 방영중 여부. 요청에서 생략하면 종료일 기준으로 계산 |
| isExposed | Boolean | ✅ | 노출 여부 |
| description | String |  | 설명 |
| missionCount | int | ✅ | 연결된 미션 수 |

## 프로그램 조회

`GET /api/admin/programs`
`GET /api/admin/programs?keyword=환승연애&genre=DATINGSHOW&isOnAir=true&isExposed=true`

| Query | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| keyword | String |  | 프로그램명 검색어 |
| genre | Genre |  | 장르 필터 |
| isOnAir | Boolean |  | 방영 상태 필터 |
| isExposed | Boolean |  | 노출 여부 필터 |

### Response

```json
[
  {
    "programId": 1,
    "programName": "환승연애4",
    "broadcaster": "TVING",
    "genre": "DATINGSHOW",
    "season": "시즌4",
    "episodeCount": 5,
    "missionCount": 24,
    "isOnAir": true,
    "isExposed": true,
    "registeredDate": "2026-05-31",
    "thumbnailImageUrl": "https://cdn.kpick.com/programs/transit-love-4.png"
  }
]
```

목록의 `missionCount`는 실제 연결된 미션 수를 조회 시점에 집계한다.

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| programId | Long | ✅ | 프로그램 식별값 |
| programName | String | ✅ | 프로그램명 |
| season | String |  | 시즌 |
| broadcaster | String | ✅ | 방송사 |
| genre | Genre | ✅ | 장르 |
| episodeCount | int | ✅ | 총 회차 수. 미정이면 `0` |
| missionCount | long | ✅ | 연결된 미션 수 |
| isOnAir | Boolean | ✅ | 방영중 여부 |
| isExposed | Boolean | ✅ | 앱 노출 여부 |
| registeredDate | String | ✅ | 등록일. `yyyy-MM-dd` |
| thumbnailImageUrl | String |  | 썸네일 이미지 URL |

## 프로그램 상세 조회

`GET /api/admin/programs/{programId}/details`

### Response

```json
{
  "programId": 1,
  "programName": "환승연애4",
  "broadcaster": "TVING",
  "genre": "DATINGSHOW",
  "season": "시즌4",
  "episodeCount": 5,
  "broadcastStartDate": "2025-02-15",
  "broadcastEndDate": null,
  "registeredDate": "2026-05-31",
  "thumbnailImageUrl": "https://cdn.kpick.com/programs/transit-love-4.png",
  "isOnAir": true,
  "isExposed": true,
  "description": "X와 재회할지 환승할지",
  "missionCount": 2,
  "missions": [
    {
      "missionId": 10,
      "missionName": "5화 마지막 커플은 누구?",
      "dueDateTime": "2026-06-20",
      "attenderCount": 4821,
      "missionState": "ONGOING"
    }
  ]
}
```

## 프로그램 수정

`PATCH /api/admin/programs/{programId}`

### Request

수정할 필드만 전달한다.

```json
{
  "programName": "환승연애4",
  "broadcaster": "TVING",
  "genre": "DATINGSHOW",
  "season": "시즌4",
  "episodeCount": 5,
  "broadcastStartDate": "2025-02-15",
  "broadcastEndDate": null,
  "thumbnailImageUrl": "https://cdn.kpick.com/programs/transit-love-4.png",
  "isOnAir": true,
  "isExposed": true,
  "description": "수정된 설명"
}
```

### Response

`ProgramResponse`

```json
{
  "programId": 1,
  "programName": "환승연애4",
  "broadcaster": "TVING",
  "genre": "DATINGSHOW",
  "season": "시즌4",
  "episodeCount": 5,
  "broadcastStartDate": "2025-02-15",
  "broadcastEndDate": null,
  "registeredDate": "2026-05-31",
  "thumbnailImageUrl": "https://cdn.kpick.com/programs/transit-love-4.png",
  "isOnAir": true,
  "isExposed": true,
  "description": "수정된 설명",
  "missionCount": 5
}
```

## 프로그램 삭제

`DELETE /api/admin/programs/{programId}`

### Response

```text
204 No Content
```

---

# Mission Admin (관리자 미션)

▶ 미션 관리 목록 조회 : `GET /api/admin/missions?keyword=커플&programId=1&adminStatus=ONGOING` -완-
▶ 미션 생성 : `POST /api/admin/missions` -완-
▶ 미션 결과 확정 : `PATCH /api/admin/missions/{missionId}/confirm-result` -완-

## 미션 관리 목록 조회

`GET /api/admin/missions`
`GET /api/admin/missions?keyword=커플&programId=1&adminStatus=ONGOING`

관리자 미션 목록과 결과 입력 패널에 필요한 선택지별 참여 비율을 함께 조회한다.
`adminStatus`는 화면 표시용 상태다. 활성 미션의 마감일이 지나면 별도 수정 요청 없이 `CLOSED`로 계산되며, 결과가 확정되면 `RESULT_PUBLISHED`로 내려간다.

| Query | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| keyword | String |  | 프로그램명 또는 미션명 검색어 |
| programId | Long |  | 프로그램 필터 |
| adminStatus | AdminMissionStatus |  | 어드민 화면 상태 필터. `ONGOING`, `CLOSED`, `RESULT_PUBLISHED`, `INACTIVE` |

### Response

```json
[
  {
    "missionId": 10,
    "programId": 1,
    "programName": "나는솔로 10기",
    "episode": "최종회",
    "missionName": "최종 커플은 누구?",
    "adminStatus": "CLOSED",
    "missionState": "PENDING",
    "attenderCount": 6234,
    "dueDateTime": "2026-05-26",
    "coinFee": 50,
    "resultPublishTiming": "IMMEDIATE",
    "resultConfirmable": true,
    "options": [
      {
        "missionOptionId": 1,
        "content": "광수 & 옥순",
        "displayOrder": 1,
        "selectedCount": 4052,
        "selectedRate": 65,
        "isCorrect": false
      },
      {
        "missionOptionId": 2,
        "content": "영호 & 순자",
        "displayOrder": 2,
        "selectedCount": 2182,
        "selectedRate": 35,
        "isCorrect": false
      }
    ]
  }
]
```

## 미션 생성

`POST /api/admin/missions`

### Request

```json
{
  "programId": 1,
  "missionName": "5화 마지막 커플은 누구?",
  "episode": "5화",
  "dueDateTime": "2026-06-20",
  "coinFee": 50,
  "isActive": true,
  "options": [
    { "content": "수지 & 현준" },
    { "content": "민아 & 재준" }
  ]
}
```

### Response

```json
{
  "missionId": 10,
  "programId": 1,
  "profileId": 1,
  "missionName": "5화 마지막 커플은 누구?",
  "episode": "5화",
  "dueDateTime": "2026-06-20",
  "coinFee": 50,
  "attenderCount": 0,
  "isActive": true,
  "resultPublishTiming": "IMMEDIATE",
  "missionState": "ONGOING",
  "options": [
    {
      "missionOptionId": 1,
      "content": "수지 & 현준",
      "displayOrder": 1,
      "isCorrect": false
    }
  ]
}
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| missionId | Long | ✅ | 미션 식별값 |
| programId | Long | ✅ | 프로그램 식별값 |
| profileId | Long | ✅ | 생성자 프로필 식별값. JWT에서 추출하여 응답에만 포함 |
| missionName | String | ✅ | 미션명 |
| episode | String | ✅ | 회차 |
| dueDateTime | String | ✅ | 마감 날짜 |
| coinFee | int | ✅ | 참여 비용 |
| attenderCount | int | ✅ | 참여자 수 |
| isActive | Boolean | ✅ | 활성 여부. 생성 요청에서 생략하면 `true` |
| resultPublishTiming | ResultPublishTiming | ✅ | 결과 공개/처리 시점 |
| missionState | MissionState | ✅ | 미션 상태 |
| options | List<MissionOptionResponse> | ✅ | 선택지 목록 |

## 미션 결과 확정

`PATCH /api/admin/missions/{missionId}/confirm-result`

### Request

```json
{
  "correctMissionOptionId": 1,
  "resultPublishTiming": "IMMEDIATE"
}
```

### Response

`MissionResponse`

```json
{
  "missionId": 10,
  "programId": 1,
  "profileId": 1,
  "missionName": "5화 마지막 커플은 누구?",
  "episode": "5화",
  "dueDateTime": "2026-06-20",
  "coinFee": 50,
  "attenderCount": 4821,
  "isActive": true,
  "resultPublishTiming": "IMMEDIATE",
  "missionState": "COMPLETED",
  "options": [
    {
      "missionOptionId": 1,
      "content": "수지 & 현준",
      "displayOrder": 1,
      "isCorrect": true
    }
  ]
}
```

---

# Mission (미션)

▶ 상태별 미션 목록 조회 : `GET /api/missions/status?missionStatus=ONGOING` (현재 컨트롤러에서 주석 처리됨)
▶ 홈 미션 추천리스트 조회 : `GET /api/missions/recommendations` -완-
▶ 홈 미션 추천리스트 프로그램 필터 조회 : `GET /api/missions/recommendations?programId=1` -완-
▶ 관련 미션/스레드 추천 조회 : `GET /api/missions/{missionId}/related` -완-
▶ 미션 상세 조회 : `GET /api/missions/{missionId}` -완-
▶ 미션 선택 및 참여 : `POST /api/missions/{missionId}/options/select` -완-

## 상태별 미션 목록 조회

`GET /api/missions/status?missionStatus=ONGOING`

현재 코드 기준 `MissionController`에서 매핑이 주석 처리되어 있어 실제 호출 가능한 API는 아니다. 아래 Response Body는 `MissionListResponse` 기준이다.

### Response

```json
[
  {
    "missionId": 10,
    "programId": 1,
    "programName": "환승연애4",
    "season": "시즌4",
    "missionName": "5화 마지막 커플은 누구?",
    "coinFee": 50,
    "attenderCount": 4821,
    "missionState": "ONGOING"
  }
]
```

## 홈 미션 추천리스트 조회

`GET /api/missions/recommendations`
`GET /api/missions/recommendations?programId=1`

`programId`가 없으면 JWT로 인증된 사용자의 관심 프로그램에 포함된 진행중 미션을 최신 생성 기준(`missionId DESC`)으로 조회한다. `programId`가 있으면 해당 프로그램의 진행중 미션만 조회한다.

### Response

```json
[
  {
    "missionId": 12,
    "programId": 2,
    "programName": "나는 솔로",
    "genre": "DATINGSHOW",
    "season": "10기",
    "missionName": "차돌 1호는?",
    "dueDateTime": "2026-06-20",
    "coinFee": 50,
    "attenderCount": 2400,
    "missionState": "ONGOING",
    "options": [
      {
        "missionOptionId": 21,
        "content": "광수 & 옥순",
        "displayOrder": 1
      },
      {
        "missionOptionId": 22,
        "content": "성훈 & 영숙",
        "displayOrder": 2
      }
    ]
  }
]
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| missionId | Long | ✅ | 미션 식별값 |
| programId | Long | ✅ | 프로그램 식별값 |
| programName | String | ✅ | 프로그램명 |
| genre | Genre | ✅ | 프로그램 장르 |
| season | String | ✅ | 프로그램 시즌 |
| missionName | String | ✅ | 미션명 |
| dueDateTime | String | ✅ | 마감 날짜 |
| coinFee | int | ✅ | 참여 비용 |
| attenderCount | int | ✅ | 참여자 수 |
| missionState | MissionState | ✅ | 미션 상태 |
| options | List<MissionOptionResponse> | ✅ | 선택지 목록 |
| options[].missionOptionId | Long | ✅ | 선택지 식별값 |
| options[].content | String | ✅ | 선택지 내용 |
| options[].displayOrder | Integer | ✅ | 선택지 순서 |

## 관련 미션/스레드 추천 조회

`GET /api/missions/{missionId}/related`

미션 상세 하단의 관련 미션 1개, 관련 스레드 1개를 추천한다. 선택 우선순위는 `같은 프로그램의 같은 편 > 같은 프로그램 > 같은 카테고리`이며, 같은 우선순위 안에서는 가장 최신 항목을 선택한다. 현재 `Mission`에 회차 필드가 없어 같은 편 여부는 미션명 안의 `5화`, `10기` 같은 회차 토큰 기준으로 판단한다.

### Response

```json
{
  "relatedMission": {
    "missionId": 12,
    "programName": "나는 솔로",
    "season": "10기",
    "missionName": "나는솔로 10기 최종 커플",
    "coinFee": 30,
    "dueDateTime": "2026-06-20"
  },
  "relatedThread": {
    "threadId": 7,
    "programName": "환승연애4",
    "title": "환승연애 최종 커플",
    "description": "스레드 내용"
  }
}
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| relatedMission | RelatedMissionResponse |  | 관련 미션. 없으면 null |
| relatedMission.missionId | Long | ✅ | 미션 식별값 |
| relatedMission.programName | String | ✅ | 프로그램명 |
| relatedMission.season | String | ✅ | 시즌 |
| relatedMission.missionName | String | ✅ | 미션명 |
| relatedMission.coinFee | int | ✅ | 참여 비용 |
| relatedMission.dueDateTime | String | ✅ | 마감 날짜 |
| relatedThread | RelatedThreadResponse |  | 관련 스레드. 없으면 null |
| relatedThread.threadId | Long | ✅ | 스레드 식별값 |
| relatedThread.programName | String | ✅ | 프로그램명 |
| relatedThread.title | String | ✅ | 스레드 제목 |
| relatedThread.description | String | ✅ | 스레드 내용 |

## 미션 상세 조회

`GET /api/missions/{missionId}`

결과 확정 전 미션은 기존 상세 화면용 `MissionDetailsResponse`를 반환한다. 결과가 확정된 미션은 같은 URL에서 결과 화면용 `MissionResultResponse`를 반환한다.

### Response - 결과 확정 전

```json
{
  "missionId": 10,
  "programId": 1,
  "programName": "환승연애4",
  "missionName": "5화 마지막 커플은 누구?",
  "dueDateTime": "2026-06-20",
  "coinFee": 50,
  "profileCoin": 2400,
  "attenderCount": 4821,
  "missionState": "ONGOING",
  "resultConfirmed": false,
  "options": [
    {
      "missionOptionId": 1,
      "content": "수지 & 현준",
      "displayOrder": 1
    }
  ]
}
```

### Response - 결과 확정 후

```json
{
  "missionId": 10,
  "programId": 1,
  "programName": "환승연애4",
  "missionName": "5화 마지막 커플은 누구?",
  "missionState": "COMPLETED",
  "resultConfirmed": true,
  "totalAttenderCount": 4821,
  "correctMissionOptionId": 1,
  "correctOptionContent": "수지 & 현준",
  "selectedMissionOptionId": 1,
  "selectedOptionContent": "수지 & 현준",
  "isCorrect": true,
  "earnedPoint": 120,
  "optionResults": [
    {
      "missionOptionId": 1,
      "content": "수지 & 현준",
      "displayOrder": 1,
      "selectedCount": 2892,
      "selectedRate": 60
    }
  ]
}
```

## 미션 선택 및 참여

`POST /api/missions/{missionId}/options/select`

### Request

```json
{
  "missionOptionId": 1
}
```

### Response

```json
{
  "missionId": 10,
  "missionAttenderId": 100,
  "selectedMissionOptionId": 1,
  "selectedOptionContent": "수지 & 현준",
  "paidCoin": 50,
  "remainingCoin": 2350,
  "resultNotificationEnabled": true
}
```

---

# Mission Suggestion (미션 건의)

▶ 미션 건의 : `POST /api/mission-suggestions` -완-

## 미션 건의

`POST /api/mission-suggestions`

### Request

```json
{
  "programId": 4,
  "episode": "5화",
  "genre": "DATINGSHOW",
  "missionTitle": "5화 마지막 커플은 누구?",
  "options": [
    { "content": "수지 & 현준" },
    { "content": "민아 & 재준" }
  ]
}
```

### Response

```json
{
  "missionSuggestionId": 1,
  "status": "SUBMITTED"
}
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| missionSuggestionId | Long | ✅ | 생성된 미션 건의 식별값 |
| status | MissionSuggestionStatus | ✅ | 건의 처리 상태 |

---

# Community (커뮤니티)

▶ 커뮤니티 게시글 목록 조회 : `GET /api/community?postType=THREAD&genre=DATINGSHOW&programId=1` -완-
▶ 커뮤니티 게시글 생성 : `POST /api/community` -완-
▶ 커뮤니티 게시글 상세 조회 : `GET /api/community/posts/{postId}?postType=THREAD` -완-
▶ 커뮤니티 게시글 좋아요 : `POST /api/community/{postType}/{postId}/likes` -완-
▶ 커뮤니티 댓글 작성 : `POST /api/community/{postType}/{postId}/comments` -완-
▶ 커뮤니티 댓글 좋아요 : `POST /api/community/{postType}/comments/{communityCommentId}/likes` -완-
▶ 커뮤니티 게시글 수정 : `PATCH /api/community/{postType}/{postId}` -완-
▶ 커뮤니티 게시글 삭제 : `DELETE /api/community/{postType}/{postId}` -완-
▶ 유저투표 투표 : `POST /api/user-votes/{userVoteId}/vote` -완-
▶ 유저투표 결과 조회 : `GET /api/user-votes/{userVoteId}/result` -완-
▶ 어드민 커뮤니티 게시글 목록 조회 : `GET /api/admin/community/posts` -완-
▶ 어드민 커뮤니티 게시글 상세 조회 : `GET /api/admin/community/posts/{postType}/{postId}` -완-
▶ 어드민 커뮤니티 게시글 상태 수정 : `PATCH /api/admin/community/posts/{postType}/{postId}/status` -완-
▶ 어드민 커뮤니티 게시글 삭제 : `DELETE /api/admin/community/posts/{postType}/{postId}` -완-
▶ 어드민 커뮤니티 게시글 신고 반려 : `PATCH /api/admin/community/posts/{postType}/{postId}/reports/reject` -완-
▶ 어드민 커뮤니티 댓글 목록 조회 : `GET /api/admin/community/comments` -완-
▶ 어드민 커뮤니티 댓글 상태 수정 : `PATCH /api/admin/community/comments/{communityCommentId}/status` -완-
▶ 어드민 커뮤니티 댓글 삭제 : `DELETE /api/admin/community/comments/{communityCommentId}` -완-
▶ 어드민 커뮤니티 댓글 신고 반려 : `PATCH /api/admin/community/comments/{communityCommentId}/reports/reject` -완-

## 커뮤니티 게시글 목록 조회

`GET /api/community`

타입, 장르, 프로그램 필터는 모두 선택값이다.

`GET /api/community?postType=THREAD`
`GET /api/community?genre=DATINGSHOW`
`GET /api/community?programId=1`
`GET /api/community?postType=USER_VOTE&genre=DATINGSHOW&programId=1`

| Query | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| postType | CommunityPostType |  | 게시글 타입 |
| genre | Genre |  | 프로그램 장르 |
| programId | Long |  | 프로그램 식별값 |

### Response

```json
[
  {
    "postType": "USER_VOTE",
    "postId": 1,
    "profileId": 1,
    "nickname": "예능페인",
    "profileImageUrl": "https://cdn.example.com/profiles/profile-image.jpg",
    "programId": 1,
    "missionId": null,
    "title": "5화 최고 커플 예측!",
    "description": "누가 될까요?",
    "dueDateTime": "2026-06-20",
    "viewCount": 10,
    "likeCount": 3,
    "commentCount": 2,
    "voteCount": 21,
	    "isVoted": true,
	    "selectedUserVoteOptionId": 1,
    "userVoteOptions": [
      {
        "userVoteOptionId": 1,
        "content": "수지 & 현준",
        "displayOrder": 1,
        "isSelected": true
      },
      {
        "userVoteOptionId": 2,
        "content": "민아 & 재준",
        "displayOrder": 2,
        "isSelected": false
      }
    ],
    "sharedMission": null,
    "createdAt": "2026-05-30"
  }
]
```

`postType=USER_VOTE`로 조회하면 목록 카드에서 필요한 투표 요약 데이터(`voteCount`, `isVoted`, `selectedUserVoteOptionId`, `userVoteOptions`)가 함께 내려간다.

## 커뮤니티 게시글 생성

`POST /api/community`

### Request

```json
{
  "postType": "USER_VOTE",
  "programId": 1,
  "missionId": null,
  "title": "최종 커플 예측!",
  "description": null,
  "isNicknamePublic": null,
  "imageUrls": [],
  "dueDateTime": null,
  "options": [
    { "content": "수지 & 현준" },
    { "content": "민아 & 재준" }
  ]
}
```

미션 결과 화면에서 스레드로 공유할 때도 같은 생성 API를 사용한다. `postType=THREAD`, `missionId`, `title`, `description`을 넘기면 `programId`는 공유된 미션의 프로그램으로 자동 설정된다. 작성자는 JWT에서 식별한다.

### Mission Share Thread Request

```json
{
  "postType": "THREAD",
  "missionId": 10,
  "title": "수지현준 진짜 될 것 같아요!",
  "description": "5화 분위기 보면 확실할 것 같은데 여러분은 어떻게 생각하세요?",
  "isNicknamePublic": true,
  "imageUrls": [
    "https://cdn.example.com/threads/thread-image.jpg"
  ]
}
```

스레드는 `imageUrls`에 최대 5장까지 전달할 수 있다. 유저투표에서는 사용하지 않는다.

### Response

`CommunityPostResponse`

```json
{
  "postType": "USER_VOTE",
  "postId": 3,
  "profileId": 1,
  "programId": 1,
  "missionId": null,
  "title": "최종 커플 예측!",
  "description": null,
  "dueDateTime": null,
  "viewCount": 0,
  "likeCount": 0,
  "commentCount": 0,
  "voteCount": 0,
  "isVoted": false,
  "selectedUserVoteOptionId": null,
  "userVoteOptions": [],
  "sharedMission": null,
  "createdAt": "2026-05-30"
}
```

## 커뮤니티 게시글 상세 조회

`GET /api/community/posts/{postId}?postType=THREAD`
`GET /api/community/posts/{postId}?postType=USER_VOTE`

`postType`에 따라 스레드 상세 화면 또는 유저투표 상세/결과 화면에 맞는 응답을 반환한다.

### THREAD Response

```json
{
  "postType": "THREAD",
  "threadId": 1,
  "programId": 1,
  "programName": "환승연애4",
  "profileId": 1,
  "nickname": "드라마왕",
  "profileImageUrl": "https://cdn.example.com/profiles/profile-image.jpg",
  "title": "5화 봤어요?",
  "description": "수지 선택 예상했는데...",
  "isNicknamePublic": true,
  "isSharedFromMission": true,
  "sharedMission": {
    "missionId": 10,
    "missionName": "5화 마지막 커플은 누구?",
    "programId": 1,
    "programName": "환승연애4"
  },
  "imageUrls": [
    "https://cdn.example.com/threads/thread-image.jpg"
  ],
  "viewCount": 128,
  "likeCount": 24,
  "commentCount": 6,
  "comments": [
    {
      "communityCommentId": 1,
      "postType": "THREAD",
      "postId": 1,
      "profileId": 2,
      "content": "저도 그렇게 생각해요",
      "likeCount": 3,
      "createdAt": "2026-05-30"
    }
  ],
  "createdAt": "2026-05-30"
}
```

### USER_VOTE Response

```json
{
  "postType": "USER_VOTE",
  "userVoteId": 1,
  "profileId": 1,
  "programId": 1,
  "title": "최종 커플 예측!",
  "description": "누가 될까요?",
  "dueDateTime": "2026-06-20",
  "viewCount": 10,
  "likeCount": 3,
  "commentCount": 2,
  "voteCount": 21,
  "selectedOption": {
    "userVoteOptionId": 1,
    "content": "수지 & 현준",
    "displayOrder": 1,
    "voteCount": 12,
    "voteRate": 57
  },
  "options": [
    {
      "userVoteOptionId": 1,
      "content": "수지 & 현준",
      "displayOrder": 1,
      "voteCount": 12,
      "voteRate": 57
    }
  ],
  "comments": [],
  "createdAt": "2026-05-30"
}
```

## 커뮤니티 게시글 좋아요

`POST /api/community/{postType}/{postId}/likes`

### Request

```json
{}
```

### Response

```json
{
  "isLiked": true,
  "likeCount": 12
}
```

## 커뮤니티 댓글 작성

`POST /api/community/{postType}/{postId}/comments`

### Request

```json
{
  "content": "저도 그렇게 생각해요"
}
```

### Response

```json
{
  "communityCommentId": 1,
  "postType": "THREAD",
  "postId": 1,
  "profileId": 1,
  "content": "저도 그렇게 생각해요",
  "likeCount": 0,
  "createdAt": "2026-05-30"
}
```

## 커뮤니티 댓글 좋아요

`POST /api/community/{postType}/comments/{communityCommentId}/likes`

### Request

```json
{}
```

### Response

```json
{
  "isLiked": true,
  "likeCount": 4
}
```

## 커뮤니티 게시글 수정

`PATCH /api/community/{postType}/{postId}`

`postType`에 따라 스레드 또는 유저투표를 수정한다. 전달한 필드만 수정한다. 유저투표에서 `options`를 수정하면 기존 투표 기록은 초기화되고 선택지가 새로 저장된다.

### Request

```json
{
  "programId": 1,
  "title": "수정된 제목",
  "description": "수정된 내용",
  "isNicknamePublic": true,
  "dueDateTime": "2026-06-20",
  "options": [
    { "content": "수지 & 현준" },
    { "content": "민아 & 재준" }
  ]
}
```

### Response

`CommunityPostResponse`

```json
{
  "postType": "THREAD",
  "postId": 1,
  "profileId": 1,
  "programId": 1,
  "missionId": 10,
  "title": "수정된 제목",
  "description": "수정된 내용",
  "dueDateTime": null,
  "viewCount": 128,
  "likeCount": 24,
  "commentCount": 6,
  "voteCount": 0,
  "isVoted": null,
  "selectedUserVoteOptionId": null,
  "userVoteOptions": [],
  "createdAt": "2026-05-30"
}
```

## 커뮤니티 게시글 삭제

`DELETE /api/community/{postType}/{postId}`

### Response

```text
204 No Content
```

## 유저투표 투표

`POST /api/user-votes/{userVoteId}/vote`

### Request

```json
{
  "userVoteOptionId": 3
}
```

### Response

`UserVoteDetailsResponse`

```json
{
  "postType": "USER_VOTE",
  "userVoteId": 1,
  "profileId": 1,
  "programId": 1,
  "title": "5화 최고 커플 예측!",
  "description": "누가 될까요?",
  "dueDateTime": "2026-06-20",
  "viewCount": 10,
  "likeCount": 3,
  "commentCount": 2,
  "voteCount": 22,
  "selectedOption": {
    "userVoteOptionId": 3,
    "content": "민아 & 재준",
    "displayOrder": 2,
    "voteCount": 10,
    "voteRate": 45
  },
  "options": [],
  "comments": [],
  "createdAt": "2026-05-30"
}
```

투표 직후 이 응답으로 바로 투표 결과 화면을 보여줄 수 있다. 유저투표는 단순 여론조사라서 정답 필드는 없다.

## 유저투표 결과 조회

`GET /api/user-votes/{userVoteId}/result`

### Response

`UserVoteDetailsResponse`

```json
{
  "postType": "USER_VOTE",
  "userVoteId": 1,
  "profileId": 1,
  "programId": 1,
  "title": "환승연애 최종 커플 예측!",
  "description": "누가 될까요?",
  "dueDateTime": "2026-06-20",
  "viewCount": 10,
  "likeCount": 3,
  "commentCount": 2,
  "voteCount": 39,
  "selectedOption": {
    "userVoteOptionId": 1,
    "content": "수지 & 현준",
    "displayOrder": 1,
    "voteCount": 25,
    "voteRate": 64
  },
  "options": [
    {
      "userVoteOptionId": 1,
      "content": "수지 & 현준",
      "displayOrder": 1,
      "voteCount": 25,
      "voteRate": 64
    },
    {
      "userVoteOptionId": 2,
      "content": "민아 & 재준",
      "displayOrder": 2,
      "voteCount": 14,
      "voteRate": 36
    }
  ],
  "comments": [],
  "createdAt": "2026-05-30"
}
```

## 어드민 커뮤니티 게시글 목록 조회

`GET /api/admin/community/posts?postType=THREAD&keyword=커플&contentStatus=NORMAL&minReportCount=5`

게시글 탭은 `postType=THREAD`, 유저투표 탭은 `postType=USER_VOTE`로 조회한다. `postType`을 생략하면 두 타입을 함께 반환한다.

| 파라미터 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| postType | CommunityPostType |  | `THREAD`, `USER_VOTE` |
| keyword | String |  | 제목 검색 |
| contentStatus | CommunityContentStatus |  | `NORMAL`, `HIDDEN` |
| minReportCount | Integer |  | 신고 누적 개수 하한. 신고 5건 이상 탭은 `5` 전달 |

### Response

```json
[
  {
    "postType": "THREAD",
    "postId": 1,
    "profileId": 3,
    "nickname": "드라마왕",
    "programId": 2,
    "title": "수지현준 진짜 될 것 같아요!",
    "createdAt": "2026-05-28",
    "reportCount": 6,
    "contentStatus": "NORMAL"
  }
]
```

## 어드민 커뮤니티 게시글 상세 조회

`GET /api/admin/community/posts/{postType}/{postId}`

스레드는 본문과 공유 미션 식별값, 유저투표는 투표 항목과 투표 수를 포함해 반환한다.

## 어드민 커뮤니티 게시글 상태 수정

`PATCH /api/admin/community/posts/{postType}/{postId}/status`

```json
{
  "contentStatus": "HIDDEN"
}
```

숨김 해제 시에는 `NORMAL`을 전달한다. 숨김 게시글은 일반 사용자 목록, 상세, 좋아요, 댓글, 투표 API에서 접근할 수 없다.

## 어드민 커뮤니티 게시글 삭제

`DELETE /api/admin/community/posts/{postType}/{postId}`

## 어드민 커뮤니티 게시글 신고 반려

`PATCH /api/admin/community/posts/{postType}/{postId}/reports/reject`

해당 게시글의 대기 중인 신고를 모두 `REJECTED` 처리한다.

## 어드민 커뮤니티 댓글 목록 조회

`GET /api/admin/community/comments?keyword=댓글&contentStatus=NORMAL&minReportCount=5`

### Response

```json
[
  {
    "communityCommentId": 1,
    "postType": "THREAD",
    "postId": 1,
    "profileId": 4,
    "nickname": "냥토리",
    "content": "저도 A 찍었어요!",
    "createdAt": "2026-05-28",
    "reportCount": 1,
    "contentStatus": "NORMAL"
  }
]
```

## 어드민 커뮤니티 댓글 상태 수정

`PATCH /api/admin/community/comments/{communityCommentId}/status`

요청 형식은 게시글 상태 수정과 동일하다. 숨김 댓글은 일반 사용자 상세 조회에서 제외된다.

## 어드민 커뮤니티 댓글 삭제

`DELETE /api/admin/community/comments/{communityCommentId}`

## 어드민 커뮤니티 댓글 신고 반려

`PATCH /api/admin/community/comments/{communityCommentId}/reports/reject`

---

# Ranking (랭킹)

▶ 랭킹 조회 : `GET /api/rankings?rankingType=SEASON` -완-
▶ 랭킹 프로필 모달 조회 : `GET /api/rankings/profiles/{profileId}` -완-
▶ 성장 기록 조회 : `GET /api/rankings/profiles/me/growth` -완-
▶ 어드민 시즌 등록 : `POST /api/admin/rankings/seasons` -완-
▶ 어드민 시즌 현황 조회 : `GET /api/admin/rankings/seasons` -완-
▶ 어드민 시즌 보상 대상자 조회 : `GET /api/admin/rankings/seasons/{rankingSeasonId}/rewards` -완-
▶ 어드민 시즌 보상 일괄 발송 : `PATCH /api/admin/rankings/seasons/{rankingSeasonId}/rewards/send-all` -완-
▶ 어드민 시즌 보상 개별 발송 : `PATCH /api/admin/rankings/seasons/{rankingSeasonId}/rewards/{seasonRewardId}/send` -완-
▶ 현재 시즌 종료 : `PATCH /api/admin/rankings/current-season/close` -완-

## 랭킹 조회

`GET /api/rankings?rankingType=SEASON`

| Query | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| rankingType | String |  | SEASON, COMMUNITY, TOTAL. 기본값 SEASON |

`rankingType=SEASON`, `rankingType=TOTAL`은 `RankingResponse`를 반환하고, `rankingType=COMMUNITY`는 커뮤니티 활동 점수 전용 필드가 있는 `CommunityRankingResponse`를 반환한다.

### SEASON Response

`GET /api/rankings?rankingType=SEASON`

```json
{
  "rankingType": "SEASON",
  "seasonName": "2026 시즌 5",
  "seasonStartDate": "2026-05-01",
  "seasonEndDate": "2026-05-31",
  "daysUntilSeasonEnd": 1,
  "totalUserCount": 4821,
  "topThree": [
    {
      "rank": 1,
      "profileId": 1,
      "nickname": "미션마스터",
      "score": 6240,
      "pointTier": {
        "step": 7,
        "tierName": "베테랑토리",
        "subLevel": 2,
        "displayName": "베테랑토리 2",
        "minPoint": 5500,
        "maxPoint": 8999,
        "nextTierName": "마스터토리",
        "pointsToNextTier": 2760
      }
    }
  ],
  "rankings": [],
  "myRanking": {
    "rank": 9,
    "profileId": 10,
    "nickname": "나의닉네임",
    "score": 285,
    "pointTier": {
      "step": 2,
      "tierName": "아기토리",
      "subLevel": 1,
      "displayName": "아기토리 1",
      "minPoint": 100,
      "maxPoint": 299,
      "nextTierName": "꼬마토리",
      "pointsToNextTier": 15
    },
    "nextStepMessage": "꼬마토리까지 15pt 남음"
  }
}
```

### TOTAL Response

`GET /api/rankings?rankingType=TOTAL`

```json
{
  "rankingType": "TOTAL",
  "seasonName": "전체",
  "seasonStartDate": null,
  "seasonEndDate": null,
  "daysUntilSeasonEnd": null,
  "totalUserCount": 4821,
  "topThree": [
    {
      "rank": 1,
      "profileId": 1,
      "nickname": "피켓토리",
      "score": 6200,
      "pointTier": {
        "step": 7,
        "tierName": "베테랑토리",
        "subLevel": 2,
        "displayName": "베테랑토리 2",
        "minPoint": 5500,
        "maxPoint": 8999,
        "nextTierName": "마스터토리",
        "pointsToNextTier": 2800
      }
    }
  ],
  "rankings": [
    {
      "rank": 4,
      "profileId": 4,
      "nickname": "드라마킹",
      "score": 2870,
      "pointTier": {
        "step": 5,
        "tierName": "새내기토리",
        "subLevel": 2,
        "displayName": "새내기토리 2",
        "minPoint": 1500,
        "maxPoint": 2999,
        "nextTierName": "직장인토리",
        "pointsToNextTier": 130
      }
    }
  ],
  "myRanking": {
    "rank": 12,
    "profileId": 10,
    "nickname": "나의닉네임",
    "score": 520,
    "pointTier": {
      "step": 3,
      "tierName": "꼬마토리",
      "subLevel": 2,
      "displayName": "꼬마토리 2",
      "minPoint": 300,
      "maxPoint": 699,
      "nextTierName": "사춘기토리",
      "pointsToNextTier": 180
    },
    "nextStepMessage": "꼬마토리 1까지 180pt 남음"
  }
}
```

### COMMUNITY Response

`GET /api/rankings?rankingType=COMMUNITY`

```json
{
  "rankingType": "COMMUNITY",
  "periodName": "커뮤니티",
  "periodStartDate": "2026-05-01",
  "periodEndDate": "2026-05-31",
  "daysUntilPeriodEnd": 1,
  "totalUserCount": 4821,
  "topThree": [
    {
      "rank": 1,
      "profileId": 1,
      "nickname": "수다토리",
      "score": 510,
      "pointTier": null,
      "threadCount": 12,
      "commentCount": 38
    }
  ],
  "rankings": [],
  "myRanking": {
    "rank": 7,
    "profileId": 10,
    "nickname": "나의닉네임",
    "activityPoint": 90,
    "threadCount": 2,
    "commentCount": 11,
    "activityHistoryMessage": "글 2 · 댓글 11"
  }
}
```

## 랭킹 프로필 모달 조회

`GET /api/rankings/profiles/{profileId}`

### Response

```json
{
  "profileId": 1,
  "nickname": "예능픽",
  "pointTier": {
    "step": 5,
    "tierName": "새내기토리",
    "subLevel": 2,
    "displayName": "새내기토리 2",
    "minPoint": 1500,
    "maxPoint": 2999,
    "nextTierName": "직장인토리",
    "pointsToNextTier": 1180
  },
  "seasonRank": 2,
  "totalRank": 8,
  "seasonPoint": 1820,
  "totalMissionPoint": 4302,
  "correctRate": 87,
  "specialBadges": []
}
```

## 성장 기록 조회

`GET /api/rankings/profiles/me/growth?pointScope=TOTAL`

### Response

```json
{
  "profileId": 1,
  "nickname": "나의닉네임",
  "pointScope": "TOTAL",
  "point": 520,
  "currentTier": {
    "step": 3,
    "tierName": "꼬마토리",
    "subLevel": 2,
    "displayName": "꼬마토리 2",
    "minPoint": 300,
    "maxPoint": 699,
    "nextTierName": "사춘기토리",
    "pointsToNextTier": 180
  },
  "nextTierMessage": "사춘기토리까지 180pt 남음",
  "tiers": [
    {
      "step": 1,
      "tierName": "알토리",
      "minPoint": 0,
      "maxPoint": 99,
      "subLevel": null,
      "completed": true,
      "current": false
    }
  ]
}
```

## 현재 시즌 종료

`PATCH /api/admin/rankings/current-season/close`

진행 중 시즌의 상위 보상 대상자를 스냅샷으로 저장하고 시즌 포인트를 30% 감소시킨다.

### Response

```json
{
  "message": "Current season closed. Points decreased by 30%.",
  "affectedProfileCount": 4821,
  "decayRate": 30
}
```

## 어드민 시즌 등록

`POST /api/admin/rankings/seasons`

진행 중 시즌은 한 번에 하나만 등록할 수 있다.

### Request

```json
{
  "seasonName": "시즌 1",
  "startDate": "2026-04-01",
  "endDate": "2026-06-09",
  "rewardTopN": 5,
  "rewardDescription": "올리브영 기프트카드 5만원"
}
```

## 어드민 시즌 현황 조회

`GET /api/admin/rankings/seasons`

### Response

```json
[
  {
    "rankingSeasonId": 1,
    "seasonName": "시즌 1",
    "startDate": "2026-04-01",
    "endDate": "2026-06-09",
    "seasonStatus": "ACTIVE",
    "participantCount": 28491,
    "participationCount": 142830,
    "rewardTopN": 5,
    "rewardDescription": "올리브영 기프트카드 5만원"
  }
]
```

## 어드민 시즌 보상 대상자 조회

`GET /api/admin/rankings/seasons/{rankingSeasonId}/rewards`

진행 중 시즌은 현재 포인트 순위 기준 미리보기이며 `seasonRewardId`가 `null`이다.
시즌 종료 후에는 종료 시점에 저장된 순위 스냅샷과 발송 상태를 반환한다.

### Response

```json
[
  {
    "seasonRewardId": 1,
    "profileId": 2,
    "rank": 1,
    "nickname": "드라마왕",
    "seasonPoint": 142830,
    "rewardDescription": "올리브영 기프트카드 5만원",
    "recipientInfoSubmitted": false,
    "rewardStatus": "NOT_SENT"
  }
]
```

## 어드민 시즌 보상 발송

일괄 발송:

`PATCH /api/admin/rankings/seasons/{rankingSeasonId}/rewards/send-all`

개별 발송:

`PATCH /api/admin/rankings/seasons/{rankingSeasonId}/rewards/{seasonRewardId}/send`

보상 발송 처리는 종료된 시즌에서만 가능하다.

---

# Benefit (혜택)

▶ 혜택 홈 조회 : `GET /api/benefits/me` -완-
▶ 오늘 출석하기 : `POST /api/benefits/me/attendance` -완-
▶ 광고 Pick 지급 : `POST /api/benefits/me/ad-reward` -완-
▶ 관리자 Pick 지급 내역 조회 : `GET /api/admin/benefits/pick-histories?pickHistoryType=ATTENDANCE&fromDate=2026-05-01&toDate=2026-05-31` -완-
▶ 관리자 혜택 설정 조회 : `GET /api/admin/benefits/setting` -완-
▶ 관리자 혜택 설정 저장 : `PATCH /api/admin/benefits/setting` -완-

## 혜택 홈 조회

`GET /api/benefits/me`

### Response

```json
{
  "profileId": 1,
  "attendance": {
    "consecutiveAttendanceDays": 4,
    "dailyAttendancePick": 3,
    "weeklyAttendanceBonusPick": 3,
    "checkedToday": false,
    "week": [
      {
        "attendanceDate": "2026-05-25",
        "dayOfWeek": "MONDAY",
        "rewardPick": 3,
        "checked": true,
        "today": false,
        "weeklyBonusDay": false
      }
    ]
  },
  "ad": {
    "dailyAdLimit": 10,
    "watchedCount": 0,
    "remainingCount": 10,
    "rewardPick": 1
  },
  "miniGames": [
    {
      "gameCode": "OX_QUIZ",
      "gameName": "OX 퀴즈",
      "status": "COMING_SOON"
    }
  ]
}
```

## 오늘 출석하기

`POST /api/benefits/me/attendance`

하루 한 번만 출석 가능하다. 기본 지급 Pick과 7일 연속 출석 보너스를 합산해 프로필 Pick에 반영한다.

### Response

```json
{
  "profileId": 1,
  "attendanceDate": "2026-05-30",
  "earnedPick": 3,
  "consecutiveAttendanceDays": 4,
  "weeklyBonusEarned": false,
  "currentCoin": 15,
  "week": []
}
```

## 광고 Pick 지급

`POST /api/benefits/me/ad-reward`

광고 시청 완료 후 호출한다. 하루 지급 가능 횟수를 넘으면 실패한다.

### Response

```json
{
  "profileId": 1,
  "rewardDate": "2026-05-30",
  "earnedPick": 1,
  "watchedCount": 1,
  "remainingCount": 9,
  "currentCoin": 16
}
```

## 관리자 Pick 지급 내역 조회

`GET /api/admin/benefits/pick-histories`
`GET /api/admin/benefits/pick-histories?pickHistoryType=ATTENDANCE`
`GET /api/admin/benefits/pick-histories?fromDate=2026-05-01&toDate=2026-05-31`

| Query | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| pickHistoryType | PickHistoryType |  | ATTENDANCE, ATTENDANCE_BONUS, AD_REWARD, MISSION_PARTICIPATION, MISSION_REWARD, ADMIN |
| fromDate | String |  | 조회 시작일. `yyyy-MM-dd` |
| toDate | String |  | 조회 종료일. `yyyy-MM-dd` |

### Response

```json
{
  "pickHistoryType": "ATTENDANCE",
  "totalCount": 1,
  "histories": [
    {
      "pickHistoryId": 1,
      "profileId": 1,
      "nickname": "냥토리",
      "pickHistoryType": "ATTENDANCE",
      "amount": 3,
      "description": "출석체크",
      "createdAt": "2026-05-30"
    }
  ]
}
```

## 관리자 혜택 설정 조회

`GET /api/admin/benefits/setting`

### Response

```json
{
  "benefitSettingId": 1,
  "dailyAttendancePick": 3,
  "weeklyAttendanceBonusPick": 3,
  "dailyAdLimit": 10,
  "adRewardPick": 1
}
```

## 관리자 혜택 설정 저장

`PATCH /api/admin/benefits/setting`

### Request

```json
{
  "dailyAttendancePick": 3,
  "weeklyAttendanceBonusPick": 3,
  "dailyAdLimit": 10,
  "adRewardPick": 1
}
```

### Response

`BenefitSettingResponse`

---

# Inquiry (문의)

▶ 문의 등록 : `POST /api/inquiries` -완-
▶ 어드민 문의 목록 조회 : `GET /api/admin/inquiries` -완-
▶ 어드민 문의 상세 조회 : `GET /api/admin/inquiries/{inquiryId}` -완-
▶ 어드민 문의 답변 임시저장 : `PATCH /api/admin/inquiries/{inquiryId}/draft` -완-
▶ 어드민 문의 답변 이메일 발송 : `POST /api/admin/inquiries/{inquiryId}/reply` -완-

## 문의 등록

`POST /api/inquiries`

문의하기 화면에서 문의 유형, 제목, 내용, 첨부 이미지, 답변 받을 이메일, 개인정보 수집 동의 값을 저장한다.

### Request

```json
{
  "inquiryType": "SERVICE_USAGE",
  "title": "서비스 이용 문의입니다",
  "content": "문의 내용을 자세히 입력합니다.",
  "imageUrls": [
    "https://example.com/image1.png"
  ],
  "replyEmail": "example@email.com",
  "privacyAgreed": true
}
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| inquiryType | InquiryType | ✅ | SERVICE_USAGE, ACCOUNT, PAYMENT, BUG_REPORT, ETC |
| title | String | ✅ | 문의 제목 |
| content | String | ✅ | 문의 내용. 최대 500자 |
| imageUrls | List<String> |  | 첨부 이미지 URL. 최대 3장 |
| replyEmail | String | ✅ | 답변 받을 이메일 |
| privacyAgreed | Boolean | ✅ | 개인정보 수집 동의 여부. true 필수 |

### Response

```json
{
  "inquiryId": 1,
  "profileId": 1,
  "inquiryType": "SERVICE_USAGE",
  "title": "서비스 이용 문의입니다",
  "imageUrls": [
    "https://example.com/image1.png"
  ],
  "inquiryStatus": "RECEIVED",
  "createdAt": "2026-05-30"
}
```

## 어드민 문의 목록 조회

`GET /api/admin/inquiries?answered=false&inquiryType=ACCOUNT`

| 파라미터 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| answered | Boolean |  | false: 미답변 탭, true: 답변완료 탭, 미입력: 전체 |
| inquiryType | InquiryType |  | 문의 유형 필터 |

### Response

`List<AdminInquiryResponse>`

```json
[
  {
    "inquiryId": 1,
    "profileId": 3,
    "nickname": "냥토리",
    "inquiryType": "ACCOUNT",
    "title": "카카오 로그인 후 기록이 없어졌어요",
    "content": "기존 Pick 기록과 포인트가 모두 사라졌습니다.",
    "imageUrls": ["https://example.com/screenshot.png"],
    "replyEmail": "user@example.com",
    "inquiryStatus": "RECEIVED",
    "answerDraft": null,
    "answerContent": null,
    "createdAt": "2026-05-28",
    "answeredAt": null
  }
]
```

## 어드민 문의 상세 조회

`GET /api/admin/inquiries/{inquiryId}`

목록 응답과 동일한 `AdminInquiryResponse` 한 건을 반환한다.

## 어드민 문의 답변 임시저장

`PATCH /api/admin/inquiries/{inquiryId}/draft`

### Request

```json
{
  "answerDraft": "확인 후 답변드리겠습니다."
}
```

### Response

`AdminInquiryResponse`

## 어드민 문의 답변 이메일 발송

`POST /api/admin/inquiries/{inquiryId}/reply`

### Request

```json
{
  "answerContent": "계정 연동 상태를 확인했습니다. 다시 로그인해주세요."
}
```

### Response

```json
{
  "inquiryId": 1,
  "inquiryStatus": "ANSWERED",
  "replyEmail": "user@example.com",
  "emailSent": true,
  "answeredAt": "2026-05-31"
}
```

배포 환경에서 `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`를 설정하면 SMTP로 답변을 발송한다. 메일 설정이 없는 로컬 환경에서는 답변 내용과 상태를 저장하고 `emailSent=false`를 반환한다.

---

# Notice (공지사항)

▶ 어드민 공지사항 등록 : `POST /api/admin/notices` -완-
▶ 어드민 공지사항 목록 조회 : `GET /api/admin/notices` -완-
▶ 어드민 공지사항 공개 상태 수정 : `PATCH /api/admin/notices/{noticeId}/status` -완-
▶ 사용자 공지사항 목록 조회 : `GET /api/notices` -완-
▶ 사용자 공지사항 상세 조회 : `GET /api/notices/{noticeId}` -완-

## 어드민 공지사항 등록

`POST /api/admin/notices`

### Request

```json
{
  "title": "픽토리 서비스 오픈 안내",
  "content": "안녕하세요, 픽토리입니다.\n픽토리 서비스가 정식 오픈했어요!",
  "noticeStatus": "PUBLISHED"
}
```

`noticeStatus`는 `PUBLISHED`, `HIDDEN` 중 하나이며 생략하면 `PUBLISHED`로 저장한다.

### Response

```json
{
  "noticeId": 1,
  "title": "픽토리 서비스 오픈 안내",
  "content": "안녕하세요, 픽토리입니다.\n픽토리 서비스가 정식 오픈했어요!",
  "noticeStatus": "PUBLISHED",
  "createdAt": "2026-06-01",
  "updatedAt": "2026-06-01"
}
```

## 어드민 공지사항 목록 조회

`GET /api/admin/notices?noticeStatus=PUBLISHED`

`noticeStatus`를 생략하면 게시 및 숨김 공지를 모두 반환한다.

### Response

```json
[
  {
    "noticeId": 1,
    "title": "픽토리 서비스 오픈 안내",
    "noticeStatus": "PUBLISHED",
    "createdAt": "2026-06-01"
  }
]
```

## 어드민 공지사항 공개 상태 수정

`PATCH /api/admin/notices/{noticeId}/status`

### Request

```json
{
  "noticeStatus": "HIDDEN"
}
```

### Response

`NoticeDetailsResponse`

## 사용자 공지사항 목록 조회

`GET /api/notices`

`PUBLISHED` 상태인 공지만 최신순으로 반환한다.

### Response

`List<NoticeListResponse>`

## 사용자 공지사항 상세 조회

`GET /api/notices/{noticeId}`

### Response

`NoticeDetailsResponse`

---

# Push Notification (푸시 알림)

▶ 알림 설정 조회 : `GET /api/notifications/me/settings` -완-
▶ 알림 설정 수정 : `PATCH /api/notifications/me/settings` -완-
▶ 디바이스 토큰 등록 : `POST /api/notifications/me/device-tokens` -완-
▶ 내 알림 목록 조회 : `GET /api/notifications/me` -완-
▶ 알림 읽음 처리 : `PATCH /api/notifications/me/{pushNotificationId}/read` -완-
▶ 이벤트·업데이트 알림 일괄 생성 : `POST /api/admin/notifications/broadcast` -완-

현재 구현은 푸시 발송 대기 내역을 DB에 저장하는 기본 구조다. 디바이스 토큰이 있으면 `PENDING`, 없으면 `STORED` 상태로 저장한다. 실제 FCM 전송은 추후 dispatcher에서 `PENDING` 내역을 처리하도록 연결하면 된다.

## 알림 설정 조회

`GET /api/notifications/me/settings`

### Response

```json
{
  "profileId": 1,
  "missionResultEnabled": true,
  "pointRewardEnabled": true,
  "interestedProgramEnabled": false,
  "commentEnabled": true,
  "likeEnabled": false,
  "rankingTierChangeEnabled": true,
  "specialBadgeEnabled": true,
  "growthBadgeLevelUpEnabled": false,
  "eventNoticeEnabled": true,
  "updateNoticeEnabled": false
}
```

## 알림 설정 수정

`PATCH /api/notifications/me/settings`

수정할 필드만 전달한다.

```json
{
  "missionResultEnabled": true,
  "commentEnabled": false,
  "likeEnabled": true
}
```

## 디바이스 토큰 등록

`POST /api/notifications/me/device-tokens`

```json
{
  "deviceToken": "fcm.device.token",
  "platform": "IOS"
}
```

## 내 알림 목록 조회

`GET /api/notifications/me`

```json
[
  {
    "pushNotificationId": 1,
    "notificationType": "MISSION_RESULT",
    "title": "미션 결과가 확정되었어요",
    "body": "5화 마지막 커플은 누구? 결과를 확인해보세요.",
    "targetType": "MISSION",
    "targetId": 10,
    "deliveryStatus": "PENDING",
    "createdAt": "2026-05-31",
    "readAt": null
  }
]
```

## 이벤트·업데이트 알림 일괄 생성

`POST /api/admin/notifications/broadcast`

`notificationType`은 `EVENT_NOTICE`, `UPDATE_NOTICE`만 허용한다.

```json
{
  "notificationType": "EVENT_NOTICE",
  "title": "새로운 이벤트가 시작됐어요",
  "body": "혜택 탭에서 이벤트를 확인해보세요.",
  "targetType": "BENEFIT",
  "targetId": null
}
```

---

# Report (신고 관리)

▶ 신고 생성 : `POST /api/reports` -완-
▶ 어드민 신고 목록 조회 : `GET /api/admin/reports?targetType=POST&reportStatus=WAITING` -완-
▶ 어드민 유저 제재 : `POST /api/admin/users/{profileId}/sanctions` -완-

## 신고 생성

`POST /api/reports`

일반 유저가 글, 댓글, 유저를 신고한다. 신고자 `profileId`는 JWT에서 추출한다.
글 신고일 때만 `postType`을 함께 전달한다.

### Request - 글 신고

```json
{
  "targetType": "POST",
  "postType": "THREAD",
  "targetId": 10,
  "reason": "ABUSE_PROFANITY",
  "reasonDetail": "욕설이 포함되어 있습니다."
}
```

### Request - 댓글 신고

```json
{
  "targetType": "COMMENT",
  "targetId": 20,
  "reason": "SPAM"
}
```

### Request - 유저 신고

```json
{
  "targetType": "USER",
  "targetId": 3,
  "reason": "OTHER",
  "reasonDetail": "반복적으로 다른 유저를 괴롭힙니다."
}
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| targetType | ReportTargetType | ✅ | 신고 대상. `POST`, `COMMENT`, `USER` |
| postType | CommunityPostType |  | 글 신고 시 필수. `THREAD`, `USER_VOTE` |
| targetId | Long | ✅ | 신고 대상 식별값 |
| reason | ReportReason | ✅ | `ABUSE_PROFANITY`, `SPAM`, `OBSCENE`, `FRAUD`, `COPYRIGHT`, `OTHER` |
| reasonDetail | String |  | 상세 사유 |

### Response

```json
{
  "reportId": 1,
  "reportStatus": "WAITING",
  "createdAt": "2026-05-31"
}
```

## 어드민 신고 목록 조회

`GET /api/admin/reports`
`GET /api/admin/reports?targetType=POST&reportStatus=WAITING`

| Query | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| targetType | ReportTargetType |  | `POST`, `COMMENT`, `USER` |
| reportStatus | ReportStatus |  | `WAITING`, `PROCESSED`, `REJECTED` |

### Response

```json
[
  {
    "reportId": 1,
    "targetType": "POST",
    "postType": "THREAD",
    "targetId": 10,
    "targetSummary": "수지현준 진짜 될 것 같아요!",
    "targetProfileId": 3,
    "targetNickname": "픽왕토리",
    "reporterProfileId": 5,
    "reporterNickname": "낭토리",
    "reason": "ABUSE_PROFANITY",
    "reasonDetail": "욕설이 포함되어 있습니다.",
    "reportStatus": "WAITING",
    "createdAt": "2026-05-31",
    "processedAt": null
  }
]
```

## 어드민 유저 제재

`POST /api/admin/users/{profileId}/sanctions`

`relatedReportId`를 전달하면 해당 신고를 `PROCESSED`로 처리한다. 신고 목록을 거치지 않고 유저 관리에서 바로 제재할 때는 생략한다.

### Request

```json
{
  "relatedReportId": 1,
  "sanctionType": "SUSPEND_7_DAYS",
  "reason": "도배 및 스팸 반복",
  "notifyUser": true
}
```

| sanctionType | 설명 |
| --- | --- |
| WARNING | 경고 |
| SUSPEND_7_DAYS | 7일 정지 |
| SUSPEND_30_DAYS | 30일 정지 |
| PERMANENT | 영구 정지 |

### Response

```json
{
  "userSanctionId": 1,
  "profileId": 3,
  "relatedReportId": 1,
  "sanctionType": "SUSPEND_7_DAYS",
  "reason": "도배 및 스팸 반복",
  "notifyUser": true,
  "startsAt": "2026-05-31",
  "endsAt": "2026-06-07"
}
```

---

# User Admin (유저 관리)

▶ 어드민 유저 목록 조회 : `GET /api/admin/users?keyword=낭토리&userStatus=NORMAL` -완-
▶ 어드민 유저 상세 조회 : `GET /api/admin/users/{profileId}` -완-

## 어드민 유저 목록 조회

`GET /api/admin/users`
`GET /api/admin/users?keyword=낭토리&userStatus=NORMAL`

| Query | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| keyword | String |  | 닉네임 또는 이메일 검색어 |
| userStatus | AdminUserStatus |  | `NORMAL`, `SUSPENDED`, `PERMANENTLY_SUSPENDED` |

### Response

```json
[
  {
    "profileId": 1,
    "nickname": "낭토리",
    "email": "nang@example.com",
    "pointTierName": "꼬마토리 2",
    "coin": 12,
    "joinedAt": "2026-03-12",
    "userStatus": "NORMAL"
  }
]
```

## 어드민 유저 상세 조회

`GET /api/admin/users/{profileId}`

상세 패널의 기본 정보와 미션 참여 기록, 커뮤니티 활동, Pick 내역을 함께 조회한다.
`correctRate`는 결과가 공개된 참여 미션 중 정답 미션 비율이다.

### Response

```json
{
  "profileId": 1,
  "nickname": "낭토리",
  "email": "nang@example.com",
  "loginType": "KAKAO",
  "pointTierName": "꼬마토리 2",
  "coin": 12,
  "totalPoint": 520,
  "correctRate": 74,
  "userStatus": "NORMAL",
  "currentSanctionType": null,
  "sanctionEndsAt": null,
  "joinedAt": "2026-03-12",
  "lastLoginAt": "2026-05-28",
  "missionHistories": [
    {
      "missionId": 10,
      "programId": 1,
      "programName": "환승연애4",
      "episode": "5화",
      "missionName": "마지막 커플은 누구?",
      "selectedMissionOptionId": 1,
      "selectedOptionContent": "수지 & 현준",
      "correctMissionOptionId": 1,
      "correctOptionContent": "수지 & 현준",
      "missionResultStatus": "CORRECT",
      "coinFee": 50,
      "earnedPoint": 120,
      "dueDateTime": "2026-05-28"
    }
  ],
  "communityActivities": [],
  "pickHistories": []
}
```
