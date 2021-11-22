package com.mycompany.myapp.service;

import com.mycompany.myapp.domain.Course;
import com.mycompany.myapp.domain.User;
import com.mycompany.myapp.domain.UserCourse;
import com.mycompany.myapp.repository.CourseRepository;
import com.mycompany.myapp.repository.UserCourseRepository;
import com.mycompany.myapp.repository.UserRepository;
import com.mycompany.myapp.service.dto.CourseDTO;
import com.mycompany.myapp.service.mapper.CourseMapper;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class CourseService {

    private UserRepository userRepository;
    private CourseRepository courseRepository;
    private UserCourseRepository userCourseRepository;
    private CourseMapper courseMapper;

    /**
     * 1. User exists?
     * 2. Course exists? check courseRepository
     * 3. UserCourse combination not exist (de-dupe)? register twice？check UserCourseRepository
     * 4. Save UserCourse to DB
     * @param username
     * @param courseName
     */
    public void enrollCourse(String username, String courseName) {
        UserCourse userCourse = getUserCourse(username, courseName); // 1. & 2.check if user/course exists
        // 3. UserCourse combination not exist (de-dupe)
        userCourseRepository
            .findOneByUserAndCourse(userCourse.getUser(), userCourse.getCourse())
            .ifPresent(existingUserCourse -> {
                throw new IllegalArgumentException(String.format("UserCourse:{} already exists!", existingUserCourse));
            });
        // 4. Save UserCourse to DB
        userCourseRepository.save(userCourse);
    }

    private UserCourse getUserCourse(String username, String courseName) {
        Optional<User> optionalUser = userRepository.findOneByLogin(username); //check if the user exists
        User user = optionalUser.orElseThrow(() -> new UsernameNotFoundException(String.format("No such user: {}", username)));

        Optional<Course> optionalCourse = courseRepository.findByCourseName(courseName); // check if the course exists
        Course course = optionalCourse.orElseThrow(() -> new IllegalArgumentException(String.format("No such course: {}", courseName)));
        return new UserCourse(user, course);
    }

    public List<CourseDTO> getCourses() {
        List<Course> courses = courseRepository.findAll();
        return courses.stream().map(course -> courseMapper.convert(course)).collect(Collectors.toList());
    }

    /**
     * 1. User exists?
     * 2. Find UserCourse by user
     * 3. Covert to List of CourseDTO
     * @param username
     * @return List<CourseDTO>
     */
    public List<CourseDTO> getSelectedCourses(String username) {
        Optional<User> optionalUser = userRepository.findOneByLogin(username); //1. check if the user exists
        User user = optionalUser.orElseThrow(() -> new UsernameNotFoundException(String.format("No such user: {}", username)));

        // 2. Find UserCourse by user
        List<UserCourse> userCourseList = userCourseRepository.findAllByUser(user);

        // 3. Covert to List of CourseDTO
        return userCourseList
            .stream()
            .map(userCourse -> userCourse.getCourse())
            .map(course -> courseMapper.convert(course))
            .collect(Collectors.toList());
    }

    /**
     * 1. User exists?
     * 2. Course exists?
     * 3. Drop UserCourse
     * @param username
     * @param courseName
     */
    public void dropCourse(String username, String courseName) {
        UserCourse userCourse = getUserCourse(username, courseName); // 1. & 2.check if user/course exists
        // 3 Drop UserCourse
        userCourseRepository.deleteByUserAndCourse(userCourse.getUser(), userCourse.getCourse());
    }
}
