# 📝 이봉주 - 소셜 네트워크 기반 할 일 관리 플랫폼

## 프로젝트 개요

**이봉주**는 소셜 네트워크 기반의 할 일 관리 플랫폼입니다.  
단순한 개인 일정 관리가 아닌, 친구와의 상호작용을 통해 동기부여를 강화하고,  
일상 속 루틴을 함께 관리할 수 있도록 설계된 서비스입니다.

---

## 타겟 유저

- 매일 알차게 살아가고 싶은 사람들
- 다른 사람들의 할 일을 보며 자극받고 싶은 사람들
- 친구와 함께 갓생을 살고 싶은 사람들

## 🚀 기술 스택

### Backend
- <img src="https://img.shields.io/badge/kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white">
- <img src="https://img.shields.io/badge/Java-007396?style=flat-square&logo=Java&logoColor=white">
- <img src="https://img.shields.io/badge/spring-6DB33F?style=for-the-badge&logo=spring&logoColor=white">
- <img src="https://img.shields.io/badge/springboot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white">
- <img src="https://img.shields.io/badge/springsecurity-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white">
- <img src="https://img.shields.io/badge/gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">
- <img src="https://img.shields.io/badge/mysql-4479A1?style=for-the-badge&logo=mysql&logoColor=white">
- <img src="https://img.shields.io/badge/h2database-09476B?style=for-the-badge&logo=h2database&logoColor=white"> 
- <img src="https://img.shields.io/badge/redis-FF4438?style=for-the-badge&logo=redis&logoColor=white"> 


### Frontend
- <img src="https://img.shields.io/badge/thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white">
- <img src="https://img.shields.io/badge/html5-E34F26?style=for-the-badge&logo=html5&logoColor=white">
- <img src="https://img.shields.io/badge/css-1572B6?style=for-the-badge&logo=css3&logoColor=white">
- <img src="https://img.shields.io/badge/javascript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black">


### DevOps & Infra
- <img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=black">
- <img src="https://img.shields.io/badge/github-181717?style=for-the-badge&logo=github&logoColor=black">
- <img src="https://img.shields.io/badge/githubactions-2088FF?style=for-the-badge&logo=githubactions&logoColor=black">
- <img src="https://img.shields.io/badge/docker-2496ED?style=for-the-badge&logo=docker&logoColor=black">
- <img src="https://img.shields.io/badge/prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=black">

## 🌟 주요 기능

### 1️⃣ 할 일(Task) 관리
- 할 일(Task) 등록 / 수정 / 삭제
- 할 일 완료 상태 관리
- 할 일에 이미지 첨부
- 할 일 완료 기록 조회 (This Month’s Memories)

### 2️⃣ 소셜 기능
- 사용자 프로필 조회 / 수정 (닉네임, 소개글, 프로필 이미지)
- 친구(팔로우) 기능: 친구 팔로우 / 언팔로우
- 친구 목록 조회
- 친구 할 일 피드 조회 (친구들의 완료한 Task 보기)

### 3️⃣ 인증 / 보안
- 소셜 로그인 (OAuth2)
- JWT 기반 인증 / 인가
- 사용자 탈퇴 기능
- Refresh Token 관리 및 재발급

### 4️⃣ 알림 기능
- 알림 내역 조회 (예: 친구가 Task 완료 시 알림)

### 5️⃣ 통계 / 마이페이지
- 내 Task 기록 확인

---

## 🗂️ 프로젝트 구조

```bash
backend/
 ├── src/
 ├── build.gradle
 └── ...
frontend/
 ├── src/
 ├── package.json
 └── ...
```

🔗 기타

ERD
![스크린샷 2025-06-18 003213](https://github.com/user-attachments/assets/0a5ed366-1f2a-4dec-8c93-fd7c9c06e5d8)


API 명세서: 

배포 주소: 
