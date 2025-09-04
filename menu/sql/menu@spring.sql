create table menu (
    id number,
    restaurant varchar2(512) not null,
    name varchar2(256) not null,
    price number,
    type varchar2(10) not null, --  한식 kr, 중식 ch, 일식 jp
    taste varchar2(10) not null, -- 순한맛 mild, 매운맛 hot
    constraint pk_menu primary key(id),
    constraint uq_menu unique (restaurant, name, taste) -- 두리순대국 순대국 mild, 두리순대국 순대국 hot
);

create sequence seq_menu_id;


insert into menu (id,restaurant,name,price,type,taste) values (seq_menu_id.nextval,'두리순대국','순대국',7000,'kr','mild');
insert into menu (id,restaurant,name,price,type,taste) values (seq_menu_id.nextval,'두리순대국','순대국',7000,'kr','hot');
insert into menu (id,restaurant,name,price,type,taste) values (seq_menu_id.nextval,'장터','뚝배기 김치찌게',7000,'kr','hot');
insert into menu (id,restaurant,name,price,type,taste) values (seq_menu_id.nextval,'만리향','간짜장',5000,'ch','mild');
insert into menu (id,restaurant,name,price,type,taste) values (seq_menu_id.nextval,'만리향','짬뽕',6000,'ch','hot');
insert into menu (id,restaurant,name,price,type,taste) values (seq_menu_id.nextval,'짬뽕지존','짬뽕',9000,'ch','mild');
insert into menu (id,restaurant,name,price,type,taste) values (seq_menu_id.nextval,'김남완초밥집','완초밥',13000,'jp','mild');
insert into menu (id,restaurant,name,price,type,taste) values (seq_menu_id.nextval,'김남완초밥집','런치초밥',10000,'jp','mild');
insert into menu (id,restaurant,name,price,type,taste) values (seq_menu_id.nextval,'김남완초밥집','참치도로초밥',33000,'jp','mild');
insert into menu (id,restaurant,name,price,type,taste) values (seq_menu_id.nextval,'진가와','자루소면',8000,'jp','mild');
insert into menu (id,restaurant,name,price,type,taste) values (seq_menu_id.nextval,'진가와','자루소바',9000,'jp','mild');
insert into menu (id,restaurant,name,price,type,taste) values (seq_menu_id.nextval,'백운봉','막국수',9000,'kr','hot');
insert into menu (id,restaurant,name,price,type,taste) values (seq_menu_id.nextval,'대우부대찌게','부대지게',10000,'kr','hot');
insert into menu (id,restaurant,name,price,type,taste) values (seq_menu_id.nextval,'봉된장','열무비빔밥+우렁된장',7000,'kr','hot');
insert into menu (id,restaurant,name,price,type,taste) values (seq_menu_id.nextval,'대우부대찌게','부대찌게',10000,'kr','hot');
insert into menu (id,restaurant,name,price,type,taste) values (seq_menu_id.nextval,'산들애','딸기',500,'kr','hot');
insert into menu (id,restaurant,name,price,type,taste) values (seq_menu_id.nextval,'대우부대찌게','청국장',13000,'kr','mild');
insert into menu (id,restaurant,name,price,type,taste) values (seq_menu_id.nextval,'스타동','사케동',8400,'jp','mild');
insert into menu (id,restaurant,name,price,type,taste) values (seq_menu_id.nextval,'진씨화로','돌솥비빔밥',7000,'kr','mild');

commit;

select
    *
from 
    menu;

-- #1. JWT토큰을 활용한 로그인
-- 유저 공통정보
CREATE TABLE USERS (
  ID     NUMBER PRIMARY KEY,
  EMAIL  VARCHAR2(255) , 
  NAME   VARCHAR2(100) ,           
  PROFILE VARCHAR2(255) DEFAULT '/profile/sample.jpg',
  CREATED_AT DATE DEFAULT SYSDATE  
);
CREATE SEQUENCE SQE_USERS_ID;
CREATE UNIQUE INDEX UX_USERS_EMAIL_CI ON USERS (UPPER(EMAIL));

-- 유저 비밀번호
CREATE TABLE USER_CREDENTIALS (
  USER_ID        NUMBER PRIMARY KEY
                 REFERENCES USERS(ID) ON DELETE CASCADE,
  PASSWORD  VARCHAR2(60) NOT NULL  
);

-- 유저별 권한 테이블(ADMIN, USER)
CREATE TABLE USER_AUTHORITY (
  USER_ID NUMBER NOT NULL REFERENCES USERS(ID) ON DELETE CASCADE,
  AUTHORITY  VARCHAR2(15) NOT NULL, 
  PRIMARY KEY (USER_ID, AUTHORITY)
);  

CREATE TABLE USER_IDENTITIES (
  ID                NUMBER PRIMARY KEY,
  USER_ID           NUMBER NOT NULL
                    REFERENCES USERS(ID) ON DELETE CASCADE,
  ACCESS_TOKEN      VARCHAR2(64),
  PROVIDER          VARCHAR2(20) NOT NULL CHECK (PROVIDER IN ('kakao','naver')),
  PROVIDER_USER_ID  VARCHAR2(150) NOT NULL,
  UNIQUE (PROVIDER, PROVIDER_USER_ID)
);
CREATE SEQUENCE SEQ_USER_IDENTITIES_ID;