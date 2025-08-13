package entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;


@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class StorageItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String itemName;

    private int quantity;
    private double volumePerUnit;

    private LocalDate dateStored;

    @Column(columnDefinition = "NVARCHAR(255)")
    private String note;

    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
}

