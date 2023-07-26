package com.dat.dateca.controller;

import com.dat.dateca.domain.course.CourseDTO;
import com.dat.dateca.domain.course.CourseService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/materia")
public class CourseController {

    @Autowired
    private CourseService courseService;

    @PostMapping
    public ResponseEntity<?> createCourse (@RequestBody CourseDTO courseDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(courseService.createCourse(courseDTO));
    }

    @GetMapping
    public ResponseEntity<List<CourseDTO>> getAllCourse() {
        return ResponseEntity.ok().body(courseService.getAllCourse());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> getAllCourse(@RequestParam Long id) {
        return ResponseEntity.ok().body(courseService.deleteCourse(id));
    }

}
