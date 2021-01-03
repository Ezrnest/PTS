use pts;
drop table if exists `user_device`;
drop table if exists `user`;
create table `user`
(
    uid      bigint primary key auto_increment,
    username varchar(50),
    password char(70),
    unique index (username)
);
# alter table `user` add column

create table `role`
(
    uid  bigint,
    role char(10),
    unique (uid, role),
    foreign key (uid) references `user` (uid)
);


create table `user_device`
(
    tid  bigint primary key auto_increment,
    uid  bigint,
    imei char(50),
    unique index (uid, imei),
    foreign key (uid) references `user` (uid)
);

insert into `user`(username, password) value
    ('test', '$2a$10$ysAY4uC0XNWPfx5tg88GbOKP51siYwP9c4diflgRGjSGtTLhGj/Gy');
insert ignore into `user_device`(uid, imei) value
    (1, 'imei0');
# insert into
# drop table `report_image`;
create table `report_image`
(
    iid    bigint primary key auto_increment,
    uid    bigint,
    `date` date,
    type   smallint,
    image  mediumblob,
    unique index (uid, `date`, type)
);
# drop table `report`;
create table `report`
(
    hid    bigint primary key auto_increment,
    uid    bigint,
    `date` date,
    name varchar(30),
    type varchar(30),
    `content` mediumtext,
    unique index (uid, `date`, name)
);
# delete from report_html where hid > 0;

