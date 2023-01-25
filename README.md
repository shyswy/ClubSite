# README

# ClubMember

간단한 클럽 멤버 로그인 서비스를 통해 스프링 시큐리티를 분석해보았다.

# Spring Security의 구조

스프링 시큐리티를 통한 작업은 크게 2가지로 나눌 수 있다.

인증(Authentication): 해당 사용자가 본인이 맞는지를 확인

인가(Authorization): 인증된 사용자가 요청한 자원에 접근 가능한지 확인

유저가 어떠한 것을 요청하면, 해당 유저가 본인이 맞는가? 에 대한 인증을 거친 뒤, 인증(Authentication)을 통해 확인한 유저가 요청한 것을 받을 자격이 있는지 확인하는 인가(Authorization) 를 거친 뒤, 요청한 결과값을 돌려준다.

보통의 필터는 스프링의 빈을 사용할 수 없기 때문에 별도의 클래스를 상속 받아야 하지만,

스프링 시큐리티는 빈과 연동할  수 있는 구조로 설계되어있다.

**[Authentication: 인증]**

스프링 시큐리티 내에서는 Filter Chain이라는 구조로 Request를 처리한다.

![https://blog.kakaocdn.net/dn/TNdmx/btrWR73IT3n/mc2SpkSLykxPvFsP06dnQK/img.png](https://blog.kakaocdn.net/dn/TNdmx/btrWR73IT3n/mc2SpkSLykxPvFsP06dnQK/img.png)

실제 사용되는 스프링 시큐리티의 주요 필터

유저가 아이디, 비밀번호와 같은 인증에 관련한 정보를 입력하면, UsernamePasswordAuthenticationToken과 같은 토큰이라는 객체로 만들어서 AuthenticationManager(인증 매니저) 에게 전달한다.

아래는 기본으로 제공되는 필터 중, UsernamePasswordAuthenticationFilter 클래스의 일부이다.

![https://blog.kakaocdn.net/dn/V5TAy/btrWU7IHegi/pE35cksFTj3P7CvaRZPLl1/img.png](https://blog.kakaocdn.net/dn/V5TAy/btrWU7IHegi/pE35cksFTj3P7CvaRZPLl1/img.png)

아이디, 비밀번호를 받은 뒤, Token객체에 넣어서 인증 매니저에게 전달한다.

## **Authentication Manager: 인증 매니저**

![https://blog.kakaocdn.net/dn/uL5Nh/btrWRzsRJFV/X0swK5FH4lOm4WkArg68o1/img.png](https://blog.kakaocdn.net/dn/uL5Nh/btrWRzsRJFV/X0swK5FH4lOm4WkArg68o1/img.png)

여러 객체가 서로 데이터를 주고 받으며 이루어진다.

인증 매니저는 데이터베이스를 통한 인증, 메모리상 데이터를 통한 인증 등 다양한 처리 방법을 이용한다.

이때, 처리에 사용하는 것이 AuthenticationProvider 이다.

## **AuthenticationProvider**

![https://blog.kakaocdn.net/dn/qJ3vq/btrWWtrfzBA/zBH1UUG9C9RWiKNB3Ci750/img.png](https://blog.kakaocdn.net/dn/qJ3vq/btrWWtrfzBA/zBH1UUG9C9RWiKNB3Ci750/img.png)

Authentication Provider이 처리가능한 하위 구현 클래스들

Authentication Provider는 인증 매니저가 전달한 토큰 타입이 처리할 수 있는 타입인지 확인한 뒤, 이를 통해 authenticate() 메서드를 수행한다.

Authentication Provider는 내부적으로  UserDetailsService를 사용하고,

UserDetailsService는 실제로 인증을 위한 데이터를 가져오는 역할을 수행한다.

**[Authorization: 인가]**

인증을 끝낸 뒤에는, 사용자가 요청한 것에대한 권한을 가지고 있는지 확인하는 작업이 필요하다.

인증 매니저 내부의 authenticate() 라는 메소드는 Authentication이라는 "인증 정보"를 리턴한다.

이 정보 내에는 Roles라는 "권한"에 대한 정보값이 있고, 이 정보를 바탕으로 사용자가 요청한 정보를 허가하거나 거부하는 Access-Control을 수행하게 된다.

간단한 예로, 유저가 로그인 없이  /sample/member 라는 url에 접근할 경우, SpringSecurity를 통한 인증, 인가의 절차를 구현하며 스프링 시큐리티의 흐름에 대해 알아보자.

# ClubMember 기능 구현

## Security Configuration

### **PasswordEncoder**

비밀번호를 어떤 방식의 암호화를 통해 저장할 지 정의한다.

앞서 보여준 SecurityConfig를 확인해보자

![https://blog.kakaocdn.net/dn/bkPoMR/btrWS3HlkDV/9ud8UvIlQCICKxneMeb981/img.png](https://blog.kakaocdn.net/dn/bkPoMR/btrWS3HlkDV/9ud8UvIlQCICKxneMeb981/img.png)

나는 BycryptPasswordEncoder 방식을 사용하였는데, 이 방식은  해시 함수를 통해 패스워드를 암호화하는 클래스로, 암호화한 패스워드는 원래대로 복호화가 불가능하고, 매번 암호화된 값도 다르다.

원본 비밀번호를 볼 수 없기 때문에, 보안성이 좋아 최근에 자주 사용한다.

### **FilterChain**

![https://blog.kakaocdn.net/dn/UdvmL/btrWREt8Vaw/G8KzKHdfhgVMa9LkOYIDT0/img.png](https://blog.kakaocdn.net/dn/UdvmL/btrWREt8Vaw/G8KzKHdfhgVMa9LkOYIDT0/img.png)

로그인 없이 /sample/member의 url을 입력하면 스프링 시큐리티의 필터에서 인증, 인가가 필요한지 여부를 판단한다.

위와 같은 filter를 생성한 경우, "/sample/member"  에 접근 시, "USER"라는 Role을 가진 사람만 허용되기에, 인증-인가 절차를 수행해야한다. http.formLogin() 메소드를 통해, 로그인 되지 않은 유저는 아래와 같이 로그인 화면으로 리다이렉트 된다.

![https://blog.kakaocdn.net/dn/EEINL/btrWZVHxs0X/VGHkmnyRCEOuUIUn384ug0/img.png](https://blog.kakaocdn.net/dn/EEINL/btrWZVHxs0X/VGHkmnyRCEOuUIUn384ug0/img.png)

\

이후의 흐름은 다음과 같다.

1) 유저가 아이디, 비밀번호를 입력하면, Token에 담아 인증 매니저에 전달한다.

2) 인증 매니저는 적절한 AuthenticationProvider를 찾아서 인증을 시도한다.

3) AuthenticationProvider가 UserDetailsService를 통해 올바른 사용자라고 인증하면, 사용자 정보를 Authentication 타입으로 전달한다.

4) 전달된 객체로 사용자에게 적절한 권한이 있는지 확인하는 인가(Authorization) 과정을 거친다.

5) 만약 유저에게 "USER" 권한이 존재한다면, 요청한 url "/sample/member"에 대한 작업을 수행한다.

1->2 의 과정은 거의 자동으로 이루어지기에,

UserDetailsService를 통해 올바른 유저인지 확인하는 인증 과정, 그리고 권한에 대한 인가 과정에 대해 실제 구현을 통해 디테일하게 알아보자.

실제 구현에선 UserDetailService 인터페이스의 loadUserByName(username)라는  username 정보를 파라미터로 넘기면 *UserDetail(인증, 인가에 사용되는 정보를 담은 객체) 를 반환하는 메소드를 사용한다.

- UserDetail

회원 정보를 저장하는 타입. 회원 정보를 가지고 올 수 있는 몇가지 메소드를 지원한다.

getAuthorities() : 사용자 권한 정보를 제공

getPassword() : username에 매칭되는 비밀번호 정보를 제공

getUsername() : 인증에 필요한 아이디와 같은 username 정보

위와 같은 정보를 가지고 처리하는 방법에는, 2가지가 있다.

1) DTO 클래스에 UserDetail 인터페이스를 구현하는 방식

2) 별도의 클래스를 구성하고 DTO 처럼 사용하는 방식.

