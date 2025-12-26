package ru.yandex.practicum.commerce.delivery.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "addresses")
public class DeliveryAddress {
    @Id
    @Column(nullable = false, name = "id")
    private UUID addressId = UUID.randomUUID();

    @Column
    private String country;

    @Column
    private String city;

    @Column
    private String street;

    @Column
    private String house;

    @Column
    private String flat;


}
