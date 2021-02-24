package my.plaground.Domain.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import my.plaground.Domain.CharacterClass;

import javax.persistence.*;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Entity(name = "Characters")
public class CharacterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true, nullable = false)
    private int id;
    private int levelValue;
    private boolean isPlayer;
    private double positionx;
    private double positiony;
    private double hp;
    @Column(name="classId")
    @Enumerated(EnumType.ORDINAL)
    private CharacterClass classId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = true)
    private UserEntity user;
}