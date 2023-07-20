package com.dat.dateca.domain.professor;

import com.dat.dateca.domain.user.RoleEnum;
import com.dat.dateca.domain.user.User;
import com.dat.dateca.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProfessorService {

    @Autowired
    ProfessorRepository professorRepository;

    @Autowired
    UserRepository userRepository;

    public ProfessorCreateDTO createProfessor(ProfessorCreate professorCreate) {
        var professor = new Professor(professorCreate);
        professorRepository.save(professor);

        User usuario = new User();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        usuario.setLogin(professorCreate.registrationNumber().toString());
        usuario.setPassword(encoder.encode("123456"));
        usuario.setRoles(RoleEnum.PROFESSOR);
        userRepository.save(usuario);

        return new ProfessorCreateDTO(professor);
    }

    public List<Professor> getAll() {
        List<Professor> professorList = professorRepository.findAll();
        return professorList;
    }

    public String deleteProfessor(Long id) {
        professorRepository.deleteById(id);

        return "Excluido com sucesso";
    }
}
