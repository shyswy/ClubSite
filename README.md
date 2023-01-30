# README

# ClubMember

간단한 클럽 멤버 로그인 서비스를 통해 Spring Security를 통한 로그인 시스템 구성,  API 서버 구성과 JWT를 통한 API 인증에 대해 분석해보았다.

# 목차

MileStone1: Spring Security를 통한 사용자 인증, 로그인 처리

MileStone 2: Spring Security와 Google OAuth2.0을 이용한 소셜 로그인 처리

MileStone 3: API 서버 구성과 Spring Security를 통한 보안, JWT를 통한 인증 처리.

# MileStone 1

## Spring Security의 구조

스프링 시큐리티를 통한 작업은 크게 2가지로 나눌 수 있다.

인증(Authentication): 해당 사용자가 본인이 맞는지를 확인

인가(Authorization): 인증된 사용자가 요청한 자원에 접근 가능한지 확인

유저가 어떠한 것을 요청하면, 해당 유저가 본인이 맞는가? 에 대한 인증을 거친 뒤, 인증(Authentication)을 통해 확인한 유저가 요청한 것을 받을 자격이 있는지 확인하는 인가(Authorization) 를 거친 뒤, 요청한 결과값을 돌려준다.

보통의 필터는 스프링의 빈을 사용할 수 없기 때문에 별도의 클래스를 상속 받아야 하지만,

스프링 시큐리티는 빈과 연동할  수 있는 구조로 설계되어있다.

**[Authentication: 인증]**

스프링 시큐리티 내에서는 Filter Chain이라는 구조로 Request를 처리한다.

