package com.dat.dateca.domain.question;

import com.dat.dateca.domain.course.Course;
import com.dat.dateca.domain.course.CourseRepository;
import com.dat.dateca.domain.professor.Professor;
import com.dat.dateca.domain.professor.ProfessorRepository;
import com.dat.dateca.domain.student.Student;
import com.dat.dateca.domain.student.StudentRepository;
import com.dat.dateca.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class QuestionService {

    @Autowired
    private QuestionMultipleChoiceRepository questionMultipleChoiceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfessorRepository professorRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private StudentRepository studentRepository;

    public QuestionMultipleChoice createQuestion(QuestionForm questionForm, String registrationNumber) {
        Professor professor = professorRepository.findByRegistrationNumber(registrationNumber);
        Course course = courseRepository.findById(questionForm.course()).get();
        QuestionMultipleChoice question = new QuestionMultipleChoice(questionForm, professor, course);
        questionMultipleChoiceRepository.save(question);
        return question;
    }

    @GetMapping
    public List<QuestionMultipleAllDTO> getAllQuestion() {
        List<QuestionMultipleAllDTO> questionList = questionMultipleChoiceRepository.findAll()
                .stream().map(QuestionMultipleAllDTO::new).toList();

        if(questionList.isEmpty()) {
            throw new EntityNotFoundException("Não há questões cadastradas");
        }

        return questionList;
    }

    public QuestionMultipleDTO getQuestion(Long id) {
        var question = questionMultipleChoiceRepository.findById(id);

        if(question.isEmpty()) {
            throw new EntityNotFoundException("Questão não encontrada");
        }
        return new QuestionMultipleDTO(question.get());
    }

    public QuestionMultipleDTO updateQuestion(Long id, QuestionForm questionUpdate) {
        var questionOptional = questionMultipleChoiceRepository.findById(id);
        if( questionOptional.isEmpty()) {
            throw new EntityNotFoundException("Professor não encontrado");
        }
        QuestionMultipleChoice questionMultipleChoice =  questionOptional.get();
        Course course = courseRepository.findById(questionUpdate.course()).get();
        questionMultipleChoice.updateQuestionMultipleChoice(questionUpdate, course);
        System.out.println(questionMultipleChoice);
        questionMultipleChoiceRepository.save(questionMultipleChoice);

        return new QuestionMultipleDTO(questionMultipleChoice);
    }

    public String deleteQuestion(Long id) {
        QuestionMultipleChoice questionMultipleChoice = questionMultipleChoiceRepository.getReferenceById(id);
        questionMultipleChoiceRepository.delete(questionMultipleChoice);

        return "Excluido com sucesso";
    }

    public QuestionMultipleChoiceRandDTO getQuestionAleatoria() {
        List<Long> multipleChoiceIds = questionMultipleChoiceRepository.findIdsOfMultipleChoiceQuestions();

        if (multipleChoiceIds.isEmpty()) {
            throw new EntityNotFoundException("Nenhuma pergunta do tipo MULTIPLE_CHOICE encontrada");
        }

        Random random = new Random();

        // Obter um índice aleatório da lista
        long indiceSorteado = random.nextLong(multipleChoiceIds.size());

        var question = questionMultipleChoiceRepository.findById(multipleChoiceIds.get((int) indiceSorteado)).get();

        return new QuestionMultipleChoiceRandDTO(question);
    }

    public QuestionAnswerDTO answerQuestion(Long id, String answer, String registrationNumber) {
        var optionalQuestion = questionMultipleChoiceRepository.findById(id);
        Student stundent = studentRepository.findByRegistrationNumber(registrationNumber);

        if (optionalQuestion.isEmpty()) {
            throw new EntityNotFoundException("Nenhuma pergunta do tipo MULTIPLE_CHOICE encontrada");
        }

        QuestionMultipleChoice question = optionalQuestion.get();


        char correctAnswerChar = answer.charAt(0);

        int result =  Character.compare(question.getCorrectAnswer(), correctAnswerChar);
        if(result == 0) {
            stundent.addPontuação(question.getPointsEnum().getKey());
            studentRepository.save(stundent);
            return new QuestionAnswerDTO(question.getCorrectAnswer(), correctAnswerChar, true);
        }

        return new QuestionAnswerDTO(question.getCorrectAnswer(), correctAnswerChar, false);
    }
}
