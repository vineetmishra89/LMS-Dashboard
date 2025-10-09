merge into courses (id, title, category, instructor_name, duration_minutes, created_at, updated_at)
key(id)
values ('11111111-1111-1111-1111-111111111111','Intro to Cloud','Cloud','Alice Johnson',120, now(), now());
values ('11111111-1111-1111-1111-111111111111','Intro to Java','Java','Vineet Mishra',90, now(), now());

merge into course_topics (course_id, topic)
key(course_id, topic)
values ('11111111-1111-1111-1111-111111111111','AWS');
values ('11111111-1111-1111-1111-111111111111','Multithreading');
values ('11111111-1111-1111-1111-111111111111','Collection');

merge into learning_hours (user_id, category, total_hours, month_hours)
key(user_id, category)
values ('22222222-2222-2222-2222-222222222222', 'Cloud', 15.5, 8.0);

merge into enrollments (id, user_id, course_id, progress_percent, status, last_accessed_at)
key(id)
values ('33333333-3333-3333-3333-333333333333', '22222222-2222-2222-2222-222222222222', '11111111-1111-1111-1111-111111111111', 75, 'in_progress', now());
