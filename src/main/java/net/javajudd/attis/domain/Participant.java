package net.javajudd.attis.domain;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotBlank;

@Entity
@Data
public class Participant {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @NotBlank(message = "Name is mandatory")
    private String name;
    @NotBlank(message = "Initials are mandatory")
    private String initials;
    @NotBlank(message = "Email is mandatory")
    private String email;
    @NotBlank(message = "Company is mandatory")
    private String company;
    private String password;
    private String access;
    private String secret;
}
