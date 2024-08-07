package com.dat.dateca.domain.student;

import com.dat.dateca.domain.email.EmailService;
import com.dat.dateca.domain.user.RoleEnum;
import com.dat.dateca.domain.user.User;
import com.dat.dateca.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class StudentService {

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public StudentDTO cadastroAluno(StudentCadastro studentCadastro) {
        var student = new Student(studentCadastro);
        studentRepository.save(student);

        User usuario = new User();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        usuario.setLogin(studentCadastro.registrationNumber().toString());
        usuario.setPassword(encoder.encode("123456"));
        usuario.setRoles(RoleEnum.STUDENT);
        userRepository.save(usuario);

        emailService.sendSimpleMessage(
                studentCadastro.email(), "Novo cadastro dateca", "Sua senha é 123456");

        return new StudentDTO(student);
    }

    public List<StudentWithIndex> rankingStudent(String registrationNumber) {
        List<Student> allStudents = studentRepository.findAllByOrderByPointsDesc();
        Student student = studentRepository.findByRegistrationNumber(registrationNumber);

        if (student == null) {
            return null;
        }

        int studentIndex = allStudents.indexOf(student);

        if (studentIndex == -1) {
            return null;
        }

        int startIndex = Math.max(0, studentIndex - 2);
        int endIndex = Math.min(allStudents.size() - 1, studentIndex + 2);

        return IntStream.rangeClosed(startIndex, endIndex)
                .mapToObj(index -> new StudentWithIndex(allStudents.get(index), index))
                .collect(Collectors.toList());
    }

    public List<StudentWithIndex> rankingAll() {
        List<Student> allStudents = studentRepository.findAllByOrderByPointsDesc();

        List<StudentWithIndex> responseList = allStudents.stream()
                .map(student -> new StudentWithIndex(student, allStudents.indexOf(student) + 1))
                .collect(Collectors.toList());

        return responseList;
    }


    public Student getPerfil(String registrationNumber) {
        Student student = studentRepository.findByRegistrationNumber(registrationNumber);

        return student;
    }

    public StudentDTO updatePerfil(String registrationNumber,StudentCadastro studentCadastro) {
        Student student = studentRepository.findByRegistrationNumber(registrationNumber);

        student.update(studentCadastro);
        studentRepository.save(student);

        return new StudentDTO(student);
    }

    public Long getStudentData() {
        return studentRepository.count();
    }
}
