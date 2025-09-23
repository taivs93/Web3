package com.kunfeng2002.be002.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "favorite")
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @Column(name = "user_id")
//    @ManyToOne(fetch = FetchType.LAZY)
//    private User userId;
//
//    @Column(name = "erc20_id")
//    @ManyToOne(fetch = FetchType.LAZY)
//    private Erc20 erc20Id;
}
