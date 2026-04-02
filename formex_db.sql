-- ======================================================
-- 1. BASE DE DATOS
-- ======================================================
CREATE DATABASE IF NOT EXISTS formex_db;
USE formex_db;

-- ======================================================
-- 2. ROLES
-- ======================================================
CREATE TABLE roles (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       name VARCHAR(50) NOT NULL,
                       UNIQUE KEY uk_roles_name (name)
);

INSERT INTO roles (name) VALUES
                             ('ROLE_ADMIN'),
                             ('ROLE_STUDENT'),
                             ('ROLE_INSTRUCTOR')
    ON DUPLICATE KEY UPDATE name = name;

-- ======================================================
-- 3. USERS
-- ======================================================
CREATE TABLE users (
                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                       full_name VARCHAR(100) NOT NULL,
                       email VARCHAR(100),
                       password VARCHAR(255),
                       phone VARCHAR(20),
                       avatar_url VARCHAR(255),

                       auth0_id VARCHAR(255) UNIQUE,

                       referral_code VARCHAR(255) NOT NULL UNIQUE,
                       referred_by_id BIGINT NULL,

                       points INT NOT NULL DEFAULT 0,
                       teaching_hours INT NOT NULL DEFAULT 0,

                       enabled BIT(1) DEFAULT 1,
                       created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                       updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                       CONSTRAINT fk_referred_by
                           FOREIGN KEY (referred_by_id) REFERENCES users(id)
);

-- ======================================================
-- 4. USER ROLES
-- ======================================================
CREATE TABLE user_roles (
                            user_id BIGINT NOT NULL,
                            role_id BIGINT NOT NULL,
                            PRIMARY KEY (user_id, role_id),
                            CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id)
                                REFERENCES users(id) ON DELETE CASCADE,
                            CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id)
                                REFERENCES roles(id) ON DELETE CASCADE
);

-- ======================================================
-- 5. PASSWORD RESET TOKENS
-- ======================================================
CREATE TABLE password_reset_tokens (
                                       id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                       token VARCHAR(255) NOT NULL,
                                       user_id BIGINT NOT NULL,
                                       expiry_date DATETIME NOT NULL,
                                       CONSTRAINT fk_token_user FOREIGN KEY (user_id)
                                           REFERENCES users(id) ON DELETE CASCADE
);

-- ======================================================
-- 6. CATEGORIES
-- ======================================================
CREATE TABLE categories (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            name VARCHAR(100) NOT NULL UNIQUE,
                            description TEXT
);

-- ======================================================
-- 7. COURSES
-- ======================================================
CREATE TABLE courses (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         title VARCHAR(200) NOT NULL,
                         description TEXT,
                         price DECIMAL(10,2) NOT NULL,
                         level VARCHAR(20),
                         image_url VARCHAR(255),

                         stripe_price_id VARCHAR(255),

                         instructor_id BIGINT,
                         category_id BIGINT,

                         created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                         CONSTRAINT fk_course_instructor FOREIGN KEY (instructor_id)
                             REFERENCES users(id),
                         CONSTRAINT fk_course_category FOREIGN KEY (category_id)
                             REFERENCES categories(id)
);

-- ======================================================
-- 8. COURSE SESSIONS
-- ======================================================
CREATE TABLE course_sessions (
                                 id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                 course_id BIGINT NOT NULL,
                                 title VARCHAR(150) NOT NULL,
                                 start_time DATETIME NOT NULL,
                                 duration_minutes INT DEFAULT 60,
                                 meeting_link VARCHAR(255),
                                 is_completed BIT(1) DEFAULT 0,
                                 enabled BIT(1) DEFAULT 1,
                                 CONSTRAINT fk_session_course FOREIGN KEY (course_id)
                                     REFERENCES courses(id) ON DELETE CASCADE
);

-- ======================================================
-- 9. ENROLLMENTS (HISTORIAL)
-- ======================================================
CREATE TABLE enrollments (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             user_id BIGINT NOT NULL,
                             course_id BIGINT NOT NULL,
                             enrolled_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                             status VARCHAR(20) DEFAULT 'ACTIVE',
                             CONSTRAINT fk_enroll_user FOREIGN KEY (user_id)
                                 REFERENCES users(id) ON DELETE CASCADE,
                             CONSTRAINT fk_enroll_course FOREIGN KEY (course_id)
                                 REFERENCES courses(id) ON DELETE CASCADE
);

-- ======================================================
-- 10. USER COURSES (PAGOS + CONTROL)
-- ======================================================
CREATE TABLE user_courses (
                              user_id BIGINT NOT NULL,
                              course_id BIGINT NOT NULL,

                              enrolled_at DATETIME DEFAULT CURRENT_TIMESTAMP,

                              payment_status VARCHAR(20) DEFAULT 'PENDING',
                              months_paid INT NOT NULL DEFAULT 0,
                              total_months INT NOT NULL DEFAULT 1,

                              referral_code_used VARCHAR(50),
                              referral_rewarded BOOLEAN NOT NULL DEFAULT FALSE,

                              PRIMARY KEY (user_id, course_id),

                              CONSTRAINT fk_user_courses_user FOREIGN KEY (user_id)
                                  REFERENCES users(id) ON DELETE CASCADE,
                              CONSTRAINT fk_user_courses_course FOREIGN KEY (course_id)
                                  REFERENCES courses(id) ON DELETE CASCADE
);

