package com.dat.dateca.domain.professor;

import com.dat.dateca.domain.email.EmailService;
import com.dat.dateca.domain.user.RoleEnum;
import com.dat.dateca.domain.user.User;
import com.dat.dateca.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class ProfessorService {

    @Autowired
    ProfessorRepository professorRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    public ProfessorDTO createProfessor(ProfessorCreate professorCreate) {
        var professor = new Professor(professorCreate);
        professorRepository.save(professor);

        User usuario = new User();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        usuario.setLogin(professorCreate.registrationNumber().toString());
        CharSequence randomCharSequence = generateRandomCharSequence(8);
        usuario.setPassword(encoder.encode(randomCharSequence));
        usuario.setRoles(RoleEnum.PROFESSOR);
        userRepository.save(usuario);

        emailService.sendSimpleMessage(
                professorCreate.email(), "Você foi cadastrado no dateca",
                "Faça seu acesso pelo link https://app-dateca-admin-front.vercel.app/login com o login "
                        + usuario.getLogin() + " e a senha " + randomCharSequence);

        return new ProfessorDTO(professor);
    }

    public static CharSequence generateRandomCharSequence(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(characters.length());
            sb.append(characters.charAt(index));
        }

        return sb;
    }

    public List<ProfessorDTO> getAllProfessors() {
        List<ProfessorDTO> professorList = professorRepository.findAll()
                .stream().map(ProfessorDTO::new).toList();

        if(professorList.isEmpty()) {
            throw new EntityNotFoundException("Não há professores cadastrados");
        }

        return professorList;
    }

    public String deleteProfessor(Long id) {
        Professor professor = professorRepository.getReferenceById(id);
        professorRepository.delete(professor);

        return "Excluido com sucesso";
    }

    public ProfessorDTO getProfessor(Long id) {
        var professor = professorRepository.findById(id);
        if(professor.isEmpty()) {
            throw new EntityNotFoundException("Professor não encontrado");
        }
        return new ProfessorDTO(professor.get());
    }

    public ProfessorDTO updateProfessor(Long id, ProfessorUpdate professorUpdate) {
        var professorOptional = professorRepository.findById(id);
        if( professorOptional.isEmpty()) {
            throw new EntityNotFoundException("Professor não encontrado");
        }
        Professor professor =  professorOptional.get();
        professor.updateProfessor(professorUpdate);
        professorRepository.save(professor);
        return new ProfessorDTO(professor);
    }

    public Long getProfessorData() {
        return professorRepository.count();
    }

    public ProfessorDTO getProfile(String registrationNumber) {
        var professorOptional = professorRepository.findByRegistrationNumber(registrationNumber);

        return new ProfessorDTO(professorOptional);
    }

    public void updatePassword(String registrationNumber, String newPassword) {
        User usuario = (User) userRepository.findByLogin(registrationNumber);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        usuario.setPassword(encoder.encode(newPassword));
        userRepository.save(usuario);
    }
}
