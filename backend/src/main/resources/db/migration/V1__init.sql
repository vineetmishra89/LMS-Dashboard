CREATE TABLE IF NOT EXISTS onedrive_db.training_details
(
    training_detail_id character varying(255) COLLATE pg_catalog."default" NOT NULL,
    training_id character varying(255) COLLATE pg_catalog."default",
    category character varying(255) COLLATE pg_catalog."default",
    module_duration integer,
    module_name character varying(4000) COLLATE pg_catalog."default",
    module_path character varying(255) COLLATE pg_catalog."default",
    module_topic character varying(4000) COLLATE pg_catalog."default",
    training_topic character varying(255) COLLATE pg_catalog."default",
    instructor_name character varying(255) COLLATE pg_catalog."default",
    thumbnail_url text COLLATE pg_catalog."default",
    "training topic" character varying(255) COLLATE pg_catalog."default",
    created_at timestamp(6) without time zone,
    training_name character varying(255) COLLATE pg_catalog."default",
    updated_at timestamp(6) without time zone,
    CONSTRAINT training_details_pkey PRIMARY KEY (training_detail_id)
)


DROP TABLE IF EXISTS onedrive_db.enrollments;
	CREATE TABLE IF NOT EXISTS onedrive_db.enrollments (
    id BIGSERIAL PRIMARY KEY,  -- auto-generated number
    user_id VARCHAR(255) NOT NULL,
    course_id VARCHAR(4000) NOT NULL REFERENCES onedrive_db.training_details(training_detail_id),
    progress_percent INTEGER NOT NULL DEFAULT 0,
    last_accessed_at TIMESTAMP,
    status VARCHAR(50) NOT NULL
);

create table if not exists onedrive_db.certificates (
  id varchar(255) primary key,
  user_id varchar(255) not null,
  course_id VARCHAR(4000) NOT NULL REFERENCES onedrive_db.training_details(training_detail_id),
  title varchar(255),
  issued_at timestamp,
  url varchar(500)
);

create table if not exists learning_hours (
  id bigint auto_increment primary key,
  user_id varchar(100) not null,
  category varchar(100) not null,
  total_hours numeric not null default 0,
  month_hours numeric not null default 0
);

create index if not exists idx_enrollments_user on enrollments(user_id);
create index if not exists idx_enrollments_course on enrollments(course_id);
