package com.hwr.hwrzeiterfassung.controller;

import com.hwr.hwrzeiterfassung.database.controller.LoginController;
import com.hwr.hwrzeiterfassung.database.controller.TimeController;
import com.hwr.hwrzeiterfassung.database.repositorys.DayRepository;
import com.hwr.hwrzeiterfassung.database.repositorys.TimeRepository;
import com.hwr.hwrzeiterfassung.database.tables.Day;
import com.hwr.hwrzeiterfassung.database.tables.Time;
import com.hwr.hwrzeiterfassung.response.models.DateAndListOfTimes;
import com.hwr.hwrzeiterfassung.response.models.DayAndListOfTimes;
import com.hwr.hwrzeiterfassung.response.models.DayOverviewCompact;
import com.hwr.hwrzeiterfassung.response.models.TimeCompact;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping(path = "/correct")
public class CorrectTimeController {
    @Autowired
    private LoginController loginController;
    @Autowired
    private DayRepository dayRepository;
    @Autowired
    private TimeRepository timeRepository;
    @Autowired
    private BookController bookController;
    @Autowired
    private TimeController timeController;


    @GetMapping(path = "/getDayInformationAndTimes")
    public Optional<DayAndListOfTimes> getDayInformationAndTimes(@RequestParam String email, @RequestParam String password, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        loginController.validateLoginInformation(email, password);
        var days = dayRepository.findAllByDateAndHuman_Email(date, email);

        if (days.isEmpty())
            return Optional.empty();

        var day = days.get(0);
        var dayOverviewCompact = new DayOverviewCompact(day.getDate(), day.getPauseTime(), day.getWorkingTimeDifference() + day.getTargetDailyWorkingTime());

        var times = timeRepository.findAllByDay(day);
        if (times.isEmpty())
            return Optional.of(new DayAndListOfTimes(dayOverviewCompact, null));

        List<TimeCompact> timeCompactList = new ArrayList<>();

        for (var time : times)
            timeCompactList.add(new TimeCompact(time.getStart(), time.getEnd(), time.isPause(), time.getNote(), time.getProject()));

        return Optional.of(new DayAndListOfTimes(dayOverviewCompact, timeCompactList));
    }


    @PostMapping(path = "/times")
    public @ResponseStatus
    HttpStatus changeDayTimes(@RequestParam String email, @RequestParam String password, @RequestParam DateAndListOfTimes dateAndListOfTimes, @RequestParam double targetDailyWorkingTime) {
        loginController.validateLoginInformation(email, password);

        var date = dateAndListOfTimes.getDate();
        var timesInput = dateAndListOfTimes.getTimes();

        var days = dayRepository.findAllByDateAndHuman_Email(date, email);
        Day day;
        if (days.isEmpty())
            day = new Day();
        else
            day = days.get(0);

        day.setTargetDailyWorkingTime(targetDailyWorkingTime);

        List<Time> timesWork = new ArrayList<>();
        List<Time> timesPause = new ArrayList<>();

        for (var timeInput : timesInput) {
            if (timeInput.isPause())
                timesPause.add(new Time(timeInput.getStart(), timeInput.getEnd(), timeInput.isPause(), timeInput.getNote(), day, timeInput.getProject()));
            else
                timesWork.add(new Time(timeInput.getStart(), timeInput.getEnd(), timeInput.isPause(), timeInput.getNote(), day, timeInput.getProject()));
        }
        day.setPauseTime(bookController.calculatePauseTime(timesWork, timesPause));
        day.setWorkingTimeDifference((timeController.calculateEntriesTimeInMinutes(timesWork) / (double) 60) - day.getTargetDailyWorkingTime());
        dayRepository.saveAndFlush(day);
        timeRepository.deleteAllByDay(day);
        timeRepository.saveAllAndFlush(timesWork);
        timeRepository.saveAllAndFlush(timesPause);

        return HttpStatus.ACCEPTED;
    }


}
