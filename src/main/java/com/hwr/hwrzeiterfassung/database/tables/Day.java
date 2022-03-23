package com.hwr.hwrzeiterfassung.database.tables;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@Data
public class Day {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private long id;

    @Column(name = "target_daily_working_time", columnDefinition = "DOUBLE")
    private double targetDailyWorkingTime;
    @Column(name = "day", nullable = false)
    private Date date;


    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "email", nullable = false)
    private Human human;

    @OneToMany(mappedBy = "day")
    private Set<Time> time;

}