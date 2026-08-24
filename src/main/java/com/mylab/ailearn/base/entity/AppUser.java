package com.mylab.ailearn.base.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Entity
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 254)
    private String email;

    @Column(name = "password_hash", length = 100)
    private String passwordHash;

    // 创建时间完全由数据库默认值生成，应用只读取，不参与插入和更新。
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
