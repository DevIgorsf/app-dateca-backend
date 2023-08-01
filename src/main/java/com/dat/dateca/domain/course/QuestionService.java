package com.dat.dateca.domain.course;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuestionService {

    @Autowired
    private QuestionMultipleChoiceRepository questionMultipleChoiceRepository;

    public Object createQuestion(QuestionForm questionForm) {
        QuestionMultipleChoice question = new QuestionMultipleChoice(questionForm);
        questionMultipleChoiceRepository.save(question);
        return question;
    }
}
