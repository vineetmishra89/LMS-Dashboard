insert into courses (id, title, category, instructor_name, duration_minutes, created_at, updated_at)
values
  ('11111111-1111-1111-1111-111111111111','Intro to Cloud','Cloud','Alice Johnson',120, now(), now())
on conflict do nothing;

insert into course_topics(course_id, topic) values
  ('11111111-1111-1111-1111-111111111111','AWS')
on conflict do nothing;