개인적으로 DTO로 처리하는 방식을 선호하여 2번 방식으로 설명하겠다.

## DTO

우선 회원 정보에 대한 DTO를 생성해준 뒤, userDetial의 하위에 있는 User를 상속하고, User의 생성자를 사용할 수 있게 만들어준다. ( alt + enter를 통해 손쉽게 기본 구조를 완성하면 편하다)

![https://blog.kakaocdn.net/dn/dZLt9u/btrWU7P3IIq/M2z0oG8oDrn0EKXx4Zkeik/img.png](https://blog.kakaocdn.net/dn/dZLt9u/btrWU7P3IIq/M2z0oG8oDrn0EKXx4Zkeik/img.png)

이후 DTO를 구성해주면 스프링 시큐리티에서 인증/인가 작업에서 사용하는 동시에 유저 정보에 대한 DTO로써 사용할 수 있다.

![https://blog.kakaocdn.net/dn/xAwgX/btrW39lsOYp/ooEMFLInLI45wlFbqnFP71/img.png](https://blog.kakaocdn.net/dn/xAwgX/btrW39lsOYp/ooEMFLInLI45wlFbqnFP71/img.png)

## UserDetailService

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

접속한 유저가 "USER" 라는 Role을 가지고 있기에 "Has USER ROLE" 이라는 문장만 화면에 보이게 되고,

isAuthenticated() 메소드를 통해 인증된 유저임을 확인한 뒤, text를 출력하고,

Authentication의 pricipal 이라는 변수를 사용하여 CLubAuthMemberDTO의 내용을 가져올 수 있다.

결과 화면

![https://blog.kakaocdn.net/dn/kJjVw/btrWYLUe2Yo/w2C8S1SvYFXHt6STbUg6zK/img.png](https://blog.kakaocdn.net/dn/kJjVw/btrWYLUe2Yo/w2C8S1SvYFXHt6STbUg6zK/img.png)

이번엔 컨트롤러에서 출력해보자.

컨트롤러에서 시큐리티에서 처리한 정보를 사용하는 것에는 2가지 방법이 있다.

1) SecurityContextHolder 객체 사용하기

2) 직접 파라미터와 어노테이션을 사용하기

이번엔 직접 @AuthenticationPrincipal 이라는 어노테이션을 통해 컨트롤러에서

시큐리티에서 처리한 사용자 정보를 사용해 보겠다.

아래는 "/sample/member" url을 처리하는 컨트롤러다.

![https://blog.kakaocdn.net/dn/ZnOTN/btrXaCAW973/aq4F6qS3ENK113bE4cSTr0/img.png](https://blog.kakaocdn.net/dn/ZnOTN/btrXaCAW973/aq4F6qS3ENK113bE4cSTr0/img.png)

결과

![https://blog.kakaocdn.net/dn/bVeW5C/btrW9lT2egU/SHGUy3AYZXV0NQFRRCnBnK/img.png](https://blog.kakaocdn.net/dn/bVeW5C/btrW9lT2egU/SHGUy3AYZXV0NQFRRCnBnK/img.png)

이렇게 스프링 시큐리티가 동작하기 위한 기본적인 구현을 마쳤다.

하지만 이렇게만 보면 어떻게? 가 알기 쉽지 않을 것이기에, 한가지 예시 상황에서 스프링 시큐리티의 동작을 매우 자세히 설명하며 전체적인 흐름을 요약하겠다.

## 요약

Q) 만약 유저가 로그인 하지 않고 "/sample/member/" url에 접근하면 어떻게 될까?

우선 .fornmLogin() 메소드를 통해, 로그인 하지 않은 유저에게 로그인 창을 노출하고, 유저가 로그인 창에 정보를 입력 시,

인증 매니저(Authentication Manager)가 Authentication Provider를 통해 처리가능한 타입인지 확인한다.

이후 UserDetiailsService를 통해 인증 절차를 시작하는데, 이때 @Service 어노테이션을 통해 빈에 등록되어

스프링 시큐리티가 UserDetailsService로 인식하는 구현 클래스 ClubUserDetialsService의 loadUserByName() 메소드를 통해 유저가 입력한 username에 대한 회원 정보가 존재하는지 확인하고, 인증/인가 정보가 담긴 UserDetail의 처리를 위한 DTO 클래스로 생성하여 반환한다.

UserDetail 타입으로 찾은 회원 정보를 유저가 입력한 회원 정보와 비교하고, 만약 password가 다르다면 Bad Cridential( 잘못된 자격 증명) 결과로 반환하고, 올바르다면 해당 유저에게 적절한 권한이 있는지 확인하는 인가(Authentication)의 과정을 수행한다.

앞서 구현한  SecurityConfig의 filter를 참고해보자.

![https://blog.kakaocdn.net/dn/dhOYxo/btrWS2PdXGi/KoIYKx2kmzJBRVpVzDNKzk/img.png](https://blog.kakaocdn.net/dn/dhOYxo/btrWS2PdXGi/KoIYKx2kmzJBRVpVzDNKzk/img.png)

이 "인가" 의 과정을 수행하는 것이 위 SecurityConfig의

auth.antMatchers("/sample/member/").hasRole("USER") 이 부분이다.

이는 "/sample/member/" 에 대한 접근 권한은 "USER" 라는 Role을 가진 사람만 가지고 있다는 의미로써, 만약 유저가 올바르게 아이디와 비밀번호를 입력하여 인증에 성공하고 로그인에 성공하더라도, 해당 권한이 없으면 진입하지 못한다는 "인가" 에 대한 내용을 처리한 것이다.