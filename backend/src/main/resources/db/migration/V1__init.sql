create table if not exists courses (
  id uuid primary key,
  title varchar(255) not null,
  category varchar(100),
  instructor_name varchar(255),
  duration_minutes integer,
  thumbnail varchar(500),
  created_at timestamptz,
  updated_at timestamptz
);

create table if not exists course_topics (
  course_id uuid references courses(id) on delete cascade,
  topic varchar(100) not null
);

create table if not exists enrollments (
  id uuid primary key,
  user_id uuid not null,
  course_id uuid not null references courses(id),
  progress_percent integer not null default 0,
  last_accessed_at timestamptz,
  status varchar(50) not null
);

create table if not exists certificates (
  id uuid primary key,
  user_id uuid not null,
  course_id uuid not null references courses(id),
  title varchar(255),
  issued_at timestamptz,
  url varchar(500)
);

create table if not exists learning_hours (
  id bigserial primary key,
  user_id varchar(100) not null,
  category varchar(100) not null,
  total_hours numeric not null default 0,
  month_hours numeric not null default 0
);

create index if not exists idx_enrollments_user on enrollments(user_id);
create index if not exists idx_enrollments_course on enrollments(course_id);
