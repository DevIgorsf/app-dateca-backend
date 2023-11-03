package com.dat.dateca.domain.question;

import com.dat.dateca.domain.professor.Professor;
import com.dat.dateca.domain.professor.ProfessorRepository;
import com.dat.dateca.domain.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
