# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table app_users (
  username                      varchar(255) not null,
  password                      varchar(255) not null,
  token                         varchar(255),
  email                         varchar(255) not null,
  constraint uq_app_users_token unique (token),
  constraint uq_app_users_email unique (email),
  constraint pk_app_users primary key (username)
);

create table posts (
  post_id                       integer auto_increment not null,
  app_users_username            varchar(255),
  content                       TEXT,
  title                         TEXT,
  timestamp                     timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  reminder                      varchar(255),
  is_reminder_active            int default 0,
  is_archive                    int default 0,
  constraint pk_posts primary key (post_id)
);

alter table posts add constraint fk_posts_app_users_username foreign key (app_users_username) references app_users (username) on delete restrict on update restrict;
create index ix_posts_app_users_username on posts (app_users_username);


# --- !Downs

alter table posts drop foreign key fk_posts_app_users_username;
drop index ix_posts_app_users_username on posts;

drop table if exists app_users;

drop table if exists posts;