-- ======================================================
-- 11. MATERIALS
-- ======================================================
CREATE TABLE materials (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           course_id BIGINT NOT NULL,
                           title VARCHAR(150) NOT NULL,
                           description TEXT,
                           file_url VARCHAR(255),
                           created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                           CONSTRAINT fk_material_course FOREIGN KEY (course_id)
                               REFERENCES courses(id) ON DELETE CASCADE
);

-- ======================================================
-- 12. RESOURCES
-- ======================================================
CREATE TABLE resources (
                           id BIGINT AUTO_INCREMENT PRIMARY KEY,
                           course_id BIGINT NOT NULL,
                           name VARCHAR(150) NOT NULL,
                           url VARCHAR(255) NOT NULL,
                           type VARCHAR(50),
                           CONSTRAINT fk_resource_course FOREIGN KEY (course_id)
                               REFERENCES courses(id) ON DELETE CASCADE
);

-- ======================================================
-- 13. FORUM MESSAGES (MODELO FINAL)
-- ======================================================
CREATE TABLE forum_messages (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                course_id BIGINT NOT NULL,
                                resource_id BIGINT NULL,
                                user_id BIGINT NOT NULL,
                                content TEXT NOT NULL,
                                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                CONSTRAINT fk_forum_course FOREIGN KEY (course_id)
                                    REFERENCES courses(id) ON DELETE CASCADE,
                                CONSTRAINT fk_forum_user FOREIGN KEY (user_id)
                                    REFERENCES users(id) ON DELETE CASCADE
);

-- ======================================================
-- 14. ATTENDANCE RECORDS
-- ======================================================
CREATE TABLE attendance_records (
                                    id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                    session_id BIGINT NOT NULL,
                                    student_id BIGINT NOT NULL,
                                    attended BOOLEAN DEFAULT TRUE,
                                    marked_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                    UNIQUE KEY uk_session_student (session_id, student_id),

                                    CONSTRAINT fk_att_session FOREIGN KEY (session_id)
                                        REFERENCES course_sessions(id) ON DELETE CASCADE,
                                    CONSTRAINT fk_att_student FOREIGN KEY (student_id)
                                        REFERENCES users(id) ON DELETE CASCADE
);

-- ======================================================
-- 15. INSTRUCTOR RATINGS
-- ======================================================
CREATE TABLE instructor_rating (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   instructor_id BIGINT NOT NULL,
                                   student_id BIGINT NOT NULL,
                                   course_id BIGINT NOT NULL,

                                   rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
                                   comment TEXT,

                                   month INT NOT NULL,
                                   year INT NOT NULL,

                                   created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                                   CONSTRAINT uq_instructor_rating
                                       UNIQUE (instructor_id, student_id, month, year),

                                   CONSTRAINT fk_rating_instructor FOREIGN KEY (instructor_id)
                                       REFERENCES users(id),
                                   CONSTRAINT fk_rating_student FOREIGN KEY (student_id)
                                       REFERENCES users(id),
                                   CONSTRAINT fk_rating_course FOREIGN KEY (course_id)
                                       REFERENCES courses(id)
);

-- ======================================================
-- 16. EVALUATIONS
-- ======================================================
CREATE TABLE evaluations (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             course_id BIGINT NOT NULL,
                             title VARCHAR(150) NOT NULL,
                             description TEXT,
                             file_url VARCHAR(255) NOT NULL,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_evaluation_course FOREIGN KEY (course_id)
                                 REFERENCES courses(id) ON DELETE CASCADE
);

-- ======================================================
-- 17. SUBMISSIONS
-- ======================================================
CREATE TABLE submissions (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             comment TEXT,
                             file_url VARCHAR(255) NOT NULL,
                             student_id BIGINT NOT NULL,
                             evaluation_id BIGINT NOT NULL,
                             grade DOUBLE,
                             rewarded BOOLEAN NOT NULL DEFAULT FALSE,
                             created_at DATETIME NOT NULL,

                             UNIQUE (student_id, evaluation_id),

                             CONSTRAINT fk_submission_student FOREIGN KEY (student_id)
                                 REFERENCES users(id) ON DELETE CASCADE,
                             CONSTRAINT fk_submission_evaluation FOREIGN KEY (evaluation_id)
                                 REFERENCES evaluations(id) ON DELETE CASCADE
);

-- ======================================================
-- 18. VERIFICACIÓN
-- ======================================================
SHOW TABLES;
