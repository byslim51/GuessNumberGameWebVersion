package com.example.GuessNumberWeb.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Random;

@Controller
@RequestMapping("/guessNumber")
public class GuessNumberController {
    int playerVote = 0;
    int hiddenNumber = 0;
    boolean gameStatus = false;
    int playerAttemptNumber;

    @GetMapping("/")
    public String mainMenu(@RequestParam(value = "range", required = false) Integer difficult,
                           Model model, HttpServletRequest request, HttpSession session) {
        Random random = new Random();

        if (difficult != null) {
            // Сохраняем сложность, если она передана в запросе
            session.setAttribute("gameDifficult", difficult);
        } else {
            // Если параметр range не передан, то используем сложность из сессии
            Object gameDifficultSession = session.getAttribute("gameDifficult");
            if (gameDifficultSession != null) {
                difficult = (Integer) gameDifficultSession;
            } else {
                difficult = 5; // Значение по умолчанию, если нет в сессии
            }
        }

        // Используем сложность для генерации случайного числа
        hiddenNumber = random.nextInt(difficult) + 1;

        return "mainMenu";
    }

    @GetMapping("/game")
    public String game(Model model, HttpServletRequest request) {
        playerAttemptNumber = 0;
        gameStatus = true;
        return "game";
    }

    @PostMapping("/game/checkPlayerVote")
    public String checkPlayerVote(Model model, HttpServletRequest request, HttpSession session) {
        // Получаем сложность из сессии
        int gameDifficult = 5;
        try {
            if (session.getAttribute("gameDifficult") == null) {
            } else {
                gameDifficult = (Integer) session.getAttribute("gameDifficult");
            }
        }catch (NullPointerException e) {}


        // Получаем голос пользователя
        playerVote = Integer.parseInt(request.getParameter("player_vote"));

        // Проверяем корректность выбора
        if (checkPlayerVote(playerVote, gameDifficult)) {
            model.addAttribute("result", "Пожалуйста, введите число в диапазоне от 1 до " + gameDifficult + ".");
            return "game";
        }

        // Проверка на угадывание числа
        if (playerVote == hiddenNumber) {
            model.addAttribute("playerAttempt", playerAttemptNumber);
            return "end"; // Победа
        } else {
            model.addAttribute("result", "Вы не угадали");
            playerAttemptNumber++;
            return "game"; // Продолжаем игру
        }
    }

    @GetMapping("/setGameDifficult")
    public String setGameDifficult(Model model, HttpServletRequest request, HttpSession session) {
        model.addAttribute("value", session.getAttribute("gameDifficult"));
        return "setGameDifficult"; // Страница настроек
    }

    // Проверка корректности введённого числа
    public boolean checkPlayerVote(int playerVote, int gameDifficult) {
        return playerVote > gameDifficult || playerVote <= 0;
    }
}
