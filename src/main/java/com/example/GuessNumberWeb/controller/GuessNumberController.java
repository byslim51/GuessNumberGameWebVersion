package com.example.GuessNumberWeb.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Controller
@RequestMapping("/guessNumber")
public class GuessNumberController {
    int playerVote = 0;
    int hiddenNumber = 0;
    boolean gameStatus = false;
    int playerAttemptNumber;
    List<Integer> wereNumbers = new ArrayList<>();
    int gameDifficult;

    @GetMapping("/")
    public String mainMenu(@RequestParam(value = "range", required = false) Integer difficult, @RequestParam(value = "action", required = false) String action,
                           Model model, HttpServletRequest request, HttpSession session) {
        Random random = new Random();


        if (!wereNumbers.isEmpty()) {
            wereNumbers.clear();
        }

        if ("confirm".equals(action)) {
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
        } else {
            if (session.getAttribute("gameDifficult") != null) {
                difficult = (Integer) session.getAttribute("gameDifficult");
            } else {
                difficult = 5;
            }
        }

        gameDifficult = difficult;
        model.addAttribute("gameDifficult", gameDifficult);

        // Используем сложность для генерации случайного числа
        hiddenNumber = random.nextInt(difficult) + 1;

        return "mainMenu";
    }

    @GetMapping("/game")
    public String game(Model model, HttpServletRequest request) {
        playerAttemptNumber = 0;
        gameStatus = true;
        model.addAttribute("gameDifficult", gameDifficult);
        return "game";
    }

    @PostMapping("/game/checkPlayerVote")
    public String checkPlayerVote(@RequestParam(value = "result", required = false) String lose, Model model, HttpServletRequest request, HttpSession session) {
//       Проверяем сдался ли игрок
        if ("lose".equals(lose)) {
            model.addAttribute("result", "Вы проиграли! Вы попробовали угадать " + playerAttemptNumber + " раз(а)");
            return "end";
        }

        // Получаем сложность из сессии

//        Текст на случай превышения диапазона числа или ловли исключения
        String textForProblem = "Пожалуйста, введите число в диапазоне от 1 до " + gameDifficult + ".";

        // Получаем голос пользователя
        try {
            playerVote = Integer.parseInt(request.getParameter("player_vote"));
        } catch (NumberFormatException e) {
            model.addAttribute("result", textForProblem);
            model.addAttribute("numbers", wereNumbers);
            model.addAttribute("gameDifficult", gameDifficult);
            return "game";
        }

        // Проверка на повтор цифры
        if (!wereNumbers.contains(playerVote)) {
            // Проверяем корректность выбора
            if (checkPlayerVote(playerVote, gameDifficult)) {
                model.addAttribute("numbers", wereNumbers);
                model.addAttribute("gameDifficult", gameDifficult);
                model.addAttribute("result", textForProblem);
                return "game";
            }

            // Проверка на угадывание числа
            if (playerVote == hiddenNumber) {
                model.addAttribute("result", "Вы выиграли! Вы попробовали угадать " + playerAttemptNumber + " раз(а)");
                return "end"; // Победа
            } else {
                model.addAttribute("result", "Вы не угадали");
                playerAttemptNumber++;
                wereNumbers.add(playerVote);
                model.addAttribute("numbers", wereNumbers);
                model.addAttribute("gameDifficult", gameDifficult);
                return "game"; // Продолжаем игру
            }
        } else { // Если цифра повторилась
            model.addAttribute("result", "Такая цифра уже была введена");
            return "game";
        }
    }

    @GetMapping("/setGameDifficult")
    public String setGameDifficultPage(Model model, HttpServletRequest request, HttpSession session) {
//       Сохранение значение сложности
        if (session.getAttribute("gameDifficult") != null) {
            model.addAttribute("value", session.getAttribute("gameDifficult"));
        } else {
            model.addAttribute("value", 5);
        }
        return "setGameDifficult"; // Страница настроек
    }

    @GetMapping("/howToPlay")
    public String howToPlayPage(Model model, HttpServletRequest request, HttpSession session) {
        return "howToPlay";
    }

    // Проверка корректности введённого числа
    public boolean checkPlayerVote(int playerVote, int gameDifficult) {
        return playerVote > gameDifficult || playerVote <= 0;
    }

}
