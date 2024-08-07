package com.dat.dateca.domain.question;

import com.dat.dateca.domain.course.Course;
import com.dat.dateca.domain.course.CourseRepository;
import com.dat.dateca.domain.enade.EnadeAllDTO;
import com.dat.dateca.domain.enade.EnadeAnswerResult;
import com.dat.dateca.domain.enade.ImageEnade;
import com.dat.dateca.domain.professor.Professor;
import com.dat.dateca.domain.professor.ProfessorRepository;
import com.dat.dateca.domain.student.Student;
import com.dat.dateca.domain.student.StudentRepository;
import com.dat.dateca.domain.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

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

    @Autowired
    private ImageQuestionRepository imageQuestionRepository;

    @Autowired
    private QuestionAnswerResultRepository questionAnswerResultRepository;

    public QuestionMultipleDTO createQuestion(QuestionForm questionForm, String registrationNumber) {
        Professor professor = professorRepository.findByRegistrationNumber(registrationNumber);
        Course course = courseRepository.findById(questionForm.course()).get();
        QuestionMultipleChoice question = new QuestionMultipleChoice(questionForm, professor, course);
        questionMultipleChoiceRepository.save(question);
        return new QuestionMultipleDTO(question);
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

        int indiceSorteado = random.nextInt(multipleChoiceIds.size());

        var question = questionMultipleChoiceRepository.findById(multipleChoiceIds.get(indiceSorteado)).get();

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
        QuestionAnswerResult questionAnswerResult;
        if(result == 0) {
            questionAnswerResult = new QuestionAnswerResult(question, true);
            stundent.addPontuacao(question.getPointsEnum().getKey());
        }
        else {
            questionAnswerResult = new QuestionAnswerResult(question, false);
        }

        questionAnswerResultRepository.save(questionAnswerResult);
        studentRepository.save(stundent);

        return new QuestionAnswerDTO(question.getCorrectAnswer(), correctAnswerChar, questionAnswerResult.getCorrect());
    }

    public Long getQuestionData() {
        return questionMultipleChoiceRepository.count();
    }

    public QuestionMultipleChoice salvarImagens(
            String registrationNumber,
            MultipartFile[] files,
            String statement,
            String pointsEnum,
            Long course,
            Character correctAnswer,
            String alternativeA,
            String alternativeB,
            String alternativeC,
            String alternativeD,
            String alternativeE) throws IOException {

        Professor professor = professorRepository.findByRegistrationNumber(registrationNumber);
        Course courseSaved = courseRepository.findById(course).get();

        var questionMultipleChoice = new QuestionMultipleChoice(statement,
                PointsEnum.fromString(pointsEnum),
                courseSaved,
                professor,
                correctAnswer,
                alternativeA,
                alternativeB,
                alternativeC,
                alternativeD,
                alternativeE);

        questionMultipleChoiceRepository.save(questionMultipleChoice);

        List<ImageQuestion> imagensSalvas = new ArrayList<>();

        for (MultipartFile file : files) {
            ImageQuestion imagem = new ImageQuestion();
            imagem.setNome(file.getOriginalFilename());
            imagem.setImagem(file.getBytes());
            imagem.setQuestion(questionMultipleChoice);

            imagensSalvas.add(imagem);
        }

        imageQuestionRepository.saveAll(imagensSalvas);

        return questionMultipleChoice;
    }

    public QuestionMultipleAllDTO updateImagens(
            MultipartFile[] files,
            String statement,
            String pointsEnum,
            Long course,
            Character correctAnswer,
            String alternativeA,
            String alternativeB,
            String alternativeC,
            String alternativeD,
            String alternativeE,
            long id) throws IOException {

        QuestionMultipleChoice questionMultipleChoice = questionMultipleChoiceRepository.findById(id).orElse(null);

        if (questionMultipleChoice == null) {
            throw new RuntimeException("Pergunta não encontrada com ID: " + id);
        }

        Course courseSaved = courseRepository.findById(course).orElse(null);

        if (courseSaved == null) {
            throw new RuntimeException("Curso não encontrado com ID: " + course);
        }

        questionMultipleChoice.updateQuestionMultipleChoice(
                new QuestionForm(
                        statement,
                        PointsEnum.fromString(pointsEnum),
                        courseSaved.getId(),
                        correctAnswer,
                        alternativeA,
                        alternativeB,
                        alternativeC,
                        alternativeD,
                        alternativeE
                ),
                courseSaved
        );

        questionMultipleChoice.getImages().clear();

        for (MultipartFile file : files) {
            ImageQuestion imagem = new ImageQuestion();
            imagem.setNome(file.getOriginalFilename());
            imagem.setImagem(file.getBytes());
            imagem.setQuestion(questionMultipleChoice);
            questionMultipleChoice.getImages().add(imagem);
        }

        questionMultipleChoiceRepository.save(questionMultipleChoice);

        return new QuestionMultipleAllDTO(questionMultipleChoice);
    }

    public QuestionResultDTO getQuestionResult() {
        var questionAnswerResult = questionAnswerResultRepository.findAll();

        return new QuestionResultDTO(questionAnswerResult);
    }
}
