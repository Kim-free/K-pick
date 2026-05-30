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

---

# Auth (인증)

▶ Apple 로그인 : `POST /api/auth/apple` -완-

## Apple 로그인

`POST /api/auth/apple`

앱에서 Apple 로그인을 완료한 뒤 받은 `identityToken`을 서버로 전달한다. 이미 가입된 Apple 계정이면 로그인하고, 처음 보는 Apple 계정이면 `AppUser`와 빈 `Profile`을 생성한 뒤 JWT를 발급한다.

### Request

```json
{
  "identityToken": "apple.identity.token",
  "authorizationCode": "apple_authorization_code",
  "fullName": "홍길동"
}
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| identityToken | String | ✅ | Apple에서 받은 identity token |
| authorizationCode | String |  | Apple authorization code. 현재는 저장하지 않음 |
| fullName | String |  | Apple 최초 로그인 시 받을 수 있는 이름. 현재는 저장하지 않음 |

### Response

```json
{
  "appUserId": 1,
  "profileId": 1,
  "accessToken": "jwt.access.token",
  "tokenType": "Bearer",
  "newUser": true,
  "signUpStatus": "NULL"
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
| programId | Long | ✅ | 프로그램 식별값 |
| missionId | Long |  | 공유된 미션 식별값. 스레드가 미션에서 공유된 경우만 존재 |
| title | String | ✅ | 제목 |
| description | String |  | 내용 |
| dueDateTime | String |  | 유저투표 마감 날짜. `yyyy-MM-dd` |
| viewCount | long | ✅ | 조회수 |
| likeCount | long | ✅ | 좋아요 수 |
| commentCount | long | ✅ | 댓글 수 |
| voteCount | long | ✅ | 투표 수 |
| isVoted | Boolean |  | 조회자가 유저투표에 참여했는지 여부. `profileId`를 넘긴 경우 확인 가능 |
| selectedUserVoteOptionId | Long |  | 조회자가 선택한 유저투표 선택지 식별값 |
| userVoteOptions | List<UserVoteOptionSummaryResponse> | ✅ | 유저투표 목록 카드에 노출할 선택지 요약. 스레드는 빈 배열 |
| sharedMission | SharedMissionSummaryResponse |  | 미션에서 공유된 스레드인 경우 노출할 미션 카드 요약 |
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
| score | long | ✅ | 랭킹 점수 |
| pointTier | PointTierResponse |  | 포인트 티어. 커뮤니티 랭킹에서는 null |
| threadCount | Long |  | 커뮤니티 랭킹에서 작성한 글 수 |
| commentCount | Long |  | 커뮤니티 랭킹에서 작성한 댓글 수 |

---

# Profile (프로필)

▶ 마이페이지 조회 : `GET /api/profiles/{profileId}/my-page` -완-  
▶ 닉네임 중복 확인 : `GET /api/profiles/nickname/check?nickname=나의닉네임` -완-  
▶ 닉네임 변경 : `PATCH /api/profiles/{profileId}/nickname` -완-  
▶ 내 픽 기록 조회 : `GET /api/profiles/{profileId}/mission-history?resultFilter=ALL` -완-  
▶ 내 커뮤니티 활동 조회 : `GET /api/profiles/{profileId}/community-activities?activityType=POST` -완-  
▶ 스페셜 뱃지 조회 : `GET /api/profiles/{profileId}/badges` -완-  
▶ 스페셜 뱃지 임시 생성 : `POST /api/profiles/{profileId}/badges` -완-  
▶ 관심 프로그램 조회 : `GET /api/profiles/{profileId}/program-interests` -완-  
▶ 관심 프로그램 키워드 검색 : `GET /api/profiles/{profileId}/program-interests/search?keyword=런닝` -완-  
▶ 관심 프로그램 수정 : `PUT /api/profiles/{profileId}/program-interests` -완-

## 마이페이지 조회

`GET /api/profiles/{profileId}/my-page`

### Response

```json
{
  "profileId": 1,
  "nickname": "나의닉네임",
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

`PATCH /api/profiles/{profileId}/nickname`

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

## 내 픽 기록 조회

`GET /api/profiles/{profileId}/mission-history`  
`GET /api/profiles/{profileId}/mission-history?resultFilter=CORRECT`

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

`GET /api/profiles/{profileId}/community-activities`  
`GET /api/profiles/{profileId}/community-activities?activityType=COMMENT`

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

`GET /api/profiles/{profileId}/badges`

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

`POST /api/profiles/{profileId}/badges`

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

`GET /api/profiles/{profileId}/program-interests`

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

`GET /api/profiles/{profileId}/program-interests/search?keyword=런닝`

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

`PUT /api/profiles/{profileId}/program-interests`

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
    "episodes": ["5화", "4화", "3화", "2화", "1화"]
  }
]
```

| 필드 이름 | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| programId | Long | ✅ | 프로그램 식별값 |
| programName | String | ✅ | 프로그램명 |
| genre | Genre | ✅ | 장르 |
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
    "broadcastStartDate": "2025-02-15",
    "broadcastEndDate": null,
    "thumbnailImageUrl": "https://cdn.kpick.com/programs/transit-love-4.png",
    "isOnAir": true,
    "isExposed": true,
    "description": "X와 재회할지 환승할지",
    "missionCount": 5
  }
]
```

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

▶ 미션 관리 목록 조회 : `GET /api/admin/missions?keyword=커플&programId=1&missionState=ONGOING` -완-  
▶ 미션 생성 : `POST /api/admin/missions` -완-  
▶ 미션 결과 확정 : `PATCH /api/admin/missions/{missionId}/confirm-result` -완-

## 미션 관리 목록 조회

`GET /api/admin/missions`  
`GET /api/admin/missions?keyword=커플&programId=1&missionState=ONGOING`

관리자 미션 목록과 결과 입력 패널에 필요한 선택지별 참여 비율을 함께 조회한다.

| Query | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| keyword | String |  | 프로그램명 또는 미션명 검색어 |
| programId | Long |  | 프로그램 필터 |
| missionState | MissionState |  | 상태 필터 |

### Response

```json
[
  {
    "missionId": 10,
    "programId": 1,
    "programName": "나는솔로 10기",
    "episode": "최종회",
    "missionName": "최종 커플은 누구?",
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
  "profileId": 1,
  "missionName": "5화 마지막 커플은 누구?",
  "episode": "5화",
  "dueDateTime": "2026-06-20",
  "coinFee": 50,
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
| profileId | Long | ✅ | 생성자 프로필 식별값 |
| missionName | String | ✅ | 미션명 |
| episode | String | ✅ | 회차 |
| dueDateTime | String | ✅ | 마감 날짜 |
| coinFee | int | ✅ | 참여 비용 |
| attenderCount | int | ✅ | 참여자 수 |
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
▶ 홈 미션 추천리스트 조회 : `GET /api/missions/recommendations?profileId=1` -완-  
▶ 홈 미션 추천리스트 프로그램 필터 조회 : `GET /api/missions/recommendations?programId=1` -완-  
▶ 관련 미션/스레드 추천 조회 : `GET /api/missions/{missionId}/related` -완-  
▶ 미션 상세 조회 : `GET /api/missions/{missionId}` -완-  
▶ 미션 선택 및 참여 : `POST /api/missions/{missionId}/options/select` -완-  
▶ 참여 확정 모달 조회 : `GET /api/missions/{missionId}/confirm-modal?profileId=1` -완-  

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

`GET /api/missions/recommendations?profileId=1`  
`GET /api/missions/recommendations?programId=1`

`programId`가 없으면 `profileId`의 관심 프로그램에 포함된 진행중 미션을 최신 생성 기준(`missionId DESC`)으로 조회한다. `programId`가 있으면 해당 프로그램의 진행중 미션만 조회한다.

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

`GET /api/missions/{missionId}?profileId=1`

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
  "profileId": 1,
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

## 참여 확정 모달 조회

`GET /api/missions/{missionId}/confirm-modal?profileId=1`

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
  "profileId": 1,
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
▶ 커뮤니티 게시글 상세 조회 : `GET /api/community/posts/{postId}?postType=THREAD&profileId=1` -완-  
▶ 커뮤니티 게시글 좋아요 : `POST /api/community/{postType}/{postId}/likes` -완-  
▶ 커뮤니티 댓글 작성 : `POST /api/community/{postType}/{postId}/comments` -완-  
▶ 커뮤니티 댓글 좋아요 : `POST /api/community/{postType}/comments/{communityCommentId}/likes` -완-  
▶ 커뮤니티 게시글 수정 : `PATCH /api/community/{postType}/{postId}` -완-  
▶ 커뮤니티 게시글 삭제 : `DELETE /api/community/{postType}/{postId}` -완-  
▶ 유저투표 투표 : `POST /api/user-votes/{userVoteId}/vote` -완-  
▶ 유저투표 결과 조회 : `GET /api/user-votes/{userVoteId}/result?profileId=1` -완-

## 커뮤니티 게시글 목록 조회

`GET /api/community`

타입, 장르, 프로그램 필터는 모두 선택값이다.

`GET /api/community?postType=THREAD`  
`GET /api/community?genre=DATINGSHOW`  
`GET /api/community?programId=1`  
`GET /api/community?postType=USER_VOTE&genre=DATINGSHOW&programId=1&profileId=1`

| Query | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| postType | CommunityPostType |  | 게시글 타입 |
| genre | Genre |  | 프로그램 장르 |
| programId | Long |  | 프로그램 식별값 |
| profileId | Long |  | 조회자 프로필 식별값. 유저투표 목록에서 내 투표 여부를 확인할 때 사용 |

### Response

```json
[
  {
    "postType": "USER_VOTE",
    "postId": 1,
    "profileId": 1,
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
  "profileId": 1,
  "programId": 1,
  "missionId": null,
  "title": "최종 커플 예측!",
  "description": null,
  "isNicknamePublic": null,
  "dueDateTime": null,
  "options": [
    { "content": "수지 & 현준" },
    { "content": "민아 & 재준" }
  ]
}
```

미션 결과 화면에서 스레드로 공유할 때도 같은 생성 API를 사용한다. `postType=THREAD`, `missionId`, `title`, `description`, `profileId`를 넘기면 `programId`는 공유된 미션의 프로그램으로 자동 설정된다.

### Mission Share Thread Request

```json
{
  "postType": "THREAD",
  "profileId": 1,
  "missionId": 10,
  "title": "수지현준 진짜 될 것 같아요!",
  "description": "5화 분위기 보면 확실할 것 같은데 여러분은 어떻게 생각하세요?",
  "isNicknamePublic": true
}
```

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

`GET /api/community/posts/{postId}?postType=THREAD&profileId=1`  
`GET /api/community/posts/{postId}?postType=USER_VOTE&profileId=1`

`postType`에 따라 스레드 상세 화면 또는 유저투표 상세/결과 화면에 맞는 응답을 반환한다.

### THREAD Response

```json
{
  "postType": "THREAD",
  "threadId": 1,
  "programId": 1,
  "programName": "환승연애4",
  "profileId": 1,
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
{
  "profileId": 1
}
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
  "profileId": 1,
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
{
  "profileId": 1
}
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
  "profileId": 1,
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

`GET /api/user-votes/{userVoteId}/result?profileId=1`

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

---

# Ranking (랭킹)

▶ 랭킹 조회 : `GET /api/rankings?rankingType=SEASON` -완-  
▶ 랭킹 프로필 모달 조회 : `GET /api/rankings/profiles/{profileId}` -완-  
▶ 성장 기록 조회 : `GET /api/rankings/profiles/{profileId}/growth` -완-  
▶ 현재 시즌 종료 : `PATCH /api/admin/rankings/current-season/close` -완-

## 랭킹 조회

`GET /api/rankings?rankingType=SEASON&profileId=1`

| Query | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| rankingType | String |  | SEASON, COMMUNITY, TOTAL. 기본값 SEASON |
| profileId | Long |  | 내 랭킹 카드가 필요할 때 전달 |

`rankingType=SEASON`, `rankingType=TOTAL`은 `RankingResponse`를 반환하고, `rankingType=COMMUNITY`는 커뮤니티 활동 점수 전용 필드가 있는 `CommunityRankingResponse`를 반환한다.

### SEASON Response

`GET /api/rankings?rankingType=SEASON&profileId=1`

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

`GET /api/rankings?rankingType=TOTAL&profileId=1`

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

`GET /api/rankings?rankingType=COMMUNITY&profileId=1`

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

`GET /api/rankings/profiles/{profileId}/growth?pointScope=TOTAL`

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

### Response

```json
{
  "message": "Current season closed. Points decreased by 30%.",
  "affectedProfileCount": 4821,
  "decayRate": 30
}
```

---

# Benefit (혜택)

▶ 혜택 홈 조회 : `GET /api/benefits/{profileId}` -완-  
▶ 오늘 출석하기 : `POST /api/benefits/{profileId}/attendance` -완-  
▶ 광고 Pick 지급 : `POST /api/benefits/{profileId}/ad-reward` -완-  
▶ 관리자 Pick 지급 내역 조회 : `GET /api/admin/benefits/pick-histories` -완-  
▶ 관리자 혜택 설정 조회 : `GET /api/admin/benefits/setting` -완-  
▶ 관리자 혜택 설정 저장 : `PATCH /api/admin/benefits/setting` -완-

## 혜택 홈 조회

`GET /api/benefits/{profileId}`

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

`POST /api/benefits/{profileId}/attendance`

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

`POST /api/benefits/{profileId}/ad-reward`

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

| Query | 타입 | 필수 | 설명 |
| --- | --- | --- | --- |
| pickHistoryType | PickHistoryType |  | ATTENDANCE, ATTENDANCE_BONUS, AD_REWARD, MISSION_PARTICIPATION, MISSION_REWARD, ADMIN |

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

## 문의 등록

`POST /api/inquiries`

문의하기 화면에서 문의 유형, 제목, 내용, 첨부 이미지, 답변 받을 이메일, 개인정보 수집 동의 값을 저장한다.

### Request

```json
{
  "profileId": 1,
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
| profileId | Long |  | 문의한 프로필 식별값 |
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
  "inquiryStatus": "RECEIVED",
  "createdAt": "2026-05-30T19:25:00"
}
```
