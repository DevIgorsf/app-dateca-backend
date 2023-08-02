package com.dat.dateca.domain.course;

import com.dat.dateca.domain.professor.Professor;
import com.dat.dateca.domain.professor.ProfessorDTO;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CourseService {
    @Autowired
    private CourseRepository courseRepository;

    public CourseDTO createCourse(CourseDTO courseDTO) {
        Course course = new Course(courseDTO);
        Course courseSaved = courseRepository.save(course);

        return new CourseDTO(courseSaved);
    }

    public List<CourseDTO> getAllCourse() {
        List<CourseDTO> courseList = courseRepository.findAll()
                .stream().map(CourseDTO::new).toList();

        if(courseList.isEmpty()) {
            throw new EntityNotFoundException("Não há professores cadastrados");
        }

        return courseList;
    }

    public String deleteCourse(Long id) {
        Course course = courseRepository.getReferenceById(id);
        courseRepository.delete(course);

        return "Excluido com sucesso";
    }

    public CourseDTO getCourse(Long id) {
        var course = courseRepository.findById(id);
        if(course.isEmpty()) {
            throw new EntityNotFoundException("Matéria não encontrada");
        }
        return new CourseDTO(course.get());
    }

    public CourseDTO updateCourse(Long id, CourseUpdate courseUpdate) {
        var courseOptional = courseRepository.findById(id);
        if( courseOptional.isEmpty()) {
            throw new EntityNotFoundException("Professor não encontrado");
        }
        Course course =  courseOptional.get();
        course.updateCourse(courseUpdate);
        courseRepository.save(course);
        return new CourseDTO(course);
    }

    public List<CourseDTO> getCourseByProfessor(Long id) {
        List<CourseDTO> courseList = courseRepository.findByProfessorListId(id)
                .stream().map(CourseDTO::new).toList();
        if(courseList.isEmpty()) {
            throw new EntityNotFoundException("Não há professores cadastrados");
        }

        return courseList;

    }

    public Long getCourseData() {
        return courseRepository.count();
    }
}