![https://blog.kakaocdn.net/dn/TNdmx/btrWR73IT3n/mc2SpkSLykxPvFsP06dnQK/img.png](https://blog.kakaocdn.net/dn/TNdmx/btrWR73IT3n/mc2SpkSLykxPvFsP06dnQK/img.png)

https://blog.kakaocdn.net/dn/TNdmx/btrWR73IT3n/mc2SpkSLykxPvFsP06dnQK/img.png

실제 사용되는 스프링 시큐리티의 주요 필터

유저가 아이디, 비밀번호와 같은 인증에 관련한 정보를 입력하면, UsernamePasswordAuthenticationToken과 같은 토큰이라는 객체로 만들어서 AuthenticationManager(인증 매니저) 에게 전달한다.

아래는 기본으로 제공되는 필터 중, UsernamePasswordAuthenticationFilter 클래스의 일부이다.

![https://blog.kakaocdn.net/dn/V5TAy/btrWU7IHegi/pE35cksFTj3P7CvaRZPLl1/img.png](https://blog.kakaocdn.net/dn/V5TAy/btrWU7IHegi/pE35cksFTj3P7CvaRZPLl1/img.png)

https://blog.kakaocdn.net/dn/V5TAy/btrWU7IHegi/pE35cksFTj3P7CvaRZPLl1/img.png

아이디, 비밀번호를 받은 뒤, Token객체에 넣어서 인증 매니저에게 전달한다.

**Authentication Manager: 인증 매니저**

![https://blog.kakaocdn.net/dn/uL5Nh/btrWRzsRJFV/X0swK5FH4lOm4WkArg68o1/img.png](https://blog.kakaocdn.net/dn/uL5Nh/btrWRzsRJFV/X0swK5FH4lOm4WkArg68o1/img.png)

https://blog.kakaocdn.net/dn/uL5Nh/btrWRzsRJFV/X0swK5FH4lOm4WkArg68o1/img.png

여러 객체가 서로 데이터를 주고 받으며 이루어진다.

인증 매니저는 데이터베이스를 통한 인증, 메모리상 데이터를 통한 인증 등 다양한 처리 방법을 이용한다.

이때, 처리에 사용하는 것이 AuthenticationProvider 이다.

**AuthenticationProvider**

![https://blog.kakaocdn.net/dn/qJ3vq/btrWWtrfzBA/zBH1UUG9C9RWiKNB3Ci750/img.png](https://blog.kakaocdn.net/dn/qJ3vq/btrWWtrfzBA/zBH1UUG9C9RWiKNB3Ci750/img.png)

https://blog.kakaocdn.net/dn/qJ3vq/btrWWtrfzBA/zBH1UUG9C9RWiKNB3Ci750/img.png

Authentication Provider이 처리가능한 하위 구현 클래스들

Authentication Provider는 인증 매니저가 전달한 토큰 타입이 처리할 수 있는 타입인지 확인한 뒤, 이를 통해 authenticate() 메서드를 수행한다.

Authentication Provider는 내부적으로  UserDetailsService를 사용하고,

UserDetailsService는 실제로 인증을 위한 데이터를 가져오는 역할을 수행한다.

**[Authorization: 인가]**

인증을 끝낸 뒤에는, 사용자가 요청한 것에대한 권한을 가지고 있는지 확인하는 작업이 필요하다.

인증 매니저 내부의 authenticate() 라는 메소드는 Authentication이라는 “인증 정보”를 리턴한다.

이 정보 내에는 Roles라는 “권한”에 대한 정보값이 있고, 이 정보를 바탕으로 사용자가 요청한 정보를 허가하거나 거부하는 Access-Control을 수행하게 된다.

간단한 예로, 유저가 로그인 없이  /sample/member 라는 url에 접근할 경우, SpringSecurity를 통한 인증, 인가의 절차를 구현하며 스프링 시큐리티의 흐름에 대해 알아보자.

## ClubMember 기능 구현

**Security Configuration**

**PasswordEncoder**

비밀번호를 어떤 방식의 암호화를 통해 저장할 지 정의한다.

앞서 보여준 SecurityConfig를 확인해보자

![https://blog.kakaocdn.net/dn/bkPoMR/btrWS3HlkDV/9ud8UvIlQCICKxneMeb981/img.png](https://blog.kakaocdn.net/dn/bkPoMR/btrWS3HlkDV/9ud8UvIlQCICKxneMeb981/img.png)

나는 BycryptPasswordEncoder 방식을 사용하였는데, 이 방식은  해시 함수를 통해 패스워드를 암호화하는 클래스로, 암호화한 패스워드는 원래대로 복호화가 불가능하고, 매번 암호화된 값도 다르다.

원본 비밀번호를 볼 수 없기 때문에, 보안성이 좋아 최근에 자주 사용한다.

**FilterChain**

![https://blog.kakaocdn.net/dn/UdvmL/btrWREt8Vaw/G8KzKHdfhgVMa9LkOYIDT0/img.png](https://blog.kakaocdn.net/dn/UdvmL/btrWREt8Vaw/G8KzKHdfhgVMa9LkOYIDT0/img.png)

로그인 없이 /sample/member의 url을 입력하면 스프링 시큐리티의 필터에서 인증, 인가가 필요한지 여부를 판단한다.

위와 같은 filter를 생성한 경우, “/sample/member”  에 접근 시, “USER”라는 Role을 가진 사람만 허용되기에, 인증-인가 절차를 수행해야한다. http.formLogin() 메소드를 통해, 로그인 되지 않은 유저는 아래와 같이 로그인 화면으로 리다이렉트 된다.

![https://blog.kakaocdn.net/dn/EEINL/btrWZVHxs0X/VGHkmnyRCEOuUIUn384ug0/img.png](https://blog.kakaocdn.net/dn/EEINL/btrWZVHxs0X/VGHkmnyRCEOuUIUn384ug0/img.png)

이후의 흐름은 다음과 같다.

1. 유저가 아이디, 비밀번호를 입력하면, Token에 담아 인증 매니저에 전달한다.
2. 인증 매니저는 적절한 AuthenticationProvider를 찾아서 인증을 시도한다.
3. AuthenticationProvider가 UserDetailsService를 통해 올바른 사용자라고 인증하면, 사용자 정보를 Authentication 타입으로 전달한다.
4. 전달된 객체로 사용자에게 적절한 권한이 있는지 확인하는 인가(Authorization) 과정을 거친다.
5. 만약 유저에게 “USER” 권한이 존재한다면, 요청한 url “/sample/member”에 대한 작업을 수행한다.

1->2 의 과정은 거의 자동으로 이루어지기에,

UserDetailsService를 통해 올바른 유저인지 확인하는 인증 과정, 그리고 권한에 대한 인가 과정에 대해 실제 구현을 통해 디테일하게 알아보자.

실제 구현에선 UserDetailService 인터페이스의 loadUserByName(username)라는  username 정보를 파라미터로 넘기면 *UserDetail(인증, 인가에 사용되는 정보를 담은 객체) 를 반환하는 메소드를 사용한다.

- UserDetail

회원 정보를 저장하는 타입. 회원 정보를 가지고 올 수 있는 몇가지 메소드를 지원한다.

getAuthorities() : 사용자 권한 정보를 제공

getPassword() : username에 매칭되는 비밀번호 정보를 제공

getUsername() : 인증에 필요한 아이디와 같은 username 정보

위와 같은 정보를 가지고 처리하는 방법에는, 2가지가 있다.

1. DTO 클래스에 UserDetail 인터페이스를 구현하는 방식
2. 별도의 클래스를 구성하고 DTO 처럼 사용하는 방식.

개인적으로 DTO로 처리하는 방식을 선호하여 2번 방식으로 설명하겠다.

**DTO**

우선 회원 정보에 대한 DTO를 생성해준 뒤, userDetial의 하위에 있는 User를 상속하고, User의 생성자를 사용할 수 있게 만들어준다. ( alt + enter를 통해 손쉽게 기본 구조를 완성하면 편하다)

![https://blog.kakaocdn.net/dn/dZLt9u/btrWU7P3IIq/M2z0oG8oDrn0EKXx4Zkeik/img.png](https://blog.kakaocdn.net/dn/dZLt9u/btrWU7P3IIq/M2z0oG8oDrn0EKXx4Zkeik/img.png)

이후 DTO를 구성해주면 스프링 시큐리티에서 인증/인가 작업에서 사용하는 동시에 유저 정보에 대한 DTO로써 사용할 수 있다.

![https://blog.kakaocdn.net/dn/xAwgX/btrW39lsOYp/ooEMFLInLI45wlFbqnFP71/img.png](https://blog.kakaocdn.net/dn/xAwgX/btrW39lsOYp/ooEMFLInLI45wlFbqnFP71/img.png)

**UserDetailService**

앞서 말했듯, 인증 매니저는 UserDetailsService 인터페이스의 loadUserByName() 메소드를 통해 username을 가지고 회원 정보를 가져오게 되는데, JPA로 사용자 정보를 가지고 오고 싶다면, UserDetailsService 인터페이스에 대한 구현 클래스를 정의해야한다.

아래는 UserDetailsService의 구현 클래스의 일부이다.

![https://blog.kakaocdn.net/dn/bpwP7u/btrWTKHMpq6/2F7ugc3EwNplRHRiqOuoX0/img.png](https://blog.kakaocdn.net/dn/bpwP7u/btrWTKHMpq6/2F7ugc3EwNplRHRiqOuoX0/img.png)

멤버들의 정보를 저장한 리포지토리를 DI받고, email(username) 을 통해 찾고자하는 회원의 멤보 정보를 찾는 과정이다.

여기서 중요한 점은 @Service 어노테이션을 통해 ClubUserDetailsService라는 UserDetailsService 인터페이스의 구현 클래스를 빈으로 등록하는 것인데, 이렇게 해주면 자동으로 스프링 시큐리티는 UserDetailsService를 상속받은 ClubUserDetialsSerivce를 UserDetialsService로 인식하기에, username을 통해 회원 정보를 가지고 오는 절차를 오버라이딩하여 구현할 수 있다.

아래는 ClubUserDetialsService 클래스의 나머지 부분이다.

![https://blog.kakaocdn.net/dn/ez1bcH/btrWS3gedGf/tQ1bhjD1wEA43fijHmXKMK/img.png](https://blog.kakaocdn.net/dn/ez1bcH/btrWS3gedGf/tQ1bhjD1wEA43fijHmXKMK/img.png)

찾은 회원 정보(clubMember)를 통해 앞서 만들어둔 ClubAuthMemberDTO라는 인증/허가에 필요한 정보를 UserDetial 정보를 처리하기 위해 DTO를 생성한 뒤, 리턴해준다.

이제 스프링 시큐리티에서 처리한 정보를 thymeleaf를 통해 View에 출력해보자.

아래는 member.html이다.

![https://blog.kakaocdn.net/dn/bamklF/btrW772LtKo/KO2cqYO1vVs1gsrLWNaxTK/img.png](https://blog.kakaocdn.net/dn/bamklF/btrW772LtKo/KO2cqYO1vVs1gsrLWNaxTK/img.png)

접두어 sec을 통해 스프링 시큐리티를 타임리프에서 사용할 수 있다.

접속한 유저가 “USER” 라는 Role을 가지고 있기에 “Has USER ROLE” 이라는 문장만 화면에 보이게 되고,

isAuthenticated() 메소드를 통해 인증된 유저임을 확인한 뒤, text를 출력하고,

Authentication의 pricipal 이라는 변수를 사용하여 CLubAuthMemberDTO의 내용을 가져올 수 있다.

**결과 화면**

![https://blog.kakaocdn.net/dn/kJjVw/btrWYLUe2Yo/w2C8S1SvYFXHt6STbUg6zK/img.png](https://blog.kakaocdn.net/dn/kJjVw/btrWYLUe2Yo/w2C8S1SvYFXHt6STbUg6zK/img.png)

이번엔 컨트롤러에서 출력해보자.

컨트롤러에서 시큐리티에서 처리한 정보를 사용하는 것에는 2가지 방법이 있다.

1. SecurityContextHolder 객체 사용하기
2. 직접 파라미터와 어노테이션을 사용하기

이번엔 직접 @AuthenticationPrincipal 이라는 어노테이션을 통해 컨트롤러에서

시큐리티에서 처리한 사용자 정보를 사용해 보겠다.

아래는 “/sample/member” url을 처리하는 컨트롤러다.

![https://blog.kakaocdn.net/dn/ZnOTN/btrXaCAW973/aq4F6qS3ENK113bE4cSTr0/img.png](https://blog.kakaocdn.net/dn/ZnOTN/btrXaCAW973/aq4F6qS3ENK113bE4cSTr0/img.png)

https://blog.kakaocdn.net/dn/ZnOTN/btrXaCAW973/aq4F6qS3ENK113bE4cSTr0/img.png

**결과**

![https://blog.kakaocdn.net/dn/bVeW5C/btrW9lT2egU/SHGUy3AYZXV0NQFRRCnBnK/img.png](https://blog.kakaocdn.net/dn/bVeW5C/btrW9lT2egU/SHGUy3AYZXV0NQFRRCnBnK/img.png)

https://blog.kakaocdn.net/dn/bVeW5C/btrW9lT2egU/SHGUy3AYZXV0NQFRRCnBnK/img.png

이렇게 스프링 시큐리티가 동작하기 위한 기본적인 구현을 마쳤다.

하지만 이렇게만 보면 어떻게? 가 알기 쉽지 않을 것이기에, 한가지 예시 상황에서 스프링 시큐리티의 동작을 매우 자세히 설명하며 전체적인 흐름을 요약하겠다.

### 요약

1. 만약 유저가 로그인 하지 않고 “/sample/member/” url에 접근하면 어떻게 될까?

우선 .fornmLogin() 메소드를 통해, 로그인 하지 않은 유저에게 로그인 창을 노출하고, 유저가 로그인 창에 정보를 입력 시,

인증 매니저(Authentication Manager)가 Authentication Provider를 통해 처리가능한 타입인지 확인한다.

이후 UserDetiailsService를 통해 인증 절차를 시작하는데, 이때 @Service 어노테이션을 통해 빈에 등록되어

스프링 시큐리티가 UserDetailsService로 인식하는 구현 클래스 ClubUserDetialsService의 loadUserByName() 메소드를 통해 유저가 입력한 username에 대한 회원 정보가 존재하는지 확인하고, 인증/인가 정보가 담긴 UserDetail의 처리를 위한 DTO 클래스로 생성하여 반환한다.

UserDetail 타입으로 찾은 회원 정보를 유저가 입력한 회원 정보와 비교하고, 만약 password가 다르다면 Bad Cridential( 잘못된 자격 증명) 결과로 반환하고, 올바르다면 해당 유저에게 적절한 권한이 있는지 확인하는 인가(Authentication)의 과정을 수행한다.

앞서 구현한  SecurityConfig의 filter를 참고해보자.

![https://blog.kakaocdn.net/dn/dhOYxo/btrWS2PdXGi/KoIYKx2kmzJBRVpVzDNKzk/img.png](https://blog.kakaocdn.net/dn/dhOYxo/btrWS2PdXGi/KoIYKx2kmzJBRVpVzDNKzk/img.png)

이 “인가” 의 과정을 수행하는 것이 위 SecurityConfig의

auth.antMatchers(“/sample/member/”).hasRole(“USER”) 이 부분이다.

이는 “/sample/member/” 에 대한 접근 권한은 “USER” 라는 Role을 가진 사람만 가지고 있다는 의미로써, 만약 유저가 올바르게 아이디와 비밀번호를 입력하여 인증에 성공하고 로그인에 성공하더라도, 해당 권한이 없으면 진입하지 못한다는 “인가” 에 대한 내용을 처리한 것이다.

# MileStone 2

## **[OAuth]**

서비스 제공 업체들이 각자 다른 방식으로 로그인하지 않도록 제공하는 공통의 인증 방식.

기존에 사용자와 관리자, 2가지의 ROLE로 유저가 구분되었지만, OAuth를 사용하면, 구글, 네이버 등의 소셜 로그인 서비스를 제공하는 제 3의 인물을 포함해야한다. 나는 Google의 소셜 로그인 서비스를 사용할 것이기에, 구글로 이 제 3자를 칭하겠다.

![https://blog.kakaocdn.net/dn/sFb2U/btrW9k8KwLq/2FAMVWUj4dSbxiN6AdFImk/img.png](https://blog.kakaocdn.net/dn/sFb2U/btrW9k8KwLq/2FAMVWUj4dSbxiN6AdFImk/img.png)

![https://blog.kakaocdn.net/dn/btNzyN/btrXanjQ3dY/YhTkOk2fpeOKyJFvfMqZC1/img.png](https://blog.kakaocdn.net/dn/btNzyN/btrXanjQ3dY/YhTkOk2fpeOKyJFvfMqZC1/img.png)

**SecurityConfig**

![https://blog.kakaocdn.net/dn/bWNSqp/btrW4FlX4ZM/XXmHqQQK2H9eSMWqIhCKn1/img.png](https://blog.kakaocdn.net/dn/bWNSqp/btrW4FlX4ZM/XXmHqQQK2H9eSMWqIhCKn1/img.png)

문제점: 소셜 로그인 처리를 통해 로그인시, ClubAuthMember 객체를 이용하지 않기에, 소셜 로그인한 유저의 정보를 알 수 없다.

따라서 소셜 로그인시, 다음과 같은 요구사항을 만족하게 설계해보자.

1) 소셜 로그인 시, 사용자의 이메일 정보 추출 ( 비밀번호 등은 따로 빼서 저장하는 것에 고민의 여지가 있다)

2) 데이터베이스와 소셜 로그인 정보를 연동하여 사용자 정보를 관리하게한다.

3) 기존 방식, 소셜 로그인 두가지 방식이 올바르게 동작되어야한다.

OAuth에서는, 기존 스프링 시큐리티에서의 UserDetailsService 인터페이스와 유사한 기능을 가진

OAuth2UserService 인터페이스가 있다.

이 인터페이스에는 여러가지 구현 클래스가 이미 구현되어 있기에,

이 중 하나를 상속받아서 구현하면 더욱 편리하게 구현이 가능하다.

아래는 구현 클래스 중 하나인, DefaultOAuth2UserService 클래스를 상속받아 작성한 클래스이다.

![https://blog.kakaocdn.net/dn/0yY2X/btrXbtqxUdR/gtuOsoGRkUSPTrYEqTuKgK/img.png](https://blog.kakaocdn.net/dn/0yY2X/btrXbtqxUdR/gtuOsoGRkUSPTrYEqTuKgK/img.png)

우선, 앞서 UserDetailsService를 상속받아 구현한 클래스와 마찬가지로, @Service 어노테이션을 붙여 스프링 빈에 자동 등록되게하면, 해당 클래스를 OAuth2UserService로 인식하고 사용하게 된다.

**loadUser**

![https://blog.kakaocdn.net/dn/bKwzZX/btrXbDUbjOD/RmIFwvmBt0VXFKuA3odpo0/img.png](https://blog.kakaocdn.net/dn/bKwzZX/btrXbDUbjOD/RmIFwvmBt0VXFKuA3odpo0/img.png)

DefaultOAuth2UserService 클래스에는 UserDetailsService의 LoadUserByName() 메소드와 유사하게

유저의 리퀘스트를 전달 받으면 OAuth2User타입으로 리턴하는 리턴하는 loadUser() 메소드가 있기에, Override하여 사용하는 것으로 인증 작업을 진행할 수 있다.

이 OAuth2User 타입의 경우, 위와 같이 .getClientRegistration() 과 같은메소드들을 통해 필요한 정보를 추출해서 사용

가능하다.

**결과 로그창**

![https://blog.kakaocdn.net/dn/b9dX9z/btrXanYWl2P/xm10FsaL33CjNi9HeVLzD0/img.png](https://blog.kakaocdn.net/dn/b9dX9z/btrXanYWl2P/xm10FsaL33CjNi9HeVLzD0/img.png)

이제 OAuth2User 타입을 통해 회원정보를 추출할 수 있게 되었으니, 데이터베이스에 이를 저장하는 영역을 구현해보자.

아래는 loadUser() 메소드에 추가된 코드이다.

![https://blog.kakaocdn.net/dn/ZLupD/btrXa3r4qjI/w4VWNhu0RXLpgSZVPY5QPK/img.png](https://blog.kakaocdn.net/dn/ZLupD/btrXa3r4qjI/w4VWNhu0RXLpgSZVPY5QPK/img.png)

email 정보를 추출하고, saveSocialMember() 메소드에 넘겨준다.

아래는 받은 email 정보를 기반으로, 데이터베이스에 해당 유저가 존재하는지 찾고, 없다면 데이터베이스에 추가한 뒤 리턴 해주는 saveSocialMember() 메소드의 구현이다.

![https://blog.kakaocdn.net/dn/cLXvxM/btrW7TqrhlJ/kvt0M03tu1KotEFbEah9r1/img.png](https://blog.kakaocdn.net/dn/cLXvxM/btrW7TqrhlJ/kvt0M03tu1KotEFbEah9r1/img.png)

이제 다시 Google을 통해 로그인을 하면, 아래와 같이 데이터베이스 내에 소셜 로그인 사용자의 정보가 들어가는 것을 확인할 수 있다.

![https://blog.kakaocdn.net/dn/bh5jBk/btrXamsbitM/HuK7ZVNYUjUxkzgHeuFp9K/img.png](https://blog.kakaocdn.net/dn/bh5jBk/btrXamsbitM/HuK7ZVNYUjUxkzgHeuFp9K/img.png)

이제 저장한 로그인 정보를 전에 했던 것처럼 View, Controller에 처리해보자.

우선 View, 그리고 Controller는 기존의 일반 로그인( 소셜 로그인이 아닌, 사이트 회원) 사용자들의 정보를

ClubAuthMemberDTO라는 인가, 인증에 더불어 DTO의 역할까지 수행하는 DTO 객체 타입을 전달받아 사용하기에

loadUser() 메소드가 반환하는 OAuth2User 타입을 ClubAuthMemberDTO 타입으로 변환해줄 필요가 있다.

여기서 재밌는 점이 하나 나온다.

ClubAuthMemberDTO는 현재 아래와 같이 "User" 클래스를 상속받고 있다.

![https://blog.kakaocdn.net/dn/0OTV7/btrXbrGhfXJ/wkrrAxDxVd7YrKuDAHoLY0/img.png](https://blog.kakaocdn.net/dn/0OTV7/btrXbrGhfXJ/wkrrAxDxVd7YrKuDAHoLY0/img.png)

그렇기에, 보통의 DTO가 Entity로 변환될 때, dtoToEntity() 같은 변환 메소드를 사용하는 것과 다르게, 변환해줄 필요가 없다.

아래 ClubMemberDetailService의 loadUserByUsername 메소드를 다시 한번 보자.

![https://blog.kakaocdn.net/dn/bSHbSk/btrXaqH5i1I/qNfFNGHviUQR2t2OUfwhKk/img.png](https://blog.kakaocdn.net/dn/bSHbSk/btrXaqH5i1I/qNfFNGHviUQR2t2OUfwhKk/img.png)

리턴 타입은 분명 UserDetails 라는 회원 정보를 담은 객체이다.

하지만, 우리는 구현할 때, 별도의 변환 과정없이, 아래와 같이 ClubAuthMemberDTO 객체를 리턴했다.

![https://blog.kakaocdn.net/dn/EnB6h/btrW9lAiWFh/WpoyWr4ki6oZKGl19c10Ak/img.png](https://blog.kakaocdn.net/dn/EnB6h/btrW9lAiWFh/WpoyWr4ki6oZKGl19c10Ak/img.png)

왜 이렇게 처리가 가능한 것 일까? 그 이유는 ClubAuthMemberDTO 클래스가 상속하는 User 클래스를 보면 알 수 있다.

![https://blog.kakaocdn.net/dn/cFUohT/btrW76wCMb1/gDhy8xpxrSVwHW46jyy2z1/img.png](https://blog.kakaocdn.net/dn/cFUohT/btrW76wCMb1/gDhy8xpxrSVwHW46jyy2z1/img.png)

User를 보면, UserDetails 인터페이스를 상속하는 것을 알 수 있다.

그렇기에, ClubAuthMemberDTO -> User -> UserDetails 이렇게 타입 캐스트가 되서 올바르게 정보가 리턴 될 수 있는 것이다.

하지만 우리는 이제 OAuth2User 객체를 ClubAuthMemberDTO로 치환해야 되고, 위에서 나온 과정처럼 마법같은 타입캐스트가 발생하기 위해선, ClubAuthMemberDTO가 OAuth2User 객체를 상속받아야만 한다.

만약 OAuth2User가 클래스라면, 2가지 클래스를 상속받지 못하는 자바 구조상 번거로운 작업이 수행되어야겠지만,

다행히도 인터페이스기에, 아래와 같이 OAuth2User를 상속하는 것으로 쉽게 타입캐스트가 가능해진다.

![https://blog.kakaocdn.net/dn/wE7oJ/btrXaBWU9d3/g3ZrMHqbRMO9OwOkRnXSF1/img.png](https://blog.kakaocdn.net/dn/wE7oJ/btrXaBWU9d3/g3ZrMHqbRMO9OwOkRnXSF1/img.png)

그리고 OAuth2User의 정보들 의 데이터를 저장한 attr 라는 변수를 만들고, 기존의 4개의 파라미터 + OAuth2User의 getAttribute()를 통해 얻을 수 있는 부가 정보를 저장하는 1개의 파라미터까지해서 

5개의 파라미터를 통한 생성자를 추가로 만들어주자.

![https://blog.kakaocdn.net/dn/Eybpb/btrXar77qws/klkyf8f8VbjJQhwSoOk3Z0/img.png](https://blog.kakaocdn.net/dn/Eybpb/btrXar77qws/klkyf8f8VbjJQhwSoOk3Z0/img.png)

OAuth2User.getAttributes() 의 리턴 타입은 Map<String,Object> 이기에 이에 맞는 변수를 생성했다.

이제 모든 선처리 작업을 완료했으니, loadUser() 메소드가 OAuth2User를 통해 ClubAuthMemberDTO를 만들고 반환하게 만들자.

![https://blog.kakaocdn.net/dn/cWSfAJ/btrXbsSJ922/jLgn8rZtDJQl3vKtHnVgHk/img.png](https://blog.kakaocdn.net/dn/cWSfAJ/btrXbsSJ922/jLgn8rZtDJQl3vKtHnVgHk/img.png)

앞서도 말했지만, clubAuthMemberDTO를 반환해주어도 알아서 타입 캐스트가 되기에 문제없다.

여기까지 성공했다면, 이제 소셜 로그인 사용자의 정보도 일반 로그인 사용자 처럼 ClubAuthMemberDTO를 통해 처리할 수 있기에, 두 사용자 모두 해당 DTO를 통해 동일한 기능을 수행할 수 있다.

다시 컨트롤러 쪽으로 가보자.

![https://blog.kakaocdn.net/dn/lGP2u/btrXcp9hAzg/Vv3USbIkdKzDd1KvIbKC6k/img.png](https://blog.kakaocdn.net/dn/lGP2u/btrXcp9hAzg/Vv3USbIkdKzDd1KvIbKC6k/img.png)

여기서, 기존에는 OAuth를 통한 소셜 로그인 사용자가 clubAuthMemberDTO에 호환되지 않아 null이 출력 되었지만, 이제 아래와 같이 올바른 정보를 출력한다.

![https://blog.kakaocdn.net/dn/xsErc/btrXdNaY8zs/K9TDoHte4mHUxYCAA4AVB0/img.png](https://blog.kakaocdn.net/dn/xsErc/btrXdNaY8zs/K9TDoHte4mHUxYCAA4AVB0/img.png)

View 영역도 마찬가지로, OAuth를 통한 로그인 정보가 올바르게 DTO에 호환되며 제대로 정보가 나타난다.

![https://blog.kakaocdn.net/dn/ZWVxf/btrXa0vsHxk/NiCJkbwUqjssScOmy3xrC1/img.png](https://blog.kakaocdn.net/dn/ZWVxf/btrXa0vsHxk/NiCJkbwUqjssScOmy3xrC1/img.png)

여기까지 왔다면 일반 로그인, 소셜 로그인한 사용자 모두 View와 Controller 영역에서 동일하게 처리할 수 있게된다.

하지만, 소셜 로그인을 통해 로그인 시, 비밀번호가 1111로 고정되어 데이터베이스에 들어가는 문제점, 사용자의 이름이 이메일로 고정되는 점 등 문제가 있다.

clubMember에는 FromSocial이라는 소셜 사용자를 식별하는 boolean이 있기에, 이러한 정보를 가지고 제 3자가 로그인하는 것은 불가능하기에 보안적으로는 상관없지만, 불편한 사항이기에 최초에 소셜 로그인 시, 비밀번호를 수정할 수 있도록 기능을 추가해보자.

그 전에, 스프링 시큐리티의 로그인 관련 처리를 돕는 2가지의 인터페이스에 대해 알아보자.

1) AuthenticationSuccessHandler

이름 그대로, 인증 성공시의 상황에 대한 핸들러이다.

2) AuthenticationFailHandler

역시 이름 그대로, 인증 실패시의 상황에 대한 핸들러이다.

이를 사용하여 아래처럼 로그만 출력하는 간단한 구현 클래스를 만들 수 있다.

![https://blog.kakaocdn.net/dn/mlyst/btrXg3ZyF1v/a7D1Cuu44rKm0mRtCnEceK/img.png](https://blog.kakaocdn.net/dn/mlyst/btrXg3ZyF1v/a7D1Cuu44rKm0mRtCnEceK/img.png)

그리고 생성한 핸들러를 사용하도록 SecurityConfig에 아래와 같이 작성해주자

**successHandler 메소드**

![https://blog.kakaocdn.net/dn/k2o5Q/btrXbDBlAqO/aZV9WBW1cKeB4KCdFwyb5k/img.png](https://blog.kakaocdn.net/dn/k2o5Q/btrXbDBlAqO/aZV9WBW1cKeB4KCdFwyb5k/img.png)

그리고 기존 OAuth 로그인 설정에서 successHandler를 추가한다.

![https://blog.kakaocdn.net/dn/cm5j4R/btrXgSxeDc7/Pl3CeEJJa13zjDEAwKtfZK/img.png](https://blog.kakaocdn.net/dn/cm5j4R/btrXgSxeDc7/Pl3CeEJJa13zjDEAwKtfZK/img.png)

그리고 실행 해보면  아직 화면처리를 SuccessHandler에서 해주지 않았기에 빈 화면이 나오지만

log에 적은 문장이 출력되어 로그인 성공시에 Handler가 올바르게 동작함을 확인할 수 있다.

![https://blog.kakaocdn.net/dn/rvi5z/btrXgwOKgpT/CU61R1c0FjGoHnlvxaBrr1/img.png](https://blog.kakaocdn.net/dn/rvi5z/btrXgwOKgpT/CU61R1c0FjGoHnlvxaBrr1/img.png)

이제 아래와 같이, 소셜 로그인시 정보 수정을 제공하는 로직을 핸들러에서 구현하면된다.

![https://blog.kakaocdn.net/dn/dYnc1t/btrXbDhhdo5/WNeE7zBS58rZPUy7HvbAT0/img.png](https://blog.kakaocdn.net/dn/dYnc1t/btrXbDhhdo5/WNeE7zBS58rZPUy7HvbAT0/img.png)

만약 소셜 로그인 이용자이고, 비밀번호가 디폴트인 "1111" 이라면 "/sample/modify" 로 리다이렉트하게 구현하였다.

여기서 편의를 위한 문법 하나를 소개하겠다.

![https://blog.kakaocdn.net/dn/blkaLT/btrXcqhLF1j/S1H7A91VJEklnRGbFtgZbk/img.png](https://blog.kakaocdn.net/dn/blkaLT/btrXcqhLF1j/S1H7A91VJEklnRGbFtgZbk/img.png)

기존에는 이런식으로 권한처리를 진행했지만, @EnableGlobalMethodSecurity 어노테이션을 사용하면

컨트롤러 영역에서 권한 설정을 진행할 수 있다.

**SecurityConfig**

![https://blog.kakaocdn.net/dn/byhY2v/btrXbEtLb9x/3vZEuszl6d8fkxPnVp3DY1/img.png](https://blog.kakaocdn.net/dn/byhY2v/btrXbEtLb9x/3vZEuszl6d8fkxPnVp3DY1/img.png)

이렇게 Config 파일에서 설정을 해둔 뒤, 컨트롤러에서 @PreAuthorize( "hasRole(~~~~)" )  어노테이션을 붙이면,

해당 어노테이션이 붙은 건 hasRole() 메소드를 통한 권한 체크를 진행하고, 어노테이션이 없다면 모든 접근을 허락한다.

![https://blog.kakaocdn.net/dn/UJlTi/btrXgw2xYFI/nHsCzxK8KojDBxHMaQenoK/img.png](https://blog.kakaocdn.net/dn/UJlTi/btrXgw2xYFI/nHsCzxK8KojDBxHMaQenoK/img.png)

또한 "특정 이름을 가진 사람만 접근 가능" 과 같은 특수한 케이스도 처리 가능하다.

![https://blog.kakaocdn.net/dn/bU2fTL/btrXhu30NBV/deqFmXn2BJC4UkLCjE8rz0/img.png](https://blog.kakaocdn.net/dn/bU2fTL/btrXhu30NBV/deqFmXn2BJC4UkLCjE8rz0/img.png)

이메일이 내 이메일과 같아야만, Mapping이 수행되게 구현한 컨트롤러이다.

아래와 같이, 내 계정의 이름을 가진 사용자만 해당 컨트롤러를 통해 이동할 수 있게 구현할 수 있다.

![Untitled](README%20c6022d99971d44e985767a55af8c0819/Untitled.png)

# MileStone 3

**JSP, Thymeleaf**: 서버에서 모든 데이터를 만들어서 브라우저에 전송하는 SSR 방식

요즘은 이런 SSR 방식이 아닌, CSR(Client Side Rendering) 을 사용하고,

점차 하나의 단독적인 애플리케이션으로 동작하는 SPA(Single Page Application)의 형태로 변화 중이다.

따라서 최근의 서버는 클라이언트가 원하는 XML, JSON 데이터를 제공하는 API 서버 방식이 주류이다.

## **[API 서버]**

요청받은 데이터만을 제공하는 서버

데이터만을 전달함으로써, 클라이언트 영역이 어떻게 구현되는지 상관 없이 구현할 수 있고,

클라이언트에서 자유롭게 전달 받은 데이터를 사용할 수 있어 유동성이 좋다.

이번 글에선 JSON을 이용하는 API 서버의 구성 방법에 대해 알아보자.

API 서버의 보안은 Spring Security로 처리하고, 인증은 JWT(JSON Web Token)을 사용하여 구현할 것이다.

우선 ClubMember라는 클럽 내의 사람들의 정보와 ManyTO One 관계를 가진 Note( 글 객체) 라는 엔티티를 만들었다.

![https://blog.kakaocdn.net/dn/baxuhl/btrXj0wf4ez/2fsVxlHpQQEOmmX4Ei1191/img.png](https://blog.kakaocdn.net/dn/baxuhl/btrXj0wf4ez/2fsVxlHpQQEOmmX4Ei1191/img.png)

작성자 이메일을 F.K로 다 대 일 관계를 형성

그리고 해당 엔티티를 담을 DTO 객체를 생성한다.

![https://blog.kakaocdn.net/dn/bdH4ge/btrXm6uZFRh/oLLgo5Nc3yPlxNuNY1CDF0/img.png](https://blog.kakaocdn.net/dn/bdH4ge/btrXm6uZFRh/oLLgo5Nc3yPlxNuNY1CDF0/img.png)

이제 Rest방식의 컨트롤러를 작성해주자.

![https://blog.kakaocdn.net/dn/bvNziZ/btrXnkzMq4j/D5NYrCekbtdCipGoPZqIkk/img.png](https://blog.kakaocdn.net/dn/bvNziZ/btrXnkzMq4j/D5NYrCekbtdCipGoPZqIkk/img.png)

JSON 데이터를 수신하면, NoteDTO로 변환하여 작업을 처리하고, 처리완료된 note의 번호와 성공 메세지를 리턴한다.

Rest방식의 데이터 송, 수신에 대해 알아보기 위한 글이기에 Service와 Repository 구현은 스킵하겠다.

이제 잘 작동하나 확인해보자.

하지만 여기서 굳이 Thymeleaf 등의 통해 View를 완전히 구현할 필요 없이, Rest 방식의 테스트 도구를 사용해서 간단하게

요청 / 응답의 결과를 확인 할 수 있는 확장 프로그램을 사용하면 편하다.

![https://blog.kakaocdn.net/dn/brERd1/btrXm6IvWEF/Q9XKvHn3K3VBguLkKbK9Sk/img.png](https://blog.kakaocdn.net/dn/brERd1/btrXm6IvWEF/Q9XKvHn3K3VBguLkKbK9Sk/img.png)

크롬 확장 프로그램의 REST방식 테스트 도구

아래와 같이 요청을 보내면

![https://blog.kakaocdn.net/dn/Ok0ef/btrXi4ljSRs/nYVWgFhgQRQTov30Mcm2bK/img.png](https://blog.kakaocdn.net/dn/Ok0ef/btrXi4ljSRs/nYVWgFhgQRQTov30Mcm2bK/img.png)

writerEmail은&nbsp; note와 Clubmember의 F.K이므로 꼭 존재하는 이메일로 해야한다.

아래와 같이 생성되는 Notes의 num을 확인 가능하다.

![https://blog.kakaocdn.net/dn/5XW8U/btrXmbxgDSs/Pr6fFUHkKgl86TAp5vHEsk/img.png](https://blog.kakaocdn.net/dn/5XW8U/btrXmbxgDSs/Pr6fFUHkKgl86TAp5vHEsk/img.png)

이번엔 url에 담긴 num 정보를 통해 매칭되는 NoteDTO 객체를 반환하는 컨트롤러를 구현해보자.

![https://blog.kakaocdn.net/dn/coghJk/btrXmcprkWa/4RQGu2v47rNLh4z7aKeV10/img.png](https://blog.kakaocdn.net/dn/coghJk/btrXmcprkWa/4RQGu2v47rNLh4z7aKeV10/img.png)

이번에도 YARC를 사용해서 get 요청을 보냈지만, Get 방식은 Post와 다르게 그냥 브라우저에 url을 쳐도 확인 가능하기에 꼭 도구를 사용할 필요는 없다.

아까 Post방식의 요청을 통해 생성한 게시글을 조회해보자. ( num =1 로 생성되었다 )

![https://blog.kakaocdn.net/dn/euPn9s/btrXmDfKrG2/aHKbyMYTKT2hId4KiRJrh0/img.png](https://blog.kakaocdn.net/dn/euPn9s/btrXmDfKrG2/aHKbyMYTKT2hId4KiRJrh0/img.png)

그럼 아래와 같이, 해당 num을 가진 NoteDTO 객체가 Reponse된다.

![https://blog.kakaocdn.net/dn/WgOtj/btrXmSwYrqt/oeGC4t77okg97228FAerg1/img.png](https://blog.kakaocdn.net/dn/WgOtj/btrXmSwYrqt/oeGC4t77okg97228FAerg1/img.png)

이번엔 email 정보를  get 방식으로 전달받아 해당 email정보르 사용자를 식별하여 해당 사용자가 작성한 모든 글을 조회해보자. 파라미터의 String email은 @RequestParam 어노테이션이 생략되어 있기에, url에 들어온 email 정보가 파라미터 email에 들어가게 됩니다.

![https://blog.kakaocdn.net/dn/b1Av3i/btrXnjOs9N4/rQqv0S71GPEuygLHO33uBk/img.png](https://blog.kakaocdn.net/dn/b1Av3i/btrXnjOs9N4/rQqv0S71GPEuygLHO33uBk/img.png)

url에  "/all?email= 이메일" 을 더해 이메일 정보를 get 으로 보내면, getList 파라미터 email에 이메일 정보가 String 타입으로 들어가게 된다.

![https://blog.kakaocdn.net/dn/bhoBbr/btrXmDGSPnn/kUy33U3xfvJkzOoQDKq2T1/img.png](https://blog.kakaocdn.net/dn/bhoBbr/btrXmDGSPnn/kUy33U3xfvJkzOoQDKq2T1/img.png)

결과로, 해당 멤버가 작성한 모든 Note 정보가 JSON 형식으로 Response된다.

![https://blog.kakaocdn.net/dn/bgY76a/btrXmJ1bQNi/CgV8lKx7VDrkJlpamgVKdk/img.png](https://blog.kakaocdn.net/dn/bgY76a/btrXmJ1bQNi/CgV8lKx7VDrkJlpamgVKdk/img.png)

이번엔 Delete, Modify 작업을 처리해보자.

Response에 "Modified", "removed" 같은 문자를 출력할 것이기에, 응답 포맷은 단순한 문자열인

MediaType.TEXT_PLAIN_VALUE로 지정하자.

![https://blog.kakaocdn.net/dn/cpCYfJ/btrXmJ1wuaJ/FQbbCGV6IXajERiKDz3xGk/img.png](https://blog.kakaocdn.net/dn/cpCYfJ/btrXmJ1wuaJ/FQbbCGV6IXajERiKDz3xGk/img.png)

**Delete**

![https://blog.kakaocdn.net/dn/oKJ4q/btrXlhLLr64/AmXBxaz9sPaSRC9xpZSRu1/img.png](https://blog.kakaocdn.net/dn/oKJ4q/btrXlhLLr64/AmXBxaz9sPaSRC9xpZSRu1/img.png)

요청

![https://blog.kakaocdn.net/dn/dUE0HI/btrXjDuNkGP/akL4XOSDGPKM2azYNl6u2K/img.png](https://blog.kakaocdn.net/dn/dUE0HI/btrXjDuNkGP/akL4XOSDGPKM2azYNl6u2K/img.png)

결과

**Modify**

![https://blog.kakaocdn.net/dn/NNYHa/btrXm9ezhpI/9KpTZ6HmrTlw2vJzWkSk4k/img.png](https://blog.kakaocdn.net/dn/NNYHa/btrXm9ezhpI/9KpTZ6HmrTlw2vJzWkSk4k/img.png)

요청

![https://blog.kakaocdn.net/dn/bA1Tey/btrXmDmWCx6/I3Kee1Cy2zbqgE2033b2O1/img.png](https://blog.kakaocdn.net/dn/bA1Tey/btrXmDmWCx6/I3Kee1Cy2zbqgE2033b2O1/img.png)

결과

여태 작성한 '/notes' 라는 경로는 외부에서 데이터를 주고 받기 위한 경로인데, 이것을 외부 제약 없이 호출하는 것은

서버에 상당한 부하를 주기에, 인증을 거친 사용자에게 한정하여 이러한 서비스를 제공하도록 만들어보자.

웹 애플리케이션에서 쿠키, 세션을 사용하면, 동일한 사이트에서만 동작하기에,

API 서버 처럼 외부에서 데이터를 자유롭게 주고 받지 못한다.

따라서 API 서버를 사용하고, 외부에서 API 호출시, 인증 키 혹은 인증 정보를 같이 전송하여 처리하면 된다.

예를 들어, 이전의 OAuth  게시글에서 OAuth API 를 사용할 때, 자신의 고유 key를 같이 전송하고,

이를 통해 해당 요청이 정상적인 사용자임을 인증하는 방식처럼 동작한다고 생각하면 된다.

이러한 인증 key는 Token이라고도 불리며, 이번 글에선 JWT(JSON Web Token)을 사용해서 처리할 것이다.

이것을 사용해서 외부에서 특정 API를 호출할 시, 인증에 사용할 토큰을 같이 전송하고, 서버에서 이를 검증하는데,

이 과정에서 특정 URL 호출 시 전달된 토큰을 검사할 Filter 가 필요하다.

이 Filter를 스프링 시큐리티를 통해 사용자가 작성하고, 시큐리티 동작의 일부로 추가하는 것으로 처리해보자.

**OncePerRequestFilter**

추상 클래스로 제공되는 필터이고, 매번 동작하는 기본적 필터이다.

해당 추상 클래스를 extends 하여 새로운 필터를 생성해보자.

![https://blog.kakaocdn.net/dn/J5iQD/btrXm6p6tVF/8YkDfNGjhbSEDuTorcg0Tk/img.png](https://blog.kakaocdn.net/dn/J5iQD/btrXm6p6tVF/8YkDfNGjhbSEDuTorcg0Tk/img.png)

OncePerRequestFilter를 상속받고,  log를 출력한 뒤, filterChain.dofilter() 메서드를 통해 다음 필터로 넘어간다.

이제 생성한 필터를 SecurityConfig( @ Configuration 어노테이션으로 빈에 등록할 수 있게 만든 Config 클래스) 에 등록해보자.

![https://blog.kakaocdn.net/dn/oOCa0/btrXnviRk0u/WkjiTcthTjmKXHH2fBTsm1/img.png](https://blog.kakaocdn.net/dn/oOCa0/btrXnviRk0u/WkjiTcthTjmKXHH2fBTsm1/img.png)

위에서 생성한 필터를 등록

이제 '/notes/2' 나 스프링 시큐리티에서 .permitAll() 을 적용한 URL 을 통해 URL을 호출하면,

아래와 같이 적용한 필터가 사용된 것을 확인할 수 있다.

![https://blog.kakaocdn.net/dn/bPdDR8/btrXpcJpijK/0ZUyyyK8odu8GRqia9jBP1/img.png](https://blog.kakaocdn.net/dn/bPdDR8/btrXpcJpijK/0ZUyyyK8odu8GRqia9jBP1/img.png)

로그를 보면 알겠지만, 현재는 생성한 APICheckFilter라는 필터는 맨 마지막에 적용되었다.

만약 이 필터의 순서를 조절하고 싶다면, 기존에 있던 특정 필터의 이전이나 다음에 동작하도록

전 후 관계를 설정할 수 있다.

예를 들어 유저의 아이디와 패스워드를 기반으로 동작하는 필터인 UsernamePasswordAuthenticationFilter의 이전에

동작하도록 만들고 싶다면, 다음과 같이 작성할 수 있다.

![https://blog.kakaocdn.net/dn/SnLb7/btrXnvJU9Pd/fTZxbK46WvHOmqrEa6yjck/img.png](https://blog.kakaocdn.net/dn/SnLb7/btrXnvJU9Pd/fTZxbK46WvHOmqrEa6yjck/img.png)

실행결과는 아래와 같다.

![https://blog.kakaocdn.net/dn/bsAvPM/btrXwiBNsBg/1b1SqKRV5ZBSWJIi9MKHSk/img.png](https://blog.kakaocdn.net/dn/bsAvPM/btrXwiBNsBg/1b1SqKRV5ZBSWJIi9MKHSk/img.png)

생성한 필터의 위치가 바뀌었다.

addFilterBefore 외에도, 아래와 같은 메소드들을 통해 생성한 필터의 동작 순서를 수정할 수 있다.

![https://blog.kakaocdn.net/dn/cD3d1W/btrXoB3I15a/weE52MIdKilVb5bnLBSljK/img.png](https://blog.kakaocdn.net/dn/cD3d1W/btrXoB3I15a/weE52MIdKilVb5bnLBSljK/img.png)

하지만 우리가 추가할 APICheckFIlter는 오직 "/notes/~" 로 시작하는 경우에만 동작하게 구현해야한다.

(기존 로그인과 다르게 토큰 기반으로 구현할 것이기에)

이를 위해, 우리는 AntPathMatcher라는 것을 사용할 것이다.

sql을 사용해본 사람이라면,  Like "A%"  은 A~~~~~ 라는 문자열을 체크한다는 것을 알고 있을 것이다.

이것처럼  ?, *, ** 이 3가지 기호를 사용해서 어떠한 문자열에 패턴에 맞는기 검사하는 메소드이다.

아래는 생성한 필터에 AntPathMatcher를 추가한 코드이다.

![https://blog.kakaocdn.net/dn/LmFJ6/btrXv3ScSXY/qiuT6Atob3vkl9hxuDxQi0/img.png](https://blog.kakaocdn.net/dn/LmFJ6/btrXv3ScSXY/qiuT6Atob3vkl9hxuDxQi0/img.png)

이제 SecurityConfig 파일에 승인할 패턴을 String 타입 파라미터로 전달하는 추가된 생성자를 적용해보자.

![https://blog.kakaocdn.net/dn/vQo25/btrXwi9Exbd/kpJh6UOiAdsrjVYed0Kxbk/img.png](https://blog.kakaocdn.net/dn/vQo25/btrXwi9Exbd/kpJh6UOiAdsrjVYed0Kxbk/img.png)

? : 1개의 문자와 매칭

- : 0개 이상의 문자와 매칭
- * : 0개 이상의 디렉토리와 매칭

으로 치환되기에, 위의 "/notes/**/*" -> /notes/~~~~ 인 url로 매칭되어 필터를 통과하게 된다.

만약 /notes/~~~ 의 형식이 아닌 url이 들어오면, antPathMatcher.match() 의 결과가 false로 도출되어

로그가 출력되지 않는다.

![https://blog.kakaocdn.net/dn/bh9cE3/btrXoChigW7/RV6xNxZKaVbre8GBzlRTlK/img.png](https://blog.kakaocdn.net/dn/bh9cE3/btrXoChigW7/RV6xNxZKaVbre8GBzlRTlK/img.png)

실행 결과

여기까지, "/notes/~~" 에 대해  동작하는 Filter에 대해 작성하였다.

이제 API를 통한 로그인, 그리고 인증 처리에 대해 살펴보자.

**ApiLoginFilter**

특정 URL로 외부에서 로그인 가능하고, 성공하면 클라이언트가 Authorization 헤더의 값으로 이용할 데이터를 전송하는 필터를 만들어보자. 이번엔 전과 다르게 AbstractAuthenticationProcessingFilter라는 추상 클래스를 상속받아 작성하자.

AbstractAuthenticationProcessingFilter는 이름에서 유추할 수 있듯이, 추상 클래스이고,

attemptAuthentication()이라는 추상 메서드와 문자열로 패턴을 받는 생성자가 기본으로 필요하다.

![https://blog.kakaocdn.net/dn/bS1T9g/btrXtRkeT1Q/vzSjC9RYTvVUcp5yBI0d1k/img.png](https://blog.kakaocdn.net/dn/bS1T9g/btrXtRkeT1Q/vzSjC9RYTvVUcp5yBI0d1k/img.png)

email 값을 파라미터로 받고, 만약 해당 값이 null( 존재하지 않는다) 이면 에러를 트리거한다.

이제 Config 파일에서 AbstractAuthenticationProcessingFilter를 사용하기 위해선 authenticationManager가 필요한데,

스프링 3.0 이전에는 WebSecurityConfigurationAdapater에 authenticationManger 변수를 사용하는 것으로 가능했다.

하지만, 스프링 3.0 이후, 해당 클래스가 deprecated 되었기에, 3.0 이후의 버전 사용자는 이제 아래와 같이 Builder를 통해

Build해주어야한다.

![https://blog.kakaocdn.net/dn/bFVzlQ/btrXrNWFT5m/pzbphOj2dcCxyVXYCJwRA0/img.png](https://blog.kakaocdn.net/dn/bFVzlQ/btrXrNWFT5m/pzbphOj2dcCxyVXYCJwRA0/img.png)

authenticationManager 생성하기

![https://blog.kakaocdn.net/dn/NRYfU/btrXrMwF1em/5SIZ33CjDubp3U14LZBTjk/img.png](https://blog.kakaocdn.net/dn/NRYfU/btrXrMwF1em/5SIZ33CjDubp3U14LZBTjk/img.png)

추가

![https://blog.kakaocdn.net/dn/bBD5ci/btrXoCO5BDw/E05NK4lckgWrbHUQxs2RJK/img.png](https://blog.kakaocdn.net/dn/bBD5ci/btrXoCO5BDw/E05NK4lckgWrbHUQxs2RJK/img.png)

Builder로 생성한 authenticationManager 사용

이제 /api/login url에 email 정보 없이 접근해보자.

![https://blog.kakaocdn.net/dn/nBvdl/btrXooDmdXV/Sj0ZN1vZ5HhvJw0frQykBk/img.png](https://blog.kakaocdn.net/dn/nBvdl/btrXooDmdXV/Sj0ZN1vZ5HhvJw0frQykBk/img.png)

"email" 값을 전달하지 않았기에 에러가 뜬다.

![https://blog.kakaocdn.net/dn/n2lqb/btrXnk9t2NB/Jzh6DhzHetZKXF8lufyY4k/img.png](https://blog.kakaocdn.net/dn/n2lqb/btrXnk9t2NB/Jzh6DhzHetZKXF8lufyY4k/img.png)

만약 url에 email 이라는 값을 파라미터로 전달하면 아래와 같이 올바르게 진입 가능하다.

![https://blog.kakaocdn.net/dn/yI0zy/btrXrOnJkDz/KjMaAyBA5w58tryI1DOuNK/img.png](https://blog.kakaocdn.net/dn/yI0zy/btrXrOnJkDz/KjMaAyBA5w58tryI1DOuNK/img.png)

View 영역을 생성하지 않아 공백이지만 올바르게 접속된 것

![https://blog.kakaocdn.net/dn/CVbaN/btrXqDUjIyj/sRSdDYQnQJrJkvklDetf1k/img.png](https://blog.kakaocdn.net/dn/CVbaN/btrXqDUjIyj/sRSdDYQnQJrJkvklDetf1k/img.png)

email의 존재가 확인되었다.

앞에서도 말했지만, 특정 API를 호출하는 클라이언트는 다른 서버나 Application으로 실행되기에 쿠키, 세션을 사용할 수 없기에 API를 호출하는 Request를 전송할 때, Http 헤더 메세지에 특별한 key값을 지정해서 전송하는 것으로 정상적인 사용자인지 판별한다. 이때 사용하는 헤더가 Authorization 헤더이다.

예를 들어, 아까 생성한 ApiCheckFilter에서 Authorization 헤더를 추출하고, 헤더의 값이 "1234" 인 경우에만 인증되게 만들고 싶다면 어떻게 할까?

이전에 생성한 ApiCheckFilter에 아래와 같은 메소드를 추가하여 Header의 값이 적절한지 확인해보겠다.

![https://blog.kakaocdn.net/dn/ch1SPe/btrXn5RxJKU/AHk2mH1WLF40lEFHyAxb5k/img.png](https://blog.kakaocdn.net/dn/ch1SPe/btrXn5RxJKU/AHk2mH1WLF40lEFHyAxb5k/img.png)

이후 해당 메소드에서 True 값이 나올때만 doFilter() 메소드를 통해 넘어가도록 구현해보자.

![https://blog.kakaocdn.net/dn/qN4HZ/btrXom6B4u9/pEKFfWLxpvyT30EobxghWk/img.png](https://blog.kakaocdn.net/dn/qN4HZ/btrXom6B4u9/pEKFfWLxpvyT30EobxghWk/img.png)

결과

![https://blog.kakaocdn.net/dn/ceZspo/btrXqr7n1qA/88bSM1sIIDAI7SYp2yCPUK/img.png](https://blog.kakaocdn.net/dn/ceZspo/btrXqr7n1qA/88bSM1sIIDAI7SYp2yCPUK/img.png)

올바른 Authentication 헤더값 일시

![https://blog.kakaocdn.net/dn/bqTzpQ/btrXoDm0JTw/WWgjmSmbO1zYZfvUl5cz9K/img.png](https://blog.kakaocdn.net/dn/bqTzpQ/btrXoDm0JTw/WWgjmSmbO1zYZfvUl5cz9K/img.png)

올바른 값 도출

Authenticatoin 헤더에 "1235" 라는 다른 인증 값을 넣었을 때, 혹은 Authentication 헤더가 없을 때는 아래와 같은 결과가 나온다.

![https://blog.kakaocdn.net/dn/dIG63V/btrXwjtZbbs/xuw2HTttKfgz7sfyUzn8l1/img.png](https://blog.kakaocdn.net/dn/dIG63V/btrXwjtZbbs/xuw2HTttKfgz7sfyUzn8l1/img.png)

에러가 검출되야하지만, 되지 않는다.

데이터가 전달되지는 않지만, 분명 "에러" 를 검출해야 되는 상황에서도 200( 정상 ) 메세지가 출력된다.

이는 앞서 생성한 ApiCheckFilter가 스프링 시큐리티가 사용하는 쿠키, 세션을 사용하지 않기에 생기는 문제이다.

따라서 간단하게 ApiCheckFIlter에서 JSON 포맷으로 에러메세지를 전송하는 것으로 에러를 표기하는 것으로 Error Handling을 해주자.

**ApiCheckFilter**

![https://blog.kakaocdn.net/dn/LrGJA/btrXv4jjPOY/PGOkokELaLx4NJ7byH8NR1/img.png](https://blog.kakaocdn.net/dn/LrGJA/btrXv4jjPOY/PGOkokELaLx4NJ7byH8NR1/img.png)

checkHeader가 false 일시

![https://blog.kakaocdn.net/dn/LmEAF/btrXnjiuSvD/6FfHrb99jzkHrHIpRrf6e0/img.png](https://blog.kakaocdn.net/dn/LmEAF/btrXnjiuSvD/6FfHrb99jzkHrHIpRrf6e0/img.png)

403 에러 메세지를 JSON으로 전달 받아 올바르게 error를 Handle하였다.

이제 로그인 처리를 수행하는 ApiLoginFilter로 다시 돌아가서 올바르게 동작하도록 수정해보자.

정상적인 동작을 위해선, 내부적으로 AuthenticationManager를 가지고 동작하도록 수정해야하는데, 이 안의 authenticate() 는 파라미터와 리턴 타입 모두 Authentication 타입이다.

UsernamePasswordAuthenticationToken

ApiLoginFilter를 수정하자.

![https://blog.kakaocdn.net/dn/cMTxgK/btrXv4DFFB5/FOAJMSgGy5igBEmQn1fIi0/img.png](https://blog.kakaocdn.net/dn/cMTxgK/btrXv4DFFB5/FOAJMSgGy5igBEmQn1fIi0/img.png)

이메일 ,password 정보를 url 에서 받아오고, UsernamePasswordAuthenticationToken으로 토큰화 하여

AuthenticationManager의 authenticate() 메소드의 파라미터로 전달하는 것으로 로그인 인증을 진행한다.

이제 아래의 url 처럼 아이디, 비밀번호 정보를 파라미터로 전달해보자.

http://localhost:8080/api/login?email=user10@naver.com&pw=1111

결과화면이다. Mapping한 Controller를 설정하지 않아 no explicit mapping이 나오긴 하지만, 올바르게 로그인 정보가 들어갔다.

![https://blog.kakaocdn.net/dn/pz6Dl/btrXrNbkyrJ/mae0lIzkKqheX9i93llSl0/img.png](https://blog.kakaocdn.net/dn/pz6Dl/btrXrNbkyrJ/mae0lIzkKqheX9i93llSl0/img.png)

![https://blog.kakaocdn.net/dn/b1wdOc/btrXrNvEGZy/WRJOdXBykEggnIosFPhJl1/img.png](https://blog.kakaocdn.net/dn/b1wdOc/btrXrNvEGZy/WRJOdXBykEggnIosFPhJl1/img.png)

로그인 사용자만 접속 가능한 /sample/member에 접근했다.

*주의*

여기서는 비밀번호까지 묶어서 토큰화하여 Get 방식으로 전달했지만, Get방식은 정보를 모두 노출하기에,

꼭 Post 방식을 사용하여 Body에 정보를 숨겨야하며, 비밀번호 파라미터의 암호화 역시 진행된 상태로 전달해야한다.

하지만 간편성을 위해 암호화, Post 방식 둘다 사용하지 않았다.

이제 ApiLoginFilter를 통한 직접 인증처리를 진행했으니, 이에 대한 인증 성공/ 실패에 대한 핸들러를 작성하여 처리해보자.

Fail/Success Handler를 별도의 클래스로 만들어 처리할 수 도 있고,  앞서 ApiLoginFilter가 상속받은

AbstractAuthenticationProcessingFilter의 fail,successhandler를 Override 하여 처리할 수 도 있기에

Fail의 핸들러는 별도의 Class로 구성하고, Success의 핸들러는 Override하여 구현해보겠다.

**FailHandler**

![https://blog.kakaocdn.net/dn/nyVzy/btrXm9NZJNq/XI7H8LKInkzKRfZBaWykqk/img.png](https://blog.kakaocdn.net/dn/nyVzy/btrXm9NZJNq/XI7H8LKInkzKRfZBaWykqk/img.png)

별도의 클래스를 생성하여 처리

![https://blog.kakaocdn.net/dn/bckn1l/btrXqD1aPNd/r5zQ5Ut8T3Ay8BgfM5x5Gk/img.png](https://blog.kakaocdn.net/dn/bckn1l/btrXqD1aPNd/r5zQ5Ut8T3Ay8BgfM5x5Gk/img.png)

SecurityConfig 파일에사 apiLoginFilter에 생서안 Fail Handler를 추가한다.

이제 올바르지 않은 인증 정보를 전달해보면, 아래와 같이 fail 상황을 handle한다.

![https://blog.kakaocdn.net/dn/cx9ACy/btrXt87l4yq/JuN3xs1KLKyJfBT4RK5EX1/img.png](https://blog.kakaocdn.net/dn/cx9ACy/btrXt87l4yq/JuN3xs1KLKyJfBT4RK5EX1/img.png)

**SuccessHandler**

![https://blog.kakaocdn.net/dn/bc8RQD/btrXrNblny5/Y81ZjUvscK3UKA0KlixhrK/img.png](https://blog.kakaocdn.net/dn/bc8RQD/btrXrNblny5/Y81ZjUvscK3UKA0KlixhrK/img.png)

이번엔 AbstractAuthenticationProcessingFilter 인터페이스에 존재하는 메소드를 Override하여 SuccessHandler를 구현했다.

아래와 같이 로그인 성공시의 상황을 log를 통해 확인하는 것으로, 로그인 성공시의 핸들러가 올바르게 동작함을

볼 수 있다.

![https://blog.kakaocdn.net/dn/c2Qd1G/btrXrMDvmbP/xdNR4q9IabMr3JiaKSGrN0/img.png](https://blog.kakaocdn.net/dn/c2Qd1G/btrXrMDvmbP/xdNR4q9IabMr3JiaKSGrN0/img.png)

- * 만약 Failt Hander도 Override하고 싶은 경우,

```ebnf
unsuccessfulAuthentication
```

위의 메소드를 Override하면 되고, 파라미터로 들어온 "failed" 가 에러 메세지를 담고 있는 AuthenticationException 이다.

## **JWT**

인증에 성공한 뒤, 사용자가 '/notes/xxx' 와 같은 API를 호출하면 적절한 데이터를 만들어서 인증 헤더에 넣는 것으로

인증을 수행하는데, 이 인증 헤더에 넣을 인증 값을 JWT를 통해 생성하겠다.

인증에 성공한 사용자에게 특수한 문자열(JWT)를 제공하고, API 호출 시 해당 문자열을 읽어 해당 Request가 정상적인지 확인하는 용도로 사용하는 것이다.

![https://blog.kakaocdn.net/dn/QEqQn/btrXpbxtlR0/psU6KJyzIak2Kb3AJ0JKzk/img.png](https://blog.kakaocdn.net/dn/QEqQn/btrXpbxtlR0/psU6KJyzIak2Kb3AJ0JKzk/img.png)

JWT 문자열 예시

위 JWT 문자열을 보면 "." 으로 구분되어 있는데, 이 "." 을 통해 3개의 파트로 나누어진다.

- Header: 토큰 타입, 알고리즘을 의미 ( 주로 RSA, HS256 )
- Payload: name, value의 순서쌍인 Claim을 모아둔 객체이다.
- Signature 헤더의 인코딩 값, 정보의 인코딩 값을 합쳐 비밀키를 만든 뒤, 해시 함수로 처리된 결과

JWT는 Header와 Payload를 단순히 Base64를 통해 인코딩한 결과이기에, 누군가가 디코딩하여 내용물을 알아낼 여지가 있다.

따라서 마지막에 Signature를 이용한 암호화 값을 같이 사용하여 암호화할 때, "비밀 키"를 모르면 검증할 수 없는 점을 통하여 이러한 문제점을 방지한다.

![https://blog.kakaocdn.net/dn/dULmoS/btrXpc4e5fw/2beWMYsQKC7K2WgcVQZ7Bk/img.png](https://blog.kakaocdn.net/dn/dULmoS/btrXpc4e5fw/2beWMYsQKC7K2WgcVQZ7Bk/img.png)

Payload, Header의 인코딩 정보를 암호화 하기 전

![https://blog.kakaocdn.net/dn/UfES2/btrXo544Khe/IAXAskkWAKemWnLELwG5nk/img.png](https://blog.kakaocdn.net/dn/UfES2/btrXo544Khe/IAXAskkWAKemWnLELwG5nk/img.png)

payload와 Header의 인코딩 정보를 암호화한 것

JWT를 직접 구현하여 이용할 수 도 있고, Spring Security OAuth에서 제공하는 클래스를 사용해도 되지만,

이번에는 가장 쉬운 라이브러리인 io.jsonwebtoken:jjwt 를 사용하겠다.

```
implementation 'io.jsonwebtoken:jjwt:0.9.1'
```

위의 라인을 build.gradle에 추가한 후, JWT를 사용해보자.

아래는 새롭게 추가한 JWTUtil 클래스의 구현 코드다.

```bash
@Log4j2
public class JWTUtil {

    private String secretKey="shyswy12345678";

    private long expire=6*24*30; // 1달간 유효
    //JWT 문자열이 노출되면 누구나 모든 내용 확인 가능하기에, 유효기간 설정

    public String generateToken(String content) throws Exception{ //JWT 토큰 생성하기.
        return Jwts.builder()
                .setIssuedAt(new Date()) //시작점
                .setExpiration(Date.from(ZonedDateTime.now().plusMinutes(expire).toInstant())) //만료 설정
                .claim("sub",content) //name, value의 claim 쌍 여기에 이메일 등 저장할 정보를 "sub"라는 이름으로 추가
                .signWith(SignatureAlgorithm.HS256,secretKey.getBytes("UTF-8")) //알고리즘, 비밀키 설정
                .compact();
    }

    public String validateAndExtract(String tokenStr)throws Exception{ //인코딩된 문자열에서 원하는 값 추출
        //"sub"의 이름으로 들어갔던 Content의 값을 추출한다.
        String contentValue=null;

        try{ //DefaultJws를 구하는 과정에서, 유효기간이 만료되었다면 Exception을 트리거한다.
            DefaultJws defaultJws =(DefaultJws) Jwts.parser() //입력으로 들어온 인코딩된 String 해독하기.
                    .setSigningKey(secretKey.getBytes("UTF-8")) //해독에 필요한 비밀 키 넣기
                    .parseClaimsJws(tokenStr);  //입력으로 들어온 인코딩된 String 넣기
            log.info(defaultJws);
            log.info(defaultJws.getBody().getClass());
            DefaultClaims claims=(DefaultClaims) defaultJws.getBody(); //name,value의 claim쌍 추출
            log.info("---------------------------------");
            contentValue=claims.getSubject(); //content 값 추출
        }catch (Exception e){
            e.printStackTrace();;
            log.error(e.getMessage());
            contentValue=null; //초기화
        }
        return contentValue;
    }

}
```

우리가 사용자를 식별하는 email 값을 content로 저장한다고 가정해보자.

generateToken() 메소드의 파라미터로 email 값을 저장한 String을 넘겨주면, 만료기간, 해독에 쓰일 비밀키,

그리고 저장할 content를 name, value의 Claim 순서쌍으로 만들어서 JWT 토큰으로 리턴해준다.

그리고 validateAndExtract를 통해 인코딩된 String을 파라미터로 넘겨주면, content를 추출하여 리턴한다.

이제 올바르게 JWT토큰이 생성되고, 해독되는지 Test 해보자.

```bash
public class JWTTest {

    private JWTUtil jwtUtil;

    @BeforeEach
    public void beforeTest(){
        jwtUtil=new JWTUtil();
    }

    @Test
    public void testEncode() throws Exception{
        String email="user10@naver.com";
        String jwtString=jwtUtil.generateToken(email);
        System.out.println("encode: "+jwtString);
        String ans=jwtUtil.validateAndExtract(jwtString);
        System.out.println("decode: "+ans);
    }
}
```

아래는 결과이다.

```bash
encode: eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE2NzUwNDE5MTAsImV4cCI6MTY3NTMwMTExMCwic3ViIjoidXNlcjEwQG5hdmVyLmNvbSJ9.vTjlHWh0yI9DOHJ0giLpxdcJ9zVFM4ga90Go1g1eXFY
10:25:10.510 [Test worker] INFO com.example.clubsite.security.util.JWTUtil - header={alg=HS256},body={iat=1675041910, exp=1675301110, sub=user10@naver.com},signature=vTjlHWh0yI9DOHJ0giLpxdcJ9zVFM4ga90Go1g1eXFY
10:25:10.510 [Test worker] INFO com.example.clubsite.security.util.JWTUtil - class io.jsonwebtoken.impl.DefaultClaims
10:25:10.510 [Test worker] INFO com.example.clubsite.security.util.JWTUtil - ---------------------------------
decode: user10@naver.com
```

generateToken을 통해 암호화가 완료되고, validateAndExtract로 올바르게 원래의 이메일 값이 해독되는 것을 볼 수 있다.

jwt.io를 통해 암호화된 JWT 토큰을 좀 더 자세히 분석해보자.

![https://blog.kakaocdn.net/dn/btdL4X/btrXnJ2Oq8z/dr27prcsz8K4vu10qLu0uK/img.png](https://blog.kakaocdn.net/dn/btdL4X/btrXnJ2Oq8z/dr27prcsz8K4vu10qLu0uK/img.png)

다음과 같이 설정한 값들이 올바르게 추출되는 것을 확인할 수 있다.

이번엔 만료기간에 대해 확인해보자.

우선 JWT 토큰의 만료기간을 임시적으로 1초로 설정하자.

![https://blog.kakaocdn.net/dn/bWnOID/btrXo5KLoeC/yR8QBaM2r5pkzZ3wThjwX1/img.png](https://blog.kakaocdn.net/dn/bWnOID/btrXo5KLoeC/yR8QBaM2r5pkzZ3wThjwX1/img.png)

만료 기간 수정

![https://blog.kakaocdn.net/dn/cBIvuE/btrXoDA1bRP/kwqTluPdefkttwk0kwDRbK/img.png](https://blog.kakaocdn.net/dn/cBIvuE/btrXoDA1bRP/kwqTluPdefkttwk0kwDRbK/img.png)

테스트 코드

![https://blog.kakaocdn.net/dn/mI0p0/btrXv4qCgQ6/QIMoH6x6BDhk5CeaxIHwZ0/img.png](https://blog.kakaocdn.net/dn/mI0p0/btrXv4qCgQ6/QIMoH6x6BDhk5CeaxIHwZ0/img.png)

만료되었다는 Exception이 뜬다.

이제 생성한 JWT 토큰이 올바르게 동작한다는 것을 알았으니,

이전에 생성한 ApiLoginFilter, ApiCheckFilter 등에 적용하여 구성한 API에 대한 인증 처리를 진행해보자.

우선 ApiLoginFilter에 JWT를 적용하자.

우리는 인증에 성공한 사용자에게 JWT토큰을 발행하여 content를 확인할 권한을 줄 것이기에,

SuccessHandler에서 Token을 발행하도록 처리한다.

![https://blog.kakaocdn.net/dn/15gQI/btrXwjVzU4K/1k8Tb0s7GLd8CfI7u2ZcX0/img.png](https://blog.kakaocdn.net/dn/15gQI/btrXwjVzU4K/1k8Tb0s7GLd8CfI7u2ZcX0/img.png)

인증 성공시에 JWT토큰 발행

로그인 인증 성공 시 JWT 토큰을 발행하는 것에 성공했으니, 이제 외부에서 접근 시, 발행 받은 JWT 토큰을 통해 접근을 허가 받는 작업을 ApiCheckFIlter를 통해 구현해보자.

기존에 ApiCheckerFilter에서 "1234" 라는 임의의 인증 헤더 값을 JWTUtil의 validateAndExtract로 Decode하는 것으로 변경하자.

![https://blog.kakaocdn.net/dn/bGLics/btrXo46hCAs/sKarBSMzZ3lhAmtKoyAT40/img.png](https://blog.kakaocdn.net/dn/bGLics/btrXo46hCAs/sKarBSMzZ3lhAmtKoyAT40/img.png)

이제 SecurityConfig에 JWTUtil을 추가하자.

![https://blog.kakaocdn.net/dn/bytS7V/btrXooqmbFb/xKJi26y0YX5mJ5Pclla05K/img.png](https://blog.kakaocdn.net/dn/bytS7V/btrXooqmbFb/xKJi26y0YX5mJ5Pclla05K/img.png)

JWTUtil을 빈으로 등록

![https://blog.kakaocdn.net/dn/PHaOC/btrXv2TU4Wm/bOW6jwCLkYKzdECI9kDQLk/img.png](https://blog.kakaocdn.net/dn/PHaOC/btrXv2TU4Wm/bOW6jwCLkYKzdECI9kDQLk/img.png)

등록한 jwtUtil을 ApiLoginFilter의 생성자 파라미터에 추가하여 생성자 주입

![https://blog.kakaocdn.net/dn/caikJg/btrXAi9Grrd/eJRGwnUrJuD6KqQI2gkMEk/img.png](https://blog.kakaocdn.net/dn/caikJg/btrXAi9Grrd/eJRGwnUrJuD6KqQI2gkMEk/img.png)

등록한 jwtUtil을 ApiCheckFilter의 생성자 파라미터에 추가하여 생성자 주입

이제 기능을 테스트 해보자.

1) 로그인 인증 성공 시, JWT 토큰을 발행하는가?

```
http://localhost:8080/api/login?email=user10@naver.com&pw=1111
```

위와 같이, DB에 존재하는 유저정보로 APILoginFilter를 사용하여 로그인 시도시 아래와 같이 JWT 토큰이 발행된다.

![https://blog.kakaocdn.net/dn/bSiLe3/btrXzZbgpjo/Q9udAYgfhZ1HJQ14V5BazK/img.png](https://blog.kakaocdn.net/dn/bSiLe3/btrXzZbgpjo/Q9udAYgfhZ1HJQ14V5BazK/img.png)

인증이 성공된 뒤, SuccessHandler에서 JWT 토큰을 발행한다.

2) 발행받은 JWT토큰을 인증헤더에 넣어서 인증이 올바르게 이루어지는가?

ApiCheckFilter를 통해 "/notes/~" 에 접근 시도 시, 인증 헤더가 올바르게 통과하고, 잘못된 인증 정보, 혹은 인증 정보가 없을 시는 접근이 반려 되는지 테스트 해보자.

![https://blog.kakaocdn.net/dn/cWS8DA/btrXrTwqhu0/gD7c3bLpvK6WnkTTbayv60/img.png](https://blog.kakaocdn.net/dn/cWS8DA/btrXrTwqhu0/gD7c3bLpvK6WnkTTbayv60/img.png)

발행받은 JWT토큰 앞에 인증 타입 "Bearer " 추가하여 인증헤더 추가

올바르게 인증정보를 추가 후, Request를 보낸다.

![https://blog.kakaocdn.net/dn/9KBq4/btrXn6QYBH4/dmPJziiiHJ88s60ZZ7Jih1/img.png](https://blog.kakaocdn.net/dn/9KBq4/btrXn6QYBH4/dmPJziiiHJ88s60ZZ7Jih1/img.png)

ApiCheckFilter에서 validateAndExtract()메소드로 인증 헤더의 값 추출 성공.

만약 JWT토큰을 바꾸거나, Authorization Header를 제거하면, 아래와 같이 오류가 발생한다.

![https://blog.kakaocdn.net/dn/bAME78/btrXwhXL6Nw/7yazVyUYhHqrOXIex9izg1/img.png](https://blog.kakaocdn.net/dn/bAME78/btrXwhXL6Nw/7yazVyUYhHqrOXIex9izg1/img.png)

잘못된 인증헤더로 인한 오류.

이와 같은 방식으로 외부 API 서버에 대한 인증을, 인증 헤더에 JWT토큰을 넣는 것으로 해결할 수 있다.

이제 REST 방식의 테스트는 모두 성공했지만, 외부에서 Ajax를 이용해서 API를 사용하려면 마지막으로 CORS(Cross-Origin-Resource-Sharing) 을 처리해야한다.

**CORS**: Origin(브라우저) 에서 Cross-Origin(다른 출처)의 리소스를 공유하는 방식이다.

same-origin이란 scheme(프로토콜), host(도메인), 포트가 같다는 말이며, 이 3가지 중 하나라도 다르면 cross-origin이다.

이를 위해 CORS 필터를 앞선 ApiCheckFilter, ApiLoginFilter 처럼 클래스를 생성하여 처리하자.

![https://blog.kakaocdn.net/dn/kBxxF/btrXtRSDGno/R5KWJGMKPO5V8ba13Cx730/img.png](https://blog.kakaocdn.net/dn/kBxxF/btrXtRSDGno/R5KWJGMKPO5V8ba13Cx730/img.png)

CORS 필터 클래스 구현

이렇게 JSON을 이용하는 API 서버에서 CRUD를 구성하고, API 서버의 보안을 Spring Security로 처리하며, 인증을 JWT를 사용해서 구현해보았다.