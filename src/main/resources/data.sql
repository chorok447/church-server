-- Seed notices (author_id will be null for seed data — DataInitializer creates admin separately)
INSERT INTO notices (title, date, content, view_count, pinned, deleted) VALUES
    ('신년 특별 새벽기도회', '2025-01-02', '신년 특별 새벽기도회가 시작됩니다. 많은 참여 부탁드립니다.', 15, false, false),
    ('부활절 연합예배 안내', '2025-03-31', '부활절 연합예배가 진행됩니다. 장소는 본당이며, 오전 11시에 시작합니다.', 42, true, false),
    ('5월 첫째주 주보', '2025-05-05', '이번 주 주보입니다. 새벽기도회와 수요예배 일정을 확인해 주세요.', 8, false, false);

INSERT INTO sermons (title, date, video_url, description, preacher) VALUES
    ('Dont look back in anger', '2025-04-20', 'https://www.youtube.com/watch?v=cmpRLQZkTb8', '테스트 1', 'Oasis'),
    ('메리골드', '2025-04-21', 'https://www.youtube.com/watch?v=H8EqIAJXl7s', '테스트 2', 'Aimyon'),
    ('너는 록을 듣지 않아', '2025-04-28', 'https://www.youtube.com/watch?v=cJnO-Y_YnFg', '테스트 3', 'Aimyon'),
    ('봄날', '2025-05-02', 'https://www.youtube.com/watch?v=mIJp7Ci14WQ', '테스트 4', 'Aimyon'),
    ('어차피 죽는다면','2025-05-05', 'https://www.youtube.com/watch?v=ggDbAJHSmlE', '테스트 5', 'Aimyon'),
    ('사랑을 전하고 싶다든가', '2025-05-07', 'https://www.youtube.com/watch?v=rlHTmo50zSI', '테스트 6', 'Aimyon'),
    ('떡잎', '2025-05-08', 'https://www.youtube.com/watch?v=PdDasn64wL4', '테스트 7', 'Aimyon');
