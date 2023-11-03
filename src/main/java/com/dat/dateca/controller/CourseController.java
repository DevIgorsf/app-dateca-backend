package com.dat.dateca.controller;

import com.dat.dateca.domain.course.CourseDTO;
import com.dat.dateca.domain.course.CourseService;
import com.dat.dateca.domain.course.CourseUpdate;
import com.dat.dateca.domain.user.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/materia")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping
    public ResponseEntity<CourseDTO> createCourse (@RequestBody CourseDTO courseDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(courseDTO));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CourseDTO> getCourse(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(courseService.getCourse(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CourseDTO> updateCourse(@PathVariable Long id, @RequestBody @Valid CourseUpdate courseUpdate) {
        return ResponseEntity.status(HttpStatus.OK).body(courseService.updateCourse(id, courseUpdate));
    }

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourse() {
        return ResponseEntity.ok().body(courseService.getAllCourse());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> getAllCourse(@RequestParam Long id) {
        return ResponseEntity.ok().body(courseService.deleteCourse(id));
    }

    @GetMapping("/professor")
    public ResponseEntity<List<CourseDTO>> getCourseByProfessor(HttpServletRequest request) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long id = ((User)principal).getId();
        return ResponseEntity.status(HttpStatus.OK).body(courseService.getCourseByProfessor(id));
    }

    @GetMapping("/dados")
    public ResponseEntity<Long> getCourseData() {
        return ResponseEntity.status(HttpStatus.OK).body(courseService.getCourseData());
    }

}
