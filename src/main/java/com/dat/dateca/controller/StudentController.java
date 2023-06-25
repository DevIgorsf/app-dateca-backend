package com.dat.dateca.controller;

import com.dat.dateca.domain.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/aluno")
public class StudentController {

    @Autowired
    private UserService userService;

//    @PostMapping("/cadastrar")
//    public cadastroAluno (@RequestBody )
}
