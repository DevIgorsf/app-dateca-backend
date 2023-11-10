package com.dat.dateca.domain.question;

import com.dat.dateca.domain.professor.Professor;
import com.dat.dateca.domain.professor.ProfessorRepository;
import com.dat.dateca.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Service
public class QuestionService {

    @Autowired
    private QuestionMultipleChoiceRepository questionMultipleChoiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    public Object createQuestion(QuestionForm questionForm, String registrationNumber) {
        Professor professor = professorRepository.findByRegistrationNumber(registrationNumber);
        QuestionMultipleChoice question = new QuestionMultipleChoice(questionForm, professor);
        questionMultipleChoiceRepository.save(question);
        return question;
    }

    @GetMapping
    public List<QuestionMultipleDTO> getAllQuestion() {
        List<QuestionMultipleDTO> questionList = questionMultipleChoiceRepository.findAll()
                .stream().map(QuestionMultipleDTO::new).toList();

        if(questionList.isEmpty()) {
            throw new EntityNotFoundException("Não há professores cadastrados");
        }

        return questionList;
    }
}
