package com.dat.dateca.domain.enade;

import com.dat.dateca.domain.question.*;
import com.dat.dateca.domain.student.Student;
import com.dat.dateca.domain.student.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class EnadeService {

    @Autowired
    private EnadeRepository enadeRepository;

    @Autowired
    private ImageEnadeRepository imageEnadeRepository;

    @Autowired
    private EnadeAnswerResultRepository enadeAnswerResultRepository;

    @Autowired
    private StudentRepository studentRepository;

    public EnadeAllDTO createEnade(EnadeForm enadeForm) {
        Enade enade = new Enade(enadeForm);
        enadeRepository.save(enade);
        return new EnadeAllDTO(enade);
    }

    public EnadeAllDTO updateEnade(Long id, EnadeForm enadeForm) {
        var enadeOptional = enadeRepository.findById(id);

        if( enadeOptional.isEmpty()) {
            throw new EntityNotFoundException("Questão não encontrada");
        }

        Enade enade =  enadeOptional.get();
        enade.updateEnade(enadeForm);
        enadeRepository.save(enade);

        return new EnadeAllDTO(enade);
    }

    public EnadeDTO getEnade(Long id) {
        var enadeOptional = enadeRepository.findById(id);

        if( enadeOptional.isEmpty()) {
            throw new EntityNotFoundException("Questão não encontrada");
        }

        Enade enade = enadeOptional.get();

        return new EnadeDTO(enade);
    }

    public EnadeRandDTO getEnadeAleatoria() {
        List<Long> enadeIds = enadeRepository.findIdsOfEnade();

        if (enadeIds.isEmpty()) {
            throw new EntityNotFoundException("Nenhuma questão do Enade foi encontrada");
        }

        Random random = new Random();
        
        int indiceSorteado = random.nextInt(enadeIds.size());

        Enade enade = enadeRepository.findById(enadeIds.get(indiceSorteado)).get();

        return new EnadeRandDTO(enade);
    }

    public String deleteEnade(Long id) {
        Enade enade = enadeRepository.getReferenceById(id);
        enadeRepository.delete(enade);

        return "Excluido com sucesso";
    }

    public QuestionAnswerDTO answerEnade(Long id, String answer, String registrationNumber) {
        var optionalEnade = enadeRepository.findById(id);
        Student student = studentRepository.findByRegistrationNumber(registrationNumber);

        if (optionalEnade.isEmpty()) {
            throw new EntityNotFoundException("Questão do enade não encontrada");
        }

        Enade enade = optionalEnade.get();
        char correctAnswerChar = answer.charAt(0);
        int result =  Character.compare(enade.getCorrectAnswer(), correctAnswerChar);

        EnadeAnswerResult enadeAnswerResult;
        if(result == 0) {
            enadeAnswerResult = new EnadeAnswerResult(enade, true);
            student.addPontuacao(enade.getPointsEnum().getKey());
        } else {
            enadeAnswerResult = new EnadeAnswerResult(enade, false);
        }

        enadeAnswerResultRepository.save(enadeAnswerResult);
        studentRepository.save(student);

        return new QuestionAnswerDTO(enade.getCorrectAnswer(), correctAnswerChar, false);
    }

    public Long getEnadeData() {
        return enadeRepository.count();
    }

    public EnadeAllDTO salvarImagens(
            MultipartFile[] files,
            int year,
            int number,
            String statement,
            String pointsEnum,
            Character correctAnswer,
            String alternativeA,
            String alternativeB,
            String alternativeC,
            String alternativeD,
            String alternativeE) throws IOException {

        Enade enade = new Enade(
                year,
                number,
                statement,
                PointsEnum.fromString(pointsEnum),
                correctAnswer,
                alternativeA,
                alternativeB,
                alternativeC,
                alternativeD,
                alternativeE);

        enadeRepository.save(enade);

        List<ImageEnade> imagensSalvas = new ArrayList<>();

        for (MultipartFile file : files) {
            ImageEnade imagem = new ImageEnade();
            imagem.setNome(file.getOriginalFilename());
            imagem.setImagem(file.getBytes());
            imagem.setEnade(enade);

            imagensSalvas.add(imagem);
        }

        imageEnadeRepository.saveAll(imagensSalvas);

        return new EnadeAllDTO(enade);
    }

    public EnadeWithImageDTO getImages(Long id) {
        Enade enade = enadeRepository.findById(id).get();

        EnadeWithImageDTO enadeWithImageDTO = new EnadeWithImageDTO(enade);
        return enadeWithImageDTO;
    }

    public List<EnadeAllDTO> getAllEnade() {
        List<EnadeAllDTO> enadeList = enadeRepository.findAll()
                .stream().map(EnadeAllDTO::new).toList();;

        if(enadeList.isEmpty()) {
            throw new EntityNotFoundException("Não há questões cadastradas");
        }

        return enadeList;
    }

    public EnadeAllDTO updateImagens(MultipartFile[] files,
                                     int year,
                                     int number,
                                     String statement,
                                     String pointsEnum,
                                     Character correctAnswer,
                                     String alternativeA,
                                     String alternativeB,
                                     String alternativeC,
                                     String alternativeD,
                                     String alternativeE,
                                     long id) throws IOException {

        Enade enade = enadeRepository.findById(id).orElse(null);

        if (enade == null) {
            throw new RuntimeException("Enade não encontrado com ID: " + id);
        }

        enade.updateEnade(new EnadeForm(
                year,
                number,
                statement,
                PointsEnum.fromString(pointsEnum),
                correctAnswer,
                alternativeA,
                alternativeB,
                alternativeC,
                alternativeD,
                alternativeE
        ));

        enade.getImages().clear();

        for (MultipartFile file : files) {
            ImageEnade imagem = new ImageEnade();
            imagem.setNome(file.getOriginalFilename());
            imagem.setImagem(file.getBytes());
            imagem.setEnade(enade);
            enade.getImages().add(imagem);
        }

        enadeRepository.save(enade);

        return new EnadeAllDTO(enade);
    }

    public EnadePorcentagemDTO getEnadePorcentagem() {
        long questoesFeitas = enadeAnswerResultRepository.count();
        long questoesCorrect = enadeAnswerResultRepository.countCorrect();

        return new EnadePorcentagemDTO(questoesFeitas, questoesCorrect);
    }
}
