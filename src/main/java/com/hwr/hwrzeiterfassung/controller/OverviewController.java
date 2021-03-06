package com.hwr.hwrzeiterfassung.controller;

import com.hwr.hwrzeiterfassung.database.controller.LoginController;
import com.hwr.hwrzeiterfassung.database.repositorys.DayRepository;
import com.hwr.hwrzeiterfassung.response.models.DayOverviewCompact;
import com.hwr.hwrzeiterfassung.response.models.DayOverviewFull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;

/**
 * Controller for the Overview over the day and this times
 */
@Controller
@RequestMapping(path = "/overview")
public class OverviewController {
    @Autowired
    private LoginController loginController;
    @Autowired
    private DayRepository dayRepository;

    /**
     * Complete Overview over the days in the requested period
     *
     * @param email    email for Login validation
     * @param password hashed password for Login validation
     * @param start    start date of the period
     * @param end      end date of the period
     * @return Iterable of Day Overview Full
     */
    @GetMapping(path = "/DayFullOverviewInInterval")
    public @ResponseBody
    Iterable<DayOverviewFull> getDayFullOverviewsInInterval(@RequestParam String email, @RequestParam String password, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {
        loginController.validateLoginInformation(email, password);

        var days = dayRepository.findAllByDateBetweenAndHuman_Email(start, end, email);

        if (days.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No days found for the user in the interval");

        var list = new ArrayList<DayOverviewFull>();
        for (var day : days)
            list.add(day.getDayOverviewFull());

        return list;
    }

    /**
     * compact Overview over the days in the requested period
     *
     * @param email    email for Login validation
     * @param password hashed password for Login validation
     * @param start    start date for the period
     * @param end      end date for the period
     * @return Iterable of Day Compact Overview
     */
    @GetMapping(path = "/DayCompactOverviewInInterval")
    public @ResponseBody
    Iterable<DayOverviewCompact> getDayCompactOverviewsInInterval(@RequestParam String email, @RequestParam String password, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {

        loginController.validateLoginInformation(email, password);

        var days = dayRepository.findAllByDateBetweenAndHuman_Email(start, end, email);

        if (days.isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No days found for the user in the interval");

        var list = new ArrayList<DayOverviewCompact>();
        for (var day : days) {
            var workingTime = day.calculateWorkingTime();
            list.add(new DayOverviewCompact(day.getDate(), day.getPauseTime(), workingTime));
        }
        return list;
    }

    /**
     * average working Hours for the days in the requested period
     *
     * @param email    email for Login validation
     * @param password hashed password for Login validation
     * @param start    start date for the period
     * @param end      end date for the period
     * @return average working hours
     */
    @GetMapping(path = "/AverageWorkingHoursInInterval")
    public @ResponseBody
    double getAverageWorkingHoursInInterval(@RequestParam String email, @RequestParam String password, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {

        loginController.validateLoginInformation(email, password);

        Optional<Double> hours = dayRepository.getWorkingTimeAverageByDateBetweenAndHuman_Email(start, end, email);

        if (hours.isEmpty())
            return 0;
        return hours.get();
    }

    /**
     * average pause for the days in the requested period
     *
     * @param email    email for Login validation
     * @param password hashed password for Login validation
     * @param start    start date for the period
     * @param end      end date for the period
     * @return average pause
     */
    @GetMapping(path = "/AveragePauseInInterval")
    public @ResponseBody
    double getAveragePauseInInterval(@RequestParam String email, @RequestParam String password, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate start, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate end) {

        loginController.validateLoginInformation(email, password);

        Optional<Double> hours = dayRepository.getPauseAverageByDateBetweenAndHuman_Email(start, end, email);
        if (hours.isEmpty())
            return 0;
        return hours.get();
    }

}
