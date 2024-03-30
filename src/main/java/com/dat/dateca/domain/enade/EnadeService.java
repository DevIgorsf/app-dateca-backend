package com.dat.dateca.domain.enade;

import com.dat.dateca.domain.question.*;
import com.dat.dateca.domain.student.Student;
import com.dat.dateca.domain.student.StudentRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Year;
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
    private StudentRepository studentRepository;

    public EnadeDTO createEnade(EnadeForm enadeForm) {
        Enade enade = new Enade(enadeForm);
        enadeRepository.save(enade);
        return new EnadeDTO(enade);
    }

    public EnadeDTO updateEnade(Long id, EnadeForm enadeForm) {
        var enadeOptional = enadeRepository.findById(id);

        if( enadeOptional.isEmpty()) {
            throw new EntityNotFoundException("Questão não encontrada");
        }

        Enade enade =  enadeOptional.get();
        enade.updateEnade(enadeForm);
        enadeRepository.save(enade);

        return new EnadeDTO(enade);
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
        
        long indiceSorteado = random.nextLong(enadeIds.size());

        Enade enade = enadeRepository.findById(enadeIds.get((int) indiceSorteado)).get();

        return new EnadeRandDTO(enade);
    }

    public String deleteEnade(Long id) {
        Enade enade = enadeRepository.getReferenceById(id);
        enadeRepository.delete(enade);

        return "Excluido com sucesso";
    }

    public QuestionAnswerDTO answerEnade(Long id, String answer, String registrationNumber) {
        var optionalEnade = enadeRepository.findById(id);
        Student stundent = studentRepository.findByRegistrationNumber(registrationNumber);

        if (optionalEnade.isEmpty()) {
            throw new EntityNotFoundException("Questão do enade não encontrada");
        }

        Enade enade = optionalEnade.get();

        char correctAnswerChar = answer.charAt(0);

        int result =  Character.compare(enade.getCorrectAnswer(), correctAnswerChar);
        if(result == 0) {
            stundent.addPontuacao(enade.getPointsEnum().getKey());
            studentRepository.save(stundent);
            return new QuestionAnswerDTO(enade.getCorrectAnswer(), correctAnswerChar, true);
        }

        return new QuestionAnswerDTO(enade.getCorrectAnswer(), correctAnswerChar, false);
    }

    public Long getEnadeData() {
        return enadeRepository.count();
    }

    public Enade salvarImagens(
            MultipartFile[] files,
            Year ano,
            int number,
            String statement,
            String pointsEnum,
            Character correctAnswer,
            String alternativeA,
            String alternativeB,
            String alternativeC,
            String alternativeD,
            String alternativeE) {
        List<ImageQuestion> imagensSalvas = new ArrayList<>();

        for (MultipartFile file : files) {
            ImageQuestion imagem = new ImageQuestion();
            imagem.setNome(file.getOriginalFilename());
            imagem.setImagem(file.getBytes());

            imagensSalvas.add(imagem);
        }

        var enade = new Enade(
                ano,
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

        imageEnadeRepository.saveAll(imagensSalvas);

        return questionMultipleChoice;
    }

    public List<ImageEnade> getImages(Long id) {
        List<ImageEnade> imageEnadesList = imageEnadeRepository.(id);
        return
    }
}
