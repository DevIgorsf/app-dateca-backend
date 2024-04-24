package com.dat.dateca.infra;

import com.dat.dateca.domain.course.Course;
import com.dat.dateca.domain.course.CourseDTO;
import com.dat.dateca.domain.course.CourseForm;
import com.dat.dateca.domain.course.CourseRepository;
import com.dat.dateca.domain.enade.Enade;
import com.dat.dateca.domain.professor.Professor;
import com.dat.dateca.domain.professor.ProfessorCreate;
import com.dat.dateca.domain.professor.ProfessorDTO;
import com.dat.dateca.domain.professor.ProfessorRepository;
import com.dat.dateca.domain.question.PointsEnum;
import com.dat.dateca.domain.user.RoleEnum;
import com.dat.dateca.domain.user.User;
import com.dat.dateca.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
 
@Component
public class DatabaseInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private CourseRepository courseRepository;
    @Override
    public void run(String... args) throws Exception {
        createUser("201810671", RoleEnum.ADMIN);

        createUser("202010782", RoleEnum.PROFESSOR);

        var professor = new Professor(new ProfessorCreate(202010782L,"Giovanna","37988237639","giovanna@gmail.com"));
        Professor professorsalved = professorRepository.save(professor);

        var course1 = new Course(new CourseForm(1L,"GAT102","Introdução à Engenharia de Controle e Automação",1, List.of(professorsalved)));
        var course2 = new Course(new CourseForm(2L,"GFI103","Conceitos de Física A",1, List.of(professorsalved)));

        courseRepository.save(course1);
        courseRepository.save(course2);
    }

    private void createUser(String registerNumber, RoleEnum tipo) {
        User usuario = new User();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        usuario.setLogin(String.valueOf(registerNumber));
        usuario.setPassword(encoder.encode("123456"));
        usuario.setRoles(tipo);
        userRepository.save(usuario);
    }
}
