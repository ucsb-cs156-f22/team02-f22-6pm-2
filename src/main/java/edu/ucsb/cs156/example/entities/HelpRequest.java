package edu.ucsb.cs156.example.entities;

import javax.persistence.Entity;
import java.time.LocalDateTime;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.Id;
import lombok.Builder;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "helprequest")

public class HelpRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String requesterEmail;
    private String teamId;
    private String tableOrBreakoutRoom;
    private LocalDateTime requestTime;
    private String explanation;
    private boolean solved;
}
