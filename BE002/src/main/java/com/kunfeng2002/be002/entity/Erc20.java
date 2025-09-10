package com.kunfeng2002.be002.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "erc20")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Erc20 {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name_token", length = 45)
    private String nameToken;

    @Column(name = "address", nullable = false, unique = true, length = 42)
    private String address;

    @Column(name = "currency", nullable = false, columnDefinition = "TEXT")
    private String currency;

    @Column(name = "decimals")
    private Integer decimals;
}