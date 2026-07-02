USE erp_db;

-- 1. Insert Global Settings
INSERT INTO settings (`key`, `value`)
VALUES ('maintenance_on', 'false')
ON DUPLICATE KEY UPDATE
    `value` = 'false';

-- 2. Insert Instructor Profile
INSERT INTO instructors (user_id, department)
VALUES (1002, 'Computer Science')
ON DUPLICATE KEY UPDATE
    department = 'Computer Science';

-- 3. Insert Student Profiles
INSERT INTO students (user_id, roll_no, program, year)
VALUES
(1003, 'S2023001', 'B.Tech CS', 1)
ON DUPLICATE KEY UPDATE
    roll_no = 'S2023001',
    program = 'B.Tech CS',
    year = 1;

INSERT INTO students (user_id, roll_no, program, year)
VALUES
(1004, 'S2023002', 'B.Tech IT', 2)
ON DUPLICATE KEY UPDATE
    roll_no = 'S2023002',
    program = 'B.Tech IT',
    year = 2;

-- 4. Insert Course
INSERT INTO courses (course_id, code, title, credits)
VALUES
(1, 'CS101', 'Intro to Programming', 4)
ON DUPLICATE KEY UPDATE
    code = 'CS101',
    title = 'Intro to Programming',
    credits = 4;

-- 5. Insert Section
INSERT INTO sections (section_id, course_id, instructor_id, day_time, room, capacity, semester, year)
VALUES
(10, 1, 1002, 'Mon/Wed 10:00-11:30', 'C-101', 50, 'Fall', 2025)
ON DUPLICATE KEY UPDATE
    course_id = 1,
    instructor_id = 1002,
    day_time = 'Mon/Wed 10:00-11:30',
    room = 'C-101',
    capacity = 50,
    semester = 'Fall',
    year = 2025;

-- 6. Insert Enrollment
INSERT INTO enrollments (enrollment_id, student_id, section_id, status, registered_at)
VALUES
(100, 1003, 10, 'REGISTERED', CURRENT_TIMESTAMP())
ON DUPLICATE KEY UPDATE
    student_id = 1003,
    section_id = 10,
    status = 'REGISTERED',
    registered_at = CURRENT_TIMESTAMP();
